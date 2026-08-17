# Hardening Queue — ProjetoFio MANUS MISSION 1

**Branch de execução:** `integration/manus-rehearsal-20260817` (8be819a)
**Regra de fila:** NOW = 1 campanha ativa; NEXT = no máximo 3 em espera. Nada vai para main.
**Classificação:** P0 = perda/exposição/corrupção de dados · P1 = quebra do fluxo principal · P2 = regressão séria · P3 = qualidade · P4 = polish.
**Estados:** PLANNED → RUNNING → (FOUND | BLOCKED | DECISION REQUIRED) → FIXED → VERIFIED → DONE

---

## Fila atual

| Ordem | Campanha | Prioridade | Estado | Resultado |
|-------|----------|------------|--------|-----------|
| 1 | **Data Torture — Unicode & encoding** | P0 | DONE (commits 00bb186, 20b7fae) | FINDING A (P0-class): surrogate halves substituídos por 0x3F silenciosamente antes da criptografia → fix `CryptoFailure.InvalidPlaintext`; findings B/C (P4): ZW-only content aceito por `isBlank()`, documentado |
| 2 | **Crypto Pre-Review + Plaintext Hunt** | P0 | DONE (commit a18e803) | `docs/security/CRYPTO-REVIEW-PACKET.md`; zero plaintext leaks ativos; notificação, backup e cleartext verificados |
| 3 | **Database / Migration Torture (Room 2→3)** | P0 | DONE (commit 84a0db2) | `Migration2To3Test` verde: 500 entries + deletados, envelopes byte-idênticos, invariante soft-delete, defaults de settings, estrutura = schema-3.json |
| 4 | **Returns Engine Torture (determinismo M4)** | P1 | DONE (commit 0aaca08) | 14 testes novos (`EngineTortureTest`): sweep de 10.000 avaliações sem violação de quiet-hours, DST, buckets 6–2000d, bootstrap, cap; 76 testes verdes |
| 5 | **Privacy Boundary & Android Backup** | P0 | DONE (commit 78ed305) | zero P0/P1: backup cloud/device-transfer/legacy excluídos, FLAG_SECURE incondicional, zero clipboard/cache/log leaks, SAF sem rastro; P4: sugestão de teclado em TextFields documentada |
| 6 | **Time Torture + Fuzz engine** | P1 | DONE (commit 35a6390) | 18 testes no `EngineTortureTest`: fusos UTC±14/+5:45, DST fall-back, epoch 1970/2038/3000, ano bissexto, sweep de sementes — 0 violações de quiet-hours |
| 7 | **Storage Failure (SAF, disco cheio, permissões)** | P1 | DONE (commit 1203f83) | `StorageFailureTest` (7 testes): disco cheio/permission-denied → `ExportOutcome.FAILED`, CancellationException propaga, falha de build não escreve; SAF não guarda cópia parcial |
| 8 | **Export Torture + Round-Trip (SHA-256 ADR-046)** | P1 | DONE (commit dea9671) | `ExportRoundTripTest` (7 testes): checksum reproduzível por implementação independente SHA-256, 1 byte muda o hash, surrogate halves do P0 sobrevive ao export sem substituição |
| 9 | **Dependencies audit** | P2 | DONE (commit 5555755) | zero CVEs ativos; transientes antigos forçados pelo Gradle; `biometric:1.1.0` P3 (avaliar androidx.credentials), `fragment` P4 (removível — usado só por FragmentActivity) |
| 10 | **Static quality (lint Android)** | P3 | DONE (commit a84312c) | 0 erros; fix `DefaultLocale` em `quietHoursLabel` (Locale.ROOT), remoção de `ic_thread`/`write_prompt` não usados, lockfile atualizado para Robolectric; ktlint/detekt recomendado como evolução futura |
| 11 | **Performance (10k+ entradas, archive tipográfico)** | P2 | DONE (commit em progresso) | `PerformanceTest` (5 testes) verde: scans 10k <0.32s, purgeExpired 1k <0.22s, rollbackImport 500 <0.5s, Flow emite 1× por mudança; seed 9k ativos + 1k soft-deletados |
| 12 | **Battery / Background (notificações em Doze)** | P2 | DONE (commit da32538*) | Doze-friendly por design: 1 work não-exact, sem foreground/alarmes/boot-receiver/rede; P3: boot receiver opcional, constraint battery-not-low |
| 13 | **Accessibility (TalkBack nas seções M2/M3)** | P2 | DONE (estática) | headings/contentDescription/liveRegion/mergeDescendants presentes; P3: teste TalkBack manual em AVD, contraste WCAG AA em labels pequenas |
| 14 | **Visual Regression (ReturnScreen no design)** | P2 | DONE (estática) | ReturnScreen 100% aderente aos tokens Verde-Sálvia v1 (cor, Fraunces, piso 48dp, heading); P4: screenshot de referência futuro |
| 15 | **Error UX** | P3 | DONE (estática) | pill terracota com liveRegion Polite, CancellationException nunca engolida, labels de issues pt-BR, garantia "nada foi apagado"; P3: retry affordance futura |
| 16 | **Fuzz / Property-based (funções puras do engine)** | P1 | DONE (merge na campanha 6, commit 35a6390) | sweep de sementes no `EngineTortureTest` + propriedade de 10.000 avaliações |
| 17 | **Large History (10k entries, GC de deletados)** | P2 | COBERTA PARCIAL (campanha 11: purgeExpired + scans 10k) | ver relatório da campanha 11 |

