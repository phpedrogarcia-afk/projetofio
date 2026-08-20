# SYSTEM-MAP — mapa real do ProjetoFio

**Status:** REFERENCE. Estrutura de UI atualizada em 2026-08-20; demais camadas
preservam a radiografia da Missão 4 e devem ser verificadas de forma dirigida.

## 1. A anatomia canônica (três capacidades, três políticas)

```text
                          FIO  (minSdk 26 · ID com.projetofio.app · v0.1.0-dev)
                            │
            ┌───────────────┼────────────────┐
            ▼               ▼                ▼
        GUARDAR         ENCONTRAR        REENCONTRAR
      (M1 core)        (M4 search)      (M2 returns)
   escrever em       procurar            o tempo devolve
   palavras próprias conscientemente    sem pedido atual
            │               │                │
            │               │                │
      infraestrutura compartilhada (cripto, Room, DI, clock, zone)
            política NUNCA compartilhada (Guarda/Encontra não gera Reencontros)
```

O princípio arquitetural registrado em `AGENTS.md` e `docs/search/SEARCH-ARCHITECTURE.md`: **infraestrutura pode compartilhar matemática; política não.** O Return Engine não usa a busca como fonte de seleção; a busca não produz devoluções.

## 2. Mapa em camadas (nomes reais)

```text
UI (single activity, navegação por estado)
│  app/mobile/src/main/java/com/projetofio/app/ui/
├── FioApp.kt                 — orquestra MainSurface {SAVE, FIND, ARCHIVE, SETTINGS}
├── home/HomeScreen.kt        — editor + escolha temporal + First Capsule
├── search/SearchScreen.kt    — query e resultados factuais
├── archive/ArchiveScreen.kt  — lista cronológica
├── entry/EntryDetailScreen.kt — leitura, edição e exclusão
├── returns/ReturnScreen.kt   — devolução aberta e Never Return
├── settings/SettingsScreen.kt — app lock, Returns, import/export e excluídos
├── security/PrivacyScreens.kt — PrivacyCover, LockedScreen, SafeOpenFailure
├── components/               — componentes compartilhados restritos
├── theme/                    — Verde-Sálvia design v1
└── FioViewModel.kt           — FioUiState (StateFlow), onDraftChanged (autosave),
      onSearchChanged (debounce 300ms), saveEntry, openReturn, import/export actions

APPLICATION (services de aplicação)
│  app/mobile/src/main/java/com/projetofio/app/application/
├── FioService.kt             — GUARDAR: autosaveDraft → insertEntryAndClearDraft (D12),
│     editEntry, moveToRecentlyDeleted → recoverEntry → permanentlyDelete, purgeExpired,
│     export (Markdown/Texto v1.0 + SHA-256 body checksum)
├── TimeReturnsService.kt     — REENCONTRAR: reconcile(), enable/pause/resume returns,
│     quiet hours, openReturn/dismissReturn/neverReturn, cancelIneligible
├── ImportService.kt          — M3: preview (staging em memória) → commit → rollback, reconcile
├── ExportCoordinator.kt      — coordena SAF/destination; ExportOutcome
└── AndroidDocumentWriter.kt  — escrita de documentos SAF

DOMAIN (puro Kotlin, sem Android)
│  app/mobile/src/main/java/com/projetofio/app/domain/
├── Models.kt                 — Entry, Draft, AppSettings, ReturnAttempt, ReturnState,
│     ReturnConsentState, AgeBucket, SearchQuery/Hit/Result, SealedSearchBehavior,
│     SearchMode LEXICAL, SearchScope, ReturnedFilter
├── Repositories.kt           — FioRepository, ReturnRepository, ImportRepository (interfaces)
├── SearchRepository.kt       — SearchRepository (loadSearchableEntries, loadReturnHistory),
│     SearchService, SearchExecutionSettings
├── TimeReturnEngine.kt       — evaluate() → Return(window) | Silent(SilenceReason),
│     cap 7 dias, bootstrap wait, quiet hours com wrap de meia-noite, age buckets
└── LocalImportParser.kt      — parse Markdown/Texto → ParsedImport; ImportFingerprint hmac

SEARCH (M4 — lexical produção + semântica removível)
│  app/mobile/src/main/java/com/projetofio/app/search/
├── LexicalTokenizer.kt       — NFD+\p{Mn}, whole-token ordered, prefix só último token,
│     snippets do texto original
├── LocalSearchService.kt     — scan-on-demand Option A; sealed = lambda isSealed default {false};
│     COUNT_ONLY default; soft-deleted invisíveis; "Já voltou" via OPENED history
└── SemanticSearchPrototype.kt — RRF k=60, EmbeddingProvider (pluggável), FakeEmbeddingProvider
      (zero pesos), HybridSearchService (braço vetorial herda TODOS os filtros)
      — FEATURE FLAG FUTURO (M4-semantic), removível, NUNCA conectado a Room/Returns

SECURITY
│  app/mobile/src/main/java/com/projetofio/app/security/
├── AppLockPolicy.kt          — shouldLock(mode, lastLockEvent) — OFF/IMMEDIATE/1min/5min
└── DeviceAuthenticator.kt    — wrapper BiometricPrompt (isAvailable, authenticate)

CRYPTO
│  app/mobile/src/main/java/com/projetofio/app/crypto/
├── ContentCipher.kt          — interface seal/open
├── AesGcmContentCipher.kt    — AES-256-GCM, envelope = IV || ciphertext || tag;
│     associatedData(kind, recordId, schemaVersion) binding ao registro
└── AndroidKeystoreKeyProvider.kt — Keystore AES key + RSA-wrapped export policy (ADR-035)

PERSISTENCE
│  app/mobile/src/main/java/com/projetofio/app/persistence/
├── FioDatabase.kt            — Room schema 3, exportSchema=true, migrations 1→2→3
│     (ManualMigrationSpecification; schema 4 NÃO existe)
├── Entities.kt               — EntryEntity (content_envelope BLOB, sem campo sealed),
│     DraftEntity (singleton_slot=1), AppSettingsEntity (singleton id=1), ReturnEntity,
│     ImportBatchEntity/ItemEntity
├── FioDao.kt                 — @Transaction insertEntryAndClearDraft; softDelete/recover/purge/
│     purgeExpired; loadActiveEntries/observeActiveEntries Flows; loadReturnsForEntry (M4)
└── RoomFioRepository.kt      — implementa FioRepository + SearchRepository (read-only p/ busca),
      decryptEntry on read

RETURNS PLATFORM BINDING
│  app/mobile/src/main/java/com/projetofio/app/returns/
├── AndroidTimeReturns.kt     — WorkManagerReturnScheduler (OneTimeWorkRequest,
│     UNIQUE_WORK_NAME=fio-time-return-opportunity-v1, ExistingWorkPolicy.REPLACE),
│     TimeReturnWorker (CoroutineWorker, retry <3, depois failure),
│     AndroidReturnNotifications (canal "Lembranças do Fio" IMPORTANCE_LOW)

ROOT
├── FioApplication.kt         — FioGraph: composição manual (lazy): service, timeReturns,
│     localImport, search(LocalSearchService), repository, clock(zoneId, InstantSource)
└── MainActivity.kt           — injecta graph → FioViewModel.Factory
```

