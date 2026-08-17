# Fio — Auditoria de Experiência

**Versão:** 1.0 · **Data:** 16 de agosto de 2026 · **Autor:** Manus AI
**Escopo:** interface existente do repositório `phpedrogarcia-afk/projetofio` (snapshot `v0.1`, M1 Android, Compose) confrontada com `docs/03-UX.md`, `docs/04-FEATURES.md` e `docs/DECISIONS.md`.

---

## 1. Resumo executivo

A base do Fio é sólida e coerente: a direção filosófica ("o usuário escreve, o Fio guarda, o tempo devolve") já está documentada e parcialmente implementada. O problema não está no produto — está na **execução visual e estrutural**. A interface atual implementa uma moldura genérica do Material 3 sem o sistema visual próprio que a especificação canônica promete, e a organização das superfícies dá o mesmo peso visual a ações primárias e secundárias, o que contradiz a hierarquia que o próprio `03-UX.md` define.

Em síntese, a auditoria identificou **4 problemas críticos**, **6 problemas importantes** e **7 problemas cosméticos**, todos solucionáveis sem tocar na arquitetura de persistência, criptografia ou no engine de devoluções.

A recomendação central é dupla: **(a)** eliminar a bottom navigation de três abas em favor de uma Home quase vazia com um único ponto de entrada para o Arquivo e o menu, restaurando a hierarquia "Escrever é o produto, Arquivo é a memória, Configurações é a casa"; **(b)** construir um design system pequeno e fechado — cores, tipografia serifada de display, escala de espaçamento, botões e ícones finos — para que toda tela futura (inclusive as de V1, como seletor temporal e notas seladas) herde a mesma linguagem silenciosa.

---

## 2. Método

Cada tela e componente do módulo `app/mobile` foi lido linha a linha (`FioApp.kt`, `FioViewModel.kt`, `Color.kt`, `Theme.kt`, `Models.kt`, `strings.xml`, `MainActivity.kt`) e confrontado com os documentos canônicos do repositório. A classificação usa três níveis:

| Nível | Critério |
|---|---|
| **Crítico** | Viola a hierarquia, a filosofia ou a especificação canônica; afeta o fluxo central do produto |
| **Importante** | Degrada a experiência sem violar a filosofia; gera fricção ou incoerência perceptível |
| **Cosmético** | Não afeta uso, mas enfraquece a percepção de produto feito à mão |

---

## 3. Problemas críticos

### C-1 — Bottom navigation de três abas destrói a hierarquia de superfície
**Onde:** `FioApp.kt`, `MainSurface` enum e `NavigationBar`.

A especificação `03-UX.md` define quatro superfícies com papéis claramente assimétricos: Write é *primária*, Archive é *deliberadamente secundária*, Return é *episódica* (aberta de uma notificação), Settings é *funcional*. A implementação, porém, renderiza as três superfícies como itens iguais em uma `NavigationBar`, cada um com `icon = {}` (sem ícone) e rótulos de mesmo tamanho. O resultado é que o Arquivo — que deve ser "secundário sem depender de gestos obscuros", mas nunca igual ao ato de escrever — recebe o mesmo status visual da Home, e o Arquivo compete pela atenção sempre que o usuário está na Home.

Além disso, uma barra de três itens fixa na base contradiz diretamente a preferência declarada de "pouquíssima navegação visível" e o desejo de que a Home pareça quase vazia. Três abas transformam silenciosamente a Home em um dashboard de três destinos.

**Recomendação:** remover a bottom bar. A Home (Escrever) torna-se a superfície quase vazia; o Arquivo passa a ser acessível por um único link textual discreto no canto inferior da Home (por exemplo, "Arquivo →"), e Configurações por um único ícone de engrenagem fina no canto superior — ou, mais silencioso ainda, um único ponto de menu "⋯" no topo que revela Arquivo e Configurações juntos. A especificação canônica exige que o Arquivo seja fácil de descobrir, e um rótulo "Arquivo" no fim da tela de escrita cumpre isso sem criar a aparência de aplicativo de três seções.

