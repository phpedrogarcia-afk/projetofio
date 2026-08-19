# UX-IDEA-INBOX

Caixa de ideias de UX coletadas durante a Missão 3. Regra herdada da Missão 2: **nada aqui é implementado**. Ideias entram como material bruto para o fundador decidir (e para o Codex avaliar quando a fase de features voltar). Cada ideia registra a fonte e a tensão com os princípios — o Fio não implementa ideias que conflitam com silêncio, devolução gentil ou privacidade, mesmo que boas.

## Ideias da Missão 3

| # | Ideia | Fonte | Tensão com princípios | Status |
|---|---|---|---|---|
| I1 | "On this day" leve: reabrir a última devolução vista no próprio Arquivo, com data absoluta primária | Benchmark Day One (On This Day) | Baixa — resurfacing já é a alma do Fio; a ideia é só um atalho de navegação | INBOX — decisão do fundador |
| I2 | Agrupamento do Arquivo por "esta semana / este mês / antigas" em vez de mês exato (Things-like) | Benchmark Things | Média — a granularidade mensal atual preserva a textura temporal; agrupar demais achata | INBOX — decisão do fundador |
| I3 | Preview do conteúdo na notificação (com ocultação parcial "•••") | Pergunta recorrente de UX de diários | Alta — o ADR das devoluções proíbe texto na notificação; violaria o princípio de privacidade por design | REJEITADA por princípio (registrada para memória) |
| I4 | Modo "cápsula selada" visível no Arquivo (badge "Selada") | Benchmark 1Password/Bitwarden (estado protegido explícito) | Baixa — o estado protegido é honesto sobre si mesmo; não revela conteúdo | INBOX — depende da feature de Nota Selada (futuro) |
| I5 | Undo estrutural pós-exclusão com snackbar de 5s (além do Excluídos recentemente) | Benchmark Google (undo como padrão) | Média — o Fio já tem o Excluídos recentemente como undo de 30 dias; snackbar duplicaria | REJEITADA por redundância (registrada para memória) |
| I6 | Paleta dinâmica Material You com harmonização do verde-sálvia | Android Dynamic Color | Alta — a identidade Verde-Sálvia é a marca; dynamic color a concorreria | REJEITADA por identidade (registrada para memória) |
| I7 | Time sheet com "Em 2 semanas / Em 6 meses" (gradação mais fina) | Benchmark Things (Someday granularity) | Baixa — mais opções = mais atrito na escolha; as 7 atuais cobrem os buckets do engine | INBOX — decisão do fundador |
| I8 | Quiet hours customizáveis além dos dois presets | Benchmark apps de notificação | Média — 2 presets já são uma escolha calibrada; customizar convida a microgerenciar | INBOX — decisão do fundador |

## Anti-inbox (decisões registradas para nunca voltar)

O Fio não implementa streaks, contadores de dias, badges de conquista, "você evoluiu", análises de humor, nuvem obrigatória, analytics de conteúdo, leitura em voz de conteúdo na notificação, nem gamificação de qualquer forma. Essas decisões vêm dos ADRs 042–047 e do benchmark anti (Google Journal: cancelado por métricas de produtividade) e ficam aqui para que nenhum agente futuro as reconsidera sem reabrir os ADRs com o fundador.
