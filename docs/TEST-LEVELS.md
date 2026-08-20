# Test levels

Status: ACTIVE — canonical test router

Execute a partir de `app/`. Use `gradlew.bat` no Windows. Um packet pode exigir
mais; nunca use presença de teste ou `UP-TO-DATE` como evidência de execução em
aparelho.

| Nível | Quando | Comando/evidência |
|---|---|---|
| L0 | compilação local da área | `.\gradlew.bat :mobile:compileDebugKotlin --offline` |
| L1 | teste específico | `.\gradlew.bat :mobile:testDebugUnitTest --tests "<classe>" --offline` |
| L2 | suíte unitária | `.\gradlew.bat :mobile:testDebugUnitTest --offline` |
| L3 | pre-merge | `.\gradlew.bat :mobile:testDebugUnitTest :mobile:assembleDebug :mobile:assembleRelease :mobile:lintDebug --offline` |
| L4 | aparelho/humano | teste instrumentado específico ou suíte em AVD/aparelho declarado; registrar alvo e output real |

## Router por área

| Mudou | L1 mínimo | Checkpoint |
|---|---|---|
| Home/Archive/Settings/UI | instrumentado da superfície (`AccessibilityContractTest`, `NoteBasicsContractTest` ou `ScaffoldLaunchTest`) | L2 + L3; L4 quando packet exigir |
| Search | `LexicalTokenizerTest`, `LocalSearchServiceTest`; híbrido também `HybridSearchServiceTest` | L2 |
| Returns | `TimeReturnEngineTest`, `EngineTortureTest`, `TimeReturnsServiceTest` | L2; L4 para WorkManager/notificação |
| Crypto | `AesGcmContentCipherTest` | L2 + packet de aparelho; revisão humana não é automatizável |
| Room/schema | teste de repository + migration correspondente | L2 + schema exportado + instrumentado |
| Import | `LocalImportParserTest`, `ImportServiceTest` | L2; SAF instrumentado se alterado |
| Export | `ExportCoordinatorTest`, `ExportRoundTripTest` | L2; SAF instrumentado se alterado |

Para executar instrumentados, isole o alvo antes. Não limpe dados do POCO para
obter evidência; use AVD com fixtures sintéticas salvo autorização específica.
