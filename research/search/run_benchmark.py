#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
FIO-SEARCH-BENCHMARK runner — pesquisa Mission 4 only.
Replicates app LexicalTokenizer (NFD + Unicode Mn strip, prefix only on the
LAST query token) and evaluates LEXICAL vs SEMANTIC (EmbeddingGemma-300M)
vs HYBRID on the synthetic PT-BR corpus in FIO-SEARCH-BENCHMARK.md.

Runs OUTSIDE the Android build. Never ships with the app.

Usage:
  python3 run_benchmark.py               # lexical baseline only (no model download)
  python3 run_benchmark.py --semantic    # downloads EmbeddingGemma weights (~600MB hf)
"""
import unicodedata, re, sys, math, argparse, time, os, json

# ----------------------------------------------------------------------------
# 1. Corpus + queries (source of truth: FIO-SEARCH-BENCHMARK.md §2-3)
# ----------------------------------------------------------------------------
ENTRIES = [
"E01|Ontem o café estava ótimo. Tomei com a vó na varanda e conversamos sobre o jardim dela.",
"E02|Não consegui dormir direito. Fiquei acordada até as 3 pensando na apresentação de quinta.",
"E03|Reunião difícil hoje: o projeto de redesign ficou para próxima semana. Saí exausto.",
"E04|Finalmente fui no médico. Ele pediu exames de sangue e disse que a pressão está controlada.",
"E05|Liguei para a mãe depois de tanto tempo. Ela estava feliz, falou da horta.",
"E06|Choveu a tarde inteira. Fiquei olhando pela janela com um chá de camomila.",
"E07|Entrega do projeto finalmente aprovada! O time comemorou com um almoço.",
"E08|Dormi mal de novo. O bebê acordou quatro vezes.",
"E09|Caminhada no parque hoje pela primeira vez em um mês. O ar estava frio e bom.",
"E10|Reuni com o gerente para negociar o prazo. Saiu melhor do que eu esperava.",
"E11|A conversa com a vó me fez lembrar da infância. Ela ainda tem o mesmo sorriso.",
"E12|O exame de sangue veio normal. Que alívio.",
"E13|Terça foi cansativa: duas reuniões seguidas e quase sem pausa para comer.",
"E14|Fiz um chá de camomila e dormi cedo. Funcionou dessa vez.",
"E15|O parque está lindo no outono. As folhas amarelaram.",
"E16|O bebê finalmente dormiu a noite inteira. Chorei de emoção com a Bia.",
"E17|Apresentei o redesign para a diretoria. Fiquei nervoso mas foi bem.",
"E18|A horta da vó tem tomate, manjericão e alecrim. Ela ensinou a plantar.",
"E19|Pressão alta outra vez. Preciso cortar o café e dormir mais cedo.",
"E20|Almoço com o time: pizza e risadas sobre o perrengue da semana passada.",
"E21|Não durmo bem quando penso demais no trabalho.",
"E22|O médico elogiou os exames. Continuar com a medicação.",
"E23|Falei com a mãe sobre o Natal. Vamos fazer a ceia na casa dela.",
"E24|Chá de camomila virou meu ritual antes de dormir.",
"E25|Andei 40 minutos no parque. Meu coração agradeceu.",
"E26|O prazo do redesign mudou de novo. Segunda vez em duas semanas.",
"E27|A vó está com 87 e ainda cuida sozinha do jardim.",
"E28|Bebê com febre ontem. Passamos a noite acordados.",
"E29|Reunião de alinhamento foi curta e produtiva. Raro.",
"E30|Cortei o café conforme o médico pediu. Chá virou o substituto.",
"E31|A apresentação de quinta foi um sucesso. Recebi elogios.",
"E32|Dormi bem pela primeira vez em semanas. Sem pensar em nada.",
"E33|O time aceitou minha proposta de cronograma. Vitória pequena mas real.",
"E34|Mãe mandou fotos do novo jardim dela. Fiquei orgulhosa.",
"E35|Exausto depois da caminhada, mas feliz.",
"E36|Segunda-feira pesada: três reuniões e um reporte inesperado.",
"E37|O bebê dormiu 6 horas seguidas. Registrei no app de sono.",
"E38|Varanda com sol, livro, café sem cafeína. Manhã perfeita.",
"E39|Negociação de prazo funcionou: ganhamos duas semanas.",
"E40|Feito o acompanhamento médico. Tudo em ordem por agora.",
"E41|Café da manhã com frutas e pão de queijo. Rotina de domingo.",
"E42|A reunião de segunda mudou para quinta. Confuso.",
"E43|Dormi no sofá vendo filme. O bebê dormiu junto.",
"E44|O médico marcou retorno para março.",
"E45|Vó contou histórias da fazenda. Quase duas horas.",
"E46|Caminhada na chuva, capuz, sem celular. Silêncio bom.",
"E47|Redesign aprovado com ressalvas. Ajustes até sexta.",
"E48|Chá antes de deitar, como sempre. 11 da noite.",
"E49|A mãe ligou no aniversário dela. Conversei 40 minutos.",
"E50|Entrega atrasada por causa da reunião de emergência.",
"E51|O parque amanheceu enevoado. Andei devagar.",
"E52|Bebê mamou bem e riu pela primeira vez.",
"E53|Pressão 13x8 hoje. Melhor que a semana passada.",
"E54|O time resolveu o bug crítico em um dia.",
"E55|Café com a vizinha depois da caminhada.",
"E56|Dormi ouvindo podcast. Desliguei o despertador duas vezes.",
"E57|A horta deu o primeiro tomate. A vó chorou.",
"E58|Exames de rotina feitos. Colesterol ok.",
"E59|A reunião virou debate acalorado. Consegui manter a calma.",
"E60|Domingo inteiro sem tela. Li um capítulo do livro.",
]

QUERIES = [
("Q01", "café", ["E01","E38","E55"], "lex"),
("Q02", "reunião exausto", ["E03","E36"], "lex"),
("Q03", "dormi mal insônia", ["E02","E08","E21"], "sem"),
("Q04", "como me senti depois da apresentação", ["E17","E31"], "sem"),
("Q05", "médico exames pressão", ["E04","E12","E22","E40","E53","E58"], "lex"),
("Q06", "conversa com a mãe", ["E05","E23","E49"], "lex"),
("Q07", "momentos com a vó", ["E01","E11","E18","E27","E45","E57"], "sem"),
("Q08", "chá de camomila", ["E06","E14","E24","E30","E48"], "lex"),
("Q09", "parque caminhada ar livre", ["E09","E15","E25","E35","E46","E51","E55"], "lex"),
("Q10", "reunião com gerente prazo", ["E10","E39"], "lex"),
("Q11", "noite difícil bebê acordando", ["E08","E16","E28","E36","E37","E43"], "sem"),
("Q12", "almoço com colegas", ["E07","E20"], "lex"),
("Q13", "quero dormir melhor", ["E02","E08","E14","E21","E24","E30","E32","E43","E48","E56"], "sem"),
("Q14", "como anda a saúde", ["E04","E12","E22","E40","E53","E58"], "sem"),
("Q15", "relação com a mãe", ["E05","E23","E34","E49"], "sem"),
("Q16", "redesign projeto entrega", ["E03","E17","E26","E31","E33","E47","E50"], "lex"),
("Q17", "jardim horta da vó", ["E01","E05","E18","E27","E34","E57"], "lex"),
("Q18", "domingo sem telas", ["E41","E60"], "sem"),
("Q19", "chuvoso tarde", ["E06"], "lex"),
("Q20", "negociação de prazo", ["E10","E39"], "lex"),
("Q21", "como foi o exame", ["E12","E22","E53","E58"], "sem"),
("Q22", "primeiro riso do bebê", ["E16","E37","E52"], "sem"),
("Q23", "ansiedade antes de apresentação", ["E02","E17"], "sem"),
("Q24", "ritual da noite", ["E14","E24","E30","E43","E48","E56"], "sem"),
("Q25", "conquista do time", ["E07","E33","E54"], "sem"),
("Q26", "cafeína cortar café", ["E19","E30","E38"], "sem"),
("Q27", "segunda-feira difícil", ["E13","E36"], "lex"),
("Q28", "outono folhas", ["E15","E51"], "lex"),
("Q29", "natal ceia família", ["E23","E27","E45","E49","E57"], "sem"),
("Q30", "silêncio solidão boa", ["E46","E60"], "sem"),
]

# ----------------------------------------------------------------------------
# 2. LexicalTokenizer replica (app/search/LexicalTokenizer.kt)
# ----------------------------------------------------------------------------
def normalize(text: str) -> str:
    nfd = unicodedata.normalize("NFD", text)
    stripped = "".join(c for c in nfd if unicodedata.combining(c) == 0
                       and unicodedata.category(c) != "Mn")
    return stripped.lower()

def tokenize(text: str):
    return [(m.start(), m.group()) for m in
            re.finditer(r"[A-Za-z0-9]+", normalize(text))]

def contains_query_tokens(doc_norm_tokens, query_tokens):
    q = 0
    last_match_idx = -1
    nq = len(query_tokens)
    for idx, tok in doc_norm_tokens:
        wanted = query_tokens[q]
        last = (q == nq - 1)
        matches = tok.startswith(wanted) if last else tok == wanted
        if matches and idx > last_match_idx:
            last_match_idx = idx
            q += 1
            if q == nq:
                return True
    return False

def lexical_rank(corpus, query: str, max_results=10):
    """Returns (results, latency_ms) — same contract as app LocalSearchService."""
    toks = tokenize(query)
    t0 = time.perf_counter()
    scored = []
    for entry_id, text in corpus:
        doc = tokenize(text)
        if contains_query_tokens(doc, [t for _, t in toks]):
            scored.append((0.0, entry_id))  # lexical: uniform relevance, recency ignored
    scored.sort(key=lambda x: -x[0])
    lat = (time.perf_counter() - t0) * 1000
    return [e for _, e in scored[:max_results]], lat

# ----------------------------------------------------------------------------
# 3. Semantic (EmbeddingGemma via sentence-transformers proxy)
# ----------------------------------------------------------------------------
_SEM_MODEL = None
def _load_semantic():
    global _SEM_MODEL
    if _SEM_MODEL is None:
        from sentence_transformers import SentenceTransformer
        # EmbeddingGemma-300M is not on sentence-transformers; use the closest
        # publicly-hostable multilingual proxy: BAAI/bge-m3 is too heavy;
        # sentence-transformers hosts `intfloat/multilingual-e5-large` (560M).
        # For the Gemma-class size, use `jinaai/jina-embeddings-v3` (572M) only
        # if present; fallback documented in RESEARCH-LOG.md.
        try:
            _SEM_MODEL = SentenceTransformer("intfloat/multilingual-e5-large")
        except Exception as exc:
            sys.exit(f"Semantic model unavailable: {exc}")
    return _SEM_MODEL

def _e5_prefix(t):
    return "query: " + t if t.startswith("query") else t

def semantic_rank(corpus, query: str, max_results=10):
    model = _load_semantic()
    t0 = time.perf_counter()
    docs = [t for _, t in corpus]
    embs = model.encode([f"query: {query}"] + docs, normalize_embeddings=True)
    sims = embs[1:].dot(embs[0])
    lat = (time.perf_counter() - t0) * 1000
    order = sorted(range(len(sims)), key=lambda i: -sims[i])
    return [corpus[i][0] for i in order[:max_results]], lat

def hybrid_rank(corpus, query: str, max_results=10, k=60):
    lex_res, lex_lat = lexical_rank(corpus, query, max_results=len(corpus))
    sem_res, sem_lat = semantic_rank(corpus, query, max_results=len(corpus))
    def rrf_score(rank1, rank2=None):
        s = 1.0 / (k + rank1)
        if rank2 is not None:
            s += 1.0 / (k + rank2)
        return s
    lex_idx = {e: i for i, e in enumerate(lex_res)}
    sem_idx = {e: i for i, e in enumerate(sem_res)}
    all_entries = list(dict.fromkeys(lex_res + sem_res))
    scored = [(rrf_score(lex_idx.get(e, 999), sem_idx.get(e, 999)), e) for e in all_entries]
    scored.sort(key=lambda x: -x[0])
    return [e for _, e in scored[:max_results]], lex_lat + sem_lat

# ----------------------------------------------------------------------------
# 4. Metrics
# ----------------------------------------------------------------------------
def recall_at(ranked, gt, k):
    if not gt:
        return 1.0
    top = set(ranked[:k])
    return len(top & set(gt)) / len(gt)

def precision_at(ranked, gt, k):
    if k == 0:
        return 0.0
    top = ranked[:k]
    return len(set(top) & set(gt)) / k

def mrr(ranked, gt):
    g = set(gt)
    for i, e in enumerate(ranked):
        if e in g:
            return 1.0 / (i + 1)
    return 0.0

# ----------------------------------------------------------------------------
# 5. Runner
# ----------------------------------------------------------------------------
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--semantic", action="store_true",
                    help="run SEMANTIC + HYBRID arms (downloads ~2GB model)")
    ap.add_argument("--out", default="benchmark_results.json")
    args = ap.parse_args()

    corpus = [(e.split("|", 1)[0], e.split("|", 1)[1]) for e in ENTRIES]
    results = {"configurations": {}, "per_query": {}}
    arms = [("lexical_v1", lexical_rank)]
    if args.semantic:
        arms += [("semantic_proxy_e5_large", semantic_rank),
                 ("hybrid_e5_large_rrf60", hybrid_rank)]

    for name, rank_fn in arms:
        recalls5, recalls10, prec5s, mrrs, lats = [], [], [], [], []
        for qid, query, gt, qtype in QUERIES:
            res, lat = rank_fn(corpus, query)
            recalls5.append(recall_at(res, gt, 5))
            recalls10.append(recall_at(res, gt, 10))
            prec5s.append(precision_at(res, gt, 5))
            mrrs.append(mrr(res, gt))
            lats.append(lat)
            results["per_query"].setdefault(qid, {})[name] = {
                "recall5": recalls5[-1], "recall10": recalls10[-1],
                "precision5": prec5s[-1], "mrr": mrrs[-1]}
        import statistics as st
        results["configurations"][name] = {
            "recall@5_mean": round(st.mean(recalls5), 3),
            "recall@10_mean": round(st.mean(recalls10), 3),
            "precision@5_mean": round(st.mean(prec5s), 3),
            "mrr_mean": round(st.mean(mrrs), 3),
            "latency_ms_p95": round(sorted(lats)[int(0.95 * len(lats))], 1)}
        print(f"{name:32s} R@5={st.mean(recalls5):.3f} R@10={st.mean(recalls10):.3f} "
              f"P@5={st.mean(prec5s):.3f} MRR={st.mean(mrrs):.3f} p95ms={sorted(lats)[int(0.95*len(lats))]:.0f}")

    with open(os.path.join(os.path.dirname(__file__) or ".", args.out), "w") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print("saved", args.out)

if __name__ == "__main__":
    main()
