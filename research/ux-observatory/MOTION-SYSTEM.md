# MOTION-SYSTEM — especificação de motion do ProjetoFio

O sistema de motion do Fio é minimalista por princípio: a UI usa motion apenas para confirmar transições de estado, nunca para entreter. Este documento inventaria o que existe, fixa a regra e registra a decisão da Missão 3 sobre o helper de acessibilidade.

## Inventário atual (toda a motion do app vive em FioApp.kt)

| # | Elemento | Mecanismo | Comportamento |
|---|---|---|---|
| M1 | Guia temporal habilita com texto | `AnimatedVisibility` implícito do bloco (linha 296) | Aparece quando o draft não é vazio; sem animação explícita definida |
| M2 | Botão Guardar press | `scale(0.98) + alpha(0.92)` com `pressed` | Feedback tátil visual; sem spring definido (padrão do sistema) |
| M3 | Notice "Guardado." | `AnimatedVisibility` com `fadeIn()` / `fadeOut()` | Aparece no save, some em 1,5s; liveRegion polite |
| M4 | Pátina botânica | `Canvas` determinístico (drawLine/drawCircle) | **Não é animação**: desenho estático que cresce com a idade do diário (ADR-045). Zero loop, zero evento. |

O app não usa: springs definidos, animateAsState, Transitions custom, Lottie, ou qualquer motion em loop. A ausência é deliberada.

## Regra do sistema

Toda motion nova deve passar no teste dos três silencios: (1) some quando o usuário ativa **Reduce Motion** do sistema; (2) some ou degrada quando **TalkBack** está ativo (o usuário TalkBack navega por ordem, não por aparência); (3) nunca compete com o conteúdo (o texto é sempre o protagonista). Motion que falhe em qualquer um dos três não entra.

## Decisão da Missão 3 (G9)

O helper atual `accessibilityInfoOf` lê apenas `isTouchExplorationEnabled` e o BotanicalMotif o chama com o nome `reduceMotion` — duplamente enganoso: o Motif é suprimido para usuários TalkBack (que podem querer vê-lo) e não é suprimido para usuários que ativaram reduzir animação. O fix do ciclo K substitui o helper por `isMotionReduced(context)` que combina os dois checks com fallback seguro, e renomeia o uso no Motif. Nenhuma outra superfície de motion depende desse helper hoje, então o risco de regressão é nulo.

## Benchmark anti (o que o Fio nunca terá)

Loops de animação de decoração (partículas, breathing), motion de conquista (confetti em save), transições de página coreografadas, parallax de conteúdo, qualquer coisa medida em "delight score". Referências: Day One e Things usam motion de transição apenas; 1Password usa fade de estado. O Journal do Google (cancelado) mostrava motion ligada a métricas — o oposto do Fio.
