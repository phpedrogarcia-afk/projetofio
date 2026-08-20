# DATA-MAP — onde cada dado vive, como é protegido, quem o toca

**Estado examinado:** HEAD `9cfd5f1` · **Evidência:** `E4` (código) + `E2` (docs `06-DATA-MODEL.md`, `08-PRIVACY-SECURITY.md`).

Cada linha da tabela responde a: origem · formato · armazenamento · criptografia · lifecycle · quem lê · quem escreve · quem apaga · backup · export · índice · risco.

## 1. Entry plaintext (o coração)

| Dimensão | Realidade no código |
|---|---|
| Origem | teclado do usuário (Home BasicTextField) ou import Markdown/Texto |
| Formato | `String` Kotlin, `ContentFormat.PLAIN_TEXT` |
| Armazenamento | **nunca persistido em texto plano** — só em memória (View/ViewModel/service, transient) |
| Criptografia | `AesGcmContentCipher.seal()` → envelope BLOB (IV 12B \|\| ciphertext \|\| GCM tag 16B); AAD = `associatedData(kind, recordId, schemaVersion)` binda o cifrado ao tipo de registro, ao ID e à versão do schema |
| Lifecycle | memória → cifrado → Room; re-aberto: Room → decrypt → memória → render |
| Leitores | `RoomFioRepository.decryptEntry`, `FioService` (edit/export), `LocalSearchService` (scan M4), UI |
| Escritores | `FioService.saveEntry/editEntry`, `ImportService.commit`, `FioService.autosaveDraft` |
| Apagadores | `purgeEntry` (row DELETE), `purgeExpired` (cascade por purge_after) |
| Backup | `allowBackup="false"` no manifest; ADR-035 |
| Export | export v1.0 plaintext por escolha explícita (SAF, fora do app) — ADR-018/046 |
| Índice | **não existe índice persistente** (FTS5 rejeitada; `SEARCH-PRIVACY-ARCHITECTURE.md` Option A) |
| Risco | **R0 se vazado**; mitigado por boundary: plaintext só em sessão; finding P0 da Missão 1 (surrogate halves → `0x3F` em `seal()`) corrigido |

## 2. Encrypted Entry (contentEnvelope)

Room `entries.content_envelope` BLOB. Chave AES derivada/guardada via `AndroidKeystoreKeyProvider` (Keystore system key, hardware-backed quando disponível; ADR-035). Sem nuvem, sem transporte. Risco residual: backup ADB desativado (`allowBackup=false` + dataExtractionRules) — perda do dispositivo = perda das chaves = perda das entradas (**R0 de disponibilidade; mitigação documentada = export antes de trocar de aparelho**).

## 3. Draft (rascunho em andamento)

Tabela `drafts`, **singleton `singleton_slot = 1`** (ADR-029 — exatamente um draft criptografado). Mesma envelope GCM. Contrato D12: `insertEntryAndClearDraft` @Transaction; autosave substitui o envelope antigo; insert falho **não apaga** o draft (testado em `DraftSurvivesFailedInsertTest`). Apaga-se em save exitoso. Risco: R1 (perda do rascunho) — coberto por testes.

## 4. Return history (ciclos de devolução)

`returns`: id, entry_id (FK), algorithm (`TIME`), algorithm_version, state (SELECTED→SCHEDULED→NOTIFIED→OPENED/DISMISSED/EXPIRED/CANCELLED), window_start/end, age_bucket, cancel_reason. **Factual, não interpretativo.** Leitores: busca M4 (`loadReturnsForEntry` — read-only, alimenta "Já voltou N vez(es)"), TimeReturnsService (reconcile), UI (ReturnScreen, Arquivo). Escritores: TimeReturnsService, ImportService (cancel reasons). Apaga-se: `cancelExternalReferences` em rollback de import (IMPORT_ROLLBACK). Sem criptografia de conteúdo (não contém texto). Risco R2/R3: crescimento unbounded — cap de 7 dias já limita eventos ativos; histórico completo cresce linearmente (monitorar em escala; sem índice adicional hoje).

## 5. Search query / tokens / scores

**Não persistidos.** Flow runtime no `FioViewModel` (`searchTermsFlow`), debounce 300 ms, resultado `SearchResult` em memória. Não há "Pesquisas recentes", não há log, não há analytics de conteúdo. Confirmado por grep no red team da Missão 4 e contrato explícito em `FioViewModel`. Risco: R1 potencial de leakage via app switcher/recents — mitigado por PrivacyCover (ADR-031); gap documentado em `KNOWLEDGE-GAPS.md`.

