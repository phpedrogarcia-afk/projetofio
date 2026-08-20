# CODEX EFFICIENCY — final report

Status: COMPLETE on `codex/context-efficiency-v1`

Date: 2026-08-20

## Problemas encontrados

- 152 arquivos Markdown sem uma classificação central; 29/50 planos, 31/45
  docs e 10/10 packets não declaravam status no próprio arquivo.
- `AGENTS.md` misturava 180 linhas de regras e história e exigia três product
  docs antes de qualquer tarefa.
- `FioApp.kt` concentrava 2.088 linhas e sete superfícies independentes.
- A precedência documental dizia simultaneamente “ADR vence” e “código sempre
  vence”, deixando drift versus decisão ambíguo.
- Packets READY não existiam como arquivos ou não limitavam leitura/mudança.
- Atlas continha valores voláteis desatualizados (linhas de UI, target SDK,
  branch/hash de instalação).

Diagnóstico detalhado: `plans/CONTEXT-EFFICIENCY-AUDIT.md`.

## Duplicação removida

- `AGENTS.md` caiu de 180 para 84 linhas: saiu a narrativa das Missões 1–5;
  ficaram invariantes, stop conditions, roteamento e workflow.
- A fila tem uma autoridade: `packets/EXECUTION-QUEUE.md`; `NEXT-WORK` é resumo.
- `docs/INDEX.md` classifica famílias sem adicionar status a dezenas de arquivos
  históricos ou mover paths.
- `docs/TEST-LEVELS.md` virou o único router operacional de testes; TEST-MAP é
  inventário de referência.
- Atlas e packets passaram a referenciar invariantes/ADRs, não recopiá-los.

## Contexto obrigatório antes/depois

Linhas são aproximações de arquivos inteiros, não tokens. O Atlas já oferecia
algum roteamento antes; o principal custo removido foi o boot obrigatório e o
monólito de UI.

| Simulação | Antes | Depois | Descoberta |
|---|---|---|---|
| A — alterar copy da Home | 5 arquivos, ~2.913 linhas: AGENTS + 01/02/03 + `FioApp.kt` | 3 arquivos, 483 linhas: AI start + UI instructions + `HomeScreen.kt`; packet/teste são adicionados para executar | caminho direto, sem `rg` global |
| B — alterar comportamento lexical de Search | 10 arquivos, ~3.441 linhas incluindo boot, mapas, monólito, service e teste | 6 arquivos, 603 linhas: AI start + P06 + Search Map + arquitetura + service + teste | packet fornece source/test exatos |
| C — corrigir regra de Returns/P19 | 8 arquivos, ~1.673 linhas incluindo boot amplo | 5 arquivos, 634 linhas: AI start + P19 + Time Map + engine + teste | chega ao blocker “DECISION REQUIRED” sem ler UI/Search/Crypto |

Tool calls de descoberta simulados: antes, 4–8 aberturas/buscas dirigidas após o
boot; depois, 1 abertura do entrypoint e 1 do packet, seguidas dos paths já
declarados. Tempo humano não foi cronometrado e tokens não foram inventados.

## Arquivos modularizados

| Responsabilidade | Arquivo | Linhas após extração |
|---|---|---:|
| Orquestração/navegação | `ui/FioApp.kt` | 164 |
| Guardar e tempo visual | `ui/home/HomeScreen.kt` | 397 |
| Encontrar | `ui/search/SearchScreen.kt` | 298 |
| Arquivo | `ui/archive/ArchiveScreen.kt` | 251 |
| Nota/edição/exclusão | `ui/entry/EntryDetailScreen.kt` | 195 |
| Return | `ui/returns/ReturnScreen.kt` | 81 |
| Ajustes | `ui/settings/SettingsScreen.kt` | 733 |
| Privacy/app lock | `ui/security/PrivacyScreens.kt` | 97 |
| Compartilhados restritos | `ui/components/FioComponents.kt` | 93 |

Todos continuam no mesmo pacote Kotlin. Nenhum framework, módulo, DI, schema,
dependência, copy, aparência, navegação ou comportamento foi introduzido.
Search não depende mais do arquivo de Archive.

## Docs classificados

- ACTIVE: produto, decisões, specs, estado, fila, router e test levels.
- REFERENCE: Atlas por área, design, security e research quando o packet aponta.
- HISTORICAL: relatórios Manus, campaigns, evidence e planos concluídos.
- SUPERSEDED: linha Apple/iOS substituída por ADR-033–036.

História não foi apagada nem movida em massa; links permanecem estáveis.

## Novo caminho de boot

```text
AGENTS.md (regras automáticas)
→ AI-START-HERE.md
→ packets/EXECUTION-QUEUE.md
→ packet NOW/READY
→ READ FIRST
→ source/testes nomeados
```

Full-repo scan só para auditoria, arquitetura desconhecida ou evidência
contraditória. Pesquisa e relatórios históricos saíram do boot normal.

## Packet improvements

O template agora exige `READ FIRST`, `OPTIONAL`, `DO NOT READ`, arquivos
esperados, comandos exatos e budget SMALL/MEDIUM/LARGE. Context packs criados
para FIO-PQ-03, P06, P07 e P10; P19 ganhou preâmbulo SMALL de decisão. Device
items sem packet deixaram de aparecer como READY executável.

## Test routing

`docs/TEST-LEVELS.md` define L0 compile, L1 específico, L2 unit suite, L3
pre-merge e L4 device/human, com roteamento por Search, Returns, Crypto, Room,
Import, Export e UI.

## Fresh-agent test

Método: tratar `AI-START-HERE.md` como única entrada, escolher uma linha do
router e validar o path sem `rg --files` ou scan do repositório.

Resultados:

- Home → `ui/home/HomeScreen.kt`: encontrado.
- Search → `ui/search/SearchScreen.kt` + `search/`: encontrados.
- Archive → `ui/archive/ArchiveScreen.kt`: encontrado.
- Returns → `domain/TimeReturnEngine.kt` + `TimeReturnsService.kt`: encontrados.
- Settings → `ui/settings/SettingsScreen.kt`: encontrado.
- Room → `persistence/` + `schemas/`: encontrados.

O teste “altere somente Archive” chega a um arquivo de 251 linhas e não precisa
abrir Home, Search, Return ou Settings.

## Baseline final

- Debug e release compilam.
- 134/134 testes unitários passam; 0 falhas/erros/ignorados.
- 28/28 instrumentados passam no AVD Android 8/API 26 após estado limpo.
- A execução agregada inicial reproduziu uma flutuação de estado já conhecida
  em `AccessibilityContractTest`; a classe isolada passou 2/2 e a suíte completa
  repetida em estado limpo passou 28/28.
- Nenhum teste foi removido, ignorado ou reescrito para aceitar a extração.

## Riscos

- `SettingsScreen.kt` ainda tem 733 linhas, mas agrupa uma única superfície com
  subpáginas coesas; dividir agora criaria fragmentação sem ganho comprovado.
- Mapas do Atlas continuam snapshots e podem envelhecer; o router evita tratá-los
  como norma.
- Instruções hierárquicas são suportadas oficialmente, mas só entram na cadeia
  quando o Codex trabalha no caminho correspondente; por isso existe também o
  router explícito na raiz.
- P19 continua P0/DECISION REQUIRED; esta missão não corrigiu nem ocultou o gap.

## Próxima tarefa recomendada

Fechar a escolha humana de FIO-P19 usando apenas seu context pack SMALL. Se a
decisão continuar pendente, promover explicitamente um dos packets técnicos
READY (P06, P07 ou P10) em `EXECUTION-QUEUE.md`; não iniciar por inferência.
