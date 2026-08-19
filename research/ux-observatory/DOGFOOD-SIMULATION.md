# DOGFOOD-SIMULATION — jornada simulada de uso (sem emulador)

Simulação linha a linha dos principais fluxos contra o código atual. Cada passo anota o comportamento real e a expectativa do usuário.

## Fluxo 1: Primeira cápsula (Home vazio → Guardar)

1. Abertura: `loading=true` por ~10–100ms (init purge+draft+settings), depois editor com placeholder "O que está passando pela sua cabeça hoje?". Botão Guardar desabilitado enquanto texto vazio (correta restrição: `blanks`).
2. Usuário escreve uma frase. Botão "Guardar" habilita (52dp, primary). Ao pressionar: `saving=true` → label muda para "Guardando…" com feedback tátil visual (scale 0.98/alpha 0.92) — botão responde ao press.
3. Save conclui → notice "Guardado. O tempo cuida do resto." (1ª cápsula, ADR-044) com liveRegion polite, fadeOut 1,5s depois. Editor limpa o campo.
4. **Anotação**: o draft é preservado durante `saving`? Verificar no FioViewModel se clearDraft é cancel-safe — se o usuário digitar durante o save, a digitação pode ser perdida (ponto de verificação do ciclo E).
5. **Anotação**: o notice some sem ação possível — aceitável (ADR-014), mas se o app fechar durante os 1,5s, o usuário nunca vê a confirmação completa. Sem retry. (Ciclo E: proposta leve — nada além do que já existe.)

## Fluxo 2: Time sheet (Guia temporal)

1. Ao escrever, "Quando isso pode voltar?" fica habilitado (só com texto não vazio — verificar linha do estado). Tocar abre ModalBottomSheet.
2. 7 opções: Algum dia / Em 7 / 30 / 90 dias / Em 1 ano / Escolher uma data / Nunca. Seleção fecha a sheet imediatamente (1 toque = escolha confirmada — rápido,Things-like).
3. "Escolher uma data" abre `DatePickerDialog` com `DatePicker` default (modal). `initialSelectedDateMillis` usa o UTC da política — **gatilho**: se o usuário estiver em UTC-3, a data selecionada no calendário UTC pode ser 1 dia atrás/adiante (fuso do picker). Verificação: `toLocalDate()` em UTC pode dar o dia errado para zonas negativas após meia-noite. (Ponto para o ciclo C: date picker deve respeitar o fuso original/loca — verificar comportamento real.)
4. Voltar à Home: sublinha "Quando isso pode voltar? · Escolhido: $policyLabel" mostra a escolha (feedback persistente).
5. Guardar → notice curto "Guardado."
6. **Anotação**: não há caminho de "trocar a política de uma entrada já salva" visível do Home — apenas no Arquivo (Editar?). Confirmar onde a política editável vive.

## Fluxo 3: Arquivo (vazio → com entries → leitura)

1. Vazio: "Quando quiser, suas palavras podem ficar aqui." (calor sem cobrança).
2. Agrupamento por "Mês de ano" (MMMM de yyyy) com SectionTitle/heading; linhas com data média + distância temporal ("há 3 meses", tertiary).
3. Toque na linha → NoteScreen (leitura); "Editar"/"Excluir" em cada linha (targets a confirmar: altura mínima).
4. NoteScreen: botão voltar (CD "Voltar"), conteúdo em SelectionContainer (cópia permitida), data longa pt-BR.
5. Botões: "Devolver para agora" → dialog "Devolver agora?" → confirmar devolve (engine). "Reescrever esta nota agora?" (se returned) → Edita via EditEntryDialog.
6. **Anotação**: a copy "Reescrever esta nota agora?" persiste no repo (linha 733) — a neutralização da Missão 2 foi em outro lugar. Verificar se é a mesma superfície (sheet de retorno imediato vs NoteScreen): o commit da Missão 2 mudou "Reescrita com o olhar de hoje?" — esta linha 733/754 ainda contém "Reescreva com o olhar de hoje" — **candidata a correção trivial (ciclo G)**.
7. "Excluir" → "Mover para Excluídos recentemente?" + "Você poderá recuperar esta entrada por 30 dias." — confirmação honesta e sem medo.

