# FIO-PXX — <título curto e acionável>

**Status:** NOW | READY | BLOCKED | DONE
**Tipo:** code | research | doc | device
**Branch:** `integration/<descrição-YYYYMMDD>`
**Evidência mínima exigida:** (o que um agente novo deve ver para acreditar)

## 1. Objetivo (2-3 frases)
O que muda no produto/código e por quê. Sem ambiguidade: uma entrega = um resultado verificável.

## 2. Contrato (o que é obrigatório)
- Comportamentos e invariantes preservados (referenciar `docs/atlas/INVARIANTS.md` por número)
- Comportamentos novos com testes de contrato (happy + adversarial)
- O que explicitamente NÃO faz (escopo negativo)

## 3. Contexto técnico (nomes reais)
Arquivos/classes/queries que serão tocados; schema version; migrations se houver; flags se houver.

## 4. Critérios de aceitação (verificáveis por script/teste)
1. `./gradlew :mobile:testDebugUnitTest --no-daemon` verde, com N total crescente ou estável
2. Testes novos do packet presentes e nomeados
3. Nenhum drift novo em `PROJECT-STATE.md` (ou drift corrigido)
4. ADR novo se mudar princípio/privacidade/produto

## 5. Riscos e portas de escape
O que pode dar errado e onde parar (kill switch do packet).

## 6. Evidence log
| Data | O que | Output |
|---|---|---|
| | | |
