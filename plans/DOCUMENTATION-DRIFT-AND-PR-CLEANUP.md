# Documentation Drift Audit & PR Cleanup Plan (Missão 2)

> **Correção factual de 2026-08-20:** a análise histórica abaixo afirmou incorretamente que `ReturnPolicy` era persistida. Ela existe somente no estado da tela; períodos, data e `Nunca` ao guardar não chegam ao domínio. A fonte atual é `docs/atlas/TIME-MAP.md`; a decisão de correção está no FIO-P19.

**Branch:** `integration/manus-pre-codex-20260817` · **Método:** comparação sistemática dos documentos canônicos contra o estado real do código (`integration/manus-rehearsal-20260817`, HEAD `e0a7b7e`) e do repositório remoto (PRs, issues, README). Nenhum histórico foi reescrito; onde o drift foi corrigido, foi por atualização de estado, nunca por apagamento do passado.

## 1. Achados de drift documentário

O README é o principal vetor de drift: foi escrito na era M1/M4 e descreve um repositório anterior às duas missões Manus. Os achados, verificados contra o código e o git:

| # | Afirmação obsoleta | Onde | Estado real | Severidade |
|---|--------------------|------|-------------|------------|
| 1 | "a suíte integrada passa 51 JVM e 25 instrumented tests" | README § Current state | **99 testes unitários verdes** após a Missão 1 (51 + 48 novas suítes de tortura); instrumentados continuam inexecutáveis neste sandbox, mas a contagem declarada de JVM está desatualizada | Alta (número concreto errado) |
| 2 | "Current next action: owner functional observation… do not infer release readiness" | README § top | Duas missões de hardening e prontidão completas; o estado verdade é o handoff desta missão; a instrução de "não inferir" ainda é válida para release, mas o contexto M4/POCO dominava o bloco inteiro | Alta (direção desatualizada) |
| 3 | Bloco M4 completo (MiniLM, POCO M3 Pro, ADB-authorized, 14 M4 tests, corpus/rubric v1) | README § 3 parágrafos | M4-R1/R2 são passado aceito e correto (ADR-040/041), mas o README não menciona as duas missões Manus, o branch de integração, nem a prontidão para piloto — o leitor chega a uma verdade parcial | Média |
| 4 | "MANUS-EXECUTION-QUEUE.md: NOW = integrar `returnPolicy` ao schema 4" | plans/ | **Correção 2026-08-20:** a integração ficou UI-only de verdade; `InPeriod`/`OnDate`/`Never` não são persistidos em `originalTimeZone` nem em outro campo. A dívida de schema/engine continua real e está no FIO-P19. | Alta (promessa de produto falsa) |
| 5 | MANUS-EXECUTION-QUEUE § DONE lista "20 unit tests verdes" | plans/ | Contagem de 16/08: hoje são 99. As fases 2.x estão obsoletas pois o design v1 foi integrado e endurecido pela Missão 1 | Média |
| 6 | PR #3 descreve "Nenhum código de aplicação foi alterado" | GitHub PR #3 | Hoje o PR #3 contém 50 arquivos, incluindo `FioApp.kt`, `FioService.kt`, `FioViewModel.kt`, temas e ícones — o pacote começou como design-only e absorveu a implementação do design v1 | Alta (descrição do PR desatualizada) |
| 7 | ADR ledger não registra as duas missões Manus | docs/DECISIONS.md | Os relatórios `MANUS-HARDENING-FINAL.md` e (este ciclo) `MANUS-PRE-CODEX-FINAL.md` cobrem o estado, mas o ledger de decisões não aponta para eles; não é drift de fato, é ausência de referência cruzada | Baixa |

### Drift corrigido nesta missão (autonomia permitida: documentação desatualizada)

O `MANUS-EXECUTION-QUEUE.md` foi atualizado para refletir o estado real: o NOW atual é o handoff desta missão (não mais schema 4), e a seção DONE registra as duas missões Manus. O README **não** foi reescrito nesta missão — ele carrega a narrativa M1/M4 do fundador, e reescrevê-lo sem o fundador seria apagar história (regra "nunca fingir que história passada não existiu"). A atualização do README ficará como recomendação explícita no handoff, com texto preparado no relatório final.

## 2. PR Cleanup Plan (recomendação, sem merge)

O repositório tem hoje dois PRs e uma branch de trabalho. A avaliação de responsabilidade:

| PR / branch | Responsabilidade atual | Diagnóstico |
|-------------|------------------------|-------------|
| PR #1 `codex/v0-time-only-checkpoint` (DRAFT) | M1 baseline + M2/M3/M4 checkpoints, 100 arquivos | OK. É o checkpoint de engenharia original; não crescerá mais |
| PR #3 `feature/design-ux-v1` (OPEN) | Título: "design reorganization package (docs/design)" — na origem era design-only; hoje carrega 50 arquivos com a implementação real do design Verde-Sálvia | **Descrição desatualizada.** O PR cresceu legitimamente (o design virou código), mas o título e o corpo ainda afirmam "nenhum código alterado" |
| `integration/manus-rehearsal-20260817` | 17 campanhas de hardening, 48 testes, 1 finding P0 corrigido | Nova; não é PR ainda |
| `integration/manus-pre-codex-20260817` (esta) | Red team, UX trivial, drift, piloto, handoff | Nova; não é PR ainda |

**Recomendação de ordem e estratégia (menor risco):**

1. **PR #1 entra primeiro.** É a fundação: schema 3, engine M2–M4, imports. Nada depois dele depende de nada que não esteja nele. É DRAFT e só o fundador pode convertê-lo/mergê-lo — manter como está, sem tocar.
2. **PR #3 entra segundo, sem dividir.** Dividir em "docs-only" + "código" agora exigiria reescrever histórico (proibido pela regra de menor risco). Em vez disso: **atualizar o título e o corpo do PR #3** para descrever o estado verdadeiro ("Design v1 Verde-Sálvia implementado + pacote de design; código alterado em FioApp/ViewModel/temas"), mantendo a seção original com nota de evolução ("este PR começou como design-only em 16/08 e incorporou a implementação aprovada"). Isso preserva a história e destrava a revisão.
3. **Branches Manus não viram PR agora.** O fundador deve decidir o veículo de entrada (PR único de integração vs. dois PRs espelhando as missões). Recomendação escrita no relatório final: um PR único "manus-hardening+pre-codex" com diff acumulada, pois os dois ciclos compartilham a mesma branch base e nenhum conflito existe entre eles.
4. **Issue #2** ("Reorganização UX v1") corresponde ao PR #3 — fechar ou ligar ao PR atualizado quando o PR entrar, não antes.

Nenhuma descrição foi alterada via GitHub nesta missão (alteração de PR público é gate do fundador; a atualização proposta está redigida pronta no `PR-CLEANUP-PLAN.md` anexo abaixo — na verdade incorporada a este documento; o executor aplica quando autorizado).

## 3. Critério de coerência documental (a pergunta do §28 da Missão 2)

"PRs, issues, ADRs e código dizem aproximadamente a mesma verdade?" — **Quase.** Os ADRs 043–047 estão coerentes com o código (verificado linha a linha no Red Team). O README não diz: faltam as duas missões Manus e os 99 testes. O MANUS-EXECUTION-QUEUE dizia o que não é mais verdade: corrigido. O PR #3 diz o que já não é: redigida a atualização.
