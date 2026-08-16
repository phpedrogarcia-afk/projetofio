# Fio — Design System

**Versão:** 1.0 · **Data:** 16 de agosto de 2026 · **Autor:** Manus AI
**Nome interno:** *Sistema Verde-Sálvia* — construído para herdar dos 5 tokens existentes (`Sage`, `SageDark`, `WarmPaper`, `WarmPaperDark`, `Charcoal`, `SoftIvory`), completando-os em vez de substituí-los.

---

## 1. Princípios do sistema

O sistema segue três regras que governam qualquer decisão futura. Primeira, **a palavra do usuário é o elemento mais escuro e mais saturado da tela**: tudo o que é interface fica mais claro e mais neutro que o texto escrito. Segunda, **nada na interface tem cantos mais arredondados que os botões primários** — cantos suaves comunicam objeto feito à mão; cantos excessivos comunicam app genérico. Terceira, **cor nunca é o único portador de significado**: todo estado comunicável por cor é também comunicável por texto, ícone ou posição, e todo texto atende contraste AA.

## 2. Cores

A paleta deriva do sálvia e do marfim existentes, com dois verdes (principal de marca e secundário de interação), um verde profundo para dark, e tons de apoio nunca puros (nunca vermelho puro, nunca cinza azul de sistema).

### 2.1 Modo claro

| Token | HEX | Uso |
|---|---|---|
| `background` | `#F8F4EA` | Fundo principal (marfim quente) — preserva o `WarmPaper` atual |
| `surface` | `#FBF8F0` | Superfícies elevadas (sheets, chips) — um degrau acima do fundo |
| `surfaceVariant` | `#F0EBDE` | Superfícies terciárias, estados hover, fundo de chips |
| `primary` | `#667A66` | Verde sálvia — ações principais, marca, links (preserva o `Sage` atual) |
| `onPrimary` | `#FAF7EE` | Texto sobre primário (preserva `SoftIvory`) |
| `secondary` | `#8CA38A` | Verde secundário — estados hover, indicadores inativos, detalhes botânicos |
| `onSecondary` | `#FBF8F0` | Texto sobre secundário |
| `tertiary` | `#B0BFA6` | Detalhes periféricos, botânicos, placeholders suaves |
| `outline` | `#DCD5C4` | Bordas, dividers, campos em repouso |
| `outlineVariant` | `#EAE4D4` | Dividers de baixa presença |
| `onBackground` / `onSurface` | `#252A25` | Texto principal (preserva `Charcoal`) |
| `onSurfaceVariant` | `#5A6158` | Texto secundário, captions, datas |
| `error` | `#A8543F` | Estado destrutivo — terracota terroso, nunca vermelho puro |
| `onError` | `#FBF7F2` | Texto sobre erro |
| `success` | `#5E7A5A` | Feedback positivo (o primário funciona; este token só existe para acessibilidade de estados) |

### 2.2 Modo escuro

O modo escuro **não inverte** o marfim: ele usa um verde-carvão profundo (`#1E2320`) com acentos sálvia *mais claros* (`#A8B9A0`), mantendo a sensação de sala escura com luz quente em vez de "modo noturno de sistema".

| Token | HEX | Uso |
|---|---|---|
| `background` | `#1E2320` | Fundo principal (verde-carvão) — herda do `WarmPaperDark` atual |
| `surface` | `#262C28` | Superfícies elevadas |
| `surfaceVariant` | `#303732` | Superfícies terciárias |
| `primary` | `#A8B9A0` | Sálvia claro — ações principais |
| `onPrimary` | `#1E2320` | Texto sobre primário (contraste alto) |
| `secondary` | `#859980` | Verde secundário claro |
| `outline` | `#404A42` | Bordas |
| `outlineVariant` | `#333B36` | Dividers sutis |
| `onBackground` / `onSurface` | `#EFEFE9` | Texto principal |
| `onSurfaceVariant` | `#B9BDB2` | Texto secundário |
| `error` | `#C87B63` | Destrutivo (terracota claro) |
| `onError` | `#2A2220` | Texto sobre erro |

### 2.3 Regras de aplicação

