# UI-local instructions

Read only the surface file being changed plus `FioViewModel.kt` when its state
contract is involved. Surface routing:

- Guardar/time picker: `home/HomeScreen.kt`
- Encontrar: `search/SearchScreen.kt`
- Arquivo: `archive/ArchiveScreen.kt`
- note read/edit/delete: `entry/EntryDetailScreen.kt`
- Return: `returns/ReturnScreen.kt`
- Ajustes: `settings/SettingsScreen.kt`
- privacy/lock failures: `security/PrivacyScreens.kt`
- shared motif/date helpers: `components/FioComponents.kt`
- root orchestration/navigation only: `FioApp.kt`

Do not change copy, appearance, navigation, product behavior or privacy while
performing a structural refactor. Do not move independent surfaces back into
`FioApp.kt`. Use `docs/atlas/UX-SURFACE-MAP.md` only for cross-surface work.

Run the smallest surface contract first, then follow `docs/TEST-LEVELS.md`.