### C-2 — Guardar é uma ação solitária e o sistema temporal não tem lugar no fluxo
**Onde:** `WriteScreen` (botão único) e ausência total de controles temporais na UI.

O modelo `Entry.returnMode` só suporta `ELIGIBLE` e `NEVER`. Não existe nenhum elemento de interface para "Lembrar depois", "Deixar descansar" ou para o estado temporal da nota. Isso é tecnicamente correto para V0 (esses recursos são Approved em V1 por `04-FEATURES.md`), mas a **arquitetura de UI não deixou o lugar vago**: o botão Guardar ocupa toda a largura do rodapé e a tela não possui nenhum gancho visual para as quatro ações temporais (Guardar / Lembrar depois / Deixar descansar / Não devolver) que o produto exige. Quando V1 for implementado, adicionar essas opções em uma UI que foi desenhada para uma única ação inevitavelmente poluirá a Home.

**Recomendação:** desenhar desde agora o seletor temporal como um pequeno mecanismo embutido no botão Guardar (ícone discreto de relógio à direita do rótulo, que abre um bottom sheet suave com "Guardar como…"), em vez de reservar quatro botões no rodapé. A Possibilidade 3 do seu briefing — o seletor como item discreto *abaixo* da caixa de texto, respondendo "Quando isso pode voltar? — Algum dia" — é a variante com menor custo de descoberta e melhor integração com a estética de linha única; a variante do ícone no botão funciona como equivalente e ambas são documentadas na seção de design.

### C-3 — O Arquivo viola a própria especificação canônica
**Onde:** `ArchiveScreen` e `EntryCard`.

`03-UX.md` prescreve uma lista "calma e completa" agrupada **por ano e data, mais novo primeiro**, com preview curto opcional e controles escondidos em um menu discreto. A implementação atual renderiza `EntryCard`: um `Card` Material 3 para cada entrada, com **o texto completo visível (até 8 linhas), a data em destaque e os botões "Editar" e "Excluir" permanentemente visíveis**. Em termos visuais, o Arquivo atual é próximo de um grade de cards do Google Keep — exatamente o que a nova diretriz proíbe.

Há ainda um problema de privacidade: mostrar o texto integral de cada nota na listagem contradiz a regra de preview curto e opcional, e as notas seladas (V1) jamais deveriam revelar conteúdo em listagem.

**Recomendação:** substituir os cards por **linhas de texto plano** (`EntryRow`), com agrupamento por mês/ano como cabeçalhos tipográficos ("Agosto de 2026"), um trecho truncado de uma linha, e apenas dois indicadores discretos (selo · devolução programada). Editar/Excluir/selecionar movem-se para um menu contextual "⋯" por item, abrindo em bottom sheet.

### C-4 — Confirmação de salvamento é estática, não é um feedback
**Onde:** `savedNotice` em `WriteScreen`/`FioViewModel`.

O texto "Guardado." é exibido como um `Text` permanente dentro da coluna, aparecendo sob a caixa de texto e ficando lá enquanto o usuário continua na tela. Isso contradiz o padrão canônico — "confirmação imediata, breve e visualmente modesta" — e ainda pior: como o estado vive no ViewModel, o texto é limpo apenas quando o usuário digita novamente, transformando um feedback de meio segundo em um estado persistente. O usuário percebe "o app disse guardado e depois o texto sumiu", em vez de um suspiro visual discreto.

**Recomendação:** transformar `savedNotice` em um **toast leve ancorado acima do botão** (checap pequeno + "Guardado.") com fade-in de 150 ms, permanência de ~1,5 s e fade-out de 300 ms, retornando a Home ao estado inicial. Quando houver data agendada: "Guardado. Volta em 1 ano." — mesmo padrão, texto uma linha mais longo.

---

## 4. Problemas importantes

