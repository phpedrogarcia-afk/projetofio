# FIO-P19 — integridade das escolhas de devolução ao guardar

**Status:** DECISION REQUIRED — nenhum código autorizado
**Tipo:** decisão de produto + schema/engine/UI se aprovada
**Branch prevista:** `feature/fio-temporal-integrity-20260820`
**Severidade atual:** P0 de confiança — a interface oferece escolhas que não são aplicadas

## 1. Problema confirmado

Na Home, `ReturnPolicy.InPeriod`, `OnDate` e `Never` existem somente no estado
Compose. Ao tocar em Guardar, `FioViewModel.saveEntry()` passa apenas o texto para
`FioService.saveEntry(content)`, e a nova `Entry` recebe o default
`returnMode = ELIGIBLE`. O schema 3 não possui a janela solicitada.

Consequência real:

- “Algum dia” funciona apenas porque coincide com o default;
- “Em 7/30/90 dias”, “Em 1 ano” e a data escolhida não chegam ao motor;
- “Nunca” ao guardar também não é aplicado;
- “Nunca mais” depois de abrir uma devolução é outro fluxo e funciona.
- no detalhe da nota, “Devolver para agora” apenas alterna os estados Compose
  locais `returning/returned`; não cria tentativa, histórico ou mudança no motor.

Manter a interface como está não é uma opção aceitável: o usuário acredita que
fez uma escolha que o produto descarta silenciosamente.

## 2. Opções honestas

### A1 — implementar janelas calmas de 7 dias (recomendada)

- promover o banco de schema 3 para 4 com os campos canônicos
  `requestedWindowStart` e `requestedWindowEnd` na Entry;
- `Nunca` grava `returnMode = NEVER`; “Algum dia” mantém `ELIGIBLE` sem janela;
- períodos e data escolhida criam uma janela de oportunidade de sete dias,
  começando na data-alvo local;
- renomear “Em 7 dias” para “A partir de 7 dias” e explicar: “Durante os sete
  dias seguintes, esta nota pode voltar. Não é um lembrete com hora marcada.”;
- o motor considera somente a janela válida e continua respeitando consentimento,
  pausa, horário silencioso, limite de frequência e uma devolução por vez;
- se a janela terminar sem oportunidade segura, a nota volta ao estado orgânico
  “Algum dia”; nada é entregue à força e nenhuma promessa exata é feita;
- migration 3→4 é aditiva: entradas existentes ficam `ELIGIBLE`, sem janela;
- a escolha atual deve chegar inteira de UI → ViewModel → serviço → repositório;
  nenhum estado temporal pode ser descartado silenciosamente.
- “Devolver para agora” deve ser removido até existir contrato real ou conectado
  a uma operação explícita que registre a ação. A1 recomenda removê-lo neste
  packet e deixar uma eventual devolução manual para decisão futura.

**Por que é a recomendação:** preserva a ideia visual já aprovada, combina com o
caráter não exato do Android e explica uma promessa que o motor consegue cumprir
sem transformar o Fio em agenda.

### A2 — uma única data estrita

Persistir uma janela de um dia. É mais próxima da expressão “em tal data”, mas
pode ser perdida por limite de frequência, pausa, bateria ou horário silencioso.
Para parecer garantida exigiria comportamento de lembrete, contrário ao Fio.

### B — simplificar agora, sem schema 4

Remover períodos e calendário. Manter apenas “Algum dia” e “Nunca”, conectando
`Nunca` ao `returnMode` já existente no schema 3. É honesta e de menor risco, mas
retira a experiência temporal valorizada pelo fundador.

### C — ocultar todo o seletor temporal

Todas as notas ficam “Algum dia” até uma versão futura. É a menor mudança e não
mente, porém elimina controle explícito do usuário.

## 3. Contrato obrigatório se A1 for aprovada

- preservar os invariantes de privacidade, conteúdo original, consentimento,
  `NEVER`, exclusão e uma devolução pendente por vez;
- zero rede, conta, analytics, alarme exato ou lembrete de produtividade;
- migration 3→4 com teste de dados existentes e downgrade seguro já previsto;
- testes unitários do mapeamento das quatro famílias de escolha;
- testes do motor antes, dentro e depois da janela, inclusive cap, pausa, quiet
  hours, fuso/DST, exclusão e `NEVER`;
- teste instrumentado que salva cada política, reinicia o app e verifica a
  persistência real;
- teste que garante a ausência de “Devolver para agora” enquanto não existir
  operação de domínio correspondente;
- texto acessível e inequívoco na Home;
- atualizar DATA-MAP, TIME-MAP, schema manifest, ADR e export se a política fizer
  parte do formato público;
- instalar no Poco preservando os dados e executar o gate físico disponível.

## 4. Fora do escopo

- escolher hora exata;
- garantir que uma devolução ocorrerá dentro da janela;
- múltiplas devoluções simultâneas;
- sync, servidor, calendário externo ou notificação recorrente;
- alterar o conteúdo escrito ou interpretar significado.

## 5. Aprovação necessária

Frase sugerida para seguir com a recomendação:

`Aprovo FIO-P19, opção A1 — janela de 7 dias e schema 4.`

Até essa aprovação, a implementação temporal fica congelada. O FIO-P18 pode ser
validado separadamente porque não altera schema nem motor.

## 6. Evidence log

| Data | Evidência | Resultado |
|---|---|---|
| 2026-08-20 | `FioApp.kt` | política é estado local e `viewModel.saveEntry()` não recebe argumento |
| 2026-08-20 | `FioViewModel.kt` + `FioService.kt` | apenas conteúdo é passado; Entry usa default `ELIGIBLE` |
| 2026-08-20 | `Entities.kt` + schema 3 | não existem colunas de janela solicitada |
| 2026-08-20 | `TimeReturnEngine.kt` | elegibilidade usa somente `returnMode`, sem data pedida |
| 2026-08-20 | `NoteScreen` em `FioApp.kt` | “Devolver para agora” usa somente `remember` local, sem serviço/histórico |
