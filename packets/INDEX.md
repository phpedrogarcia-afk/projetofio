# PACKETS INDEX — todo o trabalho futuro do Fio vive aqui

**Fontes:** este índice + `EXECUTION-QUEUE.md` (ordem). Antecedentes em `plans/` (HARDENING-QUEUE.md, DATA-TORTURE-MATRIX.md, DOCUMENTATION-DRIFT-AND-PR-CLEANUP.md, INTEGRATION-READINESS.md) permanecem como história e entrada de alguns packets.

## 1. Packets de documentação e saneamento (sem código)

| Packet | Assunto | Depende de |
|---|---|---|
| FIO-PQ-01 | Atualizar PROJECT-STATE.md (Missão 4 + estado da busca + Atlas) | — |
| FIO-PQ-02 | Sincronizar NEXT-WORK/HARDENING-QUEUE com o Atlas | FIO-PQ-01 |
| FIO-PQ-03 | Decisões pendentes: levar D-1..D-5 ao fundador com proposta escrita (1 págs cada) | — |
| FIO-PQ-04 | Revisar PR #1 (codex/v0-time-only-checkpoint) e #3 (feature/design-ux-v1) para merge-ready | fundador |

## 2. Packets de endereçamento de risco (device/evidência)

| Packet | Assunto | Depende de |
|---|---|---|
| FIO-P01 | Device gate cripto: executar suite instrumentada de crypto (AndroidKeystorePersistenceTest, EncryptedPersistenceTest, AesGcmBoundary) em aparelho real | aparelho |
| FIO-P02 | Privacy surfaces reais: PrivacyCover + TalkBack + notification preview em aparelho multi-OEM | aparelho |
| FIO-P03 | Crypto review externo (packet `docs/security/CRYPTO-REVIEW-PACKET.md`) | FIO-P01 |
| FIO-P04 | Process death simulation (draft sobrevivência em app kill) | — |
| FIO-P05 | DST real na janela de devolução (spring-forward) | aparelho |

## 3. Packets de engenharia (escala e endurecimento)

| Packet | Assunto | Depende de |
|---|---|---|
| FIO-P06 | Scale validation da busca lexical (1k/10k entries: latência, memória, debounce) | — |
| FIO-P07 | Import hardening: limite/preview streaming de arquivos grandes (OOM) | — |
| FIO-P08 | Return history archive/growth policy (centenas de ciclos) | decisão produto |
| FIO-P09 | FLAG_SECURE por superfície + app-switcher audit | aparelho |
| FIO-P10 | Migration readiness: simular migration 3→4 (schema fake) para calibrar o processo | — |

## 4. Packets de produto (com decisão do fundador)

| Packet | Assunto | Depende de |
|---|---|---|
| FIO-P11 | Backup/recovery policy (K-01): opções + proposta (export habituation, restore criptografado local, nothing) | D-5 |
| FIO-P12 | Device gate de semântica: medir EmbeddingGemma ΔRSS/latência em aparelho com NPU; aplicar kill criterion | D-3 + licença HF |
| FIO-P13 | Índice vetorial persistente (se P12 passar): boundary crypto do embedding + ADR | FIO-P12 |
| FIO-P14 | On This Day determinístico (opcional): query por data, opt-in, sem engine overlap | decisão produto |
| FIO-P15 | Sealed notes V1 (accessPolicy no schema): ADR + migration + cripto do envelope selado | D-1 + founder |
| FIO-P16 | Analytics remoto (opcional): ADR transport + schema fechado ADR-017 | D-2 |

## 5. Packets do piloto (humano, não-código)

| Packet | Assunto | Depende de |
|---|---|---|
| FIO-PILOT-01 | Executar protocolo do piloto (`pilot/PILOT-PROTOCOL.md`) com 3-5 participantes | fundador inicia |
| FIO-PILOT-02 | Análise within-participant (time vs resonance dentro do participante) | PILOT-01 |

## 6. Estado consolidado (manter atualizado)

- Pronto para execução agora: FIO-PQ-01, FIO-PQ-02, FIO-P06, FIO-P07, FIO-P10 (sem aparelho, sem decisão).
- Bloqueado por aparelho: FIO-P01, P02, P03, P04, P05, P09.
- Bloqueado por decisão: P08, P11, P12, P14, P15, P16.
- Bloqueado por humano: PILOT-01/02, PQ-04.
- Template: `PACKET-TEMPLATE.md`.
