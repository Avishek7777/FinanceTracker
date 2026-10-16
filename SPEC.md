# Finance Tracker — Product Spec

A simple, private, offline personal finance tracker for Android.

## Guiding principle

**Logging a transaction should take ~3 seconds.** If entry is effortless, people keep
doing it — and every other feature depends on that data existing.

## Decisions so far

| Topic | Decision |
|---|---|
| Audience | Personal use first; keep it clean enough for a later Play Store release |
| Platform | Android only, Kotlin + Jetpack Compose (Material 3) |
| Currency | User-selectable in Settings. One currency at a time; no exchange rates (yet) |
| Income | First-class, same flow as expenses (toggle at the top of the Add screen) |
| Data | Stored locally on the device (Room/SQLite). No account, no login, no server |

## MVP (v1) scope

### 1. Add / edit transaction
- Expense / Income toggle (Expense is the default).
- Big amount display driven by an on-screen number pad (no system keyboard needed).
- Category grid for the selected type; most-used categories appear first.
- Optional note; date defaults to today and can be changed with a date picker.
- Edit an existing transaction by tapping it; delete from the edit screen.

### 2. Overview (home screen)
- Month switcher (previous / next).
- This month's **Income**, **Expenses** and **Balance** (income − expenses).
- Donut chart + list of spending by category with percentages.
- Floating "+" button to add a transaction.

### 3. History
- All transactions grouped by day, newest first, with a daily total.
- Search (note or category name) and filter (All / Expense / Income).
- Swipe to delete with an **Undo** snackbar; tap to edit.

### 4. Settings
- **Currency** picker (all ISO currencies, searchable).
- **Categories**: add (name, icon, colour) and remove, for both expense and income.
  Removing a category hides it from the picker but keeps past transactions intact.
- **Export to CSV** (saved anywhere via the Android file picker).

### Default categories
- Expense: Food, Groceries, Transport, Shopping, Bills, Health, Entertainment, Other
- Income: Salary, Freelance, Gift, Other

## Data model

```
Category(id, name, iconKey, color, type: EXPENSE|INCOME, sortOrder, archived)
Transaction(id, amountMinor: Long, type, categoryId, note, date: LocalDate, createdAt)
Settings (DataStore): currencyCode
```

Money is stored as a `Long` in hundredths (e.g. 12.50 → `1250`) to avoid floating-point
rounding errors.

## Later (v2+) — only once v1 feels good

- Monthly budget (overall and per category) with progress bars
- Recurring transactions (rent, subscriptions, salary)
- Home-screen widget / app shortcut for even faster entry
- Daily reminder notification
- Backup & restore (JSON file / Google Drive)
- Multiple wallets/accounts (Cash, Bank, mobile wallets) with transfers
- Savings goals
- Multiple currencies with exchange rates

## Explicitly out of scope

Investments, bank syncing, shared/family accounts, complex budgeting systems.

## Tech stack

- Kotlin, Jetpack Compose, Material 3 (dynamic colour + dark mode)
- Room (database), DataStore (settings), Navigation Compose
- MVVM: Screen → ViewModel → Repository → DAO
- Simple manual dependency injection (`AppContainer`) — no Hilt, to keep it easy to follow
- minSdk 26 (Android 8.0), targetSdk 35
