# FIO-COSMIC-HOME-01 — final

- **Branch:** `codex/cosmic-home-v1`
- **Base HEAD:** `cf830cf645dcc83971ae5e6a1b51548a73a7ad0f`
- **Escopo:** somente a Home sob Céu Noturno e o acabamento da barra quando a Home está ativa.

## Assets e camadas

- `COS-ATM-001`: névoa de fundo em baixa opacidade.
- `COS-CEL-001`: constelações/órbitas no topo direito.
- `COS-CEL-003`: astrolábio periférico inferior esquerdo.
- `COS-ANC-001`: observatório line-art inferior direito.
- `COS-GLASS-001` e `COS-NAV-002`: referência de material, não imagens runtime.

O mapa completo está em `FIO-COSMIC-HOME-01-COMPOSITION-MAP.md`.

## Resultado

O editor de escrita ganhou profundidade de vidro fumê e borda clara, com o céu
e os assets visíveis apenas como atmosfera. O chip temporal, Guardar e a barra
continuam com as mesmas ações e textos. A barra ganhou vidro fumê e borda
somente na Home cósmica, sem alteração de destinos.

## Deliberadamente não feito

Nenhum fluxo de Lacrar, Favorito ou Fixado; nenhum planeta funcional; nenhuma
mudança em Time Sheet, calendário, Arquivo, Encontrar, nota, Ajustes, dados,
Search, Returns, schema ou navegação estrutural.

## Evidência e fidelidade

- Emulador: `Fio_API_36_Primary` (API 36).
- Captura: `plans/evidence/FIO-COSMIC-HOME-01-cycle1.png`.
- Score: atmosfera **5/5**, profundidade **5/5**, vidro **4/5**, ouro/luz
  **4/5**, símbolos/ornamentos **4/5**, legibilidade **5/5** — **27/30**.
- Gap residual: o vidro é implementado sem blur caro para preservar
  performance; portanto, não simula reflexo físico de modo excessivo.

## Validação

- `:mobile:assembleDebug` — passou.
- `:mobile:testDebugUnitTest` — passou.
- `:mobile:assembleRelease` — passou.
- `:mobile:lint` — passou.
- Regressão instrumentada no emulador: **31/32**. A única falha foi o teste
  preexistente `CalendarSelectionContractTest`, que não encontrou a célula de
  data esperada; calendário não foi alterado nesta missão.

## Risco residual

Há uma falha de automação de calendário no emulador a investigar em missão
própria. Não há evidência de regressão funcional causada pela Home.
