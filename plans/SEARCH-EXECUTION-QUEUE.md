# SEARCH-EXECUTION-QUEUE — Missão 4 (Manus)

**Branch:** `integration/manus-search-20260819` · **Criada:** 2026-08-19 · Base: `integration/manus-ux-refinement-20260819` (101 testes verdes, 12/12 princípios PASS)

Regra: NOW = exatamente 1 · NEXT ≤ 3 · RESEARCH = sem decisão · DECISION REQUIRED = humano · DONE = SHA + evidência.

## Estado atual

### NOW
- **S2** — threat model Search + Sealed Notes (`docs/search/SEARCH-SEALED-THREAT-MODEL.md`) + `SEARCH-PRIVACY-ARCHITECTURE.md`

### RESEARCH (ainda sem decisão)
- FTS5 vs scan local vs índice protegido (benchmark lexical manda)
- Modelo de embeddings on-device PT-BR (2026, §29–31)
- "lembrança" vs "entrada" na ReturnScreen (M1 — recomendação: manter, não substituir global)

### DECISION REQUIRED
- Analytics remoto de search (continua fora; analytics local operacional apenas se aprovado)
- Search + sealed notes (dependerá do threat model; se >1 solução defensável → DECISION REQUIRED)
- Persistir histórico de pesquisas (hipótese: NÃO, §10)
- Semântica nas devoluções (proibido nesta missão, §88)

### DONE
- S0 — branch criada, baseline OK, `docs/search/SEARCH-ARCHITECTURE.md` com definições canônicas Guardar/Encontrar/Reencontrar + princípio "search retrieves, does not interpret" (commit 7669d0a)
- D12 — fechado: `DraftSurvivesFailedInsertTest` (3 contratos, incluindo "o estado exato do editor substitui o draft antigo antes da transação"); projeção read-only `loadReturnsForEntry` em `ReturnRepository`/`FioDao`/`RoomFioRepository` sem migração de schema; auditoria em `research/search/RETURN-HISTORY-AUDIT.md` (commit a2ad34f). Suíte: **104 testes verdes, 0 falhas** (3 novos).

## Ordem recomendada (da missão)

S0 state discovery → S1 return history model → S2 threat model → S3 lexical baseline → S4 search UX → S5 temporal filters → S6 already-returned filter → S7 PT-BR benchmark → S8 model comparison → S9 semantic prototype → S10 hybrid ranking → S11 scale/delete/edit/migration torture → S12 accessibility → S13 final red team → S14 codex handoff.

## Histórico

- 2026-08-19: missão iniciada; branch criada; S0 concluído (7669d0a).
- 2026-08-19: D12 fechado; `loadReturnsForEntry` (read-only, search lens) sem schema change; pushed; 104 testes verdes (a2ad34f).
