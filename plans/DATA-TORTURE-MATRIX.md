# Data Torture Matrix — ProjetoFio MANUS MISSION 1

**Branch de execução:** `integration/manus-rehearsal-20260817`
**Propósito:** matriz consolidada das campanhas de tortura, por domínio, com payloads, critério e resultado. Esta matriz documenta o que foi torturado, o que foi encontrado e o que permanece como decisão de produto.

---

## 1. Domain e encoding (campanha 1 — CONCLUÍDA)

| # | Payload / cenário | Critério | Resultado |
|---|---|---|---|
| 1 | Surrogate half isolado (`\uD83D` sem par) | Nunca corromper em silêncio | **FINDING A (P0)**: `String.toByteArray()` substituía por 0x3F antes da criptografia → fix `CryptoFailure.InvalidPlaintext` com rejeição explícita no `seal()` |
| 2 | Emoji pareado, astral (U+1F600…U+1FAFF), skin tones, ZWJ sequences | Round-trip bit-idêntico | PASS |
| 3 | 10.000 strings aleatórias (appendCodePoint, 0–64 code points) | Round-trip 100% | PASS (strings válidas nunca rejeitadas) |
| 4 | `isBlank()` com `\u200B\u200E\u00A0` (zero-width + BIDI + NBSP) | Documentar comportamento | FINDING B (P4): aceito como conteúdo não-blank — decisão de produto aberta |
| 5 | Controle C0/C1, DEL, bidi overrides (RLI/LRI/PDI), RTL marks | Preservação byte a byte | PASS |
| 6 | Content vazio, só whitespace, só pontuação | Preservação | PASS |
| 7 | UTF-8 4-byte max (U+10FFFF), surrogates legítimos pareados | Preservação | PASS |
| 8 | Checksum SHA-256 (ADR-046) sensível a 1 byte | Detectar qualquer variação | PASS |

## 2. Criptografia (campanha 2 — CONCLUÍDA)

| # | Domínio | Critério | Resultado |
|---|---|---|---|
| 1 | `AesGcmContentCipher` — nonce aleatório por envelope (IV gerado em `seal`) | Nunca reusar nonce com a mesma key | PASS (verificado por leitura de código + `AesGcmContentCipherTest` existente) |
| 2 | AAD vinculando kind/id/schema | Tampering estrutural detectado | PASS |
| 3 | Keystore AndroidKeyStore (AES-256-GCM) | Key protegida por hardware | PASS (por leitura de código) |
| 4 | Plaintext hunt: `Log.`, `printStackTrace`, `toString` de envelopes, clipboard, SharedPreferences, cache, exceções | Zero leaks em produção | PASS — zero ocorrências |
| 5 | Notificação (`AndroidTimeReturns`) | Título fixo "Algo seu voltou." sem conteúdo | PASS |
| 6 | Cleartext network | Desativado | PASS |
| **Entregável:** `docs/security/CRYPTO-REVIEW-PACKET.md` | | | |

## 3. Database / migrations (campanha 3 — CONCLUÍDA)

| # | Cenário | Critério | Resultado |
|---|---|---|---|
| 1 | Seed schema-2 realista (500 entries, 20 soft-deletados, drafts, settings, 25 returns) → Room aplica `MIGRATION_2_3` | Sem perda de dados | PASS (`Migration2To3Test`) |
| 2 | Envelopes criptografados byte-idênticos pré/pós migração | Round-trip criptográfico | PASS |
| 3 | Invariante `deleted_at ⇔ purge_after` preservada | Consistência soft-delete | PASS |
| 4 | Novos campos (returnMode, lastReturnedAt, returnCount, importBatchId) | Null/default corretos | PASS |
| 5 | Defaults de `AppSettings` (quietHours 21h→8h, consent NOT_CONFIGURED) | Preservados | PASS |
| 6 | Estrutura física consistente com `schema-3.json` (auto-verify) | Íntegra | PASS |
| 7 | Robolectric `onDowngrade` não reproduz `IllegalStateException` | Documentado no teste | KNOWN LIMITATION (não afeta device real) |

## 4. Returns engine (campanha 4 — CONCLUÍDA)

