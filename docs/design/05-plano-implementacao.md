# Fio — Plano de Implementação

**Versão:** 1.0 · **Data:** 16 de agosto de 2026 · **Autor:** Manus AI

---

## 1. Estratégia de migração

A reorganização proposta respeita o código existente do Fio e foi desenhada como **evolução incremental em quatro etapas (A–D)**, cada uma encerrável em um pull request com escopo bem definido. Nada exige reescrever a camada de domínio: as notas, a criptografia e o motor de Returns permanecem como estão; o que muda é a camada de apresentação (`ui/`) e pequenos acréscimos de modelo descritos na Parte 2 deste pacote. A ordem das etapas prioriza o que reduz fricção do fluxo primário primeiro, porque é ele que define se a pessoa volta a escrever.

| Etapa | Escopo | Risco | Dependência |
|---|---|---|---|
| A | Fundação do design system (tokens, tema, componentes) | Baixo | Nenhuma |
| B | Nova navegação e telas (Home unificada, Arquivo agrupado, Nota) | Médio | A |
| C | Seletor temporal e menu contextual | Médio | B |
| D | Microinterações, acessibilidade e refinamento final | Baixo | B, C |

## 2. Etapa A — Fundação do design system

Nesta etapa se cria o vocabulário visual que todas as demais dependem. No módulo `app/mobile/src/main/java/com/projetofio/app/ui/theme/`, o objeto `Color.kt` atual (que hoje expõe apenas três cores primárias) é expandido para o sistema completo da Parte 3: a paleta de sete famílias (sálvia, marfim, carvão, terracota, dourado) com seus papéis semânticos (primary, onPrimary, surface, onSurface, surfaceVariant, outline, error etc.) em light e dark. As definições de `Color.kt` atuais (`SagePrimary`, `IvoryLight`, `CharcoalDark`) são preservadas como aliases e mapeadas para os novos tokens, de modo que nenhum código existente quebra.

Em seguida, `Type.kt` recebe os dois papéis tipográficos (Fraunces para display/data, Inter para corpo) com a escala completa descrita na Parte 3, e um novo objeto `Spacing.kt` define os tokens de espaçamento (2/4/8/12/16/24/32) e de raio (8/12/16/20). Os componentes base — `FioButton`, `FioTextField`, `EntryRow`, `ArchiveSection`, `EmptyState`, `SectionHeader`, `SettingsRow`, `SaveToast` — são criados em um módulo `ui/components` próprio, cada um consumindo exclusivamente os tokens, nunca cores ou números literais.

**Critério de aceitação:** o app compila com o novo tema aplicado, a Home atual (inalterada em comportamento) é renderizada com o novo esquema de cores, e um preview de todos os componentes base existe para verificação visual em dark/light.

## 3. Etapa B — Nova navegação e telas principais

Com o vocabulário pronto, a estrutura de telas de `FioApp.kt` é reorganizada conforme o mapa da Parte 2. A bottom bar de três abas sem ícones é substituída por **navegação por empilhamento**: a Home é a raiz única (sem barra inferior), e o Arquivo é alcançado a partir de um item do menu "⋯" da Home — hoje já existente como "Histórico", o que torna a transição natural para usuários atuais.

A Home passa a ser a tela unificada de escrita: prompt canônico ("O que está passando pela sua cabeça hoje?"), campo de texto sem moldura com apenas a linha de foco em sálvia, e o seletor temporal discreto (na Etapa C). O modal de edição pesado é eliminado: a tela da nota entra em modo de edição no lugar, com uma toolbar sutil de salvar/descartar, conforme descrito no fluxo 1.4 da Parte 4.

No Arquivo, a lista plana de cards é substituída por `ArchiveSection` com cabeçalhos "Agosto de 2026" e linhas tipográficas (dia + trecho de uma linha), com os indicadores discretos de selo e devolução programada na margem direita. A busca vira uma variante de tela com campo no topo e `EmptyState` com copy canônica ("Nada com essas palavras, por enquanto.").

**Critério de aceitação:** todos os fluxos da Parte 4 (1.1, 1.4, estados de vazio) funcionam em dispositivo físico e emulador; a navegação de volta preserva o rascunho; os previews das seis telas principais batem com os mockups `hi-01` a `hi-06`.

## 4. Etapa C — Seletor temporal e menu contextual

