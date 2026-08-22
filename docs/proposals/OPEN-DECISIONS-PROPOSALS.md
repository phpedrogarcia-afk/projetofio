# Propostas de Decisão Aberta (D-1 a D-5) — ProjetoFio

**Status:** PROPOSALS READY FOR FOUNDER REVIEW
**Data:** 2026-08-21
**Autoridade:** Governança técnica e privacidade (docs/DECISIONS.md, docs/atlas/DECISION-INDEX.md)

---

## D-1 — Notas Seladas na Busca: Invisibilidade Total ou Contagem Opaca?

### Contexto
O Fio prevê que determinadas notas possam ser "seladas" (protegidas por autenticação biométrica adicional ou ocultamento intencional). No domínio da busca (`SearchExecutionSettings`), existem dois modos previstos:
1. `HIDDEN`: Notas seladas são 100% invisíveis na busca. Zero pistas de que existem.
2. `COUNT_ONLY`: O texto da nota nunca é exibido nem vazado em snippets, mas o rodapé informa: *"N notas seladas também coincidem com esta busca"*.

### Opções
- **Opção 1 (Recomendada): `HIDDEN` como padrão absoluto.** Notas seladas nunca aparecem nem influenciam contadores na busca normal. Uma contagem só seria exibida se o usuário autenticar e desbloquear explicitamente o modo de notas seladas.
- **Opção 2: `COUNT_ONLY` como padrão.** Exibe a contagem opaca sem revelar conteúdo.

### Recomendação e Trade-off
A **Opção 1 (`HIDDEN`)** é recomendada para máxima privacidade. Evita o vazamento indireto de metadados (saber que uma nota selada contém determinada palavra é uma inferência de conteúdo).

---

## D-2 — Analytics Remoto: Manter Desabilitado / Local-Only ou Transporte Fechado?

### Contexto
O ADR-017 estipula que analytics deve ser content-free e com schema fechado (somente eventos técnicos operacionais, ex.: tempo de inicialização, falhas de I/O de banco). Atualmente, o aplicativo não possui nenhuma biblioteca ou tráfego de rede para analytics.

### Opções
- **Opção 1 (Recomendada): Manter Analytics Remoto estritamente ausente (Zero Network Traffic).** O Fio continua sendo 100% offline-first sem tráfego de rede ou SDKs de telemetria. Diagnósticos técnicos ficam restritos a logs locais efêmeros de depuração se o usuário exportar.
- **Opção 2: Implementar transporte de telemetria técnica de schema fechado (ADR-017).** Apenas métricas de performance do app (sem texto, sem queries, sem datas).

### Recomendação e Trade-off
A **Opção 1** é recomendada. Preserva o princípio inegociável de confiança: o aplicativo não solicita permissão `android.permission.INTERNET`, comprovando que os dados nunca saem do aparelho.

---

## D-3 — Busca Semântica em Produção: Integrar EmbeddingGemma ou Manter Apenas Lexical?

### Contexto
A busca lexical (M4) é instantânea (p95 < 1ms), consome zero memória persistente adicional e funciona perfeitamente offline para recuperação exata de palavras. O protótipo semântico com modelos de embeddings (`EmbeddingGemma-300M`) requer ~300MB de espaço e alto uso de RAM/NPU.

### Opções
- **Opção 1 (Recomendada): Manter Busca Lexical como motor de produção e condicionar Semântica a testes em hardware com NPU (Gate FIO-P12).** Não embarcar pesos no APK.
- **Opção 2: Descartar definitivamente busca semântica.** Fio permanece puramente lexical.

### Recomendação e Trade-off
A **Opção 1** é recomendada. Mantém o APK leve e rápido, enquanto reserva a pesquisa vetorial apenas para quando dispositivos suportarem aceleração sem impacto na bateria ou tamanho do app.

---

## D-4 — Modelo Semântico: Download On-Demand vs Não Embarcar

### Contexto
Se a busca semântica for futuramente ativada (D-3), o modelo de 300MB não deve inflar o APK base de 15MB.

### Opções
- **Opção 1 (Recomendada): Download opcional sob demanda (Opt-in explícito nos Ajustes).** O usuário escolhe expressamente se quer baixar o modelo de ressonância semântica.
- **Opção 2: Modelo nunca é baixado pela rede.** Apenas modelos suportados nativamente pelo Android AICore/Gemini Nano local do sistema operacional.

### Recomendação e Trade-off
A **Opção 2** (aproveitar modelos nativos do sistema on-device quando disponíveis) ou **Opção 1** (download transparente apenas se o usuário pedir).

---

## D-5 — Política de Backup e Restauração (K-01)

### Contexto
O Fio prioriza a guarda duradoura das palavras exatas. Perda de aparelho sem backup significa perda dos dados. Backup na nuvem sem criptografia de ponta-a-ponta viola a privacidade.

### Opções
- **Opção 1 (Recomendada): Exportação/Backup Criptografado Manual para Arquivo Local.** O usuário gera um arquivo `.fio-backup` protegido por senha mestra/chave derivada (Argon2id/PBKDF2 + AES-GCM), que pode ser guardado onde preferir (Google Drive pessoal, pendrive, PC).
- **Opção 2: Confiar exclusivamente na exportação em texto puro/Markdown (ADR-030/046).** O usuário exporta periodicamente seus textos legíveis.
- **Opção 3: Android Auto Backup criptografado pelo sistema.**

### Recomendação e Trade-off
A **Opção 1** combinada com a **Opção 2** é a arquitetura ideal: preserva soberania do usuário, segurança criptográfica e independência de servidores proprietários do Fio.
