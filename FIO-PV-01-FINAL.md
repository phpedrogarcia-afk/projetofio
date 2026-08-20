# FIO-PV-01 — Final

## 1. Branch e HEAD

- Branch: `codex/visual-intent-v1`
- Base: `codex/context-efficiency-v1` em `9b1463e`
- HEAD final: consultar `git rev-parse HEAD` na entrega; este relatório faz
  parte do próprio commit final.

## 2. Superfícies modificadas

Home/Escrever, navegação inferior, escolha de tempo, calendário, Encontrar,
Arquivo, detalhe da nota, Ajustes/export e tokens Material light/dark.

## 3. Before/after

O before mostrou editor sem presença, tempo oculto, indicador lilás genérico,
metadado com hora e detalhe cuja data competia com as palavras. O after aplica
papel quente, sálvia, limites discretos, hierarquia editorial e densidade da
imagem norte sem mudar os contratos de domínio.

## 4. MATCH / ADAPT / IGNORE

- MATCH: Home, time sheet, Encontrar, Arquivo e hierarquia da nota.
- ADAPT: calendário Material nativo; Ajustes mostra apenas destinos reais do
  build; dark mode é equivalente emocional, não inversão da imagem clara.
- IGNORE: hardware/gestos iOS, ações inexistentes e artefatos do mockup.

## 5. Arquivos alterados

Arquivos por superfície em `ui/`, `ui/theme/Theme.kt`, dois contratos de UI,
este packet, o visual delta e as evidências selecionadas.

## 6. Testes

- 134 unitários, 0 falhas/erros/ignorados.
- 28/28 instrumentados no `Fio_API26(AVD)`; nenhum aparelho físico usado.
- Debug, Release e lint Debug verdes.
- Light pt-BR/font 1.0, dark e fonte 1.3 inspecionados no emulador.

## 7. Screenshots

Um before e dez after representativos em `plans/evidence/FIO-PV-01/`, cobrindo
Home, tempo, calendário, Encontrar, Arquivo, nota, Ajustes, dark e fonte grande.

## 8. Diferenças restantes

- O calendário conserva layout/gestos Material, embora a copy e a seleção
  reflitam a intenção Fio.
- Ajustes não cria Ajuda, Sobre ou Notificações como destinos falsos.
- Ícones novos não foram inventados para ações que ainda não existem.

## 9. Decisões humanas

- FIO-P19: decidir o destino real de `Devolver para agora`; nesta missão ele
  foi apenas integrado à hierarquia, sem remoção ou promoção funcional.
- A política escolhida na Home continua somente em estado Compose, conforme o
  contrato existente; persistência exige packet de domínio separado.

## 10. Próxima ação

Revisão visual humana no emulador e, se aprovada, push/PR separado. Não fazer
merge nesta missão.
