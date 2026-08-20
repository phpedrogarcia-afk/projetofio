# INVARIANTS — invariantes que qualquer mudança deve preservar

**Fontes:** `AGENTS.md` (regras NUNCA), ADRs Accepted, contratos testados, red teams das missões anteriores. Cada invariante tem evidência associada. Violar um = regressão de princípio, não apenas bug.

## 1. Privacidade e criptografia

| # | Invariante | Evidência |
|---|---|---|
| I-01 | Entry plaintext nunca persiste (disco, DB, índice, arquivo interno) | I-01: código (`contentEnvelope`), `SEARCH-PRIVACY-ARCHITECTURE.md` |
| I-02 | Envelope cruza disco sempre AES-256-GCM com AAD `associatedData(kind, recordId, schemaVersion)` | I-02: `AesGcmContentCipherTest` |
| I-03 | Keystore é a única fonte de chaves; chave nunca sai do dispositivo | I-03: `AndroidKeystoreKeyProvider` + ADR-035 |
| I-04 | backup Android desligado (`allowBackup="false"`); dataExtractionRules e fullBackupContent presentes | I-04: manifest |
| I-05 | Export plaintext só por escolha explícita (SAF), nunca cópia automática | I-05: `ExportCoordinator` + ADR-018 |
| I-06 | Exclusão permanente remove entry + history associated (cancelled) + qualquer futuro índice/embedding | I-06: `permanentlyDelete` path + `cancelExternalReferences` |
| I-07 | Edição sempre re-cifra (novo envelope); versão no envelope rastreável | I-07: `updateEntry` |
| I-08 | Nenhuma dependência de rede com conteúdo privado | I-08: grep de rede no código + AGENTS.md rules |

## 2. Guarda (write)

| # | Invariante | Evidência |
|---|---|---|
| I-09 | Draft criptografado ANTES da transação de entrada | I-09: `FioService.saveEntry` |
| I-10 | Insert falho não apaga o draft (D12) | I-10: `DraftSurvivesFailedInsertTest` |
| I-11 | Exatamente um draft ativo (`singleton_slot = 1`) | I-11: ADR-029 + schema |
| I-12 | (deletedAt == null) ⇔ (purgeAfter == null) — soft-delete válido | I-12: teste + helpers |
| I-13 | Recent deleted visível 30 dias, depois purge automático | I-13: `purge_after` + `purgeExpired` |

## 3. Encontrar (search)

| # | Invariante | Evidência |
|---|---|---|
| I-14 | Queries, tokens e scores nunca persistem, logam ou vão a analytics | I-14: red team M4 + `FioViewModel` flow-only |
| I-15 | Busca retorna texto original + contexto factual; nunca texto gerado | I-15: `LexicalTokenizer` snippets originais |
| I-16 | Seladas: conteúdo sempre invisível; contagem só sob COUNT_ONLY (default); HIDDEN esconde tudo | I-16: `LocalSearchService` |
| I-17 | Soft-deleted invisível em AMBOS os braços (lexical e vetorial) | I-17: `HybridSearchServiceTest.softDeletedEntriesNeverSurfaceFromAnyArm` |
| I-18 | Braço vetorial herda todos os filtros do lexical (time, returned, sealed) — nunca reintroduz | I-18: teste indexReflects + bug-fix M4 |
| I-19 | "Já voltou" é factual (OPENED count), nunca interpretativo | I-19: `loadReturnsForEntry` + red team |
| I-20 | Busca nunca seleciona entradas para devolução; Returns nunca lê queries | I-20: `SEARCH-ARCHITECTURE.md` + DI |

## 4. Reencontrar (returns)

| # | Invariante | Evidência |
|---|---|---|
| I-21 | Devolução só com consentimento global explícito ENABLED | I-21: engine + ADR-027 |
| I-22 | Uma devolução por vez (cap); 7 dias entre eventos | I-22: `EngineTortureTest` |
| I-23 | Uma notificação, canal low, corpo genérico, sem lembrete | I-23: ADR-011 + código |
| I-24 | "Nunca mais" é permanente e cancela o attempt | I-24: `neverReturn` + ReturnEntity |
| I-25 | Pausa/delete/never cancelam external references (WorkManager) | I-25: `cancelExternalReferences` |
| I-26 | Reboot não cria devolução espúria; reconcile cancela ineligíveis | I-26: reconcile path |

## 5. Produto e UX

| # | Invariante | Evidência |
|---|---|---|
| I-27 | Retriever-not-interpreter: nenhuma resposta gerada, nenhuma interpretação emocional, nenhum chatbot | I-27: AGENTS.md + red team |
| I-28 | Sem streaks, feeds, gamificação, ads, engagement loops | I-28: AGENTS.md rules |
| I-29 | Guardar é abertura e centro; Encontrar/Arquivo são acessíveis em um toque e silenciosos; Settings permanece secundário (ADR-004/048) | I-29: UI |
| I-30 | First Capsule: única confirmação estendida, nunca repetida | I-30: ADR-044 + código |
| I-31 | ADR Accepted > Princípios > Produto > spec > Roadmap > Founder Vision em conflito | I-31: AGENTS.md |
| I-32 | main nunca tocada; feature work em branches de integração/research; PRs antes de merge | I-32: ADR-032 + workflow |

## 6. Engenharia

| # | Invariante | Evidência |
|---|---|---|
| I-33 | Nenhuma mudança de princípio/privacidade sem decisão humana documentada (ADR novo) | I-33: ADR process |
| I-34 | Gates humanos nunca reportados como aprovados sem evidência | I-34: TEST-MAP + AGENTS |
| I-35 | Instrumentados só passam com execução real (nunca "up-to-date" = aprovação) | I-35: TEST-MAP |
| I-36 | Benchmark sintético nunca entra no build do app | I-36: `research/` fora do source set |
| I-37 | Schema migration: exportSchema=true, migration manual testada byte a byte, schema_version em entidades | I-37: `Migration2To3Test` |
| I-38 | Feature flags Engineering para trabalho não estabilizado | I-38: pattern estabelecido |
| I-39 | Editar/excluir existem no detalhe e são descobríveis pelo Arquivo; ações destrutivas não se repetem em cada linha | I-39: ADR-048/049 + UI test |
| I-40 | Ajustes explica consequências antes dos controles e não expõe milestones/linguagem de engenharia | I-40: ADR-049 + UI test |
