# Fio — Fluxos Completos e Microinterações

**Versão:** 1.0 · **Data:** 16 de agosto de 2026 · **Autor:** Manus AI

---

## 1. Fluxos completos

### 1.1 Fluxo primário — escrever e guardar (zero fricção)

```
Home (campo vazio, prompt, seletor "Algum dia", botão Guardar)
  → usuário digita (autosave silencioso a cada 700 ms, rascunho cifrado)
  → toque em Guardar (52 dp, primário)
    ├─ sucesso → botão encolhe 0,98 (120 ms) → pill "✓ Guardado." aparece
    │   acima do botão (fade-in 150 ms) → permanece 1,5 s
    │   (fade-out 300 ms) → campo limpo, Home volta ao estado inicial
    └─ falha recuperável → pill terracota "Não foi possível guardar agora.
        O texto continua no editor." → botão permanece habilitado
        → Guardar tenta de novo quando o usuário tocar
```

O autosave continua invisível: nada muda na tela. O rascunho é protegido pelo mesmo caminho cifrado das notas. Nenhuma etapa exige decisão.

### 1.2 Fluxo — guardar com tempo ("Lembrar depois")

A variante escolhida para menor fricção é a **Possibilidade 3 do briefing**: um seletor de uma linha abaixo do campo de texto, sempre presente mas discreto, com estado default "Algum dia".

```
Home
  → linha discreta "Quando isso pode voltar? · Algum dia →" (caption, primary)
    sob o campo de texto, sempre visível quando há texto digitado
  → toque → bottom sheet suave (E2, radius 20 na base)
      "Guardar como…"
      ○ Guardar normalmente   (volta espontaneamente, um dia)
      ○ Lembrar depois — 1 semana / 1 mês / 3 meses / 1 ano / Escolher data
      ○ Deixar descansar — 1 mês / 3 meses / 1 ano / até eu liberar
      ○ Nunca devolver
      (toques mínimo de 48 dp; seleção com check sutil em primary)
  → seleção → sheet desce (250 ms, easing suave)
  → seletor da Home atualiza: "Volta em 1 ano →" ou "Descansa 3 meses →"
  → toque em Guardar → "✓ Guardado. Volta em 1 ano."
```

Para notas **existentes** (Arquivo/Nota), as mesmas quatro ações vivem no menu "⋯" da tela da nota — nunca duplicadas na tela de escrita.

### 1.3 Fluxo — escolher data

```
No sheet temporal → "Escolher data"
  → grade de calendário discreta (dias como círculos 40 dp, mês/ano em caption)
  → navegação por mês com setas finas; sem campo de hora — datas são sempre
    à meia-noite, porque memória não tem horário
  → confirma → volta ao sheet com a data resumida ("7 de janeiro de 2027")
```

### 1.4 Fluxo — abrir e agir sobre uma nota

```
Arquivo → tap em EntryRow (16.08 — trecho de uma linha — • selo)
  → Nota: data em Fraunces, texto em bodyNote, menu ⋯ no topo
  → ⋯ abre bottom sheet com: Lembrar depois… / Deixar descansar… /
    Não devolver / Selar esta lembrança / Editar / Excluir
  → Editar → a tela entra em modo de edição (mesma superfície, toolbar sutil
    com ✓ e ✕) → guardar atualização → "✓ Atualizado." 1,5 s
  → Selar → biometria do aparelho → sheet fecha → indicador de selo
    aparece na linha do Arquivo
```

### 1.5 Fluxo — devolução (a tela mais importante)

```
Notificação: "Fio · Algo seu voltou." (sem preview, sem repetir se ignorada)
  → toque → ReturnScreen abre direto (sem splash)
      fundo com "o fio" botânico a 15 %
      data "18 de março de 2025" (Fraunces 22)
      container suave (E3) com o texto original (bodyNote)
      nada mais. Fecha com voltar → Home.
  → ⋯ (topo) → Não mostrar novamente / Deixar descansar… / Selar
  → [build de pesquisa] ao fechar, sheet separado e pulável:
      "Isso significou algo para você?" → Sim · Não · fechar
      (nunca bloqueia o fechamento; nunca reaparece obrigatoriamente)
```

### 1.6 Fluxos de estado

