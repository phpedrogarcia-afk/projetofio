# REFERENCE-CATALOG — o que existe lá fora e o que aprendemos com cada referência

**Propósito:** catálogo de sistemas reais e soluções maduras contra os quais o Fio foi comparado. Nada aqui é código; é lente. Última verificação: 2026-08-19.

## 1. Aplicações de journaling privadas (benchmark de produto)

| Referência | O que faz | O que o Fio copia, adapta ou rejeita |
|---|---|---|
| [Day One](https://dayoneapp.com/) [1] | Journal multimídia maduro; E2EE por padrão nos journals atuais, com metadata de sync **fora** do envelope E2E (timestamps, dims, device) | Copia: export + longevidade. Rejeita: sync cloud; metadata parcial. A diferença do Fio é zero-metadata-server |
| [Apple Journal](https://support.apple.com/guide/iphone/view-and-search-journal-entries-iph6257be047/ios) [2] | On This Day por data exata; "Suggested Moments" via ML on-device (Apple Intelligence) | Copia: data-âncora. Rejeita: sugestão automática de momentos (princípio: a IA seleciona, não interpreta; consentimento antes de qualquer magia) |
| [Diarly](https://diarly.app/help/on-this-day.html) [3] | `@onThisDay` na busca; notificações de memória configuráveis (por journal, hora escolhida); widget de memória diária | Copia como padrão de controle: qualquer resurfacing futuro deve ser opt-in por journal e hora. Hoje o Fio tem o mais restritivo do mercado (cap 7 dias + never) |
| [Google Photos / Facebook Memories](https://about.fb.com/news/2015/03/introducing-on-this-day-a-new-way-to-look-back-at-photos-and-memories-on-facebook/) [4] | Resurfacing automático de conteúdo passado | **Rejeita por princípio.** Casos documentados de dano emocional (content indesejado: ex-relacionamentos, luto). É exatamente a razão do consentimento explícito + "never return" + notificação única sem lembrete do Fio (ADR-011, ADR-027) |
| [Mini Diarium](https://mini-diarium.com/encrypted-journal/) [5] | OSS (MIT, Tauri/Rust): AES-256-GCM por entry antes do disco, SQLite local, import Day One JSON/TXT/jrnl, export JSON/Markdown, backup local cifrado, zero HTTP | Referencial de segurança local-first mais próximo do Fio. Vale para: parser de import Day One JSON (compatibilidade); padrão de documentação "encrypt before disk"; backup local cifrado (o Fio hoje não tem — decisão pendente) |
| [Standard Notes](https://standardnotes.com/offline) [6] | E2EE auditado, ferramenta de descriptografia offline para backups | Copia o princípio: portabilidade = feature de privacidade. Export v1.0 do Fio (ADR-046) é a contraparte |
| [Notesnook](https://notesnook.com/) [7] | E2EE + Vericrypt (verificação offline) | Padrão de transparência criptográfica verificável |
| [Lightmark](https://www.reddit.com/r/digitaljournaling/comments/1l2el6l/) [8] | Entries imutáveis após submissão | Contraponto: Fio permite edição (re-cifra); imutabilidade é escolha de outro produto, não um defeito nosso — mas registrar que edição cria mais versões em disco |
| Wirecutter best journals [9] | Categoria de mercado define 3 funções: prompts, reminders de hábito, "peeks at older entries" | Fio ocupa a terceira função de forma radicalmente consentida |

## 2. ML on-device (para a decisão de semântica)

| Referência | Fato | Relevância para o Fio |
|---|---|---|
| [EmbeddingGemma 300M — Google](https://ai.google.dev/gemma/docs/embeddinggemma) [10] | 308M params, Gemma 3 base, 100+ idiomas, MRL 768→128 dims, 2K tokens, <200MB RAM quantizado, <22ms EdgeTPU, offline | Candidato primário confirmado no RESEARCH-LOG; licença open weights p/ uso comercial responsável (fine-tune+deploy permitidos) |
| [EmbeddingGemma — LiteRT HF](https://huggingface.co/litert-community/embeddinggemma-300m) [11] | Performance Samsung S25 Ultra: NPU seq256 init 206ms/infer 7.8ms/RSS 224MB; CPU seq256 init 17.6ms/infer 66ms/RSS 110MB; seq2048 CPU 2.5s/333MB. Tokenizer sentencepiece | Números duros para o kill criterion: medição deve ser ΔRSS do app, não RSS do modelo isolado; gate de hardware obrigatório (FIO-P12); download exige aceitar licença no HF (stop condition) |
| [On-Device RAG — GDE](https://medium.com/google-developer-experts/on-device-rag-for-app-developers-embeddings-vector-search-and-beyond-47127e954c24) [12] | LiteRT + RAG library do Google AI Edge | Alternativa de implementação se embarcar: usar a library oficial em vez de LiteRT raw (USE PLATFORM) |
| [MTEB-BR](https://huggingface.co/mteb) [13] | Painel multilingue PT-BR | Nosso benchmark sintético replica a metodologia; EmbeddingGemma 13º/93 em MTEB-BR |
| [SmolChat-Android](https://github.com/shubham0204/SmolChat-Android) [14] | App de referência de inferência local de SLMs em Android | Lente de implementação (GGUF/llama.cpp vs LiteRT); nosso caminho LiteRT é o recomendado por Google |

## 3. Plataforma Android (para decisão de scheduling e persistência)

| Referência | Fato | Relevância |
|---|---|---|
| Android background-work docs [15] | WorkManager inexact; exact alarms (AlarmManager, Doze-wake) exigem `SCHEDULE_EXACT_ALARM` e são desencorajados fora de casos time-critical | Valida ADR-038: inexact é correto para "reencontro"; entrega exata futura = ADR novo + permissão + review de bateria |
| WorkManager-AlarmManager fusion pattern [16] | Persistência via WorkManager + precisão via AlarmManager | Padrão consolidado se precisão se tornar requisito — não construir agora |
| Room 2.8 + KSP | Migrations manuais com exportSchema | Nosso processo (Migration2To3Test byte a byte) segue a prática recomendada |

## 4. O que este catálogo confirma (síntese)

O Fio já implementa o padrão-ouro emergente de privacidade em journaling ("encrypt before disk", offline, export portável) — o que Mini Diarium e Standard Notes documentam como os pilares. Sua contribuição distintiva não é a criptografia em si, mas a **política temporal consentida**: nenhum sistema comparado combina (a) devolução sem calendário fixo, (b) cap de frequência, (c) "never return" por entrada, e (d) busca factual sem interpretação. O risco competitivo inverso existe: resurfacing automático (Google/Apple) é o padrão de massa; o Fio aposta que o controle explícito é o posicionamento correto para conteúdo autobiográfico — hipótese que só o piloto (pilot/) pode testar.

## References

[1]: https://dayoneapp.com/features/on-this-day/ "Day One — On This Day"
[2]: https://support.apple.com/guide/iphone/view-and-search-journal-entries-iph6257be047/ios "Apple Journal — View and search entries"
[3]: https://diarly.app/help/on-this-day.html "Diarly — On This Day"
[4]: https://about.fb.com/news/2015/03/introducing-on-this-day-a-new-way-to-look-back-at-photos-and-memories-on-facebook/ "Facebook — On This Day announcement (2015)"
[5]: https://mini-diarium.com/encrypted-journal/ "Mini Diarium — Encrypted journal"
[6]: https://standardnotes.com/offline "Standard Notes — Offline decryption tool"
[7]: https://notesnook.com/ "Notesnook — E2EE notes"
[8]: https://www.reddit.com/r/digitaljournaling/comments/1l2el6l/ "Lightmark — immutable entries"
[9]: https://www.nytimes.com/wirecutter/reviews/best-journaling-apps/ "Wirecutter — Best Journaling Apps"
[10]: https://ai.google.dev/gemma/docs/embeddinggemma "Google — EmbeddingGemma model overview"
[11]: https://huggingface.co/litert-community/embeddinggemma-300m "Hugging Face — litert-community/embeddinggemma-300m"
[12]: https://medium.com/google-developer-experts/on-device-rag-for-app-developers-embeddings-vector-search-and-beyond-47127e954c24 "GDE — On-Device RAG for App Developers"
[13]: https://huggingface.co/mteb "MTEB — Massive Text Embedding Benchmark"
[14]: https://github.com/shubham0204/SmolChat-Android "SmolChat-Android"
[15]: https://developer.android.com/develop/background-work/background-tasks/persistent "Android — Persistent background work"
[16]: https://proandroiddev.com/a-fusion-between-workmanager-and-alarmmanager-fe188e8b53dc "ProAndroidDev — WorkManager + AlarmManager fusion"
