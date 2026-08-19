# CODEX-EXECUTION-PROTOCOL — como o Codex executa trabalho sem corromper o produto

## 1. Pré-condições (todas, antes de qualquer arquivo)

1. Estar em branch de integração ou research (`integration/*`, `research/*`). **Nunca em `main`.** `main` == `origin/main` == snapshot de referência; merge só pelo fundador.
2. Ler `AGENTS.md` integralmente (é o contrato de conduta).
3. Ler `docs/atlas/INVARIANTS.md` (o que é intocável).
4. Ler o packet em fila em `packets/EXECUTION-QUEUE.md` — trabalho é sempre via packet, nunca ad-hoc.
5. Confirmar os testes atuais passam: `cd app && ./gradlew :mobile:testDebugUnitTest --no-daemon` (134 hoje).
6. Se o packet pede aparelho/AVD e não há um, declarar o gap em vez de fingir evidência.

## 2. Ciclo de trabalho por packet

1. **Entender:** mapa (docs/atlas/), depois código, depois packet. Nunca o contrário.
2. **Decidir:** se o packet implica mudança de princípio/privacidade/produto → parar e propor ADR. Não decidir sozinho.
3. **Isolar:** branch nova derivada do HEAD da branch de integração; nome descritivo com data.
4. **Implementar:** código mínimo + testes de contrato (não só happy path; incluir adversarial onde o packet manda — ver `plans/DATA-TORTURE-MATRIX.md`).
5. **Verificar:** testes verdes; sem `up-to-date` como evidência; instrumentados só com execução real reportada.
6. **Documentar:** atualizar o packet (o que mudou, evidência), SEARCH-EXECUTION-QUEUE ou equivalente, e PROJECT-STATE se o estado mudou.
7. **Entregar:** PR descritivo; nunca mergear; registrar no ledger se virou ADR.

## 3. Regras duras (violação = trabalho descartado)

| Regra | Por quê |
|---|---|
| Não adicionar dependência de rede que toque conteúdo privado | Princípio local-first (AGENTS) |
| Não persistir queries/tokens/scores de busca | Invariante I-14 |
| Não permitir sealed content via nenhum caminho (busca, export, log, stacktrace) | Threat model |
| Não conectar semântica a Returns ou Room mutável | ADR-040 |
| Não reportar gate humano/avaliação como aprovado sem evidência | Integridade |
| Não executar código iOS; não reabrir M4 sem ADR novo | Decisões aceitas |
| Não escrever texto gerado como resposta de busca/devolução | Retrieve-not-interpret |
| Schema migration: exportSchema=true + migration manual + teste byte a byte | Processo Room |
| Feature novo e instável = flag `*_ENGINEERING_ENABLED` em `BuildConfig` | Padrão removível |
| Draft: cifra antes do insert; falha nunca apaga draft | D12 + ADR-029 |

## 4. Quando parar e escalar ao fundador

Mudança de princípio; qualquer analytics remoto; sync/backup cloud; semântica em produção; schema 4; monetização; mudanças de notificação (mais de um canal/corpo); "On This Day" ou resurfacing novo; import de formatos novos grandes; remoção de testes/proteções. Nestes casos: propor ADR, registrar em DECISION-INDEX §2, esperar decisão.

## 5. Evidência: o que conta

Conta: output de `gradlew` real, screenshots com timestamp, XML de testes, checksums. Não conta: "deveria funcionar", testes com `@Ignore`, runs `up-to-date`, afirmações sem output.

## 6. Fresh-agent check (auto-verificação)

Ao terminar cada packet, responder por escrito: (a) qual invariante poderia ter sido violado e por que não foi; (b) qual doc ficou desatualizado e o que fazer; (c) o que um agente novo precisaria saber que não está em nenhum arquivo (→ corrigir o Atlas).
