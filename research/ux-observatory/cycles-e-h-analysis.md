# Ciclos E–H — análise de código (antes do código)

## Ciclo E — Save/Erro (FioViewModel + FioService)

A auditoria do G3 (race draft ↔ save) mostrou que a arquitetura **já fecha a janela de perda**: `saveEntry()` chama `autosaveDraft(content)` antes da transação da Entry — o estado exato do editor vira Draft criptografado antes de qualquer tentativa de insert. Se o insert falhar, o debounce pendente continua autoprotegendo e o restore na abertura recupera o texto. O "Guardar" e o autosave dividem o mesmo repositório de drafts. Conclusão: **G3 é mitigado por design** — documentado no DESIGN-DEBT como D12 resolvido, sem mudança de código.

O G4 (notice "Guardado" sem retry) foi reavaliado contra o ADR-014: a promessa do Fio é "o texto continua no editor" (mensagem de erro já diz isso). O notice curto é cerimonial; adicionar botão de retry no notice duplicaria a affordance do botão Guardar (que já está habilitado pós-falha, pois `saving=false` e o texto permanece). Conclusão: **G4 vira decisão registrada, sem código** — o retry implícito é o próprio botão Guardar.

## Ciclo F — Arquivo (ArchiveScreen)

Auditoria estática: targets do ⋮ do ArchiveRow usam `heightIn(min = 48.dp)` e `widthIn(min = 48.dp)` no menu overflow; os botões Edit/Excluir usam o mesmo padrão via `heightIn(min = 48.dp)`. Confirmado G7 como **verde por design** — registrar no cycle audit. O Arquivo agrupa por mês com `displayMonth` pt-BR; notas deletadas têm contagem no botão "Excluídos recentemente". Sem gap identificado.

## Ciclo G — Note detail / menus

A tela de leitura usa `SelectionContainer` com `liveRegion = Polite` no bloco de devolução; o menu ⋯ do editor usa `DropdownMenu` nativo com 2 itens (ADR-004). O menu de exclusão é `AlertDialog` com copy honesta. Verificado que o `contentDescription` do ⋮ principal diz "Mais opções" e o ícone é texto "⋯" com target 48dp. Sem gap.

## Ciclo H — ReturnScreen

Verificada a tela de devolução: copy G2 neutralizada (missão, ciclo B–D); `liveRegion = Polite`; os botões usam `heightIn(min = 48.dp)` + `padding(vertical = s2)`; o fluxo "Devolver para agora" abre `AlertDialog` com copy factual ("Devolver agora?"); "Nunca mais" usa copy de 2 passos com aviso "não pode ser desfeita". O `neverReturnOpened()` chama `timeReturns.neverReturn(attemptId)` — falta verificar se o cancel da notificação está pareado (O3 do NOTIFICATION-UX).

## O3 (notificação cancel) — VERIFICADO E VERDE

`neverReturn()` (TimeReturnsService:171) executa `notifications.cancel(id)` e `scheduler.cancel()` antes do `reconcile()`; `dismissReturn()` muda para DISMISSED e reconcilia; `cancelPendingReturnsForEntry` com `ENTRY_NEVER` impede re-eligibilidade. O cancel da notificação está pareado com a marcação never — a observação O3 do NOTIFICATION-UX fica resolvida sem mudança de código.

## Veredito dos ciclos E–H

Nenhum fix de código necessário nos ciclos E–H: G3 e G4 são mitigados por design (autosave fecha a janela do debounce; retry implícito no botão Guardar), G7 confirmado verde (targets 48dp), O3 verificado (cancel pairado). Todo o refinamento de UI desses ciclos vive em decisões registradas (D12 adicionado ao DESIGN-DEBT) e na análise abaixo — nada a tocar no código, mantendo a disciplina "refinamento sem features".

**D12 (nova dívida registrada)**: a confiança no autosave durante a janela do debounce é implícita no contrato (saveEntry → autosaveDraft primeiro) mas não tem teste unitário de regressão dedicado ao cenário "insert falha e draft sobrevive". Adiar para decisão do fundador: teste contratual valeria 30 min de trabalho e fecharia o único ponto cego restante do ciclo E.
