# 2026-08-16 — Coordenação do pacote de design com o trabalho M2/M3/M4 do PR #1

DECISION REQUIRED

## Contexto

O repositório contém hoje dois corpos de trabalho abertos sobre `main` (que tem apenas o commit Genesis `a4fc835`):

1. **PR #3** (`feature/design-ux-v1`, commit `0dbc274`): o pacote de design — auditoria UX, arquitetura de informação, design system Verde-Sálvia, fluxos/microinterações, plano de implementação e 16 artefatos visuais em `docs/design/`. Nenhum código de aplicação é alterado.
2. **PR #1** (`codex/v0-time-only-checkpoint`, commit `982ad53`, ~100 arquivos): o checkpoint de engenharia M2/M3/M4 — engine temporal determinístico (`TimeReturnEngine`) com testes, Room schemas 2–3, agendamento local de devoluções, importação local com rollback atômico, benchmark semântico PT-BR isolado (early-stop: sem modelo semântico no V0, ADR-041), e reescrita da UI (`FioApp.kt`/`FioViewModel.kt`) que **não aplica** o novo design — a bottom bar de 3 abas e os demais achados da auditoria UX continuam presentes.

A FASE 2 do plano aprovado (organização visual: design system, Home, confirmação sutil, menu, Arquivo, nota, devolução, configurações) precisa ser aplicada sobre a UI que estará em `main`. Se o PR #1 for mergeado primeiro, qualquer aplicação da UI nova sobre o branch atual conflitará fortemente com `FioApp.kt`, `FioViewModel.kt`, `Models.kt`, `Entities.kt`, `Dao` e `FioDatabase` reescritos.

## Opções

**Opção A — Rebase do design sobre o checkpoint codex (recomendada).** Criar `feature/design-ui-v2` partindo de `codex/v0-time-only-checkpoint` e aplicar o design system e as telas novas sobre a UI reescrita. Preserva ambos os trabalhos sem duplicação e sem reescrever histórico. O pacote `docs/design/` não conflita (o PR #1 não toca em `docs/design/`).

**Opção B — Aguardar merge do PR #1 e então aplicar.** Nenhuma ação agora sobre a FASE 2; quando o PR #1 for mergeado em `main`, o trabalho de design continua sobre `main`. Custo: paralisa a FASE 2 até o merge humano.

**Opção C — Aplicar design sobre o branch atual (a4fc835).** Rápido, mas gera um conflito inevitável de centenas de linhas com o PR #1 quando ambos forem integrados — duplica o esforço exato da FASE 2, que é o trabalho de UI mais pesado do plano.

## Recomendação

Opção A. O PR #1 é validação de engenharia; a UI nova precisa existir sobre ela de qualquer forma. O rebase preserva os dois trabalhos e mantém os commits do checkpoint intactos (sem force push, sem reescrita de história).

## Riscos

| Opção | Risco |
|---|---|
| A | Assume que o PR #1 é funcionalmente aceito (tests verdes do checkpoint); revisão humana do merge continua sendo do fundador |
| B | Bloqueia a FASE 2; nenhuma perda técnica |
| C | Conflito inevitável e retrabalho da FASE 2 |

## Menor decisão necessária

Autorizar: **(A)** criar `feature/design-ui-v2` rebasado sobre o checkpoint codex agora, ou **(B)** aguardar merge do PR #1 antes de tocar em UI, ou **(C)** aplicar sobre o estado atual e absorver o conflito depois.
