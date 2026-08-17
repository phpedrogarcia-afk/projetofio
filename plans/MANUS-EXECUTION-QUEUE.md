# Manus Execution Queue — ProjetoFio

Regras: NOW tem exatamente UMA fatia. NEXT tem no máximo 3. CHECKPOINTs listam SHA, testes e data.

## NOW

- [Fase 3] Integração do engine temporal: conectar o ReturnPolicy da UI (7 rótulos, ADR-043) ao schema — campo `returnPolicy` em Entry (migração Room schema 4) — e à futura integração com o TimeReturnEngine do PR #1; mapear cada rótulo ao vocabulário canônico (ELIGIBLE / explicit / anchored / never-return).

## NEXT

1. [Fase 4] Primeira Cápsula persistente: gate first-save via `settings.firstCapsuleAcknowledged` no schema 4; copy canônica única (ADR-044).
2. [Fase 6] Data-Âncora: devolução no aniversário da nota, respeitando cap/consent/never (ADR-043).
3. [Fase 11] Ritual do Fio: Planned + gates (ADR-047) — tela de virada de ano lendo as notas do ano, sem gamificação.

## LATER

- Leitura de Carta (TalkBack com dignidade de carta): BLOCKED — human/device validation.
- Devolução selada (biometria na devolução): pronta na UI; hook real depende do engine no PR #1.

## BLOCKED — HUMAN/DEVICE VALIDATION REQUIRED

- Merge do PR #1 (codex/v0-time-only-checkpoint, M2/M3/M4): revisão e merge são do fundador. Nossas fases de UI (2–4) dependem da coordenação de branches decidida em `plans/2026-08-16-design-ui-branch-decision.md`.
- Gates manuais M1 (TalkBack real, segundo OEM, revisão independente de cripto): documentar como `BLOCKED — HUMAN/DEVICE VALIDATION REQUIRED`, não declarar fechados.
- Pilot (FASE 12): instrumentação fechada, consentimento, recrutamento — depende do fundador.


## DONE

- Fase 0–1: Verdade do repositório (PR #1 codex descoberto, fora de main), ADRs 043–047, export v1.0, fila + idea inbox (16/08)
- Fases 2.3–2.5: TalkBack labels, error pill, leitura de carta + gesto "Devolver" com reescrita pré-carregada (commit d79a476); build + 20 testes verdes
- Fase 2.2: Home redesenhada (commit 9e9c15a): sem bottom bar (pilha + menu ⋯), editor zero-fricção, seletor temporal ADR-043 (ReturnPolicy), copy primeira cápsula ADR-044, pill "Guardado." ADR-014, arquivo tipográfico agrupado por mês com distância temporal S-2, leitura de nota em Fraunces; ícones vetoriais; build + 20 testes verdes.
- Fase 2.1: Design system foundation — `ui/theme` completo (Color/Theme/Type), fontes Fraunces+Inter em res/font (OFL), FioSpace/FioRadius; BUILD verde, 20 unit tests verdes, APK debug gerado (commit 2ceaa9f, branch feature/design-ux-v1)

