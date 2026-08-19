# CYCLES-M-N-REPORT — consistência, identity check, micro-detail hunt, final polish audit, red team final

Relatório dos ciclos M–N da Missão 3. O audit final percorreu **toda a copy de UI do app** (FioApp.kt, MainActivity.kt, returns/) — cerca de 130 literais únicos — e conferiu cada um contra o INTERACTION-DICTIONARY e os 12 princípios.

## Identity check — terminologia canônica

| Termo canônico | Uso no app | Veredito |
|---|---|---|
| "Guardado." / "Guardando…" | Notice do save | Consistente — sem "salvo", "salvando" misturados |
| "Arquivo" | Tela + referências nos diálogos | Consistente; "O Arquivo" com artigo capitalizado nos fluxos de import/export |
| "devolução" / "devolver" | ReturnScreen, settings, notificação | Consistente; "Uma palavra sua voltou" na notificação é factual |
| "Excluídos recentemente" | Archive + diálogos | Consistente, inclusive na frase de retenção "30 dias" |
| "entrada" / "nota" | Diálogos e labels | "Entrada" domina (termo do schema); "Esta nota voltou" na ReturnScreen — uso singular coloquial aceitável, mas registrado como observação M1 abaixo |

A copy dos flows de risco (exclusão permanente, desativar bloqueio, import) mantém a fórmula honesta: afirma o que acontece, nega o que não acontece ("Nada foi apagado.", "O Fio não promete apagamento físico seguro do armazenamento.", "A prévia não altera o Arquivo.").

## Micro-detail hunt — achados P4

**M1 (registrado, sem código):** "Esta nota voltou" (ReturnScreen) usa "nota" enquanto o restante do app usa "entrada". É o único ponto de variação lexical — coloquial na tela íntima da devolução, provavelmente intencional pelo tom de carta, mas é o tipo de detalhe que um red team deve apontar para o fundador decidir.

**M2 (confirmado verde):** a home tem duas copies de estado vazio que **não são redundância** — "O que está passando pela sua cabeça hoje?" é o placeholder do editor; "Escreva quando quiser." aparece quando o editor está vazio sem foco; "Quando quiser, suas palavras podem ficar aqui." é a copy da home vazia (sem nenhuma entrada). Cada uma tem dono claro.

**M3 (verde):** as políticas temporais ("Imediato", "Após 1 minuto/5 minutos", "Em 7/30/90/365 dias", "1 ano", "Algum dia", "Nunca") seguem o vocabulário do ADR-043 e a promise factual "O Fio decide o momento exato. Sua escolha apenas guia o que pode voltar."

**M4 (verde):** os erros de importação formam um léxico diagnóstico consistente (UTF-8 inválido, >20.000 linhas, >5 MiB, >2.000 entradas, controles não suportados, HTML/executável não aceito, data original inválida, >256 KiB por entrada, sem data explícita, estrutura não reconhecida, limite de leitura de segurança, arquivo compactado) — cada erro fecha com "Nada foi importado." quando o estado permaneceu íntegro.

**M5 (verde):** botões de cancelação consistentes ("Cancelar" em todos os dialogs), "Mais opções" no ⋮, "Voltar" com seta ←, "Fechar", "Agora não" — sem variação de affordance.

## Red team final (rodada de fechamento)

A rodada final confrontou o estado atual do código com os 12 princípios, agora incluindo os fixes desta missão. O relatório consolidado (PRODUCT-INTEGRITY-AUDIT da Missão 2: 11 PASS, 1 WEAK corrigido) foi revalidado com os commits 0336cbe, b87d9f2 e desta fase. Nenhum princípio regrediu: o date picker local (G1) **fortalece** a corretude temporal; o helper de motion (G9) **fecha** o WEAK de a11y; a copy factual (G2) **reforça** o princípio da identidade sem interpretação. O sistema de devolução continua sem qualquer affordance de streak/métrica; a exportação continua 100% local; a nota selada continua não implementada (alicerces auditados no ciclo J). **Resultado: 12/12 princípios PASS — o WEAK do repo está quitado desde o commit b87d9f2.**

## Síntese

Os ciclos M–N não produziram código — o estado do app ficou **12/12 princípios PASS, copy canônica verificada, 1 observação M1 registrada** para decisão do fundador, e o polish audit confirma que os micro-detalhes (notice, cancel, labels, erros de import) estão consistentes. A missão termina com 3 recomendações práticas ao fundador documentadas no relatório final: decidir M1 (nota vs entrada na ReturnScreen), agendar os 2 gates AVD, e considerar o teste D12.
