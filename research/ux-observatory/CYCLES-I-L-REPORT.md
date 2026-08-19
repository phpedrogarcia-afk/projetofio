# CYCLES-I-L-REPORT — notificações, privacidade/selada, settings, dark/a11y/adaptive

Relatório dos ciclos I–L da Missão 3. Como nos ciclos E–H, o resultado é majoritariamente **verde por design**, com decisões registradas e nenhuma feature nova proposta.

## Ciclo I — Notificações

Já especificado no NOTIFICATION-UX.md (fase 3): canal `fio_returns_v1` com `IMPORTANCE_LOW` e `VISIBILITY_PRIVATE`, título fixo "Algo seu voltou." sem conteúdo da nota, categoria `REMINDER`, auto-cancel, PendingIntent imutável direto para a ReturnScreen, permission check silencioso. Verificação adicional deste ciclo: o cancel da notificação está pareado em todos os caminhos terminais (`openReturn` → `notifications.cancel`; `neverReturn` → `cancel` + `cancelPendingReturnsForEntry(..., ENTRY_NEVER)`; `reconcilePending` expirado → `cancel`). O3 do NOTIFICATION-UX fechado. **Verde por design.**

## Ciclo J — Privacidade / Nota Selada

O `PrivacyCover` (MainActivity:94) mostra apenas "Fio" em displayLarge sobre o background do tema quando o app vai para o segundo plano — nenhuma captura de tela revela conteúdo. `FLAG_SECURE` (MainActivity:88) impede screenshot/recents preview. A camada de biometria (`DeviceAuthenticator` + `BiometricPrompt`) é opcional e factual: o lock screen explica o que a barreira é ("Protegido pelo bloqueio do seu aparelho.") e o fallback quando a autenticação do aparelho some é honesto ("O conteúdo continuará cifrado no armazenamento, mas o Fio abrirá sem esta barreira adicional."), com 2 passos para desativar ("Rever bloqueio" → dialog com copy clara). `SafeOpenFailure` usa a mesma fórmula factual com a garantia "Nada foi apagado". A Nota Selada é feature futura (I4 no inbox): não há estado "selada" implementado, então o ciclo é auditoria dos alicerces — **verdes por design**.

## Ciclo K — Settings / IA do sistema

A settings screen segue o gradiente de risco documentado (Privacidade → Devoluções → Import → Export → Excluídos). O modo de bloqueio (OFF → BIOMETRIC/DEVICE) é apresentado com copy factual e o `setAppLockMode` recarrega settings após a mudança. Quiet hours usam 2 presets (ADR); o engine M2/M3 respeita `consent` com as validações do schema (PAUSED exige `returnsPausedAt`, testado na Missão 1). Não há nenhum "IA do sistema" que interprete conteúdo: o único uso de ML é a biometria do aparelho, que é delegação padrão do Android. **Verde por design.**

## Ciclo L — Dark mode / acessibilidade / adaptive

O tema `FioTheme` usa `isSystemInDarkTheme()` — respeita a preferência do sistema automaticamente. O `DarkColors` não é inversão pura: charcoal-esverdeado com sálvia mais clara, preservando a identidade Verde-Sálvia no escuro (comentário no arquivo confirma a intenção). Dynamic Color (Material You) foi **deliberadamente não adotado**: a paleta Verde-Sálvia é a marca (I6 do inbox, rejeitada por identidade). A tipografia usa Fraunces (display) + Inter (body) com fallback do sistema; fontes do sistema são respeitadas via `MaterialTheme.typography` — sem fontSize hardcode em sp fora dos tokens. A a11y factual está coberta (G9 pago): headings no "Fio", liveRegion polite nos notices e na ReturnScreen, contentDescription nos ícones de ação, targets 48dp, Reduce Motion + TalkBack suprimem a Pátina. O teste de TalkBack manual e a conferência de contraste dos tokens dark são gates humanos de AVD (PILOT-PROTOCOL). **Verde por design** com 2 gates humanos abertos.

## Síntese

| Ciclo | Veredito | Código alterado |
|---|---|---|
| I — Notificações | Verde por design; O3 fechado | — |
| J — Privacidade/selada | Verde por design (alicerces) | — |
| K — Settings/IA | Verde por design | — |
| L — Dark/a11y/adaptive | Verde por design; 2 gates AVD abertos | — |

Os dois gates humanos: (1) TalkBack walkthrough completo em AVD (ordem de leitura da Home, Arquivo, ReturnScreen); (2) conferência de contraste dos tokens `DarkColors` com ferramenta de contraste (WCAG AA) — os valores foram auditados visualmente nos testes das missões anteriores mas merecem medição formal antes do piloto.
