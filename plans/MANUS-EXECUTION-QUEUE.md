# Manus Execution Queue — ProjetoFio

Regras: NOW tem exatamente UMA fatia. NEXT tem no máximo 3. CHECKPOINTs listam SHA, testes e data.

## NOW

- [Fase 2.2] Home redesenhada: escrita zero-fricção + seletor temporal "Pode voltar" (ADR-043) + confirmação sutil única na primeira nota (ADR-044), sobre o FioApp atual (main antigo) — aguardando decisão A/B/C do fundador sobre o PR #1; os tokens já estão disponíveis via MaterialTheme.

## NEXT

1. [Fase 2.3] Menu ⋯ (Arquivo/Configurações) + pill discreto "Guardado." + estados de interação do sistema.
2. [Fase 2.4] Arquivo tipográfico agrupado por mês com distância temporal secundária (S-2 revisada), sem cards.
3. [Fase 2.5] Nota e devolução no novo sistema (FioDisplayDate, leitura de carta, selo).

## LATER

4. [Fase 2] Menu ⋯ (Arquivo/Configurações) + confirmação Guardado discreta.
5. [Fase 2] Arquivo tipográfico agrupado por mês com distância temporal secundária (S-2 revisada).
6. [Fase 3] Sistema temporal da Home mapeado a ReturnMode/Remember later (ADR-043).
7. [Fase 4] Primeira Cápsula (ADR-044): estado first-save, copy canônica única.
8. [Fase 6] Data-Âncora como candidato especial respeitando cap/consent/never (ADR-043).
9. [Fase 8] Leitura de Carta: TalkBack com dignidade de carta (BLOCKED — human/device validation; gates manuais M1).
10. [Fase 9] Export v1.0: version marker + checksum no output.
11. [Fase 10] Pátina Temporal: módulo decorativo determinístico e removível (ADR-045).
12. [Fase 11] Ritual do Fio: Planned, gated (ADR-047).

## BLOCKED — HUMAN/DEVICE VALIDATION REQUIRED

- Merge do PR #1 (codex/v0-time-only-checkpoint, M2/M3/M4): revisão e merge são do fundador. Nossas fases de UI (2–4) dependem da coordenação de branches decidida em `plans/2026-08-16-design-ui-branch-decision.md`.
- Gates manuais M1 (TalkBack real, segundo OEM, revisão independente de cripto): documentar como `BLOCKED — HUMAN/DEVICE VALIDATION REQUIRED`, não declarar fechados.
- Pilot (FASE 12): instrumentação fechada, consentimento, recrutamento — depende do fundador.


## DONE

- Fase 0–1: Verdade do repositório (PR #1 codex descoberto, fora de main), ADRs 043–047, export v1.0, fila + idea inbox (16/08)
- Fase 2.1: Design system foundation — `ui/theme` completo (Color/Theme/Type), fontes Fraunces+Inter em res/font (OFL), FioSpace/FioRadius; BUILD verde, 20 unit tests verdes, APK debug gerado (commit 2ceaa9f, branch feature/design-ux-v1)

