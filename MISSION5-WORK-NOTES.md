# MISSION 5 — FIO MASTER ATLAS — notas de trabalho (não entregar)

## Estado do repositório (verificado §1)
- Branch de trabalho: `research/manus-fio-master-atlas-20260819`, criada de HEAD 9cfd5f1 (`integration/manus-search-20260819`, Mission 4 completa).
- `main` == `origin/main` == a4fc835 "Genesis: initial ProjetoFio repository snapshot" — intocada. Nunca mergear.
- PRs abertos: #3 `feature/design-ux-v1` (OPEN, design source of truth), #1 `codex/v0-time-only-checkpoint` (DRAFT).
- Working tree limpa. Testes: **134 unitários verdes, 0 falhas** (28 up-to-date).
- Não publicar, não release.

## Estrutura do repositório (radiografia A1)
```
Raiz: AGENTS.md AGENTS/ README.md CONTRIBUTING.md CHANGELOG.md SECURITY.md PROJECT-MANIFEST.md
      releases/v0.1/ releases/v0.1.0-validation/ .agent/
docs/: 01-PRODUCT.md(184) 02-PRINCIPLES.md(130) 03-UX.md(296) 04-FEATURES.md(130)
       05-ARCHITECTURE.md(368) 06-DATA-MODEL.md(414) 07-RETURNS-ENGINE.md(422)
       08-PRIVACY-SECURITY.md(410) 09-ANALYTICS-EXPERIMENTS.md(395) 10-ROADMAP.md(405)
       11-FOUNDER-VISION.md(220) DECISIONS.md(787) export-format.md(52)
docs/design/: 01-auditoria-ux, 02-arquitetura-navegacao, 03-design-system, 04-fluxos-microinteracoes, 05-plano-implementacao, 06-guia-artefatos, README-FIO-DESIGN, mockups/, wireframes/
docs/search/: SEARCH-ARCHITECTURE.md, SEARCH-PRIVACY-ARCHITECTURE.md, SEARCH-SEALED-THREAT-MODEL.md
docs/security/: CRYPTO-REVIEW-PACKET.md
plans/: PROJECT-STATE.md, SEARCH-EXECUTION-QUEUE.md, NEXT-WORK.md, IDEA-INBOX.md,
       DATA-TORTURE-MATRIX.md, INTEGRATION-READINESS.md, PRODUCT-INTEGRITY-AUDIT.md,
       HARDENING-QUEUE.md, BOOT-PROMPT.md, CODEX-HANDOFF.md, DOCUMENTATION-DRIFT-AND-PR-CLEANUP.md,
       campaigns/, evidence/, inventory_copy.py, decisions packets M1-M4 (2026-08-10..16),
       design-system-tokens.md, design-ui-branch-decision.md, home-redesign.md, fase9-10/23-25.md
pilot/: PILOT-PROTOCOL.md, INTERVIEW-GUIDE.md, CONSENT-DRAFT.md, ANALYTICS-PRIVACY-MATRIX.md
research/: m4/ search/ ux-observatory/
       search/: FIO-SEARCH-BENCHMARK.md, RESEARCH-LOG.md, RETURN-HISTORY-AUDIT.md, run_benchmark.py, benchmark_results.json
Raiz (relatórios): MANUS-HARDENING-FINAL.md, MANUS-PRE-CODEX-FINAL.md, MANUS-UX-REFINEMENT-FINAL.md, MANUS-SEARCH-FINAL.md
```

## Código (app/mobile/src) — MAIN
- `FioApplication.kt` (composition root FioGraph), `MainActivity.kt`
- `application/`: AndroidDocumentWriter.kt, ExportCoordinator.kt, FioService.kt, ImportService.kt, TimeReturnsService.kt
- `crypto/`: AesGcmContentCipher.kt, AndroidKeystoreKeyProvider.kt, ContentCipher.kt
- `domain/`: LocalImportParser.kt, Models.kt, Repositories.kt, SearchRepository.kt, TimeReturnEngine.kt
- `persistence/`: DatabasePreflight.kt, Entities.kt, FioDao.kt, FioDatabase.kt, RoomFioRepository.kt
- `returns/`: AndroidTimeReturns.kt
- `search/`: LexicalTokenizer.kt, LocalSearchService.kt, SemanticSearchPrototype.kt (M4, removível)
- `security/`: AppLockPolicy.kt, DeviceAuthenticator.kt
- `ui/`: FioApp.kt (Home/Archive/Return/Settings/single-Activity), FioViewModel.kt, theme/(Color,Theme,Type)

