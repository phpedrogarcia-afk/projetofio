# FIO-PV-01 — Acabamento de Intenção / Visual North Star v1

Status: COMPLETE

## Objective

Adaptar com alta fidelidade a intenção da imagem norte para o aplicativo
Android real, preservando comportamento, arquitetura, acessibilidade,
privacidade e padrões nativos maduros.

## Scope

- Home, bottom navigation, escolha de tempo e calendário.
- Encontrar, Arquivo, detalhe da nota e Ajustes/export.
- Tokens visuais compartilhados, light/dark e fonte grande.
- Evidência before/after exclusivamente no emulador.

## Out of scope

- Features, schema, Search semantics, Return policy, dependências externas,
  fontes novas, crypto e mudanças de segurança.

## Read first

- `AI-START-HERE.md`
- `app/mobile/src/main/java/com/projetofio/app/ui/AGENTS.md`
- `docs/03-UX.md`
- `docs/atlas/UX-SURFACE-MAP.md`
- `docs/design/DESIGN-TOKENS.md`

## Expected files

- `app/mobile/src/main/java/com/projetofio/app/ui/**`
- tema/tokens e testes de UI estritamente relacionados
- `plans/FIO-PV-01-VISUAL-DELTA.md`
- `FIO-PV-01-FINAL.md`

## Validation

Executar ciclos A–G no emulador, testes locais por superfície e, ao final,
baseline unitário/instrumentado atual, Debug, Release e lint.

## Stop conditions

Parar antes de feature, schema, navegação estrutural, semântica de busca,
política de Returns, dependência externa, fonte ou segurança.
