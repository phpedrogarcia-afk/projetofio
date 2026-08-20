# MANUS-PRE-CODEX-FINAL — Relatório final da Missão Manus 2

> **Correção factual de 2026-08-20:** `ReturnPolicy` não é apenas uma dívida leve de arquitetura. Períodos, data e `Nunca` ao guardar são exibidos, mas não persistidos nem honrados. O estado atual é P0 de confiança; ver `packets/FIO-P19-TEMPORAL-INTEGRITY-DECISION.md`.

**Missão:** Product Red Team, Pilot Readiness & Codex Handoff (MANUS MISSION 2)
**Branch:** `integration/manus-pre-codex-20260817` · **Checkpoint final:** `c688cfb` · **Data:** 2026-08-17
**Método:** red team em modo somente-leitura + correções triviais permitidas pela autonomia da missão; nenhuma feature criada; `main` intacta.

## 1. Resumo executivo

A Missão 2 existiu para responder três perguntas que a Missão 1 (hardening) deliberadamente deixou em aberto: o produto é coerente consigo mesmo? O piloto pode ser preparado sem ser iniciado? E o que o próximo agente (Codex) precisa saber para retomar sem perder contexto? A resposta consolidada é: **o produto está mais coerente do que o próprio time de engenharia suspeitava — dos 12 princípios canônicos, 11 passam direto e o único WEAK foi corrigido durante a missão** —, o pacote de piloto está pronto e aguarda apenas autorização humana, e o repositório agora contém uma trilha de documentos de handoff (`plans/PROJECT-STATE.md`, `AGENTS/PROJECT-STATE.md`, `plans/CODEX-HANDOFF.md`, `plans/NEXT-WORK.md`, `plans/IDEA-INBOX.md`, `plans/BOOT-PROMPT.md`) que permite a retomada por qualquer agente sem perda de contexto.

A integridade da Missão 1 foi preservada: a baseline de 99 testes verdes foi verificada no início, as duas correções triviais desta missão passaram pela suíte completa, e a branch carrega a Missão 1 como base limpa.

## 2. Red Team de produto — matriz dos 12 princípios

O red team percorreu linha a linha todo o código de UI, strings, engine, notificações, import e export, confrontando-o com `docs/02-PRINCIPLES.md`, `docs/03-UX.md` e os ADRs 043–047.

| # | Princípio | Veredito | Evidência |
|---|-----------|----------|-----------|
| 1 | As palavras do usuário são o produto | **PASS** | Zero manipulação de texto: sem reescrita, sumarização, labels ou correção automática; `isBlank()` é o único filtro e é sobre vazio |
| 2 | Seleção sem interpretação | **PASS** | `TimeReturnEngine.chooseAllowedDelivery` seleciona por idade e janela, nunca por conteúdo; nenhum `sentiment`/`meaning`/`insight`/`padrão`/`evolu` em todo o código |
| 3 | O tempo é a feature | **PASS** | Engine com bootstrap, cap, quiet-hours, `OnDate`/`Never`; picker ADR-043 com 7 rótulos mapeados ao vocabulário canônico; `EngineTortureTest` com 10.000 avaliações horárias e 0 violações |
| 4 | Silêncio sobre engajamento | **PASS** | Sem streaks, badges, DAU, "você escreveu X dias"; pill "Guardado." some em 1.5s; devolução nunca re-notifica após ignorada |
| 5 | Autonomia explícita | **PASS** | Pause/Nunca/Descansar/Excluir presentes no time picker; consent `NOT_CONFIGURED→ENABLED` só com ação do usuário |
| 6 | Privacidade por arquitetura | **PASS** | Confirmed pelo CRYPTO-REVIEW-PACKET (Missão 1): zero plaintext no log/export/analytics; Keystore AES-256-GCM; fronteira local-first inalterada |
| 7 | Local-first | **PASS** | Sem rede; sem sync; sem SDK externo que envie dados; export local com checksum |
| 8 | Beleza que não compete | **PASS** | Verde-Sálvia aplicado aos tokens; sem animação de recompensa; pátina é ornamento, não feedback de mérito |
| 9 | A cápsula é um rito | **PASS** | Primeira nota com copy única "Guardado. O tempo cuida do resto."; sem pressão de primeira escrita |
| 10 | Simplicidade que respeita | **PASS** | Home sem bottom bar, menu ⋯; editor direto; arquivo tipográfico com distância temporal |
| 11 | Partida sem custo | **PASS** | Export ADR-046 completo e verificado byte a byte; soft-delete com purgeAfter; zero vendor lock-in |
| 12 | Aprendizado sem observação | **PASS** | Sem telemetria alguma hoje; piloto preparado com matriz `pilot/ANALYTICS-PRIVACY-MATRIX.md` (10 eventos sem texto, transporte local) |

**WEAK corrigido durante a missão:** o princípio 8 (e o gate humano de TalkBack) tinha um ponto fraco — a Pátina Temporal (motivo botânico animado) ignorava `Reduce Motion`. Corrigido: `BotanicalMotif` consulta `AccessibilityManager` e não desenha nada para quem pediu menos movimento, conforme ADR-045.