## Tests
- unit (31 arquivos): application/(DraftSurvivesFailedInsertTest, ExportCoordinatorTest, ExportRoundTripTest, FioServiceTest, ImportServiceTest, StorageFailureTest, TimeReturnsServiceTest), crypto/(AesGcmContentCipherTest), domain/(DataTortureTest, EngineTortureTest, HypothesisCheckTest, LocalImportParserTest, TimeReturnEngineTest), persistence/(Migration2To3Test, PerformanceTest), search/(HybridSearchServiceTest, LexicalTokenizerTest, LocalSearchServiceTest), security/(AppLockPolicyTest), ui/(DatePickerZoneTest, MotionReducedTest), ProjectContractTest.kt
- androidTest (instrumentados, NÃO executados no sandbox): AccessibilityContractTest, AdverseConditionTest, AndroidKeystorePersistenceTest, AndroidProviderFailureTest, EncryptedPersistenceTest, M2AndroidContractTest, M3RepositoryContractTest, PrivacySurfacesTest, RoomSchemaTest, SafLaunchTest, ScaffoldLaunchTest, TrustedLocalFlowTest
- debug: testing/DebugFixtureReceiver.kt

## ADRs (docs/DECISIONS.md, 787 linhas)
ADR-001..047: 001 Fio name, 002 autobiographical continuity, 003 AI selects not interprets, 004 Archive secondary, 005 iOS-first (SUPERSEDED 033), 006 no server V0, 007 embeddings private, 008 Time core/Semantic removable, 009 no Contrast V0, 010 explicit return controls, 011 one generic notification, 012 pilot feedback outside Return, 013 no cursive body, 014 no lock animation, 015 bounded import V0, 016 within-participant pilot, 017 content-free analytics, 018 export+delete, 019 freeze Book/Legacy, 020 philosophy no silent change, 021 execution plans, 022 persistence (SUPERSEDED 033), 023 crypto record design (Superseded 033/035), 024 embedding model selection DEFERRED, 025 sync/recovery V1, 026 monetization, 027 global return consent, 028 iOS toolchain (SUPERSEDED 033), 029 single encrypted draft, 030 UTF-8 export, 031 privacy cover+app lock, 032 git/publication separate, 033 pivot iOS→Android, 034 Android stack (Room+Compose), 035 Android protection/lock/backup/export, 036 device matrix, 037 M1 acceptance gates, 038 bounded M2 scheduling validation-only, 039 M3 import atomic rollback, 040 M4 isolated benchmark research, 041 V0 semantic path ended no model, 042 preserve long-term time/Legacy vision, 043 absolute date anchor temporal UI, 044 First Capsule onboarding, 045 Temporal Patina botanical, 046 export longevity, 047 Ritual do Fio Planned (gated).

## PROJECT-STATE.md fatos-chave
- minSdk 26, ID com.projetofio.app, Room schema 3 (schema 4 NÃO existe, UI-only tradeoff), Keystore AES-256-GCM, P0 finding corrigido M1, design v1 Verde-Sálvia, export Markdown+SHA-256 ADR-046, Migration2→3 byte a byte (500 entries), instrumentados NÃO executados (sem AVD; M2AndroidContractTest gate CI), pilot pacote pronto não iniciado, semantic M4 encerrado (ADR-041).
- Regras NUNCA: sem streaks/feeds/ads/chat IA/engagement/notifs guilt/server plaintext/Book-Legacy/nova dep de rede com conteúdo privado; não executar iOS; não reportar gates humanos como aprovados; não reabrir M4.
- Decisões abertas do fundador: ReturnPolicy schema 4, zero-width, analytics remoto.

