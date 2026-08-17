# ANALYTICS-PRIVACY-MATRIX

Cada evento do piloto é avaliado campo a campo. Regra mestre: **nenhum campo que contenha ou revele conteúdo autobiográfico entra no registro.** Eventos locais e exportáveis (mesma fronteira ADR-046 do export de conteúdo); sem servidor, sem nuvem, sem identificador de dispositivo transmitido. Retenção padrão: até 2 anos após o fim do piloto, depois destruição documentada — ajustável na revisão legal.

| Evento | Campos | Justificativa | Retenção | Privacidade | Necessário? |
|--------|--------|----------------|----------|-------------|-------------|
| `entry_saved` | `created_at` (ms), `return_policy` (enum) | Prova que a escrita ocorreu e com qual política temporal — insumo para o denominador do MUR | 2 anos pós-piloto | Nenhum conteúdo; política é escolha do usuário, não o texto | **Sim** |
| `return_scheduled` | `scheduled_at` (ms), `entry_id` (hash interno) | Demonstra o mecanismo operando; sem texto nem timestamp de escrita | 2 anos pós-piloto | Só referencia a entrada por ID opaco | **Sim** |
| `return_candidate_selected` | `selected_at` (ms), `entry_age_days` (inteiro) | Evidência da seleção temporal (idade da entrada) — o coração da hipótese | 2 anos pós-piloto | Idade é estatística, não conteúdo | **Sim** |
| `return_delivered` | `delivered_at` (ms), `delivery_type` (organic/anchored/explicit) | Distingue devolução espontânea de agendada — relevante para a hipótese central | 2 anos pós-piloto | Sem payload de conteúdo | **Sim** |
| `return_opened` | `opened_at` (ms), `entry_id` | Prova a leitura da devolução — numerador qualitativo | 2 anos pós-piloto | Sem conteúdo | **Sim** |
| `return_feedback_yes` | `answered_at` (ms) | Resposta "Sim" da pergunta de ressonância | 2 anos pós-piloto | Sem texto de justificativa (botão único, sem campo aberto) | **Sim** |
| `return_feedback_no` | `answered_at` (ms) | Resposta "Não" | 2 anos pós-piloto | Sem justificativa | **Sim** |
| `return_suppressed` | `suppressed_at` (ms), `suppress_method` (dismiss/never_show) | Mede a autonomia exercida (princípio 5) sem revelar o quê | 2 anos pós-piloto | Método, nunca conteúdo | **Sim** |
| `return_never_enabled` | `enabled_at` (ms) | Usuários que optaram por Nunca — o app deve aprender com eles também | 2 anos pós-piloto | Escolha, não conteúdo | **Sim** |
| `return_rest_started` | `rest_at` (ms) | Uso do Descansar — sinal de controle, não de desengajamento | 2 anos pós-piloto | Escolha, não conteúdo | **Sim** |

## Campos explicitamente excluídos

| Campo tentador | Decisão | Motivo |
|----------------|---------|--------|
| Texto da entrada (qualquer forma) | **Proibido** | Viola princípios 1, 6 e 12 |
| `content_length` | Excluído | Proxy de conteúdo; pode induzir otimizá-lo |
| Embeddings / similaridade semântica | **Proibido** | Sem model v0 (ADR-041); violaria a fronteira semântica privada |
| Timestamp de escrita original como campo separado | Excluído | `entry_age_days` cobre a necessidade analítica sem expor a data exata da vida da pessoa |
| GPS / localização | **Proibido** | Fora da tese; privacidade por arquitetura |
| ID de instalação persistente transmitido | **Proibido** | Sem transmissão alguma; coletas são locais |
| `session_duration`, `sessions_per_day` | **Proibidos como eventos** | Anti-métricas (seção 5 do protocolo) |
| `return_missed` (entrou na tela e não abriu) | Excluído | Mediria vigilância do usuário; o retorno deve continuar quieto |
| Contagem acumulada de saves/devoluções no log | Excluído | Somável a partir dos eventos; duplicar criaria incentivo de otimização |

## Transporte

O log é um arquivo local (`pilot_events.ndjson`, anexado ao export ADR-046, checksum SHA-256 incluso). Entrega ao pesquisador ocorre presencialmente no momento da entrevista, com cópia destruída do dispositivo se o participante preferir (princípio 11: partida sem custo).
