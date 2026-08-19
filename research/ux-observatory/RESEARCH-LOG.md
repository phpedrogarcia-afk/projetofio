# RESEARCH-LOG — UX Observatory Fio

Registro de fontes, datas e aprendizados. Nunca depender de memória.

## Things (Cultured Code)

| Fonte | Data observada | Aprendizado |
|---|---|---|
| [culturedcode.com/things/support — Today, Upcoming, Anytime, Someday](https://culturedcode.com/things/support/articles/4001304/) | ago/2026 | "Someday" é a ausência de data exata com propósito explícito: guarda ideias no espectro oposto de "Upcoming", tomando visualmente um backseat (não distraem do acionável); revisita-se de meses em meses. "Upcoming" é agenda futura com visão de 7 dias; itens dormem e "pulam" para Today na data. "Inbox" captura sem exigir estrutura e devolve os itens processados. **Princípio extraído:** cada estado temporal tem um nome e um lugar; o sistema nunca força uma data exata onde o usuário só tem intenção. |
| [mariusmasalar.me — Things 3 first impressions](https://mariusmasalar.me/things-3-first-impressions-8f0155c60cf2) | mai/2017 | Date picker refinado com NLP ("next Wednesday", "June 4") — reagenda em toques/clicks mínimos, funciona bem a teclado. Tarefa como objeto: lista parece texto plano até tocar, então a tarefa "se levanta" como card. "Cancel" como terceiro estado entre completar e excluir (não se perde o registro). **Princípio extraído:** a edição temporal deve ser mais rápida que a criação; o objeto deve emergir do texto. |
| [apple.com/design/human-interface-guidelines/pickers](https://developer.apple.com/design/human-interface-guidelines/pickers) | ago/2026 | Pickers nativos oferecem múltiplos modos (calendário, wheel, numeric keypad); a escolha do modo deve seguir o tipo de dado. **Princípio extraído:** o controle de data deve escolher o modo certo para o tipo de escolha (relativa vs absoluta). |

### Interpretação Fio (Things)

- "Someday" → **"Algum dia" do Fio**: mesmo papel — ausência de data exata com nome próprio. Fio já usa. CONFIRMA a tese.
- "Upcoming" → devolução programada é o "Upcoming" emocional do Fio; mas sem pressão de "Today list".
- NLP de data → **não importar** (IA interpretativa, proibida pelo princípio 2), mas a velocidade de reagendamento sim: a sheet de 3 níveis resolve.
- "Cancel" → paralelo Fio: **soft-delete + Recently Deleted** (direito de recuperar); o "Descansar" do Fio é análogo a "hibernação".

## Day One (Bloom Built)

| Fonte | Data observada | Aprendizado |
|---|---|---|
| [dayoneapp.com](https://dayoneapp.com/) (página oficial, features) | ago/2026 | "On This Day" é o resurfacing clássico: "quickly revisit moments from the past — no time machine needed". Marketing usa "feels almost sacred: a completely private digital space" (tom de santuário, não de cofre). Privacidade como feature declarada: passcode+biometria, E2E encryption, export sempre disponível ("you own the data"). Metadados automáticos por entrada (localização, clima, fases da lua). |
| [Medium/Design Decisions — 5 design details (Koo, 2014)](https://medium.com/design-decisions/5-design-details-i-about-dayone-35d95a900bc5) | jul/2014 (histórico) | 5 padrões: (1) feedback sonoro sutil em cada ação — multi-sensorial sem ostentação; (2) sync em background sem bloquear acesso — nunca um loading screen de sync como entrada do app; (3) **função avançada obscurecida** — edição "barebones" diz "tudo que você precisa é uma linha de texto"; (4) restrição deliberada (1 foto por entrada) como proteção contra intimidação; (5) timestamp da foto com um toque — planejado para o fracasso do usuário (não escrever todo dia). |
| [Play Store — Day One](https://play.google.com/store/apps/details?id=com.dayoneapp.dayone) | ago/2026 | Widget "On This Day"; confirma que resurfacing é feature persistente e central (não campanha). |

### Interpretação Fio (Day One)

- On This Day → o **ReturnScreen** do Fio é a versão Fio: no Day One o usuário busca o reencontro; no Fio o reencontro encontra o usuário (inversão da agência). Manter a inversão — é a tese.
- "Planejado para o fracasso" (timestamp da foto, 1 foto) → aplicar: **autosave, erro que preserva texto, não exigir data exata**. Todos já em linha com o Fio; confirmar implementação.
- Sync loading → **local-first é o fim dessa dor**: nenhuma tela de carregamento de rede, nunca. Confirmar que o Fio não exibe estados de rede.
- O que NÃO combina com o Fio (exigência da missão): metadados automáticos (clima/localização quebram privacidade e simplicidade), notificações de streak ("1.469 day streak" nos reviews — engajamento, proibido), calendar entries criados no calendário do sistema (agenda, proibido), fotos/mídia (modalidade de conteúdo — Fio é texto), E2E+cloud (Fio é 100% local), subscription/paywall.
- "Feels almost sacred" → copy de privacidade factual: o Fio diz o que faz ("exige autenticação para ser aberta"), não vende santuário. OK.

## Material 3 DatePicker (Android/Compose)

| Fonte | Data observada | Aprendizado |
|---|---|---|
| [developer.android.com — Date pickers Compose](https://developer.android.com/develop/ui/compose/components/datepickers) | ago/2026 | 3 tipos: **docked** (inline, dropdown do campo), **modal** (dialog), **modal input** (campo de texto + dialog). `DatePicker` + `DatePickerDialog`; `selectableDates` restringe datas; `showModeToggle=false` esconde o botão calendário/input (modo único — útil para o Fio). API experimental. |
| [m3.material.io — guidelines](https://m3.material.io/components/date-pickers/guidelines) | ago/2026 | **NÃO usar modal picker para datas no passado distante ou futuro distante** (ex.: nascimento) — usar modal input ou docked. Navegação: swipe entre meses, tap no ano para lista vertical de anos. Modal mobile = full-screen no compact breakpoint. Dismiss: OK/Cancel/tap fora. Selection por cor. |
| [m3.material.io — accessibility](https://m3.material.io/components/date-pickers/accessibility) | ago/2026 | Touch targets 48x48dp; contraste 4.5:1; **verbalização completa da data pelo screen reader** ("segunda-feira, 17 de agosto", não só "17"); formatação pós-digitação (aceitar vários separadores); shortcuts teclado (PageUp/Down mês, Shift+PageUp ano); label do campo deve espelhar o placeholder; dia da semana não é foco de teclado. |

### Interpretação Fio (calendário)

- O M3 DatePicker modal atende o Nível 2 (data específica) com familiaridade e acessibilidade completas — reinventar calendário custom teria custo de acessibilidade injustificável (regra §62: identidade Fio via tokens/typografia/sheet, não via reinvenção de gestos).
- `selectableDates` → desabilitar datas passadas no calendário Fio (distância autobiográfica exige futuro). Datas muito próximas: o engine já impõe bootstrap mínimo (7/14/30 dias para maturidade ≥4/2/1); desabilitar no UI coerente com o backend.
- "Não usar modal picker para futuro distante" da guideline não se aplica literalmente — "12 set 2027" não é distante demais para swipe de anos; mas o header com distância ("daqui a 1 ano e 24 dias") resolve a cognição do futuro distante.
- `showModeToggle=false` + single mode (sem input de data manual? — decisão: manter input numérico para teclado/assistive — dois métodos de entrada são requisito de acessibilidade M3).
- PT-BR: verbalização "quinta-feira, 17 de setembro de 2026" — o locale do sistema resolve.

## Apps privacy-first (1Password / Bitwarden)

| Fonte | Data observada | Aprendizado |
|---|---|---|
| [support.1password.com — biometric unlock Android](https://support.1password.com/android-biometric-unlock/) | ago/2026 | Biometria como camada opcional sobre a senha-mestra; o app 1Password só sabe "reconhecido/não" (não recebe o template — arquitetura que o Fio herda via Keystore/CryptoObject). |
| [1Password community — lock + pause biometrics](https://www.1password.community/1password-at-home-31/feature-request-lock-1password-and-pause-biometrics-12856) | fev/2023 | Padrão "Lock immediately and pause biometrics": estado protegido extra quando o usuário quer reforço temporário; biometria pausada até senha manual. **Adaptação Fio: Nota Selada** — selar exige autenticação para abrir; app lock é configuração separada (Background Lock). |
| [support.1password.com — biometric security Android](https://support.1password.com/android-biometric-unlock-security/) | ago/2026 | Autenticação via CryptoObject/KeyStore: o conteúdo descriptografa só com a chave atrelada à biometria; falha de auth = nada é revelado (nem parcialmente). |
| [support.google.com/android — Private Space](https://support.google.com/android/answer/15341885) | ago/2026 | Android 15 Private Space: apps ocultos que exigem auth do sistema para entrar e ficam "escondidos" na UI do sistema (recent apps blur, launcher). **Adaptação Fio**: o PrivacyCover em background (blur) + re-auth no return-to-foreground já cobre; não replicar Private Space. |
| [community.bitwarden.com — remove overwrite confirmation](https://community.bitwarden.com/t/remove-the-confirmation-dialog-about-overwriting-password/64245) | mar/2024 | Debate interno: usuários pedem REMOÇÃO de confirmações redundantes em ações de baixo risco — confirmações demais viram ruído e treina o usuário a não ler o dialog. **Princípio:** confirmar só o destrutivo real. |

### Interpretação Fio (privacy apps)

- Nota Selada: **revele nada no estado protegido** (título/preview ocultos, não "••••••" enganoso — mostrar o estado, não o conteúdo). Falha de auth: mensagem factual, re-tentar 1x, depois cancel sem lockout dramático.
- Background: PrivacyCover já implementado (Missão 1/2 — capa de privacidade ao sair do app). Verificar no inventário se cobre screen-off e multi-window.
- Confirmação destrutiva: 1 passo + undo quando possível (soft-delete/recently deleted é o "undo" estrutural do Fio — herança Day One/Apple).

## Android System (Dynamic Color, A11y)

| Fonte | Data observada | Aprendizado |
|---|---|---|
| [developer.android.com — Dynamic Color](https://developer.android.com/develop/ui/views/theming/dynamic-colors) | ago/2026 | Material You (Android 12+): sistema gera paleta do wallpaper; **tokens semânticos** (`primary`, `onPrimary`, `surface`...) são a porta de entrada — a app define papéis, o sistema define valores. `DynamicColors.applyToActivitiesIfAvailable()` ou `applyToContextIfAvailable()` em runtime. Paletas custom podem ser harmonizadas (`HarmonizedColors`). Suporte opcional: o app pode manter cores próprias OU aderir. |
| [developer.android.com — Compose accessibility](https://developer.android.com/develop/ui/compose/accessibility) | jun/2026 | Semantics: contentDescription nunca nulo/vazio para ações; estado (selected/disabled) declarado via semantics; headings booleanos; `liveRegion` para mudanças dinâmicas anunciadas. |
| [eevis.codes — Android animations and reduced motion](https://eevis.codes/blog/2022-12-12/android-animations-and-reduced-motion/) | dez/2022 | Setting "Remove animations" (Accessibility → Display/Text size) — apps devem respeitar; padrão: `AccessibilityInfo.isReduceMotionEnabled` / `TransitionUtils`. |

### Interpretação Fio (tema/cores)

- O Fio usa **paleta fixa Verde-Sálvia** (tokens definidos em código, design v1). Dynamic Color seria uma personalização do sistema que **concorreria com a identidade Verde-Sálvia**. Decisão registrada: NÃO adotar dynamic color; a identidade é a paleta fixa. Dark mode já respeita tokens (fase 6 verificar contraste).
- Se no futuro o fundador quiser adesão ao Material You: tokens semânticos permitiriam harmonização sem perder o verde (HarmonizedColors), mas hoje não faz parte do escopo (sem features).

### Pesquisa concluída (fontes externas). Próximos achados virão do próprio código (inventário).