## Plano de arquivos do Atlas (entrega)
docs/atlas/: SYSTEM-MAP.md, DATA-MAP.md, PRIVACY-BOUNDARY-MAP.md, TIME-MAP.md, SEARCH-MAP.md, UX-SURFACE-MAP.md, DEPENDENCY-MAP.md, TEST-MAP.md, DECISION-INDEX.md, INVARIANTS.md, RISK-MAP.md, KNOWLEDGE-GAPS.md, BUILD-ADAPT-MATRIX.md, DO-NOT-BUILD.md, DO-NOT-REINVENT.md, EXTERNAL-SOLUTION-INDEX.md, SOURCE-OF-TRUTH.md, CODEX-MASTER-MAP.md, CODEX-EXECUTION-PROTOCOL.md, DOC-DRIFT-AUDIT.md (drift = §68)
research/atlas/: REFERENCE-CATALOG.md, PATTERN-CARDS.md
packets/: INDEX.md, DEPENDENCIES.md, EXECUTION-QUEUE.md, PACKET-TEMPLATE.md, FIO-P01..nn
plans/ATLAS-QUEUE.md (fila de trabalho)
Raiz: CODEX-ATLAS-BOOT-PROMPT.md, MANUS-FIO-ATLAS-FINAL.md
Status packet: DRAFT/READY/IN_PROGRESS/BLOCKED/DONE/SUPERSEDED. Priority P0-P4.

## Decisões pendentes FOUNDADOR (da M4, incorporar ao Atlas)
- D-1 sealed search behavior (invisible vs count opaco)
- D-2 analytics remoto (local-first default)
- D-3 semantic shipping (kill criterion: recall@10≥+10pp ou MRR≥+0,08; RAM≤+200MB; latência≤500ms NPU)
- D-4 model download on-demand consent

## Commits Missão 5 (por estágio)
- (a confirmar no fim): docs(atlas): system/data/privacy/time maps etc.

## Benchmark M4 números (para SEARCH-MAP/KNOWLEDGE-GAPS)
lexical_v1: recall@5 0,098 / recall@10 0,109 / P@5 0,060 / MRR 0,200 / p95 <1ms
semantic/hybrid proxy e5-large: R@5 0,797 / R@10 0,917 / P@5 0,573 / MRR 1,000 / p95 ~2,6s
EmbeddingGemma-300M: MTEB-BR 13º/93 (0,649), TFLite oficial litert-community, Matryoshka 768/512/256/128.

## Radiografia código REAL (A1-A2) — fatos verificados
### Room schema 3 (persistence/Entities.kt)
- `entries`: id PK, created_at, original_created_at, original_time_zone?, updated_at, source (EntrySource), content_envelope BLOB (AES-256-GCM via AesGcmContentCipher), content_format, return_mode (ReturnMode ELIGIBLE/NEVER), last_returned_at?, return_count default 0, import_batch_id?, import_fingerprint_envelope BLOB?, deleted_at?, purge_after? (invariante: (deletedAt==null)==(purgeAfter==null)), schema_version. Índices: (deleted_at,original_created_at), (purge_after). **SEM campo sealed em EntryEntity** — seladas = conceito futuro; LocalSearchService tem isSealed lambda default {false}.
- `drafts`: singleton_slot=1 (exatamente 1 draft criptografado ADR-029), content_envelope BLOB.
- `app_settings`: singleton id=1; return_consent_state (NOT_CONFIGURED/ENABLED/PAUSED), returns_paused_at?, app_lock_mode (OFF/IMMEDIATE/ONE_MINUTE/FIVE_MINUTES), privacy_cover_enabled, analytics_enabled, quiet_hours_start_minute default 1260 (21h), quiet_hours_end_minute default 480 (8h), notification_permission_observed (UNKNOWN/GRANTED/DENIED), schema_version.
- `returns`: id PK, entry_id (FK), algorithm, algorithm_version, state (SELECTED/SCHEDULED/NOTIFIED/OPENED/DISMISSED/EXPIRED/CANCELLED), created_at, window_start, window_end, age_bucket, source?, cancel_reason? (PAUSED/ENTRY_DELETED/ENTRY_NEVER/SUPERSEDED/INELIGIBLE/IMPORT_ROLLBACK), opened_at?, dismissed_at?, expired_at?, algorithm_version.
- `import_batches` + `import_batch_items`: COMMITTED/ROLLED_BACK; items IMPORTED/ROLLED_BACK/EDITED_EXCLUDED; import_fingerprint (hmac da entry) para idempotência.
- FioDatabase: exportSchema=true, migrations 1→2→3 (ManualMigrationSpecification), schema 4 não existe.
- FioDao: observeActiveEntries/observeDeletedEntries (Flows), findEntry, loadActiveEntries, loadDraft, upsertDraft, clearDraft, insertEntryAndClearDraft (@Transaction), updateEntry, softDelete(deletedAt,purgeAfter), recover, purge, purgeExpired, loadSettings, upsertSettings, loadReturnHistory, loadReturnsForEntry (M4).

