# DOC-DRIFT-AUDIT — docs que não batem com o código (verificado linha a linha)

**Método:** cada claim relevante dos docs 01-11, search/*, plans/* foi conferida contra o código real da branch `integration/manus-search-20260819` em 2026-08-19. Classificação: **OK** (bate), **SPEC-AHEAD** (doc é canônico aspiracional, código ainda não implementa — legítimo, mas deve ser sabido), **DRIFT** (doc desatualizado — corrigir), **SUPERSEDED** (história, não usar).

## 1. Resultados por documento

| Doc | Veredito | Detalhe |
|---|---|---|
| docs/01-PRODUCT.md | OK | Fundamentos; nenhum claim de implementação |
| docs/02-PRINCIPLES.md | OK | Filosofia; intemporal por desenho |
| docs/03-UX.md | OK | Design source-of-truth (PR #3 aberto); implementado (Verde-Sálvia, tokens) |
| docs/04-FEATURES.md | OK | Pátina (ADR-045), First Capsule (044) implementados; M1-M4 descritos |
| docs/05-ARCHITECTURE.md | OK | Stack (Kotlin/Compose/Room/Keystore) bate; busca textual local bate |
| docs/06-DATA-MODEL.md | **SPEC-AHEAD** | Canônico campo-a-campo; campos `accessPolicy`, `sourceExternalId`, `restUntil`, `requestedWindowStart/End` (Remember Later), `embeddingCiphertext` **não existem no código** — são o contrato futuro (sealed V1, dedupe de import, memória, semântica). Não é bug: o doc declara "in V0"/"in V1". Mas quem codar sealed/V1 deve saber que o código hoje não tem nenhum desses campos |
| docs/07-RETURNS-ENGINE.md | **SPEC-AHEAD (pilot vs produção)** | Tabela de bootstrap por participante (4 entries/7d; 2-3/14-21d; 1/30-45d) é do **piloto** (`pilot/PILOT-PROTOCOL.md`), enquanto o código de produção usa `bootstrapMinimumAgeDays`: 0/30/60/90 dias por contagem. As duas existem deliberadamente (protocolo de pesquisa vs app de produção). Regras de elegibilidade/cap/quiet-hours no doc batem com o código |
| docs/08-PRIVACY-SECURITY.md | OK | Cripto (envelope, Keystore, AAD, biometria) bate com código; backup desligado ✓ |
| docs/09-ANALYTICS-EXPERIMENTS.md | OK | Content-free analytics = ADR-017; nada implementado remotamente ✓ |
| docs/10-ROADMAP.md | OK | Direção; sem claims falsos |
| docs/11-FOUNDER-VISION.md | OK | Intenção; Book/Legacy congelados ✓ (ADR-019) |
| docs/DECISIONS.md | OK | Ledger atualizado (ADR-047) |
| docs/export-format.md | OK | v1.0 descrita == FioService/ExportCoordinator |
| docs/search/* (3 docs) | OK | Arquitetura, privacidade, threat model = M4 entregue |
| plans/PROJECT-STATE.md | **DRIFT (menor)** | Faltam: Missão 4 concluída (busca lexical em produção + protótipo híbrido, 134 testes), branch `integration/manus-search-20260819`, PRs #1/#3. O resto bate |
| plans/NEXT-WORK.md, HARDENING-QUEUE.md | DRIFT (menor) | Não refletem M4 (busca) nem este Atlas |
| research/search/RESEARCH-LOG.md | OK | EmbeddingGemma candidato, kill criterion ✓ |
| pilot/* | OK | Protocolo pronto, não iniciado — status correto |
| docs/design/* | OK | Fonte do design (PR #3) |

## 2. Correções exigidas (não executadas — são trabalho do Codex, ver EXECUTION-QUEUE)

1. **FIO-PQ (quick win, sem código)**: atualizar `PROJECT-STATE.md` — adicionar seção Missão 4 (busca M4: lexical em produção, protótipo híbrido removível, 134 testes, kill criterion), e a fila do Atlas no `NEXT-WORK.md`.
2. Registrar no PROJECT-STATE a decisão de status do `@SearchRepository`/busca como "M4 production baseline" para que o Codex não a trate como research.

## 3. O que deliberadamente NÃO é drift

O spec canônico (docs/06) frente ao código atual é **contrato, não inconsistência**: é a prática registrada no SOURCE-OF-TRUTH (doc > spec de engenharia detalhada, código vence em comportamento). A tabela de bootstrap do piloto (docs/07 §participante) é pesquisa; o código de produção tem os 0/30/60/90 deliberados. Nenhum dos dois deve ser "consertado" — devem ser entendidos.
