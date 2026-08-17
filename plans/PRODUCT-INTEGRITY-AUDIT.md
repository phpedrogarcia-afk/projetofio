# Product Integrity Audit — Red Team de Produto (Missão 2)

**Branch:** `integration/manus-pre-codex-20260817` (derivada da `integration/manus-rehearsal-20260817`, HEAD `e0a7b7e`)
**Método:** auditoria adversarial em modo somente-leitura do código-fonte (`FioApp.kt`, `AndroidTimeReturns.kt`, `MainActivity.kt`, `TimeReturnEngine.kt`, design tokens) contra os 12 princípios canônicos de `docs/02-PRINCIPLES.md` e os ADRs 043–047. **Nenhum código foi alterado nesta fase** (corretivas triviais seguem na fase seguinte, sob as regras de autonomia da missão).

---

## Matriz dos princípios

| # | Princípio | Veredito | Evidência | Risco | Recomendação |
|---|-----------|----------|-----------|-------|--------------|
| 1 | As palavras do usuário dominam | **PASS** | `ReturnScreen`: título + data + texto puro (`SelectionContainer`), sem decoração competindo; conteúdo original e data original têm precedência sobre ornamento (motivo botânico tem alfa 0.55, 32dp de largura, TalkBack o ignora) | Baixo | Manter |
| 2 | Seleção sem interpretação | **PASS** | `TimeReturnEngine` seleciona por bucket/borda; nenhuma string no repo contém "evolui", "padrão", "insight", "sentimento", "percebemos", "você cresceu" (grep confirmou 0 ocorrências); notificação usa título fixo "Algo seu voltou."; ReturnScreen não interpreta | Baixo | Manter |
| 3 | O tempo é uma feature | **PASS** | Engine por janelas/quiet-hours/cap; policy choices mapeiam vocabulário ADR-043 ("Algum dia", "Em 7/30/90 dias", "Escolher uma data", "Nunca"); texto honesto: "O Fio decide o momento exato. Sua escolha apenas guia o que pode voltar." | Baixo | Manter |
| 4 | Silêncio sobre engajamento | **PASS** | 0 streaks/badges/pontos/meta/contadores; confirmação "Guardado." some em 1.5s (pill, não tela); sem "continue escrevendo"; Pátina não depende de produtividade (só idade desde a 1ª entrada, determinística); sem notificação de ausência; notificação IMPORTANCE_LOW, visibilidade private | Baixo | Manter |
| 5 | Autonomia antes de magia | **PASS** | Pausar/retomar devoluções (1 toque), "Nunca" (ineligível permanente), "Deixar descansar" implícito via Rest, "Não mostrar novamente" no ReturnScreen, exclusão e export sem barreiras; exclusão de 30 dias recuperável; exclusão permanente com confirmação | Baixo | Manter |
| 6 | Privado por arquitetura | **PASS** | Missão 1 confirmou: backup cloud/transfer excluídos, FLAG_SECURE, zero Log., notificação sem conteúdo; conteúdo cifrado com AAD | Baixo | Manter |
| 7 | Local e offline primeiro | **PASS** | Zero dependência de rede (usesCleartextTraffic=false, nenhum domínio); export local via SAF; confirmação "Exportação concluída no local escolhido." | Baixo | Manter — **analytics remoto do piloto será o primeiro ponto que desafia este princípio** (ver pilot protocol) |
| 8 | Beleza não compete | **PASS** | Sálvia/marfim, Fraunces só para identidade/data, sem cursiva no corpo, sem animação de cadeado/envelope, motivo botânico pequeno (56dp) e alfa 0.55; **finding WEAK: `Reduce Motion` não é respeitado explicitamente — `Canvas` sempre desenha e não há checagem de `AccessibilityServiceInfo`** | Médio | Corrigir (P2): respeitar Reduce Motion na Pátina |
| 9 | Arquivo completo mas secundário | **PASS** | Arquivo não é CTA principal (Home prioriza escrita: placeholder "Escreva quando quiser.", pergunta temporal secundária); entries completas com data absoluta + relativa; Excluídos recentemente preserva 30 dias | Baixo | Manter |
| 10 | Simplicidade ganha complexidade | **PASS** | Apenas 1 work no background; sem motor semântico; semantic selection fica atrás de flag (conforme ADR-010); sem feature flag de analytics embutido | Baixo | Manter |
| 11 | Confiança inclui partida | **PASS** | Export Markdown+Texto sem proprietary format (ADR-046, checksum SHA-256 confirmado na Missão 1); exclusão permanente possível | Baixo | Manter |
| 12 | Pesquisa não contamina | **PASS** | Nenhum analytics embutido hoje; pergunta de ressonância do piloto planejada para APÓS o retorno, nunca como comentário sob a memória (a ser codificada no pilot protocol) | Baixo | Manter — vigilância ativa no piloto |

