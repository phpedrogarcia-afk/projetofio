# MANUS-FIO-ATLAS-FINAL — Relatório da Missão 5 (FIO MASTER ATLAS)

**Missão:** transformar o ProjetoFio inteiro em um mapa técnico preciso — radiografia do sistema real, pesquisa de estado da arte, invariantes, risco, decisões e packets — sem escrever uma linha de código de app novo, sem tocar a main. **Executada em:** 2026-08-19. **Branch:** `research/manus-fio-master-atlas-20260819` (commit `7770caa`, 26 arquivos, ~1.640 linhas). **Base:** M4 completa (9cfd5f1, 134 testes verdes).

## 1. O que o Atlas é (e o que não é)

O Atlas é o sistema de leitura do ProjetoFio. Nove mapas descrevem o que o sistema realmente é (não o que se esperava que fosse); dois documentos de Codex dizem como trabalhar nele sem corrompê-lo; quinze cartões de padrão e um catálogo de referências dizem o que já existe lá fora e o que aprendemos com cada coisa; o catálogo de invariantes diz o que nunca pode quebrar; o mapa de risco diz onde o produto está exposto; e a fábrica de packets organiza todo o trabalho futuro em unidades executáveis com dependências explícitas.

O Atlas deliberadamente **não é** norma. A hierarquia canônica permanece: código em produção > ledger de decisões > princípios > produto > specs > roadmap > visão. O Atlas é lente: ele envelhece a cada commit e existe para que um agente (ou humano) novo se oriente em trinta minutos em vez de três dias.

## 2. Radiografia em resumo (os nove mapas)

| Mapa | Síntese da descoberta |
|---|---|
| SYSTEM-MAP | Seis camadas reais: UI (FioApp.kt, 1.914 linhas, single-activity sem NavController), aplicação (FioService guarda com autosave e draft criptografado antes do insert), domínio puro (TimeReturnEngine), returns (WorkManager), persistência (Room 3, migrations manuais), fronteiras (crypto/search/security). DI manual via FioGraph |
| DATA-MAP | Entry (schema 3: sealedAt, purgeAfter) + contentEnvelope GCM + ReturnAttempt (com OPENED para o "já voltou" factual) + draft singleton + import staging atômico. Nenhum campo de spec futuro (accessPolicy, restUntil) existe no código ainda |
| PRIVACY-BOUNDARY-MAP | A fronteira é o envelope application-level: Keystore → chave → GCM com AAD(kind, recordId, schemaVersion). backup=false, manifest sem rede. Sealed: conteúdo nunca decifrado em nenhum caminho de leitura coletiva |
| TIME-MAP | Motor completo em produção: elegibilidade, cap 7 dias, bootstrap 0/30/60/90, quiet hours com midnight wrap, DST, fusos. WorkManager inexact (ADR-038 deliberado). Nunca conectado à busca |
| SEARCH-MAP | V1 léxica em produção (Option A scan-on-demand, tokens nunca persistidos, seladas opacas, soft-deleted invisível) + protótipo híbrido RRF k=60 removível cujo braço vetorial herda todos os filtros. Kill criterion quantitativo registrado |
| UX-SURFACE-MAP | Home (editor + pátina botânica + time sheet), Arquivo (com busca inline), Return, Settings. Acessibilidade: contentDescriptions factuais, live regions, 48dp. PrivacyCover na troca de apps |
| DEPENDENCY-MAP | Tudo platform (Room, Compose, Keystore, WorkManager, BiometricPrompt). Rede proibida. Sem ML em produção. LiteRT/EmbeddingGemma como único candidato ML futuro, com gates |
| TEST-MAP | 134 unitários verdes (31 arquivos) + 12 instrumentados que **nunca executaram** (sem AVD) — a maior lacuna de evidência honestamente declarada. Data torture matrix em prática |
| DECISION-INDEX | ADR-001..047 com status; 4 superseded (iOS era); 1 deferred (semântica); 5 decisões abertas do fundador catalogadas (D-1..D-5) |

## 3. O que a pesquisa externa confirmou

