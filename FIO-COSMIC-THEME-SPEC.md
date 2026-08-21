# Fio — Especificação canônica do tema Céu Noturno

Status: aprovado para FIO-PV-03

Referência visual canônica: `WhatsApp Image 2026-08-20 at 15.52.38.jpeg`

Escopo: aparência das superfícies existentes; nenhuma nova função de produto.

## 1. Intenção

Céu Noturno é a expressão noturna do Fio: íntima, temporal e contemplativa.
Deve parecer um objeto pessoal guardado sob um céu profundo, não um aplicativo
espacial, um jogo ou um painel esotérico. A palavra do usuário continua sendo o
elemento principal.

Palavras de direção: profundidade, tempo, cuidado, mistério, delicadeza,
cartografia celeste e ouro envelhecido.

## 2. Autoridades e limites

- A imagem canônica governa estrutura visual, material, paleta, simbologia e
  atmosfera.
- O aplicativo existente governa navegação, conteúdo, estados, ações e regras.
- Sereno permanece integralmente preservado, incluindo seus valores de cor,
  tipografia, espaços, raios e comportamento.
- Céu Noturno não cria uma segunda cópia de Home, Encontrar, Arquivo, Nota,
  Ajustes, Return, privacidade ou navegação.
- Nenhuma mudança de schema, domínio, Search, Return, crypto, rede, dependência
  ou persistência é autorizada.

## 3. Tradução da referência

### MATCH — reproduzir com fidelidade

- fundo azul-petróleo quase preto, com profundidade atmosférica e vinheta;
- névoas em camadas, estrelas de escalas distintas e constelações esparsas;
- superfícies de vidro escuro translúcido, com separação por luz, contorno e
  highlight superior muito leve;
- detalhes em ouro suave e creme quente, nunca amarelo saturado;
- títulos serifados, texto de leitura sereno e hierarquia editorial;
- ícones lineares e símbolos temporais já existentes, mais bússola,
  astrolábio, órbitas e observatório original somente como ornamentos;
- botões primários escuros/translúcidos com borda e texto dourado;
- cantos arredondados, divisores silenciosos e seleção luminosa contida;
- atmosfera consistente em Home, folha de tempo, calendário, Encontrar,
  Arquivo, nota aberta e Ajustes.

### ADAPT — manter intenção, respeitando o produto real

- A composição de cada tela segue sua superfície atual e responsiva.
- O motivo cósmico deve passar por trás das superfícies compartilhadas; não é
  um wallpaper ilustrado por tela.
- Ornamentos por superfície são line-art original de baixa opacidade e nunca
  são anunciados por acessibilidade, interpretam uma nota ou criam ação.
- O calendário permanece Material 3 maduro e mantém o contrato de data civil
  em UTC corrigido no FIO-PV-02.
- A confirmação de Guardar continua no fluxo atual; o mockup não autoriza uma
  nova rota ou uma nova tela.
- Os ícones atuais de tempo e navegação recebem a paleta cósmica; sua semântica
  não muda.

### IGNORE — não autorizado pelo mockup

- perfil/conta;
- lembretes;
- sugestões e chips de busca;
- busca por temas inferidos;
- abas `Linha do tempo` / `Por temas`;
- prazo de cinco anos;
- escolha de hora exata;
- favoritos, compartilhamento ou novas ações na nota;
- nova tela de confirmação `Guardado`;
- qualquer texto, entrada ou memória mostrada apenas como exemplo no mockup.

## 4. Paleta canônica

| Token | Valor | Papel |
|---|---:|---|
| `cosmicBackground` | `#071D20` | abismo azul-petróleo |
| `cosmicBackgroundDeep` | `#041417` | profundidade e bordas externas |
| `cosmicSurface` | `#0D282B` a 95% | vidro principal |
| `cosmicSurfaceElevated` | `#123033` a 96% | folha, diálogo e seleção elevada |
| `cosmicSurfaceSoft` | `#163538` a 92% | campos e grupos silenciosos |
| `cosmicGold` | `#D5B773` | ação, seleção e símbolo principal |
| `cosmicGoldBright` | `#E8D49B` | brilho focal mínimo |
| `cosmicCream` | `#F0E6D2` | texto principal |
| `cosmicMuted` | `#B9AE99` | texto secundário e metadados |
| `cosmicOutline` | `#8F7A4D` a 72% | contorno dourado silencioso |
| `cosmicOutlineSoft` | `#426063` a 72% | separação de vidro |
| `cosmicError` | `#D69A86` | erro sem vermelho puro |

Contraste mínimo: 4,5:1 para texto corrente e 3:1 para texto grande, ícones e
controles essenciais. Transparência nunca pode reduzir legibilidade abaixo
desses valores.

## 5. Material

- O vidro é obtido por cor translúcida, contorno fino e contraste local; não
  usar blur caro, reflexos fortes ou animação contínua.
- Superfícies principais: 12–20 dp de raio, conforme tokens existentes.
- Contorno: 1 dp; ouro apenas em foco, ação ou seleção. Contornos comuns usam
  azul mineral.
- Elevação é baixa. Profundidade vem de camadas, não de sombras pretas.
- Botão primário: vidro escuro, contorno dourado, texto creme/ouro. Estado
  desabilitado permanece claramente distinto.

## 6. Céu e símbolos

O fundo é um desenho Compose determinístico e compartilhado:

- gradiente vertical azul-petróleo;
- duas névoas radiais de baixa opacidade;
- estrelas fixas em três tamanhos, sem cintilação;
- no máximo duas constelações lineares suaves por viewport;
- mapa/rosa celeste abstrato opcional em um canto, nunca atrás do texto;
- nenhum planeta grande, zodíaco, templo, pirâmide ou ornamento narrativo.

Símbolos do tempo mantidos: infinito, calendário, retorno temporal e círculo
cortado. Lua, estrela e bússola são marcas atmosféricas periféricas, não novas
ações. Envelope/carta continua reservado para ideias futuras.

## 7. Tipografia

- Marca, títulos de tela e datas memoráveis: Fraunces.
- Texto funcional, controles e metadados: Inter.
- Texto da nota: Inter, preservando legibilidade e ADR-013; o tema não altera
  nem estiliza as palavras da pessoa.
- Céu Noturno pode ampliar a presença editorial dos títulos, mas mantém a
  escala responsiva, fonte do sistema e suporte a 200%.

## 8. Critérios de aceite visual

Uma superfície só avança quando a comparação confirmar:

1. mesma estrutura e ações do produto atual;
2. fundo cósmico visível sem competir com conteúdo;
3. vidro legível e perceptível, sem bloco chapado;
4. ouro restrito a hierarquia, tempo e ação;
5. atmosfera contínua entre tela e navegação;
6. ícones semanticamente corretos;
7. nenhuma regressão visual no Sereno;
8. nenhuma função importada do mockup.

O ciclo obrigatório é: `captura → comparação → delta → correção`. Diferença
relevante de estrutura, material, paleta, simbologia ou atmosfera impede o
avanço para a superfície seguinte.
