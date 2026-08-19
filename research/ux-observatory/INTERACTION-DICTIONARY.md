# INTERACTION-DICTIONARY

Vocabulário canônico de interações do ProjetoFio. Cada gesto/elemento tem um nome, um comportamento definido e um princípio que o sustenta. Quando o Codex (ou o Manus) precisar estender a UI, este dicionário é a fonte da verdade — qualquer nova interação deve ser nomeada e registrada aqui antes de entrar no código, ou o nome é escolhido no PR com aprovação do fundador.

## Atores e superfícies

| Termo | Definição |
|---|---|
| Casa (Home) | A única tela com o editor. Nunca exibe métricas, listas de produção ou cronologia. |
| Arquivo | Cronologia tipográfica agrupada por mês; a escrita antiga vive aqui, nunca na Casa. |
| Nota | Uma entrada do diário; tem data original, zona original, conteúdo, modo de devolução. |
| Cápsula | Metáfora do ADR-044: a primeira nota; momento ritual marcado com copy estendida. |
| Devolução | O momento em que uma nota antiga reaparece (engine M2/M4). |
| Guia temporal | A escolha do usuário sobre quando uma nota pode voltar (Time sheet). |
| Selo / Nota Selada | Estado protegido futuro (feature; não implementada). |
| Pátina | Motivo botânico determinístico que amadurece com a idade do diário (ADR-045). |

## Gestos e alvos

| Interação | Comportamento | Princípio |
|---|---|---|
| Guardar | Botão 52dp; desabilitado com texto vazio; label "Guardando…" com feedback de press; notice "Guardado." 1,5s | ADR-014: confirmação contida |
| Guia temporal | 7 opções em bottom sheet; 1 toque = escolha confirmada; sublinha persistente "Escolhido: …" | Decisão rápida, sem segunda confirmação (coisas triviais não pedem cerimônia) |
| Ler nota | SelectionContainer; data longa pt-BR; botões Devolver/Reescrever | Leitura é o estado protegido; cópia permitida, edição cerimoniosa |
| Devolver | Dialog "Devolver agora?" → confirmar → engine | Destrutivo-real exige 1 passo + undo estrutural (Excluídos recentes 30 dias) |
| Excluir | Dialog "Mover para Excluídos recentemente?" com a promessa dos 30 dias | Nunca "apagar" sem rede; exclusão permanente é 2 passos com aviso honesto |
| Excluir para sempre | Action em erro (terracota/error) com confirmação "não pode ser desfeita" | Único destino final; dito sem eufemismo |
| Editar nota | Dialog nativo AlertDialog (não drawer, não tela) | Edição é exceção, não fluxo principal |
| Menu ⋯ | DropdownMenu discreto com 2 itens (Arquivo, Configurações) | ADR-004: navegação nunca compete com a escrita |
| PrivacyCover | Tela "Fio" em displayLarge nas recentes | O conteúdo nunca aparece fora da proteção |
| Bloqueio | Biometria quando disponível; fallback factual | Camada opcional sobre cifragem permanente (Keystore) |
| Pátina | Canvas 32×56dp, alpha 0.55; ausente com TalkBack OU motion reduzido | Decorativa; nunca streak, nunca métrica |

## Copy padrão (registros do dicionário)

A copy do Fio é factual, calma e sem julgamento. Proibidas por princípio: interpretativas ("você evoluiu", "insight", "padrão"), gamificadas (streak, meta, conquista), produtivistas (eficiência, hábito, produtividade) e interpretativas de estado emocional ("seu dia foi pesado?"). A copy de erro é sempre factual + garantia ("nada foi apagado"). Registro canônico completo: SURFACE-INVENTORY.md §3.

## Notificações

A notificação de devolução é o único ponto onde o Fio fala com o mundo externo. Regras: nunca carrega texto da nota, nunca abre fora da ReturnScreen, nunca repete a mesma nota sem nova elegibilidade, respeita quiet hours, visibility privada. Especificação completa: NOTIFICATION-UX.md.

## Motion

A motion do Fio é de transição, nunca de espetáculo: fade de notice, scale de press, sem loop. Reduce Motion (sistema) e TalkBack suprimem a Pátina. Especificação completa: MOTION-SYSTEM.md.
