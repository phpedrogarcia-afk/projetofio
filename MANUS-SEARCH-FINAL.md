# MANUS-SEARCH-FINAL — Missão 4: a busca do Fio (Encontrar)

**Missão:** Manus Mission 4 — Intelligent Search & Autobiographical Memory Retrieval
**Branch:** `integration/manus-search-20260819` · **Base:** `integration/manus-ux-refinement-20260819` · **Data:** 2026-08-19
**Autor:** Manus AI · **Commit final:** `1a09699` (HEAD) · `main` intocado (`a4fc835`, nunca mergeado)

## 1. Resumo executivo

A Missão 4 construiu a primeira arquitetura de busca do Fio, seguindo o princípio canônico: **a busca pode encontrar, ordenar e filtrar as palavras do usuário — ela não responde perguntas autobiográficas no lugar dele.** O resultado é uma busca lexical de produção (tokenização PT-BR, snippets do texto original, filtros de tempo e de "já voltou") integrada à tela de Arquivo, junto com um protótipo semântico híbrido (RRF k=60) totalmente isolado, atrás de boundary explícito, removível sem tocar o código de produção.

Números-chave: **134 testes verdes, 0 falhas** (29 novos nesta missão), D12 fechado, 7 commits no branch, red team final PASS. O benchmark PT-BR sintético confirma o teto do léxico puro (MRR 0,200) e o potencial da semântica (MRR 1,000), mas a latência CPU fria do proxy (~2,6 s) viola o teto de 500 ms — a decisão de embarcar semântica fica **deferred** com um kill criterion quantitativo documentado.

## 2. O que esta missão fez (e por quê)

| # | Entrega | Commit | Estado |
|---|---|---|---|
| S0 | Boundary canônico (AGENTS.md) + `docs/search/SEARCH-ARCHITECTURE.md` + fila de execução | `7669d0a` | Done |
| S1 | Auditoria do modelo de retornos; fechamento de D12 (3 contratos) + `loadReturnsForEntry` read-only | `a2ad34f` | Done |
| S2 | Threat model de notas seladas + privacy architecture (Opção A, FTS5 rejeitada) | `e77de5e` | Done |
| S3 | Baseline lexical: `LexicalTokenizer` + `LocalSearchService` + `SearchRepository` (12 testes) | `0bd694d` | Done |
| S4 | UI Encontrar no Arquivo (debounce 300 ms, snippets, "Já voltou", notas neutras) | `44ce800` | Done |
| S5 | Benchmark PT-BR (60 entradas, 30 consultas) + RESEARCH-LOG (EmbeddingGemma-300M) | `87aa73c` | Done |
| S6 | Prototype híbrido (RRF, `EmbeddingProvider`, `HybridSearchService`) — removível (9 testes) | `8f2eb38` | Done |
| S7 | Acessibilidade (TalkBack factual) + red team final (PASS) | `a6d8215` / `3e0e27d` / `1a09699` | Done |

## 3. Definições canônicas: Guardar / Encontrar / Reencontrar

O Fio tem três capacidades distintas, que esta missão registra como invariantes de produto:

| Capacidade | Quem inicia | O que é |
|---|---|---|
| **Guardar** | Usuário | Cria um registro autobiográfico |
| **Encontrar** | Usuário | Procura conscientemente palavras do próprio passado |
| **Reencontrar** | Fio | Devolve algo sem que a pessoa tenha procurado naquele momento |

A busca pertence a **Encontrar**; as devoluções pertencem a **Reencontrar**. A infraestrutura matemática pode eventualmente ser compartilhada — **a política de produto nunca**. O Return Engine não usa a busca como fonte de seleção, e a busca não produz devoluções.

## 4. O princípio fundamental (comportamental, não retórico)

> "A busca pode encontrar, ordenar e filtrar as palavras do usuário. Ela não responde perguntas autobiográficas no lugar dele."

