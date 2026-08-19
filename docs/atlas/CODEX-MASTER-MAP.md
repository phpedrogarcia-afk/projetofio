# CODEX-MASTER-MAP — o mapa que o Codex lê antes de tocar em qualquer arquivo

Este documento é a visão única do sistema para um agente de código que nunca viu o ProjetoFio. Ele combina os nove mapas do Atlas em uma sequência de leitura obrigatória. **Ordem de leitura: esta página → INVARIANTS → os mapas indicados por assunto → o packet em fila → o código.**

## 1. O que é o Fio, em uma frase

Um diário autobiográfico Android, 100% local e criptografado antes do disco, cuja função distintiva é o **tempo**: o app devolve entradas antigas ao usuário sem calendário e sem pressão (Reencontrar), guarda as novas com proteção (Guardar) e as recupera sem interpretar nada (Encontrar). Ele nunca responde perguntas autobiográficas no lugar do usuário.

## 2. Anatomia em 6 camadas (de fora para dentro)

| Camada | Pacotes | O que faz | Arquivos-chave |
|---|---|---|---|
| UI (Compose, single-activity) | `ui/` | Home (editor+pátina+time sheet), Arquivo (+busca), Return, Settings; estado em `FioViewModel.FioUiState` | `FioApp.kt` (1.914 linhas — a tela inteira), `FioViewModel.kt` |
| Serviço de aplicação | `application/` | `FioService` (guarda: draft→cifra→insere, autosave), `ImportService` (preview→commit atômico), `ExportCoordinator` (Markdown+SHA-256, SAF), `TimeReturnsService` (orquestra devolução), `AndroidDocumentWriter` | `FioService.kt` |
| Domínio puro (Kotlin, testável) | `domain/` | `TimeReturnEngine` (elegibilidade, cap 7d, bootstrap, quiet hours, buckets), `Models.kt` (Entry, ReturnAttempt, SearchQuery...), `LocalImportParser` | `TimeReturnEngine.kt` |
| Devoluções (Android) | `returns/` | `AndroidTimeReturns` (WorkManager inexact; cancela on never/pause/delete) | `AndroidTimeReturns.kt` |
| Persistência | `persistence/` | Room schema 3: `FioDatabase`, `FioDao`, `RoomFioRepository`, `Entities.kt`, `DatabasePreflight`, migrations manuais (2→3 byte a byte) | `Entities.kt`, `FioDatabase.kt` |
| Fronteiras | `crypto/`, `search/`, `security/` | `AesGcmContentCipher` (GCM+AAD), `KeystoreKeyProvider`, `LexicalTokenizer`/`LocalSearchService`/`SemanticSearchPrototype`, `AppLockPolicy`/`DeviceAuthenticator` | `AesGcmContentCipher.kt`, `LocalSearchService.kt` |

**Composição raiz:** `FioApplication.kt` monta `FioGraph` (DI manual lazy); `MainActivity` injeta o grafo no `FioViewModel.Factory`. Não há Hilt, não há NavController.

## 3. Contratos que matam testes se quebrarem (os invariantes críticos)

O catálogo completo está em `docs/atlas/INVARIANTS.md`. Os cinco que mais pegam agentes despreparados:

1. **I-01/I-06:** plaintext nunca persiste; exclusão permanente remove history + futuro embedding.
2. **I-09/I-10:** draft criptografado ANTES do insert; insert falho nunca apaga o draft (D12).
3. **I-17/I-18:** soft-deleted invisível em ambos os braços de busca; o braço vetorial herda todos os filtros.
4. **I-14/I-15:** queries nunca persistem; busca retorna só texto original.
5. **I-21/I-23:** devolução só com consentimento; uma notificação, canal low, corpo genérico, sem lembrete.

## 4. Onde moram as fronteiras sensíveis (não tocar sem packet)

| Fronteira | Localização | Regra |
|---|---|---|
| Criptografia | `crypto/` | Só primitivas padrão; AAD por kind+recordId+schemaVersion |
| Busca | `search/` | Semântica só atrás de flag, removível, nunca conectada a Returns/Room mutável |
| Devolução | `domain/TimeReturnEngine.kt` + `returns/` | A política temporal é o coração; qualquer mudança = decisão de produto |
| Notas seladas | `EntryEntity.sealedAt` + `LocalSearchService.isSealed` | Invisíveis por conteúdo; threat model em `docs/search/SEARCH-SEALED-THREAT-MODEL.md` |
| Analytics | (nenhum collector) | Banido por default; ADR-017 schema fechado se um dia existir |
| Rede | (nenhuma) | Proibida com conteúdo privado (AGENTS.md) |

## 5. Como trabalhar aqui (protocolo resumido)

Branch de integração/research separada; `main` nunca tocada; PR antes de merge; testes unitários verdes antes de commit (134 hoje); instrumentados rodam só em aparelho; gates humanos nunca reportados como aprovados sem evidência; decisão nova = ADR novo. Detalhes completos em `docs/atlas/CODEX-EXECUTION-PROTOCOL.md`.

## 6. O que NÃO existe (e que agentes tendem a inventar)

Não existe: servidor, conta, sync, LLM, chat, embeddings em produção, FTS5, histórico de queries, pesquisas recentes, streaks, tags, pastas, iOS, Book/Legacy (congelados), `accessPolicy` no código (spec futuro), `restUntil`/`requestedWindow*` (spec futuro). Se o Codex "encontrar" qualquer um desses no código, ele achou um bug de leitura e deve reportar.

## 7. Números que ancoram o mapa

134 testes unitários verdes; Room schema 3; minSdk 26 / target 37 preview; com.projetofio.app; AES-256-GCM via Keystore; export v1.0 Markdown+SHA-256; 1 canal de notificação low; cap de 7 dias entre devoluções; busca: debounce 300ms, Option A scan-on-demand, RRF k=60 no protótipo.
