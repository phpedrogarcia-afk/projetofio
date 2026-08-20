# FIO-PQ-01 — sincronizar o PROJECT-STATE com M4 e o Atlas

**Status:** DONE
**Tipo:** doc
**Branch:** `integration/codex-pq01-project-state-20260820`
**Evidência mínima exigida:** `plans/PROJECT-STATE.md` descreve o HEAD Atlas, distingue evidência histórica de validação atual e não declara gates humanos como concluídos.

## 1. Objetivo

Corrigir o drift conhecido de `plans/PROJECT-STATE.md` após as Missões 4 e 5. O snapshot deve registrar a busca lexical de produção, o protótipo semântico removível, a transferência pelo Atlas e o estado real das branches sem alterar código do aplicativo.

## 2. Contrato

- Preservar todos os invariantes de `docs/atlas/INVARIANTS.md`.
- Registrar Search e Returns como capacidades conceitualmente separadas.
- Separar resultados históricos de testes de resultados reproduzidos nesta execução.
- Não modificar código, arquitetura, schema, dependências, produto, gates humanos ou `main`.

## 3. Contexto técnico

Arquivos permitidos neste packet:

- `plans/PROJECT-STATE.md`
- `packets/FIO-PQ-01.md`
- `packets/EXECUTION-QUEUE.md`

Room permanece no schema 3. Nenhuma migration, flag ou dependência é criada.

## 4. Critérios de aceitação

1. O snapshot identifica M1–M5 e o HEAD de transferência do Atlas.
2. A busca lexical aparece como baseline de produção; o braço semântico, como protótipo não conectado.
3. Os 134 testes unitários verdes são identificados como evidência histórica de 2026-08-19 até nova execução local válida.
4. Gates `HUMAN/DEVICE` permanecem abertos na ausência de evidência.
5. A fila marca este packet como concluído e aponta `FIO-PQ-02` como próximo.

## 5. Riscos e portas de escape

- Parar se documentação e código divergirem e registrar o drift, sem reconciliar por inferência.
- Não declarar testes verdes se o Gradle não executar a suíte.
- Não alterar branches antigas com trabalho local nem integrar em `main`.

## 6. Evidence log

| Data | O que | Output |
|---|---|---|
| 2026-08-20 | Código e Atlas comparados no HEAD de transferência | Room 3; lexical conectada; semântica removível; 134 testes unitários presentes |
| 2026-08-20 | Reexecução unitária tentada | Bloqueada antes dos testes pela verificação de dependências do Gradle; resultado histórico não promovido a validação atual |
| 2026-08-20 | Snapshot sincronizado | `plans/PROJECT-STATE.md` atualizado sem mudança no app |
