# CYCLES-B-H-REPORT — Home, sistema temporal, calendário, save/erro, arquivo, note detail, ReturnScreen

Relatório dos ciclos B–H da Missão 3. Cada ciclo terminou em um de três destinos: fix aplicado, verde por design, ou decisão registrada. Nenhum ciclo propôs feature nova; a disciplina "refinamento sem features" das missões anteriores foi mantida.

## Ciclo B–C–D — Home e sistema temporal (código aplicado)

| Item | Destino | Commit |
|---|---|---|
| G1 — date picker em UTC deslocava o dia em fusos negativos | **FIX** (2 locais: ancoragem e decodificação agora usam `ZoneId.systemDefault()`; teste reprodutor `DatePickerZoneTest` com 2 cenários) | 0336cbe |
| G9 — helper de motion só lia TalkBack | **FIX** (`isMotionReduced` combina `ANIMATOR_DURATION_SCALE <= 0` + TalkBack; uso renomeado no `BotanicalMotif`; stub de teste + gate AVD no PILOT-PROTOCOL) | b87d9f2 |
| G2 — copy interpretativa na ReturnScreen | **FIX** ("Esta nota voltou. Reescrever se quiser — ou apenas deixe seguir.") | b87d9f2 |
| Suíte pós-fixes | 101 testes verdes, 0 falhas | — |

## Ciclo E — Save/Erro

O G3 (race draft ↔ save) foi **mitigado por design**: `saveEntry()` executa `autosaveDraft(content)` antes da transação da Entry, então o estado exato do editor vira Draft criptografado antes de qualquer tentativa de insert; se o insert falhar, o debounce pendente continua autoprotegendo e o restore na abertura recupera o texto. O G4 (notice sem retry) foi reavaliado contra o ADR-014: a affordance de retry é o próprio botão Guardar, que permanece habilitado após falha com o texto no editor e a mensagem de erro já promete exatamente isso ("O texto continua no editor; tente novamente antes de fechar o Fio."). Adicionar um botão de retry no notice duplicaria affordance sem ganho. Registro como dívida D12 (teste contratual do cenário adiado para decisão do fundador).

## Ciclo F — Arquivo

Auditoria estática do `ArchiveScreen`: os alvos de toque do overflow ⋮ e dos botões Editar/Excluir usam `heightIn(min = 48.dp)` / `widthIn(min = 48.dp)` — **verde por design** (G7 confirmado). O agrupamento por mês com `displayMonth` pt-BR preserva a textura temporal (benchmark anti do Things). Sem gap.

## Ciclo G — Note detail / menus

A tela de leitura usa `SelectionContainer` com `liveRegion = Polite`; o menu ⋮ principal tem `contentDescription = "Mais opções"` com alvo 48dp; o menu de exclusão é `AlertDialog` com copy honesta; o dropdown do editor tem 2 itens (ADR-004). **Sem gap identificado.**

## Ciclo H — ReturnScreen

Copy neutralizada (G2); `liveRegion = Polite`; botões com alvo 48dp; "Devolver para agora" abre dialog com copy factual; "Nunca mais" é 2 passos com aviso de irreversibilidade. A observação O3 do NOTIFICATION-UX foi **verificada e fechada**: `neverReturn()` (TimeReturnsService:171) executa `notifications.cancel(id)` e `scheduler.cancel()` antes do `reconcile()`, e `cancelPendingReturnsForEntry` com `ENTRY_NEVER` impede re-eligibilidade. **Verde por design.**

## Síntese

Dos 4 ciclos com código, 3 achados reais foram corrigidos (G1, G9, G2) e 1 dívida registrada (D12). Dos 4 ciclos de auditoria pura, todos fecharam verdes por design (G3, G4, G7, O3). O sistema temporal da Home, o arquivo e a ReturnScreen estão firmes quanto aos princípios: factualidade, privacidade, devolução gentil e silêncio.