### I-1 — Edição acontece dentro de um AlertDialog grande
**Onde:** `EditEntryDialog` em `ArchiveScreen`.

Editar uma nota — ação recorrente — exige abrir um `AlertDialog` com título, corpo e dois botões de diálogo. Diálogos modais são a linguagem de *confirmação* do Material 3, não a linguagem de *edição*. O resultado é uma experiência densa: borda de diálogo, títulos redundantes, botão "Guardar alterações" e "Cancelar", tudo para uma tarefa de texto simples.

**Recomendação:** abrir a nota em uma **tela de leitura dedicada** (`NoteScreen`): data no topo em serifa, texto em corpo legível, menu "⋯" com Editar (que vira modo de edição inline na própria tela), Selar, Lembrar depois, Deixar descansar, Não devolver e Excluir. A tela se fecha com gesto de voltar — sem botões de "Cancelar".

### I-2 — Settings é uma lista única sem hierarquia visual
**Onde:** `SettingsScreen`.

Privacidade, exportação e lixeira convivem na mesma `LazyColumn` separadas apenas por `HorizontalDivider` e títulos `titleLarge` de mesmo peso do cabeçalho da página. O usuário não percebe que está numa página *de categorias*: tudo parece o mesmo nível de importância, e a lixeira (secundária) ocupa o mesmo espaço visual que o controle de bloqueio do app (primário de privacidade).

**Recomendação:** reorganizar em grupos com subtítulos (`SectionHeader` em caption) e usar células de lista clicáveis (padrão de configurações moderno) em vez de 4 + 2 botões `OutlinedButton` de largura total. Importar (V0 approved, sem UI) e "Sobre o Fio" entram aqui.

### I-3 — Bottom bar sem ícones gera incompletude
**Onde:** `NavigationBar { icon = {} }`.

Passar `icon = {}` vazio quebra a expectativa da plataforma (o Material 3 reserva espaço para ícone e renderiza rótulos deslocados) e sinaliza "aplicativo inacabado". Se a decisão for manter a bottom bar em alguma iteração intermediária, ela precisa de ícones finos coerentes com o sistema (folha/linha para escrever, estante para arquivo, engrenagem fina para configurações).

**Recomendação:** removê-la (ver C-1). Se mantida em transição, usar `NavigationBarItem` com ícones finos do pacote de ícones do design system.

### I-4 — Não existe tela de devolução desenhada
**Onde:** ausência no módulo `ui`; M1 não entrega Returns (por `03-UX.md`, Returns dependem de consentimento global, estado `notConfigured`).

O produto afirma que a tela de devolução é "provavelmente a tela mais importante de todo o aplicativo", e ela simplesmente não existe no código — nem placeholder. Quando a notificação "Algo seu voltou." abrir o app em M2, não haverá destino. Isso não é um bug de UI atual, mas a ausência total deixa o fluxo narrativo do produto incompleto e a arquitetura de navegação sem um destino para deep links.

**Recomendação:** criar `ReturnScreen` agora (visível somente atrás de feature flag), seguindo exatamente a gramática canônica: data em serifa, texto original, fechamento silencioso, menu "⋯" com Não mostrar novamente / Deixar descansar, e — em builds de pesquisa — o sheet separado "Isso significou algo para você?".

### I-5 — Estados vazios e de erro são funcionais mas sem personalidade
**Onde:** strings literais em `ArchiveScreen`, `SettingsScreen`, `FioApp.kt`.

As copys canônicas estão corretas ("Quando quiser, suas palavras podem ficar aqui."; "Algo seu voltou."; "Não foi possível guardar agora. O texto continua no editor…"), mas são renderizadas como `Text` simples sem qualquer gramática visual de empty state (nenhum detalhe botânico discreto, nenhuma tipografia de estado). O vazio, num produto que quer parecer "caixa organizada de escritos pessoais", é uma das oportunidades mais baratas de emoção.

