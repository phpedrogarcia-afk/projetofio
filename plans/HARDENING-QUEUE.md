# HARDENING-QUEUE — histórico consolidado

**Atualizado em:** 2026-08-20 · **Estado:** histórico · **Fila vigente:** `packets/EXECUTION-QUEUE.md`

Esta fila registrou a Missão 1. Ela não seleciona mais trabalho NOW e não deve competir com a packet factory do Atlas.

## Campanhas concluídas

| Campanha | Prioridade | Resultado preservado |
|---|---|---|
| Unicode e encoding | P0 | Surrogate halves deixaram de sofrer substituição silenciosa; conteúdo ZW-only permaneceu como decisão de produto |
| Crypto pre-review e plaintext hunt | P0 | Boundary revisada; revisão criptográfica independente ainda aberta |
| Room migration 2→3 | P0 | Preservação byte a byte e invariantes verificados historicamente |
| Returns engine torture | P1 | Determinismo, quiet hours, DST, fusos, cap e bootstrap exercitados |
| Privacy boundary e Android backup | P0 | Backup desabilitado, notificação discreta e superfícies estáticas auditadas |
| Storage/export torture | P1 | Falhas SAF e round-trip SHA-256 cobertos |
| Dependências e lint | P2/P3 | Auditoria histórica concluída; metadata atual deve continuar verificável |
| Performance e histórico grande | P2 | Cobertura histórica até 10k entradas; Search tem packet específico de escala |
| Bateria/background | P2 | WorkManager não-exato, sem foreground service ou rede |
| Acessibilidade e visual | P2 | Auditoria estática concluída; gates reais continuam abertos |
| Error UX e fuzz | P1/P3 | Falhas preservam dados; invariantes do engine exercitados |

## Pendências convertidas em packets

| Tema | Packet |
|---|---|
| Criptografia em aparelho | `FIO-P01` e `FIO-P03` |
| PrivacyCover, TalkBack e preview | `FIO-P02` |
| Process death | `FIO-P04` |
| DST real | `FIO-P05` |
| Escala de Search | `FIO-P06` |
| Importação/OOM | `FIO-P07` |
| Crescimento do histórico | `FIO-P08` |
| FLAG_SECURE por superfície | `FIO-P09` |
| Processo de migration | `FIO-P10` |

## Regra atual

Nenhuma campanha deste documento deve ser reaberta diretamente. Use `packets/INDEX.md`, `packets/DEPENDENCIES.md` e o único item NOW de `packets/EXECUTION-QUEUE.md`. Ausência de evidência física ou humana continua significando “não validado”.
