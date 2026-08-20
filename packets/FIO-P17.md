# FIO-P17 — reorganizar navegação primária, Encontrar e Arquivo

**Status:** DONE
**Tipo:** code
**Branch:** `feature/fio-primary-navigation-20260820`
**Evidência mínima exigida:** Guardar, Encontrar e Arquivo acessíveis em um toque; fluxo temporal preservado; busca separada do Arquivo; ações destrutivas fora da lista; testes unitários verdes e APK validado no aparelho autorizado.

## 1. Objetivo

Corrigir a arquitetura de informação que esconde Arquivo no menu de overflow e mistura Encontrar com a lista cronológica. Criar uma navegação principal calma e persistente sem transformar o Fio em dashboard, feed ou aplicativo de produtividade.

## 2. Contrato

- Preservar I-01–I-28 e I-30–I-38 de `docs/atlas/INVARIANTS.md`.
- Preservar integralmente o seletor de data/período e `ReturnPolicy`.
- Guardar permanece superfície inicial e centro do produto.
- Encontrar recupera texto original e factual; nunca interpreta nem influencia Returns.
- Arquivo permanece cronológico, sem ranking, feed, estatísticas ou cartões de engajamento.
- Configurações continuam secundárias.
- Editar e excluir saem das linhas do Arquivo e vivem no detalhe da nota.

## 3. Contexto técnico

- `FioApp.kt`: `MainSurface`, `Scaffold`, Home, Search, Archive e Note.
- `FioViewModel.kt`: estado de query somente em memória, sem mudança de persistência.
- Recursos vetoriais locais para três destinos.
- Testes instrumentados de navegação e fluxo local.
- Room permanece schema 3; nenhuma migration ou dependência nova.

## 4. Critérios de aceitação

1. Navegação persistente oferece `Guardar`, `Encontrar` e `Arquivo`, com alvo mínimo de 48dp e estado selecionado.
2. Home não contém mais Arquivo dentro de `⋯`; Ajustes permanece ação secundária.
3. Encontrar possui tela dedicada com orientação, campo de busca, estados vazio/carregando/erro e resultados factuais.
4. Arquivo não contém busca; exibe contagem, agrupamento cronológico e linhas silenciosas.
5. Editar/Excluir aparecem apenas ao abrir uma nota.
6. Trocar de destino fecha teclado/foco sem persistir query.
7. 134 testes unitários existentes permanecem verdes; testes de contrato de navegação são atualizados.
8. APK instala no aparelho e abre sem crash; inspeção após biometria é registrada honestamente.

## 5. Riscos e portas de escape

- Parar se o fluxo temporal mudar além de padding provocado pela navegação.
- Reverter para navegação textual se ícones prejudicarem contraste ou TalkBack.
- Não executar testes instrumentados no pacote principal se houver risco aos dados reais; usar variante isolada ou validação manual protegida.

## 6. Evidence log

| Data | O que | Output |
|---|---|---|
| 2026-08-20 | Referências visuais analisadas | navegação inferior estável; criação acessível; coleção legível; secundários em menu |
| 2026-08-20 | Código auditado | Arquivo em overflow; Search misturado ao Arquivo; Editar/Excluir repetidos em cada linha |
| 2026-08-20 | Implementação | Guardar/Encontrar/Arquivo persistentes; Search dedicado; Arquivo cronológico; Editar/Excluir no detalhe; schema 3 preservado |
| 2026-08-20 | Testes locais | 134/134 verdes; 0 falhas, 0 erros, 0 ignorados |
| 2026-08-20 | Testes Android 8/API 26 | 26/26 instrumentados verdes no AVD `Fio_API26`, incluindo navegação, touch targets e persistência após recriação |
| 2026-08-20 | Inspeção de interface | hierarquia real confirmou as três telas, estado selecionado e rótulos de acessibilidade; 1080×1920, densidade 420 |
| 2026-08-20 | Poco M3 Pro/API 33 | APK debug atualizado com `adb install -r -d`, dados preservados; abertura fria sem crash até o gate biométrico |

## 7. Resultado

Critérios 1–8 atendidos. A validação interna completa foi executada na variante
debug isolada do emulador. No aparelho físico, a atualização e a abertura foram
confirmadas sem contornar a autenticação do fundador.