**Recomendação:** cada estado vazio recebe um único elemento gráfico mínimo (um fio botânico de linha fina, um selo, uma prateleira) em opacidade ~20 %, centralizado com generosidade, e a copy canônica em `bodyLarge` com margem generosa.

### I-6 — O sistema de cores não cobre o mapa completo de estados
**Onde:** `Color.kt` (5 tokens) e `Theme.kt`.

O tema define apenas `primary`, `background`, `surface` e `onSurface` para light; o dark scheme omite `onPrimary`, `onSurface` e todos os slots secundários. Não existem tokens para `outline`, `surfaceVariant`, `error`/`destructive`, nem um verde secundário, o que força componentes (dividers, chips, botões `Outlined`, mensagens de erro) a caírem em defaults genéricos. É por isso que hoje há `OutlinedButton` cinza genérico convivendo com um verde sálvia que o Material 3 tenta (mal) harmonizar.

**Recomendação:** completar o color scheme com tokens explícitos, incluindo variantes para modo escuro (verde mais claro sobre fundo profundo, nunca verde escuro sobre preto), e mapear destructive a um tom terroso-vermelho discreto em vez do vermelho puro de alerta.

---

## 5. Problemas cosméticos

| # | Problema | Onde | Por que importa |
|---|---|---|---|
| X-1 | Sem escala tipográfica própria; display usa `headlineLarge` Material default (Roboto/Noto) | `FioApp.kt` | O nome "Fio" deveria ser serifa elegante, como a especificação exige; fonte default dilui a identidade |
| X-2 | Sem tokens de espaçamento, radius ou elevação | Global | Componentes novos nascerão com espaçamentos arbitrários |
| X-3 | Botão Guardar é um `Button` Material padrão (elevação e fill genéricos) | `WriteScreen` | O CTA principal do produto é o elemento mais importante da tela e parece genérico |
| X-4 | Data do Arquivo usa `DateTimeFormatter.MEDIUM` com hora ("16 de agosto de 2026 14:32") | `displayDate()` | A hora compete com a memória; a gramática canônica usa só o dia |
| X-5 | Cards com `Card` Material de contorno default e sombra genérica | `EntryCard` | Borda cinza do Card cria grid visual de Keep |
| X-6 | Erro de salvamento aparece como texto inline sem hierarquia de estado | `WriteScreen` | Erros recuperáveis precisam de tratamento visual próprio (cor, ícone sutil, não apenas cor de texto) |
| X-7 | Sem modo escuro visualmente testado (tokens incompletos) | `Theme.kt` | Modo escuro é requisito do briefing (item 17 dos estados) |

---

## 6. O que a auditoria confirma que está certo

Para não descartar trabalho bom: o fluxo Write → Guardar → Arquivo está **correto e alinhado à filosofia**; o autosave com debounce silencioso (700 ms) é exatamente o comportamento desejado; o tratamento de falha ("O texto continua no editor") preserva o rascunho sem dramatizar; a `PrivacyCover` e a política de bloqueio seguem os ADRs; as copys canônicas já existem em código. A reorganização, portanto, é majoritariamente **reorganizar o que existe e dar-lhe linguagem visual própria** — não reconstruir o produto.

---

## 7. Diagnóstico consolidado

| Dimensão | Estado atual | Estado desejado |
|---|---|---|
| Hierarquia de superfícies | 3 abas iguais | Home dominante; Arquivo como link; Configurações como ponto único |
| Guardar | Botão único, sem sistema temporal | Guardar + seletor temporal discreto em 1 toque |
| Confirmação | Texto estático persistente | Feedback transitório 300–600 ms, "Guardado." |
| Arquivo | Grid de cards com texto integral e botões | Lista tipográfica por ano/mês, 1 linha, menu "⋯" |
| Menu | Settings como lista única | Página agrupada em seções com células de lista |
| Tema | 5 tokens, incompleto em dark | Design system fechado (cores, tipo, espaçamento, radius, ícones) |
| Estados | Literais simples | Empty states com elemento gráfico e copys canônicas |
