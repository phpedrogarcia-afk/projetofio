# Campaign 15 — Error UX

**Prioridade:** P3 · **Método:** auditoria estática de todo o caminho erro → ViewModel → UI nas quatro seções. · **Resultado:** VERDE — zero achados P0–P2; 2 observações P3/P4.

## Caminho do erro (verificado ponta a ponta)

| # | Caminho | Veredito | Evidência |
|---|---------|----------|-----------|
| 1 | `runCatching`/`onFailure`/`catch` em todas as ações do ViewModel | **Bom** — nenhum crash silencioso; toda ação de usuário passa por `onFailure(::showDataFailure)` | FioViewModel.kt:83, 87, 92, 98, 213, 235, 312 |
| 2 | `CancellationException` nunca é engolida | **Bom** — relançada em todos os pontos; cancelamento de job não vira erro falso na UI | FioViewModel.kt:127-129, FioApp.kt:317 |
| 3 | Erro recuperável: pill terracota discreta (`errorContainer` + ícone + `liveRegion Polite`) | **Bom** — visível sem alarmismo, anunciada ao TalkBack, coerente com o design Verde-Sálvia (terracota terroso, nunca vermelho puro) | FioApp.kt:326-353 |
| 4 | Erro de archive: "O Arquivo não pôde ser aberto com segurança. Nada foi apagado." | **Bom** — mensagem tranquilizadora + garantia de invariante (nada apagado) | FioApp.kt:585 |
| 5 | Import: prévia explícita com contagem de erros por issue (`UNSUPPORTED_STRUCTURE`, `MISSING_DATE`, `INVALID_DATE`, `INVALID_SIZE`, `TIME_LIMIT`) | **Bom** — cada issue tem label pt-BR dedicada | FioApp.kt:962-992 |
| 6 | Export: `StorageFailureTest` (campanha 7) já cobriu `ExportOutcome.FAILED` → UI recebe falha genérica sem detalhe interno | **Bom** — sem stack trace na tela | — |
| 7 | Ações destrutivas ("Excluir", "Excluir para sempre") em terracota com confirmação explícita | **Bom** — cor de alerta reservada para destruição | FioApp.kt:682, 859, 1067, 1241 |
| 8 | `returnError` na seção de devolução | **Bom** — erro pontual na lista de devolução | FioApp.kt:962 |

## Observações (P3/P4)

1. **Sem retry affordance no pill de erro recuperável:** o pill informa mas não oferece botão "Tentar novamente" — o usuário reexecuta a ação manualmente. Aceitável para o escopo atual (edição e arquivo reabrem sozinhos); registrar como melhoria P3.
2. **Sem log de erro persistente:** erros são mostrados e descartados; se o fundador quiser diagnosticar problemas em campo, uma fila local cifrada de eventos (ADR futuro) seria o caminho. Não é problema hoje.

## Conclusão

A UX de erros é **tranquila, acessível e coerente com a essência** (terracota em vez de vermelho, mensagens em pt-BR sem jargão técnico, garantia explícita "nada foi apagado"). Zero achados P0–P2.
