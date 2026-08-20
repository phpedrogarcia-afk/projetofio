# NEXT-WORK — visão consolidada

**Atualizado em:** 2026-08-20 · **Fonte de ordem:** `packets/EXECUTION-QUEUE.md`

Este documento é um resumo humano. A escolha e a ordem do trabalho são governadas exclusivamente pelos packets, suas dependências e a fila central.

## NOW

1. `FIO-PQ-03` — apresentar ao fundador as decisões D-1..D-5 com propostas escritas, sem implementar as opções.

## READY técnico

- `FIO-P06`: escala da busca lexical em 1k/10k entradas.
- `FIO-P07`: hardening de importação contra arquivos grandes/OOM.
- `FIO-P10`: simulação do processo de migration 3→4, sem criar o schema 4 real.

Esses itens só começam quando promovidos a NOW em `packets/EXECUTION-QUEUE.md` e quando existir packet completo.

## DEVICE / HUMAN / REVIEW

- Aparelho disponível: Xiaomi M2103K19PG (Poco M3 Pro), Android 13/API 33.
- Ainda sem evidência aprovada: suíte instrumentada completa, TalkBack, PrivacyCover, process death, DST real e revisão externa de criptografia.
- Segundo OEM indisponível.
- Piloto, consentimento e recrutamento continuam sob decisão humana.

## DECISÕES ABERTAS

As decisões D-1..D-5 vivem em `docs/atlas/DECISION-INDEX.md`. Nenhuma opção deve ser implementada antes do fechamento documental e da aprovação necessária.

## NÃO INICIAR

- Schema 4 real, sealed notes, índice vetorial persistente ou analytics remoto.
- Motor semântico em produção sem D-3, licença, medição em aparelho e kill criterion.
- Feature fora da fila, RAG/backend, conta, sync ou rede de conteúdo privado.
- Gamificação, feed/social, publicidade, IA interpretativa ou notificações de engajamento.
- Merge em `main` sem autorização explícita.

## Histórico resumido

- M1: hardening e failure hunting — concluída.
- M2: red team, piloto preparado e handoff — engenharia concluída; gates humanos abertos.
- M3: refinamento profundo de UX — concluída.
- M4: busca lexical em produção e protótipo semântico removível — concluída.
- M5: FIO Master Atlas e packet factory — concluída.
- PQ-01/PQ-02: sincronização documental pós-Atlas — concluída.

Detalhes permanecem nos relatórios das missões, em `plans/HARDENING-QUEUE.md`, `plans/ATLAS-QUEUE.md` e em `docs/atlas/`.
