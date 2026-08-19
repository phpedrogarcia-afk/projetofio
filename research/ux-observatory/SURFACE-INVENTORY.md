# SURFACE-INVENTORY — inventário de superfícies e estados do Fio

Snapshot da branch `integration/manus-ux-refinement-20260819` (base: `integration/manus-pre-codex-20260817`). Usado como referência dos ciclos B–M. Qualquer mudança de código nesta missão deve atualizar este documento.

## 1. Mapa de telas (FioApp.kt, ~1320 linhas)

| # | Composable | Linha | Responsabilidade |
|---|---|---|---|
| 1 | `FioApp` | 120 | Navegação de superfícies (`MainSurface.HOME`); notice de guardado (1,5s); hooks de export/import/devoluções |
| 2 | `HomeScreen` | 195 | Editor (o coração): placeholder, menu ⋯ (Arquivo/Configurações), time sheet, date sheet, retorno imediato, copy da primeira cápsula |
| 3 | `TimeOption` | 479 | Opção da time sheet (48dp, check "Selecionado", highlight primary 12%) |
| 4 | `ArchiveScreen` | 521 | Arquivo: agrupamento por mês (MMMM de yyyy), estado vazio, loading, erro seguro |
| 5 | `ArchiveRow` | 650 | Linha: data (labelLarge, onSurfaceVariant) + distância temporal (tertiary); edição/exclusão |
| 6 | `NoteScreen` | 695 | Leitura: SelectionContainer, voltar, editar, devolver (3 botões) |
| 7 | `EditEntryDialog` | 789 | Diálogo de edição (AlertDialog nativo, Guardar alterações/Cancelar) |
| 8 | `SettingsScreen` | 816 | Privacidade (app lock choices), devoluções (consent/quiet hours/pause), import preview, export, deletados recentes |
| 9 | `DeletedRow` | 1057 | Linha de Excluídos recentemente: Recuperar / Excluir para sempre |
| 10 | `ReturnScreen` | 1108 | Telas de devolução: "Uma palavra sua voltou" + fechar + nunca mais |
| 11 | `AppLockChoices` | 1132 | 4 opções de bloqueio (Desativado/Imediato/1min/5min) |
| 12 | `SectionTitle` | 1170 | Título de seção com heading semantics |
| 13 | `BotanicalMotif` | 1186 | Pátina: caule+fim de 2 folhas, 32×56dp, alpha 0.55, skip com Reduce Motion |
| 14 | `PrivacyCover` | 1235 | Tela de capa "Fio" em displayLarge (recent apps) |
| 15 | `LockedScreen` | 1244 | Bloqueio: biometria, "Protegido pelo bloqueio do seu aparelho", fallback |
| 16 | `SafeOpenFailure` | 1293 | Fallback de segurança de dados: "Nada foi apagado. Feche o Fio e tente novamente." |

## 2. Máquina de estado (FioViewModel / FioUiState)

`FioUiState` tem 17 campos: `draftText`, `entries`, `deletedEntries`, `settings` (AppSettings), `loading`, `saving`, `savedNotice`, `recoverableError`, `archiveError`, `m2EngineeringEnabled`, `pendingReturnId`, `openedReturn`, `returnError`, `m3EngineeringEnabled`, `importPreview`, `importBatches`, `importMessage`, `importing`.

Transições-chave: init (purgeExpired → draft/settings → loading=false; reconcile M2; batches M3); observeActiveEntries.observeDeletedEntries (Flows); saveDraft (guardando→guardado→notice 1,5s); eventos de consentimento, import preview→commit→batch; return open/close.

## 3. Inventário de copy (hardcoded em FioApp.kt — strings.xml tem apenas 1 recurso)

