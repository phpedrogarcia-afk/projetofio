# AI START HERE

## O que é o Fio?

Fio é um aplicativo Android privado e local-first para escrever livremente e
reencontrar, depois do tempo, as palavras exatas da própria pessoa. O produto
pode selecionar ou recuperar; nunca interpreta a vida do usuário.

## Onde está o estado?

- Estado vivo: `plans/PROJECT-STATE.md`
- Ordem atual: `packets/EXECUTION-QUEUE.md`
- Código Android: `app/mobile/src/main/java/com/projetofio/app/`
- Estado Compose: `app/mobile/src/main/java/com/projetofio/app/ui/FioViewModel.kt`

## Onde estão as decisões?

- Autoridade: `docs/DECISIONS.md`
- Escopo/fase: `docs/04-FEATURES.md`
- Hierarquia e status documental: `docs/INDEX.md`
- Invariantes numerados: `docs/atlas/INVARIANTS.md`

## Onde está a fila?

Leia `packets/EXECUTION-QUEUE.md`, abra o packet marcado `NOW` e respeite suas
dependências. Não escolha trabalho em `NEXT-WORK` ou em relatórios históricos.

## Como escolher o que ler?

| Tarefa | Leia depois do packet | Código provável |
|---|---|---|
| Home/Guardar/tempo visual | `docs/atlas/UX-SURFACE-MAP.md`, `docs/03-UX.md` | `app/mobile/src/main/java/com/projetofio/app/ui/home/HomeScreen.kt` |
| Encontrar/Search | `docs/atlas/SEARCH-MAP.md`, `docs/search/SEARCH-ARCHITECTURE.md` | `app/mobile/src/main/java/com/projetofio/app/ui/search/SearchScreen.kt`, `app/mobile/src/main/java/com/projetofio/app/search/` |
| Arquivo | `docs/atlas/UX-SURFACE-MAP.md` | `app/mobile/src/main/java/com/projetofio/app/ui/archive/ArchiveScreen.kt` |
| Nota/editar/excluir | `docs/06-DATA-MODEL.md`, `docs/atlas/UX-SURFACE-MAP.md` | `app/mobile/src/main/java/com/projetofio/app/ui/entry/EntryDetailScreen.kt` |
| Returns/tempo | `docs/atlas/TIME-MAP.md`, `docs/07-RETURNS-ENGINE.md` | `app/mobile/src/main/java/com/projetofio/app/domain/TimeReturnEngine.kt`, `app/mobile/src/main/java/com/projetofio/app/application/TimeReturnsService.kt` |
| Ajustes | `docs/03-UX.md` | `app/mobile/src/main/java/com/projetofio/app/ui/settings/SettingsScreen.kt` |
| Privacidade/app lock | `docs/08-PRIVACY-SECURITY.md`, packet | `app/mobile/src/main/java/com/projetofio/app/ui/security/PrivacyScreens.kt`, `app/mobile/src/main/java/com/projetofio/app/security/`, `app/mobile/src/main/java/com/projetofio/app/crypto/` |
| Room/migration | `docs/atlas/DATA-MAP.md`, `docs/06-DATA-MODEL.md` | `app/mobile/src/main/java/com/projetofio/app/persistence/`, `app/mobile/schemas/` |
| Import/export | packet, `docs/export-format.md` | `app/mobile/src/main/java/com/projetofio/app/application/ImportService.kt`, `app/mobile/src/main/java/com/projetofio/app/application/ExportCoordinator.kt` |

Atlas completo somente quando a arquitetura estiver desconhecida:
`docs/atlas/CODEX-MASTER-MAP.md`.

## O que nunca fazer?

- Não interpretar, resumir ou reescrever as palavras do usuário.
- Não misturar Search com Returns.
- Não persistir/logar queries ou plaintext.
- Não adicionar rede, SDK ou dependência de produção por conveniência.
- Não criar streaks, feed, gamificação, anúncios, IA companheira ou profiling.
- Não implementar item Planned/Research/Deferred/Frozen.
- Não alterar schema, crypto ou privacidade sem packet/decisão apropriados.
- Não usar conteúdo real em testes, screenshots ou evidência.
- Não declarar gates humanos/aparelho como aprovados sem execução real.
- Não trabalhar em `main` nem tocar FioOS neste repositório.

## Como executar uma tarefa?

1. `git status` e branch.
2. Packet → `READ FIRST` → arquivos indicados.
3. Busca dirigida por símbolo; sem full scan por padrão.
4. Mudança mínima dentro de `FILES EXPECTED TO CHANGE`.
5. Testes exatos do packet; níveis em `docs/TEST-LEVELS.md`.
6. Se SMALL expandir, pare com `SCOPE EXPANSION REQUIRED`.
7. Relate apenas `Changed`, `Tests`, `Risk`, `Next`.
