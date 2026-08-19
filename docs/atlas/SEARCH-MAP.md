# SEARCH-MAP — busca no Fio (separando matemática de política)

**Estado examinado:** HEAD `9cfd5f1` (Missão 4 concluída) · **Evidência:** `E4` (`search/`, `LocalSearchServiceTest`, `HybridSearchServiceTest`, `LexicalTokenizerTest`) + `E1` (`docs/search/*`) + `E3` (benchmark sintético).

## 1. As quatro camadas — e o que cada uma é

```text
┌─────────────────────────────────────────────────────────────┐
│ SEARCH POLICY (produto) — nunca compartilhada com Returns   │
│  - escopo, seladas, filtros, "o que entra"                  │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│ LEXICAL (produção, M4)                                      │
│  LexicalTokenizer: NFD + \p{Mn} (café↔cafe)                 │
│    whole-token ordered match · prefix SÓ no último token    │
│    (digitação em andamento) · snippets do TEXTO ORIGINAL    │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│ SEMANTIC (protótipo removível M4 — feature flag futuro)     │
│  EmbeddingProvider (interface pluggável, zero pesos shipados)│
│  FakeEmbeddingProvider: hash→unit vector (testes)           │
│  Candidato futuro: embeddinggemma-300m TFLite (E2/E3)       │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│ FUSÃO (protótipo): RRF k=60 — `reciprocalRankFusion()`      │
│  braço vetorial HERDA todos os filtros do lexical           │
│  (seladas, soft-deleted, time, returned) ANTES da fusão     │
│  fallback total ao lexical se provider falhar               │
└─────────────────────────────────────────────────────────────┘
```

## 2. LEXICAL — comportamento real

| Aspecto | Implementação |
|---|---|
| Normalização | NFD + remoção `\p{Mn}` (diacríticos PT-BR caem); match case-insensitive |
| Ordem | todos os query tokens presentes no texto, **na ordem da query** |
| Prefixo | permitido **apenas no último token** (`"trabalho no"` casa `"trabalho novo"` e `"trabalho nos anos"`; `"tra novo"` não) |
| Snippets | cortados do texto original (não normalizado), com `…`; SelectionContainer na UI |
| Escaneamento | scan-on-demand Option A: `loadSearchableEntries` (read-only) → decrypt → tokenize → match; plaintext descarta ao fim |
| Latência | p95 <1 ms no benchmark sintético (60 entries); escala real = gate futuro |

## 3. SEMANTIC — estado atual (pesquisa)

Nenhum peso embarcado. O protótipo carrega `FakeEmbeddingProvider` (determinístico: mesmo texto = 1,0; textos diferentes ≈ aleatório com floor 0,1). Benchmark sintético (`research/search/run_benchmark.py`, replicando o algoritmo Kotlin exato):

| Config | Recall@5 | Recall@10 | P@5 | MRR | p95 |
|---|---|---|---|---|---|
| lexical_v1 | 0,098 | 0,109 | 0,060 | 0,200 | <1 ms |
| semantic (proxy e5-large) | 0,797 | 0,917 | 0,573 | 1,000 | ~2,6 s |
| hybrid RRF k=60 | 0,797 | 0,917 | 0,573 | 1,000 | ~2,6 s |

Kill criterion (`research/search/RESEARCH-LOG.md`): embarcar só se recall@10 ≥ +10pp **ou** MRR ≥ +0,08 vs lexical, RAM ≤ +200MB, latência ≤ 500 ms em aparelho com NPU. **Nenhum dos três medidos em hardware ainda (E5 missing).**

## 4. TEMPORAL (filtro de busca)

`SearchTimeFilter`: por data de criação original (`originalCreatedAt`), com buckets/períodos definidos no domínio. Aplicado no `matchesFilters` **e no braço vetorial** (o fix do bug de regressão da Missão 4: sem isso o vetor reintroduzia entries que o filtro temporal descartaria — violando "a busca sempre reflete a versão atual + filtros").

## 5. RETURN HISTORY na busca

`loadReturnsForEntry` (read-only, sem schema migration): conta ciclos OPENED por entry → badge factual "Já voltou N vez(es)". Filtro `ReturnedFilter` (ANY / EVER_RETURNED / NEVER_RETURNED) aplica-se em ambos os braços.

## 6. FILTERS (contrato de visibilidade)

| Entrada | Na busca? |
|---|---|
| Active (never returned / já voltou / scheduled) | sim |
| Soft-deleted (Recently Deleted, 30 dias) | **não** — invisível a ambos os braços |
| Purged (permanente) | **nunca** — row não existe; history cancelada |
| Seladas | conteúdo invisível; contagem opaca `sealedCount` sob `COUNT_ONLY` (default); HIDDEN esconde até a contagem — **D-1 decidirá o padrão** |

## 7. RANKING

Lexical puro: ordem de scan (estável por ID). Híbrido: RRF k=60 `score = 1/(k+rank)`, desempate por score vetorial depois lexical. Sem boosting de recência, sem TF-IDF, sem BM25 — decisão deliberada de minimalismo (se precisar, BM25 é `USE LIBRARY` candidato futuro, não custom).

## 8. SEARCH POLICY vs RETURN POLICY (a parede)

| | Busca | Returns |
|---|---|---|
| Fonte de seleção | query do usuário (runtime, efêmera) | TimeReturnEngine (cap, buckets, bootstrap) |
| Pode usar o outro como fonte? | **não** | **não** |
| Compartilha | Room read-only, clock, zone, models de estado | idem |
| Persiste estado entre si? | **não** (busca não escreve nada) | escreve `returns` (factual) |
| Interpreta? | **não** (snippets originais, datas factuais) | **não** (notificação genérica) |

## 9. O que a busca NÃO é (limites reais)

Não é FTS5 (rejeitada V1). Não é vetor em produção. Não tem histórico de queries. Não tem "Pesquisas recentes". Não tem analytics de conteúdo (local-first schema fechado eventual, DECISION REQUIRED D-2). Não responde perguntas ("quando comecei…?" retorna hits, não resposta). Não toca `ReturnEngine` na seleção. Queries nunca chegam a Room, Keystore, logs, analytics ou disco.
