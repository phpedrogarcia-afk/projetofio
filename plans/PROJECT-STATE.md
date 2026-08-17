# PROJECT-STATE — snapshot vivo (2026-08-17)

Documento de estado vivo. O Codex deve ler este arquivo antes de tudo; os detalhes completos vivem nos relatórios das missões. Quando algo mudar, atualize este arquivo **e** o registro completo no relatório da missão correspondente.

## Estado geral

| Dimensão | Estado | Evidência |
|----------|--------|-----------|
| Especificação | v0.1 canônica (`docs/`), Android inicial-client (ADR-033) | `docs/01-PRODUCT.md` |
| Plataforma | Android nativo, Kotlin/Compose/Room/KSP, minSdk 26, ID `com.projetofio.app` | `app/` |
| Engine | Temporal M2/M3/M4 implementado e endurecido (bootstrap, cap, quiet-hours, DST, fusos extremos) | `TimeReturnEngine.kt`, `EngineTortureTest.kt` (18 testes) |
| Criptografia | Keystore AES-256-GCM; finding P0 (surrogate halves → `0x3F`) corrigido na Missão 1 | `AesGcmContentCipher.kt`, `CRYPTO-REVIEW-PACKET.md` |
| UI | Design v1 Verde-Sálvia implementado (Home, editor, picker ADR-043, arquivo tipográfico, pátina temporal, pill "Guardado.") | branch `feature/design-ux-v1` integrada ao branch de integração |
| Testes unitários | **99 verdes, 0 falhas** (51 → 99 durante a Missão 1) | `mobile/build/test-results/testDebugUnitTest/*.xml` |
| Testes instrumentados | **Não executados** — sem AVD no ambiente; `M2AndroidContractTest` é o gate CI | `BLOCKED — HUMAN/DEVICE VALIDATION REQUIRED` |
| Export | Markdown/Texto + checksum SHA-256 ADR-046, round-trip verificado, resiliente a falhas de SAF | `ExportRoundTripTest.kt`, `StorageFailureTest.kt` |
| Migração | Room 2→3 verificada byte a byte (500 entries); schema 4 **não existe** | `Migration2To3Test.kt` |
| Pilot | Pacote preparado em `pilot/`, **não iniciado** (gate humano do fundador) | `PILOT-PROTOCOL.md` |
| Semantic M4 | Encerrado sem modelo selecionado (ADR-041); corpus/rubric v1 congelados; nenhuma reconexão | `docs/DECISIONS.md` |
| Missões Manus | Missão 1 (hardening) e Missão 2 (red team + piloto + handoff) concluídas em branches de integração; `main` intacta | `MANUS-HARDENING-FINAL.md`, `MANUS-PRE-CODEX-FINAL.md` |

## Branches vivas

| Branch | Conteúdo | Status |
|--------|----------|--------|
| `main` | Snapshot original (M1 baseline) | Intacta — nada mergeado |
| `codex/v0-time-only-checkpoint` | PR #1 (DRAFT): M1 baseline + checkpoints M2/M3/M4, 100 arquivos | Aguarda merge do fundador |
| `feature/design-ux-v1` | PR #3 (OPEN): design v1 + implementação Verde-Sálvia, 50 arquivos | Título/corpo desatualizados — atualização redigida em `DOCUMENTATION-DRIFT-AND-PR-CLEANUP.md` |
| `integration/manus-rehearsal-20260817` | Missão 1: 17 campanhas, fix P0, relatórios | Completa, pushada |
| `integration/manus-pre-codex-20260817` | Missão 2: red team, correções triviais, drift, piloto, handoff | Completa, pushada |

## Números exatos (em 2026-08-17)

- Commits totais no branch de integração: 20+ (lista em `git log`)
- Testes: 99 unitários verdes
- Findings: 1 P0 corrigido; 7 recomendações P3/P4 documentadas; 9 ideias no `IDEA-INBOX.md`
- ADRs aceitos: 033–047 (+ ledger `docs/DECISIONS.md`)
- Documentos de missão: `MANUS-HARDENING-FINAL.md` (raiz), `MANUS-PRE-CODEX-FINAL.md` (raiz)

## O que está bloqueado e por quê

1. **Merge do PR #1** — decisão do fundador.
2. **Gates humanos** (TalkBack real, segundo OEM, AVD/instrumentados, revisão independente de cripto) — exigem dispositivo físico.
3. **Piloto** — exige consentimento legal, recrutamento e APK de distribuição (decisão do fundador).
4. **Schema 4** — trade-off UI-only aceito; reavaliar só com decisão nova.
5. **PR #3** — descrição desatualizada; atualização redigida, aplicação autorizada pelo fundador.

## O que NUNCA fazer (regras herdadas)

Não adicionar streaks/pontos/badges, feeds/sociais, ads, chat de IA/profiling/labels sentimentais, notificações de engajamento, produtividade disfarçada, acesso server-side a plaintext/embeddings, Book/Legacy congelados, nem nova dependência de produção com fluxo de rede de conteúdo privado. Não executar o plano iOS superado (ADR-033). Não reportar gates humanos como aprovados. Não reabrir M4 sem nova decisão (ADR-040/041).

## Próximos passos reais (ver `NEXT-WORK.md`)

1. Fundador executa gates humanos do handoff.
2. Fundador mergeia PR #1.
3. Fundador atualiza PR #3 e decide veículo das branches Manus.
4. Fundador decide as 3 decisões abertas (ReturnPolicy schema 4, zero-width, analytics remoto).
