# EXECUTION-QUEUE — fila de execução atual (manter no topo sempre verdadeiro)

**Atualizado em:** 2026-08-20 (FIO-PQ-02 concluído). A fila é viva: quando um packet vira NOW, mover para o topo e remover de DONE do topo. Histórico de packets concluídos fica em `plans/` e nos documentos `packets/FIO-*.md`.

## Fila atual

| Ordem | Packet | Tipo | Status |
|---|---|---|---|
| 1 | FIO-PQ-03 (D-1..D-5 propostas ao fundador) | doc | **NOW** |
| 4 | FIO-P06 (busca lexical em escala 1k-10k) | code | READY |
| 5 | FIO-P07 (import hardening OOM) | code | READY |
| 6 | FIO-P10 (migration 3→4 simulação) | code | READY |
| 7 | FIO-P01/P02/P04/P05/P09 (device gates) | device | READY (bloqueado por aparelho) |
| 8 | FIO-PILOT-01/02 (piloto) | human | WAITING fundador |
| 9 | FIO-P03 (crypto review) | device/review | depois de P01 |
| 10 | FIO-P12 (device gate semântica) | device | depois de D-3 + licença HF |
| 11 | FIO-P08/P11/P14/P15/P16 | product | depois das decisões D-* |

## Concluídos nesta fila

| Packet | Tipo | Estado | Evidência |
|---|---|---|---|
| FIO-PQ-01 (PROJECT-STATE atualizado) | doc | DONE | `packets/FIO-PQ-01.md`, `plans/PROJECT-STATE.md` |
| FIO-PQ-02 (NEXT-WORK/HARDENING-QUEUE sincronizados) | doc | DONE | `packets/FIO-PQ-02.md`, `plans/NEXT-WORK.md`, `plans/HARDENING-QUEUE.md` |

## Histórico (contexto)

- M1 (2026-08-10..12): baseline Android — DONE.
- M2 (2026-08-13): returns scheduling + privacy surfaces — DONE (engineering; validation pending device).
- M3 (2026-08-14): import Markdown/Text com rollback — DONE.
- M4 (2026-08-15..19): busca lexical + protótipo híbrido + D12 — DONE (134 testes).
- M5 (2026-08-19): Atlas — DONE (esta missão; sem código de app).

## Regra de entrada na fila

Nenhum trabalho entra na fila sem packet escrito (`PACKET-TEMPLATE.md`) e sem dependências resolvidas (`DEPENDENCIES.md`). Trabalho ad-hoc sem packet é proibido por protocolo (`docs/atlas/CODEX-EXECUTION-PROTOCOL.md`).
