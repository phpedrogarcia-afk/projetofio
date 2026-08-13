#!/usr/bin/env python3
"""Generate the frozen M4 synthetic corpus and two blind annotation packets."""

from __future__ import annotations

import csv
import hashlib
import json
import random
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CORPUS_PATH = ROOT / "corpus" / "m4_pairs_v1.jsonl"
MANIFEST_PATH = ROOT / "corpus" / "manifest.json"
ANNOTATION_DIR = ROOT / "annotations"
HELDOUT_FAMILIES = {"amizade-distante", "casa-chuva", "mudanca-cidade", "silencio-familia"}
VARIANTS = (
    ("a", "Na semana passada, ", "Pouco tempo depois, "),
    ("b", "No começo do ano, ", "Meses depois, "),
    ("c", "Numa tarde de inverno, ", "Quando a estação mudou, "),
)


SCENARIOS = (
    {
        "id": "casa-chuva",
        "anchor": "voltei para casa debaixo de chuva e deixei o casaco secando perto da porta.",
        "indirect": "o cheiro de terra molhada trouxe de volta a sensação de finalmente chegar a um lugar conhecido.",
        "near": "cheguei em casa sob a chuva e pendurei o casaco molhado ao lado da porta.",
        "trap": "consultei a previsão de chuva antes de escolher a tinta para a porta da casa.",
        "negation": "não choveu, não usei casaco e passei o dia inteiro longe de casa.",
        "rating": 2,
    },
    {
        "id": "amizade-distante",
        "anchor": "recebi uma mensagem de uma amizade antiga e demorei muito antes de responder.",
        "indirect": "reencontrar uma voz conhecida mostrou quanto espaço o silêncio tinha criado entre nós.",
        "near": "uma pessoa amiga de muitos anos escreveu, e eu hesitei bastante antes de responder.",
        "trap": "o aplicativo demorou para enviar uma mensagem sobre uma promoção de amizade virtual.",
        "negation": "não recebi mensagem alguma e respondi imediatamente a todas as conversas novas.",
        "rating": 3,
    },
    {
        "id": "mudanca-cidade",
        "anchor": "andei por uma rua desconhecida e percebi que a nova cidade começava a parecer minha.",
        "indirect": "aprendi o caminho sem abrir o mapa e isso fez a mudança parecer menos provisória.",
        "near": "caminhei por uma rua que não conhecia e senti que começava a pertencer à cidade nova.",
        "trap": "a prefeitura mudou o nome de uma rua no mapa oficial da cidade.",
        "negation": "não saí de casa e a cidade continuou completamente estranha para mim.",
        "rating": 3,
    },
    {
        "id": "silencio-familia",
        "anchor": "durante o almoço em família, uma pausa longa disse mais do que a conversa inteira.",
        "indirect": "ninguém tocou no assunto, mas todos pareciam saber por que a mesa ficou quieta.",
        "near": "no almoço com a família, um silêncio comprido falou mais que todas as frases.",
        "trap": "comprei uma mesa grande para reduzir o barulho durante o almoço da família.",
        "negation": "o almoço foi barulhento, sem pausa alguma, e ninguém ficou em silêncio.",
        "rating": 3,
    },
    {
        "id": "onibus-retorno",
        "anchor": "peguei o ônibus de volta sem pressa e observei as luzes passando pela janela.",
        "indirect": "o trajeto conhecido me deu alguns minutos em que eu não precisava decidir nada.",
        "near": "voltei devagar de ônibus, olhando as luzes correrem do outro lado da janela.",
        "trap": "a empresa trocou as luzes internas e instalou janelas novas em todos os ônibus.",
        "negation": "não entrei no ônibus, tive muita pressa e não olhei pela janela.",
        "rating": 2,
    },
    {
        "id": "cafe-manha",
        "anchor": "fiz café cedo e fiquei alguns minutos acompanhando a claridade chegar à cozinha.",
        "indirect": "antes das outras pessoas acordarem, a casa pareceu oferecer um intervalo só meu.",
        "near": "preparei café de manhã e observei a luz entrar aos poucos na cozinha.",
        "trap": "comparei a claridade de duas lâmpadas para fotografar uma xícara de café na cozinha.",
        "negation": "acordei tarde, não fiz café e mantive a cozinha completamente escura.",
        "rating": 2,
    },
    {
        "id": "livro-esquecido",
        "anchor": "abri um livro esquecido na estante e encontrei uma anotação que eu já não lembrava.",
        "indirect": "uma frase antiga na margem parecia ter esperado em silêncio até eu mudar de opinião.",
        "near": "peguei na estante um livro antigo e achei uma nota de que não me recordava.",
        "trap": "organizei a estante por tamanho e anotei quantos livros ainda faltavam catalogar.",
        "negation": "não abri nenhum livro e lembrava perfeitamente de todas as anotações guardadas.",
        "rating": 3,
    },
    {
        "id": "trabalho-recusa",
        "anchor": "recusei uma tarefa extra no trabalho e saí da conversa com as mãos tremendo.",
        "indirect": "dizer que eu não podia pareceu pequeno por fora e enorme enquanto eu voltava à mesa.",
        "near": "neguei um trabalho adicional e terminei a conversa ainda com as mãos trêmulas.",
        "trap": "o sistema recusou uma tarefa porque a mesa de trabalho estava sem conexão.",
        "negation": "aceitei todas as tarefas extras e saí da conversa completamente tranquilo.",
        "rating": 3,
    },
    {
        "id": "planta-varanda",
        "anchor": "notei uma folha nova na planta da varanda depois de semanas pensando que ela não resistiria.",
        "indirect": "continuei regando sem sinal de mudança, até perceber que a espera também fazia parte do cuidado.",
        "near": "depois de achar que a planta morreria, vi uma folha nova surgindo na varanda.",
        "trap": "a loja recebeu uma planta de varanda com folhas impressas no catálogo da semana.",
        "negation": "não apareceu folha nova e eu nunca pensei que a planta estivesse em risco.",
        "rating": 2,
    },
    {
        "id": "musica-cozinha",
        "anchor": "uma música antiga tocou enquanto eu lavava a louça e interrompeu meus movimentos por um instante.",
        "indirect": "a melodia trouxe uma sala que já não existe para dentro daquela cozinha comum.",
        "near": "parei por um momento ao ouvir uma canção antiga enquanto lavava a louça.",
        "trap": "troquei a música do anúncio de detergente exibido na televisão da cozinha.",
        "negation": "lavei tudo em silêncio e nenhuma música alterou o ritmo dos meus movimentos.",
        "rating": 3,
    },
    {
        "id": "consulta-espera",
        "anchor": "esperei por uma consulta olhando o relógio e tentando não imaginar respostas antes da hora.",
        "indirect": "cada pessoa chamada pelo nome fazia a sala parecer mais vazia e a espera mais alta.",
        "near": "na espera da consulta, conferi o relógio e evitei antecipar qualquer resposta.",
        "trap": "a consulta do relógio respondeu corretamente ao horário de funcionamento da sala.",
        "negation": "fui atendido sem esperar e inventei várias respostas muito antes de entrar.",
        "rating": 2,
    },
    {
        "id": "praia-inverno",
        "anchor": "caminhei pela praia vazia no frio e ouvi meus passos com uma nitidez incomum.",
        "indirect": "sem o movimento do verão, aquele lugar conhecido parecia ter guardado outra voz.",
        "near": "andei na praia deserta durante o frio e escutei claramente cada passo.",
        "trap": "o relatório mediu a nitidez do áudio de passos usado numa campanha da praia de inverno.",
        "negation": "a praia estava lotada, fazia calor e não consegui ouvir meus próprios passos.",
        "rating": 2,
    },
    {
        "id": "receita-avo",
        "anchor": "tentei repetir uma receita da minha avó e percebi que as medidas nunca tinham sido escritas.",
        "indirect": "o sabor ficou diferente, mas o gesto de ajustar tudo aos poucos parecia familiar.",
        "near": "refiz a receita da avó e descobri que ninguém havia registrado as quantidades.",
        "trap": "escrevi as medidas do armário onde guardo o caderno de receitas da minha avó.",
        "negation": "segui medidas exatas de uma receita impressa que não tinha relação com minha família.",
        "rating": 3,
    },
    {
        "id": "chave-antiga",
        "anchor": "encontrei uma chave antiga numa gaveta e não consegui lembrar qual porta ela abria.",
        "indirect": "guardei o objeto outra vez porque descartar aquela dúvida pareceu mais difícil que mantê-la.",
        "near": "achei numa gaveta uma chave velha e não recordei a porta correspondente.",
        "trap": "a gaveta nova veio com uma chave e um manual para regular a abertura da porta.",
        "negation": "não encontrei chave alguma e sabia exatamente como abrir todas as portas.",
        "rating": 2,
    },
    {
        "id": "telefone-pai",
        "anchor": "liguei para meu pai sem assunto específico e ficamos falando sobre coisas pequenas.",
        "indirect": "a conversa não resolveu nada, mas a voz do outro lado deixou o dia menos distante.",
        "near": "telefonei para meu pai sem motivo definido e conversamos sobre detalhes comuns.",
        "trap": "meu pai comprou um telefone pequeno porque o aparelho anterior não resolvia chamadas.",
        "negation": "não fiz ligação e passei o dia evitando qualquer conversa com meu pai.",
        "rating": 2,
    },
    {
        "id": "fotografia-caixa",
        "anchor": "uma fotografia caiu de uma caixa e mostrou um rosto meu que eu quase não reconheci.",
        "indirect": "demorei para aceitar que aquela expressão também tinha sido uma forma de estar no mundo.",
        "near": "vi cair da caixa uma foto em que meu próprio rosto parecia quase desconhecido.",
        "trap": "a caixa da câmera reconheceu automaticamente um rosto para ajustar a fotografia.",
        "negation": "nenhuma fotografia apareceu e reconheci imediatamente todos os rostos que vi.",
        "rating": 3,
    },
    {
        "id": "janela-vizinha",
        "anchor": "a vizinha acenou da janela depois de meses em que apenas cruzávamos o corredor.",
        "indirect": "um gesto breve mudou a sensação de anonimato que o prédio tinha acumulado.",
        "near": "após meses só passando pelo corredor, recebi um aceno da vizinha na janela.",
        "trap": "o corredor recebeu uma janela nova conforme o pedido técnico da vizinha.",
        "negation": "a vizinha não acenou e nunca havíamos nos cruzado no corredor.",
        "rating": 2,
    },
    {
        "id": "curso-desistencia",
        "anchor": "fechei o material de um curso que eu já não queria terminar e senti alívio antes de culpa.",
        "indirect": "deixar uma escolha incompleta abriu um espaço que eu vinha tentando preencher por obrigação.",
        "near": "abandonei o curso que não desejava concluir e o alívio chegou antes da culpa.",
        "trap": "o material do curso explicava como medir culpa e alívio num exercício de vocabulário.",
        "negation": "terminei o curso com entusiasmo e não senti alívio nem culpa ao fechar o material.",
        "rating": 3,
    },
    {
        "id": "cachorro-parque",
        "anchor": "um cachorro desconhecido sentou ao meu lado no parque e ficou ali até eu levantar.",
        "indirect": "por alguns minutos, a companhia não exigiu apresentação, conversa ou promessa.",
        "near": "no parque, um cachorro que eu não conhecia permaneceu ao meu lado até minha saída.",
        "trap": "o parque instalou uma placa ao lado do lugar reservado para cachorro desconhecido.",
        "negation": "não havia cachorro no parque e passei o tempo inteiro caminhando sem sentar.",
        "rating": 2,
    },
    {
        "id": "caixa-mudanca",
        "anchor": "deixei uma caixa da mudança fechada porque ainda não sabia onde colocar aquelas coisas.",
        "indirect": "nem tudo que veio comigo parecia pronto para ganhar lugar na casa nova.",
        "near": "mantive fechada uma caixa da mudança por não saber onde guardar seu conteúdo.",
        "trap": "a transportadora indicou onde colocar a etiqueta na caixa usada durante a mudança.",
        "negation": "abri todas as caixas imediatamente e já conhecia o lugar exato de cada objeto.",
        "rating": 2,
    },
)