**Caças com resultado negativo (confirmam a saúde):** gamificação acidental — zero strings de streak/ponto/badge/evolução em todo o código; IA interpretativa acidental — zero uso de modelo, zero label semântico, zero similaridade; produtividade acidental — zero tarefa/calendário/lembrete disfarçado; IA acidental na UX — zero copy interpretativa (exceto 1 frase no retorno imediato, corrigida para copy neutra: "Reescrever esta nota agora?").

## 3. Correções triviais aplicadas (autonomia da missão)

Duas correções de aquisibilidade clara, ambas com suíte verde pós-aplicação:

1. **Reduce Motion na Pátina Temporal** — detalhado na seção 2.
2. **Copy neutra no retorno imediato** — "Reescrita com o olhar de hoje?" → "Reescrever esta nota agora?", alinhada ao `contentDescription` neutro já existente e ao princípio 2.

O retry no pill de erro foi deliberadamente **não** implementado: exigiria estado persistente no ViewModel e um contrato novo (quando a falha some do pill, ela permanece acessível via bloco full-width e menu). Registrado no `IDEA-INBOX.md` como item para observar no piloto.

## 4. Documentation drift encontrado e tratado

O README carrega a narrativa M1/M4 do fundador e **não foi reescrito** (apagar história é proibido pela missão; a atualização ficou redigida como recomendação pronta para o fundador, com texto em `DOCUMENTATION-DRIFT-AND-PR-CLEANUP.md`). O `MANUS-EXECUTION-QUEUE.md` dizia uma NOW que já não é verdade (schema 4 com campo `returnPolicy` em Entry — o design v1 resolve ADR-043 sem novo campo de schema) e foi atualizado para o estado real. O AGENTS.md recebeu o bloco `Current implementation state` com as duas missões Manus. A matriz completa dos 7 drifts está em `plans/DOCUMENTATION-DRIFT-AND-PR-CLEANUP.md`.

## 5. PR Cleanup Plan (recomendação, sem alteração no GitHub)

O PR #3 começou como "design-only package" e hoje carrega 50 arquivos com código (FioApp, ViewModel, temas). Recomendação redigida: atualizar título/corpo para o estado verdadeiro com nota de evolução preservando a história original; não dividir o PR (reescrever histórico é risco maior que descrição imperfeita); PR #1 entra primeiro; branches Manus entram como um PR único acumulando as duas missões, decisão do fundador. Nenhuma alteração foi feita via GitHub nesta missão — textos prontos para o fundador aplicar.

## 6. Pacote de piloto (preparado, não iniciado)

`pilot/` contém quatro documentos: `PILOT-PROTOCOL.md` (hipótese central, 2 coortes, 8 semanas mínimo, MUR como métrica principal, anti-métricas proibidas, critério de aborto P0/P1), `ANALYTICS-PRIVACY-MATRIX.md` (10 eventos operacionais sem texto, transporte 100% local, analytics remoto marcado DECISION REQUIRED), `INTERVIEW-GUIDE.md` (4 blocos sem perguntar conteúdo) e `CONSENT-DRAFT.md` (marcado HUMAN/LEGAL REVIEW REQUIRED — não é aconselhamento jurídico). O piloto existe como preparação: autorizar, recrutar e distribuir APK são gates humanos do fundador.

## 7. Handoff Codex

Os documentos de retomada estão em: `plans/PROJECT-STATE.md` (snapshot vivo), `AGENTS/PROJECT-STATE.md` (estado canônico), `plans/CODEX-HANDOFF.md` (ponte de contexto), `plans/NEXT-WORK.md` (agora=1, then=3), `plans/IDEA-INBOX.md` (9 ideias, nenhuma implementada) e `plans/BOOT-PROMPT.md` (prompt pronto para colar em nova sessão). O AGENTS.md já aponta para o estado atualizado.

## 8. Decisões abertas para o fundador (nenhuma bloqueia código)

| # | Decisão | Natureza |
|---|---------|----------|
| 1 | Merge do PR #1 e veículo das branches Manus | Estratégia de branches |
| 2 | Atualizar título/corpo do PR #3 (texto pronto em `DOCUMENTATION-DRIFT-AND-PR-CLEANUP.md` §2) | Higiene |
| 3 | Executar gates humanos: TalkBack real (~10 min), large font, dark mode, `M2AndroidContractTest` em AVD/API 36 | Evidência física |
| 4 | `ReturnPolicy` UI-only: escolhas não aplicadas (P0; FIO-P19) | Produto + arquitetura |
| 5 | Conteúdo só-ZW aceito por `isBlank()` — aceitar ou rejeitar | Produto |
| 6 | Analytics remoto do piloto — DECISION REQUIRED (quebraria fronteira local-first) | Privacidade |
| 7 | Consentimento legal (`pilot/CONSENT-DRAFT.md`) | Jurídico |
| 8 | Atualizar README com as missões Manus (texto preparado sob pedido) | Documentação |

## 9. O que esta missão não fez (por desenho)

Não criou features, não mudou dependências, não apagou históricos, não alterou PRs no GitHub, não iniciou o piloto, não reescreveu o README, não executou testes instrumentados (sem AVD no ambiente). Todas as quatro paradas obrigatórias do contrato foram desnecessárias — a única mudança de código foi endurecimento e acessibilidade triviais.
