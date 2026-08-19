# UX-SURFACE-MAP — cada tela, seu propósito e seus contratos

**Estado examinado:** HEAD `9cfd5f1` · **Evidência:** `E4` (`ui/FioApp.kt`, `FioViewModel.kt`) + `E1` (`docs/03-UX.md`, `docs/design/`, ADR-043/044/045).

Navegação: **single activity, navegação por estado** (`MainSurface {HOME, ARCHIVE, SETTINGS}`), sem NavController. Home é o centro; Arquivo e Configurações vivem em um menu "⋯" (ADR-004: secundários, nunca em pé de igualdade com a escrita).

## 1. Home (Write)

| Dimensão | Realidade |
|---|---|
| Propósito | **Guardar**: o ato de escrever é o produto; nada compete com ele |
| Entrada | BasicTextField sem moldura (as palavras são o elemento mais escuro da tela); prompt "O que está passando pela sua cabeça hoje?" |
| Saída | `draftText` → autosave criptografado → Button Guardar → `saveEntry` → notice "Guardado." |
| Ação principal | Guardar (autosave pré-criptografado; D12 protege o draft) |
| Ações secundárias | menu ⋯ → Arquivo / Configurações; BotanicalMotif (pátina, ADR-045); First Capsule copy "Guardado. O tempo cuida do resto." (ADR-044) |
| Estados | draft carregado do singleton; saving…; savedNotice; recoverableError |
| Erros | `recoverableError` — mensagem, nada apagado |
| Accessibility | `semantics { heading() }` no brand; targets ≥48dp; focusManager/keyboardController; motion reduced check |
| Fonte da verdade | `docs/03-UX.md`, ADR-043 (TimeSheet), ADR-044, ADR-045 |

**TimeSheet** (a joia temporal): "Quando isso pode voltar?" — Someday / 7 / 30 / 90 / 365 dias / **Calendário** (DateSheet com Material DatePicker) / Nunca. Escolha grava a `ReturnPolicy`; nenhuma pressão, nenhuma recorrência.

## 2. Archive (Saved)

| Dimensão | Realidade |
|---|---|
| Propósito | **Encontrar**: revisar o que está guardado, conscientemente |
| Entrada | lista cronológica desc (ativo); busca inline (M4) |
| Saída | NoteScreen (leitura), EditEntryDialog, mover para Excluídos |
| Ação principal | abrir/ler uma entrada |
| Ações secundárias | editar; excluir (diálogo "Mover para Excluídos recentemente? Você poderá recuperar esta entrada por 30 dias."); recuperar; purge; busca |
| Estados | loading; archiveError ("O Arquivo não pôde ser aberto com segurança. Nada foi apagado." — segurança antes de UX); searchLoading/searchResult/searchError; seções por período |
| Busca (M4) | `ArchiveSearchField` debounce 300ms; resultados inline "N resultado(s) para '…'"; `ArchiveSearchHitRow` (data + snippet original selecionável + "Já voltou N vez(es)") ; sealed note neutra; liveRegion polite; TalkBack factual |
| Erros | archiveError (decryption falhou — nada apagado); searchError |
| Accessibility | liveRegion Polite no contador e resultados; contentDescription factual; targets 48dp |
| Fonte da verdade | `docs/03-UX.md`, `docs/search/SEARCH-ARCHITECTURE.md` |

## 3. Note (leitura/edição)

`NoteScreen`: exibe data original (na zona da entrada) + texto original; ações: editar (EditEntryDialog), mover para excluídos. Sem interpretação, sem tags, sem comentários. `SafeOpenFailure` se decrypt falhar.

## 4. Return (Reencontrar)

`ReturnScreen(entry, onClose, onNeverReturn)`: mostra a entrada devolvida com controles discretos — fechar (dispensa), "Nunca mais" (never). Sem explicação de significado. Notificação de origem: canal "Devoluções" IMPORTANCE_LOW, corpo genérico (ADR-011).

## 5. Settings

| Bloco | Realidade |
|---|---|
| Consent | estado de consentimento global de devolução (NOT_CONFIGURED/ENABLED/PAUSED) |
| Pause | pausa/retoma devoluções (Silent CONSENT_DISABLED + cancel dos attempts) |
| App lock | OFF/IMMEDIATE/1min/5min via `AppLockPolicy` + `DeviceAuthenticator` (BiometricPrompt) |
| Privacy cover | toggle — cobre o app ao fundo (ADR-031) |
| Analytics | toggle local (`analyticsEnabled`); **sem transporte remoto hoje**; ADR-017 content-free |
| Import | preview (lista de candidatos + issues) → commit; batches com rollback; issues traduzidas (`importIssueLabel`) |
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
