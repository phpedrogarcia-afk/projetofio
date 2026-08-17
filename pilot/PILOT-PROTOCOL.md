# PILOT-PROTOCOL — Fio

**Status:** Prepared, not started. **This document plans a pilot; it does not authorize one.**
Iniciar o piloto é gate humano do fundador (participantes, consentimento, APK de distribuição).
**Branch de referência:** `integration/manus-pre-codex-20260817` · 99 testes unitários verdes · finding P0 da Missão 1 corrigido.

## 1. Objetivo

Descobrir, antes de qualquer investimento de produção, se a tese central do Fio resiste ao contato com pessoas reais. O piloto não existe para validar gosto estético nem para medir engajamento — existe para verificar a promessa que diferencia o Fio de um diário comum: **a devolução temporal espontânea de palavras autobiográficas produz uma experiência qualitativamente diferente de simplesmente reler uma nota antiga.**

## 2. Hipótese central

> Uma devolução de palavras autobiográficas depois de distância temporal produz uma experiência significativamente diferente de simplesmente acessar uma nota antiga?

Esta hipótese é a única razão de ser do piloto. Toda métrica, entrevista e critério de parada abaixo serve a ela. Não testamos "as pessoas gostam do Fio?" — isso seria métrica de vaidade e trairia o princípio 4 (silêncio sobre engajamento).

## 3. Duração e tamanho

A duração mínima é ditada pelo próprio mecanismo do produto: a primeira oportunidade real de devolução com uma única entrada ocorre entre 30 e 45 dias (bootstrap table do engine). Portanto o piloto dura **mínimo 8 semanas** (2 semanas de familiarização + janela 30–45d + 2 semanas de entrevistas e análise). Tamanho inicial: **8–12 participantes** (4–6 por coorte), suficiente para entrevistas em profundidade e insuficiente para generalização — o piloto é qualitativo com um número âncora.

## 4. Coortes

| Coorte | Perfil | Objetivo de aprendizado |
|--------|--------|--------------------------|
| A — do zero | Instala limpa, sem histórico | O loop write→wait→return se explica sozinho sem tutorial? A primeira cápsula ("Guardado. O tempo cuida do resto.") é compreendida? |
| B — com histórico importado | Importa diário antigo (mín. 50 entradas, idealmente com datas antigas) | O bootstrap do engine funciona sobre vida real? A devolução de memória genuinamente antiga ressoa mais que a de memória recente? |

A coorte B só se ativa **quando a importação estiver pronta e segura** (a import Markdown/TXT existe; a importação de arquivos grandes e a deduplicação são o critério técnico de ativação). A coorte B estuda o bootstrap sem usar palavras de terceiros — o participante importa o próprio material.

## 5. Métrica principal

**Meaningful User Rate (MUR):** percentual de participantes que relata, na entrevista, pelo menos uma devolução que considerou significativa dentro da janela do piloto. MUR é declarado depois da entrevista (codificação 0–3), nunca calculado a partir de cliques — a pergunta de ressonância na interface existe apenas como memória da entrevista, não como instrumento de medição.

### Anti-métricas (proibidas como objetivo)

O piloto **não otimiza para**: tempo de tela, sessões por dia, streaks, quantidade máxima de notas, DAU, frequência de notificação, nem cliques a qualquer custo. Qualquer relatório de piloto que apresente essas variáveis como sucesso terá traído o desenho.

## 6. Feedback de evento (interface)

Na primeira devolução após o fechamento, uma única pergunta factual, uma vez:

> **Isso significou algo para você?** · Sim · Não

Um toque por opção; sem escala, sem justificativa obrigatória; a pergunta só reaparece em outra devolução (nunca repetida para o mesmo retorno); a resposta não altera o comportamento do app além do registro privado local. O design do registro segue a seção 8.

## 7. Codificação qualitativa (interna, nunca exibida)

