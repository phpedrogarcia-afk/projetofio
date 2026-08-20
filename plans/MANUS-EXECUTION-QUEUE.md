# Manus Execution Queue — ProjetoFio

Regras: NOW tem exatamente UMA fatia. NEXT tem no máximo 3. CHECKPOINTs listam SHA, testes e data.

## NOW

- Missão Manus 2 concluída nesta branch (`integration/manus-pre-codex-20260817`): Red Team de produto (12 princípios, 11 PASS diretos), correções triviais (Reduce Motion na Pátina, copy neutra), documentação atualizada, pacote de piloto (`pilot/`) e handoff Codex. Próximo passo real: merge do PR #1 (decisão do fundador) e execução dos gates humanos descritos no `MANUS-PRE-CODEX-FINAL.md`.

> **Correção factual de 2026-08-20:** a conclusão abaixo estava errada. O design v1 criou `ReturnPolicy` somente no estado da tela; ao guardar, apenas o texto chega ao domínio e a entrada recebe o default `ELIGIBLE`. Períodos, data escolhida e `Nunca` não são persistidos nem honrados. A decisão deixou de ser “levíssima” e está isolada no `FIO-P19` como P0 de integridade, sem implementação automática.

## NEXT

1. PR #1: merge pelo fundador (foundation: schema 3, engine M2–M4, imports).
2. PR #3: atualizar título/corpo para o estado real (50 arquivos; começou design-only e incorporou a implementação) e abrir para revisão.
3. Gates humanos do handoff: TalkBack real (~10 min em dispositivo), large font, dark mode manual, M2AndroidContractTest em AVD.

## LATER

- Leitura de Carta (TalkBack com dignidade de carta): BLOCKED — human/device validation.
- Devolução selada (biometria na devolução): pronta na UI; hook real depende do engine no PR #1.

## BLOCKED — HUMAN/DEVICE VALIDATION REQUIRED

- Merge do PR #1 (codex/v0-time-only-checkpoint, M2/M3/M4): revisão e merge são do fundador. Nossas fases de UI (2–4) dependem da coordenação de branches decidida em `plans/2026-08-16-design-ui-branch-decision.md`.
- Gates manuais M1 (TalkBack real, segundo OEM, revisão independente de cripto): documentar como `BLOCKED — HUMAN/DEVICE VALIDATION REQUIRED`, não declarar fechados.
- Pilot (FASE 12): instrumentação fechada, consentimento, recrutamento — depende do fundador.


## DONE

- Missões Manus 1 (hardening: 17 campanhas, 99 testes, finding P0 corrigido) e 2 (Red Team + piloto + handoff) — relatórios `MANUS-HARDENING-FINAL.md` e `MANUS-PRE-CODEX-FINAL.md`
- Fase 0–1: Verdade do repositório (PR #1 codex descoberto, fora de main), ADRs 043–047, export v1.0, fila + idea inbox (16/08)
- Fases 2.3–2.5: TalkBack labels, error pill, leitura de carta + gesto "Devolver" com reescrita pré-carregada (commit d79a476); build + 20 testes verdes
- Fase 2.2: Home redesenhada (commit 9e9c15a): sem bottom bar (pilha + menu ⋯), editor zero-fricção, seletor temporal ADR-043 (ReturnPolicy), copy primeira cápsula ADR-044, pill "Guardado." ADR-014, arquivo tipográfico agrupado por mês com distância temporal S-2, leitura de nota em Fraunces; ícones vetoriais; build + 20 testes verdes.
- Fase 2.1: Design system foundation — `ui/theme` completo (Color/Theme/Type), fontes Fraunces+Inter em res/font (OFL), FioSpace/FioRadius; BUILD verde, 20 unit tests verdes, APK debug gerado (commit 2ceaa9f, branch feature/design-ux-v1)

