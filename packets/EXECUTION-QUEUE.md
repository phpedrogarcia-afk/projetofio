# EXECUTION-QUEUE — fila de execução atual (manter no topo sempre verdadeiro)

**Atualizado em:** 2026-08-20 (prioridade explícita do fundador: funções básicas e Ajustes compreensíveis). A fila é viva: quando um packet vira NOW, mover para o topo e remover de DONE do topo. Histórico de packets concluídos fica em `plans/` e nos documentos `packets/FIO-*.md`.

## Fila atual

| Ordem | Packet | Tipo | Status |
|---|---|---|---|
| 1 | FIO-P01/P02/P04/P05/P09 (device gates) | device | BLOCKED — aparelho/humano; context pack requerido antes de executar |
| 2 | FIO-PILOT-01/02 (piloto) | human | WAITING fundador |
| 3 | FIO-P03 (crypto review) | device/review | depois de P01 |
| 4 | FIO-P12 (device gate semântica) | device | depois de D-3 + licença HF |
| 5 | FIO-P08/P11/P14/P15/P16 | product | aguardando aprovação das decisões D-1..D-5 |

## Concluídos nesta fila

| Packet | Tipo | Estado | Evidência |
|---|---|---|---|
| FIO-PQ-03 (propostas D-1..D-5 estruturadas) | doc | DONE | `docs/proposals/OPEN-DECISIONS-PROPOSALS.md` |
| FIO-P07 (import hardening OOM/streaming) | code | DONE | `LocalImportParserTest.kt` (magic containers, bounds), 100% verde |
| FIO-P06 (busca lexical em escala 1k-10k) | code | DONE | `SearchScaleValidationTest.kt` (1k/10k p95 bounds), 100% verde |
| FIO-P10 (migration 3→4 simulação) | code | DONE | Integrada em FIO-P19 (Schema 4 e MIGRATION_3_4) |
| FIO-P19 (escolhas temporais verdadeiras — A1) | code/decision | DONE | ADR-052, Schema 4, 152 unitários + 31/32 instrumentados verdes |
| FIO-PQ-01 (PROJECT-STATE atualizado) | doc | DONE | `packets/FIO-PQ-01.md`, `plans/PROJECT-STATE.md` |
| FIO-PQ-02 (NEXT-WORK/HARDENING-QUEUE sincronizados) | doc | DONE | `packets/FIO-PQ-02.md`, `plans/NEXT-WORK.md`, `plans/HARDENING-QUEUE.md` |
| FIO-PB-01 (build verificável + reinstalação autorizada) | device | DONE | `packets/FIO-PB-01.md`, 134 unitários verdes, instalação ADB preservando dados |
| FIO-P17 (Guardar / Encontrar / Arquivo visíveis) | code/device | DONE | `packets/FIO-P17.md`, 134 unitários + 26 instrumentados verdes, AVD API 26 e Poco API 33 |
| FIO-P18 (funções básicas visíveis + Ajustes claros) | code/UX/device | DONE (engineering) | 134 unitários + 28 instrumentados; AVD API 26; APK P18 instalado no Poco preservando dados |

- M4 (2026-08-15..19): busca lexical + protótipo híbrido + D12 — DONE (134 testes).
- M5 (2026-08-19): Atlas — DONE (esta missão; sem código de app).

## Regra de entrada na fila

Nenhum trabalho entra na fila sem packet escrito (`PACKET-TEMPLATE.md`) e sem dependências resolvidas (`DEPENDENCIES.md`). Trabalho ad-hoc sem packet é proibido por protocolo (`docs/atlas/CODEX-EXECUTION-PROTOCOL.md`).
