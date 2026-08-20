# FIO-P18 — funções básicas visíveis e Ajustes em linguagem humana

**Status:** DONE — engineering gates passed; founder comprehension observation remains external
**Tipo:** code + UX audit
**Branch:** `feature/fio-primary-navigation-20260820`
**Plano:** `plans/2026-08-20-note-basics-settings-clarity.md`

## Contrato

- Tornar editar/excluir/recuperar encontráveis sem transformar Arquivo em feed.
- Separar Ajustes por intenção humana, com visão geral e páginas focadas.
- Remover termos internos da interface do usuário.
- Preservar schema 3, criptografia, autenticação, Returns e Import existentes.
- Registrar funcionalidades comuns deliberadamente ausentes em vez de
  adicioná-las por imitação de outro aplicativo.
- Não declarar o seletor temporal funcional além da evidência atual.

## Gate

134 testes unitários e todos os instrumentados verdes em API 26; inspeção da
hierarquia de UI; atualização no Poco preservando dados.

## Evidence log

| Data | Evidência | Resultado |
|---|---|---|
| 2026-08-20 | Build debug + validation + AndroidTest | sucesso |
| 2026-08-20 | Testes unitários | 134/134, 0 falhas |
| 2026-08-20 | Instrumentados API 26 | 28/28, incluindo editar/excluir/recuperar e Voltar |
| 2026-08-20 | Hierarquia UI, build normal | Ajustes/Proteção/Exportar/Excluídos; nenhuma cópia M1/validação |
| 2026-08-20 | Hierarquia UI, build validation | Lembranças que voltam e Importar em páginas focadas com explicação antes dos controles |
| 2026-08-20 | Poco M3 Pro, primeira tentativa | aparelho desconectado; condição superada mais tarde no mesmo dia |
| 2026-08-20 | Poco M3 Pro/API 33 | APK P18 instalado com `install -r`, dados preservados; MainActivity em primeiro plano; nenhum fatal recente |
| 2026-08-20 | APK P18 | 15.596.621 bytes; SHA-256 `28837ED4792B3788D34F655CC185B28B1EEDE2BF27423F6192A05561EFFC2110` |
| 2026-08-20 | Revisão final de linguagem | “Lembranças que voltam”, importação sem “lote”, orientação TXT/Markdown e exclusão em linguagem humana; 134 unitários + 28 instrumentados verdes |
| 2026-08-20 | APK P18 revisado no Poco | 15.602.960 bytes; SHA-256 final `52E7A8C289D4B5333E23860CFA353EB17997B2236A452A5367C92A13A44B4D85`; instalação preservou dados |
| 2026-08-20 | Ação Never Return | rótulo explícito + confirmação explica permanência no Arquivo e ausência de reversão; 134 unitários + 28 instrumentados verdes em estado limpo |

O gate do packet comprova instalação e comportamento técnico. A afirmação
“qualquer usuário entende Ajustes” exige observação humana e não é inferida dos
testes; o fundador fará essa verificação sem reabrir o gate de engenharia.
