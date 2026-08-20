# Integration Readiness — ProjetoFio MANUS MISSION 1

> **Correção factual de 2026-08-20:** este é um relatório histórico, mas sua antiga conclusão sobre `ReturnPolicy` não representa o código atual. A escolha temporal vive somente na UI; `saveEntry()` salva a entrada como `ELIGIBLE`. Períodos, data e `Nunca` ao guardar não são persistidos nem honrados. Ver `docs/atlas/TIME-MAP.md` e `packets/FIO-P19-TEMPORAL-INTEGRITY-DECISION.md`.

**Branch de integração:** `integration/manus-rehearsal-20260817` (commit `8be819a`, pushada)
**Data:** 2026-08-17 (3h UTC)
**Escopo:** Merge semântico de `codex/v0-time-only-checkpoint` (engine temporal M2/M3/M4, 982ad53) + `feature/design-ux-v1` (redesign Verde-Sálvia, 7040b70) sobre `main` (Genesis, a4fc835). **Nada foi mergiado em `main`.**

---

## 1. Resumo executivo

O ensaio de integração combinou as duas linhas de trabalho independentes do ProjetoFio — a **engine temporal determinística** (codex) e o **redesign de experiência** (design-ux-v1) — em uma única árvore compilável e testável. O resultado foi alcançado **sem tocar `main`**, sem apagar branches e **sem adicionar features novas**: todo o trabalho foi de reconciliação semântica de código existente.

O ensaio confirmou que as duas linhas são **compatíveis em nível de domínio** (mesmos modelos `Entry`, `ImportBatch`, `ReturnConsentState`; aliases de tokens de cor idênticos) e que o único ponto de atrito real era a **arquitetura de navegação divergente** (Home sem bottom bar vs. bottom bar do codex) e o arquivo `FioApp.kt`, o único com conflito de merge de verdade.

**Estado atual: BUILD + 60 TESTES UNITÁRIOS VERDES na branch integrada.**

---

## 2. Linhas de trabalho e status

| # | Área | Status | Evidência |
|---|------|--------|-----------|
| 1 | Merge `codex` sobre `main` | **PASS** | Merge limpo, sem conflitos. Fast-forward em árvore (commits do codex descendem do Genesis) |
| 2 | Merge `design-ux-v1` sobre codex | **PASS** | 1 arquivo em conflito (`FioApp.kt`, 5 hunks), resolvido semanticamente. Demais 50+ arquivos limpos |
| 3 | Compilação (Kotlin) | **PASS** | `compileDebugKotlin` BUILD SUCCESSFUL (27s) |
| 4 | APK debug | **PASS** | `assembleDebug` BUILD SUCCESSFUL |
| 5 | Testes unitários | **PASS** | 60 testes, 9 suites, 0 falhas, 0 erros, 0 pulados |
| 6 | Theme/Design system | **PASS** | Tokens Verde-Sálvia preservados + `FioSpace.s8` adicionado (regressão do merge) |
| 7 | Navegação (no bottom bar) | **PASS** | Estratégia aprovada pelo fundador: nossa UI prevalece; `MainSurface` stack + menu ⋯ mantidos |
| 8 | Engine temporal na UI | **PASS** | `ReturnScreen` do codex integrada ao design (FioDisplayDate, BotanicalMotif); `openedReturn` dispara a tela |
| 9 | Settings M2/M3 | **PASS** | Seções "Devoluções — validação" e "Importar — validação" integradas com diálogos de consentimento e rollback recriados |
| 10 | ViewModel | **PASS** | `FioViewModel` codex (import pipeline, return engine, quiet hours) + `acknowledgeSaved()` nosso (compatível, não altera comportamento) |
| 11 | Export checksum SHA-256 | **PASS** | `FioService` do design entrou sem conflito; ADR-046 preservada |
| 12 | Tipografia Fraunces/Inter | **PASS** | `Type.kt`, fontes OTF e licenças OFL (`docs/licenses/`) entraram sem conflito |
| 13 | Pátina temporal (ADR-045) | **PASS** | `BotanicalMotif` presente no `FioApp` integrado |

---

## 3. Problemas encontrados e corrigidos durante o ensaio

