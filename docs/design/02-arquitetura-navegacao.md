# Fio — Arquitetura de Informação e Mapa de Navegação

**Versão:** 1.0 · **Data:** 16 de agosto de 2026 · **Autor:** Manus AI

---

## 1. Princípio organizador

A hierarquia do Fio deve refletir a filosofia: **o ato de escrever é o centro; o tempo e a memória são orbitais; a configuração é perimetral**. A regra prática adotada é: *cada superfície secundária exige no máximo dois toques a partir da Home, e nenhum elemento fixo de navegação compete com a caixa de texto*.

Isso elimina bottom navigation, menu hambúrguer e drawer. A navegação do Fio deixa de ser um mapa de abas e passa a ser uma **linha**: do vazio da escrita, para trás no tempo (Arquivo), e para dentro da configuração (⋯ → Configurações). Todas as superfícies têm um único caminho de volta: o gesto de voltar da plataforma.

## 2. Arquitetura de informação proposta

```
Fio (aplicativo)
│
├── Home — Escrever                    [superfície primária]
│   ├── Pergunta-guia: "O que está passando pela sua cabeça hoje?"
│   ├── Caixa de texto (único protagonista)
│   ├── Seletor temporal discreto — "Quando isso pode voltar? · Algum dia"
│   ├── Botão Guardar (CTA principal) + feedback "Guardado."
│   ├── Link "Arquivo" (canto inferior, texto pequeno)
│   └── Menu "⋯" (canto superior)
│       ├── Arquivo
│       ├── Buscar                    [V1 — aprovado]
│       ├── Configurações
│       └── Sobre o Fio
│
├── Arquivo                            [secundária — "caixa organizada"]
│   ├── Cabeçalhos por ano e mês: "Agosto de 2026"
│   ├── EntryRow: dia · trecho de 1 linha · indicador selo · indicador devolução
│   ├── Tap → Tela da nota
│   └── Menu do arquivo (⋯ no cabeçalho)
│       ├── Buscar
│       └── Lixeira (Excluídos recentemente)
│
├── Nota (aberta)                      [leitura + controles]
│   ├── Data original (serifa), texto, anexos futuros
│   └── Menu "⋯"
│       ├── Lembrar depois…
│       ├── Deixar descansar…
│       ├── Não devolver
│       ├── Selar esta lembrança      [V1 — aprovado]
│       ├── Editar                    (modo inline, sem modal)
│       └── Excluir                   (mover para Lixeira)
│
├── Buscar                             [V1 — iniciada pelo usuário]
│   ├── Campo único, resultados com data + 1 linha
│   └── Sem "nenhum resultado" culpabilizador
│
├── Devolução                          [episódica — abre da notificação "Algo seu voltou."]
│   ├── Data original (serifa) + texto original
│   ├── Fechamento silenciosa (voltar)
│   ├── Menu "⋯": Não mostrar novamente · Deixar descansar · Selar
│   └── [Pesquisa] sheet "Isso significou algo para você? Sim · Não" (pulável)
│
├── Configurações                      [perimetral]
│   ├── Privacidade
│   │   ├── Bloqueio do app (Off · Imediato · 1 min · 5 min)
│   │   ├── Notas seladas (biometria por nota)        [V1]
│   │   └── Cobertura de privacidade (on/off)
│   ├── Devoluções
│   │   ├── Consentimento (ligar devoluções)          [onboarding M2]
│   │   ├── Pausar devoluções
│   │   └── Lembranças programadas / Em descanso      [V1]
│   ├── Dados
│   │   ├── Importar                                    [V0 aprovado, sem UI hoje]
│   │   ├── Exportar (Markdown · Texto)
│   │   └── Lixeira — Excluídos recentemente
│   ├── Preferências
│   │   ├── Aparência (claro · escuro · automático)
│   │   ├── Texto e legibilidade (fonte grande)
│   │   └── Notificações
│   └── Sobre o Fio
│       ├── O que é o Fio (copy curta da filosofia)
│       └── Privacidade e segurança (explicação honesta)
│
└── Superfícies de sistema (não-navegáveis)
    ├── Privacy Cover (recentes do Android)
    ├── Tela de bloqueio (biometria do aparelho)
    └── Safe Open Failure (raro)
```

### Decisões de hierarquia (e por quê)

**Por que um menu "⋯" em vez de hambúrguer:** o hambúrguer comunica "há muita coisa escondida aqui". O "⋯" comunica "há poucas coisas aqui, se precisar". O Fio cabe.