| Estado | Comportamento |
|---|---|
| Primeiro uso | Home limpa com o fio botânico central a 15 % + prompt. Sem onboarding de slides. |
| Home com rascunho salvo | Ao reabrir, o texto restaurado não gera nenhum aviso — apenas aparece, como se a pessoa tivesse parado de escrever. |
| Sem internet | Nenhuma mudança visível: tudo é local-first. Erro só aparece se a escrita local falhar. |
| Lixeira | Lista simples com Recuperar / Excluir para sempre (este exige biometria se bloqueio ativo). |
| Busca sem resultados | "Nada com essas palavras, por enquanto." + fio botânico. |
| Devolução programada | Indicador de relógio fino na linha do Arquivo + entrada "Em descanso" agrupada no fim do mês atual quando aplicável. |
| Nota selada no Arquivo | Linha mostra apenas data + ícone de selo; nenhum texto; tap pede biometria antes de abrir. |

---

## 2. Especificação de microinterações

Todas as transições seguem a regra canônica de 150–300 ms (com tolerância até 600 ms para sheets), easing `fastOutSlowIn`, e todas respeitam **Reduce Motion** (configuração de acessibilidade do Android): com Reduce Motion ativo, sheets deslizam 0 px (fade-only), pills aparecem sem fade e feedback tátil é o único retorno.

| Interação | Comportamento | Duração | Easing |
|---|---|---|---|
| Toque no Guardar | Press (escala 0,98, superfície −8 %) | 120 ms | linear |
| Feedback "Guardado." | Pill desliza 8 dp + fade-in; stays 1,5 s; fade-out | 150 / 300 ms | fastOutSlowIn |
| Pill "Guardado. Volta em 1 ano." | Igual, texto de 2 linhas permitido | 150 / 300 ms | fastOutSlowIn |
| Keyboard aparece | Home sobe com o campo (adjustResize), sem animação extra | — | — |
| Seletor temporal (sheet) | Sobe 24 dp + fade na máscara (40 % preto) | 250 ms | fastOutSlowIn |
| Seleção no sheet | Check em primary, linha eleva `surfaceVariant` | 150 ms | linear |
| Sheet fecha | Desce + fade | 250 ms | fastOutSlowIn |
| Abrir nota | A tela entra com o texto em fade (30 %) + subida 6 dp | 200 ms | linear-out |
| Devolução abre | Container E3 com fade + subida 8 dp; data antes do texto (stagger 60 ms) | 300 + 60 ms | fastOutSlowIn |
| Selar nota | Feedback tátil curto (tick ~30 ms) + selo desenha em 200 ms | 200 ms | linear |
| Excluir (mover à lixeira) | Linha encolhe em altura + fade (sem deslizar para fora) | 250 ms | fastOutSlowIn |
| Arquivo carrega | Linhas entram em stagger de 40 ms, 5 primeiros itens | 40 ms/item | linear-out |
| Modo escuro | Cross-fade do esquema de cores | 300 ms | linear |
| Alterar modo de edição | Toolbar desce 12 dp + fade | 200 ms | fastOutSlowIn |

### Feedback háptico (usar `VibrationEffect.COMPOSE_RICOCHET` com parcimônia)

| Momento | Haptic |
|---|---|
| Guardar com sucesso | Tick suave (1 tick) |
| Selecionar opção temporal | Tick suave |
| Selar nota | Tick duplo curto (padrão "trava") |
| Erro de salvamento | Nenhum (texto basta; não punir) |

---

## 3. Component inventory

Componentes reutilizáveis do sistema, prontos para o módulo `ui/components`:

| Componente | Responsabilidade | Usado em |
|---|---|---|
| `FioButton` (Primary/Secondary/Text/Destructive/Icon) | Botões com escala, press e alto de toque | Home, sheets, configurações |
| `FioTextField` | Campo do editor; sem borda quando foco; placeholder em tertiary | Home, edição inline |
| `EntryRow` | Linha do Arquivo: dia + trecho 1 linha + indicadores | Arquivo, busca |
| `ReturnView` | Container E3 + data Fraunces + texto | Devolução |
| `TimePickerSheet` | Sheet temporal com 4 modos + calendário sem hora | Home, menu da nota |
| `ContextMenu` | Bottom sheet de ações da nota | Tela da nota, devolução |
| `ArchiveSection` | Cabeçalho "Agosto de 2026" + lista de EntryRow | Arquivo |
| `PrivacyIndicator` | Selo / relógio / nunca-devolver discretos | EntryRow |
| `SaveToast` | Pill "Guardado." com ícone check | Home |
| `SectionHeader` | Título de seção de configurações (caption) | Configurações |
| `SettingsRow` | Célula de lista clicável com título + detalhe + seta fina | Configurações |
| `EmptyState` | Fio botânico + copy canônica | Home vazia, busca, lixeira |
| `FioTheme` (expandido) | ColorScheme completo + typography + spacing/radius tokens | Global |
| `BotanicalThread` | Motivo gráfico de linha e folhas | Empty states, devolução |