O Fio **recupera evidência; não produz conclusão**. Permitido: a query "quando comecei a pensar em sair daquele emprego?" retorna a data e o trecho original. Proibido: a busca responder "Você começou a querer sair do emprego em março de 2024." Nenhuma interface desta missão gera texto, resume, interpreta ou emociona — apenas datas originais, snippets de texto original (preserváveis com `SelectionContainer`) e contagens factuais ("Já voltou N vez(es)").

## 5. D12 fechado: o rascunho sobrevive a uma inserção falha

A dívida de design D12 ("um rascunho em andamento deve sobreviver a uma falha de inserção de entrada") foi fechada com `DraftSurvivesFailedInsertTest` (3 contratos): o estado exato do editor substitui o draft antigo antes da transação; uma inserção que falha não apaga o rascunho; e o rascunho permanece restaurável após a falha. Em paralelo, a projeção read-only `loadReturnsForEntry` foi adicionada a `ReturnRepository`, `FioDao` e `RoomFioRepository` **sem migração de schema** — a busca enxerga o histórico de reencontros sem tocar o Write Path. Auditoria completa em `research/search/RETURN-HISTORY-AUDIT.md`.

## 6. Modelo de retornos: auditado, nada a migrar

A auditoria de S1 verificou que o schema atual (schema 3, `ReturnEntity` com `state`, `opened_at`, `dismissed_at`, `expired_at`) já cobre a necessidade da busca: o fato "já voltou N vezes" é computável sobre os ciclos OPENED existentes, por Entry, sem duplicar texto e sem criar estrutura nova. O histórico de reencontros é factual (datas e estados), não interpretativo — cada devolução é um evento, o texto pertence à Entry.

## 7. Notas seladas: threat model antes de qualquer implementação

`docs/search/SEARCH-SEALED-THREAT-MODEL.md` responde às 6 perguntas obrigatórias do threat model (snippet pode vazar texto? índice lexical pode vazar palavras? embedding pode revelar conteúdo? a busca pode revelar existência/data? autenticação desbloqueia só a nota ou uma sessão?). Conclusão operacionalizada no código: **seladas são invisíveis por conteúdo em ambos os braços da busca** (léxico e vetorial); uma contagem opaca opcional ("N nota(s) selada(s) não pesquisável(is)") existe apenas sob o comportamento `COUNT_ONLY`, com texto neutro que nunca expõe nada. A questão "contagem visível ou invisibilidade total" é **DECISION REQUIRED** do fundador — o threat model recomenda o padrão mais restritivo (invisibilidade total), com toggle explícito caso se queira a contagem opaca.

## 8. Privacidade da busca: o texto plano nunca sai da fronteira de crypto

`docs/search/SEARCH-PRIVACY-ARCHITECTURE.md` mapeia onde o plaintext existe e por quanto tempo. Decisão V1: **Opção A (scan sob demanda)** — cada busca descriptografa, varre e descarta o plaintext em memória, na sessão. FTS5 foi rejeitada para V1 (índice persistente de plaintext exigiria um segundo boundary de crypto, complexidade não justificada ainda). Contratos operacionais cumpridos no código: exclusão permanente remove tudo (índice, embeddings, histórico de retornos); edição é sempre refletida (scan-on-demand garante isso por construção); o boundary crypto (Keystore AES-256-GCM, contentEnvelope BLOB) permanece intocado — se a busca exigir enfraecimento, a missão para. Queries, tokens e scores **nunca são persistidos, logados ou enviados** (verificado por grep no red team final e pelo contrato explícito em `FioViewModel`).

## 9. Baseline lexical (produção, S3)

O baseline lexical é modesto de propósito — **"mensurável e excelente antes de qualquer coisa semântica"** (SEARCH-ARCHITECTURE, §14). Componentes:

- **`LexicalTokenizer`**: normalização NFD + remoção de `\p{Mn}` (diacríticos de português caem: "café" ↔ "cafe"), match de tokens completos em ordem, prefixo **apenas no último token** (suporta digitação em andamento), snippets cortados do **texto original** (nunca normalizado).
- **`LocalSearchService`**: scan sob demanda (Opção A), seladas com `COUNT_ONLY` default, soft-deleted invisíveis, histórico OPENED como evidência de "já voltou", filtros de tempo e de retorno aplicados sobre `originalCreatedAt` e ciclo OPENED.
- **`SearchRepository`**: interface read-only (`loadSearchableEntries`, `loadReturnHistory`) implementada por `RoomFioRepository`/`FioDao` com queries SELECT puras.

