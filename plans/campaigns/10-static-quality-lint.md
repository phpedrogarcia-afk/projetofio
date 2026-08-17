# Campanha 10 — Static Quality (lint Android)

**Data:** 2026-08-17 · **Estado:** DONE
**Prioridade:** P3 · **Branch:** `integration/manus-rehearsal-20260817`

## Configuração

O projeto não usa ktlint/detekt como plugins dedicados (apenas plugin Compose + KSP). Rodamos `./gradlew :mobile:lintDebug` (lint 9.3.1) — **0 erros**.

## Warnings (13) — distribuição

| Categoria | Count | Ação |
|---|---|---|
| GradleDependency / NewerVersionAvailable | 8 | Informativo — versões já auditadas na campanha 9; nada crítico |
| AndroidGradlePluginVersion | 1 | Informativo |
| OldTargetApi | 1 | Informativo (targetSdk=36, lint sugere alinhar ao SDK previsto) |
| **DefaultLocale** | 1 | **Corrigido inline** (ver finding) |
| **UnusedResources** | 2 | **Removidos** (ver finding) |

## Findings aplicados

1. **(P3 → FIXED) `DefaultLocale` em `FioApp.kt:1075`** — `quietHoursLabel` formata minutos com `String.format` implícito usando o locale do dispositivo para zeros numéricos. Em locales com dígitos não-ocidentais (ar-EG, fa), o rótulo de quiet-hours renderizaria "22h٠٠". **Fix:** passar `Locale.ROOT` ao `String.format`. Como não há ktlint instalado e o risco é visível, aplicamos diretamente (bug claro e reproduzível).
2. **(P4 → FIXED) `UnusedResources`:** `res/drawable/ic_thread.xml` (asset antigo do PR de redesign não referenciado) e `string write_prompt` (não usado em nenhum composable) — **removidos**, reduzindo APK e ruído de strings.

## Decisões

- ktlint/detekt como plugins permanentes: **recomendado como evolução**, mas adicionar agora criaria um diff de formatação massivo fora do escopo da missão (manter confiança no código existente). Documentado como recomendação P3.