### Domain
- Models.kt: Entry, Draft, AppSettings, ReturnAttempt, ImportBatch/Commit/RollbackResult, ReturnAttempt.windowStart/End, SearchQuery/Hit/Result (sealedCount!), SearchTimeFilter, ReturnedFilter ANY/EVER_RETURNED/NEVER_RETURNED, SearchMode LEXICAL, SearchScope ALL/EXCLUDE_SEALED/SEALED_ONLY, SealedSearchBehavior HIDDEN/COUNT_ONLY.
- Repositories.kt: FioRepository (observeActive/DeletedEntries, findEntry, draft load/save/clear, insertEntryAndClearDraft, updateEntry, deleteEntry(id,deletedAt,purgeAfter), recoverEntry, purgeEntry), ReturnRepository, ImportRepository (interfaces).
- TimeReturnEngine.kt: evaluate(now,settings,zone,active,history,random) → TimeReturnDecision.Return(windowStart,windowEnd,deliveryAt) ou Silent(SilenceReason: CONSENT_DISABLED/PENDING_RETURN/FREQUENCY_CAP/BOOTSTRAP_WAIT/NO_ELIGIBLE_ENTRY/...). Regras: cap 7 dias, bootstrap age (0/30/60/90 dias por contagem), quiet hours wrap-midnight, age buckets (7-29..730+), prefer never-returned, uniform random bucket+entry.
- LocalImportParser: parse markdown/text → ParsedImport; ImportIssueCode; ImportFingerprint (deterministic hmac para dedupe).

### Application layer
- FioService: observeActive/Deleted, loadDraft, autosaveDraft (D12: substitui conteúdo antes da transação), saveEntry(autosave→insertEntryAndClearDraft), editEntry, moveToRecentlyDeleted, recoverEntry, permanentlyDelete, purgeExpired, loadSettings, setAppLockMode, export(MARKDOWN/TEXT) com checksum SHA-256 do corpo (export v1.0 ADR-046, manifest.json + entries/YYYY/ + deleted/).
- TimeReturnsService (M2): reconcile(), enableReturns (consent ENABLED), pause/resume, observeNotificationPermission, setQuietHours, pendingReturnId, openReturn, dismissReturn, neverReturn; reconcilePending/cancelIneligible/scheduleAfterSilence (cancela attempts quando ineligible).
- ImportService: preview → stage (em memória) → commit (ImportBatch) / cancel / rollback(batchId) / reconcile.
- ExportCoordinator: export() genérico sobre Destination (SAF); ExportOutcome SUCCESS/CANCELLED/FAILED.
- AndroidTimeReturns (returns/): WorkManagerReturnScheduler (OneTimeWorkRequest TimeReturnWorker; schedule/cancel), TimeReturnWorker (CoroutineWorker doWork → engine.evaluate → scheduleAfterSilence/notify), AndroidReturnNotifications (NotificationManager, canPostNotifications, createChannel, post/cancel, intent MainActivity).
- search/: LexicalTokenizer (NFD+\p{Mn}, whole-token ordered, prefix último token, snippets originais), LocalSearchService (scan-on-demand Option A, isSealed lambda, COUNT_ONLY default), SemanticSearchPrototype (RRF k=60, EmbeddingProvider, FakeEmbeddingProvider, HybridSearchService com filtros herdados; removível, feature-flag M4-semantic futuro).
- security/: AppLockPolicy.shouldLock(mode, lastLockEvent), DeviceAuthenticator (BiometricPrompt wrapper: isAvailable, authenticate with success/error callbacks).