O estado da arte de journaling privado (Day One, Standard Notes, Notesnook, Mini Diarium, Obsidian, Joplin) valida a arquitetura do Fio: "encrypt before disk", offline total e export portável são o padrão-ouro — e o Fio já os implementa. A contribuição distintiva do Fio não é a criptografia (commoditizada entre os bons produtos), mas a **política temporal consentida**: ninguém no mercado combina devolução sem calendário, cap de frequência, "never return" por entrada e notificação única sem preview. Do lado oposto, o resurfacing automático de massa (Google Photos/Facebook Memories) tem dano emocional documentado — é o contra-exemplo que justifica o opt-in do Fio [4].

No ML on-device, os números verificados do EmbeddingGemma 300M no Samsung S25 Ultra (NPU: init 206ms, infer 7.8ms, RSS 224MB, modelo 182MB; CPU seq256: 66ms/110MB) dão dureza ao kill criterion que ficou registrado: a medição deve ser ΔRSS do app, não RSS do modelo isolado, e o download exige aceitar a licença "gemma" no Hugging Face — stop condition explícita [10] [11].

## 4. Riscos honestos (top 5)

1. **Perda total por aparelho (K-01):** chaves Keystore são do aparelho e allowBackup=false; export manual é a única recuperação. O fundador precisa saber disso hoje — a mitigação proposta (lembrete local de export) é decisão de produto.
2. **Devolução em momento indesejado (K-06):** único risco emocional do core; só o piloto testa.
3. **Crypto review nunca executado (K-04):** um finding ali invalidaria tudo; packet pronto, nunca rodado.
4. **Cobertura instrumentada fantasma (K-17):** 12 testes de crypto/surfaces nunca executados.
5. **PrivacyCover não verificado em aparelho (K-02):** claim central de privacidade sem evidência device.

## 5. Drift encontrado e corrigido

A auditoria §6 achou dois drifts reais: `PROJECT-STATE.md` não registrava a Missão 4 concluída (busca em produção + protótipo) nem o estado atual da branch — corrigido no packet FIO-PQ-01 — e identificou duas tensões legítimas que não são bugs: `docs/06` é spec canônico à frente do código (accessPolicy/embeddingCiphertext/restUntil não existem ainda; são o contrato V1), e a tabela de bootstrap do piloto em `docs/07` difere do código de produção (0/30/60/90) deliberadamente.

## 6. A fábrica de packets

Todo trabalho futuro vive em `packets/`: template, índice (PQ documentação, P0x device/escala, P1x produto, PILOT humano), grafo de dependências com caminho crítico (`PQ-03 → P12 → P13 → P15` para semântica; `P01 → P03` para crypto) e fila de execução viva. Regras: nenhum trabalho ad-hoc; no máximo 2 packets de código simultâneos; aparelho é recurso exclusivo; piloto congela a superfície Return durante a coleta.

## 7. O que a Missão 5 NÃO fez (por desenho)

Nenhuma linha de código de app; nenhum merge; nenhuma decisão tomada no lugar do fundador (as cinco decisões abertas foram propostas por escrito, não resolvidas); nenhuma execução de testes instrumentados (sem AVD — declarado como gap, não fingido); nenhum packet executado (apenas escritos e ordenados). O que foi produzido é conhecimento comprimido: 26 arquivos que fazem o Codex começar do meio do caminho em vez do zero.

## 8. Para o próximo agente (boot prompt)

> "Você está trabalhando no ProjetoFio, um diário autobiográfico Android 100% local e criptografado. Antes de qualquer arquivo: leia `AGENTS.md` e `docs/atlas/CODEX-MASTER-MAP.md`; depois `INVARIANTS.md`; depois o packet em fila em `packets/EXECUTION-QUEUE.md`. Nunca em `main`; branch própria sempre; testes reais (não up-to-date); decisão nova = ADR novo. Quando terminar, responda o fresh-agent check do `CODEX-EXECUTION-PROTOCOL.md`."

## 9. Referências

[10]: https://ai.google.dev/gemma/docs/embeddinggemma "Google — EmbeddingGemma model overview"
[11]: https://huggingface.co/litert-community/embeddinggemma-300m "Hugging Face — litert-community/embeddinggemma-300m"
[4]: https://about.fb.com/news/2015/03/introducing-on-this-day-a-new-way-to-look-back-at-photos-and-memories-on-facebook/ "Facebook — On This Day (2015)"

Fonte completa da pesquisa externa: `research/atlas/REFERENCE-CATALOG.md` (16 referências numeradas e analisadas).
