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
| UI | Design v1 Verde-Sálvia, editor, Arquivo, busca e superfícies de devolução implementados | `FioApp.kt` |
| Importação/exportação | Import local Markdown/Texto com rollback; export Markdown/Texto + SHA-256 | serviços de import/export e testes correspondentes |
| Rede/conta/sync | Ausentes; aplicativo permanece local-first | Manifest, dependências e wiring do aplicativo |
| Analytics | Analytics remoto ausente e estado efetivo desabilitado | `RoomFioRepository.kt`, dependências |
| Testes unitários | 134 testes verdes, 0 falhas, 0 erros e 0 ignorados em 2026-08-20, com dependency verification ativa | relatórios XML de `testDebugUnitTest`, `packets/FIO-PB-01.md` |
| Testes instrumentados | 25 métodos presentes; execução em aparelho ainda não registrada como gate aprovado | `mobile/src/androidTest/` |
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
- Packet documental atual: `integration/codex-pq01-project-state-20260820`.
- `main` permanece snapshot antigo e não é fonte do estado integrado atual.
- A worktree anterior `codex/v0-time-only-checkpoint` contém alterações locais e deve permanecer preservada até integração deliberada.
- Estados de PR descritos pelos relatórios do Atlas são históricos até consulta explícita ao GitHub.

## Drifts e riscos conhecidos

1. O protótipo híbrido calcula candidatos exclusivamente vetoriais, mas a montagem atual dos resultados descarta candidatos sem hit lexical; ele não comprova recall semântico exclusivo no comportamento presente.
2. Algumas assinaturas PGP de artefatos não puderam ser recuperadas; o metadata mantém hashes SHA-256 e registra explicitamente essas exceções para revisão futura.
3. A interface interna do pacote principal após o gate biométrico ainda depende da autenticação do usuário.
4. Evidência histórica de PR, teste ou aparelho não deve ser promovida a estado atual sem nova verificação.

## Gates abertos

- TalkBack em aparelho físico.
- Segundo fabricante/modelo Android.
- PrivacyCover em aparelho.
- Revisão independente de criptografia.
- Testes instrumentados completos em dispositivo/AVD.
- Consentimento e execução do piloto.
- Decisões de produto listadas em `docs/atlas/DECISION-INDEX.md`.

## Instalação atual

- `com.projetofio.app` 0.1.0-dev reinstalado com preservação de dados no Xiaomi M2103K19PG em 2026-08-20.
- APK debug validado: SHA-256 `E83814E640DB8DF349FA2282337F676B4FE284614248E915C803B46E9EE84106`.
- Abertura confirmada até o gate biométrico, sem crash fatal observado.
- A inspeção interna não contorna a impressão digital/PIN e permanece pendente de autenticação do usuário.

## Próximo trabalho autorizado

Seguir `packets/EXECUTION-QUEUE.md`. Após o fechamento de `FIO-PQ-01`, o próximo packet é `FIO-PQ-02`; nenhuma feature deve ser escolhida fora da fila.

## Regras que permanecem

Não adicionar gamificação, feed/social, publicidade, IA interpretativa, profiling, histórico de queries, rede de conteúdo privado ou comando semântico sobre Returns. Não declarar gates humanos aprovados sem evidência e não integrar em `main` sem decisão explícita.