### UI (FioApp.kt single activity, FioViewModel)
- MainSurface HOME/ARCHIVE/SETTINGS; navegação por estado (Single Activity).
- HomeScreen: "Fio" displayLarge + menu ⋯ (Arquivo/Configurações, ADR-004 secundários); prompt "O que está passando pela sua cabeça hoje?"; BasicTextField sem borda (palavras = elemento mais escuro); BotanicalMotif (pátina ADR-045 determinística); Button Guardar (saves entry com draft pré-criptografado); notice "Guardado." / "Guardado. O tempo cuida do resto." (first capsule ADR-044); timeSheet "Quando isso pode voltar?" políticas Someday/7/30/90/365/Calendário/Nunca; dateSheet (calendário); timePicker quiet hours.
- ArchiveScreen: back, heading "Arquivo"; ArchiveSearchField (debounce 300ms, placeholder "Buscar no arquivo", ✕ limpar 48dp); resultados inline: N resultado(s), hit row (data, snippet original SelectionContainer, "Já voltou N vez(es)"), sealed note neutra; cards por entrada (abrir/editar/excluir→30 diasRecentlyDeleted); Excluídos recentemente (recover/purge); erro seguro ("não pôde ser aberto com segurança. Nada foi apagado.").
- NoteScreen: visualização; EditEntryDialog; deleted list; quietHoursLabel.
- ReturnScreen(entry,onClose,onNeverReturn); SettingsScreen: consent, pause, app lock, privacy cover, analytics toggle, import (preview/commit/batches/rollback), export (md/txt), deleted management.
- PrivacyCover/LockedScreen/SafeOpenFailure (biometria); isMotionReduced; displayDate/displayImportDate.
- FioViewModel state: draftText, entries, deletedEntries, settings, loading, saving, savedNotice, recoverableError, archiveError, m2/m3EngineeringEnabled, pendingReturnId, openedReturn, returnError, importPreview, importBatches, importMessage, importing, searchTerms, searchResult, searchLoading, searchError. DI: FioGraph(application) { service, timeReturns, localImport, search = LocalSearchService(repo) }; MainActivity → FioViewModel.Factory(search: SearchService? = null).

### Gradle/deps (libs.versions.toml)
agp 9.3.1, ksp 2.3.11, lifecycle 2.10.0, room 2.8.4, coroutines 1.11.0, serialization 1.8.1, biometric 1.1.0, fragment 1.8.9, work 2.11.2, junit4 4.13.2, robolectric 4.14.1, espresso 3.7.0; Compose BOM + material3 + activity.compose + lifecycle compose + room.ktx + biometric androidx; ksp room.compiler; room.schemaLocation $projectDir/schemas, exportSchema=true. versionName 0.1.0-dev.

### Manifest
POST_NOTIFICATIONS + permission (notification); allowBackup="false"; dataExtractionRules + fullBackupContent xml (ver @xml); application label @string/app_name.

### TimeReturnEngine detalhes avaliados (Torture: EngineTortureTest 18+; DataTortureTest; HypothesisCheckTest)
- bootstrapMinimumAgeDays(entryCount): 0/30/60/90 conforme contagem; evaluate filtra active (deleted null, return_mode ELIGIBLE, source nativa?), agrupamento por age bucket, cap de 7 dias sobre history non-CANCELLED.
- SilenceReason enum; ReturnRandom interface (testável).

