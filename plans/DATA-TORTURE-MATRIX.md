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

## Próximas campanhas na fila (`plans/HARDENING-QUEUE.md`)

| Ordem | Campanha | Prioridade |
|---|---|---|
| 6 | Time Torture (fusos extremos, DST, epoch edges) | P1 |
| 7 | Storage Failure (SAF, disco cheio, permissões) | P1 |
| 8 | Export Torture + Round-Trip (SHA-256 ADR-046) | P1 |
| 9–17 | Dependencies, static quality, performance, battery, accessibility, visual regression, error UX, fuzz, large history | P2–P4 |
