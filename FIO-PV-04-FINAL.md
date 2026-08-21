# FIO-PV-04 — Cosmic Atmosphere Fidelity Pass

Status: implementação e validação locais concluídas; sem merge.

## Branch e base

- Branch: `codex/cosmic-atmosphere-v1`.
- Base: `codex/visual-cosmic-theme-v1`.
- O trabalho preserva a preferência local Sereno/Céu Noturno aprovada depois
  da PV-03 e mantém uma única árvore de UI.

## Camadas implementadas

1. `FioBackdrop` agora compõe base azul-petróleo profunda, três planos de
   névoa radial, campo de estrelas determinístico em duas escalas, estrelas
   focais, constelação, órbitas, vinheta e observatório original periférico.
2. `ui/theme/cosmic/CosmicOrnaments.kt` adiciona Canvas line-art reutilizável:
   bússola, astrolábio, órbita e observatório. Não possui semântica, ação ou
   ligação com dados autobiográficos.
3. Tokens cósmicos passaram a incluir névoa azul/quente e escala de ouro
   suave, muted, glow e borda. As superfícies são mais translúcidas para que
   a atmosfera permaneça perceptível sem sacrificar contraste.
4. A nota aberta recebe véu de leitura próprio, reduzindo a presença do fundo
   atrás das palavras da pessoa.

## Superfícies

- Home: bússola periférica, vidro mais profundo no editor e no controle de
  tempo, ouro contido e observatório distante.
- Folha de tempo: material escuro translúcido, contorno dourado apenas na
  seleção e símbolos existentes preservados.
- Calendário: continua Material 3 e preserva a data civil; usa os tokens de
  vidro e ouro do tema, sem hora exata ou nova lógica.
- Encontrar: campo já existente em vidro e ornamento orbital somente no estado
  sem consulta; não há sugestões, tags ou alteração de busca.
- Arquivo: cartões translúcidos, atmosfera mais rica e observatório somente no
  estado vazio; símbolos não classificam notas.
- Nota: intensidade atmosférica reduzida, para manter texto e ações legíveis.
- Ajustes: cartões em vidro, seleção Céu Noturno com ouro e astrolábio
  decorativo no rodapé; o seletor continua limitado aos dois temas aprovados.

## Biblioteca de assets/canvas

| Elemento | Função | Uso | Opacidade |
|---|---|---|---|
| névoa radial | distância e material | backdrop | 7–19% |
| estrelas | profundidade | backdrop | 18–56% |
| observatório | memória antiga/periferia | backdrop, Arquivo vazio | 7–20% |
| bússola | marca temporal | Home | 28% |
| astrolábio | detalhe contemplativo | Ajustes | 34% |
| órbita | detalhe de busca | Encontrar vazio | 14% |

Todos são vetoriais por Canvas, estáticos, sem bitmap grande, sem animação e
sem efeitos de acessibilidade.

## MATCH / ADAPT / IGNORE

**MATCH:** céu profundo, névoa, estrelas em camadas, ouro envelhecido, vidro,
constelações, órbitas, bússola, astrolábio e observatório line-art original.

**ADAPT:** calendário nativo, Search, Arquivo, nota e confirmação existentes
recebem material e atmosfera sem importar a estrutura ou ações do mockup.

**IGNORE:** perfil/conta, tags, temas de busca, abas novas, hora exata, prazo
de cinco anos, favoritos, compartilhamento, lembretes e qualquer função que
apareça somente na imagem de referência.

## Evidência visual

Capturas sintéticas no `emulator-5556` em `plans/evidence/`:

- `FIO-PV-04-home-after.png`
- `FIO-PV-04-time-sheet-layered.png`
- `FIO-PV-04-calendar-after.png`
- `FIO-PV-04-find-after.png`
- `FIO-PV-04-archive-after.png`
- `FIO-PV-04-note-after.png`
- `FIO-PV-04-settings-layered.png`

Os pares BEFORE das mesmas superfícies são o baseline preservado em
`plans/evidence/FIO-PV-03/`; a PV-04 adiciona as capturas AFTER acima.

O ponto principal do loop foi a nota: a primeira captura mostrou fundo forte
demais para leitura; o véu específico foi adicionado antes do fechamento.

## Pontuação de fidelidade

| Superfície | Atmosfera | Paleta | Símbolos | Vidro | Composição | Leitura | Total |
|---|---:|---:|---:|---:|---:|---:|---:|
| Home | 5 | 5 | 5 | 4 | 4 | 4 | 27 |
| Folha de tempo | 4 | 5 | 5 | 4 | 4 | 4 | 26 |
| Calendário | 3 | 5 | 4 | 4 | 3 | 5 | 24 |
| Arquivo | 4 | 5 | 3 | 5 | 4 | 4 | 25 |
| Encontrar | 4 | 5 | 4 | 4 | 4 | 4 | 25 |
| Nota | 3 | 5 | 3 | 4 | 4 | 5 | 24 |
| Ajustes | 4 | 5 | 4 | 5 | 4 | 4 | 26 |

## Performance e acessibilidade

- Canvas é estático e determinístico; não foram introduzidos shader, blur
  contínuo, animação, dependência ou asset raster pesado.
- Navegação, Archive com listas e abertura da folha de tempo foram inspecionados
  no emulador sem jank evidente. Esta é observação qualitativa, não benchmark.
- Ornamentos não têm conteúdo semântico. Texto creme e secundário mantêm os
  pares de contraste já cobertos pela paleta Material; a nota recebeu redução
  de atmosfera como proteção adicional de leitura.
- Sereno continua com seus tokens congelados e sua árvore original.

## Testes

- Unitários: verdes.
- Instrumentados: **32/32 verdes** no `emulator-5556`.
- Calendário, nota longa, acessibilidade de alvos e persistência de tema foram
  exercitados pela suíte.
- Debug, Release e lint Debug: verdes.

## Escopo e lacunas

Não houve mudança em schema, banco autobiográfico, Search, Returns, crypto,
rede, dependência, navegação, cópia de produto ou feature. A comparação visual
fica limitada pela adaptação responsiva do Android: a referência continua sendo
um north star artístico, não uma tela copiada. Não há merge, push, PR ou uso do
POCO nesta missão.