## Fluxo 4: Settings

1. Privacidade: AppLockChoices (Desativado/Imediato/1min/5min), "— atual" marca a opção ativa. Fallback biometria indisponível: dialog honesto "Desativar o bloqueio do Fio?" com "Desativar e continuar"/"Manter bloqueado".
2. Devoluções: consent flow ("Ativar devoluções?" + 2 parágrafos factual), pause/retomar, quiet hours (21h–8h / 22h–9h), "Abrir devolução disponível".
3. Import: preview com contagem (novas/duplicadas/problema), issues com 11 mensagens factuais, batches desfazíveis.
4. Export: Markdown/Texto, aviso "fora da proteção do Fio".
5. **Anotação**: a ordem das seções segue Privacidade → Devoluções → Import → Export → Excluídos — coerente com o gradiente de risco. Verificar se há seção "Sobre"/créditos (M1 local footer?).

## Fluxo 5: Privacidade/segurança

1. PrivacyCover em recent apps (displayLarge "Fio").
2. LockedScreen: biometria quando disponível; fallback "A autenticação do aparelho não está disponível." com "Rever bloqueio".
3. SafeOpenFailure: mensagem de segurança sem pânico.
4. **Anotação**: não há notificação de "alguém tentou abrir" nem trilha de acesso (intencional — sem métricas).

## Gaps consolidados (para o DESIGN-DEBT e UX-REFINEMENT-QUEUE)

| # | Gap | Fluxo | Gravidade |
|---|---|---|---|
| G1 | **CONFIRMADO**: date picker inicializa/interpreta via UTC puro (`atStartOfDay(ZoneOffset.UTC)`, `atZone(ZoneOffset.UTC).toLocalDate()`, linhas 454/463). Para o usuário em UTC−3 entre 21h–00h, a data selecionada pode ser lida como o dia seguinte/anterior. Corrigir com o fuso do dispositivo (`ZoneId.systemDefault()`) ou com a zona da entrada. | 2 | P1 (corretude temporal) |
| G2 | **CONFIRMADO**: linha 754 — "Suas palavras voltaram. Reescreva com o olhar de hoje, se quiser — ou apenas deixe seguir." (e linha 733 o botão "Reescrever esta nota agora?"). Copy interpretativa residual — candidata a neutralização trivial no ciclo G. A neutralização da Missão 2 foi em superfície diferente (time picker). | 3 | P3 (identidade) |
| G3 | **CONFIRMADO parcialmente**: autosave via `draftChanges.debounce(700)` roda sempre (não bloqueia durante `saving`), mas o clear pós-save (`draftChanges.value = ""`) pode apagar texto digitado durante a janela do save (race entre o debounce pendente e o clear). Cancel-safe mas com janela estreita de perda. Mitigação de baixo risco: usar debounce com job cancelável e limpar só se o valor coincidir. | 1 | P2 (confiança) |
| G4 | Notice "Guardado" sem retry/manual — se fechado durante fade, sem confirmação visível | 1 | P3 |
| G5 | Política de retorno não editável do Home pós-save | 2 | P2 |
| G6 | strings.xml quase vazio — copy dispersa no código | todos | P3 (higiene) |
| G7 | Edit/Excluir do ArchiveRow: targets 48dp a confirmar | 3 | P2 (a11y) |
| G8 | `openedReturn`/pendingReturnId: fluxo de reabrir devolução — onde entra? (pending badge?) | 4/5 | P3 |
| G9 | **A VERIFICAR**: o helper `accessibilityInfoOf` (linha 1224) lê apenas `isTouchExplorationEnabled` (TalkBack). A linha 1197 chama esse helper e nomeia `reduceMotion` — se o nome é enganoso (TalkBack ≠ motion), o Motif é pulado para usuários TalkBack mesmo pedindo animação. O correto: combinar (TalkBack OU motion-reduced) para pular o Motif, e adicionar leitura de `ACCESSIBILITY_ANIMATIONS`/scale. Fix trivial do ciclo K. | — | P2 (a11y) |
| G10 | Archive vazio vs Home vazio: mensagens distintas mas tom consistente — bom. Sem gap. | — | — |
