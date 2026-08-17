# Crypto Review Packet — ProjetoFio

**Campanha:** 2 — Crypto Pre-Review + Plaintext Hunt (MANUS MISSION 1)
**Branch revisada:** `integration/manus-rehearsal-20260817` (pós-campanha 1, commit `20b7fae`)
**Escopo:** revisão estática completa do pacote criptográfico, caça a plaintext em logs/memória/backups/superfícies do sistema e verificação das fronteiras de privacidade. **Nenhuma mudança criptográfica foi feita nesta campanha** — apenas documentação e uma correção de política de encoding (campanha 1).

---

## 1. Resumo da avaliação

O esquema criptográfico do Fio é **sólido e bem dimensionado para o escopo M1** (diário local criptografado com chave no Android Keystore). AES-256-GCM com nonce aleatório por operação, tag de autenticação de 128 bits, AAD vinculando tipo de registro + identificador + versão de schema, envelope com magic bytes e validação estrita, Keystore com `setRandomizedEncryptionRequired(true)`, backup na nuvem totalmente excluído e tráfego cleartext desativado.

Foram encontrados **zero vazamentos ativos de plaintext** em logs, notificações, backups, clipboard ou superfície de apps recentes. A superfície de ataque restante está concentrada em **memória RAM** (estado do UI e documento de export temporário), o que é inerente a qualquer app que precisa renderizar o conteúdo, e em **exportação para arquivo** (decisão consciente do produto, ADR-046).

| Área | Veredito | Severidade residual |
|------|----------|---------------------|
| Cifra + envelope (AES-256-GCM) | Correto | — |
| Gerenciamento de chave (Android Keystore) | Correto | — |
| Logs / stack traces | Sem vazamento | — |
| Notificações | Sem conteúdo (título genérico "Algo seu voltou.") | — |
| Backup na nuvem / device transfer | Totalmente excluído | — |
| Tráfego de rede | Cleartext desativado; app não faz rede | — |
| Tela de recentes / screenshots | FLAG_SECURE ativo | — |
| Clipboard | Não utilizado pelo app | — |
| Memória RAM (UI state, export) | Exposta por design | Baixa (janela curta) |
| Plaintext em exceptions | Nenhuma mensagem de erro carrega conteúdo | — |
| Encoding de entrada | **Achado P0-class já corrigido na campanha 1** | Resolvido |

---

## 2. Revisão do esquema criptográfico

### 2.1 Cifra e envelope — `AesGcmContentCipher`

A transformação `AES/GCM/NoPadding` é o padrão recomendado para dados em repouso. O IV é gerado aleatoriamente pelo próprio `Cipher.init(ENCRYPT_MODE, ...)` em cada `seal()`, e o teste `everySealUsesDifferentIv` (suíte existente, 8 testes) confirma que envelopes do mesmo conteúdo produzem ciphertexts distintos — ou seja, **não há reuso de nonce** no fluxo normal. A tag de autenticação de 128 bits é o máximo do GCM.

O envelope (`EnvelopeCodec`) é bem comportado: magic `FIO1`, versão explícita (1), tamanho do IV declarado (12–16 bytes), tamanho do ciphertext declarado e validação de que não sobram bytes (`input.available() != 0` — impede truncamento silencioso e injeção de sufixo). Truncamento, versão desconhecida e tampering são todos rejeitados com exceções tipadas (`InvalidEnvelope`, `UnsupportedVersion`, `AuthenticationFailed`), nunca com conteúdo vazio ou corrompido.

O **AAD** vincula `RecordKind + recordId + schemaVersion` com codificação length-delimited (impede ambiguidade de concatenação). Abrir um envelope com id, kind ou schema errados falha a autenticação — testado tanto na suíte existente (`wrongAssociatedDataStopsSafely`) quanto na campanha 1 (`AAD binds kind, record id and schema version to the ciphertext`). Isso protege contra reordenação de registros entre tabelas e contra replay de versões antigas de schema.

### 2.2 Gerenciamento de chave — `AndroidKeystoreKeyProvider`

A chave `fio_m1_content_v1` é gerada no `AndroidKeyStore` com `KeyGenParameterSpec`: AES-256, GCM obrigatório, sem padding, `setRandomizedEncryptionRequired(true)` (o próprio Keystore rejeita uso com IV fixo — defesa em profundidade contra o reuso de nonce). A chave nunca sai do Keystore; apenas operações criptográficas são expostas.

Um ponto de atenção já coberto pelo design: se a chave do Keystore for perdida (wipe do aparelho, troca de usuário), `loadKey()` retorna null e `hasPersistentCiphertext()` impede a geração de uma chave nova silenciosa, lançando `CryptoFailure.MissingKey` em vez de tornar os dados antigos ilegíveis sem aviso. A tela `SafeOpenFailure` informa o usuário explicitamente ("Nada foi apagado. Feche o Fio e tente novamente."). O comportamento é correto para o posicionamento M1 (local-first, sem conta).

### 2.3 O que NÃO há (e está certo)

Não há KDF manual, não há sal armazenado, não há cifra em modo ECB/CBC, não há compressão antes da cifragem (o GCM com AAD não sofre os problemas de CRIME em contexto local), e não há exportação da chave para fora do Keystore. A ausência de "forgot password"/recuperação é uma decisão de produto coerente com o local-first: **quem perde a chave perde os dados**, e o app diz isso na tela de falha.

---

