# Auditoria de base — o que um aplicativo de notas precisa no Fio

Status: live product audit
Updated: 2026-08-20

Esta matriz evita dois erros opostos: esquecer controles básicos e copiar por
reflexo recursos de produtividade que contradizem o Fio. "Comum em outro app"
é evidência para investigar, não autorização automática para implementar.

## Base necessária e presente

| Necessidade do usuário | Estado atual | Evidência/experiência esperada |
|---|---|---|
| Escrever uma nota livre | Implementado | editor principal sem título/tag obrigatórios |
| Não perder um texto antes de guardar | Implementado | um rascunho criptografado com autosave |
| Guardar offline | Implementado | transação Entry + limpeza do rascunho |
| Ler o que foi guardado | Implementado | Arquivo cronológico e detalhe integral |
| Editar a própria nota | Implementado e validado no emulador | `FioService.editEntry`; Arquivo explica o gesto, o detalhe oferece Editar, preserva data original e atualiza `updatedAt` |
| Excluir sem perda imediata | Implementado | mover para Excluídos recentemente por 30 dias |
| Recuperar uma exclusão | Implementado | ação Recuperar em Excluídos recentemente |
| Excluir definitivamente | Implementado | confirmação + autenticação recente |
| Encontrar por palavras | Implementado | busca lexical local, query não persistida |
| Ver notas em ordem temporal | Implementado | Arquivo agrupado cronologicamente |
| Levar os próprios dados | Implementado | exportação local TXT e Markdown legíveis |
| Trazer um histórico TXT/Markdown | Implementado na variante de validação | prévia, deduplicação e rollback; ainda não promovido ao build principal |
| Proteger ao abrir o app | Implementado | bloqueio Android opcional + proteção da tela de recentes |
| Saber se uma ação funcionou | Implementado no fluxo atual | Guardar confirma; editar atualiza o texto; excluir/recuperar removem o item da lista correspondente; falhas preservam o dado e mostram mensagem segura |

## Referências observadas nos vídeos do fundador

Em 2026-08-20 foram assistidos os dois vídeos enviados como exemplos. Eles são
evidência comparativa, não instruções para copiar integralmente outro produto.

| Referência observada | O que ela tornou evidente | Resposta do Fio |
|---|---|---|
| App de notas com lista, busca, cadernos, lixeira e configurações | Ler/encontrar, editar, excluir, recuperar e entender Ajustes são expectativas básicas | Cobertos por Guardar/Encontrar/Arquivo, detalhe editável, Excluídos recentemente e Ajustes P18 |
| Mesmo app com navegação Criar/Notas/Cadernos/Mais | Pastas/cadernos são comuns, mas também introduzem organização manual permanente | Não entram no V0; só reavaliar se usuários reais não conseguirem reencontrar conteúdo pela busca e pelo tempo |
| App com calendário e lista “A fazer” | Notas podem virar agenda/produtividade em produtos generalistas | Não copiar: tarefas e calendário de produtividade contradizem o Fio |
| App em cartões com busca, tags, imagem, gravação de voz e “Pergunte algo” por IA | Mídia e IA são expansões de produto, não requisitos mínimos de uma nota pessoal | Imagem/áudio ficam V2 com análise de privacidade; chat/IA interpretativa permanece proibido |

Conclusão dos vídeos: **não falta ao Fio uma função básica de posse e manutenção
da nota**. O que falta fechar é a honestidade das escolhas temporais (P0 abaixo)
e o gate físico da interface P18 no Poco.

## Comum em apps de notas, mas deliberadamente ausente agora

| Recurso próximo | Decisão do Fio |
|---|---|
| Título obrigatório | Não adicionar; a nota continua texto livre |
| Tags, pastas e cadernos | Não fazem parte do V0; organização manual não pode virar obrigação |
| Checklist, tarefas e calendário | Rejeitado: muda o produto para produtividade |
| Feed, colaboração e compartilhamento social | Rejeitado: o Fio é privado |
| Formatação rica | Fora do V0; texto simples favorece durabilidade e exportação |
| Fixar/favoritar | Não aprovado; pode competir com a lógica temporal e Returns |
| Duplicar nota | Não aprovado; cria cópias ambíguas e exige decisão de provenance |
| Copiar por botão | Seleção de texto existe; botão explícito exigiria avaliação do risco de clipboard |
| Ordenação manual | Não aprovada; Arquivo é cronológico por contrato |
| Fotos e áudio | Planejados apenas para V2, com novos riscos de privacidade |
| Histórico de versões | Planejado para V1; deve ser recuperação, não analytics de edição |
| Sync e backup criptografado | Planejados para V1 após desenho de chave/recuperação |

## Dívidas de integridade encontradas

### P0 — seletor temporal apresenta opções ainda não persistidas

`InPeriod` e `OnDate` vivem apenas no estado Compose da Home. O schema 3 grava
somente `ReturnMode.ELIGIBLE` ou `NEVER`, e `FioViewModel.saveEntry()` não recebe
a escolha visual. Portanto, datas/períodos não podem ser descritos como
funcionais até um packet separado implementar modelo, migration, engine e
testes — ou reduzir honestamente as opções visíveis.

O mesmo packet inclui “Devolver para agora” no detalhe: hoje a ação somente
alterna um estado visual local e mostra “Esta nota voltou”, sem registrar uma
devolução. Ela não pode ser descrita como funcional; a recomendação A1 é
removê-la até existir uma decisão própria para devolução manual.

### P1 corrigido no P18 — editar precisava ser descoberto

O Arquivo agora explica que tocar permite ler, editar ou excluir; cada linha tem
chevron e descrição acessível. O detalhe mantém `Editar` e `Excluir`, sem
reintroduzir botões destrutivos em todas as linhas. Contrato validado no AVD.

### P1 corrigido no P18 — Ajustes expunha linguagem de implementação

Termos como `M1`, `validação`, `lote` sem contexto e intervalos `21h–8h` não
explicavam consequências. P18 trocou a lista técnica por uma visão geral curta
e páginas focadas: Proteção ao abrir, Lembranças que voltam, Importar notas, Exportar uma
cópia e Excluídos recentemente. A hierarquia da UI normal e de validação foi
inspecionada no AVD, sem jargão interno visível.

## Regra contínua

Antes de declarar o app pronto, cada item “Implementado” precisa de pelo menos
uma evidência de serviço/domínio e, quando é ação do usuário, um fluxo de
interface executado. “Existe uma função no código” não prova que a pessoa
consegue encontrá-la ou entendê-la.
