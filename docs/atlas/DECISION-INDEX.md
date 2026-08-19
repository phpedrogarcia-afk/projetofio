# DECISION-INDEX — índice de decisões (não repete os ADRs)

**Fonte:** `docs/DECISIONS.md` (787 linhas, ADR-001..047) · **Examinado em:** 2026-08-19 · **Evidência:** `E4` (ledger lido integralmente).

## 1. Índice

| ADR | Assunto | Status | Arquivos afetados | Depende de |
|---|---|---|---|---|
| 001 | Nome "Fio" | Accepted | docs/01 | — |
| 002 | Continuidade autobiográfica (não hábito) | Accepted | docs/01-02 | — |
| 003 | IA seleciona, não interpreta | Accepted | docs/02, AGENTS | — |
| 004 | Arquivo completo mas secundário | Accepted | docs/04 | — |
| 005 | iOS-first local-first | **Superseded** (033) | — | — |
| 006 | Sem servidor para o primeiro proof | Accepted | docs/05 | — |
| 007 | Embeddings são conteúdo privado | Accepted | docs/08 | — |
| 008 | Tempo é core; Semantic Resonance removível | Accepted | docs/07, docs/10 | — |
| 009 | Excluir Contrast do V0 | Accepted | docs/04 | — |
| 010 | Controles explícitos de devolução > algoritmos | Accepted | docs/07 | — |
| 011 | Uma notificação genérica, sem lembrete | Accepted | returns/, docs/07 | — |
| 012 | Feedback do piloto fora da tela de Return | Accepted | pilot/ | — |
| 013 | Sem texto cursivo no corpo | Accepted | docs/03 | — |
| 014 | Remover animação de lock/save grande | Accepted | ui/ | — |
| 015 | Import faz parte do V0, delimitado | Accepted | domain/, application/ | — |
| 016 | Piloto compara Time e Resonance dentro do participante | Accepted | pilot/ | — |
| 017 | Analytics content-free e de schema fechado | Accepted | docs/09 | — |
| 018 | Exportar e apagar permanentemente | Accepted | application/, docs/06 | — |
| 019 | Congelar Book e Legacy | **Frozen** | docs/11 | — |
| 020 | Filosofia não muda em silêncio | Accepted | docs/02 | — |
| 021 | Execution plans para trabalho cross-cutting | Accepted | plans/ | — |
| 022 | Camada de persistência local | **Superseded** (033) | — | — |
| 023 | Design criptográfico local do registro | Accepted (Apple→Android: ver 033/035) | crypto/ | 033, 035 |
| 024 | Selecionar modelo de embedding on-device | **Deferred** | research/ | 040/041 |
| 025 | Sync, recovery, deleção V1 | Deferred | — | piloto |
| 026 | Monetização | Deferred | — | — |
| 027 | Consentimento global explícito de devolução | Accepted | docs/07, app_settings | — |
| 028 | Validação só em toolchain Apple | **Superseded** (033) | — | — |
| 029 | Exatamente um draft ativo criptografado | Accepted | drafts, FioService | — |
| 030 | Export UTF-8 explícito (M1) | Accepted (Apple→Android: 033/035) | export v1.0 | 046 |
| 031 | Privacy cover padrão + app lock opcional | Accepted | security/, PrivacyCover | — |
| 032 | Git e publicação separadamente autorizados | Accepted | release hygiene | — |
| 033 | Pivot iOS → Android | **Accepted** (pivot) | AGENTS, build, manifest | — |
| 034 | Stack nativa Android + Room | Accepted | Room 2.8.4, Compose | 033 |
| 035 | Proteção local Android, lock, backup, export | Accepted | crypto/, security/, manifest | 033 |
| 036 | Matriz de execução/dispositivo Android | Accepted ( Proposed no código ) | PROJECT-MANIFEST | 034 |
| 037 | Aceitação de engenharia separada de validação externa | Accepted | plans/, reports | — |
| 038 | Scheduling Android delimitado; M2 validation-only | Accepted | WorkManager, TimeReturns | 034 |
| 039 | M3 import limitado a formatos locais + rollback atômico | Accepted | ImportService | 034 |
| 040 | M4 isolado como benchmark sintético offline | Accepted | research/search/ | 024 |
| 041 | Fim do caminho semântico V0 sem modelo | Accepted | ADR-024 permanece Deferred | 040 |
| 042 | Preservar visão longa de tempo e Legacy | Accepted | docs/11 | 019 |
| 043 | Data absoluta como âncora; UI temporal mapeada ao vocabulário | Accepted | ui/ (TimeSheet, DateSheet) | 010 |
| 044 | First Capsule onboarding (S-3) | Accepted | ui/ (save copy única) | 027 |
| 045 | Pátina Temporal no motivo botânico (S-4 revisado) | Accepted | ui/ BotanicalMotif | 042 |
| 046 | Promessa de longevidade do formato de export | Accepted | docs/export-format.md, FioService | 030 |
| 047 | Ritual do Fio (S-1) Planned, gated em maturidade | **Planned** | — | 042, arquivo |

## 2. Decisões pendentes para o fundador (abertas no momento do exame)

| # | Assunto | Contexto | Recomendação da última missão |
|---|---|---|---|
| D-1 | Notas seladas na busca: invisibilidade total ou contagem opaca? | `SealedSearchBehavior` existe no domínio; `isSealed` lambda default `{false}`; threat model feito | Invisível por padrão; contagem só atrás de toggle explícito |
| D-2 | Analytics remoto de busca/app | local-only hoje; toggle local existe | Local-first até decisão; schema fechado ADR-017 |
| D-3 | Semântica em produção (Embarcar EmbeddingGemma?) | ADR-024 Deferred; kill criterion documentado | Medir em aparelho com NPU antes (FIO-P12) |
| D-4 | Download do modelo on-demand com consentimento explícito | modelo ~300–580MB fora do APK; gate HF | ADR próprio se D-3 for positiva |
| D-5 (herdada) | ReturnPolicy schema 4 (UI-only), zero-width, analytics remoto transporte | registradas em PROJECT-STATE | — |

## 3. Regras de leitura para Codex

Fontes conflitantes resolvem-se pela ordem do AGENTS.md: ADRs Accepted > Princípios > Produto > spec de domínio > Roadmap > Founder Vision. Nenhuma mudança de princípio/privacidade sem decisão humana. Não interpretar um ADR Superseded como vigente; não reabrir Deferred sem novo ADR.