| # | Cenário | Critério | Resultado |
|---|---|---|---|
| 1 | Replay determinístico (RNG fixo) | Decisões idênticas em execuções repetidas | PASS |
| 2 | Propriedade: 100 sementes — entrega nunca em quiet-hours [21h, 08h) | 0 violações | PASS |
| 3 | Propriedade: 100 sementes — entry selecionada nunca em histórico | 0 re-seleções | PASS |
| 4 | Consent (NOT_CONFIGURED/PAUSED) como gate absoluto | Silent imediato | PASS |
| 5 | Pending states (SELECTED/SCHEDULED/NOTIFIED) silenciam; terminais não | Correct gates | PASS |
| 6 | Frequency cap 7d — boundary `>` estrito no `createdAt` | Comportamento pinned | PASS |
| 7 | Pools vazios/deletados/NEVER | Silent apropriado (BOOTSTRAP_WAIT/NO_ELIGIBLE_ENTRY) | PASS (ordem observada pinned) |
| 8 | Limites de buckets: 6–2000 dias | Bucket canônico exato | PASS |
| 9 | DST forward-gap (America/New_York março) | Delivery em local time válido | PASS |
| 10 | Janela permitida de 1h ([08h, 09h)) e cruzando meia-noite ([23h, 01h)) | Entrega dentro da janela | PASS (50 sementes cada) |
| 11 | Bootstrap por contagem (1→30d, 2→14d, 4+→7d) | Gates corretos | PASS |
| 12 | Varredura 10.000 avaliações horárias (horizonte 730d, 40 entries) | 0 violações, >0 seleções | PASS |

## 5. Privacy boundary & backup Android (campanha 5 — CONCLUÍDA)

| # | Domínio | Critério | Resultado |
|---|---|---|---|
| 1 | `android:allowBackup="false"` | Bloqueio de backup legado | PASS |
| 2 | `data-extraction-rules.xml` — todos os domínios excluídos em cloud-backup **e** device-transfer | Nada vai para nuvem nem para device transfer | PASS |
| 3 | `backup_rules.xml` — todos os domínios excluídos (full-backup legado) | Defesa em profundidade | PASS |
| 4 | `FLAG_SECURE` em `MainActivity.onCreate` (incondicional) | Sem thumbnail em recents, sem screenshot | PASS |
| 5 | Clipboard | Nenhum `ClipboardManager` no código | PASS |
| 6 | SAF export — sem arquivo temporário no app dir; mensagem de erro já declara "nenhuma cópia adicional foi mantida" | Sem rastro residual | PASS |
| 7 | Logcat em produção | Zero `Log.` | PASS |
| 8 | Receivers/serviços exported | Nenhum receiver; apenas Activity LAUNCHER | PASS |
| 9 | Trade-off conhecido (P4): teclado do sistema pode oferecer sugestões de texto para o `BasicTextField`/`OutlinedTextField` padrão | Documentado; mudança exigiria UX trade-off → decisão de produto | DECISION REQUIRED (leve) |

---

## 6. Time torture (campanha 6 + 16 — CONCLUÍDA)

| # | Cenário | Critério | Resultado |
|---|---|---|---|
| 1 | Fusos extremos: UTC±14 e +5:45 (Kathmandu) | Delivery sempre em local time válido | PASS |
| 2 | DST fall-back (America/New_York, 01/11/2026) | Sem hora ambígua em janela | PASS |
| 3 | Epoch edges: 1970-01-01, 2038-01-19 (Y2038), 3000-01-01 | Sem overflow/underflow | PASS |
| 4 | Leap day (29/02) em ano bissexto e não-bissexto | Buckets corretos | PASS |
| 5 | Sweep de sementes (propriedade) + varredura 10.000 avaliações horárias | 0 violações de quiet-hours | PASS |

## 7. Storage Failure (campanha 7 — CONCLUÍDA)

| # | Cenário | Critério | Resultado |
|---|---|---|---|
| 1 | `ExportCoordinator` sob `IOException` (disco cheio) | `ExportOutcome.FAILED` genérico, sem crash | PASS (`StorageFailureTest`) |
| 2 | Permission-denied do SAF | FAILED, nada parcial escrito | PASS |
| 3 | `CancellationException` (usuário fecha o app durante export) | Propaga, sem estado corrompido | PASS |
| 4 | URI nula do SAF | Falha controlada, sem NPE | PASS |
| 5 | Document build falha | `ExportOutcome.FAILED`, nenhum arquivo de destino | PASS |

## 8. Export Round-Trip (campanha 8 — CONCLUÍDA)

| # | Cenário | Critério | Resultado |
|---|---|---|---|
| 1 | Checksum SHA-256 (ADR-046) reproduzido por implementação independente | Idêntico ao do `FioService` | PASS (`ExportRoundTripTest`) |
| 2 | 1 byte alterado no documento | Hash muda | PASS |
| 3 | Surrogate halves do finding A presentes no conteúdo | Preservados no export (sem substituição) | PASS |
| 4 | Markdown e plain text | Ambos com checksum válido | PASS |