def _sentence(prefix: str, text: str) -> str:
    return prefix + text[0].upper() + text[1:]


def build_records() -> list[dict]:
    records: list[dict] = []
    for scenario_index, scenario in enumerate(SCENARIOS):
        split = "heldout" if scenario["id"] in HELDOUT_FAMILIES else "development"
        unrelated_scenario = SCENARIOS[(scenario_index + 7) % len(SCENARIOS)]
        for variant, anchor_prefix, candidate_prefix in VARIANTS:
            group_id = f"{scenario['id']}-{variant}"
            anchor = _sentence(anchor_prefix, scenario["anchor"])
            candidates = (
                ("indirect_relation", scenario["indirect"], scenario["rating"], False),
                ("near_duplicate", scenario["near"], 3, True),
                ("lexical_trap", scenario["trap"], 1, False),
                ("negation", scenario["negation"], 0, False),
                ("unrelated", unrelated_scenario["anchor"], 0, False),
            )
            for ordinal, (category, text, rating, near_duplicate) in enumerate(candidates, 1):
                records.append(
                    {
                        "schemaVersion": 1,
                        "pairId": f"m4-{scenario['id']}-{variant}-{ordinal}",
                        "split": split,
                        "scenarioFamily": scenario["id"],
                        "groupId": group_id,
                        "category": category,
                        "anchor": anchor,
                        "candidate": _sentence(candidate_prefix, text),
                        "authoringTargetRating": rating,
                        "expectedNearDuplicate": near_duplicate,
                        "humanRatings": [],
                        "goldRating": None,
                        "provenance": "purpose-written-synthetic-m4-v1",
                    }
                )
    return records


