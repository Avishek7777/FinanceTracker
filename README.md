# Finance Tracker

A simple, private, offline personal finance tracker for Android — log an expense or income in
about three seconds, see where your money goes each month.

See [SPEC.md](SPEC.md) for the product spec and roadmap.

## Features (v1)

- **Quick add**: number pad, Expense/Income toggle, category grid (most-used first), optional note and date
- **Overview**: monthly income, expenses and balance, plus a spending-by-category donut chart
- **History**: grouped by day, search, filter by type, swipe to delete with undo, tap to edit
- **Settings**: choose any currency, manage categories, export everything to CSV

All data stays on your phone. No account, no internet permission.

## Build & run

1. Install a recent [Android Studio](https://developer.android.com/studio) (Ladybug or newer).
2. **File → Open** and choose this folder. Let Gradle sync (first sync downloads dependencies).
3. Plug in a phone with USB debugging enabled, or create an emulator (Device Manager), then press **Run ▶**.

From a terminal: `./gradlew assembleDebug` (APK ends up in `app/build/outputs/apk/debug/`)
and `./gradlew test` for unit tests.

## Project layout

```
app/src/main/java/com/avishek/financetracker/
├── FinanceApp.kt / MainActivity.kt   App entry point and AppContainer (manual DI)
├── data/                             Room entities, DAOs, database, repositories, DataStore settings
└── ui/
    ├── FinanceNavHost.kt             Navigation + bottom tabs
    ├── add/                          Add / edit transaction screen
    ├── overview/                     Monthly summary + donut chart
    ├── history/                      Transaction list
    ├── settings/                     Settings, currency picker, categories, CSV export
    ├── common/                       Money formatting, category icons, helpers
    └── theme/                        Material 3 theme
```

Each screen follows the same pattern: a `Screen` composable observes `uiState` from its
`ViewModel`, which reads from a repository, which talks to Room DAOs.