## 9. Dependencies (campanha 9 — CONCLUÍDA)

| # | Domínio | Critério | Resultado |
|---|---|---|---|
| 1 | CVEs conhecidos (Room, coroutines, lifecycle, Kotlin) | Nenhum ativo nas versões usadas | PASS |
| 2 | Transitivas antigas (kotlin-stdlib 1.3/1.6, coroutines 1.8) | Forçadas pela transitiva; sem exposição direta | FINDING (P4) — documentado |
| 3 | `biometric:1.1.0` (legado) | Avaliar `androidx.credentials` | RECOMENDAÇÃO (P3) |
| 4 | `fragment` (só `FragmentActivity`) | Removível | RECOMENDAÇÃO (P4) |

## 10. Static quality (campanha 10 — CONCLUÍDA)

| # | Domínio | Critério | Resultado |
|---|---|---|---|
| 1 | `lintDebug` Android | 0 erros, warnings mínimos | PASS (0 erros após fix) |
| 2 | `DefaultLocale` em `quietHoursLabel` | `Locale.ROOT` no format numérico | FIX APLICADO |
| 3 | Recursos não usados (`ic_thread`, `write_prompt`) | Removidos | FIX APLICADO |
| 4 | ktlint/detekt | Não configurados | RECOMENDAÇÃO (P3 — evolução futura) |

## 11. Performance 10k+ (campanha 11 — CONCLUÍDA)

| # | Caminho | Cenário | Resultado |
|---|---|---|---|
| 1 | Scan de entries ativos | 10k linhas (9k ativos + 1k soft-deletados) | <0.32s, ordem correta end-to-end |
| 2 | Scan de deletados | 1k soft-deletados | PASS, ordem correta |
| 3 | `purgeExpired` (GC) | 1.000 expirados | <0.22s, idempotente |
| 4 | `rollbackImport` | 500 itens + 500 returns pendentes | <0.5s, sem blowup quadrático |
| 5 | Estabilidade de Flow | 1 insert → 1 re-emissão; 1 soft-delete → 1 re-emissão | PASS (Room 1× por mudança) |

## 12. Battery / Doze (campanha 12 — CONCLUÍDA)

| # | Domínio | Critério | Resultado |
|---|---|---|---|
| 1 | Superfície de background | Único work não-exact (`setInitialDelay`), sem alarmes exact | PASS |
| 2 | Manifest | `FOREGROUND_SERVICE` e `SystemForegroundService` removidos; sem receiver BOOT | PASS |
| 3 | Retry | Limitado a 3 tentativas | PASS |
| 4 | Rede em background | Inexistente (`usesCleartextTraffic=false`, sem domínios) | PASS |
| 5 | Re-schedule pós-boot | Recriado por interação do usuário (`reconcile`); janela expirada reagenda | ACCEPTABLE (finding aberto P3) |

## 13. Accessibility (campanha 13 — CONCLUÍDA, estática)

| # | Critério | Resultado |
|---|---|---|
| 1 | `heading()`, `contentDescription`, `liveRegion = Polite`, `mergeDescendants` nas 4 seções | PASS |
| 2 | `contentDescription = null` apenas em decorativos | PASS |
| 3 | Piso de toque ≥48dp | PASS |
| 4 | TalkBack real (AVD) | RECOMENDAÇÃO (P3 — checklist 10 min) |

## 14. Visual Regression (campanha 14 — CONCLUÍDA, estática)

| # | Token v1 | Resultado |
|---|---|---|
| 1 | ReturnScreen: marfim, Fraunces na data, terracota sem vermelho puro, piso 48dp | PASS — 100% aderente |
| 2 | Screenshot de referência em CI | RECOMENDAÇÃO (P4) |

## 15. Error UX (campanha 15 — CONCLUÍDA)

| # | Critério | Resultado |
|---|---|---|
| 1 | Toda ação com `onFailure`/`catch`; `CancellationException` nunca engolida | PASS |
| 2 | Pill terracota com `liveRegion Polite` | PASS |
| 3 | Labels de issues de import em pt-BR; garantia "nada foi apagado" | PASS |
| 4 | Retry affordance no pill | RECOMENDAÇÃO (P3) |

---

## Próximas campanhas na fila (`plans/HARDENING-QUEUE.md`)

Todas as 17 campanhas estão **CONCLUÍDAS** (16 e 17 cobertas pelas campanhas 6 e 11 respectivamente). Pendências finais de relatórios: `MANUS-HARDENING-FINAL.md` (raiz do repositório).
