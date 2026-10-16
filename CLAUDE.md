# CLAUDE.md — notes for picking this project back up

Personal finance tracker Android app. Owner: Avishek. Built for personal use first, with a possible
Play Store release later, so keep the code clean and the UX simple. Product spec and roadmap are in
`SPEC.md`; setup and folder layout are in `README.md`.

## Current status (as of 2026-10-16)

- **v1 MVP code is written but has never been compiled.** It was written in a cloud session that
  couldn't reach Google Maven or the Android SDK. Only `ui/common/Money.kt` + `MoneyTest.kt` were
  compiled and tested (5/5 pass); all other files passed a Kotlin syntax check only.
- **First job on a real machine:** run Gradle sync + `./gradlew assembleDebug test` in Android Studio
  and fix whatever compile errors come up (likely small API/import mismatches), then run it on a
  device and click through every screen.
- Room schema export is enabled (`app/schemas/`). Commit the generated schema JSON after the first
  successful build so future migrations have a baseline.

## About the owner

- Some Kotlin/Java background, no Flutter. Explain Android-specific concepts (Compose, ViewModel,
  Room, Flow) briefly when introducing new ones; avoid heavy abstractions.
- Will use Android Studio (possibly with the Claude Code JetBrains plugin). Often checks in from
  a phone, so keep summaries short.

## Decisions already made (don't relitigate without reason)

- Kotlin + Jetpack Compose + Material 3, minSdk 26, targetSdk 35.
- Room for data, DataStore for settings, Navigation Compose with string routes.
- **No Hilt.** Manual DI via `AppContainer` in `FinanceApp.kt`; ViewModels get it through
  `CreationExtras.appContainer()` in their `Factory`.
- **Money is a `Long` in hundredths** (`amountMinor`, 12.50 → 1250), always positive; `type`
  (EXPENSE/INCOME) gives direction. Never use Float/Double for money.
- **One user-selectable currency** (Settings → stored in DataStore). No exchange rates yet.
  Changing the currency only changes the display; stored numbers aren't converted.
- Income is first-class and uses the same add flow as expenses.
- Categories are **archived, never deleted** (FK from transactions is RESTRICT).
- Default categories are seeded in `AppDatabase` `SeedCallback.onCreate`.
- Fully offline: no INTERNET permission, no accounts.

## Code conventions

- Each feature is `ui/<feature>/<Name>Screen.kt` + `<Name>ViewModel.kt`. The ViewModel exposes one
  `uiState: StateFlow<...>` built with `combine(...).stateIn(WhileSubscribed(5_000))`, and the screen
  collects it with `collectAsStateWithLifecycle()`.
- Repositories (`data/`) are the only thing that touch DAOs.
- Category icons are stored by string key → `ui/common/CategoryIcons.kt`. Add new icons there; never
  rename existing keys (they're in users' databases).
- Pure logic (formatting, parsing, CSV) lives outside Android classes so it can be unit tested.
- Dependency versions live in `gradle/libs.versions.toml`.

## Known rough edges / ideas noted while building

- Editing a transaction whose category has been archived: Save stays disabled until a different
  category is picked (`AddEditUiState.canSave`).
- Overview's "next month" button is disabled for future months, so future-dated transactions are
  only visible in History.
- History's undo snackbar is tied to the screen's coroutine scope; navigating away drops the undo.
- `TabRow` may show a deprecation warning (newer API is `PrimaryTabRow`).
- No app/widget shortcut yet, even though fast entry is the core goal; it's high on the v2 list.

## Next steps (suggested order)

1. Get it compiling and running; fix bugs found while using it for a few days.
2. Optional: GitHub Actions workflow that builds a debug APK on push (lets the owner install from
   their phone and catches compile errors in the cloud).
3. v2 candidates from `SPEC.md`, in rough priority: monthly budget, recurring transactions,
   home-screen widget/shortcut, backup & restore, multiple wallets.

## Commands

- Build: `./gradlew assembleDebug`
- Unit tests: `./gradlew test`
- Install on a connected phone: `./gradlew installDebug`
