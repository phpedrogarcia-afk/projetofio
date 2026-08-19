# SEARCH-PRIVACY-ARCHITECTURE — onde o plaintext pode existir

**Autor:** Manus AI · **Data:** 2026-08-19 · **Branch:** `integration/manus-search-20260819`

## 1. Ciclo de vida do plaintext na busca

O conteúdo é encriptado em repouso (AES-256-GCM, Keystore). Toda busca precisa, em algum momento, de plaintext. Este documento rastreia cada ponto onde o texto descriptografado existe e por quanto tempo.

| Estágio | Plaintext? | Duração | Fronteira |
|---|---|---|---|
| 1. Leitura da Entry do Room | Envelope criptografado | — | DB (sandbox do app, fora de backup) |
| 2. `cipher.open()` | Sim | Milissegundos por Entry | Memória heap da JVM |
| 3. Tokenização/normalização | Sim (tokens) | Duração da query | Memória heap |
| 4. Matching/ranking | Sim (tokens + scores) | Duração da query | Memória heap |
| 5. Construção do snippet | Sim | Duração da query + exibição | Memória heap → render |
| 6. Índice persistente (se houver) | Sim (estruturas derivadas) | Permanente até purge | **Disco — risco novo** |
| 7. Embedding (se houver) | Derivado do plaintext | Persiste com o vetor | Disco/memória — risco novo |

Regras derivadas: (a) plaintext nunca chega a disco sob qualquer forma que não seja o envelope criptografado; (b) tokens de query existem apenas em memória durante a execução; (c) snippet nunca é persistido, nunca entra em cache de imagem/analytics, nunca vai ao `Recents`; (d) `PrivacyCover` cobre a tela de resultados; (e) a query não vai ao clipboard.

## 2. Opções para o baseline lexical

| Opção | Descrição | Privacidade | Custo/performance | Reindexação | Decisão |
|---|---|---|---|---|---|
| **A. Scan sob demanda** | Descriptografar + tokenizar + match em memória a cada query | Excelentes: plaintext confinado ao ciclo da query; nada novo no disco | O(n) por query; ~1 ms/entry esperado; 10k entries ≈ baixo dígito de segundos com tokenização por streaming | N/A — sempre atual | **V1 baseline** |
| B. FTS5 embutido | Tabela FTS da Room com conteúdo em plaintext (ou coluna FTS shadow) | Ruins: plaintext persiste em disco fora do envelope; exige reavaliação de backup/extração | Muito rápido | Automática por trigger; edit/delete propagam | Rejeitada p/ V1 (vaza selado + quebra fronteira crypto) |
| C. FTS com coluna criptografada + tokenização no indexante | FTS indexa tokens normalizados criptografados individualmente | Fraca: busca por termo exige comparar com todos os tokens criptografados (linear), perdendo o benefício | Não melhor que A | Manual | Rejeitada |
| D. Índice invertido protegido (mesma chave do envelope) | Estrutura própria: token → set de IDs, encriptada com a chave de conteúdo | Boa: ciphertext no disco, mas a chave única é a mesma do conteúdo (root compromete tudo igualmente); manutenção e versionamento custosos | Rápida em leitura; complexidade de edição alta | Edit/delete exigem delta de índice idempotente | Candidata a V1.1 se scan for insuficiente |

**Decisão V1: Opção A.** Justificativa: (1) o benchmark de escala (S11) determinará se scan em memória atende o alvo de latência — a arquitetura não presume FTS; (2) A mantém o plaintext dentro da mesma fronteira da exibição de Entry (ciclo de vida curto, heap apenas); (3) notas seladas são excluídas por contrato do threat model; (4) zero schema change, zero migration; (5) totalmente removível — o search é um serviço novo isolado.

## 3. Shadow data e purge

Toda estrutura derivada de conteúdo (tokens, scores, embeddings) é **shadow data**: deve morrer quando a fonte morre. Contratos:

1. **Exclusão permanente** (`purgeEntry`): remove a Entry; o scan nunca mais a vê. Não há índice persistente para limpar em A.
2. **Edição**: `updateEntry` reescreve o envelope; a próxima query lê a versão nova. Contrato "o índice sempre reflete a versão atual" é satisfeito por construção em A.
3. **Soft-delete**: `deletedAt != null` exclui da busca imediata; `purge_after` garante remoção física.
4. **Import rollback**: `rollbackImport` marca entradas e apaga returns (CASCADE) — entradas rolled-back somem da busca no mesmo ciclo.

## 4. Queries nunca persistidas

A query existe como `String` em memória durante a execução e morre com ela. Não há `search_history` table, não há "Pesquisas recentes", não há sugestões derivadas de queries anteriores. Se analytics local operacional for aprovado (DECISION REQUIRED), apenas os quatro eventos não-conteúdo (`search_executed`, `result_count_bucket`, `search_mode`, `latency_bucket`) — nenhum termo, nenhum ID de resultado.

## 5. Semântica futura (protótipo apenas)

O protótipo semântico (feature flag) usa embeddings **em memória ou em arquivo cifrado com a mesma chave de conteúdo**, versionado (`embeddingModelVersion`). Reindex é idempotente e cancelável. Embeddings de notas seladas são proibidos (threat model, Q3). Se algum dia embeddings viverem no disco de forma útil, será necessário envelope separado por política de acesso — nova decisão.

## 6. Resumo das decisões

| Item | Decisão | Tipo |
|---|---|---|
| Baseline lexical | Scan sob demanda (Opção A) | Pesquisa (Manus), reversível por benchmark |
| Notas seladas na busca | Conteúdo nunca; contagem opcional atrás de toggle (DECISION REQUIRED do fundador) | DECISION REQUIRED |
| Persistência de queries | Nunca | Pesquisa (Manus), alinhada ao §10 |
| FTS5 / índice persistente | Rejeitados para V1 | Pesquisa (Manus) |
| Analytics de busca | Local somente, 4 eventos não-conteúdo; remoto = DECISION REQUIRED | DECISION REQUIRED (remoto) |
| Embeddings de seladas | Proibidos até envelope separado | Pesquisa (Manus), threat model Q3 |
