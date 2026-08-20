# FIO-PB-01 — restaurar build verificável e reinstalar no aparelho

**Status:** DONE
**Tipo:** device
**Branch:** `integration/codex-build-device-20260820`
**Evidência mínima exigida:** dependency verification ativa, 134 testes unitários verdes, APK debug gerado e atualização instalada com sucesso no aparelho autorizado.

## 1. Objetivo

Restaurar a reprodução local segura do build depois da transferência do Atlas e atualizar o Fio no Poco M3 Pro sem apagar dados. Validar inicialização e ausência de crash até o gate biométrico.

## 2. Contrato

- Não desabilitar dependency verification.
- Não apagar dados do pacote principal `com.projetofio.app`.
- Não contornar biometria ou PIN.
- Não declarar validação interna das telas protegidas sem autenticação humana.
- Não alterar código, arquitetura, schema ou dependências declaradas.

## 3. Contexto técnico

- Gradle 9.5.0 / AGP 9.3.1.
- Android SDK local em `local.properties` ignorado pelo Git.
- Metadata auditável em `app/gradle/verification-metadata.xml`.
- Aparelho: Xiaomi M2103K19PG, Android 13/API 33.

## 4. Critérios de aceitação

1. `:mobile:testDebugUnitTest` executa com verification ativa.
2. 134 testes, 0 falhas, 0 erros e 0 ignorados.
3. `:mobile:assembleDebug` conclui.
4. `adb install -r -d` retorna `Success`.
5. `MainActivity` fica em primeiro plano sem `AndroidRuntime` fatal.

## 5. Riscos e portas de escape

- Se a assinatura for incompatível, parar sem desinstalar o aplicativo principal.
- Se a biometria bloquear a inspeção, validar somente até o gate e manter a parte interna como não validada.
- Chaves PGP indisponíveis permanecem acompanhadas dos hashes SHA-256 gerados; revisar o metadata em mudanças futuras de dependência.

## 6. Evidence log

| Data | O que | Output |
|---|---|---|
| 2026-08-20 | Suíte unitária | 134 testes, 0 falhas, 0 erros, 0 ignorados |
| 2026-08-20 | Build debug | BUILD SUCCESSFUL |
| 2026-08-20 | APK | SHA-256 `E83814E640DB8DF349FA2282337F676B4FE284614248E915C803B46E9EE84106` |
| 2026-08-20 | Reinstalação preservando dados | `adb install -r -d` → Success |
| 2026-08-20 | Inicialização | `com.projetofio.app/.MainActivity` ativa; gate biométrico exibido; nenhum fatal do app |
