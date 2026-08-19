# INTERACTION-SPEC-V2 — especificação de interações do Fio (v2)

Versão 2 da especificação de interações, consolidada pela Missão 3. A v1 vivia dispersa nos ADRs; a v2 as une com as decisões desta missão e passa a ser o documento de referência para qualquer future work de UI. A v1 permanece nos ADRs — a v2 não a substitui, apenas a organiza.

## Vocabulário (canônico)

Entrada, nota (singular coloquial da ReturnScreen — decisão M1 pendente), Arquivo, Excluídos recentemente, devolução, políticas temporais (Imediato; Após 1/5 minutos; Em 7/30/90/365 dias; 1 ano; Algum dia; Nunca), Guardado./Guardando…, hora silenciosa.

## Interações canônicas

| Interação | Especificação | ADR / origem |
|---|---|---|
| Escrever | Editor sem placeholder pressionante quando há conteúdo; autosave 700 ms criptografado; `basicTextField` padrão | ADR-014, FioViewModel:96 |
| Guardar | Debounce não perde texto (autosaveDraft precede insert); notice 1,5 s auto-dismiss; retry = botão Guardar re-habilitado | ADR-014, Missão 3 ciclo E |
| Apagar | 1 passo → Excluídos recentes (30 dias, recoverável); 2 passos → exclusão permanente com copy de irreversibilidade e disclaimer de non-wipe | ADR-004 |
| Devolver | Notificação sem texto da nota; ReturnScreen com carta na íntegra + data original; "Devolver para agora" / "Nunca mais" (2 passos); nunca = cancel da notificação + ENTRY_NEVER | ADR-043, NOTIFICATION-UX |
| Importar | Prévia não altera o Arquivo; 13 erros léxicos com "Nada foi importado."; commit por lote desfazível | ADR-047 |
| Exportar | 100% local (SAF); checksum SHA-256 ADR-046; copy "O arquivo escolhido ficará fora da proteção do Fio" | ADR-046 |
| Bloquear | Opcional; usa a biometria do aparelho; fallback honesto quando a autenticação some; 2 passos para desativar | ADR-013 |
| Silêncio visual | Pátina decorativa, 1 ramo de 30 dias, suprimida com Reduce Motion OU TalkBack (isMotionReduced) | ADR-045, Missão 3 G9 |

## Regras transversais (as leis da UI do Fio)

1. **Factualidade** — a UI descreve o que acontece, nunca o que significa. Copy interpretativa ("olhar de hoje", "você evoluiu") é proibida por princípio.
2. **Silêncio** — nada na UI pressiona, ranqueia ou mede. Sem streaks, sem contadores de produtividade, sem "insights".
3. **Reversibilidade por padrão** — qualquer ação destrutiva tem 2 passos; nada destrutivo acontece em 1 clique.
4. **Acessibilidade como contrato** — targets ≥48dp, liveRegion polite nos notices, contentDescription nos ícones, heading "Fio", motion suprimida sob Reduce Motion ou TalkBack.
5. **Localidade** — data/hora exibidas no fuso do aparelho (fix G1); nada na UI depende de UTC.

## O que mudou da v1 para a v2

A v2 incorpora três decisões da Missão 3: o fuso local no date picker (G1/0336cbe), o helper `isMotionReduced` combinando movimento e TalkBack (G9/b87d9f2) e a copy factual da ReturnScreen (G2/b87d9f2). Ela também registra as decisões em aberto para o fundador: M1 (nota vs entrada na ReturnScreen), D12 (teste contratual do cenário draft-sobrevive-a-falha), e os dois gates humanos de AVD (TalkBack walkthrough e contraste WCAG AA dos tokens dark).
