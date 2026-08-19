# KNOWLEDGE-GAPS — o que ninguém sabe ainda (e o que faria saber)

**Regra da missão:** gap ≠ defeito. Gap é ausência de evidência. Cada linha diz o que falta, por que importa, e qual packet/evidência o fecha. Nunca reportar gap como "coberto".

## 1. Gaps de hardware/aparelho (E5 — exigem aparelho real)

| Gap | Por que importa | Fecha com |
|---|---|---|
| G-01. Crypto em Keystore real (AndroidKeystorePersistenceTest, EncryptedPersistenceTest) | toda a confiança do produto passa por aqui; nunca executado | device gate + `connectedDebugAndroidTest` |
| G-02. PrivacyCover em aparelho (PrivacySurfacesTest) | proteção contra app switcher é claim central de privacidade | device gate |
| G-03. Performance do motor em aparelho antigo (minSdk 26) | bootstrap/cap/evaluate são baratos, mas Room+decrypt em loop (busca futura) não medido | FIO-P07 scale |
| G-04. WorkManager/biometria em OEMs (Samsung/Xiaomi/Motorola) | customizações quebram notificação e biometria com frequência | device matrix (ADR-036) + teste físico |
| G-05. Notification preview em OEMs com lockscreen customizado | risco R2: preview expõe título/canal | teste físico multi-OEM |
| G-06. RAM real do app com EmbeddingGemma (ΔRSS, não RSS do modelo) | kill criterion depende disso | FIO-P12 |
| G-07. TalkBack real + navegação completa | accessibility claims sem evidência humana | teste humano |
| G-08. DST real na janela de devolução (spring-forward 2h30 notificação) | java.time deve resolver, mas ninguém testou | device gate |

## 2. Gaps de escala (E5 — exigem dados reais)

| Gap | Por que importa | Fecha com |
|---|---|---|
| G-09. Busca lexical com 1k/10k entries | Option A é scan completo; latência e memória não medidas além de 60 entries sintéticas | FIO-P07 |
| G-10. Import de arquivo grande (preview OOM) | staging é em memória | FIO-P11 (import hardening) |
| G-11. Return history com centenas de ciclos | grows linear; query loadReturnHistory sem limite | medir + decidir (cap histórico? archive de history?) |
| G-12. Migration 3→4 futura | nunca houve migration em produção; processo testado mas sem precedente real | primeira migration real (ADR de schema) |

## 3. Gaps de validação humana

| Gap | Por que importa | Fecha com |
|---|---|---|
| G-13. Piloto (pilot/ pronto, não iniciado) | a tese central (tempo como core, consentimento) é hipótese | protocolo em `pilot/PILOT-PROTOCOL.md` |
| G-14. "Já voltou" como experiência (badge no Arquivo/busca) | produto novo sem feedback real | piloto + feedback ADR-012 |
| G-15. Essência percebida pelo fundador (UI real em uso diário) | "grandioso sem perder a essência" é julgamento qualitativo | uso diário + DESIGN-SYSTEM audit (doc 01-04) |

## 4. Gaps de documentação (E2/E3 — docs não batem com código em pontos)

Ver `DOC-DRIFT-AUDIT.md` para a lista completa item a item.

## 5. Gaps deliberados (não fechar)

G-16. Analytics remoto: gap de conhecimento é deliberado até D-2. G-17. Sync/multi-device: gap deliberado (ADR-025). G-18. iOS: fora de escopo (ADR-033). G-19. LLM conversacional: nunca fechar (proibido).
