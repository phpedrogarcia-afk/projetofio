# DEPENDENCY-MAP — tudo o que o Fio depende, classificado

**Estado examinado:** HEAD `9cfd5f1` · **Evidência:** `E4` (`build.gradle.kts`, `gradle/libs.versions.toml`, `AndroidManifest.xml`, código) + `E1` (docs oficiais Android).

## 1. Android / plataforma (USE PLATFORM — decisões ADR-033/034/035/038)

| Dependência | Papel no Fio | Status |
|---|---|---|
| Kotlin 2.x + Coroutines 1.11.0 | linguagem, concorrência estruturada, Flows | produção |
| Jetpack Compose (BOM + material3) | toda a UI; tema Verde-Sálvia via tokens | produção |
| Activity Compose | MainActivity single-activity | produção |
| Lifecycle (runtime+viewmodel-compose 2.10.0) | ViewModel, StateFlow UI | produção |
| Room 2.8.4 + KSP | persistence (schema 3, exportSchema=true, migrations manuais 1→2→3) | produção |
| androidx.work 2.11.2 | agendamento de devoluções (OneTimeWorkRequest) | produção |
| androidx.biometric 1.1.0 | app lock (DeviceAuthenticator) | produção |
| Android Keystore (API 23+) | chave AES, envelope GCM | produção |
| NotificationManager | canal "Devoluções" | produção |
| SAF (ContentResolver / DocumentFile) | import e export | produção |
| java.time | engine temporal (ZoneId, LocalDate, ZonedDateTime) | produção |
| Manifest | `POST_NOTIFICATIONS` (API 33+), `allowBackup="false"`, extraction rules | produção |

Risco de plataforma: `allowBackup="false"` protege contra backup ADB mas significa **sem recover via backup do Google** — perda de aparelho = perda de chaves. Mitigação de produto: export antes de trocar (ADR-018).

## 2. Bibliotecas externas (revisão uma a uma)

| Lib | Versão (toml) | Função | Nativa alternativa? | Risco | Manutenção | Licença |
|---|---|---|---|---|---|---|
| Compose BOM / material3 / activity / lifecycle | BOM | UI | — (padrão Google) | baixo | ativa (Google) | Apache-2.0 |
| Room + KSP | 2.8.4 / 2.3.11 | DB | SQLite raw (mais risco) | baixo | ativa | Apache-2.0 |
| kotlinx.serialization | 1.8.1 | **não usada em produção ainda** (manifest.json do export é hand-rolled; verificar uso futuro — risco de dependência órfã se nunca usar) | json hand-rolled | baixo | ativa | Apache-2.0 |
| AGP | 9.3.1 | build | — | médio (AGP 9 mudou DSL; upgrades exigem atenção) | ativa | — |
| JUnit4 4.13.2 + Robolectric 4.14.1 + Espresso 3.7.0 | — | testes unit/instrumentados | Kotest (mudança de custo) | baixo | ativa | Apache-2.0/MIT |
| `testImplementation room-testing` | 2.8.4 | Migration2To3Test | — | baixo | idem | Apache-2.0 |

**Nenhuma dependência de produção com fluxo de rede.** Nenhuma SDK externa de analytics, crash, ads, ou ML embarcada. Qualquer nova dependência com rede + conteúdo privado = proibida por `AGENTS.md` sem decisão humana.

## 3. Pesquisa (fora do build — `research/search/`)

| Artefato | Papel | Evidência |
|---|---|---|
| `run_benchmark.py` + `benchmark_results.json` | benchmark sintético PT-BR (réplica do algoritmo Kotlin) | E3 |
| Proxy multilingual-e5-large | proxy de qualidade semântica (proxy ≠ candidato de produção) | E2 |
| EmbeddingGemma-300M (HuggingFace `litert-community`) | candidato primário on-device | E2 (documentação HF/Google); **E5 hardware ausente** |
| MTEB-BR (22 tarefas, 93 modelos) | painel de ranking PT-BR nativo | E2 (paper/dashboard) |

Licença relevante: EmbeddingGemma = Google open license (verificar termo no download gateado HF antes de qualquer embarque — packet `FIO-Pxx` inclui verificação de licença como stop condition).

## 4. O que o Fio deliberadamente NÃO depende

Não há: Retrofit/OkHttp (sem rede), Hilt/Dagger (DI manual lazy no `FioGraph` — decisão deliberada de simplicidade M1), NavController (navegação por estado), Timber/Log collectors, Firebase/Play Services, DataStore/SharedPreferences (settings em Room singleton), SQLCipher (criptografia no envelope application-level + Keystore, decisão ADR-023/035), qualquer vetor DB (Qdrant, SQLite-vss, Vespa — FTS5 rejeitada; vetorial futuro = decisão gateada).

## 5. Regra para novas dependências

Cadeia obrigatória (regra "não reinventar a roda", §6 da missão): (1) Android/Jetpack resolve? → (2) lib madura resolve? → (3) OSS confiável adaptável? → (4) padrão consolidado de produto? → (5) somente então construir. Qualquer adição passa por `docs/atlas/EXTERNAL-SOLUTION-INDEX.md` + decisão humana se tocar princípios/privacidade/rede.