O `primary` só aparece em: marca, CTA primário, link "Arquivo", confirmações breves e ícones ativos de controles. Nunca em texto corrido longo (o corpo usa `onBackground`). O `error` aparece apenas em mensagens de falha recuperável e ações destrutivas confirmadas — erros nunca piscam em fundo vermelho; usam ícone + texto terracota sobre o fundo normal. A cor de selo ("selada") e de devolução programada usam `secondary`/`tertiary`, nunca `primary`, para não competir com a ação de guardar.

## 3. Tipografia

Duas famílias, três papéis. **Fraunces** (serifa display, eixo óptico variável, calda botânica discreta) para a marca e datas de memória; **Inter** (grotesk extremamente legível, excelente em tamanhos pequenos) para corpo, editor, controles e acessibilidade. Nenhuma cursiva no corpo — ADR-013.

| Papel | Família | Peso | Tamanho | Line-height | Uso |
|---|---|---|---|---|---|
| `displayBrand` | Fraunces | 500 | 34 sp | 40 sp | Nome "Fio" (topo da Home) |
| `displayDate` | Fraunces | 400 | 22 sp | 28 sp | Data em devolução e nota ("18 de março de 2025") |
| `titleScreen` | Inter | 600 | 20 sp | 28 sp | Títulos de tela ("Arquivo", "Configurações") |
| `titleSection` | Inter | 600 | 15 sp | 22 sp | Cabeçalhos de grupo ("Privacidade") |
| `subtitle` | Inter | 400 | 15 sp | 22 sp | Prompts e descrições |
| `body` | Inter | 400 | 16 sp | 24 sp | Textos de interface |
| `bodyNote` | Inter | 400 | 17 sp | 27 sp | **Texto da nota** — corpo maior e respirado, o texto mais importante da tela |
| `bodyNoteArchive` | Inter | 400 | 15 sp | 22 sp | Trecho da nota no Arquivo (1 linha, truncado) |
| `caption` | Inter | 400 | 13 sp | 18 sp | Metadados, datas, indicadores |
| `captionSmall` | Inter | 400 | 12 sp | 16 sp | Timestamps secundários |
| `button` | Inter | 500 | 15 sp | 20 sp | Rótulos de botão |

Escala dinâmica: todos os tamanhos usam `sp`; `bodyNote` cresce com preferência de fonte do sistema sem clipping (testar até 200 %). O texto da nota nunca usaFraunces — a serifa é do sistema, não do diário.

## 4. Espaçamento

Escala base de **4 dp**, com saltos em 4/8/12/16/24/32/48. Espaçamento interno de componentes segue um único princípio: *conteúdo respira em múltiplos de 8; grupos respiram em múltiplos de 24*.

| Token | Valor | Uso canônico |
|---|---|---|
| `space1` | 4 dp | Ícone–texto, entre linhas de metadata |
| `space2` | 8 dp | Padding de chips, entre ações adjacentes |
| `space3` | 12 dp | Padding interno de botões, entre itens de lista |
| `space4` | 16 dp | Margem horizontal padrão da tela |
| `space5` | 24 dp | Padding vertical entre seções; padding de sheets |
| `space6` | 32 dp | Respiração da Home; entre grupos do Arquivo |
| `space7` | 48 dp | Espaçamento do estado vazio; hero da Home |

A Home usa `space6` (32 dp) de margem lateral em vez dos 24 dp atuais: o vazio é parte da identidade.

## 5. Radius

Três níveis, herdados da ideia de "objeto". **`radiusFull` 999 dp** apenas para pills de feedback ("Guardado.") e badges de indicador. **`radiusLg` 20 dp** para botões primários e bottom sheets (a partir da base). **`radiusMd` 12 dp** para chips e campos em foco. Nenhum card do Arquivo tem container arredondado: linhas de texto não precisam de caixa — o agrupamento tipográfico faz o trabalho (isso elimina o problema "parece Google Keep" na origem).

## 6. Botões

