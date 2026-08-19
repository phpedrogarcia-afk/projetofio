# RISK-MAP — riscos reais, classificados com severidade honesta

**Legenda:** R0 = perda/exposição de conteúdo privado (crítico) · R1 = perda de dados não-crítricos ou trabalho (alto) · R2 = comportamento errado observável (médio) · R3 = débito técnico/escala (baixo-médio). Probabilidade: B (baixa), M (média), A (alta). Mitigação = o que existe; Gap = o que falta.

## 1. Matriz consolidada

| # | Risco | Sev | Prob | Onde mora | Mitigação atual | Gap / Packet |
|---|---|---|---|---|---|---|
| K-01 | Perda total do archive ao perder o aparelho (chaves Keystore são do aparelho; allowBackup=false) | R0 | M | plataforma/crypto | Export manual do usuário | **K-01: backup/restore policy (D-5)** — maior risco de disponibilidade do produto; export-first habituation no onboarding não existe hoje |
| K-02 | Plaintext em memória durante sessão (editor/leitura/export) exposto a app switcher, screenshots automáticos, casting | R0 | M | UI/privacy surfaces | PrivacyCover ADR-031; LockedScreen | **K-02: FLAG_SECURE por superfície + TalkBack físico** (instrumentado não roda no sandbox) |
| K-03 | Crash/exception com plaintext em stacktrace ou relatório (se um dia existir collector) | R0 | B | app | nenhum collector hoje (proibido) | **K-03: packet de collector content-free** (pré-existente em `docs/security/CRYPTO-REVIEW-PACKET.md`) |
| K-04 | Bug de cripto (GCM nonce reuse, AAD missing, Keystore falhando em OEMs) | R0 | B | crypto | AesGcmContentCipherTest; surrogate-fix M1; P0 resolvido | **K-04: crypto review externo** (packet existe, nunca executado) |
| K-05 | Notificação de devolução expõe timing/conteúdo (lockscreen preview, canais custom) | R2 | B | returns | canal low + corpo genérico; IMPORTANCE_LOW evita preview | verificar em OEMs específicos (Samsung/Xiaomi customizam previews) |
| K-06 | Devolução em momento emocionalmente indesejado (luto, ex) | R2 | M | engine | cap 7d + never-return + consent | **piloto** é o único teste real; "never return" cobre caso a caso |
| K-07 | WorkManager inexact → devolução fora da janela preferida (doze, bateria extrema) | R2 | M | scheduling | ADR-038 (aceito); reconcile | aceitável hoje; exato futuro = ADR + permissão |
| K-08 | Reboot sem devolução até primeira abertura (hiato de reconcile) | R2 | B | returns | documentado | testável só em aparelho |
| K-09 | Soft-delete não purgeável por bug de purge_after/clock | R1 | B | persistence | purgeExpired + teste | coberto por Migration2To3Test domínio |
| K-10 | Draft perdido em process death no meio do save | R1 | B | guarda | autosave em mudança | **K-10: simular process death** (nenhuma suíte hoje) |
| K-11 | Import preview enorme (MBs de texto) OOM | R1 | B | import | staging em memória | **K-11: streaming/limit de tamanho no preview** (não medido) |
| K-12 | Busca léxica degrada em escala (1k-10k entries: debounce, memória do scan) | R3 | M | search | Option A <1ms em 60 entries | **K-12: scale validation 1k+** (FIO-P07) |
| K-13 | EmbeddingGemma RAM (RSS 224-332MB NPU) estoura dispositivo de entrada | R3 | M | ML futuro | não embarcado | **K-13: FIO-P12 device gate** + kill criterion ΔRAM |
| K-14 | Licenciamento EmbeddingGemma ("gemma license", aceitar no HF) | R2 | B | ML futuro | stop condition | K-13 |
| K-15 | Drift de documentação vs código (docs 01-11 escritos na era iOS; parte superseded) | R3 | A | docs | DECISIONS.md é o ledger | **DOC-DRIFT-AUDIT.md** (§6 da missão) |
| K-16 | ADR-045 (pátina) / motifs crescerem em complexidade e virarem distraction | R2 | B | UX | ADR-045 determinístico e removível | monitorar em revisão |
| K-17 | Testes instrumentados nunca executados = falsa sensação de cobertura em crypto/surfaces | R2 | M | testing | CI sem AVD | **K-17: device gate + relatório honesto** (TEST-MAP) |
| K-18 | FioViewModel single-flow para tudo crescer em complexidade (state explosion) | R3 | M | UI | um estado FioUiState | aceitável hoje; observar |
| K-19 | Export corrompido/parcial salvo em destino SAF sem checksum conferido pelo usuário | R1 | B | export | checksum documentado no rodapé/manifest | **K-19: verificação de checksum no app?** decisão de produto (hoje o humano confere) |
| K-20 | "Pesquisas recentes"/histórico de queries implementado no futuro vazando conteúdo | R0 | B | search futuro | nunca persistido hoje | invariante I-14 + ADR para qualquer mudança |

## 2. Top 5 por severidade × probabilidade

1. **K-01** (perda de aparelho = perda de archive) — o fundador deve saber que o produto hoje NÃO recupera dados; export é a única saída. Mitigação de produto recomendada: lembrar export periodicamente (sem coleta: timestamp local do último export no settings) — decisão.
2. **K-06** (devolução em momento indesejado) — único risco emocional do core; mitigação é o piloto, não código.
3. **K-04** (crypto review externo) — nunca executado; um finding aqui invalidaria tudo.
4. **K-17** (cobertura instrumentada fantasma).
5. **K-02** (switcher/screenshots) — PrivacyCover ajuda, mas não é verificado em aparelho.

## 3. O que NÃO é risco (esclarecimento contra ansiedade comum)

O benchmark sintético não polui o app (`research/` fora do build). O protótipo semântico não toca produção (não injetado). A falta de sync não é vulnerabilidade (sem servidor = sem vetor). FTS5 não-existente não é débito: é escolha de boundary. `xkcd-1172` (padrões caseiros de cripto) não se aplica: usamos primitivas padrão (AES-GCM, Keystore, BiometricPrompt) — o único finding histórico (surrogate halves) foi corrigido e testado.
