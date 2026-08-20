# FIO-PV-03 — Final

## 1. Branch + HEAD

- Branch: `codex/visual-cosmic-theme-v1`
- Base: `codex/visual-symbolic-polish-v1` em `8eba133`
- Implementation HEAD validado antes deste relatório:
  `567605c26a92ffde540bd5ec3f0fbb0e5ac6b10a`
- Merge: não realizado.

## 2. Especificação e arquitetura

- `FIO-COSMIC-THEME-SPEC.md` fixa a referência, a paleta, o vidro, a
  simbologia, a atmosfera e a fronteira `MATCH / ADAPT / IGNORE`.
- `FIO-THEME-ARCHITECTURE.md` fixa uma única árvore de superfícies.
- `FioVisualTheme`, `FioThemeProfile` e `LocalFioThemeProfile` resolvem tokens
  visuais sem alcançar ViewModel, banco ou domínio.
- `FioBackdrop` desenha um céu Compose determinístico e estático atrás das
  mesmas telas. Não há dependência, bitmap por tela ou animação contínua.
- Céu Noturno está ativado explicitamente na raiz desta branch para validação;
  não existe preferência, seletor ou persistência de tema.

## 3. Sereno preservado

Nenhuma cor, tipografia, espaço ou raio do Sereno foi alterado. O tema continua
com seus modos claro e escuro e usa exatamente os mesmos composables. Três
testes unitários novos congelam os principais tokens claros, escuros e cósmicos.

## 4. Linguagem Céu Noturno

- abismo azul-petróleo e carvão azul;
- névoas, estrelas, constelação e órbitas discretas;
- vidro escuro de 92–96% com contornos minerais/dourados;
- creme quente para palavras e ouro envelhecido para tempo, ação e seleção;
- Fraunces na marca e nos títulos de tela; Inter em seções, controles,
  metadados e palavras da pessoa;
- símbolos temporais e navegação outline preservados;
- barras do Android integradas à atmosfera somente neste tema.

## 5. Superfícies validadas

Home vazia e preenchida, folha de tempo, calendário, confirmação existente,
Encontrar vazio e com resultado, Arquivo, nota aberta, Ajustes, barras do
sistema e navegação inferior. A fonte 130% foi inspecionada em Home e Ajustes;
o botão Guardar permanece integralmente alcançável por rolagem.

## 6. Loop visual

O ciclo `captura → comparação → delta → correção` detectou e fechou quatro
deltas relevantes:

1. barras claras quebrando a atmosfera;
2. vidro da folha de tempo transparente demais;
3. calendário elevado claro/plano demais;
4. texto raiz preto e linhas planas em Encontrar/Arquivo.

Os pares e as capturas finais estão em `plans/evidence/FIO-PV-03/`. O mapa
explicativo está em `FIO-PV-03-DELTA.md`.

## 7. Testes e builds

- 137 unitários: verdes; 0 falhas, 0 erros, 0 ignorados.
- 30/30 instrumentados: verdes no `emulator-5556`.
- Regressão de calendário: verde.
- Nota longa e ações alcançáveis: verde.
- Debug: verde.
- Release: verde.
- lint Debug: verde.
- APK principal e APK de testes foram instalados explicitamente somente no
  `emulator-5556`; o POCO conectado não foi usado.

## 8. Funcionalidade e dados

Não houve feature, schema, migration, dependência, semântica de Search, política
de Return, crypto, rede, analytics, cópia de produto ou navegação estrutural.
Todo conteúdo usado nas capturas é sintético.

## 9. Itens do mockup não implementados

Conta/perfil, seletor de tema, lembretes, sugestões/chips de busca, temas
inferidos, abas do Arquivo, prazo de cinco anos, hora exata, favoritos,
compartilhamento e nova tela Guardado. Permanecem fora do escopo.

## 10. Estado de entrega

Implementação e validação locais concluídas. Não houve merge, push, Pull
Request ou instalação no aparelho físico. Próxima ação segura: revisão humana
das evidências e autorização separada caso se deseje publicar a branch.