Estados de entrada na busca (contrato de política): Never/Resting/Scheduled/Already returned aparecem; Deleted só na área de apagados; Permanentemente excluídas nunca.

## 10. UI Encontrar (S4) — integrada ao Arquivo, sem drama

O campo de busca aparece inline na lista do Arquivo (`ArchiveSearchField`), com debounce de 300 ms, queries puramente runtime (a UI não conhece nem grava a query). Resultados inline: contador "N resultado(s) para '…'", snippets com seleção preservável, data original, badge factual "Já voltou N vez(es)" e nota neutra de seladas quando houver. Sem "Pesquisas recentes" (ausência confirmada no red team). Injeção: `FioGraph.search` (Lazy) → `MainActivity` → `FioViewModel.Factory` — o serviço lexical é o único injetado em produção; a UI não conhece o modo híbrido.

## 11. Benchmark PT-BR (S5) — o teto do léxico e o preço da semântica

Corpus sintético de 60 entradas autobiográficas e 30 consultas com ground truth (`research/search/FIO-SEARCH-BENCHMARK.md` + `run_benchmark.py`, replicando o algoritmo exato do `LexicalTokenizer` Kotlin). Fora do build do app, puro artefato de pesquisa:

| Configuração | Recall@5 | Recall@10 | Precisão@5 | MRR | Latência p95 |
|---|---|---|---|---|---|
| `lexical_v1` (produção) | 0,098 | 0,109 | 0,060 | **0,200** | **<1 ms** |
| `semantic` (proxy multilingual-e5-large) | 0,797 | 0,917 | 0,573 | **1,000** | ~2,6 s |
| `hybrid` (RRF k=60) | 0,797 | 0,917 | 0,573 | **1,000** | ~2,6 s |

Leitura honesta: 24 das 30 consultas exigem semântica para recall completo — o léxico tem um teto real em consultas parafrásticas. A semântica recupera quase tudo, mas a latência fria em CPU viola o teto de 500 ms. O benchmark valida em ambos os sentidos: **há ganho real a explorar, e há um custo real a vencer.**

## 12. Pesquisa de embeddings on-device (S5) — `research/search/RESEARCH-LOG.md`

Evidência dominante: **MTEB-BR** (22 tarefas PT-BR, 93 modelos, jul/2026) — o rank multilíngue só prediz moderadamente o rank PT-BR (ρ=0,75), logo a escolha deve se apoiar em evidência nativa. Candidato primário: **`embeddinggemma-300m`** (308M, TFLite oficial via LiteRT, Matryoshka 768/512/256/128, 2K ctx, Google open license), 13º de 93 em PT-BR e melhor on-device do painel. Descartados: gte-small TFLite (fim do painel em PT-BR), multilingual-e5-large/bge-m3 (sem TFLite oficial, >500MB), nomic (EN apenas). Custos de armazenamento aceitáveis (768d ≈ 3KB/entrada; 10k entradas ≈ 30MB) desde que o arquivo de embeddings herde o boundary de crypto do contentEnvelope. O artefato TFLite é gateado no HF (requer login HF); o peso (~300–580MB) é inviável embutido no APK — download sob demanda com consentimento explícito, decisão a registrar em ADR se a via semântica avançar.

## 13. Prototype híbrido (S6) — removível, isolado, com contrato forte

`SemanticSearchPrototype.kt` (`app/mobile/src/main/java/com/projetofio/app/search/`):

