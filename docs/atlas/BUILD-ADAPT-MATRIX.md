# BUILD-ADAPT-MATRIX — construir, adaptar ou usar plataforma para cada necessidade

**Regra de decisão (ordem fixa):** (1) Android/Jetpack resolve? → USE PLATFORM · (2) biblioteca madura resolve sem violar princípios? → ADAPT · (3) OSS confiável adaptável? → ADAPT COM AUDIT · (4) padrão de produto consolidado e simples? → BUILD DELIBERADO · (5) nada acima? → **não construir; registrar em IDEA-INBOX**.

Toda linha com risco de violar princípios (privacidade, retrieve-not-interpret, local-first) tem decisão humana obrigatória, mesmo em "USE PLATFORM".

## 1. Core do produto (diferenciais — aqui CONSTRUÍMOS)

| Necessidade | Decisão | Justificativa |
|---|---|---|
| Guardar (editor + cripto + draft) | **BUILD** (feito) | É o produto; ninguém externaliza |
| Time Return Engine (cap, bootstrap, buckets, wrap) | **BUILD** (feito) | Política temporal própria é o diferencial central; biblioteca nenhuma modela "devolução consentida" |
| Privacy boundary (envelope GCM + Keystore + AAD) | **BUILD** (feito) | Arquitetura de confiança própria; SQLCipher esconderia tudo inclusive índices (decisão ADR-023/035) |
| Export v1.0 + checksum | **BUILD** (feito) | Promessa de longevidade é do produto |
| Import local com rollback atômico | **BUILD** (feito) | ADR-039; parser Markdown próprio |
| Busca lexical PT-BR | **BUILD** (feito) | Minimalista deliberado; FTS5 rejeitada por boundary |
| Semântica on-device (se embarcar) | **ADAPT** (prototipado) | EmbeddingGemma via LiteRT/RAG library (USE LIBRARY) + RRF próprio (padrão aberto) — pesos não são nossos, a política de fusão é |

## 2. Plataforma e OS (USE PLATFORM — já decidido)

| Necessidade | Decisão | Estado |
|---|---|---|
| DB relacional + migrations | Room (platform) | produção |
| UI | Compose + Material 3 (platform) | produção |
| Agendamento | WorkManager (platform) | produção |
| Biometria | androidx.biometric (platform) | produção |
| Keystore | Android Keystore (platform) | produção |
| Notificações | NotificationManager (platform) | produção |
| Arquivos SAF | ContentResolver (platform) | produção |
| Data/hora | java.time (platform) | produção |
| Coroutines/Flows | kotlinx (platform padrão) | produção |
| Tests | JUnit4 + Robolectric + Espresso (padrão) | produção |

## 3. Candidatos futuros — pré-decisão documentada

| Necessidade futura | Opções | Recomendação prévia | Gate |
|---|---|---|---|
| Busca vetorial em produção | LiteRT RAG library / LiteRT raw / sqlite-vss / Faiss mobile | **ADAPT**: Google AI Edge RAG library (wrapper oficial LiteRT + tokenização) | D-3 positiva + FIO-P12 |
| Índice vetorial persistente | sqlite-vss, Faiss, Chroma-mobile, custom B-tree | **DECIDIR COM ADR**: hoje Option A não usa índice; índice = nova fronteira de plaintext? Não: só embeddings. Decisão: onde o embedding dorme (boundary crypto herdado) | D-3 |
| Modelo de embedding | EmbeddingGemma 300M (referência), alternativas MTEB-BR | **ADAPT** (download on-demand, licenciamento HF stop condition) | D-4 |
| Entrega exata de devolução | setExpedited / AlarmManager exact | **NÃO AGORA** (bateria, permissão, contraria espírito de "sem pressão"); padrão WorkManager+AlarmManager existe se necessário | ADR novo |
| Backup/restore multi-aparelho | allowBackup rules, encrypted restore flow | **DECIDIR**: hoje perda de aparelho = perda total; export é a única recuperação. Fluxo de restore criptografado = trabalho grande com impacto de princípio | D-5 herdada |
| Analytics local com schema fechado | ADR-017 já aceita; transporte remoto | **USE (local) / NÃO AGORA (remoto)** | D-2 |
| Hilt/Dagger DI | USE LIBRARY se módulos crescerem | Manter FioGraph até 3+ módulos pesados | custo |
| NavHost gráfico | USE PLATFORM se navegação crescer | Single-activity state basta hoje | custo |
| Tags/pastas/favoritos | BUILD deliberado (simples) se fundador pedir | **NÃO AGORA** (princípio anti-arquivo-complexo ADR-004) | fundador |
| On This Day (determinístico) | BUILD simples (query por data) | Só se fundador pedir; não compete com engine | fundador |
| LLM conversacional | — | **DO NOT BUILD** (violência de princípio: retrieve, not interpret; banido por AGENTS.md) | — |
| Sync servidor | — | **DO NOT BUILD** (ADR-006/025: deferred; primeiro sync = decisão fundacional) | piloto + fundador |

## 4. O que deliberadamente NÃO usamos (e por quê)

Não usamos: Firebase (rede + conteúdo), Retrofit/OkHttp (rede), SQLCipher (criptografia application-level escolhida), Hilt (simplicidade), NavController (simplicidade), any SDK de analytics (D-2), any ML server-side (princípio), FTS5 (boundary), SQLite extensions não-auditadas com conteúdo (sqlite-vss ainda não auditada = gate).
