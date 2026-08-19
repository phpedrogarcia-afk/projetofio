# FIO-SEARCH-BENCHMARK — Conjunto de referência de busca PT-BR (Missão 4)

**Branch:** `integration/manus-search-20260819` · **Data:** 2026-08-19
**Natureza:** Material de pesquisa apenas (`research/search/`). Este dataset **nunca** é distribuído com o app, nunca entra no build, nunca toca Room ou Returns. Serve exclusivamente para comparar a busca lexical V1 contra um protótipo semântico sob feature flag (ADR-040/ADR-041).

## 1. O que este benchmark mede

A busca do Fio precisa recuperar entradas de diário escritas em português brasileiro, com vocabulário coloquial, elipses, diacríticos e paráfrases ("como me senti", "sobre a conversa com a mãe"). O benchmark compara três configurações sobre o mesmo corpus:

| Configuração | Descrição | Função |
|---|---|---|
| `LEXICAL_V1` | `LexicalTokenizer` + scan sob demanda (produção) | Baseline de referência |
| `SEMANTIC_EG300` | EmbeddingGemma-300M TFLite + cosine (protótipo, flag) | Candidata a semântica |
| `HYBRID_EG300` | rrf(k=60) sobre ranks léxico + semântico | Híbrida, se justificada |

## 2. Corpus sintético (60 entradas, PT-BR)

Escrito por esta missão para imitar entradas reais de diário: frases curtas e longas, mistura de registros (afetivo, cotidiano, trabalho, saúde), diacríticos (`café`, `família`, `médico`), abreviações (`tb`, `pq`), e variações de expressão da mesma intenção. Entradas estão numeradas; cada consulta tem um **ground truth** de 1–4 entradas.

```
E01  Ontem o café estava ótimo. Tomei com a vó na varanda e conversamos sobre o jardim dela.
E02  Não consegui dormir direito. Fiquei acordada até as 3 pensando na apresentação de quinta.
E03  Reunião difícil hoje: o projeto de redesign ficou para próxima semana. Saí exausto.
E04  Finalmente fui no médico. Ele pediu exames de sangue e disse que a pressão está controlada.
E05  Liguei para a mãe depois de tanto tempo. Ela estava feliz, falou da horta.
E06  Choveu a tarde inteira. Fiquei olhando pela janela com um chá de camomila.
E07  Entrega do projeto finalmente aprovada! O time comemorou com um almoço.
E08  Dormi mal de novo. O bebê acordou quatro vezes.
E09  Caminhada no parque hoje pela primeira vez em um mês. O ar estava frio e bom.
E10  Reuni com o gerente para negociar o prazo. Saiu melhor do que eu esperava.
E11  A conversa com a vó me fez lembrar da infância. Ela ainda tem o mesmo sorriso.
E12  O exame de sangue veio normal. Que alívio.
E13  Terça foi cansativa: duas reuniões seguidas e quase sem pausa para comer.
E14  Fiz um chá de camomila e dormi cedo. Funcionou dessa vez.
E15  O parque está lindo no outono. As folhas amarelaram.
E16  O bebê finalmente dormiu a noite inteira. Chorei de emoção com a Bia.
E17  Apresentei o redesign para a diretoria. Fiquei nervoso mas foi bem.
E18  A horta da vó tem tomate, manjericão e alecrim. Ela ensinou a plantar.
E19  Pressão alta outra vez. Preciso cortar o café e dormir mais cedo.
E20  Almoço com o time: pizza e risadas sobre o perrengue da semana passada.
E21  Não durmo bem quando penso demais no trabalho.
E22  O médico elogiou os exames. Continuar com a medicação.
E23  Falei com a mãe sobre o Natal. Vamos fazer a ceia na casa dela.
E24  Chá de camomila virou meu ritual antes de dormir.
E25  Andei 40 minutos no parque. Meu coração agradeceu.
E26  O prazo do redesign mudou de novo. Segunda vez em duas semanas.
E27  A vó está com 87 e ainda cuida sozinha do jardim.
E28  Bebê com febre ontem. Passamos a noite acordados.
E29  Reunião de alinhamento foi curta e produtiva. Raro.
E30  Cortei o café conforme o médico pediu. Chá virou o substituto.
E31  A apresentação de quinta foi um sucesso. Recebi elogios.
E32  Dormi bem pela primeira vez em semanas. Sem pensar em nada.
E33  O time aceitou minha proposta de cronograma. Vitória pequena mas real.
E34  Mãe mandou fotos do novo jardim dela. Fiquei orgulhosa.
E35  Exausto depois da caminhada, mas feliz.
E36  Segunda-feira pesada: três reuniões e um reporte inesperado.
E37  O bebê dormiu 6 horas seguidas. Registrei no app de sono.
E38  Varanda com sol, livro, café sem cafeína. Manhã perfeita.
E39  Negociação de prazo funcionou: ganhamos duas semanas.
E40  Feito o acompanhamento médico. Tudo em ordem por agora.
```

(Entradas E41–E60 na segunda parte: repetição temática deliberada para medir precisão — mesmas palavras-chave em contextos distintos, para punir retrieval raso por token.)

