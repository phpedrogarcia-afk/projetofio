# Funções básicas de notas visíveis e Ajustes compreensíveis

Status: In progress
Owner: Codex com validação do fundador
Related milestone: M1 Trusted Local Core + refinamento de UX
Relevant decisions: ADR-004, ADR-029, ADR-035, ADR-048, ADR-049

## Outcome

Uma pessoa consegue descobrir, sem adivinhar, como abrir, editar, excluir,
recuperar, buscar e exportar suas notas. A tela Ajustes apresenta primeiro
escolhas humanas e curtas, com cada assunto em uma página própria e sem termos
de engenharia.

## Non-goals

- Não adicionar títulos obrigatórios, tags, pastas, tarefas, checklists ou feed.
- Não adicionar nuvem, conta, compartilhamento ou analytics.
- Não implementar histórico de revisões, mídia, sync ou Legacy neste packet.
- Não promover flags de engenharia de Returns/Import para produção.
- Não criar schema 4 neste packet; a divergência do seletor temporal será
  tratada em packet próprio antes de qualquer afirmação de funcionamento.

## Context read

- `AGENTS.md`
- `docs/01-PRODUCT.md`
- `docs/02-PRINCIPLES.md`
- `docs/03-UX.md`
- `docs/04-FEATURES.md`
- `docs/10-ROADMAP.md`
- `docs/DECISIONS.md`
- `app/mobile/src/main/java/com/projetofio/app/ui/FioApp.kt`
- `app/mobile/src/main/java/com/projetofio/app/ui/FioViewModel.kt`
- serviços e testes de salvar/editar/excluir/recuperar/exportar

## Assumptions and open questions

Verified:

- Edit, soft delete, recovery, permanent delete, search and export exist.
- The current Archive does not explain that opening a row exposes Edit/Delete.
- Settings uses internal milestone/validation language and one long list.
- The temporal picker keeps UI-only `InPeriod`/`OnDate` state; current Entry
  schema stores only `ELIGIBLE`/`NEVER` and `saveEntry()` receives no policy.

Open follow-up:

- The temporal-picker behavior requires a separate schema/Returns decision;
  do not represent it as complete until persistence and engine tests prove it.

## Implementation sequence

1. Publish a basic-note capability matrix with implemented, intentionally
   absent, planned and integrity-debt states.
2. Make Archive explicitly say that a note opens for reading/editing/deletion;
   add a clear affordance and accessible description to each row.
3. Replace the long Settings page with a plain-language overview and focused
   pages for protection, returns, import, export and recently deleted.
4. Remove user-visible engineering terms and explain consequences before
   controls.
5. Add instrumented coverage for editing through the UI and Settings
   navigation/comprehension.
6. Run unit + instrumented suites on API 26; install preserving Poco data and
   confirm launch without crash.

## Privacy and data impact

No new persisted data, network, analytics, dependency, migration or plaintext
surface. Existing export warning remains next to the export action. Existing
fresh authentication remains required for lock changes and permanent delete.

## Tests and acceptance evidence

- Existing 134 unit tests remain green.
- Instrumented test creates, edits, deletes and recovers a synthetic note.
- Instrumented test reaches every Settings destination from the overview.
- TalkBack labels identify opening a note and each Settings destination.
- API 26 UI hierarchy contains no `M1`, `validação` or engineering jargon.
- Poco update preserves app data and reaches the authentication gate.

## Rollback or disable path

UI-only rollback to the previous single Settings list and Archive row. No data
rollback or migration is required.

## Progress log

- 2026-08-20 — current UI and tests audited; feature/service evidence confirms
  basic CRUD/recovery/search/export implementation and discoverability debt.
- 2026-08-20 — temporal picker persistence gap recorded as a separate P0
  integrity debt; not silently included in this no-migration packet.
- 2026-08-20 — debug/validation builds and 134 unit tests passed.
- 2026-08-20 — 28/28 instrumented tests passed on Android 8/API 26. The new
  contract created, edited, deleted and recovered a synthetic note and tested
  system Back on note detail and Settings.
- 2026-08-20 — UIAutomator hierarchy verified the normal and validation
  Settings maps. Returns and Import each expose a focused explanation page;
  no milestone/validation jargon is present in the normal user surface.
- 2026-08-20 — physical update not attempted because the Poco is not currently
  listed by ADB; emulator evidence does not substitute for that gate.

## Final report

Implementation and API 26 evidence are complete. The Poco update/launch and
founder comprehension check remain pending. No schema, dependency, network or
stored-data change was introduced. The temporal-picker P0 remains a separate
decision/migration packet.
