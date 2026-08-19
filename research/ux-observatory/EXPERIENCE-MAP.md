# EXPERIENCE-MAP — a jornada do usuário do Fio

Documento da Missão 3 que desenha a experiência completa do Fio do ponto de vista do usuário, mapeada contra o código atual (branch `integration/manus-ux-refinement-20260819`). Serve de contrato de experiência: qualquer future work deve poder apontar em qual estágio interfere.

## Os cinco estágios

| Estágio | Momento do usuário | O que o Fio faz (código atual) | Princípio que sustenta |
|---|---|---|---|
| 1. Escrever | Abre o app, editor vazio com "O que está passando pela sua cabeça hoje?" | Editor com `BasicTextField`, autosave a cada 700 ms (`autosaveDraft`, criptografado), placeholder "Escreva quando quiser." quando vazio | Silêncio — nada pressiona |
| 2. Guardar | Aperta Guardar | `saveEntry` grava o Draft antes do insert da Entry (janela de perda fechada), notice "Guardado." por 1,5 s, reconcilia devoluções | Confiança — "O texto continua no editor" se falhar |
| 3. Esquecer | Fecha o app | `FLAG_SECURE` + `PrivacyCover` (só "Fio") no recents; bloqueio opcional via biometria do aparelho | Privacidade — conteúdo nunca exposto |
| 4. Receber | Dias, meses ou anos depois | Notificação discreta "Uma palavra sua voltou." sem texto da notificação; ao abrir, a ReturnScreen mostra a carta na íntegra com "Quando isso pode voltar? · $policyLabel" | Devolução gentil — sem streak, sem culpa |
| 5. Responder | Na ReturnScreen | "Devolver para agora" (dialog "Devolver agora?" → "Esta nota voltou. Reescrever se quiser — ou apenas deixe seguir.") ou "Nunca mais" (2 passos, cancel da notificação pareado) | Agência — o usuário decide o fim da história |

## Momentos de fragilidade mapeados (e como o código os cobre hoje)

O editor pode ser perdido no meio do caminho apenas se o app morrer entre dois autossaves — a janela é de 700 ms e o conteúdo fica criptografado no dispositivo (G3 mitigado; D12 registrada como teste pendente). A devolução pode ser percebida como intrusiva se a notificação chegar em horário ruim — quiet hours e `IMPORTANCE_LOW` cobrem isso por padrão. A devolução pode ser rejeitada por permissão desativada — o app declara "Notificações estão desativadas. O Fio continuará funcionando normalmente e não pedirá novamente aqui." e segue. O Arquivo pode ser perdido se o aparelho morrer — a exportação local continua sendo o único caminho de backup que o app conhece, e a missão 1 provou o round-trip criptografado.

## O que esta missão descobriu que a experiência não mostra

Três achados de observatório que afetam a jornada sem alterar sua arquitetura: o date picker usava UTC puro e devolvia o dia errado em fusos negativos após as 21h locais (G1 — pago, commit 0336cbe); a Pátina decorativa ignorava a preferência de movimento reduzido e aparecia para usuários de TalkBack (G9 — pago, commit b87d9f2); a copy da ReturnScreen interpretava o significado da volta ("Suas palavras voltaram. Reescreva com o olhar de hoje") em vez de descrever o fato (G2 — pago, commit b87d9f2). Os três foram corrigidos; o único item restante para decisão humana é M1 (variação lexical "nota" vs "entrada" na ReturnScreen).

## Portas de entrada e saída da experiência

O usuário entra por três portas: a primeira nota (first-capsule, onboarding mínimo do ADR-044), o restore de um backup importado (import M3 com validação em 13 erros léxicos), ou o app já com histórico. A saída única é a exportação local — o app não conhece nenhum caminho de saída remoto, por design. Entre entrada e saída, a experiência é cíclica: escrever → esquecer → receber → responder (ou nunca) → escrever de novo. O ciclo é o produto.
