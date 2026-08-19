# S1 — Auditoria do modelo de reencontros ("já voltou")

**Autor:** Manus AI · **Data:** 2026-08-19 · **Branch:** `integration/manus-search-20260819`

## Objetivo

O produto exige (docs/05, spec canonical §4–5, §63) que o Fio preserve não apenas quando algo foi escrito, mas também quando aquela lembrança **voltou a atravessar a vida do usuário**. A busca (Encontrar) precisa saber, por Entry, se/quando/quão vezes ela já foi devolvida — e mostrar isso como fato, sem interpretar.

## O que já existe (schema 3, sem migração)

| Artefato | O que modela | Cobre o requisito? |
|---|---|---|
| `ReturnEntity` (tabela `returns`) | **Uma tentativa de devolução**, com ciclo de vida completo: `state`, `windowStart/End`, `scheduledAt`, `notifiedAt`, `openedAt`, `dismissedAt`, `expiredAt`, `cancelledAt`, `cancelReason`, `ageBucket`. FK CASCADE para a Entry. | Parcialmente |
| `EntryEntity.returnCount` / `lastReturnedAt` | Resumo denormalizado: quantas vezes aberta e quando pela última vez. Atualizado em `TimeReturnsService.openReturn()` (quando state ∈ PENDING_STATES, window válida, consent ENABLED, entry ativa e ELIGIBLE). | Sim, para "quantas/quando" |
| `FioDao.loadReturnHistory()` | Toda a tabela `returns` ordenada por `created_at ASC`. Usada por `TimeReturnsService` para reconciliação. | Sim, para detalhe |
| `ReturnState` | SELECTED, SCHEDULED, NOTIFIED, OPENED, DISMISSED, EXPIRED, CANCELLED | Sim |

## Divergências encontradas (fatos vs requisito)

1. **`lastReturnedAt`/`returnCount` contam only OPENED.** `cancelIneligible` não atualiza; devoluções que expiram ou são descartadas abertas não contam. Isso é correto pela política: "voltou" = a pessoa efetivamente reabriu a lembrança. Confirmado em `TimeReturnsService.openReturn()`: update só ocorre no caminho de abertura válida.
2. **Não há um registro "ReturnEvent" separado do histórico de tentativa.** O requisito conceitual ("o reencontro pertence ao evento, não à Entry") é satisfeito pela própria `ReturnEntity`: cada devolução é um registro distinto com sua própria marcação `openedAt`. Nenhuma cópia do texto é guardada no evento (política anti-duplicação respeitada).
3. **Não existe query "qual Entry já voltou e quantas vezes".** O dado existe (returnCount/lastReturnedAt), mas só como campos da Entry; não há projeção para a tela de busca/arquivo. A busca precisará de `returnCount`/`lastReturnedAt` já presentes na Entry decryptada — suficiente para os filtros "já voltou", "nunca voltou", "voltou N vezes".

## Decisão (S1): nenhuma migração de schema

O schema atual **já cobre** o modelo de histórico de reencontros para os fins da busca:

- **"Já voltou"** → `returnCount > 0` (factual, abre = reabertura real).
- **"Quando voltou"** → `lastReturnedAt`.
- **Detalhe por evento** → `ReturnEntity` via `loadReturnHistory().filter { it.entryId == id && it.state == OPENED }` (e outros estados para o histórico completo).
- **Nunca duplicar texto; nunca derivar Entry de evento.** Confirmado: evento referencia `entryId` e não guarda conteúdo.

Adições mínimas necessárias (camada de repositório apenas, schema intocado):

- `ReturnRepository.loadReturnsForEntry(entryId)` — para o card "últimas devoluções" da Entry nos resultados.
- Projeções de busca usam apenas campos já existentes da Entry (`returnCount`, `lastReturnedAt`, `originalCreatedAt`, `content`, `deletedAt`, `returnMode`).

## Consultas canônicas para o search baseline (contrato)

| Consulta | Fonte | Custo |
|---|---|---|
| results(query, filters) | decrypt + scan (baseline lexical) | O(n) sobre entries ativas |
| everReturned() | `returnCount > 0` na Entry | zero custo extra |
| lastReturnedAt(entryId) | campo da Entry | zero custo extra |
| returnsWithin(range) | `ReturnEntity.openedAt` filtrado via `loadReturnsForEntry` | O(k) |
| returnsDetail(entryId) | `loadReturnsForEntry(entryId)` | O(k) por card |

**Riscos e mitigações:** (a) `returnCount` só conta OPENED — documentado; se o produto algum dia quiser contar NOTIFIED, isso é uma mudança de política do engine, não da busca. (b) Reconciliação do engine é a única fonte de atualização dos resumos — a busca nunca escreve em `returnCount`/`lastReturnedAt`. (c) Soft-delete exclui a Entry dos resultados imediatos; purge remove `returns` via CASCADE — nenhuma sombra.

## D12 — draft sobrevive a insert falho (fechado nesta fase)

Contrato observado em `FioService.saveEntry()`/autosave: o draft é persistido **antes** de `insertEntryAndClearDraft`. Teste adicionado: `DraftSurvivesFailedInsertTest` (mock repositório que lança em `insertEntryAndClearDraft`; assert que `loadDraft()` continua retornando o conteúdo). Resultado: teste verde junto dos 101 existentes (102 total após commit).

## Conclusão

Sem migração. Sem schema novo. O "já voltou" da busca é servido por `returnCount`/`lastReturnedAt` + projeção mínima `loadReturnsForEntry`. O risco de policy é contido: a busca **lê** o histórico de reencontros, nunca o escreve.
