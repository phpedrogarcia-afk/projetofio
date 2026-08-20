# PROJECT-STATE — snapshot vivo (2026-08-20)

Documento de estado vivo do ProjetoFio. O código vence quando divergir da documentação; resultados históricos não substituem uma validação reproduzida no ambiente atual.

## Estado geral

| Dimensão | Estado atual | Evidência |
|---|---|---|
| Especificação | Fio v0.1 canônico; Android é o cliente inicial | `docs/01-PRODUCT.md`, ADR-033 |
| Plataforma | Android nativo, Kotlin, Compose, Room e KSP; minSdk 26; ID `com.projetofio.app` | `app/mobile/build.gradle.kts` |
| Persistência | Room schema 3; conteúdo e fingerprints sensíveis em envelopes criptografados | `FioDatabase.kt`, `Entities.kt`, schemas Room |
| Criptografia | AES-256-GCM apoiado pelo Android Keystore; revisão independente ainda aberta | `AesGcmContentCipher.kt`, `CRYPTO-REVIEW-PACKET.md` |
| Guardar | Editor, draft criptografado e transação Entry+clearDraft implementados | `FioService.kt`, `RoomFioRepository.kt` |
| Reencontrar | Engine temporal, consentimento, cap, quiet hours e notificação discreta implementados | `TimeReturnEngine.kt`, `TimeReturnsService.kt` |
| Encontrar | Busca lexical local scan-on-demand conectada à produção; query somente em memória; apagados excluídos | `LocalSearchService.kt`, `FioApplication.kt`, `FioDao.kt` |
| Semântica | Protótipo híbrido removível, não conectado à aplicação e sem modelo ML embarcado | `SemanticSearchPrototype.kt`, ADR-040/041 |
| Search × Returns | Sistemas separados; Search não seleciona nem comanda Returns | `FioApplication.kt`, `SearchRepository.kt`, `TimeReturnsService.kt` |
| UI | Guardar/Encontrar/Arquivo; superfícies Compose separadas por pasta; Ajustes usa visão geral e páginas focadas em linguagem humana | `ui/FioApp.kt`, `ui/{home,search,archive,entry,returns,settings}/`, ADR-048/049 |
| Importação/exportação | Import local Markdown/Texto com rollback; export Markdown/Texto + SHA-256 | serviços de import/export e testes correspondentes |
| Rede/conta/sync | Ausentes; aplicativo permanece local-first | Manifest, dependências e wiring do aplicativo |
| Analytics | Analytics remoto ausente e estado efetivo desabilitado | `RoomFioRepository.kt`, dependências |
| Testes unitários | 134 testes verdes, 0 falhas, 0 erros e 0 ignorados em 2026-08-20, com dependency verification ativa | relatórios XML de `testDebugUnitTest`, `packets/FIO-PB-01.md` |
| Testes instrumentados | 28/28 verdes no AVD Android 8/API 26 em 2026-08-20 | execução `AndroidJUnitRunner`, `packets/FIO-P18.md` |
| Pilot | Material preparado; piloto não iniciado | `pilot/` |

## Missões herdadas

| Missão | Resultado | Estado |
|---|---|---|
| M1 | Hardening e failure hunting | Concluída |
| M2 | Product Red Team, preparação de piloto e handoff | Concluída; gates humanos permanecem abertos |
| M3 | Refinamento de UX e interação | Concluída |
| M4 | Busca lexical, arquitetura semântica removível e histórico factual de reencontros | Concluída |
| M5 | FIO Master Atlas, mapas, invariantes, riscos e fila de packets | Concluída |

## Git e transferência

- Branch de transferência confirmada: `research/manus-fio-master-atlas-20260819`.
- HEAD recebido do Atlas: `771074e069eed457aba1c9cda42e9c697f6d66d7`.
- Branch atual de eficiência/contexto: `codex/context-efficiency-v1`, derivada da branch de UX.
- `main` permanece snapshot antigo e não é fonte do estado integrado atual.
- A worktree anterior `codex/v0-time-only-checkpoint` contém alterações locais e deve permanecer preservada até integração deliberada.
- Estados de PR descritos pelos relatórios do Atlas são históricos até consulta explícita ao GitHub.

## Drifts e riscos conhecidos

1. O protótipo híbrido calcula candidatos exclusivamente vetoriais, mas a montagem atual dos resultados descarta candidatos sem hit lexical; ele não comprova recall semântico exclusivo no comportamento presente.
2. Algumas assinaturas PGP de artefatos não puderam ser recuperadas; o metadata mantém hashes SHA-256 e registra explicitamente essas exceções para revisão futura.
3. A interface interna do pacote principal no Poco após o gate biométrico ainda depende da autenticação do usuário; a mesma interface foi validada integralmente no AVD isolado.
4. Evidência histórica de PR, teste ou aparelho não deve ser promovida a estado atual sem nova verificação.
5. P0 de integridade: o seletor visual `InPeriod`/`OnDate` ainda não persiste a escolha; schema 3 armazena apenas `ELIGIBLE`/`NEVER`. Não descrever datas/períodos como funcionais até decisão e migration próprias.

## Gates abertos

- TalkBack em aparelho físico.
- Segundo fabricante/modelo Android.
- PrivacyCover em aparelho.
- Revisão independente de criptografia.
- Consentimento e execução do piloto.
- Decisões de produto listadas em `docs/atlas/DECISION-INDEX.md`, com FIO-P19 em prioridade P0.

## Instalação atual

- `com.projetofio.app` 0.1.0-dev atualizado com preservação de dados no Xiaomi M2103K19PG/API 33 em 2026-08-20.
- APK debug da navegação FIO-P17: SHA-256 `FD35B1398DF8151A8DC0EF86D6A539346ED2E189EDA2D0A5089EFFA72DEC334A`.
- Abertura confirmada até o gate biométrico, sem crash fatal observado.
- A inspeção interna completa foi executada no AVD `Fio_API26` (Android 8/API 26): 28 testes instrumentados; navegação, funções básicas e páginas de Ajustes confirmadas pela hierarquia real.
- A inspeção no Poco não contorna a impressão digital/PIN e permanece pendente da autenticação do usuário.
- O build debug temporário de ADR-050 foi instalado no Poco com preservação de
  dados em 2026-08-20; versão `0.1.0-dev`, MainActivity em primeiro plano e
  captura validada sem reter a imagem. SHA-256 instalado:
  `2EB546FDB42E14B2165BF48A7A5FA4B0E081C6D96464E5D708F29B87E4DFEFC5`.

## Próximo trabalho autorizado

Seguir `packets/EXECUTION-QUEUE.md`. O FIO-P18 fechou os gates de engenharia; a
observação de compreensão pelo fundador continua externa. O FIO-P19 é NOW e
aguarda escolha explícita, sem autorizar schema 4 por si só. Depois dele, retomar
FIO-PQ-03. Nenhuma feature deve ser escolhida fora da fila.

## Regras que permanecem

Não adicionar gamificação, feed/social, publicidade, IA interpretativa, profiling, histórico de queries, rede de conteúdo privado ou comando semântico sobre Returns. Não declarar gates humanos aprovados sem evidência e não integrar em `main` sem decisão explícita.