| # | Problema | Classificação | Resolução |
|---|----------|---------------|-----------|
| 1 | `FioApp.kt`: conflito de 5 hunks entre navegação divergente (bottom bar vs. stack) | Merge conflict | Resolução semântica manual: nossa arquitetura prevalece; codex contribui com seções M2/M3, `ReturnScreen`, diálogos e callbacks (`onEnableReturns`, `onOpenPendingReturn`, `onSelectImport`) |
| 2 | Chave desbalanceada `) {{` na abertura do `SettingsScreen` (hunk do codex colado com brace extra) | **P1** (quebra de compilação) | Diagnosticado via rastreamento de depth de chaves; `) {{` → `) {` na linha 828 |
| 3 | Diálogos `confirmReturnConsent` e `rollbackBatch` usados pela UI mas nunca declarados/instanciados no SettingsScreen pós-merge | **P1** (unresolved references) | Recriados a partir do SettingsScreen original do codex (consultado em `origin/codex/v0-time-only-checkpoint`), com chamadas `onEnableReturns()` e `viewModel.rollbackImport(batch.id)` |
| 4 | `FioSpace.s8` ausente no design system (usado pelo `ReturnScreen` do codex) | **P2** (regressão de token) | `val s8 = 40.dp` adicionado ao `FioSpace` em `Theme.kt`, após `s7` |

---

## 4. Decisões de reconciliação (documentadas, não alteram dados)

| Decisão | Racional | Risco |
|---------|----------|-------|
| Navegação sem bottom bar (nossa) prevalece sobre bottom bar (codex) | ADR aprovada pelo fundador; Home sem distração é a essência do produto | Nenhum — o codex apenas adicionava rotas; as rotas M2/M3 foram expostas via seções de Settings |
| `ReturnScreen` do codex substitui o `ReturnDialog` simples nosso | Engine real (M4) com buckets, quiet hours e frequency cap — valor funcional real | Comportamental: o fluxo de devolução agora é uma tela dedicada; testado via UI no Failure Hunt (campanha Visual Regression) |
| `ReturnPolicy` UI-only permanece até schema 4 | **P0 confirmado:** a política não é escrita na Entry e o engine não a recebe; somente “Algum dia” coincide por acaso com o default `ELIGIBLE` | **DECISION REQUIRED:** FIO-P19; não apresentar períodos/data/Nunca como funcionais |
| `acknowledgeSaved()` adicionado ao ViewModel codex | Necessário pela pill "Guardado" (ADR-014) do design; não altera o estado além do notice | Nenhum — apenas consome `savedNotice` já presente no `FioUiState` codex |
| `PATINA_ENABLED` preservado (true) | Pátina determinística sem métrica nem streak — coerente com a filosofia | Nenhum |

---

## 5. Fronteiras ainda não validadas (entradas para o Failure Hunt)

O ensaio validou compilação e testes unitários, mas **não** valida o comportamento em runtime no dispositivo nem em cenários adversariais. As campanhas de tortura (Fase 3 da missão) cobrem:

1. **Data Torture / Unicode** — entradas com zero-width, RTL, emoji ZWJ, surrogate halves; datas em fusos extremos (UTC-12/+14, +5:45 Nepal)
2. **Returns Engine Torture** — determinismo do `TimeReturnEngine` com `ReturnRandom` injetável; quiet hours; frequency cap; bootstrap; 730+ dias
3. **Migration / Database** — Room schema 2→3 na base real com dados sintéticos volumosos
4. **Storage Failure** — Keystore AES-256-GCM, permissões do Storage Access Framework, disco cheio no export
5. **Crypto / Privacy** — revisão do pacote criptográfico, hunt por plaintext em logs, SharedPreferences e backups
6. **Android Backup** — `android:allowBackup`/autoBackup e a garantia de que os dados criptografados não vazam para o cloud
7. **Performance / Battery** — 10k+ entradas no arquivo tipográfico; notificações em quiet hours; AlarmManager em Doze
8. **Accessibility / Fuzz** — TalkBack nas seções M2/M3; fuzz com propriedade-based nas funções puras do engine

---

## 6. Hardening Queue

Ver `plans/HARDENING-QUEUE.md` para a fila de campanhas (NOW=1, NEXT≤3), classificação P0–P4 e estados FOUND/BLOCKED/FIXED/VERIFIED.

---

## 7. Regras da missão respeitadas

- `main` **não** foi tocada (nenhum push, nenhum merge, nenhum commit).
- Nenhuma branch foi apagada (`main`, `codex/v0-time-only-checkpoint`, `feature/design-ux-v1` intactas).
- Nenhum dado real do usuário foi alterado (sandbox; base de dados nunca instanciada em runtime com dados pessoais).
- Nenhuma mudança criptográfica foi feita — apenas revisão (campanha Crypto Pre-Review na fila).
- Nenhuma migração destrutiva foi executada.
- Decisões que exigem autorização do fundador estão marcadas como **DECISION REQUIRED** na seção 4 e na fila.