```
E41  Café da manhã com frutas e pão de queijo. Rotina de domingo.
E42  A reunião de segunda mudou para quinta. Confuso.
E43  Dormi no sofá vendo filme. O bebê dormiu junto.
E44  O médico marcou retorno para março.
E45  Vó contou histórias da fazenda. Quase duas horas.
E46  Caminhada na chuva, capuz, sem celular. Silêncio bom.
E47  Redesign aprovado com ressalvas. Ajustes até sexta.
E48  Chá antes de deitar, como sempre. 11 da noite.
E49  A mãe ligou no aniversário dela. Conversei 40 minutos.
E50  Entrega atrasada por causa da reunião de emergência.
E51  O parque amanheceu enevoado. Andei devagar.
E52  Bebê mamou bem e riu pela primeira vez.
E53  Pressão 13x8 hoje. Melhor que a semana passada.
E54  O time resolveu o bug crítico em um dia.
E55  Café com a vizinha depois da caminhada.
E56  Dormi ouvindo podcast. Desliguei o despertador duas vezes.
E57  A horta deu o primeiro tomate. A vó chorou.
E58  Exames de rotina feitos. Colesterol ok.
E59  A reunião virou debate acalorado. Consegui manter a calma.
E60  Domingo inteiro sem tela. Li um capítulo do livro.
```

## 3. Consultas e ground truth (30 consultas PT-BR)

Cada consulta é uma pergunta de busca plausível, com intenção lexical (palavras na consulta aparecem na entrada) e intenção parafrástica (a consulta descreve, mas não usa as mesmas palavras). `lex` = recuperável pelo léxico V1; `sem` = exige semântica; `ambos` = recuperável pelas duas.

| # | Consulta | GT (entradas) | Tipo de evidência |
|---|---|---|---|
| Q01 | "café" | E01,E38,E55 (+E41 parcial) | lex |
| Q02 | "reunião exausto" | E03,E36 | lex |
| Q03 | "dormi mal insônia" | E02,E08,E21 | sem |
| Q04 | "como me senti depois da apresentação" | E17,E31 | sem |
| Q05 | "médico exames pressão" | E04,E12,E22,E40,E53,E58 | lex |
| Q06 | "conversa com a mãe" | E05,E23,E49 | lex |
| Q07 | "momentos com a vó" | E01,E11,E18,E27,E45,E57 | sem |
| Q08 | "chá de camomila" | E06,E14,E24,E30,E48 | lex |
| Q09 | "parque caminhada ar livre" | E09,E15,E25,E35,E46,E51,E55 | lex |
| Q10 | "reunião com gerente prazo" | E10,E39 | lex |
| Q11 | "noite difícil bebê acordando" | E08,E16,E28,E36,E37,E43 | sem |
| Q12 | "almoço com colegas" | E07,E20,E37 | lex |
| Q13 | "quero dormir melhor" | E02,E08,E14,E21,E24,E30,E32,E43,E48,E56 | sem |
| Q14 | "como anda a saúde" | E04,E12,E22,E28,E40,E53,E58 | sem |
| Q15 | "relação com a mãe" | E05,E23,E34,E49 | sem |
| Q16 | "redesign projeto entrega" | E03,E17,E26,E31,E33,E47,E50,E54 | lex |
| Q17 | "jardim horta da vó" | E01,E05,E18,E27,E34,E57 | lex |
| Q18 | "domingo sem telas" | E41,E60 | sem |
| Q19 | "chuvoso tarde" | E06,E46 | lex |
| Q20 | "negociação de prazo" | E10,E39 | lex |
| Q21 | "como foi o exame" | E12,E22,E53,E58 | sem |
| Q22 | "primeiro riso do bebê" | E16,E37,E52 | sem |
| Q23 | "ansiedade antes de apresentação" | E02,E17 | sem |
| Q24 | "ritual da noite" | E14,E24,E30,E43,E48,E56 | sem |
| Q25 | "conquista do time" | E07,E33,E54 | sem |
| Q26 | "cafeína cortar café" | E19,E30,E38 | sem |
| Q27 | "segunda-feira difícil" | E13,E36 | lex |
| Q28 | "outono folhas" | E15,E51 | lex |
| Q29 | "natal ceia família" | E23,E27,E45,E49,E57 | sem |
| Q30 | "silêncio solidão boa" | E46,E60 | sem |

## 4. Métricas

Cada configuração roda as 30 consultas sobre as 60 entradas e reporta:

| Métrica | Definição | Por que importa para o Fio |
|---|---|---|
| `Recall@5` | fração de GTs com todos relevantes no top-5 | o usuário só vê ~5 linhas na primeira tela |
| `Recall@10` | idem, top-10 | cobertura quando "tudo está lá" |
| `MRR` (reciprocal rank médio) | 1/rank do primeiro relevante | o primeiro resultado deve ser o certo |
| `Precisão@5` | relevantes / 5 | punir ruído: resultados errados poluem o diário |
| `latência p95` | ms por consulta (scan completo) | deve ficar invisível (<200ms no alvo) |
| `custo de indexação` | s por 100 entradas; bytes totais do índice/vetores | memória, bateria, tamanho do app |

**Critério de decisão (kill criterion):** a semântica só avança para o roadmap se, no aggregate, melhorar Recall@10 em ≥10 pontos percentuais **ou** MRR em ≥0.08 sobre o lexical, sem ultrapassar +200MB de RAM, +15s de indexação/10k entradas e sem latência de consulta >500ms em aparelho mediano (Android 12, 4GB RAM). Falhar em qualquer um mantém o app 100% lexical — resultado válido, documentado.

## 5. Regras de execução

O benchmark roda **fora do módulo mobile** (script Python autossuficiente em `research/search/run_benchmark.py` — a criar). O texto do corpus e as consultas ficam apenas em `research/search/`. Proibido copiar para `assets/`, `raw/`, testes do app ou qualquer recurso do build. O tokenizer usado para a via lexical é o mesmo `LexicalTokenizer` do `app/` (cópia do arquivo para o script, citando a fonte).

## 6. Limitações reconhecidas

Corpus sintético de 60 entradas não cobre diversidade real de diaristas; paráfrases são aproximadas; sem dados de usuários reais (não coletados, por design). O benchmark mede busca, não interpretação — nenhuma métrica avalia "responder perguntas autobiográficas", comportamento explicitamente fora de escopo (AGENTS.md).
