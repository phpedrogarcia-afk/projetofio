# TEST-MAP — referência de cobertura e lacunas

**Status:** REFERENCE. O router operacional atual é `docs/TEST-LEVELS.md`.
Este mapa preserva o inventário do checkpoint examinado em HEAD `9cfd5f1`;
resultados históricos não substituem a execução exigida pelo packet.

## 1. Mapa por sistema

```text
Crypto (AES-256-GCM, Keystore)
 ├── AesGcmContentCipherTest          round-trip, AAD binding, envelope format
 ├── AndroidKeystorePersistenceTest   (instrumentado — NÃO executado no sandbox)
 ├── EncryptedPersistenceTest         (instrumentado)
 ├── PrivacySurfacesTest              (instrumentado — PrivacyCover; E5 gate)
 └── P0 Missão 1: surrogate halves → 0x3F fixado + testado

Returns / Time (motor + plataforma)
 ├── TimeReturnEngineTest             elegibilidade, buckets, bootstrap
 ├── EngineTortureTest                fusos extremos, DST, wrap meia-noite, edge cases
 ├── TimeReturnsServiceTest           reconcile, pause, consent, cancelIneligible
 ├── AdverseConditionTest             (instrumentado)
 ├── M2AndroidContractTest            gate CI do milestone (instrumentado, não executado)
 └── hipóteses: HypothesisCheckTest

Guarda (write path)
 ├── FioServiceTest                   save/edit/delete lifecycle
 ├── DraftSurvivesFailedInsertTest    D12 fechada (3 contratos)
 ├── StorageFailureTest               export resiliente a falha SAF
 ├── DataTortureTest                  corrupção/edge de dados

Import / Export
 ├── ImportServiceTest                preview/commit/rollback/reconcile
 ├── LocalImportParserTest            markdown/texto parsing, issues
 ├── ExportCoordinatorTest            destinos, outcomes
 ├── ExportRoundTripTest              export→leitura→checksum
 ├── SafLaunchTest / M3RepositoryContractTest  (instrumentados)

Search (M4)
 ├── LexicalTokenizerTest             NFD/diacríticos, prefixo último token, snippets (10)
 ├── LocalSearchServiceTest           filters, sealed lambda, soft-delete, já voltou (12)
 ├── HybridSearchServiceTest          RRF, filtros herdados no braço vetorial, invariantes (9)

UI / UX
 ├── DatePickerZoneTest               ADR-043 picker + zones
 ├── MotionReducedTest                preferência do sistema
 ├── AccessibilityContractTest        (instrumentado)
 ├── ScaffoldLaunchTest               (instrumentado)

Persistência
 ├── Migration2To3Test                byte a byte, 500 entries
 ├── RoomSchemaTest                   (instrumentado)
 ├── PerformanceTest                  unit perf sanity

Contrato global
 └── ProjectContractTest              invariantes cross-cutting (AGENTS.md rules)
```

## 2. Lacunas honestas (não escondidas)

| Lacuna | Por quê | Risco | Packet |
|---|---|---|---|
| **Instrumentados nunca executados** (12 suítes) | sem AVD/emulador no sandbox | R1/R2 — crypto e surfaces sem evidência `E5` | `FIO-Pxx` (device gate humano) |
| Lexical em escala (~1k+ entries) | benchmarks só com 60 entradas sintéticas | R2 — debounce/memória sob carga | `FIO-P07` (scale validation) |
| Process death no meio de save/import | nenhuma suíte simula morte de processo | R1 — perda de draft em cenário raro | packet process-death |
| Reboot com WorkManager pendente | exige instrumentado | R2 — janela sem devoluções documentada mas não testada | device gate |
| TalkBack físico | TalkBack real exige aparelho + humano | R2 a11y | gate humano |
| Segundo OEM (Samsung/Xiaomi etc.) | WorkManager/biometria variam por OEM | R2 | gate humano |
| Revisão independente de cripto | nenhum par externo revisou `AesGcmContentCipher` | R0 | gate humano (packet crypto-review já escrito: `docs/security/CRYPTO-REVIEW-PACKET.md`) |
| `searchMode SEMANTIC` end-to-end no app | protótipo testado em unit, não injetado | R3 | `FIO-P12` (validação device) |
| Backup/restore do app (migração de aparelho) | allowBackup=false — sem fluxo testado | R0 disponibilidade | packet backup-policy |

## 3. Comando de verificação (o que Codex deve rodar)

```bash
cd app && ./gradlew :mobile:testDebugUnitTest --no-daemon   # 134 verdes, 0 falhas
# instrumentados (exigem AVD real):
./gradlew :mobile:connectedDebugAndroidTest                 # NÃO REPORTAR COMO PASSADO SEM EVIDÊNCIA
```
