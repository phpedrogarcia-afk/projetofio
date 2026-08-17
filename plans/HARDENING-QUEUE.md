# Hardening Queue — ProjetoFio MANUS MISSION 1

**Branch de execução:** `integration/manus-rehearsal-20260817` (8be819a)
**Regra de fila:** NOW = 1 campanha ativa; NEXT = no máximo 3 em espera. Nada vai para main.
**Classificação:** P0 = perda/exposição/corrupção de dados · P1 = quebra do fluxo principal · P2 = regressão séria · P3 = qualidade · P4 = polish.
**Estados:** PLANNED → RUNNING → (FOUND | BLOCKED | DECISION REQUIRED) → FIXED → VERIFIED → DONE

---

## Fila atual

| Ordem | Campanha | Prioridade | Estado | Resultado |
|-------|----------|------------|--------|-----------|
| 1 | **Data Torture — Unicode & encoding** | P0 | PLANNED (próxima NOW) | — |
| 2 | **Crypto Pre-Review + Plaintext Hunt** | P0 | NEXT | — |
| 3 | **Database / Migration Torture (Room 2→3)** | P0 | NEXT | — |
| 4 | **Returns Engine Torture (determinismo M4)** | P1 | NEXT | — |
| 5 | **Privacy Boundary & Android Backup** | P0 | WAITING | — |
| 6 | **Time Torture (fusos extremos, DST, epoch edges)** | P1 | WAITING | — |
| 7 | **Storage Failure (SAF, disco cheio, permissões)** | P1 | WAITING | — |
| 8 | **Export Torture + Round-Trip (SHA-256 ADR-046)** | P1 | WAITING | — |
| 9 | **Dependencies audit** | P2 | WAITING | — |
| 10 | **Static quality (ktlint/detekt, lint Android)** | P3 | WAITING | — |
| 11 | **Performance (10k+ entradas, archive tipográfico)** | P2 | WAITING | — |
| 12 | **Battery / Background (notificações em Doze)** | P2 | WAITING | — |
| 13 | **Accessibility automation (TalkBack nas seções M2/M3)** | P2 | WAITING | — |
| 14 | **Visual Regression (ReturnScreen no design)** | P2 | WAITING | — |
| 15 | **Error UX** | P3 | WAITING | — |
| 16 | **Fuzz / Property-based (funções puras do engine)** | P1 | WAITING | — |
| 17 | **Large History (10k entries, GC de deletados)** | P2 | WAITING | — |

---

## NOW (campanha ativa)

### 1. Data Torture — Unicode & encoding
**Objetivo:** provar que nenhuma entrada, independente de códigopoints hostis, quebra o app, corrompe dados ou vaza texto em logs/notificações.
**Plano de ataque:**
- Testes unitários com payloads: zero-width (`U+200B`, `U+200E` RTL), combining sequences, emoji ZWJ, surrogate halves (`\uD800`), C1 controls, NUL bytes, overlong UTF-8, homoglyphs de "Nunca"/"Algum dia" (usar texto com ligaduras), 64KB de uma linha, 1000 \n no meio, RTL paragraphos misturados com LTR, timestamps com ZWSP, Markdown literal (`# *>` ``) como conteúdo de nota.
- Verificar: salvamento/edição/retorno de conteúdo intacto (round-trip binário por códigopoint), truncamento de notificação sem cortar surrogate pair, checksum SHA-256 do export inclui o conteúdo hostil sem mudança.
**Critério de DONE:** todos os payloads preservados bit a bit no round-trip Entry→Room→UI→Export; nenhuma exceção em runtime; nenhuma quebra de layout (maxLines/ellipsize).
**Decisões exigidas:** nenhuma esperada (leitura/escrita é idempotente).

## NEXT (máx. 3)

### 2. Crypto Pre-Review + Plaintext Hunt — P0
**Objetivo:** revisar `FioService`/keystore AES-256-GCM, caçar plaintext em logs, SharedPreferences, cache, backups e exceções.
**Plano:** grep por `Log.`/`printStackTrace`/`toString` de chaves; verificar `KeyStore` key aliases; verificar que o AES-GCM usa nonce aleatório por operação e não reusa; verificar onde o conteúdo em claro existe em memória; conferir `android:allowBackup` e `android:dataExtractionRules`; conferir que exceptions não logam conteúdo de Entry.
**Entregável:** `docs/security/CRYPTO-REVIEW-PACKET.md`.
**Decisões exigidas:** QUALQUER mudança no esquema criptográfico PARA e pede autorização.

### 3. Database / Migration Torture — Room 2→3 — P0
**Objetivo:** migrar schema 2→3 (M2/M3/M4: returnMode, lastReturnedAt, returnCount, importBatchId, AppSettings quietHours) com dados sintéticos realistas.
**Plano:** gerar base schema 2 com 500 entries + deletados; aplicar migração (autoMigration ou manual); verificar contagem, integridade de content, mapeamento de returnMode default (SOMEDAY), defaults de quietHours.
**Critério:** mesma contagem pré/pós; conteúdo idêntico; engine funciona sobre base migrada.
**Decisões exigidas:** se a migração exigir transformação destrutiva, PARA e pede autorização.

### 4. Returns Engine Torture — P1
**Objetivo:** provar determinismo e correção do `TimeReturnEngine` (buckets 7–29…730+, bootstrap, quiet hours, frequency cap 7 dias, `ReturnRandom` injetável).
**Plano:** testar com RNG fixo (reprodutibilidade), 10.000 simulações de horizonte 730 dias, edge cases (0 entradas, todas nunca-retornadas, todas never, quiet hours cobrindo todo o dia, cap atingido, bootstrap completo), propriedade: nunca notifica dentro de quiet hours; nunca viola cap; nunca retorna "Nunca"; monotonicidade do pool.
**Critério:** 100% das propriedades satisfeitas no RNG fixo e em 100 sementes aleatórias.