## Investigação específica 1 — Gamificação acidental

Resultado: **nenhum indicador encontrado.** `grep` por streak/badge/pontos/nível/progresso/meta em `strings.xml` e código: zero. A Pátina Temporal foi auditada linha a linha (`BotanicalMotif`): input único = `ChronoUnit.DAYS.between(firstEntryAt, now)`; count/frequência/sessões/retornos explicitamente excluídos; sem níveis exibidos (4 estados internos determinísticos, nunca apresentados como níveis); **WEAK: não respeita Reduce Motion** (ver princípio 8). Nenhum texto celebra ("parabéns", "ótimo", "sucesso"): zero ocorrências.

## Investigação específica 2 — IA/interpretação acidental

Resultado: **limpo.** O repo não contém "você evoluiu", "você mudou", "percebemos", "seu padrão", "você costuma", "insight", "sentimento", "emotional score", "mental state". A máquina seleciona (engine) e nunca conclui — inclusive a confirmação pós-save e a notificação são factuais e curtas.

## Investigação específica 3 — Produtividade acidental

Resultado: **limpo.** Sem deadline, sem "completo/tarefa", sem alarmes recorrentes, sem calendário de produtividade. Único ponto de atenção: `TimeOption("Em 7 dias", "Em 30 dias"...)` — os períodos são escolhas do usuário, não metas; o texto de apoio "O Fio decide o momento exato..." reforça que não é agenda. **WEAK menor:** "Reescrita com o olhar de hoje?" (linha 732, retorno imediato) — levemente interpretativa ("com o olhar de hoje" insinua mudança); recomendo simplificar para neutro como correção trivial permitida.

## Investigação específica 4 — Home: hierarquia "escreva em segundos"

Resultado: **PASS com ressalva.** A Home abre com editor + placeholder "Escreva quando quiser." + pergunta temporal secundária. O chip "Quando isso pode voltar?" (linha 320) e o botão "Escolher" coexistem. **WEAK identificado:** o peso visual do chip temporal está logo acima do botão Guardar — aceitável, mas o founder pode querer testar em piloto se a pergunta rouba atenção da escrita. **WEAK identificado (repetição):** as TimeOptions "Em 7/30/90 dias/1 ano" usam linguagem de calendário ("Em X dias"), coerente com ADR-043, mas "Nunca" aparece na mesma lista — coerente; sem problema.

## Investigação específica 5 — Primeira cápsula (S-3)

Resultado: **PASS.** ADR-044 implementado: `isFirstSave = consent NOT_CONFIGURED && entries.isEmpty() && savedNotice`; copy estendida só na primeira vez: "Guardado. O tempo cuida do resto."; demais saves: "Guardado."; sem tutorial, sem nome, sem permissão no primeiro segundo. A permissão de notificação é pedida **após o consentimento de devoluções** (`requestNotificationPermissionAfterConsent`) — timing contextual correto, documentado na seção de permissões do audit.