def _write_jsonl(records: list[dict]) -> bytes:
    payload = "".join(json.dumps(record, ensure_ascii=False, sort_keys=True) + "\n" for record in records)
    encoded = payload.encode("utf-8")
    CORPUS_PATH.write_bytes(encoded)
    return encoded


def _write_packet(records: list[dict], label: str, seed: int) -> tuple[Path, list[dict]]:
    rows = list(records)
    random.Random(seed).shuffle(rows)
    path = ANNOTATION_DIR / f"annotator-{label}.csv"
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(
            handle,
            fieldnames=("packet_row", "pair_id", "anchor", "candidate", "rating", "near_duplicate", "reason_code"),
        )
        writer.writeheader()
        for index, record in enumerate(rows, 1):
            writer.writerow(
                {
                    "packet_row": index,
                    "pair_id": record["pairId"],
                    "anchor": record["anchor"],
                    "candidate": record["candidate"],
                    "rating": "",
                    "near_duplicate": "",
                    "reason_code": "",
                }
            )
    return path, rows


def _write_html_packet(records: list[dict], label: str) -> Path:
    template_path = ANNOTATION_DIR / "annotation-template.html"
    template = template_path.read_text(encoding="utf-8")
    visible = [
        {"pair_id": record["pairId"], "anchor": record["anchor"], "candidate": record["candidate"]}
        for record in records
    ]
    html = template.replace("__PACKET_LABEL__", label.upper()).replace(
        "__PACKET_JSON__", json.dumps(visible, ensure_ascii=False).replace("</", "<\\/")
    )
    path = ANNOTATION_DIR / f"annotator-{label}.html"
    path.write_text(html, encoding="utf-8")
    return path


