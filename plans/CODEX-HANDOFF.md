# CODEX-HANDOFF — instruções de retomada para o Codex

Este documento é a ponte entre o trabalho das duas missões Manus e a retomada pelo Codex. Ele pressupõe que o Codex já obedece a `AGENTS.md`; o que ele adiciona é o **contexto fresco** que nenhuma missão anterior do Codex conhece.

## 1. O que aconteceu no intervalo

O repositório passou por duas missões de engenharia conduzidas por outro agente (Manus), ambas concluídas e documentadas, nenhuma tocando `main`:

- **Missão 1 — Integration Rehearsal & Hardening** (branch `integration/manus-rehearsal-20260817`): 17 campanhas de tortura de dados, falhas, migração, performance e cripto. Resultado: 99 testes verdes, um finding P0 corrigido (`AesGcmContentCipher.seal()` substituía surrogate halves por `0x3F` silenciosamente; agora rejeita com `CryptoFailure.InvalidPlaintext`), e a pilha inteira endurecida sem nenhuma feature nova. Relatório: `MANUS-HARDENING-FINAL.md`.
- **Missão 2 — Product Red Team & Pilot Readiness** (branch `integration/manus-pre-codex-20260817`, esta): red team contra os 12 princípios (11 PASS diretos, 1 WEAK corrigido — Reduce Motion na Pátina Temporal), correção de copy interpretativa, reparo de drift documental (`MANUS-EXECUTION-QUEUE.md`, `AGENTS.md` estado), pacote de piloto em `pilot/`, handoff completo. Relatório: `MANUS-PRE-CODEX-FINAL.md`.

As duas branches compartilham a mesma base (`feature/design-ux-v1` integrada) e **não conflitam entre si**; a `integration/manus-pre-codex-20260817` carrega a Missão 1 como base (cherry-pick limpo confirmado no rebase local).

## 2. O que mudou no código (diff relevante)

- `AesGcmContentCipher.seal()` — valida surrogate halves com `REPLACE_ON_MALFORMED`→`REPORT` (P0 fix).
- `FioApp.kt` — `BotanicalMotif` agora respeita Reduce Motion; copy do retorno imediato neutralizada ("Reescrever esta nota agora?").
- `FioDao`/`PerformanceTest.kt` — novo teste de performance (10k entries, scans <0.32s, rollback <0.5s).
- 15 novos arquivos de teste de tortura (campanhas 1–8, 16), 5 relatórios por campanha em `plans/campaigns/`.
- Temas/ícones do design v1 (já estavam na branch de design; o design virou código).

## 3. O que o Codex NÃO deve fazer ao retomar

- Não mergear nada em `main` — decisão do fundador.
- Não reescrever o README sem o fundador (ele carrega a narrativa M1/M4 deliberada).
- Não reformatar o repo inteiro para ktlint/detekt (regra da Missão 2); gate incremental apenas.
- Não iniciar schema 4 sem aprovação do FIO-P19. O seletor UI-only é um P0 de confiança porque períodos/data/Nunca não são aplicados; não o descrever como aceito ou funcional.
- Não reportar gates humanos como aprovados; eles permanecem `BLOCKED — HUMAN/DEVICE VALIDATION REQUIRED`.
- Não iniciar o piloto — o pacote `pilot/` é preparação, não autorização.

## 4. Ponto de partida recomendado

O Codex deveria começar lendo, nesta ordem: `AGENTS.md` → `plans/PROJECT-STATE.md` → `plans/NEXT-WORK.md` → `plans/IDEA-INBOX.md` → `MANUS-PRE-CODEX-FINAL.md`. A primeira tarefa de código autorizada é apenas o que o fundador desbloquear em `NEXT-WORK.md` § THEN.