- **`SemanticSearchPrototype`** (object): `reciprocalRankFusion` (RRF, k=60), exposto para teste direto da matemática de fusão.
- **`EmbeddingProvider`**: interface pluggável; nenhuma implementação de produção embarcada — LiteRT só entra se o kill criterion for satisfeito por medição em aparelho.
- **`FakeEmbeddingProvider`**: determinístico (hash → vetor unitário), zero pesos embarcados; textos iguais score 1,0, distintos ≈ aleatório (floor 0,1).
- **`HybridSearchService`**: braço lexical reutilizando o contrato completo do `LocalSearchService` + braço vetorial. **O braço vetorial herda TODOS os filtros** (seladas, soft-deleted, `matchesFilters` de tempo/retorno aplicados ANTES da fusão) — a semântica nunca reintroduz conteúdo que o léxico descartou. Fallback total para léxico se o provider falhar.

Foi encontrado e corrigido no próprio S6 um bug de projeto: sem o `matchesFilters` no braço vetorial, uma entrada editada (que deixou de casar lexicalmente) podia voltar ao resultado via score vetorial — violando o contrato "a busca sempre reflete a versão atual da Entry". O fix e os testes de regressão (`repeat` determinísticos) fecham o caso. 9 testes cobrem fusão, propagação de filtros em ambos os braços, invariantes de seladas/deletadas, fallback e reflexão de edição/exclusão em tempo real.

## 14. Kill criterion — a régua que decide se a semântica embarca

Documentado em RESEARCH-LOG e no header do arquivo: a semântica só embarca em produção se **recall@10 ≥ +10pp ou MRR ≥ +0,08 vs lexical**, com **RAM ≤ +200MB**, índice ≤ ~100MB @10k entradas e **latência ≤ 500ms** medidos em aparelho com NPU. O ganho de qualidade está provado no sintético; o que pode matar a semântica é o custo (latência fria em CPU, peso do modelo, manutenção do pipeline). Recomendação: medir o `embeddinggemma-300m` em aparelho real com NPU antes de decidir. **Manter só o léxico é um resultado válido e respeitado** (ADR-040/SEARCH-ARCHITECTURE §87) — a complexidade pode ser excluída.

## 15. Decisões pendentes para o fundador (DECISION REQUIRED)

| # | Decisão | Opções | Recomendação da missão |
|---|---|---|---|
| D-1 | **Notas seladas na busca**: contagem opaca visível ou invisibilidade total? | Visível (default atual do código, toggável) / Invisível | Invisibilidade total por padrão (threat model); contagem só atrás de toggle explícito |
| D-2 | **Analytics remoto de busca**: local-first (`search_executed`, `result_count_bucket`, `search_mode`, `latency_bucket`) é o padrão; transporte remoto segue DECISION REQUIRED | Local only / Remoto com aprovação | Local only até decisão; nada chega ao servidor sem aprovação |
| D-3 | **Semântica em produção**: embarcar EmbeddingGemma ou manter só léxico? | Embarcar (com kill criterion) / Manter lexical only | Medir em aparelho com NPU antes; ADR de model selection a registrar |
| D-4 | **Download do modelo on-demand** com consentimento explícito (peso ~300–580MB fora do APK) | ADR próprio se D-3 for positiva | — |

## 16. Cobertura de testes e acessibilidade

A suíte final tem **134 testes verdes, 0 falhas**: `LexicalTokenizerTest` (10), `LocalSearchServiceTest` (12), `HybridSearchServiceTest` (9 — fusão, filtros duplos, invariantes, fallback, live-edits), `DraftSurvivesFailedInsertTest` (3) + os demais do baseline da missão 3. Acessibilidade auditada em S7: linha de resultado anuncia cada hit de forma **factual** via TalkBack (`contentDescription` = "data. trecho original. Já voltou N vez(es)"), `liveRegion = Polite` no contador e na linha de resultados (anúncios sem roubar o foco do campo), botão de limpar com alvo de toque 48 dp e descrição "Limpar busca", campo rotulado pelo placeholder (sem dupla leitura). O princípio retrieve-not-interpret vale também para o leitor de tela: ele anuncia palavras originais e datas, nunca resumos.

## 17. O que esta missão NÃO fez (e por quê)