**Por que "Arquivo" aparece duas vezes (link na Home + item do ⋯):** o link na Home é o caminho afetivo ("quero reler"); o item no menu é o caminho estrutural. A duplicação custa zero poluição visual e satisfaz a regra canônica de não tornar o Arquivo difícil de descobrir.

**Por que Buscar e Lixeira estão no ⋯ do Arquivo:** são ferramentas de recuperação, não destinos. Não merecem presença na navegação principal.

**Por que "Lembranças programadas" e "Em descanso" vivem em Configurações → Devoluções:** são gestão de política temporal, não conteúdo. Em V1, quando existirem notas agendadas, o próprio Arquivo ganhará filtros discretos ("Com devolução marcada · Em descanso · Seladas") no seu ⋯ — nunca abas novas.

**Por que nenhuma tela de "minha história", "estatísticas" ou "perfil":** o briefing e `04-FEATURES.md` proíbem dashboard, feed e métricas de uso. A "história" da pessoa **é** o Arquivo.

## 3. Mapa de navegação (caminhos)

```
Lançar o app
  → Privacy Cover (recentes) ou Tela de bloqueio
  → HOME

HOME (Escrever)
  → digitar + Guardar → feedback "Guardado." (300–600 ms) → HOME limpa
  → Guardar + segurar / ícone relógio → TimePickerSheet → Guardar como…
  → link "Arquivo" (rodapé) → ARQUIVO
  → ⋯ (topo)
       → Arquivo → ARQUIVO
       → Buscar → BUSCAR [V1]
       → Configurações → CONFIGURAÇÕES
       → Sobre o Fio → SOBRE

ARQUIVO
  → tap em EntryRow → NOTA
  → ⋯ (cabeçalho)
       → Buscar → BUSCAR
       → Lixeira → LIXEIRA

NOTA
  → ⋯ (topo)
       → Lembrar depois… → TimePickerSheet → "Voltará em…" (feedback sutil)
       → Deixar descansar… → ConfirmSheet → "Descansando até…"
       → Não devolver → confirmação única → indicador "Nunca devolvida"
       → Selar esta lembrança → biometria → indicador de selo
       → Editar → modo inline → guardar edição → NOTA atualizada
       → Excluir → confirmação única → LIXEIRA

DEVOLUÇÃO (abre da notificação "Algo seu voltou.")
  → fechar (voltar) → HOME
  → ⋯ (topo)
       → Não mostrar novamente → NOTA marcada NEVER
       → Deixar descansar → ConfirmSheet
       → Selar → biometria
  → [pesquisa] sheet "Isso significou algo?" → respondido ou pulado

CONFIGURAÇÕES
  → Privacidade → Bloqueio do app / Notas seladas / Cobertura
  → Devoluções → Consentimento / Pausar / Lembranças programadas [V1]
  → Dados → Importar / Exportar / Lixeira
  → Preferências → Aparência / Legibilidade / Notificações
  → Sobre o Fio → filosofia + privacidade

SISTEMA
  → notificação "Algo seu voltou." → DEVOLUÇÃO
  → app em background → Privacy Cover
  → falha de abertura segura → Safe Open Failure (sem navegação)
```

### Matriz de toques

| Destino | Toques a partir da Home |
|---|---|
| Guardar uma nota (padrão) | 0 (escrever) + 1 (toque) |
| Guardar com tempo definido | 1 toque adicional no seletor |
| Abrir o Arquivo | 1 |
| Abrir uma nota | 2 (link Arquivo + tap na linha) |
| Programar "Lembrar depois" numa nota existente | 3 |
| Configurações | 1 (⋯) |
| Devolução (da notificação) | 0 além de tocar a notificação |

Nenhum destino principal exige mais de três toques; o fluxo primário (escrever → guardar) permanece em praticamente zero pensamento.

## 4. Onde cada pedido do briefing se encaixa

| Pedido | Solução |
|---|---|
| Home sem virar dashboard | Bottom bar removida; um link, um ⋯, uma caixa de texto |
| Quatro ações temporais sem poluir | Seletor "Quando isso pode voltar?" de 1 linha abaixo do campo; sheet de opções; menu da nota para ações em nota existente |
| Confirmações sutis | Toast "Guardado." 300–600 ms; "Guardado. Volta em 1 ano." |
| Menu planejado | ⋯ → grupos: navegar (Arquivo, Buscar), cuidar (Configurações), conhecer (Sobre) |
| Arquivo como caixa organizada | Lista por ano/mês, 1 linha, indicadores discretos |
| Nota aberta com controles ocultos | Nota como tela própria; menu ⋯ |
| Devolução minimalista | Data em serifa + texto + fechamento silencioso |
| Pouquíssima navegação visível | Zero barras fixas |
