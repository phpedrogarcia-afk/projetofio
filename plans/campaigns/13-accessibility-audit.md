# Campaign 13 — Accessibility (TalkBack / seções M2/M3)

**Prioridade:** P2 · **Método:** auditoria estática dos modificadores de semântica do Compose + checklist de boas práticas (sem AVD no sandbox, portanto sem execução real de TalkBack; a verificação de conteúdo falado depende do emulador e fica como recomendação de teste manual) · **Resultado:** VERDE — zero achados P0–P2; 3 observações P3/P4.

## O que foi verificado (FioApp.kt, ~1.200 linhas)

| # | Critério | Veredito | Evidência |
|---|----------|----------|-----------|
| 1 | Headings de navegação (`heading()`) | **Bom** — "Arquivo", "Configurações", "Uma palavra sua voltou" e títulos secundários marcados como heading; TalkBack anuncia rota entre seções. | FioApp.kt:228, 579, 899, 1113, 1170 |
| 2 | Content descriptions em botões com ícones | **Bom** — "Voltar" (3×), "Mais opções", ação de devolver, estado "Selecionado" e política de devolução descritas explicitamente. | FioApp.kt:241, 495, 577, 713, 745, 897 |
| 3 | liveRegion = Polite para mudanças dinâmicas | **Bom** — mensagens de status (import, export, erro) anunciam ao leitor de tela sem interromper. | FioApp.kt:338, 395, 756, 1006, 1030 |
| 4 | mergeDescendants em linhas de lista | **Bom** — cada linha de entry é anunciada como um único foco coeso. | FioApp.kt:279 |
| 5 | Sem `contentDescription = null` silencioso em elementos clicáveis | **Verificado** — os dois `contentDescription = null` restantes são em elementos decorativos (ícones de textura), corretamente silenciados. | FioApp.kt:315, 343 |
| 6 | Tamanhos de toque ≥ 48dp | **Aceitável** — botões usam `FioSpace` com padding generoso; nenhum botão abaixo de 44dp identificado na revisão estática. | FioApp.kt (modificadores) |
| 7 | Contraste Verde-Sálvia (design v1) | **Aceitável com ressalva** — paleta usa sálvia escuro sobre off-white; recomendação P4 de validar contraste WCAG AA 4.5:1 em labels secundárias com fonte pequena. | theme/ |
| 8 | Focus/IME acessibilidade | **Bom** — editor usa `BasicTextField` com keyboardOptions padrão do Android (IME não desabilitada); o trade-off de sugestão de teclado já está documentado na campanha 5 (P4). | — |

## Recomendações (P3/P4 — evolução futura)

1. **Teste manual com TalkBack:** com um AVD, percorrer as 4 telas (Arquivo, Editor, Return, Configurações) ouvindo a sequência falada — a única forma de validar conteúdo e ordem de foco reais. Checklist de 10 min para o fundador.
2. **Contraste em labels pequenas:** validar WCAG AA 4.5:1 na paleta sálvia para bodySmall/caption.
3. **Testes instrumentados de a11y:** a suíte `M2AndroidContractTest` (instrumented) cobre contratos M2/M3; rodá-la em CI com TalkBack habilitado seria o próximo degrau automático.

## Conclusão

As quatro seções M2/M3 estão **cobertas por semântica explícita** (headings, descriptions, live regions, merge). Não há achados P0–P2. As três recomendações acima são baratas de executar em um AVD real e ficam abertas para o fundador.
