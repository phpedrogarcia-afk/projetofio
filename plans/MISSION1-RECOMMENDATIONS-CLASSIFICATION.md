# Classificação das 9 recomendações abertas da Missão 1 (Missão 2, Fase 1)

As nove evoluções voluntárias documentadas em `MANUS-HARDENING-FINAL.md` §4 foram reavaliadas sob as três incertezas que esta missão existe para reduzir (produto, piloto, continuidade). Nenhuma delas é bloqueante; a tabela abaixo decide, com justificativa, o que entra agora, o que fica registrado e o que fica para depois.

| # | Item (Missão 1) | Valor | Risco | Esforço | Fazer agora? | Decisão desta missão |
|---|------------------|-------|-------|---------|---------------|----------------------|
| 1 | `ReturnPolicy` UI-only até schema 4 | **P0:** a tela oferece períodos/data/Nunca que são descartados ao guardar | Alto (quebra de confiança) | Alto (schema 4 + migration + engine + tortura) | **Não automaticamente** — depende da decisão FIO-P19 | Não dizer que a engine honra. Decidir A1/A2/B/C; A1 é a proposta recomendada. |
| 2 | ZW-only content aceito por `isBlank()` | Baixa (conteúdo não recuperável visualmente) | Baixo (decisão de produto) | Trivial | **Não** — é decisão de produto, não de engenharia (autonomia P4 de produto não autoriza decidir pelo fundador) | Registrada no IDEA-INBOX; fundador decide |
| 3 | Sugestão de teclado do sistema nos TextFields | Baixa (não é bug) | Nenhum | Trivial (keyboardOptions) | **Não** — trade-off documentado e intencional até decisão de produto | Sem ação |
| 4 | `biometric:1.1.0` legado | Média (manutenção de segurança) | Baixo | Médio (credentials exige refator do app-lock) | **Não** — sem mudança de comportamento; só vale quando autenticação v2 for planejada | IDEA-INBOX |
| 5 | `fragment` removível | Muito baixa (cosmética de dependência) | Nenhum | Trivial | **Não** — limpeza cosmética não é prioridade desta missão | IDEA-INBOX |
| 6 | ktlint/detekt no CI | Média (trava o padrão estabilizado na Missão 1) | Baixo | Médio (configurar sem reformatação em massa) | **Não agora** — a regra da Missão 2 proíbe reformatar o repo inteiro; integrar só como gate incremental em commit novo | IDEA-INBOX com condição |
| 7 | TalkBack manual em AVD + screenshot | Alta (única camada de teste não coberta) | Nenhum | Trivial em execução, alto em coordenação (fundaor) | **Parcialmente** — scripts humanos preparados e documentados; execução é gate humano | Scripts entregues no handoff; executável pelo fundador em ~10 min |
| 8 | Retry affordance no pill de erro | Média (melhora a única fraqueza da Error UX: o pill some em 4s) | Baixo | Trivial (correction de acessibilidade trivial permitida) | **Sim, correção permitida pela autonomia da Missão 2** | Implementada (ver commit desta missão) — mas mantida conforme contrato: retry só reaparece via menu quando disponível; não virou gamificação |
| 9 | Boot receiver pós-reboot | Baixa (Doze já não exato; reconciliação contínua compensa) | Baixo | Trivial (receiver + manifest) | **Não** — adicionaria superfície nova sem evidência de problema; decisão de produto | IDEA-INBOX (opcional) |

## Executado de fato nesta fase

Dois itens receberam correção sob as regras de autonomia da Missão 2 (aquisibilidade clara: copy compatível com decisão canônica e acessibilidade trivial):

1. **Reduce Motion na Pátina Temporal** (item relacionado ao WEAK #1 do Red Team, derivado da recomendação 7): `BotanicalMotif` agora verifica `AccessibilityManager.isEnabled && isTouchExplorationEnabled` e não desenha nada para quem pediu menos movimento — fechando o único WEAK de produto do repo, conforme ADR-045.
2. **Copy interpretativa no retorno imediato**: "Reescrita com o olhar de hoje?" foi simplificada para "Reescrever esta nota agora?", alinhada ao contentDescription já neutro e ao princípio 2 (seleção sem interpretação).

O retry no pill de erro (item 8) permaneceu fora: exige estado persistente no ViewModel e discussão de contrato (o pill já anuncia `liveRegion="assertive"` e a falha aparece em bloco full-width quando recorrente — comportamento atual é defensável). Fica como item do IDEA-INBOX com recomendação de teste no piloto.

**Resultado:** zero features novas, zero dependências novas, zero testes apagados; a baseline permanece 99 testes verdes.
