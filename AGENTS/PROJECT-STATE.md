# PROJECT-STATE — estado vivo do repositório (2026-08-17)

O Codex deve ler este arquivo antes de qualquer tarefa. É o snapshot autorizado do estado atual; detalhes completos estão nos relatórios das missões (`MANUS-HARDENING-FINAL.md`, `MANUS-PRE-CODEX-FINAL.md`) e em `plans/PROJECT-STATE.md`.

## Verdades que o Codex deve aceitar como base

O produto é o Fio: diário autobiográfico local-first cuja única feature diferenciante é a devolução espontânea de palavras antigas. A especificação canônica é v0.1 em `docs/`. A plataforma inicial é Android (minSdk 26, ID `com.projetofio.app`), Kotlin/Compose/Room/KSP, local-first com AES-256-GCM via Keystore. O engine temporal (M2/M3/M4) está implementado e endurecido: bootstrap de maturidade, cap diário, quiet-hours, fusos extremos, DST e Y2038 cobertos por `EngineTortureTest.kt` (18 testes). O design Verde-Sálvia v1 está implementado na UI (Home, editor zero-fricção, picker temporal ADR-043, arquivo tipográfico, pátina temporal, pill "Guardado."). A migração Room 2→3 foi verificada byte a byte; **não existe schema 4**. O caminho semântico M4 foi encerrado sem modelo selecionado (ADR-041) e **não pode ser reconectado** sem nova decisão.

## Números verificados

99 testes unitários verdes (51 → 99 durante a Missão Manus 1). 1 finding P0 descoberto e corrigido na Missão 1 (substituição silenciosa de surrogate halves antes da criptografia). 0 testes instrumentados executados no intervalo Manus — os gates `M2AndroidContractTest` e TalkBack real continuam `BLOCKED — HUMAN/DEVICE VALIDATION REQUIRED`.

## Branches e PRs

`main` está intacta e não deve ser tocada pelo Codex. O PR #1 (`codex/v0-time-only-checkpoint`, DRAFT) contém a fundação de engenharia e aguarda merge do fundador. O PR #3 (`feature/design-ux-v1`, OPEN) implementa o design v1, mas seu título e corpo afirmam "nenhum código alterado" quando hoje são 50 arquivos com código — a atualização está redigida em `plans/DOCUMENTATION-DRIFT-AND-PR-CLEANUP.md` §2 e aguarda aplicação pelo fundador. As branches `integration/manus-rehearsal-20260817` e `integration/manus-pre-codex-20260817` contêm as duas missões Manus; a próxima recomenda o veículo de entrada (um PR acumulando ambas) mas a decisão é do fundador.

## O que o Codex não deve fazer

Não mergear em `main`; não reescrever o README sem o fundador; não reformatar o repo inteiro para ktlint/detekt; não iniciar schema 4; não iniciar o piloto (o pacote `pilot/` é preparação, não autorização); não reportar gates humanos como aprovados; não adicionar nenhuma dependência, métrica de engajamento, interpretador de texto ou SDK externo (lista completa em `AGENTS.md` § Never add).

## Próximos passos

Ver `plans/NEXT-WORK.md` — o próximo item autorizada é a execução dos gates humanos pelo fundador; nada de código novo começa antes de um gate ser fechado com evidência. Depois: merge do PR #1 (fundador), atualização do PR #3 (fundador), e as 3 decisões abertas (ReturnPolicy schema 4, conteúdo só-ZW, analytics remoto do piloto).