## 3. Fluxos reais

### Guardar
```text
HomeScreen (BasicTextField)
  → FioViewModel.onDraftChanged → FioService.autosaveDraft
      (criptografa draft ANTES da transação — contrato D12: conteúdo do draft
       substitui o anterior; insert que falha não apaga o rascunho)
  → Button Guardar → FioService.saveEntry
      → repository.insertEntryAndClearDraft (@Transaction Room)
          (clearDraft + insertEntry atômicos)
  → notice "Guardado." / "Guardado. O tempo cuida do resto." (first capsule ADR-044)
  → se consent ENABLED: reconcilia oportunidades via TimeReturnsService.reconcile()
```

### Encontrar (M4)
```text
ArchiveScreen
  → ArchiveSearchField (debounce 300ms, query runtime-only, NUNCA persistida)
  → FioViewModel.onSearchChanged → SearchService.search(query)
      → LocalSearchService (produção): loadSearchableEntries (decrypt+scan, Option A)
          → LexicalTokenizer.matchesQuery/filters → hits com snippets originais
          → seladas: isSealed lambda; COUNT_ONLY default (seladas invisíveis + contagem opaca)
          → "Já voltou N vez(es)" via loadReturnsForEntry (OPENED)
      → (protótipo M4, NÃO injetado): HybridSearchService = lexical arm + vector arm
          RRF k=60; braço vetorial herda todos os filtros; fallback total ao léxico
  → ArchiveSearchHitRow: data original + snippet (SelectionContainer) + badge factual
```

### Reencontrar
```text
WorkManagerReturnScheduler.schedule(at) [OneTimeWorkRequest, delay até Instant]
  → WorkManager dispara TimeReturnWorker.doWork
      → TimeReturnsService.reconcile()
          → TimeReturnEngine.evaluate(now, settings, zone, active, history, random)
              → Return(windowStart, windowEnd, deliveryAt) | Silent(SilenceReason)
              (regras: consent ENABLED, sem pending, cap 7 dias, bootstrap wait,
               só ELIGIBLE, quiet hours wrap-meia-noite, age bucket uniforme)
          → reconcilePending / cancelIneligible (pausa, exclusão, never → cancela attempts)
      → scheduleAfterSilence → post(notification canal "Lembranças do Fio", IMPORTANCE_LOW)
  → notificação → intent MainActivity → ReturnScreen(entry, onClose, onNeverReturn)
  → openReturn / dismissReturn / neverReturn (ReturnState, ReturnEntity)
  → reboot: WorkManager reagenda; reconcile pós-boot cancela elegibilidade perdida
```

## 4. Estados de vida de uma entrada (política)

| Estado | Como entra | Comportamento |
|---|---|---|
| Active | saveEntry | visível no Arquivo, elegível se `return_mode = ELIGIBLE` |
| Scheduled | TimeSheet (Someday / 7-90-365 / calendário) | política de devolução gravada; engine avalia |
| Already returned | ciclo OPENED | continua visível; "Já voltou N vez(es)" na busca |
| Never | `return_mode = NEVER` | jamais devolvida (SilenceReason implícito) |
| Resting / paused | pause returns global | não devolvida enquanto pausada |
| Recently deleted (30 dias) | moveToRecentlyDeleted (deleted_at + purge_after) | invisível ao Arquivo/busca; recuperável; purge automático |
| Purged | purgeEntry / purgeExpired | **removida de tudo**: entry, draft? não, draft é separado; retornos cancelados |

## 5. O que NÃO existe hoje (também é mapa)

Não existe: sync, backend, conta de usuário, analytics remoto (transporte), índice persistente (FTS5), campo `sealed` em EntryEntity, embeddings em produção, qualquer fluxo de rede de conteúdo privado, schema 4, navegação multi-activity/NavController gráfico, módulos por feature, camera/mídia na entry, tags/pastas, multi-dispositivo.
