# Plan — Design system foundation (Fase 2, passo 1)

Fatia: adicionar tokens completos do sistema Verde-Sálvia ao módulo `ui/theme`
sem alterar nenhuma tela, nenhum comportamento e nenhuma dependência nova.

## State atual
`ui/theme/` tem apenas `Color.kt` (6 tokens) e `Theme.kt` (schemes parciais,
sem `secondary`, `outline`, `error`, sem `typography`). `FioApp.kt` usa
`MaterialTheme.colorScheme` e `typography` genéricos. Build atual: branch em
`feature/design-ux-v1` sobre main antigo (a4fc835). Nenhum teste de UI existe
ainda (verify em TEST abaixo).

## Ações
1. Expandir `Color.kt` com os 16 tokens do design system (modo claro) e os
   12 do dark, preservando os 6 nomes existentes como aliases.
2. Criar `Type.kt` com a escala tipográfica (Fraunces para displayBrand/
   displayDate, Inter para tudo o mais — ADR-013: nunca cursiva no corpo).
   Fontes via resource XML (`res/font`) empacotadas no APK — sem Google Fonts
   runtime, sem nova dependência de rede.
3. Completar `lightColorScheme`/`darkColorScheme` (secondary, tertiary,
   outline, outlineVariant, onSurfaceVariant, error, onError, success).
4. Aplicar `typography` no `FioTheme`.
5. Verificar: `./gradlew :mobile:assembleDebug` (se toolchain disponível;
   senão registrar BLOCKED — Android SDK ausente no sandbox, compilar em
   host Android do fundador) + testes unitários existentes não quebrados.

## Riscos
- Se Android SDK/Gradle não compilar no sandbox, a fatia vira código pronto
  com declaração honesta de que o build não foi executado aqui — testável
  quando o PR for integrado ao ambiente do fundador.
- O PR #1 (codex) também reescreve a UI; aplicar estes tokens sobre main
  antigo pode conflitar. Mitigação: tokens de cor são aditivos ao `Color.kt`
  (aliases preservados); se o PR #1 substituir `Color.kt`, o rebase de
  `feature/design-ui-v2` (após decisão do fundador) resolve com
  preferência pelos novos tokens.

## Next
Fase 2 passo 2: Home redesenhada (fila NEXT).