- **Não conectou a semântica às devoluções** (proibido por ADR-040/SEARCH-ARCHITECTURE §45 — infraestrutura pode ser compartilhada, política nunca).
- **Não embarcou nenhum modelo de embeddings no app** — o prototype carrega zero pesos; `HybridSearchService` não é injetado na UI de produção.
- **Não criou índice persistente (FTS5)** — rejeitada para V1 pelo boundary de plaintext; Opção A (scan sob demanda) cobre o uso atual com latência <1 ms.
- **Não implementou "Pesquisas recentes"**, histórico de queries, analytics remoto, ou qualquer persistência de query.
- **Não tocou o main** — tudo no branch de integração; `main` local == `origin/main` (a4fc835).
- **Não fez medição em aparelho real** — a latência do benchmark é de CPU sandbox (o EdgeTPU mede <15 ms na Google; NPU Android é a incógnita crítica).
- **Não respondeu "quando devo…"** — nenhuma saída da busca interpreta; o usuário conecta os fatos.

## 18. Entregáveis e onde encontrá-los

| Arquivo | Papel |
|---|---|
| `app/mobile/src/main/java/com/projetofio/app/search/*.kt` | Tokenizer, serviço lexical, prototype híbrido, interfaces de domínio |
| `app/mobile/src/test/java/com/projetofio/app/search/*Test.kt` | 31 testes de busca + D12 (134 na suíte total) |
| `docs/search/SEARCH-ARCHITECTURE.md` | Definições canônicas, princípio, contratos operacionais |
| `docs/search/SEARCH-SEALED-THREAT-MODEL.md` | 6 perguntas do threat model de seladas |
| `docs/search/SEARCH-PRIVACY-ARCHITECTURE.md` | Lifecycle do plaintext, Opção A, FTS5 rejeitada |
| `research/search/FIO-SEARCH-BENCHMARK.md` + `run_benchmark.py` + `benchmark_results.json` | Benchmark PT-BR sintético (fora do build) |
| `research/search/RESEARCH-LOG.md` | Comparativo de embeddings on-device |
| `research/search/RETURN-HISTORY-AUDIT.md` | Auditoria S1 ("já voltou" cobre o schema) |
| `plans/SEARCH-EXECUTION-QUEUE.md` | Fila de execução com histórico SHA-a-SHA |

## 19. Riscos remanescentes e trabalho recomendado para o Codex

1. **Medição em aparelho** com `embeddinggemma-300m_seq512_mixed-precision.tflite` (NPU): latência fria, quente e RAM são o kill criterion — sem isso, nenhuma decisão D-3.
2. Se D-3 avançar: ADR de model selection + ADR de download on-demand com consentimento; arquivo de embeddings herdando o boundary do Keystore; reindex idempotente/cancelável/resiliente (SEARCH-ARCHITECTURE §Phase B).
3. **Torture em escala**: o protótipo S6 prova o contrato em unit tests; falta validação com ~1k entradas e edição massiva em aparelho (memória, debounce sob carga).
4. **Decisão D-1 (seladas)**: definir o padrão (invisível total vs contagem opaca) antes de qualquer exposição adicional.
5. **PrivacyCover/app switcher**: a tela de busca deve respeitar o PrivacyCover e a query não deve aparecer no app switcher (contrato §53) — conferir quando o PrivacyCover entrar no app.
6. **Screenshot/recents da query**: garantir que `FLAG_SECURE`-equivalent ou PrivacyCover cubra o campo de busca em aparelhos-alvo do piloto.

## 20. Nota de handoff

Esta missão entregou a **primeira camada de "Encontrar"**: lexical em produção, semântica como pesquisa isolada e removível. A essência do Fio — grandiosa e quieta — foi preservada em cada camada: snippets originais, datas factuais, queries efêmeras, texto plano confinado à memória, seladas invisíveis, nada que fale pelo usuário. O próximo movimento responsável não é mais código: é **medição em aparelho** (D-3) e as decisões do fundador listadas na seção 15. Se a semântica não vencer o kill criterion, o léxico sozinho é a resposta — e esta missão terá feito seu trabalho do mesmo jeito.
