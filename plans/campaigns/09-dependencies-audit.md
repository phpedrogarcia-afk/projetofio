# Campanha 9 — Dependencies Audit

**Data:** 2026-08-17 · **Estado:** DONE
**Prioridade:** P2 · **Branch:** `integration/manus-rehearsal-20260817`

## Método

Inspeção de `app/gradle/libs.versions.toml` + árvore de dependências resolvida (`./gradlew :mobile:dependencies --configuration debugRuntimeClasspath`), cruzada com buscas públicas de CVEs (NVD, Snyk, GitHub Security Lab) para cada artefato em versão não-BOM.

## Versões diretas auditadas

| Dependência | Versão atual | Última estável conhecida (ago/2026) | Veredicto |
|---|---|---|---|
| AGP | 9.3.1 | recente | OK |
| Compose BOM | 2026.06.01 | recente | OK |
| Kotlin plugin (compose-compiler/KSP) | 2.3.x | recente | OK |
| Activity Compose | 1.13.0 | recente | OK |
| Lifecycle | 2.10.0 | recente | OK (fix regressão NPE) |
| Room | 2.8.4 | recente | OK |
| Coroutines | 1.11.0 | recente | OK |
| Serialization | 1.8.1 | recente | OK |
| WorkManager | 2.11.2 | recente | OK |
| AndroidX Test / Espresso | 1.7.0 / 3.7.0 | recente | OK (test-only) |

## Transientes envelhecidas (resolvidas pelo Gradle)

- `kotlin-stdlib` transitivo antigo (1.3.71–1.9.24) de `androidx.concurrent:concurrent-futures-ktx:1.1.0` → **forçado a 2.2.20** pelo Gradle (linha `-> 2.2.20`). Sem risco real, mas o artefato transitivo antigo permanece declarado na árvore.
- `androidx.compose.material3:material3:1.3.1` aparece como transitivo antigo resolvido para 1.4.0 — BOM 2026.06 domina. Sem risco.

## CVEs

Busca em NVD/Snyk/GitHub Security Lab para Room 2.8.4, kotlinx-coroutines 1.11.0, lifecycle 2.10.0, work-runtime 2.11.2: **zero CVEs ativos registrados** para as versões usadas. Nenhum artefato na árvore tem vulnerabilidade conhecida (verificação também via `secure.software`/Snyk DB para coroutines-core: "No direct vulnerabilities").

## Findings

1. **(P3) `biometric:1.1.0`** — sem CVE, mas o artefato 1.1.x está além do ciclo de vida e pode ter comportamento inconsistente em alguns OEMs. Sugere-se avaliar `androidx.credentials` + `androidx.biometric` mais recente quando houver migração planejada. **Decisão de produto necessária** (não fazemos mudança criptográfica/UX nesta missão).
2. **(P4) `fragment:1.8.9`** — usado apenas como ponte de Compose→fragment? Verificar se a dependência é necessária (o app é Compose-puro). Se não houver `Fragment`/`FragmentActivity` em uso, pode ser removida, reduzindo superfície transitiva (appcompat, lifecycle-process).
3. **(P4) `androidx.concurrent:concurrent-futures-ktx:1.1.0`** traz stdlib antigos na árvore (forçados, sem efeito binário). Sem ação necessária além do `dependency-verification` atual que já controla checksums — **verificação de dependências está ativa e é um ponto forte** (não temos `--write-verification-metadata` a rodar).

## Conclusão

**Sem finding P0/P1/P2.** Ecossistema moderno e sem CVEs ativos. Dois P3/P4 de higiene (biometric legacy, fragment possivelmente removível) ficam como recomendações documentadas, não aplicadas.
