# Documentation index

Status: ACTIVE — canonical router and classification

This file routes; it does not replace the linked source.

| Área | Fonte principal | Apoio | Status |
|---|---|---|---|
| Produto | `01-PRODUCT.md`, `02-PRINCIPLES.md` | `11-FOUNDER-VISION.md` | ACTIVE |
| Decisões | `DECISIONS.md` | `atlas/DECISION-INDEX.md` | ACTIVE |
| Escopo e sequência | `04-FEATURES.md`, `10-ROADMAP.md` | packets | ACTIVE |
| UX | `03-UX.md` | `atlas/UX-SURFACE-MAP.md`, `design/` | ACTIVE + REFERENCE |
| Arquitetura | `05-ARCHITECTURE.md` | `atlas/SYSTEM-MAP.md` | ACTIVE + REFERENCE |
| Dados | `06-DATA-MODEL.md` | `atlas/DATA-MAP.md` | ACTIVE + REFERENCE |
| Returns | `07-RETURNS-ENGINE.md` | `atlas/TIME-MAP.md` | ACTIVE + REFERENCE |
| Privacidade | `08-PRIVACY-SECURITY.md` | `atlas/PRIVACY-BOUNDARY-MAP.md`, `security/` | ACTIVE + REFERENCE |
| Pesquisa/analytics | `09-ANALYTICS-EXPERIMENTS.md` | `research/` somente por packet | ACTIVE + REFERENCE |
| Search | `search/SEARCH-ARCHITECTURE.md` | `atlas/SEARCH-MAP.md` | ACTIVE + REFERENCE |
| Export | `export-format.md` | ADR-046 | ACTIVE |
| Testes | `TEST-LEVELS.md` | `atlas/TEST-MAP.md` | ACTIVE + REFERENCE |
| Atlas | `atlas/CODEX-MASTER-MAP.md` | demais mapas | REFERENCE |
| Estado/fila | `../plans/PROJECT-STATE.md`, `../packets/EXECUTION-QUEUE.md` | `../plans/NEXT-WORK.md` | ACTIVE |

## Classificação

- **ACTIVE:** fonte necessária para decisões ou execução atual.
- **REFERENCE:** detalhe útil quando o router/packet aponta; não faz parte do
  boot normal.
- **HISTORICAL:** evidência preservada; nunca governa trabalho atual.
- **SUPERSEDED:** substituída explicitamente; ler somente para história.

## Famílias fora do boot normal

| Família | Classificação | Como usar |
|---|---|---|
| `docs/atlas/*` exceto invariantes/router indicado | REFERENCE | somente por área ou auditoria |
| `docs/design/*` | REFERENCE | somente trabalho visual |
| `docs/security/*` | REFERENCE | somente packet de segurança |
| `research/*` | REFERENCE | somente pesquisa apontada por packet |
| `plans/evidence/*`, `plans/campaigns/*` | HISTORICAL | prova de checkpoint, não instrução atual |
| planos M1–M4 concluídos | HISTORICAL | preserve decisões/evidência; fila atual vence |
| `MANUS-*-FINAL.md`, `MISSION5-WORK-NOTES.md` | HISTORICAL | checkpoints das Missões 1–5 |
| planos Apple/iOS e ADRs 005/022/023/028 | SUPERSEDED | Android ADR-033–036 governam |

Não mova arquivos históricos em massa. Esta classificação os retira do caminho
de execução sem quebrar links existentes.
