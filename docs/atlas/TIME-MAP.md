# TIME-MAP — como o Fio lida com o tempo

**Status:** REFERENCE. Estado temporal examinado em 2026-08-20. UI temporal:
`ui/home/HomeScreen.kt`; detalhe: `ui/entry/EntryDetailScreen.kt`; estado/serviço:
`FioViewModel.kt`, `FioService.kt`, `Models.kt`, `TimeReturnEngine.kt`.

## 1. Os conceitos canônicos

| Conceito | Representação real | Significado |
|---|---|---|
| Someday | UI local `ReturnPolicy.Someday`; `saveEntry()` cria Entry default `ELIGIBLE` | funciona apenas porque coincide com o default do domínio |
| Scheduled (calendário) | `ReturnPolicy.InPeriod(n)` / `OnDate` existem somente no estado Compose e não chegam a `saveEntry()` | **P0: não persistido e não honrado pelo motor; não descrever como funcional** |
| Resting / paused | `returns_paused_at` + `ReturnConsentState.PAUSED` | engine retorna `Silent(CONSENT_DISABLED)`; attempts existentes cancelados |
| Never ao guardar | UI local `ReturnPolicy.Never`; `saveEntry()` ainda cria Entry default `ELIGIBLE` | **P0: opção visual não aplicada** |
| Never após uma Return | `TimeReturnsService.neverReturn(attemptId)` → `return_mode = NEVER` | funcional apenas no fluxo de Return já aberta |
| “Devolver para agora” no detalhe | `NoteScreen.returning/returned` em `remember`; nenhuma chamada de domínio | **P0: encenação local, sem tentativa/histórico; remover até contrato real** |
| Data-Âncora | `original_created_at` + `original_time_zone` na Entry | a data que o usuário associa ao momento vivido (não a data de gravação); usada no export e no display |
| Return history | `ReturnEntity` (state machine: SELECTED→SCHEDULED→NOTIFIED→OPENED/DISMISSED/EXPIRED/CANCELLED) | evidência factual "já voltou N vezes" (usada pela busca M4) |
| Quiet hours | `quiet_hours_start_minute` (default 1260 = 21:00) / `end_minute` (default 480 = 08:00) | janela proibida; **com wrap de meia-noite** o allowed window cruza o dia |
| Timezone | `ZoneId.systemDefault()` no motor; `originalTimeZone` por entrada | decisões em hora local; display em IANA da entrada |
| DST | `chooseAllowedDelivery` trabalha com `LocalDate`/`LocalTime` → `ZonedDateTime` → Instant; transições tratadas pela API java.time (não há ajuste manual no código; gaps documentados em `EngineTortureTest` com fusos extremos) |
| Reboot | WorkManager persiste `OneTimeWorkRequest`; `TimeReturnsService.reconcile()` pós-boot cancela attempts de entradas que perderam elegibilidade (delete, never, import rollback) | sem alarmes perdidos silenciosos |
| WorkManager | `WorkManagerReturnScheduler`: `OneTimeWorkRequest` com delay até o `Instant` alvo; `UNIQUE_WORK_NAME = fio-time-return-opportunity-v1`; `ExistingWorkPolicy.REPLACE`; tag `fio-time-return` | exato? **inexact por padrão Android 12+** para delay longos; acceptable per ADR-038 |
| Notifications | canal "Lembranças do Fio" `IMPORTANCE_LOW` (sem som padrão disruptive); uma notificação genérica, sem lembrete repetido (ADR-011) | |

## 2. Precedência de regras (ordem real do `TimeReturnEngine.evaluate`)

```text
1. consent ≠ ENABLED            → Silent(CONSENT_DISABLED)
2. pending return ativo         → Silent(PENDING_RETURN)          (uma por vez)
3. cap 7 dias (history non-CANCELLED > now-7d) → Silent(FREQUENCY_CAP)
4. bootstrap wait (0/30/60/90 dias conforme contagem de entradas)
                                  → Silent(BOOTSTRAP_WAIT)
5. nenhuma entry ELIGIBLE       → Silent(NO_ELIGIBLE_ENTRY)
6. sorteia bucket de idade (uniforme, nunca-retornadas preferidas),
   sorteia entry dentro do bucket
7. chooseAllowedDelivery: próxima janela fora das quiet hours (wrap meia-noite)
8. windowStart = deliveryAt; windowEnd = deliveryAt + 24h
9. schedule(at=windowStart) via WorkManager
```

Silenciar nunca é falha: é política. O motor só responde; o scheduler só agenda; o serviço reconcilia.

## 3. Estados de devolução e seus gatilhos

| State | Quem escreve | Gatilho |
|---|---|---|
| SELECTED | reconcile() | engine escolheu a entry |
| SCHEDULED | reconcile() | WorkManager agendado |
| NOTIFIED | post() | notificação entregue |
| OPENED | openReturn() | usuário tocou |
| DISMISSED | dismissReturn() | usuário dispensou |
| EXPIRED | reconcile() pós-windowEnd | janela passou |
| CANCELLED | cancelIneligible() / rollback | pause, delete, never, superseded, ineligível, import rollback |

## 4. O que o tempo NÃO faz (limites reais)

O Fio não repete notificações após ignorar (ADR-011). Não há "On This Day" nem aniversários de entrada. Não há reminders recorrentes (proibido por princípio: sem produtividade disfarçada). Não há janelas de entrega múltiplas por evento (uma janela de 24h). O cap de 7 dias é hardcoded no motor. O bootstrap espera por contagem de entradas (0/30/60/90). Timezone do motor é a do dispositivo no momento da decisão; a Data-Âncora preserva a zona original da memória.

## 5. Gaps de tempo (para KNOWLEDGE-GAPS)

1. **P0 ReturnPolicy:** períodos, data e Nunca ao guardar são UI-only. ADR-043 define o contrato desejado, não evidência de implementação.
2. DST na **janela de 24h**: um `deliveryAt.plus(24h)` pode cair em hora "inexistente" em spring-forward; java.time resolve (offset fixo), mas o comportamento de notificação às 2h30 no dia do salto não foi testado em aparelho (`E5` missing; `EngineTortureTest` cobre fusos extremos em lógica).
3. Exact alarms: o app usa inexact WorkManager; para um app de "reencontro" isso é aceitável hoje; se um dia o fundador quiser entrega em hora exata (ex.: o dia exato do calendário escolhido), precisaria `SCHEDULE_EXACT_ALARM` + decisão (ADR novo) — **não construir agora**.
4. Reboot sem abertura do app: WorkManager reexecuta, mas `reconcile()` só roda quando o processo sobe; entre reboot e primeira abertura há um hiato em que nenhuma devolução acontece (documentado, aceitável).
