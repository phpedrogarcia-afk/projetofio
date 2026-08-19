# RESEARCH-LOG — Embeddings on-device para o Fio (Missão 4)

**Branch:** `integration/manus-search-20260819` · **Data:** 2026-08-19
**Escopo:** Pesquisa autorizada por ADR-040/ADR-041 (protótipo removível, isolado). Nenhuma decisão de produção é tomada neste log; a escolha final permanece DEFERRED (ADR-024) até que os critérios de custo sejam satisfeitos em aparelho real.

## 1. O que a pesquisa precisava responder

Qual modelo de embeddings on-device entrega recuperação semântica de qualidade para diários em PT-BR, respeitando os limites do Fio: dados criptografados em repouso (o índice semântico é derivado, protegido pelo mesmo boundary de crypto), RAM ≤ +200MB, índice persistente ≤ ~100MB para 10k entradas, e removibilidade total (ADR-040).

## 2. Evidência sobre qualidade PT-BR: MTEB-BR

O benchmark nativo MTEB-BR (22 tarefas PT-BR, 93 modelos, jul/2026, arXiv 2607.04581v2) fornece a melhor evidência pública disponível para esta escolha. Dois achados definem o espaço de decisão:

| Achado | Implicação para o Fio |
|---|---|
| O rank em leaderboards multilíngues só prediz moderadamente o rank em PT-BR (ρ=0.75; um modelo 3º no multilingual é 49º em PT-BR) | Escolher por evidência PT-BR nativa, não por MTEB global |
| `embeddinggemma-300m` (308M, open) é o 13º de 93, a 0.033 do líder comercial, e o melhor modelo on-device do painel | Candidato primário para o protótipo |

Top-15 resumido: gemini-embedding-001 (0.682, fechado), Qwen3-Embedding-8B/4B (0.670/0.662, 4–8GB — inviável on-device), voyage-*/codestral (fechados), **embeddinggemma-300m (0.649, 308M)**. No extremo baixo, gte-small (33M), multilingual-e5-small (118M) e paraphrase-multilingual (278M) ocupam o fim do painel em PT-BR — pequenos demais para valer em português.

## 3. Candidatos avaliados

| Modelo | Params / peso | TFLite | RAM estimada | PT-BR (MTEB-BR) | Observação |
|---|---|---|---|---|---|
| **EmbeddingGemma-300M** | 308M, ~578MB fp32 / mixed-precision quantizado | **Sim, oficial** (`litert-community/embeddinggemma-300m`, seq256/512/1024/2048) | <200MB com QAT | **0.649 — 13º/93** | Matryoshka 768/512/256/128; 2K ctx; mesmo tokenizer da Gemma 3n; Google licencia open |
| multilingual-e5-large | 560M | Não oficial | >500MB | não medido (proxy no benchmark) | Usado só como proxy de qualidade no benchmark; sem TFLite oficial |
| bge-m3 | 568M | Não (ONNX) | >500MB | medido (568M) | Bom, mas grande e sem pipeline Android nativo |
| gte-small (TFLite) | 33M, 127MB | Sim | ~100MB | fim do painel PT-BR | Só como contraste: pequeno demais para PT-BR |
| nomic-embed-text-v1.5 | 274M | Não oficial | ~300MB | EN apenas | Descartado (diário é PT-BR) |
| Serafim-335m-pt / Albertina-900m | 335M/900M PT-nativos | Não | — | medidos (335M) | PT-BR dedicado, mas sem formato on-device documentado; ficam como candidatos se o ecosystem avançar |

**Escolha do protótipo (S6):** `embeddinggemma-300M_seq512_mixed-precision.tflite` (ou seq256 para memória mais restrita). Nota: o artefato no HF LiteRT community é gateado (requer login HF no momento da pesquisa); o protótipo S6 implementa a via de inferência via LiteRT Python/runtime Android com download manual, e a decisão de embutir o peso no APK permanece pendente (peso ~300–580MB é inviável embutido; download sob demanda pós-instalação com consentimento explícito do usuário — ADR a registrar se a via semântica avançar).

## 4. Resultado do benchmark sintético PT-BR (`run_benchmark.py`, corpus de 60 entradas, 30 consultas)

Executado em CPU sandbox (sem aceleração TPU/NPU; latências reais de aparelho tendem a ser menores).

| Configuração | Recall@5 | Recall@10 | Precisão@5 | MRR | Hit@10 | Latência p95 |
|---|---|---|---|---|---|---|
| `lexical_v1` (produção) | 0.098 | 0.109 | 0.060 | 0.200 | 0.200 | **<1 ms** |
| `semantic` (proxy multilingual-e5-large) | 0.797 | 0.917 | 0.573 | 1.000 | 1.000 | ~2.6 s |
| `hybrid` (RRF k=60) | 0.797 | 0.917 | 0.573 | 1.000 | 1.000 | ~2.6 s |

Leituras honestas: (a) o lexical falha onde a consulta é parafrástica (24 de 30 consultas exigem semântica para recall completo) — confirma que "encontrar melhor" tem teto com léxico puro; (b) a semântica recupera com recall quase total, mas **a latência CPU fria de ~2.5s viola o teto de 500ms** — EmbeddingGemma em CPU de aparelho mediano é a incógnita crítica; no EdgeTPU a Google mede <15ms, e CPUs modernas de mid-range Android com NPU (MediaTek APU, Snapdragon Hexagon, Tensor) oferecem aceleração real. O benchmark replica o algoritmo exato do `LexicalTokenizer` do app (validado contra o Kotlin).

**Custo de armazenamento:** embeddings 768d fp32 = 3KB/entrada → 10k entradas ≈ 30MB; em 256d ≈ 10MB. Aceitável, desde que o arquivo de embeddings herde o boundary de crypto do contentEnvelope (mesmo Keystore, mesma política de delete permanente).

## 5. Kill criterion — leitura preliminar

O ganho de qualidade é inegável no benchmark sintético (MRR 0.200 → 1.000; recall@10 0.109 → 0.917). O que pode matar a semântica é o custo: latência fria em CPU sem NPU, peso do modelo (~300–580MB fora do APK), e manutenção do pipeline de inferência. Recomendação de pesquisa: levar o protótipo S6 e medir em aparelho real com NPU antes de decidir; o fallback 100% lexical já está em produção e é a condição padrão do app.

## 6. Referências

- MTEB-BR: arXiv 2607.04581v2 (jul/2026); leaderboard interativo: https://huggingface.co/spaces/mteb-br/leaderboard
- EmbeddingGemma: Google Developers Blog, 4 set/2025; tech report arXiv 2509.20354; pesos HF `google/embeddinggemma-300m` e TFLite `litert-community/embeddinggemma-300m`
- gte-small TFLite: `Bombek1/gte-small-litert` (127MB, MIT, EN)
