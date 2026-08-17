# Plan — Fase 2.2: Home redesenhada (escrita zero-fricção + temporal + primeira cápsula)

Fatia: redesenhar a Home do Fio sobre o design system Verde-Sálvia, mantendo o
modelo de dados e o serviço intocados (sem migração, sem nova superfície além
do seletor temporal por entrada).

## State atual
`FioApp.kt` usa `Scaffold` com bottom bar de 3 abas (WRITE/ARCHIVE/SETTINGS),
`OutlinedTextField`, "Guardado." como texto persistente (violando ADR-014),
data da entrada exibida solta (S-2), entry cards no Arquivo, edição por
AlertDialog. `FioViewModel` tem autosave de draft (debounce 700ms),
`savedNotice`, e ações entry-level. Tokens do design system já estão em
`MaterialTheme` (Fase 2.1 feita, commit 2ceaa9f).

## Ações
1. Navegação: remover bottom bar. Home é a raiz; ⋯ (menu de topo) abre um
   `DropdownMenu` com Arquivo e Configurações — mantém tudo acessível sem
   hierarquia de app-genérica. ADR-004 + pacote docs/design.
2. Home (`WriteScreen`): prompt "O que está passando pela sua cabeça hoje?"
   em Inter 15sp, marca "Fio" em Fraunces 34sp, campo sem contorno visível
   (editor limpo, cor de fundo `surface`), margem lateral 32dp.
3. Botão Guardar: full-width, 52dp, radius 20, com ícone relógio (clock)
   à direita abrindo o **seletor temporal** — bottom sheet com 4 opções:
   Algum dia (default, ELIGIBLE), Especificar um período (7d/30d/90d/1 ano),
   Escolher uma data (date picker), Nunca (NEVER). Mapeamento ADR-043:
   período/data → `ReturnMode.ELIGIBLE` + marcação "quando-voltar" local;
   por ser V0 sem engine M2 no main antigo, persistir como
   `lastReturnedAt`-adjacente: usar estado de draft temporal via ViewModel
   (`draftReturnPolicy` em memória — a aplicação real depende do merge do
   PR #1 que traz o engine; registrar o contrato agora).
4. Confirmação: pill flutuante "Guardado." que desaparece após 1,5 s
   (ADR-014). Primeira nota da vida: copy estendida "Guardado. O tempo cuida
   do resto." (ADR-044 — estado persistido via `service` settings não existe
   ainda; usar campo `returnConsentState == NOT_CONFIGURED` como proxy da
   primeira experiência).
5. Arquivo no novo sistema: tipográfico, agrupado por mês ("Março de 2026"
   em Inter 600), distância temporal secundária ("há 4 meses"), linhas sem
   cards, divider `outlineVariant`, entrada abre a leitura da nota (nova
   tela de leitura com FioDisplayDate), editar/excluir preservados com
   mesma confirmação.
6. Estados de interação: pressed -8% escala 0,98 120ms; focus ring 2dp;
   erro terracota com ícone+texto.
7. Testes: manter 20 unit tests verdes + BUILD; nova tela de leitura
   coberta por preview do Compose (sem instrumentados no sandbox).

## Riscos
- O seletor temporal "especificar período/data" cria política que o engine
  atual do main (V0, sem M2) não agenda — comportamento correto: a escolha
  fica registrada na entrada e o engine do PR #1 (time-only) a honra como
  candidato. Documentar no plano: UI grava, engine decide.
- Conflito futuro com PR #1 (codex reescreve FioApp.kt) — aceito; a decisão
  de coordenação (A/B/C) foi enviada ao fundador. Rebase A preserva tudo.

## Next
Fase 2.3: pill discreta + estados + menu ⋯ refinado (parcialmente coberto
aqui); depois 2.4 (Arquivo agrupado — já feito nesta fatia) e 2.5 (nota/
devolução).
