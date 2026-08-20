# FIO-PQ-02 — sincronizar NEXT-WORK e HARDENING-QUEUE com o Atlas

**Status:** DONE
**Tipo:** doc
**Branch:** `integration/codex-pq02-queues-20260820`
**Evidência mínima exigida:** as duas filas históricas deixam de anunciar campanhas antigas como trabalho atual e encaminham toda execução para `packets/EXECUTION-QUEUE.md`.

## 1. Objetivo

Remover o drift pós-M4/M5 das filas legadas. Preservar seus resultados como histórico, mas manter uma única fila operacional vigente.

## 2. Contrato

- `packets/EXECUTION-QUEUE.md` é a única autoridade de ordem corrente.
- Gates humanos continuam abertos sem evidência.
- Resultados M1–M5 são preservados, sem reescrever a história.
- Nenhum código, schema, dependência, arquitetura, produto ou `main` será alterado.

## 3. Contexto técnico

Arquivos permitidos: `plans/NEXT-WORK.md`, `plans/HARDENING-QUEUE.md`, `packets/FIO-PQ-02.md` e `packets/EXECUTION-QUEUE.md`.

## 4. Critérios de aceitação

1. NEXT-WORK identifica M4/M5 e aponta o packet NOW.
2. HARDENING-QUEUE é explicitamente histórica e não mantém campanha antiga como NOW.
3. A fila central marca PQ-02 concluído e promove PQ-03.
4. Nenhum gate humano é declarado aprovado.

## 5. Riscos e portas de escape

Não apagar resultados históricos; resumir e manter links para evidências detalhadas. Parar se surgir conflito com o Atlas.

## 6. Evidence log

| Data | O que | Output |
|---|---|---|
| 2026-08-20 | Filas comparadas ao Atlas | Drift M4/M5 confirmado |
| 2026-08-20 | Filas sincronizadas | Autoridade operacional única em `packets/EXECUTION-QUEUE.md` |
