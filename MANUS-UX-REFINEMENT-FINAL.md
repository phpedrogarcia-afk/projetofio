# MANUS MISSION 3 — UX Observatory, Interaction Architecture & Refinement em ciclos

**Branch:** `integration/manus-ux-refinement-20260819` · **Base:** `integration/manus-pre-codex-20260817` (Missões 1 e 2 como baseline) · **Autor:** Manus AI · **Data:** 19 de agosto de 2026

## Resumo executivo

A terceira missão Manus sobre o ProjetoFio trocou o hardening da Missão 1 e o red team de produto da Missão 2 por uma missão de **observatório e refinamento contínuo de UX**: benchmarks de produtos maduros, inventário de superfícies, simulação de dogfood em fluxo real e 14 ciclos de refinamento (B–N) cobrindo toda a UI. O resultado central são **3 fixes reais de código** (date picker UTC P1, helper de motion P2, copy interpretativa P3), **12/12 princípios PASS** no red team final (o único WEAK da Missão 2 quitado), **suíte em 101 testes verdes** e um sistema vivo de processo que permite o refinamento contínuo sem depender de missões Manus: fila governada (NOW=1, NEXT≤3), dicionário de interações, design debt numerada, specs de notificações e motion, experience map e spec de interações v2.

Nenhuma feature foi criada, a `main` não foi tocada e todas as decisões estruturais permanecem com o fundador.

## O que foi encontrado e corrigido

| Item | Gravidade | Correção | Commit |
|---|---|---|---|
| G1 — date picker em UTC devolvia o dia errado em fusos negativos após ~21h locais | P1 | `atStartOfDay(ZoneId.systemDefault())` na ancoragem e `atZone(systemDefault()).toLocalDate()` no decode | 0336cbe |
| G9 — helper de a11y lia só TalkBack; Pátina aparecia sob Reduce Motion e para usuários TalkBack | P2 | `isMotionReduced` combina `ANIMATOR_DURATION_SCALE <= 0` + TalkBack | b87d9f2 |
| G2 — copy interpretativa na ReturnScreen ("Suas palavras voltaram. Reescreva com o olhar de hoje") | P3 | Copy factual: "Esta nota voltou. Reescrever se quiser — ou apenas deixe seguir." | b87d9f2 |

Os demais gaps do dogfood (G3, G4, G7, O3) fecharam **verde por design**: o autosaveDraft precede o insert da Entry (janela de perda do debounce fechada por contrato), o retry do notice é o próprio botão Guardar re-habilitado, os targets de toque são ≥48dp e o cancel da notificação está pareado em todos os caminhos terminais da devolução.

## O que os ciclos auditaram

Os ciclos B–D cobriram home, sistema temporal e calendário (fixes acima). E–H cobriram save/erro, Arquivo, note detail/menus e ReturnScreen (G3/G4/G7/O3 verdes por design; D12 registrada como teste contratual pendente). I–L cobriram notificações (especificadas no NOTIFICATION-UX), privacidade/selada (FLAG_SECURE + PrivacyCover + biometria factual), settings (gradiente de risco) e dark/a11y/adaptive (tema segue `isSystemInDarkTheme`, Dynamic Color recusado por identidade Verde-Sálvia). M–N fecharam com o identity check de ~130 literais de copy (terminologia canônica consistente), o micro-detail hunt (5 micro-achados, nenhum código) e o red team final: **12/12 princípios PASS**.

## Artefatos criados (todos pushados na branch)

| Documento | Papel |
|---|---|
| `research/ux-observatory/RESEARCH-LOG.md` | Benchmarks: Things, Day One, Material 3 DatePicker, 1Password/Bitwarden, Android system |
| `research/ux-observatory/SURFACE-INVENTORY.md` | 16 composables + copy completa + estados de UI mapeados |
| `research/ux-observatory/DOGFOOD-SIMULATION.md` | 5 fluxos simulados com gaps G1–G9 |
| `research/ux-observatory/UX-REFINEMENT-QUEUE.md` | Fila governada (G1/G9/G2 pagos, G3 verde por design) |
| `research/ux-observatory/UX-IDEA-INBOX.md` | 9 ideias, nenhuma implementada; anti-inbox de irreversíveis |
| `research/ux-observatory/DESIGN-DEBT.md` | Dívidas D1–D12 (D12 nova: teste draft-sobrevive-a-falha) |
| `research/ux-observatory/INTERACTION-DICTIONARY.md` | Vocabulário canônico |
| `research/ux-observatory/NOTIFICATION-UX.md` | Spec do sistema de notificações |
| `research/ux-observatory/MOTION-SYSTEM.md` | Spec do sistema de motion |
| `research/ux-observatory/EXPERIENCE-MAP.md` | Jornada do usuário em 5 estágios |
| `research/ux-observatory/INTERACTION-SPEC-V2.md` | Spec de interações v2 (consolida ADRs + decisões da missão) |
| `research/ux-observatory/CYCLES-B-H-REPORT.md`, `CYCLES-I-L-REPORT.md`, `CYCLES-M-N-REPORT.md` | Relatórios dos ciclos |

## Decisões pendentes para o fundador

1. **M1** — "nota" vs "entrada" na ReturnScreen (variação lexical única do app; provavelmente intencional pelo tom de carta, mas a decisão é de produto).
2. **D12** — teste contratual do cenário "insert falha e o draft sobrevive" (mitigado por design; 30 min de trabalho para fechar o ponto cego).
3. **2 gates humanos de AVD** — TalkBack walkthrough completo (ordem de leitura da Home, Arquivo, ReturnScreen) e medição WCAG AA dos tokens `DarkColors` (o dark theme foi auditado por intenção, não por ferramenta).
4. **Dynamic Color** — mantido fora por identidade Verde-Sálvia; se o piloto mostrar que usuários esperam Material You, a decisão muda, não o código.

## Próximos passos sugeridos (herdados das missões 1–3)

O repositório está preparado para o piloto do PILOT-PROTOCOL e para a retomada por qualquer agente via `plans/BOOT-PROMPT.md` e `plans/PROJECT-STATE.md` (Missão 2). A disciplina de refinamento contínuo fica institucionalizada no UX-REFINEMENT-QUEUE: cada novo ciclo começa pelo item NOW, paga ou registra, e atualiza a fila — sem depender de missões Manus.