def main() -> None:
    records = build_records()
    CORPUS_PATH.parent.mkdir(parents=True, exist_ok=True)
    ANNOTATION_DIR.mkdir(parents=True, exist_ok=True)
    encoded = _write_jsonl(records)
    packet_a, rows_a = _write_packet(records, "a", 41041)
    packet_b, rows_b = _write_packet(records, "b", 41042)
    html_a = _write_html_packet(rows_a, "a")
    html_b = _write_html_packet(rows_b, "b")
    manifest = {
        "schemaVersion": 1,
        "corpus": CORPUS_PATH.name,
        "sha256": hashlib.sha256(encoded).hexdigest(),
        "recordCount": len(records),
        "developmentCount": sum(r["split"] == "development" for r in records),
        "heldoutCount": sum(r["split"] == "heldout" for r in records),
        "scenarioFamilyCount": len({r["scenarioFamily"] for r in records}),
        "groupCount": len({r["groupId"] for r in records}),
        "annotationPackets": [packet_a.name, packet_b.name],
        "annotationInterfaces": [html_a.name, html_b.name],
        "labels": "synthetic-authoring-targets-only",
        "humanGate": "open",
    }
    MANIFEST_PATH.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(manifest, ensure_ascii=False))


if __name__ == "__main__":
    main()
