# SEARCH-ARCHITECTURE — a busca do Fio

**Status:** Research+Implementation (Manus Mission 4, branch `integration/manus-search-20260819`) · **Autor:** Manus AI · **Data:** 2026-08-19

## Definição canônica de capacidades (Manus Mission 4, §1)

O Fio tem três capacidades distintas e o produto deve tratá-las como tais:

| Capacidade | Quem inicia | O que é |
|---|---|---|
| **Guardar** | Usuário | Cria um registro autobiográfico |
| **Encontrar** | Usuário | Procura conscientemente palavras do próprio passado |
| **Reencontrar** | Fio | Devolve algo sem que a pessoa tenha procurado naquele momento |

A **busca pertence a Encontrar**. Returns pertence a **Reencontrar**. As duas experiências nunca se misturam: o mecanismo de devoluções não usa a busca como fonte de seleção, e a busca não produz devoluções.

## Princípio fundamental da busca

> A busca pode encontrar, ordenar e filtrar as palavras do usuário. Ela não responde perguntas autobiográficas no lugar dele.

O Fio **recupera evidência; não produz a conclusão**. Exemplo permitido: a query "quando comecei a pensar em sair daquele emprego?" retorna a data **3 de março de 2024** com o trecho original "Tenho chegado em casa completamente esgotado…" — a pessoa conecta os fatos. Exemplo proibido: a busca responder "Você começou a querer sair do emprego em março de 2024."

## Fronteira com o Return Engine (§45)

```text
Search: query explícita → retrieval → ranking → vários resultados
Return: tempo → eligibility → candidate generation → uma devolução
```

Infraestrutura matemática pode ser compartilhada; **política de produto não**. Não existe `findSimilar()` compartilhado como lógica final dos dois. Semântica nunca é ligada às devoluções nesta missão (§88).

## Regras de política sobre os estados de entrada (§7)

| Estado | Aparece na busca? |
|---|---|
| Never (não devolver) | Sim — nunca impede busca consciente |
| Resting (descansando) | Sim — não esconde da busca consciente |
| Scheduled (programada) | Sim |
| Already returned | Sim — com histórico de retorno |
| Deleted (Excluídos recentes) | Não na busca normal; só na área de apagados |
| Permanently deleted | Nunca |

## História de reencontros (§4–5, §63)

> O Fio preserva não apenas quando algo foi escrito, mas também quando aquela lembrança voltou a atravessar a vida do usuário.

Uma mesma Entry pode voltar várias vezes. O texto sempre pertence à Entry; o reencontro pertence ao evento. **Não duplicar Entry, não guardar cópia do texto no evento.** Este é histórico factual, não interpretação.

O schema atual já possui `ReturnEntity` (tabela `returns`, FK CASCADE sobre a Entry, com `state`, `notified_at`, `opened_at`, `dismissed_at`, `expired_at`, `cancelled_at`, `cancel_reason`), que modela a tentativa de devolução (uma por devolução, com histórico de ciclo de vida). A decisão de S1 (audit) documenta se `ReturnEntity` cobre o "já voltou" da busca ou se uma estrutura mínima de histórico adicional é necessária.

## Privacidade da busca (§9–11)

Toda query, tokens, índice lexical, snippets, embeddings, scores, filtros, histórico de busca e relações entre documentos são **dados privados**. Queries **nunca** são persistidas por padrão, nunca logadas, nunca enviadas a analytics (§10, §67). Nenhum conteúdo sai do aparelho (§31): sem API remota de embedding; analytics remoto continua DECISION REQUIRED.

O principal risco de segurança (§11) é o plaintext: mapear onde o texto descriptografado existe e por quanto tempo no caminho `texto → decrypt → search/index → resultado`. Nenhuma opção (FTS5, índice persistente, scan sob demanda) é prescrita antes do benchmark (§12, §16).

## Notas seladas (§8)

Ver `SEARCH-SEALED-THREAT-MODEL.md`. Antes de qualquer indexação considerar notas seladas, o threat model obrigatório responde: snippet pode vazar texto? índice lexical pode vazar palavras? embedding pode revelar conteúdo? a busca pode revelar existência/data? autenticação desbloqueia só a nota ou uma sessão?

## Estrutura de camadas (construída de baixo para cima, §14)

1. **Phase A — Lexical baseline**: scan/índice textual local, snippets, diacríticos, estabilidade de ranking. Mensurável e excelente antes de anything semântico.
2. **Phase B — Semantic research**: embeddings on-device 100% locais (§29–31), versionados (§39), com reindex idempotente/cancelável/resiliente (§40).
3. **Phase C — Hybrid ranking** (§42): lexical-only / semantic-only / hybrid comparados com métricas; sem peso de "emoção"; returns nunca são boost automático (§43).

## Contratos operacionais

- **Deletion (§49):** exclusão permanente remove embedding, índice e relações de busca. Sem shadow data.
- **Edit (§50):** a busca sempre reflete a versão atual da Entry; nunca texto obsoleto por índice antigo.
- **Crypto (§51):** cipher, key lifecycle, Keystore e encryption boundary **intocados** apenas para facilitar a busca. Se a busca exigir enfraquecimento, parar.
- **Failure (§41):** índice semântico ausente/corrompido → busca lexical continua. Nunca bloquear acesso às palavras.
- **Process death (§52):** privacidade ganha de conveniência; query sensível não precisa ser restaurada se isso aumentar exposição.
- **Screenshot/recents (§53):** a tela de busca respeita PrivacyCover; a query não aparece no app switcher.
- **Clipboard (§54):** nunca copiar a query automaticamente.
- **Kill criterion (§87):** se a semântica não melhorar resultados o suficiente para justificar tamanho/complexidade/memória/bateria/manutenção, manter só lexical. Excluir complexidade é resultado válido.