Esta etapa implementa o coração conceitual da reorganização: o **tempo como dimensão de primeira classe da nota**. O componente `TimePickerSheet` é construído com os quatro modos (normal, lembrar depois, deixar descansar, nunca devolver), os chips de duração, o calendário sem hora e a lógica de persistência do campo `returnPolicy` no modelo `Entry` (extensão mínima descrita na Parte 2). O mesmo sheet é reaproveitado pelo menu contextual da nota (`ContextMenu`), que também abriga "Selar esta lembrança" (com chamada de biometria via `BiometricPrompt`) e "Editar".

A tela de devolução (`ReturnScreen`) recebe o tratamento mais cuidadoso de toda a etapa: fundo com o fio botânico a 15 %, data em Fraunces, texto em container suave, zero distração, e o sheet opcional de pesquisa ("Isso significou algo para você?") marcado como `[build de pesquisa]` para ser implementado apenas se e quando o produto validar a necessidade.

**Critério de aceitação:** uma nota criada com "lembrar depois — 1 ano" não reaparece antes de 365 dias; "nunca devolver" retira a nota do ciclo de Returns; selar uma nota exige biometria para abrir; a tela de devolução abre em menos de 300 ms após o toque na notificação.

## 5. Etapa D — Microinterações, acessibilidade e refinamento

A última etapa adiciona a camada de polimento que transforma uma interface correta em uma interface afetiva. As transições da tabela da Parte 4 são implementadas com os parâmetros exatos (duração, easing, stagger), o feedback háptico é conectado aos quatro momentos definidos, e o suporte a **Reduce Motion** é verificado com a configuração de acessibilidade do Android ativa. A verificação de contraste atinge AA para todo o texto (a exceção intencional do fio botânico decorativo permanece abaixo de 4,5:1 por ser puramente ornamental). O modo escuro recebe o teste final de cross-fade, e a escala de fonte dinâmica do sistema é validada até 130 %.

**Critério de aceitação:** checklist de microinterações da Parte 4 executado em relatório de QA; contraste validado por ferramenta automática; app testado com texto grande e com Reduce Motion; nenhum crash em teste de exploração de 15 minutos por usuário de teste.

## 6. Riscos e mitigação

| Risco | Mitigação |
|---|---|
| Migração do modelo Entry (novo `returnPolicy`) quebra notas existentes | Migration com valor default `ReturnPolicy.Someday` preservando comportamento atual |
| Bottom sheet temporal adiciona um passo ao fluxo primário | Seletor na Home começa sempre em "Algum dia" — um toque é exigido apenas de quem quer controlar o tempo; fluxo primário mantém um toque único |
| Fraunces (fonte externa) em Jetpack Compose | Fallback imediato para a serifa do sistema; download da fonte via `FontFamily` com `res/font` |
| Biometria indisponível no aparelho | "Selar" oculta-se ou exibe justificativa; nunca falha silenciosamente |
| Escopo da tela de devolução pode inflar (v0.3 quer socialização, compartilhamento) | Regra: devolução é leitura pura na v1; tudo além disso entra por feature flag |

## 7. Métricas de sucesso (definir baseline antes da Etapa B)

O desenho todo se defende por uma única hipótese central: **reduzir a fricção do primeiro gesto aumenta a taxa de retorno**. As métricas a acompanhar após o rollout são a taxa de notas criadas por usuário ativo semanal (baseline atual vs. pós-Etapa B), a taxa de abandono do fluxo de escrita iniciada e não guardada (hoje o modal de edição é suspeito de gerar perdas), a taxa de abertura das devoluções (engajamento com o mecanismo central do produto) e o tempo de escrita mediano até o primeiro Guardar. Se após as quatro etapas a taxa de notas guardadas não subir em pelo menos duas dessas quatro métricas, o seletor temporal deve ser revisado — não removido, mas reposicionado (por exemplo, movido para o menu contextual apenas).

## 8. Onde este pacote vive no repositório

Os seis documentos deste pacote (`docs/01` a `docs/06`) e os assets (`wireframes/`, `mockups/`) devem ser enviados ao repositório em `docs/design/`, junto de um `DESIGN-README.md` apontando cada artefato para sua seção no código de destino. Recomenda-se abrir um issue-mãe "Reorganização UX v1" com quatro sub-issues (um por etapa), e anexar este documento como descrição, de modo que qualquer pessoa futura — humana ou agente — entenda por que a interface é como é.
