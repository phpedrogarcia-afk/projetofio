# FIO-PV-02 — Final

## 1. Branch + HEAD

- Branch: `codex/visual-symbolic-polish-v1`
- Base validada: `codex/visual-intent-v1` em `f0587aa`
- Implementation HEAD validado antes deste relatório: `f00773bc10710c4c641f430a231b7f1e5ff5d062`

## 2. Símbolos

- `Algum dia`: infinito.
- Períodos: calendário temporal.
- Data específica: calendário de dias.
- `Nunca`: círculo cortado.
- `Pode voltar`: retorno temporal, não apenas relógio.
- Navegação: família outline coerente.
- Ajustes: cadeado, retorno, importar, exportar e excluir.
- Envelope/carta não foi usado; permanece reservado à futura cápsula.

## 3. Paleta

Papel claro mais quente, carvão escuro sem preto puro, sálvia mineral menos
crua, container de seleção dedicado e bordas mais silenciosas. Contrastes
medidos: botão claro 5,47:1; texto principal claro 13,30:1; texto principal
escuro 13,87:1.

## 4. Natureza sutil

O ramo botânico foi redesenhado como vetor Compose escalável e aparece apenas
no vazio de Encontrar/Arquivo e no rodapé informativo de Ajustes. Não há fundo
ilustrado, wallpaper ou ornamento recorrente.

## 5. Bug do calendário

Corrigido. O DatePicker Material representa o dia civil como meia-noite UTC;
a tela misturava esse valor com o fuso local. Inicialização, cabeçalho e
confirmação agora usam o mesmo contrato UTC. O cabeçalho nativo alto foi
substituído por um cabeçalho compacto do Fio, sem trocar o calendário Android.

## 6. Telas validadas

Home, time sheet, calendário com escolha real, guardar, Arquivo, nota longa,
Encontrar e Ajustes. Light, dark e font scale 1,3 foram inspecionados somente no
`emulator-5556`; o botão Guardar permanece alcançável por rolagem na fonte
grande. Nenhum aparelho físico foi usado.

## 7. Before / after

Antes: verde funcional, símbolos genéricos, navegação de pesos mistos,
calendário com cabeçalho alto e motivo botânico quase invisível em alta
densidade. Depois: vocabulário temporal próprio, sálvia/papel mais orgânicos,
ícones outline consistentes, calendário compacto e ramo silencioso escalável.
Evidências: `plans/evidence/FIO-PV-02/`.

## 8. Testes

- 134 unitários: verdes, 0 falhas/erros/ignorados.
- 30/30 instrumentados: verdes no emulador (28 baseline + calendário + nota longa).
- Regressão do calendário: célula escolhida, headline e política confirmada mantêm a mesma data.
- Nota longa: cinco parágrafos; leitura e ações Editar/Excluir alcançáveis.
- Debug: verde.
- Release: verde.
- lint Debug: verde.

## 9. Decisões não implementadas

Nota lacrada, nota temporária e cápsula por e-mail/mensagem após a morte foram
somente registradas no `plans/IDEA-INBOX.md`. Não houve feature, schema,
dependência, semântica de Search, política de Return ou navegação estrutural.

## 10. Próximas recomendações

Revisão humana das evidências e, se aprovada, push/PR separado. A instalação no
POCO deve acontecer apenas mediante pedido explícito posterior. Screenshot e
gravação continuam sendo decisão temporária do build Debug já existente.
