# NOTIFICATION-UX — especificação do sistema de notificações do Fio

Implementação atual: `AndroidReturnNotifications` (returns/AndroidTimeReturns.kt), gateway do contrato `ReturnNotificationGateway`, disparada pelo WorkManager não-exact do `AndroidTimeReturns`. Este documento especifica o que a notificação é, o que ela nunca será, e registra as observações da Missão 3.

## Contrato observado

| Aspecto | Implementação | Avaliação |
|---|---|---|
| Canal | `fio_returns_v1`, nome pt-BR, `IMPORTANCE_LOW`, descrição neutra | Correto — baixa intrusão, sem som padrão |
| Conteúdo | Título fixo "Algo seu voltou." — nunca carrega texto da nota | Alinhado ao princípio: a notificação nunca revela conteúdo |
| Visibilidade | `VISIBILITY_PRIVATE` (lockscreen oculta título e ícone) | Correto — privacidade na tela de bloqueio |
| Categoria | `CATEGORY_REMINDER`, `setShowWhen(false)`, `autoCancel=true` | Correto — lembrete sem timestamp dramático |
| Toque | `PendingIntent` imutável → MainActivity com `ACTION_OPEN_RETURN` + returnId | Direto: o toque abre a ReturnScreen, único lugar onde o conteúdo aparece |
| Permission | `canPostNotifications()` verifica permissão + sistema habilitado; silente se não | Correto — não pede permissão agressivamente |
| ID | `returnId.hashCode() and Int.MAX_VALUE` | Aceito — collisions improváveis; uma devolução = uma notificação |

## O que a notificação nunca será (princípio)

A notificação é a única voz externa do Fio e fala em frases mínimas. Ela nunca: carrega o texto da nota ou qualquer parte dele; revela título, data ou contagem no lockscreen; usa urgência (`IMPORTANCE_HIGH`), som contínuo ou vibração agressiva; repete a mesma devolução; usa copy gamificada ou produtivista; depende de servidor (é 100% local — `AndroidTimeReturns` roda no dispositivo).

## Observações da Missão 3

O1. O título "Algo seu voltou." é a copy fixa para todas as devoluções — consistente com o tom, mas não diferencia devolução de nota selada/reaberta (quando essas features existirem, será preciso um segundo canal ou uma segunda copy; adiar).

O2. `setShowWhen(false)` remove o timestamp — coerente com a atemporalidade do Fio. Manter.

O3. O cancelamento de notificação (`cancel(returnId)`) existe no gateway; o fluxo "Não mostrar novamente" da ReturnScreen precisa garantir o cancel pairado com a marcação de never — verificar no ViewModel que ambos acontecem (ciclo H).

O4. O channel `fio_returns_v1` já é versionado (`_v1`) — se o sistema mudar o canal no futuro, o número evolui sem migrar o usuário manualmente. Boa prática já presente.

## Decisões

Sem mudanças propostas nesta missão. O sistema de notificações atual é fiel aos princípios; as observações O1–O4 ficam como material para o UX-REFINEMENT-QUEUE quando as features correspondentes (selada, never-return) evoluírem.
