# UX-REFINEMENT-QUEUE

Fila viva do refinamento contínuo de UX do ProjetoFio. Regra de governo (herdada das missões Manus 1 e 2): **NOW = 1** item executável, **NEXT = no máximo 3** candidatos, o restante vive no UX-IDEA-INBOX ou no DESIGN-DEBT. Itens marcados como "sem código" nunca entram em NOW enquanto houver trabalho de código pendente — a disciplina das missões anteriores é preservada.

## Governança

O item NOW é sempre o de maior risco corrigido ou o de maior valor de confiança no sistema. Correções triviais de identidade (copy, acessibilidade factual) podem ser executadas junto de um NOW de código quando não conflitam, conforme a autorização permanente dada pelo fundador na Missão 1 ("bugs claros, lint, edge cases reproduzíveis, falta de teste, pequenas correções de acessibilidade — seguir em frente"). Qualquer item que mude contrato de criptografia, migração de dados ou comportamento destrutivo permanece DECISION REQUIRED para o fundador.

## Estado atual (ciclos B–H concluídos)

A fila de trabalho está limpa: G1, G9 e G2 foram pagos (commits 0336cbe e b87d9f2) e G3 fechou verde por design (autosaveDraft precede o insert; dívida D12 registrada). O NOW atual é o **ciclo I–L** (notificações, privacidade/selada, settings, dark/a11y/adaptive), que é auditoria pura — sem código previsto, conforme o veredito dos ciclos E–H.

| Status | Item | Origem | Gravidade | Nota |
|---|---|---|---|---|
| PAGO | G1 — date picker em UTC puro (0336cbe) (`atStartOfDay(ZoneOffset.UTC)` / `atZone(ZoneOffset.UTC).toLocalDate()`) devolve o dia errado para usuários em fusos negativos após 21h locais | DOGFOOD-SIMULATION G1 | P1 | Corretude temporal; fix com fuso local (detalhes no ciclo C) |
| PAGO | G9 — helper de motion (b87d9f2) `isTouchExplorationEnabled`; `BotanicalMotif` salta para usuários TalkBack mesmo sem reduzir animação, e não lê o setting de movimento reduzido | DOGFOOD-SIMULATION G9 | P2 | Combinar checks; ler `ACCESSIBILITY_ANIMATIONS` |
| PAGO | G2 — copy interpretativa (b87d9f2) "Suas palavras voltaram. Reescreva com o olhar de hoje..." (linha 754) e botão "Reescrever esta nota agora?" (linha 733) | DOGFOOD-SIMULATION G2 | P3 | Neutralizar para factual (identidade sem interpretação) |
| VERDE POR DESIGN | G3 — race draft (autosaveDraft precede o insert — D12) do draft e o `debounce(700)` pendente: digitação durante a janela de save pode ser perdida | DOGFOOD-SIMULATION G3 | P2 | Mitigação cancel-safe no FioViewModel |

## Backlog (não entra em NEXT até espaço)

| Item | Origem | Gravidade | Nota |
|---|---|---|---|
| G4 | Notice "Guardado" sem caminho de retry manual | G4 | P3 | Deixar como está (ADR-014) até decisão do fundador |
| G5 | Política de retorno não editável da Home após o save | G5 | P2 | Requer UI nova — NÃO criar nesta missão (fora do escopo de refinamento) |
| G6 | Copy dispersa no código (strings.xml com 1 recurso) | G6 | P3 | Higiene — adiar para quando houver i18n real no horizonte |
| G7 | Targets 48dp do Edit/Excluir no ArchiveRow a confirmar | G7 | P2 | Verificação de UI em AVD (gate humano) |
| G8 | Reabrir devolução pendente sem badge visível | G8 | P3 | Comportamento do engine M2 — verificar antes de propor |

## Histórico

- Criado na Missão 3 com G1–G9 do dogfood simulation. G1 entrou NOW por ser P1 de corretude.
- Ciclos B–D: G1 pago (0336cbe), G9 pago + G2 pago (b87d9f2).
- Ciclos E–H: G3/G4/G7/O3 fechados verde por design; D12 registrada.
