# Fio — Arquitetura compartilhada de temas

Status: contrato de implementação da FIO-PV-03.

## 1. Objetivo

Sereno e Céu Noturno são duas expressões visuais do mesmo aplicativo. Toda
superfície, estado, evento e regra de negócio existe uma única vez.

```text
FioApp + superfícies únicas
        |
        v
FioTheme(theme)
        |
        +-- Material ColorScheme + Typography
        +-- FioThemeProfile (atmosfera e material)
        +-- FioBackdrop (papel Sereno ou céu Céu Noturno)
```

## 2. Modelo

- `FioVisualTheme`: identidade visual (`SERENO`, `CEU_NOTURNO`).
- `FioTheme`: resolve `ColorScheme`, `Typography` e `FioThemeProfile`.
- `FioThemeProfile`: tokens que não pertencem ao Material — tipo de fundo,
  transparência de vidro, intensidade de ornamento e estilo de título.
- `LocalFioThemeProfile`: leitura Compose local e somente visual.
- `FioBackdrop`: uma camada compartilhada atrás do conteúdo. Em Sereno desenha
  somente a cor já existente; em Céu Noturno desenha céu determinístico,
  névoa, estrelas, vinheta e observatório periférico.
- `ui/theme/cosmic/CosmicOrnaments.kt`: biblioteca Canvas semanticamente muda
  com bússola, astrolábio, órbita e observatório. É decorativa e não conhece
  dados, tela de domínio ou fluxo.
- Componentes e telas consultam tokens; não consultam regra de negócio pelo
  tema e não ramificam navegação.

## 3. Invariantes

1. Não existem `CosmicHomeScreen`, `SereneHomeScreen` ou equivalentes.
2. O tema não entra em ViewModel, banco, entidades, casos de uso ou analytics.
3. O tema não altera cópia, ação, content description ou automação de teste.
4. O estado atual de Sereno é congelado como baseline de regressão.
5. Céu Noturno usa somente APIs Compose/Material já disponíveis; nenhuma
   dependência nova.
6. O desenho atmosférico é determinístico, estático e sem coleta de dados.
7. A escolha de tema é uma preferência local, síncrona e estritamente visual.
   Ela não entra em ViewModel, Room, entidades, casos de uso, analytics ou
   backups. Instalações sem escolha anterior começam em Sereno.

## 4. Fronteiras de arquivos

| Responsabilidade | Local |
|---|---|
| cores | `ui/theme/Color.kt` |
| esquemas, perfil e seleção | `ui/theme/Theme.kt` |
| papéis tipográficos | `ui/theme/Type.kt` |
| céu/vidro compartilhados | `ui/components/FioThemeComponents.kt` |
| ornamentos cósmicos reutilizáveis | `ui/theme/cosmic/CosmicOrnaments.kt` |
| aplicação raiz | `MainActivity.kt`, `ui/FioApp.kt` |
| ajustes locais inevitáveis | arquivo da superfície existente |

## 5. Estratégia de migração

1. Congelar Sereno com testes dos tokens principais.
2. Introduzir a identidade e o perfil sem alterar o tema padrão.
3. Implementar o fundo compartilhado e validar Sereno sem delta.
4. Ativar Céu Noturno apenas na composição raiz da branch FIO-PV-03.
5. Ajustar transparências e hierarquia nas superfícies existentes, uma a uma.
6. Capturar cada estado no emulador e registrar deltas.
7. Rodar novamente a regressão do Sereno por teste, sem duplicar telas.

## 6. Política de evolução

A escolha aprovada fica em Ajustes > Aparência > Tema, com somente Sereno e Céu
Noturno. Ela é guardada em preferências privadas do aplicativo e aplicada na
raiz única. Os dois temas continuam compartilhando integralmente a mesma árvore
de UI; não existe escolha automática nem personalização adicional.
