# DESIGN-DEBT

Dívidas de design conhecidas do ProjetoFio. Diferentemente do UX-REFINEMENT-QUEUE (que lista o trabalho executável agora), este documento lista o que está **conscientemente não ideal** — escolhas registradas para que ninguém as trate como bugs futuros nem as "corrija" sem contexto. A regra das missões Manus: dívida registrada é dívida aceita; só volta à mesa por decisão do fundador ou quando o custo de carregar superar o custo de pagar.

## Dívidas aceitas

| # | Dívida | Onde | Por que foi aceita |
|---|---|---|---|
| D1 | Copy hardcoded no Kotlin (strings.xml com 1 recurso) | FioApp.kt, ~163 literais | O app é pt-BR único por design; centralizar sem i18n no horizonte seria burocracia sem ganho. Se o fundador algum dia quiser outros idiomas, a migração é mecânica e documentada aqui. |
| D2 | Formatters de data criados a cada composição | FioApp.kt (`displayDate`, `groupKey`, `temporalDistance`, `noteDate`, `displayImportDate`) | Robolectric e uso real: custo insignificante em telas pequenas; manter legibilidade local. Se houver regressão de performance futura, elevar para `object Formatter` compartilhado. |
| D3 | Date picker Material 3 padrão, sem customization | `DatePickerDialog` nativo | O picker nativo é o contrato do sistema (consistente com Android); customizar o calendário quebraria a expectativa do usuário Android. A dívida real (G1, UTC) é de lógica, não de visual — paga no ciclo C. |
| D4 | Menu ⋯ com DropdownMenu nativo (sem bottom sheet) | HomeScreen | ADR-004: Arquivo/Configurações nunca devem competir com a escrita; o dropdown é o mecanismo mais discreto. Aceito por design. |
| D5 | Notice "Guardado" com fade de 1,5s sem retry manual | HomeScreen | ADR-014: confirmação contida. A janela curta é deliberada; o autosave protege a perda de verdade (o texto persiste). |
| D6 | BotanicalMotif desenhado com Canvas raw (sem biblioteca de animação) | BotanicalMotif | O motivo é estático-determinístico por princípio (ADR-045): nunca anima em loop, nunca reage a eventos. Canvas puro é o caminho certo. |
| D7 | SelectionContainer sem ações de compartilhar/copiar estruturadas | NoteScreen, ArchiveRow | O sistema já oferece copiar via seleção; adicionar menu de compartilhar abriria superfície para "produtividade acidental". Aceito. |
| D8 | Single-locale pt-BR em todos os formatters | FioApp.kt, Theme | Decisão de produto (essência local); reabrir só se o fundador quiser o Fio em outros mercados. |
| D9 | Testes de UI instrumentados inexistentes no CI | M2AndroidContractTest e similares | Sem emulador no pipeline atual; as missões Manus cobrem unidade+Robolectric. TalkBack/visual dependem de AVD humano — gate documentado no PILOT-PROTOCOL. |

## Dívidas que exigem decisão do fundador

| # | Dívida | Decisão pendente |
|---|---|---|
| D10 | Política de retorno editável após o save (UI ainda não existe para isso) | Schema 4 / UI de edição — DECISION REQUIRED (Missão 1) |
| D11 | Analytics remoto do piloto (matriz define 10 eventos locais; transporte remoto aberto) | DECISION REQUIRED (PILOT-PROTOCOL) |
| D12 | Sem teste contratual dedicado ao cenário "insert falha e o draft sobrevive" (o autosave antes do insert em saveEntry fecha a janela, mas a garantia é implícita) | Teste unitário de regressão | Missão 3, ciclos E–H: mitigado por design (autosaveDraft precede a transação); o teste valeria 30 min e fecharia o último ponto cego do ciclo E |

## Regra de atualização

Toda dívida nova entra aqui com número, localização, justificativa e um dos três destinos: aceita, decisão pendente, ou paga (com link para o commit que a pagou). Dívidas pagas não saem do histórico — apenas ganham o carimbo "PAGA em <commit>".
