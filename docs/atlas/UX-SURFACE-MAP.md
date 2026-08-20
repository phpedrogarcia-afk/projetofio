# UX-SURFACE-MAP — cada tela, seu propósito e seus contratos

**Estado examinado:** branch de eficiência em 2026-08-20 · **Evidência:**
`E4` (`ui/FioApp.kt`, arquivos por superfície, `FioViewModel.kt`) + 28 testes
instrumentados no AVD API 26 + `E1` (`docs/03-UX.md`, ADR-043/044/045/048/049).

Arquivos: Home `ui/home/HomeScreen.kt`; Encontrar
`ui/search/SearchScreen.kt`; Arquivo `ui/archive/ArchiveScreen.kt`; nota
`ui/entry/EntryDetailScreen.kt`; Return `ui/returns/ReturnScreen.kt`; Ajustes
`ui/settings/SettingsScreen.kt`; privacidade `ui/security/PrivacyScreens.kt`.

Navegação: **single activity, navegação por estado** (`MainSurface {SAVE, FIND, ARCHIVE, SETTINGS}`), sem NavController. Guardar abre o app; Guardar/Encontrar/Arquivo ficam na barra inferior; Ajustes é secundário no cabeçalho (ADR-048).

## 1. Home (Write)

| Dimensão | Realidade |
|---|---|
| Propósito | **Guardar**: o ato de escrever é o produto; nada compete com ele |
| Entrada | BasicTextField sem moldura (as palavras são o elemento mais escuro da tela); prompt "O que está passando pela sua cabeça hoje?" |
| Saída | `draftText` → autosave criptografado → Button Guardar → `saveEntry` → notice "Guardado." |
| Ação principal | Guardar (autosave pré-criptografado; D12 protege o draft) |
| Ações secundárias | Ajustes; barra inferior Encontrar/Arquivo; BotanicalMotif; First Capsule copy "Guardado. O tempo cuida do resto." |
| Estados | draft carregado do singleton; saving…; savedNotice; recoverableError |
| Erros | `recoverableError` — mensagem, nada apagado |
| Accessibility | `semantics { heading() }` no brand; targets ≥48dp; focusManager/keyboardController; motion reduced check |
| Fonte da verdade | `docs/03-UX.md`, ADR-043 (TimeSheet), ADR-044, ADR-045 |

**TimeSheet**: "Quando isso pode voltar?" — Someday / 7 / 30 / 90 / 365 dias / calendário / Nunca. **Integridade atual P0:** a escolha vive somente no estado Compose; `FioViewModel.saveEntry()` recebe apenas o texto e o domínio cria `Entry.returnMode = ELIGIBLE`. Períodos/data/Nunca ao guardar não são persistidos.

## 2. Archive (Saved)

| Dimensão | Realidade |
|---|---|
| Propósito | **Arquivo**: revisar cronologicamente o que está guardado |
| Entrada | lista cronológica desc; Search vive na superfície separada Encontrar |
| Saída | NoteScreen (leitura), EditEntryDialog, mover para Excluídos |
| Ação principal | abrir/ler uma entrada |
| Ações secundárias | editar; excluir (diálogo "Mover para Excluídos recentemente? Você poderá recuperar esta entrada por 30 dias."); recuperar; purge; busca |
| Estados | loading; archiveError ("O Arquivo não pôde ser aberto com segurança. Nada foi apagado." — segurança antes de UX); searchLoading/searchResult/searchError; seções por período |
| Descoberta | subtítulo explica que tocar permite ler/editar/excluir; chevron + descrição TalkBack factual |
| Erros | archiveError (decryption falhou — nada apagado); searchError |
| Accessibility | liveRegion Polite no contador e resultados; contentDescription factual; targets 48dp |
| Fonte da verdade | `docs/03-UX.md`, `docs/search/SEARCH-ARCHITECTURE.md` |

## 3. Note (leitura/edição)

`NoteScreen`: exibe data original (na zona da entrada) + texto original; ações reais: editar (EditEntryDialog) e mover para excluídos. Sem interpretação, sem tags, sem comentários. `SafeOpenFailure` se decrypt falhar. **P0:** “Devolver para agora” apenas alterna estado Compose local e não é uma ação real do domínio; pertence ao FIO-P19 e não pode ser considerada funcional.

## 4. Return (Reencontrar)

`ReturnScreen(entry, onClose, onNeverReturn)`: mostra a entrada devolvida com controles discretos — fechar (dispensa) e “Não devolver esta nota novamente”. A segunda ação exige confirmação e explica que a nota permanece no Arquivo e que não há reversão nesta versão. Notificação de origem: canal "Lembranças do Fio" IMPORTANCE_LOW, corpo genérico (ADR-011).

## 5. Settings / Ajustes (ADR-049)

| Destino | Realidade |
|---|---|
| Visão geral | mapa curto por intenção; explica o efeito antes do controle; sem M1/M2/M3/"validação" |
| Proteção ao abrir | OFF/IMMEDIATE/1min/5min em linguagem humana via autenticação Android |
| Lembranças que voltam (validation build) | estado, consentimento, pausa, horário silencioso e pending Return em página focada |
| Importar notas (validation build) | explicação → escolher TXT/Markdown → prévia/commit/rollback |
| Exportar uma cópia | explica arquivo legível e perda da proteção do Fio → TXT ou Markdown |
| Excluídos recentemente | explica retenção de 30 dias → Recuperar ou Excluir para sempre |
| Export | Markdown ou Texto → SAF; checksum no rodapé/manifest |
| Quiet hours | time pickers start/end (default 21h→8h) |
| Deletados | lista Recently Deleted com recover/purge |

## 6. Estados transversais

| Estado global | Superfície | Comportamento |
|---|---|---|
| PrivacyCover | todas | overlay ao voltar ao app (ADR-031) |
| LockedScreen | após app lock | biometria (DeviceAuthenticator) antes de qualquer dado |
| SafeOpenFailure | leitura/export | mensagem, nada apagado |
| isMotionReduced | Home | respeita preferência do sistema (pátina) |

## 7. O que NÃO existe na UX (também mapear)

Sem onboarding tutorial (ADR-044: nada de captura precoce). Sem perfis/avatars. Sem tags, pastas, favoritos. Sem trilhas, streaks ou qualquer gamificação. Sem chat, sem IA conversacional. Sem feed. Sem dark mode custom (usa Material 3 dinâmico + tokens Verde-Sálvia). Sem multi-janela específica (edge-to-edge/adaptive: apenas responsivo por padding — `FioSpace`).
