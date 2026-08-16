# Fio — Reorganização Completa de UX e Design

**Versão:** 1.0 · **Data:** 16 de agosto de 2026 · **Autor:** Manus AI

Este pacote é a resposta integral ao briefing de reorganização da experiência do **Fio**, o diário pessoal em que "o usuário escreve, o Fio guarda, o tempo devolve". Ele contém seis documentos de design, seis wireframes e dez mockups de alta fidelidade, todos alinhados à filosofia de objeto de afeto em verde-sálvia e construídos a partir de uma auditoria real do código em `app/mobile/` e dos documentos canônicos `docs/03-UX.md`, `docs/04-FEATURES.md` e `docs/DECISIONS.md` do repositório `phpedrogarcia-afk/projetofio`.

---

## 1. A ideia em uma frase

> O Fio deixa de ser um aplicativo com três abas e passa a ser **uma única superfície quase vazia onde se escreve**, com o Arquivo acessível por um único gesto discreto, o tempo como dimensão natural de cada nota e uma linguagem visual fechada em sálvia e marfim que nenhuma tela futura poderá quebrar.

## 2. O que a auditoria encontrou

A base do produto é sólida; o problema está na execução visual e estrutural. A auditoria classificou **4 problemas críticos**, **6 importantes** e **7 cosméticos**. Os críticos, em resumo:

| # | Problema | Efeito no usuário |
|---|---|---|
| C-1 | Bottom navigation de três abas sem ícones | A Home vira um dashboard; o Arquivo compete pela atenção e a hierarquia "escrever é o produto" se perde |
| C-2 | O sistema temporal (Returns) não tem lugar na UI | As ações "lembrar depois / deixar descansar / nunca devolver" não têm onde viver; V1 vai poluir a Home |
| C-3 | Arquivo em cards com texto integral | Parece uma grade de notas genérica (Google Keep), viola a especificação de lista tipográfica por ano e expõe texto que deveria ficar privado |
| C-4 | Confirmação "Guardado." estática e persistente | O feedback vira estado: o usuário percebe o app "mentindo" quando o texto some ao digitar de novo |

A recomendação central é dupla: **eliminar a navegação em abas** em favor de uma Home unificada com menu "⋯" no topo, e **construir um design system fechado** que herde os tokens existentes (`Sage`, `WarmPaper`, `Charcoal`) em vez de substituí-los.

## 3. Os seis documentos deste pacote

| Documento | Conteúdo | Arquivo |
|---|---|---|
| 1 · Auditoria UX | Diagnóstico completo da interface atual (crítico/importante/cosmético) confrontado com as specs | `docs/01-auditoria-ux.md` |
| 2 · Arquitetura e navegação | Novo mapa de navegação, hierarquia de superfícies, reorganização do Arquivo e extensão mínima do modelo `Entry` | `docs/02-arquitetura-navegacao.md` |
| 3 · Design system | Paleta (light/dark), tipografia Fraunces+Inter, escala de espaçamento 4dp, radius, botões, ícones e regras de aplicação | `docs/03-design-system.md` |
| 4 · Fluxos e microinterações | Fluxos completos (escrita zero-fricção, seletor temporal, devolução, selo) e tabela de transições com durações, easing e háptica | `docs/04-fluxos-microinteracoes.md` |
| 5 · Plano de implementação | Quatro etapas A–D, critérios de aceitação, riscos, métricas de sucesso e lugar no repositório | `docs/05-plano-implementacao.md` |
| 6 · Guia dos artefatos visuais | Índice de todos os wireframes e mockups com o que cada tela comunica | `docs/06-guia-artefatos.md` |

## 4. Os artefatos visuais

**Wireframes de baixa fidelidade** (`wireframes/`, estilo esboço em grafite): `wf-01-home` · `wf-02-arquivo` · `wf-03-nota` · `wf-04-seletor-temporal` · `wf-05-devolução` · `wf-06-configurações`.

**Mockups de alta fidelidade** (`mockups/`, modo claro salvo indicação): `hi-01-home` (vazia, referência de estilo) · `hi-02-home-escrevendo` · `hi-03-seletor-temporal-aberto` · `hi-04-toast-guardado` · `hi-05-arquivo` · `hi-06-nota-com-menu` · `hi-07-devolução` · `hi-08-notas-seladas` · `hi-09-configurações` · `hi-10-modo-escuro`.

## 5. Decisões de design mais importantes

**A navegação é uma pilha, não uma barra.** A Home é a raiz única; Arquivo e Configurações vivem sob o menu "⋯" do topo. Isso restaura a assimetria que a especificação exige: escrever é primário, Arquivo é secundário, devolução é episódica.

**O tempo é uma dimensão da nota, não um recurso.** O seletor "Quando isso pode voltar? · Algum dia" mora sempre abaixo do campo de texto, discreto, com quatro modos (normal, lembrar depois, deixar descansar, nunca devolver) num único bottom sheet. Um toque para quem só quer guardar; controle total para quem quer curar a própria memória.

**O Arquivo é tipográfico, não visual.** Linhas de texto com cabeçalhos "Agosto de 2026", trecho de uma linha, e apenas dois indicadores finos (selo · relógio). Nenhum card, nenhuma caixa — o agrupamento faz o trabalho que as caixas fariam.

**A devolução é leitura pura.** Data em serifa, texto em container suave, fio botânico a 15 % no fundo, zero distração. O feedback de pesquisa ("Isso significou algo para você?") é opcional e pulável — nunca obrigatório.

**Cor nunca carrega significado sozinha.** Todo estado comunicável por cor é também comunicável por texto, ícone ou posição; o terracota destrutivo nunca é vermelho puro; e a palavra do usuário é sempre o elemento mais escuro da tela.

## 6. Como implementar

O plano segue quatro etapas encerráveis em pull requests independentes: **A** — fundação do design system (tokens, tema, componentes base); **B** — nova navegação e telas (Home unificada, Arquivo agrupado, Nota com edição in-place); **C** — seletor temporal e menu contextual (coração conceitual); **D** — microinterações, háptica, Reduce Motion e acessibilidade. Nenhum passo toca na persistência, na criptografia ou no motor de Returns — apenas o `Entry` ganha um campo `returnPolicy` com migration de default seguro. O detalhamento completo, com critérios de aceitação, riscos e métricas de sucesso, está no documento 5.

## 7. Próximos passos sugeridos

Primeiro, revisar este pacote e apontar ajustes de tom, copy ou paleta. Depois, enviar os documentos e assets para `docs/design/` no repositório com um issue-mãe de quatro subtarefas (A–D). Em seguida, executar a Etapa A e validar o tema novo sobre a interface existente antes de tocar em navegação.
