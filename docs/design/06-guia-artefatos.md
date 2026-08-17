# Fio — Guia dos Artefatos Visuais

**Versão:** 1.0 · **Data:** 16 de agosto de 2026 · **Autor:** Manus AI

Este documento indexa os doze artefatos visuais produzidos para a reorganização da experiência do Fio, explicando o que cada um comunica, a qual fluxo se liga e onde ele deve ser usado como referência durante a implementação.

---

## 1. Wireframes de baixa fidelidade (`wireframes/`)

Os wireframes seguem o estilo esboço em grafite (papel e lápis) e servem para validar **estrutura, hierarquia e conteúdo** antes de qualquer investimento em polimento visual. São o material recomendado para discussão de trade-offs estruturais.

| Arquivo | Tela | O que comunica |
|---|---|---|
| `wf-01-home.png` | Home (estado vazio) | Wordmark, prompt canônico, campo de escrita, seletor temporal discreto em linha única e botão Guardar. Anotações laterais numeradas mapeiam cada zona da tela. |
| `wf-02-arquivo.png` | Arquivo | Cabeçalhos tipográficos por mês ("Agosto de 2026"), linhas com dia + trecho, indicadores finos de selo e de devolução programada, link "Voltar". |
| `wf-03-nota.png` | Nota aberta + menu contextual | Data em serifa, corpo respirado, e o bottom sheet do "⋯" com as seis ações (lembrar depois, descansar, não devolver, selar, editar, excluir). |
| `wf-04-timepicker.png` | Seletor temporal aberto | Sheet "Guardar como…" com os quatro modos, chips de duração, "Escolher data" e botão Aplicar. |
| `wf-05-devolucao.png` | Tela de devolução | Quase-vazio intencional: data, texto em container suave, fio botânico de fundo a baixa densidade e "Fechar". |
| `wf-06-config.png` | Configurações | Seções em captions maiúsculos (Privacidade, Devoluções, Dados, Preferências, Sobre) com células de lista e setas. |

## 2. Mockups de alta fidelidade (`mockups/`)

Os mockups aplicam integralmente o Sistema Verde-Sálvia (documento 3) e são a referência de **acabamento** para a implementação Compose. Todos em 9:16 (1440×2560), exceto `hi-06` que combina dois estados da mesma tela.

| Arquivo | Tela | Estado retratado |
|---|---|---|
| `hi-01-home.png` | Home | Vazia, com placeholder. **É a referência de estilo** — usar como imagem de referência para qualquer mockup futuro. |
| `hi-02-home-escrevendo.png` | Home | Escrevendo: linha de foco em sálvia, link "Volta em 1 ano" e pill "Guardado." flutuando. |
| `hi-03-timepicker.png` | Home + sheet | Sheet "Guardar como…" aberto sobre scrim escuro, com os quatro modos e chips de duração. |
| `hi-04-guardado.png` | Home | Momento exato da confirmação: pill "Guardado. Volta em 1 ano." com check em sálvia. |
| `hi-05-arquivo.png` | Arquivo | Lista tipográfica por mês com ícones discretos de selo (roseta) e de devolução (relógio). |
| `hi-06-nota.png` | Nota + menu | Leitura da nota com o menu contextual aberto — as seis ações com ícones finos e "Excluir" em terracota. |
| `hi-07-devolucao.png` | Devolução | Data em serifa + texto em card suave sobre o fio botânico a 15 %. |
| `hi-08-seladas.png` | Arquivo | Estado "selada": roseta, legenda "Selada — toque e confirme com a biometria para abrir". |
| `hi-09-configuracoes.png` | Configurações | Seções completas com toggle "Ocultar em recentes", detalhes e chevrons. |
| `hi-10-dark.png` | Home (dark) | Modo escuro verde-carvão `#1E2320` com sálvia claro `#A8B9A0` — confirma que o dark não é inversão do light. |

## 3. Regras de uso

Durante a implementação, os mockups valem como **fonte de verdade visual** e os wireframes como **fonte de verdade estrutural**. Em caso de divergência entre um mockup e o documento 3 (design system), o documento prevalece — o mockup foi gerado com base nele e pode conter pequenas variações de renderização. Novos mockups devem ser gerados sempre usando `hi-01-home.png` como referência de estilo para manter a consistência da linguagem sálvia-marfim.

Os artefatos devem ser enviados ao repositório em `docs/design/` (mockups) e `docs/design/wireframes/` (wireframes), referenciados por caminho absoluto nos documentos 1 a 5 com a sintaxe `![alt](../design/mockups/hi-XX.png)`.