| Código | Significado |
|--------|-------------|
| 0 — irrelevante | A devolução não agregou nada |
| 1 — interessante | Notável, mas sem impacto |
| 2 — ressonante | Tocou algo verdadeiro e atual |
| 3 — transformador | Alterou a relação da pessoa com aquela memória |

A codificação é ferramenta de análise das entrevistas; nunca é exibida ao participante nem persistida no app.

## 8. Analytics minimalista (events locais, exportáveis)

Somente eventos operacionais, **sem texto** (a regra do princípio 12 e a fronteira local-first): `entry_saved`, `return_scheduled`, `return_candidate_selected`, `return_delivered`, `return_opened`, `return_feedback_yes`, `return_feedback_no`, `return_suppressed`, `return_never_enabled`, `return_rest_started`. Sem timestamps de escrita, sem conteúdo, sem embeddings, sem localização além do fuso já armazenado no banco.

**Fronteira de privacidade:** estes eventos permanecem **locais e exportáveis** (arquivo de log com checksum, mesma promessa ADR-046 do export de conteúdo). Nenhum servidor, nenhum endpoint remoto, nenhuma conta. O pesquisador coleta via export local no momento da entrevista. Adicionar instrumentação remota (cloud) é **DECISION REQUIRED** — quebraria o princípio 7 e a privacidade por arquitetura; o desenho atual não precisa dela.

Matriz campo a campo em `pilot/ANALYTICS-PRIVACY-MATRIX.md`.

## 9. Consentimento (texto preparado, revisão pendente)

Texto pronto para revisão em `pilot/CONSENT-DRAFT.md`. Marcado como **HUMAN / LEGAL REVIEW REQUIRED** — não é aconselhamento jurídico. Cobre: o que é o Fio; objetivo do piloto; o que é coletado (eventos operacionais sem texto + entrevistas gravadas com autorização separada); o que nunca é coletado (conteúdo, texto, chaves, biometria); direito de saída a qualquer momento sem justificativa; exclusão total de dados sob demanda; contato do responsável.

## 10. Segurança do participante

O Fio não é apresentado como terapia, tratamento, prevenção ou aconselhamento psicológico. O participante pode pausar devoluções, descansar, não devolver, excluir qualquer entrada e sair do piloto a qualquer momento, sem justificar e sem custo de dados. Nenhuma pergunta do piloto indaga sobre conteúdo íntimo; a entrevista fala da **experiência**, nunca do texto.

## 11. Critérios de aborto (definidos antes de começar)

| Classe | Condição | Ação |
|--------|----------|------|
| P0 | Perda ou corrupção de qualquer entrada | Abortar imediatamente; analisar causa-raiz; nenhum participante adicional até correção e reteste |
| P0 | Plaintext exposto (log, export não cifrado, notificação com conteúdo) | Abortar imediatamente; correção + hardening re-testado (campanhas da Missão 1 são o padrão) |
| P0 | Notificação revela conteúdo privado sem consentimento | Abortar; revalidar canal e título ("Algo seu voltou." sem payload) |
| P1 | Devoluções duplicadas sistematicamente | Pausar novas devoluções; corrigir; revalidar contra `EngineTortureTest` |
| P1 | Crash que bloqueia o uso principal | Pausar coorte afetada; correção + 99 testes verdes antes de retomar |

Abortar uma coorte não encerra o piloto: a decisão é de retomada, registrada no log do piloto.

## 12. Suporte e eventos

Uma conversa de check-in por participante (assíncrona, sem invasão), disponível o canal de suporte documentado no consentimento. Entrevistas semi-estruturadas por `pilot/INTERVIEW-GUIDE.md` ao final da janela.

## 13. Critérios de parada do piloto

O piloto termina quando: (a) a janela de 8 semanas se esgota; (b) um critério de aborto P0 é atingido; ou (c) saturação qualitativa — novas entrevistas deixam de produzir códigos novos na escala 0–3. O resultado é um relatório de decisão: **aprovar iteração**, **aprovar com correções**, ou **pausar o produto**. O piloto nunca termina "por engajamento".