## 3. Plaintext Hunt — superfícies verificadas

| Superfície | Resultado da verificação | Evidência |
|------------|--------------------------|-----------|
| `Log.*` / `printStackTrace` / `println` no código de produção | **Zero ocorrências** | grep em `app/mobile/src/main/**/*.kt` retorna vazio |
| `toString()` sobre envelopes/bytes cifrados | Único uso é no decode→String do plaintext (intencional) | `AesGcmContentCipher.kt:63` |
| Mensagens de exceção | Nenhuma exception carrega conteúdo de entrada; erros ao usuário são genéricos ("Não foi possível guardar agora…") | `FioViewModel.kt` recoverableError; `CryptoFailure` mensagens sem dados |
| Notificações de devolução | Título genérico "Algo seu voltou."; `VISIBILITY_PRIVATE`; canal `IMPORTANCE_LOW`; badge oculto | `AndroidReturnNotifications.kt` |
| Backup na nuvem (Auto Backup) | `android:allowBackup="false"` + `data_extraction_rules.xml` exclui **todos** os domínios em cloud-backup e device-transfer | `AndroidManifest.xml`, `res/xml/*.xml` |
| Device-to-device transfer | Excluído no mesmo arquivo de rules | idem |
| Tráfego de rede | `android:usesCleartextTraffic="false"` + `network-security-config` proíbe cleartext; app não declara permissões de internet e não faz requisições | manifest + `res/xml/network_security_config.xml` |
| Capturas de tela / apps recentes | `FLAG_SECURE` aplicado na `MainActivity` | `MainActivity.kt:88` |
| Clipboard | O app não usa `ClipboardManager` em lugar nenhum | grep vazio |
| SharedPreferences | Nenhuma preferência armazena conteúdo; apenas configurações estruturais | arquitetura do repository |
| Memória RAM — UI state | `draftText` e o conteúdo das entradas existem em claro na memória enquanto o app está aberto | inerente a qualquer editor |
| Memória RAM — export | O documento completo existe como `String` em memória durante a geração do export (ADR-046) e é gravado via SAF direto para o destino escolhido | `ExportCoordinator.kt` |

### 3.1 Fronteira de privacidade do export (decisão de produto, ADR-046)

O export descreve a si mesmo com honestidade: "O arquivo escolhido ficará fora da proteção do Fio." O documento gerado é plaintext por construção (Markdown/TXT legível daqui a décadas — objetivo de durabilidade). O checksum SHA-256 do corpo permite detectar adulteração posterior, mas **não é criptografia**. Essa é a única saída deliberada de plaintext do sistema e está documentada, informada ao usuário na SettingsScreen e testada (campanha 1: checksum sensível a 1 byte).

---

## 4. Achados da campanha

### Finding A — encoding de entrada com perda silenciosa (P0-class, JÁ CORRIGIDO na campanha 1)

O `seal()` anterior usava `plaintext.toByteArray(StandardCharsets.UTF_8)`, que em Kotlin/JVM substitui surrogate halves isolados por `0x3F` ("?") **antes** da cifragem — perda irreversível e silenciosa. Corrigido em `00bb186`: o encoder agora usa `CodingErrorAction.REPORT` e lança `CryptoFailure.InvalidPlaintext`. A decodificação já usava `REPORT` (strict) desde a origem. Strings Unicode válidas (incluindo emoji ZWJ, RTL, C1 controls, 64 KB) round-tripam bit a bit (campanha 1: 21 payloads hostis + 10.000 strings aleatórias).

### Finding B — draft em memória (P4, observação)

O rascunho existe em claro no `FioUiState` enquanto o app está aberto. Protegido por `FLAG_SECURE` na tela de recentes e pelo lock opcional (biometria via `DeviceAuthenticator`), mas um dump de memória do processo aberto revelaria o draft. Mitigação razoável para o escopo M1: a janela de exposição exige acesso físico ao aparelho desbloqueado com o app em primeiro plano.

### Finding C — documento de export em memória (P4, observação)

O export materializa o diário completo como uma `String` em memória antes de gravar. Para diários muito grandes isso significa um pico de memória com plaintext. O `ExportCoordinator` já falha de forma controlada (`ExportOutcome.FAILED`) e o app não faz cache do resultado. Sem ação proposta — documentado.

---

## 5. Cobertura de testes criptográficos (pós-campanhas 1+2)

A suíte `AesGcmContentCipherTest` (8 testes existentes) cobre: round-trip Unicode, unicidade de IV, estabilidade do AAD, AAD incorreto, tampering de byte, truncamento, versão inválida e chave ausente. A campanha 1 acrescentou ao `DataTortureTest`: rejeição explícita de surrogate halves, AAD por kind/id/schema no cenário integrado, preservação byte a byte de 21 payloads hostis e sensibilidade do checksum. **Nenhum teste existente foi quebrado; 60+9 testes verdes.**

---

## 6. Conclusão

O pacote criptográfico do Fio está em estado saudável para o M1 local-first. Os controles de maior risco (reuso de nonce, backup na nuvem, vazamento em logs/notificações, cleartext, screenshots) estão todos cobertos por design ou verificados por inspeção. A recomendação principal é **manter o invariant da campanha 1** (nada é cifrado após perda silenciosa de bytes) e revisar a fronteira de export quando o produto ganhar conta/sincronização — pois o plaintext exportado hoje será o elo mais fraco se uma camada de sync for adicionada no futuro.
