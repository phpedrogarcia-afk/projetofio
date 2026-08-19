# PATTERN-CARDS — padrões consolidados de fora que o Fio pode reutilizar

Cada cartão: nome, fonte, estado no Fio, o que fazer se um dia adotarmos. Regra da missão: **não construir nada aqui**; estes são cartões prontos para o Codex executar quando o fundador aprovar.

## 1. SCAN-ON-DEMAND (busca sem índice)

Fonte: escolha de arquitetura interna (Option A, `SEARCH-PRIVACY-ARCHITECTURE.md`). Estado: **em produção** (M4 lexical). Se escalar: FTS5 permanece rejeitada (plaintext indexado = fronteira nova); vetorial precisaria de índice → decisão ADR obrigatória + boundary crypto do índice.

## 2. ENCRYPT-BEFORE-DISK (entry-at-a-time GCM + AAD binding)

Fonte: ADR-023/035; praticado também por Mini Diarium [5]. Estado: produção. Cartão só para manutenção: rodar a cada upgrade de Room/AGP; `AndroidKeystorePersistenceTest` (instrumentado) é a evidência que falta.

## 3. SINGLETON DRAFT (exatamente um rascunho criptografado)

Fonte: ADR-029 + D12. Estado: produção, testado. Se evoluirmos para edição multi-ponto: ADR novo (hoje o editor é o único writer).

## 4. CONSENT-GATED REENGAGEMENT (devolução só com consentimento explícito)

Fonte: ADR-011/027; contraponto do mercado = resurfacing automático (Google Photos/Facebook, criticado por conteúdo indesejado). Estado: produção. Padrão emergente de responsabilidade no mercado: controles opt-out por canal [3] [9]. Fio vai além: opt-**in**.

## 5. FREQUENCY CAP + BOOTSTRAP WAIT (engine temporal)

Fonte: motor próprio (cap 7d, bootstrap 0/30/60/90). Estado: produção. Padrão análogo em notificações de apps: daily digest em vez de notificação por evento. Se quisermos "mais devolução" um dia: elevar cap é decisão de produto (muda a essência).

## 6. RRF FUSION (reciprocal rank fusion k=60)

Fonte: IR clássico (Cormack et al. 2009); prototipado em `SemanticSearchPrototype.kt`. Estado: protótipo testado, não embarcado. Cartão: se embarcar semântica, RRF já está pronto; não criar pesos treinados.

## 7. FEATURE-FLAGGED PROTOTYPE (EmbebbedSearch via flag, removível)

Fonte: prática interna (TIME_RETURNS_ENGINEERING_ENABLED, LOCAL_IMPORT_ENGINEERING_ENABLED). Estado: padrão estabelecido. Cartão: M4-semantic usa o mesmo mecanismo quando for hora (ADR próprio).

## 8. ATOMIC IMPORT WITH ROLLBACK (preview → commit atômico → rollback idempotente)

Fonte: ADR-039. Estado: produção. Cartão de manutenção: fingerprints HMAC para dedupe; se adicionarmos import de outras fontes (Day One JSON, jrnl), o contrato rollback permanece.

## 9. EXPORT-AS-DEPARTURE (longevity v1.0 + checksum + SAF)

Fonte: ADR-046; precedentes: Standard Notes offline tool [6], Mini Diarium export [5]. Estado: produção. Cartão: versionar o formato (v1.1 se mudar) sem quebrar leitores v1.0.

## 10. SINGLE-ACTIVITY STATE NAVIGATION

Fonte: escolha interna (sem NavController). Estado: produção. Cartão: se o app crescer (>3 superfícies profundas), considerar NavHost com grafo — decisão ADR, custo de migração baixo hoje, alto depois.

## 11. QUIET-HOURS WITH MIDNIGHT WRAP

Fonte: motor próprio + java.time. Estado: produção. Cartão de manutenção: DST spring-forward testável em aparelho (lacuna E5 documentada em TIME-MAP).

## 12. FACTUAL SEARCH SNIPPETS (SelectionContainer, original text, sem highlight generativo)

Fonte: escolha de produto (retrieve, not interpret). Estado: produção. Cartão: se semântica embarcar, snippet continua original — nunca gerar texto.

## 13. NOTIFICATION BODY-FREE REDELIVERY (canal low, corpo genérico)

Fonte: ADR-011; Diarly tem o oposto (notificação com preview) [3]. Estado: produção. Cartão: padrão do Fio é mais restritivo que o mercado — manter.

## 14. MANUAL DI (FioGraph lazy)

Fonte: escolha interna. Estado: produção. Cartão: se módulos crescerem, Hilt/Dagger é candidato `USE LIBRARY`; trade-off: simples hoje, custo de migração cresce com a base.

## 15. ON-DEVICE EMBEDDING VIA LiteRT (EmbeddingGemma)

Fonte: Google [10] [11]. Estado: pesquisa. Cartão de execução futura: LiteRT API + sentencepiece tokenizer; RAG library do Google AI Edge como alternativa; medir ΔRSS do app, não só RSS do modelo; licenciamento HF como stop condition.