---

## NOW (campanha ativa)

### 1. Data Torture — Unicode & encoding — CONCLUÍDA
**Resultado:** 21 payloads hostis + 10.000 strings aleatórias preservadas bit a bit no round-trip Entry→envelope→Entry→Export; checksum SHA-256 sensível a 1 byte; AAD vincula kind/id/schema. Finding A (P0-class): surrogate halves eram substituídos por 0x3F antes da criptografia — fixado com `CryptoFailure.InvalidPlaintext`. Findings B/C (P4): ZW-only content aceito — documentado, decisão de produto aberta.

## NEXT (máx. 3)

### 2. Crypto Pre-Review + Plaintext Hunt — P0 — CONCLUÍDA
**Resultado:** review completo em `docs/security/CRYPTO-REVIEW-PACKET.md`: AES-256-GCM com nonce aleatório por envelope (IV gerado em `seal`), AAD vinculando kind/id/schema, Keystore AndroidKeyStore, zero leaks em Log/clipboard/backup (cloud backup excluído via `android:allowBackup=false` + dataExtractionRules), notificação sem conteúdo, FLAG_SECURE ativo. Sem mudanças criptográficas necessárias.

### 2b (objetivo original, agora superset pela versão concluída)
**Objetivo:** revisar `FioService`/keystore AES-256-GCM, caçar plaintext em logs, SharedPreferences, cache, backups e exceções.
**Plano:** grep por `Log.`/`printStackTrace`/`toString` de chaves; verificar `KeyStore` key aliases; verificar que o AES-GCM usa nonce aleatório por operação e não reusa; verificar onde o conteúdo em claro existe em memória; conferir `android:allowBackup` e `android:dataExtractionRules`; conferir que exceptions não logam conteúdo de Entry.
**Entregável:** `docs/security/CRYPTO-REVIEW-PACKET.md`.
**Decisões exigidas:** QUALQUER mudança no esquema criptográfico PARA e pede autorização.

### 3. Database / Migration Torture — Room 2→3 — P0 — CONCLUÍDA
**Resultado:** `Migration2To3Test` (Robolectric): seed schema-2 realista (500 entries incl. 20 soft-deletados, drafts, settings, 25 returns) → Room aplica `MIGRATION_2_3` sozinho → contagens preservadas, envelopes byte-idênticos, invariante deleted_at⇔purge_after, novos campos null, defaults de settings sobrevivem, estrutura física consistente com schema-3.json. Confirma que `fallbackToDestructiveMigration(false)` + migration existente é o caminho único sem perda. Robolectric shadow não reproduz o IllegalStateException (documentado no teste).

### 3b. Database / Migration Torture (objetivo original)
**Objetivo:** migrar schema 2→3 (M2/M3/M4: returnMode, lastReturnedAt, returnCount, importBatchId, AppSettings quietHours) com dados sintéticos realistas.
**Plano:** gerar base schema 2 com 500 entries + deletados; aplicar migração (autoMigration ou manual); verificar contagem, integridade de content, mapeamento de returnMode default (SOMEDAY), defaults de quietHours.
**Critério:** mesma contagem pré/pós; conteúdo idêntico; engine funciona sobre base migrada.
**Decisões exigidas:** se a migração exigir transformação destrutiva, PARA e pede autorização.

### 4. Returns Engine Torture — P1 — CONCLUÍDA
**Resultado:** 14 testes novos em `EngineTortureTest.kt` — replay determinístico com RNG fixo, propriedade de 100 sementes (entrega nunca em quiet-hours, sem re-seleção de histórico), consent/pending/cap gates, pools vazios, limites exatos dos buckets 6–2000 dias, DST forward-gap, janelas permitidas de 1h e cruzando meia-noite, bootstrap por contagem, varredura de 10.000 avaliações horárias (0 violações). Invariants dos models documentados no cabeçalho da análise (`campaign4_analysis.md`). 76 testes no total, 0 falhas.

## NOW (campanha ativa — campanha 5)

### 5. Privacy Boundary & Android Backup — P0
**Objetivo:** confirmar que a fronteira de privacidade resiste sob pressão: backup Android (auto/ADB), `backup_rules.xml`, `android:dataExtractionRules`, `FLAG_SECURE`, clipboard, recent apps, notificações, cache, snapshots de UI.
**Plano:** auditar manifest + XMLs de backup; inspecionar `AndroidTimeReturns` (notificação), `PrivacyCover`, `FLAG_SECURE` no `FioApp`; verificar que drafts/em memória não vazam; tentar captura de tela em recents; revisar `ExportCoordinator` (arquivo temporário com modo `MODE_PRIVATE`? SAF não deixa rastro em app dir — confirmar); verificar SharedPreferences/cache de imagem.
**Critério:** zero achados P0/P1; qualquer leak listado como finding com severidade.
**Entregável:** seção em `plans/DATA-TORTURE-MATRIX.md` + commit com evidências.
**Decisões exigidas:** QUALQUER mudança de política de backup PARA e pede autorização.
