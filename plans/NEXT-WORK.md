# NEXT-WORK

**Atualizado em:** 2026-08-17 · **Branch:** `integration/manus-pre-codex-20260817` · **Checkpoint:** `c688cfb` · 99 testes verdes.

## NOW

1. **Fundador executa os gates humanos do handoff** (`MANUS-PRE-CODEX-FINAL.md` § gates): TalkBack real no dispositivo (~10 min), large font, dark mode manual, `M2AndroidContractTest` em AVD/API 36. Nada de código começa antes de um gate ser fechado com evidência.

## THEN

1. **Fundador revê e mergeia o PR #1** (`codex/v0-time-only-checkpoint`) — a fundação (schema 3, engine M2–M4, import) precisa estar em `main` antes de qualquer integração subsequente.
2. **Fundador atualiza título/corpo do PR #3** (texto redigido em `plans/DOCUMENTATION-DRIFT-AND-PR-CLEANUP.md` §2) e abre para revisão; em seguida decide o veículo das branches Manus (recomendação: um PR único acumulando as duas missões).
3. **Fundador decide as 3 decisões abertas** (nenhuma bloqueia código): `ReturnPolicy` UI-only até schema 4 (DECISION REQUIRED levíssima, reavaliar em schema 4); conteúdo só-ZW aceito por `isBlank()` (decisão de produto); analytics remoto do piloto (DECISION REQUIRED — fronteira local-first).

## BLOCKED — humano/externo

- Piloto real (participantes, consentimento legal, APK de distribuição): `pilot/` pronto, autorizado só pelo fundador.
- Consentimento: `pilot/CONSENT-DRAFT.md` — HUMAN / LEGAL REVIEW REQUIRED.
- Screenshot regression (Paparazzi): opcional, não iniciado; não iniciar antes do merge do PR #1.

## DO NOT START

Não iniciar: schema 4 / campo `returnPolicy` em Entry (trade-off atual é UI-only e aceito); motor semântico/M4-v2 (sem modelo v0, ADR-041); Ritual do Fio (Planned, dependências não cumpridas); importação de dados de terceiros (coorte B usa só material próprio); qualquer feature de "sugerir momentos" (IA interpretativa, princípio 2); reformatação em massa para ktlint/detekt (regra da Missão 2).