| Variante | Aparência | Uso |
|---|---|---|
| **Primary** | Fundo `primary`, texto `onPrimary`, radius 20, altura 52 dp, largura total na Home | Único na tela: **Guardar**. Pressionado: escurece a superfície em 8 %, escala 0,98 por 120 ms |
| **Primary + ícone temporal** | Igual ao Primary, com ícone de relógio fino (20 dp) à direita do rótulo | Mesma ação, acesso ao seletor temporal |
| **Secondary** | Borda `outline` 1,2 dp, fundo transparente, texto `onBackground` | Ações alternativas fora do fluxo primário (Importar, Exportar) |
| **Text** | Sem container, texto `primary` 15 sp | Links: "Arquivo", ações de menu |
| **TextDestructive** | Sem container, texto `error` | Excluir (sempre com confirmação) |
| **Icon** | Área de toque 48×48 dp, ícone 22 dp, cor `onSurfaceVariant`, hover `tertiary` | O ⋯ do topo; ações de linha |
| **Pill (feedback)** | `surface` elevada + sombra suave, texto `onBackground`, raio full, 36 dp de altura | Toast "Guardado." — não é um botão |

Alto mínimo de toque: 48 dp. O botão Guardar tem 52 dp para hierarquia afetiva (o maior elemento tocável do app).

## 7. Cards — onde eles existem de verdade

A regra do sistema: **cards só existem onde há um objeto a ser elevado**. No Fio, isso significa exatamente três lugares: (1) a devolução, quando a nota ressurge sozinha — um container suave que a separa do fundo, como uma carta sobre a mesa; (2) o sheet de confirmação (bottom sheet); (3) agrupamentos de configuração quando necessário. **O Arquivo não usa cards** — linhas de texto com dividers `outlineVariant` entre meses. EntryRows usam padding, não container.

## 8. Ícones

Pacote único: linha fina (stroke 1,5 dp), terminais levemente arredondados, estilo "botânico-contemporâneo". Densidade 22 dp em contexto, 20 dp em botões. Símbolos definidos: `clock` (temporal), `seal` (selo — lacre fino circular), `archive` (estante/linha de arquivo), `leaf` (marca), `more` (⋯ vertical), `arrowUp` (voltar/topo), `check` (feedback), `search` (busca), `gear` (configurações — versão fina). Nenhum ícone preenchido (filled); preenchimento só em estado ativo de um controle, com `primary`.

## 9. Elevação e sombras

Três níveis de sombra, todos suaves e difusas (sem "drop shadow" dura de sistema): **E1** 0 1 2 rgba(0,0,0,0,04) para chips e pills de feedback; **E2** 0 8 24 rgba(0,0,0,0,08) para bottom sheets; **E3** 0 12 32 rgba(0,0,0,0,10) para devolução. Em dark mode, elevação é comunicada por clareamento da superfície (+8 % / +12 %) em vez de sombra.

## 10. Estados de interação (componentes)

| Estado | Tratamento |
|---|---|
| Default | Cor base |
| Hover (tablet/teclado) | `surfaceVariant` ou `secondary` 10 % |
| Pressed | Superfície escurecida 8 %, escala 0,98 (120 ms) |
| Disabled | `onSurfaceVariant` 40 %, sem sombra |
| Focus (acessibilidade) | Anel de 2 dp em `primary` com offset 2 dp |
| Error (campo) | Borda `error`, mensagem em `error` 13 sp abaixo |

## 11. Detalhes botânicos

Um único motivo gráfico: **o fio** — uma linha vertical fina (`outlineVariant`) com duas pequenas folhas laterais (stroke 1 dp, `tertiary` 60 %), usada *exclusivamente* em estados vazios e no fundo da devolução (opacidade 15 %). Nunca sobre texto do usuário, nunca animada em loop. É a assinatura visual que diferencia o Fio do Evernote sem copiar nada dele.

## 12. Copys canônicas do sistema

| Momento | Texto |
|---|---|
| Home | O que está passando pela sua cabeça hoje? |
| Placeholder | Escreva quando quiser. |
| CTA | Guardar |
| Seletor temporal | Quando isso pode voltar? · Algum dia |
| Sucesso | Guardado. |
| Sucesso agendado | Guardado. Volta em 1 ano. |
| Devolução | Algo seu voltou. |
| Arquivo vazio | Quando quiser, suas palavras podem ficar aqui. |
| Busca vazia | Nada com essas palavras, por enquanto. |
| Lixeira | Excluídas recentemente ficam aqui por 30 dias. |