## Pesquisa externa (fase 3) — findings verificados com URLs
### Journaling privado estado da arte
1. DeepJournal blog "7 Best Private Journaling Apps in 2026" (2026-07-15): https://deepjournal.app/blog/best-private-journaling-apps-in-2026 — Day One E2EE + metadata não E2E (timestamps, dims, device); Standard Notes auditado open-source + offline decryption tool (longevidade); Notesnook E2EE+Vericrypt; Obsidian vault local NÃO cifrado; Joplin E2EE opcional configurável; Penzu "military-grade" = marketing sem docs; DeepJournal = enclaves confidenciais p/ AI. Lição: "architecture decides who controls the writing" — Fio já faz o padrão-ouro (encrypt-before-disk + offline + export).
2. Mini Diarium (fjrevoredo/mini-diarium, GitHub, MIT, Tauri+SolidJS+Rust): AES-256-GCM por entry antes do disco, SQLite local, import Day One JSON/TXT, Mini Diary JSON, jrnl; export JSON/Markdown; backup local da DB cifrada; sem HTTP client/analytics. URL: https://mini-diarium.com/encrypted-journal/ — REFERÊNCIA DIRETA para import (formato Day One JSON compatível com parser M3?) e padrão de segurança.
3. Day One "On This Day": https://dayoneapp.com/features/on-this-day/ — retrospectiva determinística por data (mesma data em anos anteriores), multimídia, sem aleatoriedade.
4. Diarly On This Day: https://diarly.app/help/on-this-day.html — `@onThisDay` na busca + notificações configuráveis ("Let me know about past memories", por journal, hora escolhida) + widget. — Padrões de resurfacing determinístico.
5. Facebook/Google Photos "Memories/On This Day": resurfacing automático (2015+), frequentemente criticado por trazer conteúdo indesejado (casamentos perdidos, ex-relacionamentos) — VERGA tweet sobre deletar Day One por analogia com Memories. Lição: resurfacing automático = risco emocional; Fio's escolha de consentimento explícito + cap 7 dias + "never return" é diferenciada e defendável.
6. Glimmo article (2026-07): "responsible digital journal app should let you switch memories off, limit notifications, or control which journals are included" — controles de usuário são o padrão emergente de responsabilidade.

### EmbeddingGemma 300M (candidato primário)
- HF: https://huggingface.co/litert-community/embeddinggemma-300m — licença "gemma" (ACEITAR LICENÇA NO HF PARA DOWNLOAD — stop condition FIO-P12); variantes LiteRT; tokenizer sentencepiece; RAG library Google AI Edge.
- Google oficial: https://ai.google.dev/gemma/docs/embeddinggemma — 308M params, baseado Gemma 3, multilingue 100+ idiomas, MRL 768→128 dims, 2K tokens, <200MB RAM quantizado, <22ms EdgeTPU, offline. Licença open weights uso comercial responsável (fine-tune+deploy ok).
- Performance Android (SAMSUNG S25 ULTRA): NPU seq256: init 206ms, inference 7.8ms, RSS 224MB, modelo 182MB tflite; CPU seq256: init 17.6ms, inference 66ms, RSS 110MB; CPU seq2048: 2455ms, RSS 333MB. Quantização int4 embeddings/feedforward, int8 attention.
- **Implicação kill criterion**: NPU 7.8ms vs CPU 66ms(256) a 2.5s(2048); RAM NPU 224-332MB (ACIMA do teto de +200MB do Fio em alguns cenários; RSS total inclui app) — medir RSS DELTA do app, não modelo isolado.
- MTEB-BR: 13th de 93 (documentado no nosso RESEARCH-LOG).

### WorkManager/Android scheduling
- developer.android.com background-work persistent: WorkManager exato via setExpedited/AlarmManager exact alarms (wake Doze) — "Only use exact alarms for time-critical"; API 31+ exige permissão SCHEDULE_EXACT_ALARM. Fio usa inexact OneTimeWorkRequest — correto per ADR-038.
- Fusion WorkManager+AlarmManager pattern (2019): WorkManager para persistência + AlarmManager para precisão — opção se um dia quiser entrega exata (decisão ADR nova necessária; não construir agora).

### Search local on-device
- Medium GDE "On-Device RAG for App Developers" (2026-02): LiteRT RAG library, EmbeddingGemma 308M. ertas.ai blog: embeddings on-device + índice local (sqlite-vss/Faiss local) — nosso protótipo evita índice persistente (Option A) deliberadamente.

### Padrões de resurfacing temporal (para PATTERN-CARDS)
- On This Day determinístico (Day One, Diarly, Apple Journal, Google Photos) — por data-exata.
- Random/curated memories (Facebook/Photos — criticado por conteúdo indesejado).
- Apple Journal "Suggested Moments" ML (local, Apple Intelligence) — sugestão, não entrega.
- Lightmark: entries imutáveis pós-submissão (contraponto ao nosso edit livre).
- Diarly notificações configuráveis por journal/hora — padrão de controle emergente.
- Glimmo: responsabilidade = controles de opt-out/limites.

