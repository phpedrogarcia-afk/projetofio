# PRIVACY-BOUNDARY-MAP — onde o plaintext pode existir e o que o contém

**Estado examinado:** HEAD `9cfd5f1` · **Evidência:** `E4` (código) + `E1/E2` (`docs/search/SEARCH-PRIVACY-ARCHITECTURE.md`, `docs/08-PRIVACY-SECURITY.md`, `docs/09-ANALYTICS-EXPERIMENTS.md`).

## 1. O mapa de lifecycle do plaintext

```text
teclado do usuário
        │
        ▼
 memória (BasicTextField → onDraftChanged → autosaveDraft)
        │
        ├─────────────────────── GUARDAR ───────────────────────┐
        │                                                       │
        ▼                                                       │
   seal() AES-256-GCM (AesGcmContentCipher)                    │
        │  AAD = kind + recordId + schemaVersion (binding)     │
        ▼                                                       │
   contentEnvelope BLOB ─────────────────────────────── Room   │
        │           (nunca plaintext em disco)                  │
        ▼                                                       │
   open() (decrypt)  ◄─── render / edição ──── UI              │
        │                                                       │
        └─────────────────────── ENCONTRAR ─────────────────────┤
                                                                │
   scan-on-demand (Option A): decrypt → tokenize → match       │
        → snippet original (memória, transient)                │
        → SearchResult (memória) → ArchiveSearchHitRow         │
        → descarta ao re-render; NUNCA persiste                 │
                                                                │
        └─────────────────────── EXPORT ────────────────────────┘
   decrypt por entrada → markdown/texto → SAF (escolha do usuário)
   plaintext sai do app APENAS por exportação explícita (ADR-018)
```

## 2. Cada superfície onde plaintext pode existir — e seu contêiner

| Superfície | Plaintext existe? | Contêiner / lifetime | Mitigação documentada |
|---|---|---|---|
| Editor (Home) | sim | `FioUiState.draftText` / BasicTextField; autosave criptografa | PrivacyCover ao background (ADR-031) |
| Autosave draft | sim → cifrado | envelope em memória até insert | D12: draft criptografado antes da transação |
| Note screen (leitura) | sim | `decryptEntry` on demand, em memória | PrivacyCover; LockedScreen após app lock |
| Edição (EditEntryDialog) | sim | memória de diálogo | idem |
| Busca (M4) | sim | scan sob demanda; snippet transient | Option A; queries nunca persistidas; SelectionContainer factual |
| Protótipo semântico (M4) | sim (potencial) | `HybridSearchService` executa no scan-on-demand | removível, flag futuro, mesmo boundary |
| Export | sim | transitório no serviço; destino SAF | escolha explícita; checksum; sem cópia do app |
| Import | sim | preview em memória (staging `ImportService`) | rollback atômico; falha isolada por item |
| Notificação | **não** | canal low, corpo genérico | ADR-011; verificado em código |
| Analytics (local) | **não** | schema fechado, content-free | ADR-017; `analyticsEnabled` toggle |
| Logs (Logcat) | **não deve** | — | grep: nenhum log de conteúdo no código M1-M4; regra `AGENTS.md` |
| Clipboard | não existe no app hoje | — | não copiar texto íntimo (futuro: decidir) |
| Crash reports | não | — | sem collector; `AGENTS.md` proíbe conteúdo em payload |
| App switcher / recents | risco de screenshot do editor/retorno | — | PrivacyCover (ADR-031); gap `FLAG_SECURE` por superfície em `KNOWLEDGE-GAPS.md` |
| Backup Android | bloqueado | `allowBackup="false"` + extraction rules | ADR-035 |
| Keychain/Keystore | nunca plaintext | system keystore | ADR-035 |

## 3. A fronteira de criptografia (o contrato)

A fronteira é o **contentEnvelope**: tudo que cruza disco cruza AES-256-GCM com AAD atrelado a `(kind, recordId, schemaVersion)`. Regras operacionais (contratos `SEARCH-PRIVACY-ARCHITECTURE.md`): exclusão permanente remove a entry, os ciclos de retorno (cancelados com reason), e qualquer futuro índice/embedding; edição sempre re-cifra (o envelope é re-sealed em `updateEntry`); schema_version no envelope permite rotação; o plaintext nunca aparece em índice persistente na V1 (FTS5 rejeitada — Option A cobre o uso com latência <1 ms).

## 4. O que é sensível além do plaintext

Retorno history (factual, mas pessoal), sealed state (futuro), fingerprints de import (derivados de conteúdo — sensíveis por associação), embeddings (quando existirem — ADR-007), e **queries de busca** (efêmeras, mas se alguém optasse por persisti-las, seriam sensíveis — por isso nunca persistem).

## 5. Lacunas conhecidas (não escondidas)

1. **App switcher**: PrivacyCover cobre o app em background; a captura do momento exato (screenshot automático ao abrir) não tem verificação instrumentada (`PrivacySurfacesTest` existe mas não roda no sandbox). — `E2` docs / `E5` missing.
2. **FLAG_SECURE por superfície**: não há `FLAG_SECURE` aplicado; dependência do PrivacyCover UI.
3. **Crash handling**: nenhum collector; se um dia existir, payload content-free precisa de teste (packet `FIO-Pxx`).
4. **Recents thumbnail** de `ReturnScreen` (contém data + trechos?): não verificado em aparelho.
5. **Clipboard**: sem funcionalidade de cópia de entrada hoje; se houver no futuro, decisão de avisar é de produto.
