# SEARCH-SEALED-THREAT-MODEL — busca e notas seladas

**Autor:** Manus AI · **Data:** 2026-08-19 · **Branch:** `integration/manus-search-20260819`

Este documento existe porque a missão proíbe indexar ou pesquisar notas seladas antes de um threat model mínimo. Ele registra o estado atual do produto, os vetores de exposição que a busca cria, e as decisões defensáveis. Enquanto houver **mais de uma solução defensável** para qualquer item abaixo, o resultado é **DECISION REQUIRED** e a busca não toca em notas seladas.

## Estado atual de "selado" no M1

O campo `accessPolicy` do modelo de dados prevê `sealed` em V1; no M1 operacional, selado funciona como uma **fronteira de autorização local**: a entrada selada exige reautenticação antes de exibição, nunca aparece em notificação ou preview desfocado, e não é aberta por simples navegação. Importa para este threat model:

1. A nota selada está **encriptada com a mesma chave de conteúdo** das demais entradas — o "selo" é uma camada de **política de acesso local (UI + reauth)**, não um envelope criptográfico separado.
2. A exibição do texto selado ocorre no mesmo caminho de descriptografia usado por toda a app (`ContentCipher.open`).
3. Não há hoje isolamento de chave por entrada; todo conteúdo compartilha a chave AES-256-GCM do Keystore.

## As seis questões obrigatórias

### Q1. O snippet de busca pode vazar texto selado?

**Sim, se a busca tratar selado como padrão.** O snippet é derivado diretamente do plaintext descriptografado; se uma nota selada for indexada/pesquisável com a UI desbloqueada apenas uma vez, o snippet aparece na lista de resultados sem reautenticação, destruindo o selo. **Decisão mínima:** notas seladas **nunca** contribuem com snippet, termo ou hit para a busca enquanto o selo estiver ativo. A busca lista o hit apenas como metadado opaco ("1 nota selada corresponde"), ou nem lista.

### Q2. O índice lexical pode vazar palavras seladas?

**Sim, persistentemente.** Um índice lexical persistente contém os termos da nota selada em plaintext (ou estrutura reversível) no disco. Um atacante com acesso ao sistema de arquivos do app (root, backup não excluído, extração) recupera as palavras seladas **sem precisar da chave do Keystore** — o selo inteiro desaparece. Por isso, o baseline lexical **não indexa notas seladas**; o scan sob demanda (Opção A) mantém o plaintext dentro do mesmo ciclo de vida da exibição: existe enquanto a query roda e some quando termina.

### Q3. O embedding pode revelar conteúdo selado?

**Sim, em vários graus.** O vetor é derivado do texto selado; se indexado em storage compartilhado com notas padrão, a consulta por embedding de uma frase conhecida aproxima o conteúdo selado semanticamente — sem nunca ter descriptografado. Mesmo "protegido por encriptação do mesmo envelope" (§46), um modelo de embeddings não tem fronteira de acesso: a comparação vetorial é feita *antes* de qualquer reautenticação. **Decisão mínima:** embeddings de notas seladas não são construídos no V1; qualquer path semântica futura precisa de envelope separado por política de acesso — e isso é DECISION REQUIRED.

### Q4. A busca pode revelar existência/data de uma nota selada?

**Sim, por metadados.** Se o resultado da busca mostrar "nota selada de 12/03/2025", a própria existência e o timestamp vazam para quem observa a tela. Para o dono do aparelho isso é aceitável (ele já sabe que existe); o risco real é **shoulder surfing** e **device sharing**. **Decisão mínima:** hits em notas seladas não expõem data nem snippet — apenas contagem opcional na própria tela (que já exige reauth para abrir).

### Q5. A autenticação desbloqueia só a nota ou uma sessão?

No M1, a reautenticação de uma nota selada **não cria sessão estendida** — cada exibição pede fresh authorization (parâmetro de design do app lock/sealed). A busca, porém, é um **mecanismo de múltiplas leituras**: um loop de resultados retornaria vários hits selados por uma única reauth ou por nenhuma. **Decisão mínima:** qualquer acesso ao conteúdo de nota selada pela busca passa pelo mesmo contrato de exibição selada (reauth por acesso), e a busca não faz bulk-open.

### Q6. Se a busca rodar com o app desbloqueado, o que muda para o selado?

O app lock cobre a abertura do app; o selo cobre a nota individual. A busca é acessível **dentro do app desbloqueado** — portanto o contexto de risco é o app já aberto (shoulder surfing, empréstimo momentâneo do aparelho). Nesse contexto, o selo é a única barreira restante, e o threat model **Q1–Q5** mostra que um índice persistente a derrubaria por completo.

## Resultado do threat model

| Vetor | Risco no baseline lexical (scan sob demanda) | Risco com índice persistente | Risco com embeddings |
|---|---|---|---|
| Snippet em plaintext | Confinado ao ciclo da query | Persiste no disco | — |
| Termos do índice | Não existem para selado | Vaza palavras sem Keystore | — |
| Vetores semânticos | Não construídos | — | Vaza similaridade sem descriptografar |
| Existência/data | Ocultos (count opcional) | Ocultos | Ocultos |
| Bulk-open | Impossível por contrato | Impossível por contrato | Impossível por contrato |

**Decisão (defensável única para o V1):** o baseline lexical **exclui notas seladas da indexação e dos resultados por conteúdo**. O máximo permitido é um **metadado opaco de contagem** ("N seladas correspondem"), visível somente se o usuário explicitamente incluir seladas na busca — e mesmo assim sem termo, data ou snippet. Embeddings de seladas permanecem proibidos até existir envelope separado por política de acesso. Se algum futuro design criar isolamento criptográfico real do selo, este documento precisa ser reaberto (nova decisão, não edição desta análise).

**DECISION REQUIRED pendente:** contagem opaca visível ou seladas totalmente invisíveis à busca. Recomendação da pesquisa (Manus): **invisíveis por padrão; contagem opcional atrás de toggle explícito de busca** — mas como existem duas opções defensáveis, o fundador decide antes da implementação.