## Auditoria de drift (§6) — achados verificados
1. **docs/06-DATA-MODEL.md é o spec CONCEITUAL (campo a campo canônico)**: define accessPolicy (standard V0 / sealed V1), sourceExternalId, contentLocale, restUntil, requestedWindowStart/End (Remember Later), embeddingCiphertext/embeddingModelVersion. O CÓDIGO não implementa nenhum desses ainda (EntryEntity real: sem accessPolicy, sem sourceExternalId, sem restUntil, sem requestedWindow*, embedding inexistente). NÃO é drift malicioso — doc 06 é aspiracional/canônico (fonte nível 4-5), código é o estado atual. Tratar no DOC-DRIFT-AUDIT como "spec ahead of code" documentado.
2. **docs/05-ARCHITECTURE.md**: aceito direção Android; claims coerentes (Room via Gradle, minSdk 26, com.projetofio.app, search local textual, semântica V1 experiment). Coerente com código.
3. **docs/07-RETURNS-ENGINE.md**: bootstrap do PILOTO (4 entries/7d; 2-3 entries 14-21d; 1 entry 30-45d) é tabela de PARTICIPANTE (pilot), não do código production — código production tem bootstrapMinimumAgeDays(entryCount): 0/30/60/90. Divergência legítima (pilot vs produção), registrar.
4. **PROJECT-STATE.md**: coerente — "Semantic M4 encerrado sem modelo selecionado (ADR-041)", engine M2/M3/M4 implementado, PR #1 DRAFT (M1+checkpoints, 100 arquivos, aguarda merge fundador), PR #3 OPEN design-ux-v1. NÃO menciona Missão 4 concluída nem a busca M4 lexical+protótipo (branch integration) — drift real: PROJECT-STATE desatualizado vs código (busca em produção existe). Registrar correção no DOC-DRIFT-AUDIT (PROJECT-STATE precisa das linhas da busca M4).
5. RESEARCH-LOG.md existe em research/search/ (citado como inexistente no grep anterior por path — na verdade existe: ls mostra FIO-SEARCH-BENCHMARK.md, RETURN-HISTORY-AUDIT.md, benchmark_results.json, RESEARCH-LOG.md).

## Arquivos da fase 4 já escritos (todos em docs/atlas/ ou research/atlas/)
SYSTEM-MAP.md, DATA-MAP.md, PRIVACY-BOUNDARY-MAP.md, TIME-MAP.md, SEARCH-MAP.md, UX-SURFACE-MAP.md, DEPENDENCY-MAP.md, TEST-MAP.md, DECISION-INDEX.md, BUILD-ADAPT-MATRIX.md, INVARIANTS.md, RISK-MAP.md, KNOWLEDGE-GAPS.md, SOURCE-OF-TRUTH.md; research/atlas/REFERENCE-CATALOG.md + PATTERN-CARDS.md.

## Pendente fase 4: DOC-DRIFT-AUDIT.md (único faltando da fase 4)
Pontos a registrar: PROJECT-STATE sem Missão 4 (busca); docs 06 spec-ahead-of-code (accessPolicy etc.); 07 pilot-vs-production bootstrap; docs 01-04 era-iOS (design v1 Verde-Sálvia implementado ok); AGENTS.md atualizado pela M4 ✓; docs/search ✓ atualizados pela M4; MANUS-SEARCH-FINAL.md ✓.

## Pendente fase 5: CODEX-MASTER-MAP.md, CODEX-EXECUTION-PROTOCOL.md, EXTERNAL-SOLUTION-INDEX.md (docs/atlas/)
## Pendente fase 6: packets/INDEX.md, DEPENDENCIES.md, EXECUTION-QUEUE.md, PACKET-TEMPLATE.md, FIO-P01..nn
## Pendente fase 7: plans/ATLAS-QUEUE.md, MANUS-FIO-ATLAS-FINAL.md, commits+push, entrega
Nota: packets existentes relevantes em plans/: DOCUMENTATION-DRIFT-AND-PR-CLEANUP.md, HARDENING-QUEUE.md, INTEGRATION-READINESS.md — reutilizar no INDEX como antecedentes.
