# EXTERNAL-SOLUTION-INDEX — soluções externas para quando o Codex precisar de uma roda pronta

**Critério de entrada:** madura, auditável ou oficial do vendor, compatível com princípios do Fio (local-first, privacidade, retrieve-not-interpret). Nada aqui entra no app sem decisão e ADR. Última verificação: 2026-08-19.

## 1. ML on-device

| Solução | O que resolve | Estado p/ Fio | URL |
|---|---|---|---|
| LiteRT (ex-TFLite) | Inferência on-device em Android/iOS | Plataforma aprovada (Google); não decidir nada além | [ai.google.dev/edge/litert](https://ai.google.dev/edge/litert) |
| EmbeddingGemma 300M (LiteRT variants) | Embeddings multilingues on-device; MRL 768→128 dims; seq 256 | **Candidato primário** (RESEARCH-LOG); stop condition: licença "gemma" aceita no HF; NPU seq256 infer 7.8ms/RSS 224MB; CPU 66ms/110MB | [HF litert-community](https://huggingface.co/litert-community/embeddinggemma-300m) |
| Google AI Edge RAG Library | Wrapper oficial LiteRT + tokenizer + retrieval | Alternativa de implementação se embarcar (USE LIBRARY) | [ai-edge-apis rag](https://github.com/google-ai-edge/ai-edge-apis/tree/main/local_agents/rag) |
| MTEB-BR | Benchmark de embeddings PT-BR | Referência de seleção de modelo | [huggingface.co/mteb](https://huggingface.co/mteb) |
| SmolChat-Android | App de referência de SLM local | Lente de implementação | [GitHub](https://github.com/shubham0204/SmolChat-Android) |

## 2. Persistência e cripto

| Solução | O que resolve | Estado p/ Fio | URL |
|---|---|---|---|
| Room 2.8 + KSP | ORM SQLite com migrations | Em produção | [developer.android](https://developer.android.com/jetpack/androidx/releases/room) |
| SQLite extensions: sqlite-vss, sqlite-vec | Busca vetorial em SQLite | **Gate**: não auditada p/ conteúdo íntimo; decisão ADR se semântica embarcar com índice | [github.com/asg017/sqlite-vss](https://github.com/asg017/sqlite-vss) |
| SQLCipher | Criptografia integral da DB | Rejeitado deliberadamente (ADR-023/035: cripto application-level com boundary explícita) | [sqlcipher.net](https://www.sqlcipher.net/) |
| Android Keystore + BiometricPrompt | Chaves + desbloqueio | Em produção | Platform |

## 3. Scheduling e plataforma

| Solução | O que resolve | Estado p/ Fio | URL |
|---|---|---|---|
| WorkManager (inexact) | Agendamento resiliente | Em produção (ADR-038) | [developer.android](https://developer.android.com/develop/background-work/background-tasks/persistent) |
| AlarmManager exact + SCHEDULE_EXACT_ALARM | Precisão exata | Não usar agora (bateria/permissão); padrão fusion existe | [developer.android](https://developer.android.com/develop/background-work/background-tasks/schedule-exact-alarms) |
| androidx.security / EncryptedFile | Cifra de arquivos | Não necessário (nossa fronteira é o envelope); conhecer | Platform |

## 4. Produto de referência (padrões)

| Referência | Padrão extraído | Cuidado |
|---|---|---|
| [Mini Diarium](https://github.com/fjrevoredo/mini-diarium) | Import Day One JSON/TXT + jrnl; encrypt-before-disk por entry; backup local cifrado | Parser Day One JSON compatível com nosso LocalImportParser? verificar antes de adaptar |
| [Day One](https://dayoneapp.com/) | On This Day determinístico; export longo | Metadata de sync fora do E2E — nunca replicar esse meio-termo |
| [Diarly](https://diarly.app/help/on-this-day.html) | Notificações de memória opt-in por journal/hora | Padrão de controle se resurfacing evoluir |
| [Standard Notes](https://standardnotes.com/offline) | Tool offline de descriptografia de backup | Inspirar fluxo de recuperação |
| [Lightmark](https://www.reddit.com/r/digitaljournaling/comments/1l2el6l/) | Entries imutáveis | Rejeitado: Fio edita livre (re-cifra) |
| Facebook/Google Photos Memories | Resurfacing automático | **Rejeitado por dano emocional documentado** — é o contra-exemplo do nosso design de consentimento |

## 5. O que foi deliberadamente descartado (para não redescobrir)

LLM conversacional server-side (proibido); Firebase/crashlytics com conteúdo (proibido; crash reporting content-free exigiria ADR próprio); FTS5 (boundary); Hilt (simplicidade atual); NavController (simplicidade atual); streaks/gamificação (princípio); any "AI journaling assistant" SDK (interpretação = violação do princípio 3).