## 6. Search snippet (retorno)

Texto original copiado do plaintext em memória durante o scan, cortado pelo tokenizer (original, não normalizado). UI exibe com `SelectionContainer`. Não persiste. Herda o boundary do plaintext (R0 em sessão; fora de sessão não existe).

## 7. Embedding (M4 — só protótipo)

**Não existe em produção.** `FakeEmbeddingProvider` é determinístico (hash → vetor unitário), zero pesos. RESEARCH-LOG: `embeddinggemma-300m` TFLite candidato; se embarcar, arquivo de embeddings **herda o boundary de crypto** do contentEnvelope (mesma keystore) e é dado sensível (ADR-007); reindex idempotente; purge em deleção permanente. Estado: `E2` research / `E3` benchmark sintético / **`E5` hardware benchmark ausente** → gate `FIO-Pxx`.

## 8. Sealed state (notas seladas)

**Ainda não existe em EntryEntity.** O contrato M4 é preparado por lambda `isSealed: (id) -> Boolean = { false }` no `LocalSearchService`; `SealedSearchBehavior` (HIDDEN / COUNT_ONLY) e `SearchScope` (ALL/EXCLUDE_SEALED/SEALED_ONLY) já modelados no domínio. Quando o campo existir, threat model em `docs/search/SEARCH-SEALED-THREAT-MODEL.md` é pré-requisito; decisão de visibilidade da contagem = **DECISION REQUIRED (D-1)**.

## 9. Notification metadata

Canal "Lembranças do Fio" `IMPORTANCE_LOW` (ADR-011 — uma notificação genérica, sem lembrete). Notificação contém **sem texto íntimo** (só título genérico; intent abre MainActivity → ReturnScreen). `NotificationManager` + `POST_NOTIFICATIONS` (API 33+). Metadata persistida: none além do state em `returns`. Risco R2: notificação não expõe conteúdo; mitigação = canal low + sem body (verificado em código).

## 10. Export

Export v1.0 (ADR-046): diretório com `README.md`, `manifest.json` (versão, UTC timestamp, timezone do dispositivo, contagem, SHA-256 per-entry), `entries/YYYY/YYYY-MM-DD_<opaque-id>.md` + `.txt`, `deleted/` quando aplicável. Body checksum SHA-256 (sem rodapé). Via SAF — o usuário escolhe o destino; o app não copia para lugar nenhum. **Export não modifica a origem** (invariante). Riscos: R0 se o usuário guarda o export em nuvem pessoal (escolha consciente, não do app); integridade = checksum documentado.

## 11. AppSettings

Singleton `id = 1` em `app_settings`: return_consent_state, returns_paused_at, app_lock_mode, privacy_cover_enabled, analytics_enabled, quiet_hours (start/end minute), notification_permission_observed, schema_version. Sem dado de conteúdo. Escrito só via SettingsScreen/FioService. Backup proibido (allowBackup=false).

## 12. Import batch / fingerprint

`import_batches` + `import_batch_items` (COMMITTED/ROLLED_BACK; IMPORTED/ROLLED_BACK/EDITED_EXCLUDED). `ImportFingerprint` (HMAC determinístico do conteúdo+fonte) em `import_fingerprint_envelope` BLOB — dedupe de re-importação. Rollback idempotente (ADR-039). Fingerprint é derivado de conteúdo sensível → tratado como sensível (não exposto na UI além do preview em memória).

## 13. Resumo de superfície de ataque por dado

| Dado | Em disco? | Criptografado? | Em memória? | Efêmero? | Envio à rede? |
|---|---|---|---|---|---|
| Entry plaintext | não | — | sim (sessão) | sim | nunca |
| contentEnvelope | sim (Room) | AES-256-GCM + AAD | — | não | nunca |
| Draft envelope | sim (Room) | idem | — | não | nunca |
| Return history | sim (Room) | não (sem conteúdo) | — | não | nunca |
| Search query/tokens/scores | não | — | sim (runtime) | sim | nunca |
| Embeddings (M4) | não (hoje) | — | fake só | — | nunca |
| Sealed flag | não existe ainda | — | — | — | — |
| Notification metadata | não | — | transient | sim | nunca |
| Export | só via SAF, escolha do usuário | não (plaintext por design) | — | não | nunca (local) |
| AppSettings | sim (Room) | não (sem conteúdo) | — | não | nunca |
