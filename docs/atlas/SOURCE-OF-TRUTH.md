# SOURCE-OF-TRUTH — hierarquia de fontes canônicas do ProjetoFio

**Princípio:** quando fontes discordam, a hierarquia decide. A hierarquia existe exatamente para os dias de dúvida (e sempre há dúvida). Última revisão: 2026-08-19 (Missão 5).

## 1. A hierarquia

| Nível | Fonte | Cobertura | Quando vence |
|---|---|---|---|
| 1 | **DECISIONS.md** (ADR ledger Accepted) | intenção normativa de produto, privacidade e arquitetura | Nunca é silenciosamente anulada por código divergente |
| 2 | **Código + testes executados** | comportamento real comprovado | Vence descrições factuais desatualizadas; divergência com ADR é bug/stop condition |
| 3 | **Princípios** (`docs/02-PRINCIPLES.md`) | filosofia do produto | Sobre specs detalhadas |
| 4 | **Product docs** (`docs/01, 03, 04, 11`) | produto e UX | Sobre docs de engenharia |
| 5 | **Specs de engenharia** (`docs/05, 06, 07, 08, 09`) | arquitetura, data, engine, privacidade | — |
| 6 | **Roadmap** (`docs/10`) | direção | Nunca sobre um ADR Accepted |
| 7 | **Founder Vision** (`docs/11`) | intenção de longo prazo | Não autoriza item Frozen/Planned |
| 8 | **Atlas** (`docs/atlas/*`) | radiografia + roteamento | Fonte de LENTE, nunca de NORMA; drift deve ser corrigido |

## 2. Arquivos por assunto (para onde olhar primeiro)

| Assunto | Fonte primária | Fonte de apoio |
|---|---|---|
| Regras de conduta para agentes | `AGENTS.md` | DECISIONS.md |
| Criptografia/privacidade | `docs/08` + código `crypto/` + `SEARCH-PRIVACY-ARCHITECTURE.md` | `docs/atlas/PRIVACY-BOUNDARY-MAP.md`, `INVARIANTS.md` |
| Devoluções (tempo) | código `domain/TimeReturnEngine.kt` | `docs/07`, `docs/atlas/TIME-MAP.md` |
| Busca | código `search/` + `docs/search/SEARCH-ARCHITECTURE.md` | `docs/atlas/SEARCH-MAP.md`, `research/search/*` |
| Dados/schema | código `persistence/Entities.kt` + `docs/06` | `docs/atlas/DATA-MAP.md` |
| UX | código `ui/` + `docs/03` + `docs/design/*` | `docs/atlas/UX-SURFACE-MAP.md` |
| Export | `docs/export-format.md` + código `FioService` | ADR-046 |
| Piloto | `pilot/*` | ADR-016 |
| Estado do projeto | `plans/PROJECT-STATE.md` + `plans/NEXT-WORK.md` | `plans/ATLAS-QUEUE.md` |
| Decisões abertas | `docs/atlas/DECISION-INDEX.md` §2 | PROJECT-STATE |
| Pesquisa externa | `research/atlas/REFERENCE-CATALOG.md`, `PATTERN-CARDS.md` | `research/search/RESEARCH-LOG.md` |
| Trabalho em fila | `packets/INDEX.md` + `packets/EXECUTION-QUEUE.md` | `plans/ATLAS-QUEUE.md`, `HARDENING-QUEUE.md` |

## 3. Regras operacionais

1. O **ledger (DECISIONS.md) é a única fonte de decisões**; se uma decisão existe só em chat/documento solto, ela não existe até virar ADR.
2. Docs superseded/frozen (ADR-005, 019, 022, 023, 028, 030) **não são fontes**; ler apenas para história.
3. O Atlas (`docs/atlas/*`) é radiografia com timestamp; ele envelhece a cada commit. Verifique no código apenas a afirmação factual necessária à tarefa — não faça scan integral por padrão.
4. `plans/PROJECT-STATE.md` declara o estado para o Codex; quando divergir do código, o código vence e o PROJECT-STATE deve ser corrigido (isso é a definição de drift que a missão auditou).
5. Testes são evidência de comportamento; **evidência de execução** (output do gradle) vale mais que presença de arquivo de teste.
