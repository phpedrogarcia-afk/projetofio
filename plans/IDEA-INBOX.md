# IDEA-INBOX — ProjetoFio

Regra: ideias descobertas durante as missões, **nenhuma implementada**. Cada entrada permanece aqui até o fundador promover, rejeitar ou deixar evaporar.

| # | Ideia | Problema que endereça | Valor | Risco | Princípio afetado | Estágio futuro |
|---|-------|----------------------|-------|-------|-------------------|----------------|
| 1 | ktlint + detekt no CI como gate incremental | Manter o padrão de qualidade estabilizado nas Missões 1–2 | Médio (evita drift de estilo em commits futuros) | Baixo se incremental; alto se virar reformatação em massa | 10 (simplicidade) | Depois do merge do PR #1; configurar apenas sobre commits novos |
| 2 | Paparazzi / screenshot regression (ReturnScreen primeiro) | Regressão visual automática da superfície emocional mais importante | Alto | Médio (fragilidade de snapshots, manutenção) | 8 (beleza não compete) | Opcional pós-merge; ordem ReturnScreen → Home → Archive → TimePicker |
| 3 | Retry affordance no pill de erro | Falha de save pode exigir navegar ao menu para entender o que houve | Médio | Baixo se contrato for mantido (pill não vira recompensa) | 4 (silêncio) | Testar no piloto; se usuários relatam confusão, botão "Tentar novamente" discreto |
| 4 | Boot receiver opcional pós-reboot | Devoluções dependem de WorkManager re-agendado (hoje a reconciliação contínua compensa) | Baixo | Baixo; adiciona superfície sem evidência de problema | 7 (local-first) | Só se o piloto reportar devoluções perdidas após reboot |
| 5 | `androidx.credentials` substituindo `biometric:1.1.0` legado | Dependência antiga; Credentials Manager é o caminho moderno | Médio | Médio (refator do app-lock, gate biometric real) | 6 (privado por arquitetura) | Quando autenticação v2 for planejada pelo fundador |
| 6 | Remoção do artefato `fragment` | Limpeza de dependência sem uso direto | Muito baixo | Nenhum | 10 | Qualquer janela de limpeza futura; cosmético |
| 7 | Conteúdo só-zero-width: aceitar ou rejeitar no `isBlank()` | Strings invisíveis passam na validação atual | Baixo | Baixo | 1 (palavras dominam) | Decisão de produto do fundador; nada implementado até lá |
| 8 | Schema 4 com campo `returnPolicy` em Entry | Persistir política temporal como dado, não só UI-state | Médio | Médio (migration + tortura) | 3 (tempo é feature) | Reavaliar quando engine e Archive amadurecerem; trade-off UI-only atual é aceito (DECISION REQUIRED levíssima) |
| 9 | Sugestão de teclado do sistema nos TextFields | Digitação mais nativa | Baixo | Nenhum | 8 | Trade-off documentado; decidir no piloto se incomodar alguém |