| Superfície | Copy | Observação |
|---|---|---|
| Home (notice) | "Guardado." / "Guardado. O tempo cuida do resto." | Estendido só na 1ª cápsula |
| Home (editor) | "O que está passando pela sua cabeça hoje?" | Placeholder do campo |
| Home (vazio) | "Escreva quando quiser." | Estado vazio |
| Menu ⋯ | "Mais opções", "Arquivo", "Configurações" | — |
| Time sheet | "Quando isso pode voltar?" / sub-explicação / opções: Algum dia, Em 7 dias, Em 30 dias, Em 90 dias, Em 1 ano, Escolher uma data, Nunca | Botões: Escolher / Cancelar |
| Return imediato (sheet) | "Quando isso pode voltar? · $policyLabel" / "Escolhido: $policyLabel" | — |
| Guardando | "Guardando…" / "Guardar" | Botão guarda em 2 estados |
| Archive | "Arquivo", "Voltar" (←), "Editar", "Excluir", "O Arquivo não pôde ser aberto com segurança. Nada foi apagado.", "Quando quiser, suas palavras podem ficar aqui." | — |
| Deletar | "Mover para Excluídos recentemente?" / "Você poderá recuperar esta entrada por 30 dias." / "Mover" / "Cancelar" | — |
| ArchiveRow | "Hoje"/"Ontem"/"há N dias"/"há N meses"/"há N anos" | Distância como 2ª linha, tertiary |
| NoteScreen | "Reescrever esta nota agora?" / "Devolver para agora" / "Devolver esta nota agora" / "Suas palavras voltaram. Reescreva com o olhar de hoje, se quiser — ou apenas as deixe ser." / "Devolver agora?" / "Devolver" | **Copy interpretativa a reavaliar (Missão 2 já neutralizou o equivalente)** |
| EditEntryDialog | "Editar entrada" / "Guardar alterações" / "Cancelar" | — |
| Settings | "Ativar devoluções?" / 2 parágrafos explicativos / "Ativar devoluções" / "Agora não" / "Pausar devoluções" / "Retomar devoluções" / "Horário silencioso: 21h–8h, 22h–9h" / "Notificações estão desativadas..." / "Abrir devolução disponível" | — |
| Settings (privacidade) | "Privacidade" / explicação ocultação / fallback lock Android | — |
| Import | "Importar — validação" / "Escolher arquivo" / "Preparando…" / "Previa: N novas, N duplicadas, N com problema" / "Duplicada — não será importada." / "Importar N" / issues (11 mensagens) | — |
| Import (batches) | "Lote: N entradas" / "Nome do arquivo não mantido" / "Desfazer este lote" / "COMMITTED" | — |
| Deletados | "Excluídos recentemente" / "Nenhuma entrada excluída." / "Recuperar" / "Excluir para sempre" | — |
| M1 local | "M1 local: sem conta, sincronização, analytics ou devoluções ativas." | Rodapé de privacidade |
| ReturnScreen | "Uma palavra sua voltou" / "Fechar" / "Não mostrar novamente" | — |
| AppLockChoices | "Desativado" / "Imediato" / "Após 1 minuto" / "Após 5 minutos" / "— atual" | — |
| LockedScreen | "Desativar o bloqueio do Fio?" / "Protegido pelo bloqueio do seu aparelho." / "A autenticação do aparelho não está disponível." / "Abrir" / "Rever bloqueio" | — |
| SafeOpenFailure | "Os dados locais não puderam ser abertos com segurança. Nada foi apagado. Feche o Fio e tente novamente." | — |

## 4. Padrões de design observados (insumo para os ciclos)

- **Tokens**: FioRadius (md/lg), FioSpace (s1–s8), FioDisplayDate — usar em tudo; sem valores literais.
- **Targets**: `heightIn(min = 48.dp)` em tudo clicável; `widthIn(min = 48.dp)` para "← " — padrão correto de 48dp.
- **Semantics**: `heading()` em títulos, contentDescription em ações iconográficas, "Selecionado" via icon CD.
- **Datas**: formatters pt-BR hardcoded no FioApp (não strings.xml) — single locale, coerente.
- **Erro sempre factual + "nada foi apagado"**: padrão de segurança em toda a copy de falha.
- **SelectionContainer** na leitura de conteúdo (copiar permitido, não editar).
- **DropdownMenu nativo** para menu overflow; AlertDialog nativo para ações destrutivas/confirmatórias.

## 5. Gaps candidatos para os ciclos

- strings.xml praticamente vazio (1 recurso) — toda copy hardcoded; i18n inviável hoje (aceitável para pt-BR único, mas sem centralização).
- NoteScreen: copy residual "Reescrever com o olhar de hoje" (neutralizada na Missão 2 — verificar se o commit está na base).
- `BotanicalMotif` usa `isTouchExplorationEnabled` apenas — a decisão da Missão 2 foi sobre motion reduction; conferir se a verificação cobriu TalkBack (leitura) vs motion.
- `quietHoursLabel` usa `String.format(Locale.ROOT)` para minutos — correto.
- Sem `contentDescription` no "← " de NoteScreen (tem CD "Voltar"? conferir linha 714).
- Time sheet: opções de distância com `days` arbitrários ("Em $days dias") — OK factual.
- Notice "Guardado" some em 1,5s (delay sem cancelamento explícito do efeito) — comportamento aceito.
