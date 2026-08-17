# Campaign 14 — Visual Regression (ReturnScreen no design Verde-Sálvia)

**Prioridade:** P2 · **Método:** auditoria estática dos tokens de design aplicados na `ReturnScreen` contra o sistema Verde-Sálvia (Color.kt, Type.kt, FioSpace, FioRadius); screenshot real requer AVD/emulador — não disponível no sandbox. · **Resultado:** VERDE — a ReturnScreen aplica o design v1 com fidelidade; 1 recomendação P4.

## Verificação estática da ReturnScreen (FioApp.kt:1107-1136)

| # | Token do design v1 | Aplicação na ReturnScreen | Veredito |
|---|--------------------|---------------------------|----------|
| 1 | Background marfim `LightBackground` (#F8F4EA) | `MaterialTheme.colorScheme.background` (Scheme v1 define background = marfim) | OK |
| 2 | Fraunces display na data devolvida | `FioDisplayDate` (type system v1) + cor `primary` (sálvia) | OK |
| 3 | Tipografia body para o conteúdo | `bodyLarge` + `SelectionContainer` (permitindo cópia silenciosa) | OK |
| 4 | Botão primário ≥48dp, cantos grandes | `heightIn(min=48.dp)` + `RoundedCornerShape(FioRadius.lg)` | OK |
| 5 | CTA secundário discreto ("Não mostrar novamente") | `TextButton` (sem preenchimento), mesmo piso de toque | OK |
| 6 | Heading acessível | `headlineMedium` + `semantics { heading() }` | OK |
| 7 | Espaçamento `s4` entre blocos, `s6`/`s8` de página | `Arrangement.spacedBy(FioSpace.s4)` | OK |
| 8 | Paleta coerente (sem vermelho puro) | zero cores hardcoded fora do sistema; `LightError` terracota reservado ao ADR-045 | OK |

## Recomendação (P4)

1. **Screenshot de referência futuro:** quando houver AVD, capturar a ReturnScreen com um entry fixo e armazenar como referência em `docs/reference/` (ou integrar `paparazzi` ao CI). Hoje a verificação via diff de pixels não é possível no sandbox — o código demonstra adesão token a token, o que é o que importa para manter a essência.

## Conclusão

A seção de entrega (ReturnScreen) está **100% aderente ao sistema Verde-Sálvia v1**: tokens de cor, tipografia Fraunces na data, botões com piso de toque e heading acessível. Sem regressão visual detectável em código. A recomendação P4 (referência de screenshot) fica aberta.
