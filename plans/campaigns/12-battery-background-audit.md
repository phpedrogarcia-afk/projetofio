# Campaign 12 — Battery / Background (Doze) Audit

**Prioridade:** P2 · **Método:** auditoria estática de código + manifest (sem AVD no sandbox; WorkManager não é testável unitariamente sem emulador, mas o contrato é fino e verificável linha a linha) · **Resultado:** VERDE — zero achados P0–P2; 2 recomendações P3.

## O que o app faz em background

O Fio tem exatamente **uma** superfície de background: `WorkManagerReturnScheduler` (AndroidTimeReturns.kt), que agenda **um único** `OneTimeWorkRequest` com `setInitialDelay` até a próxima janela de devolução permitida. Não há `PeriodicWorkRequest`, `AlarmManager`, `BroadcastReceiver` de BOOT, nem work em cadeia.

## Achados (todos verificados linha a linha)

| # | Item | Veredito | Evidência |
|---|------|----------|-----------|
| 1 | `setInitialDelay` não-exact | **Bom** — work não-exact é deferível pelo sistema; o Android pode movê-lo para a maintenance window do Doze, exatamente o comportamento desejado para um lembrete pessoal silencioso. Não usa `setExact`/`setAndAllowWhileIdle`. | AndroidTimeReturns.kt:33-36 |
| 2 | Manifest REMOVE `FOREGROUND_SERVICE` e `SystemForegroundService` | **Bom** — decisão explícita documentada no próprio manifest ("Fio never promotes a Return to foreground work"). O worker nunca roda como foreground; sem bateria de notificação persistente. | AndroidManifest.xml:7-13, 33-36 |
| 3 | `ExistingWorkPolicy.REPLACE` em work único | **Bom** — nunca acumula trabalhos pendentes; cada nova reconciliação substitui o agendamento anterior. | AndroidTimeReturns.kt:38 |
| 4 | Sem receiver `BOOT_COMPLETED` | **Observação aceitável** — após reboot o agendamento é recriado automaticamente: `TimeReturnsService.reconcile()` agenda via `scheduler.schedule(due)` em qualquer interação do usuário (abertura do app, edit, import). O único cenário real é uma devolução perdida entre reboot e próxima abertura, tratada pelo engine como janela expirada (→ novo agendamento). Perda aceitável para um app de lembrete pessoal silencioso. | — |
| 5 | Worker com retry limitado (runAttemptCount < 3) e then failure | **Bom** — evita loops de retry infinitos que drenariam bateria sob falha persistente; o fallback está no reconcile (retryAt). | AndroidTimeReturns.kt:57-62 |
| 6 | Sem network em background | **Bom** — app sem acesso à rede (usesCleartextTraffic=false + network-security-config sem domínios), nada é feito em background via rede. | AndroidManifest.xml:18 |
| 7 | `delayMillis.coerceAtLeast(0)` | **Bom** — protege work imediato infinito em caso de relógio alterado (o engine recalcula a janela). | AndroidTimeReturns.kt:33 |
| 8 | Notification IMPORTANCE_LOW + setShowBadge(false) | **Bom** — não acorda o usuário; Doze-safe. | AndroidTimeReturns.kt:79-83 |

## Recomendações (P3 — evolução futura, sem ação imediata)

1. **BOOT receiver opcional:** se o fundador quiser garantia de devolução pós-reboot, registrar um `BOOT_COMPLETED` receiver que chama `reconcile()`. Trade-off: um wake-up a mais por boot (baixo custo, ~10ms). Documento isso como finding aberto até decisão de produto.
2. **Constraints explícitas no WorkRequest:** adicionar `.setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())` evita rodar reconcile com bateria <15%. Hoje o defer natural do Doze já resolve na prática, mas a constraint é gratuita.

## Conclusão

O app é **Doze-friendly por design**: um único work não-exact, sem foreground, sem alarmes exact, sem receiver persistente, sem rede. Comportamento compatível com "lembrete pessoal silencioso" — nenhuma ação corretiva P0–P2 exigida. Os dois itens P3 ficam como recomendações abertas para o fundador.