## Investigação específica 6 — Data-âncora (S-5)

Resultado: **PASS.** "Escolher uma data" → `ReturnPolicy.OnDate` → candidatos do engine (nunca entregas forçadas), respeitando cap/rest/Never/consent. Copy factual única do engine: "Algo seu voltou." — sem interpretação. Data-âncora como obrigação anual ainda **não implementada** (correto: é futuro, ADR-047).

## Investigação específica 7 — S-2 revisado (data absoluta primária)

Resultado: **PASS.** `ArchiveRow` mostra `displayDate(entry)` (data absoluta, locale pt-BR, MEDIUM/SHORT) **antes** de `temporalDistance(entry)` ("há N dias/meses/anos"). Data absoluta é primária; relativa é contexto. Conforme especificação.

## Investigação específica 8 — Pátina Temporal (S-4 revisado)

Resultado: **PASS com 1 WEAK.** Conforme ADR-045: input = apenas idade desde primeira entrada; não regredir (cresce enquanto usuário não escreve — correto, `ageDays` usa `Instant.now()`); sem níveis exibidos; TalkBack ignora (`Canvas` sem semântica, decorativo); removível via `PATINA_ENABLED` flag. **WEAK (P2):** não respeita Reduce Motion — em dispositivos com "Remover animações", o `Canvas` ainda desenha o motivo completo. Corrigível trivialmente.

## Investigação específica 9 — Devolução (ReturnScreen como superfície crítica)

Resultado: **PASS com 1 WEAK.** Estrutura: título "Uma palavra sua voltou" (heading) → data Fraunces em sálvia → texto puro (SelectionContainer) → espaçamento → botão primário "Fechar" (48dp) → secundário "Não mostrar novamente". As palavras são o centro; ornamento zero; dark mode usa tokens dedicados (verde-carvão, não inversão pura). **WEAK menor:** a linha 732 "Reescrita com o olhar de hoje?" é levemente interpretativa — ver Investigação 3. **WEAK operacional:** `large font` e `dark mode` não foram executáveis neste sandbox (sem AVD); ficam como gates humanos documentados no handoff.

## Investigação específica 10 — Permissões

Resultado: **PASS.** `POST_NOTIFICATIONS` é pedida só após o usuário ativar devoluções (`requestNotificationPermissionAfterConsent`), nunca no primeiro segundo. Nenhum receiver/boot/serviço exporta superfície. FLAG_SECURE incondicional. Backup excluído (campanha 5 da Missão 1).

## Investigação específica 11 — Ritmo do ritual e copy do "desfazer lote"

Resultado: **PASS.** "Desfazer este lote" com texto explicativo honesto (o que vai, o que fica). Nenhum teaser sensacionalista. O Ritual do Fio permanece Planned (ADR-047) e não foi antecipado — conforme regra da Missão 2 (não desbloquear antes das dependências).

---

## Resumo executivo do Red Team

O Fio **não trai seus próprios princípios**. Dos 12 princípios: 11 PASS diretos, 1 PASS com WEAK (Princípio 8 — Pátina sem Reduce Motion). Investigações de gamificação/IA/produtividade acidental: limpas. WEAKs encontrados (ordenados):

| # | WEAK | Severidade | Correção |
|---|------|------------|----------|
| 1 | Pátina não respeita Reduce Motion | P2 | Verificar `isReduceMotionEnabled`/service info; pular `Canvas` quando ativo |
| 2 | "Reescrita com o olhar de hoje?" é levemente interpretativo | P3 | Trocar por neutro ("Reescrever esta nota agora?") — coerente com contentDescription já neutra |
| 3 | Large font / dark mode / TalkBack real não executáveis no sandbox | Gate | Scripts humanos documentados no handoff |
| 4 | Chip temporal compete levemente com o editor na Home | P4 | Testar no piloto, não alterar sem evidência |
