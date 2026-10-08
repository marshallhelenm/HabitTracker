# Habit Piggy

An Android habit tracker where keeping your streaks fills a piggy bank. Every time you check off a habit, you earn money toward a savings goal: $1 for each day of the habit's current streak.

Built with Kotlin, Jetpack Compose (Material 3) and Room.

## Features

- **Habits:** create, edit, track or untrack, and check off habits once a day. Streaks continue when a habit was done yesterday and restart otherwise. A habit checked by mistake can be un-checked the same day, which restores its streak and takes back the money it earned.
- **Savings goals:** several goals at once, each with a target and an optional daily earnings cap. One goal is always **pinned**.
- **Home carousel:** swipe, or tap the arrows, between active goals with the pinned goal first. The habit list below follows whichever goal is on screen.
- **Goals screen:** sort by pinned first, A–Z or progress (tap A–Z or Progress again to reverse it), and hide completed goals.

## How the money works

- A habit pays into its assigned goal. Unassigned habits, and habits whose goal is completed or gone, pay into the pinned goal.
- A goal's **daily cap** limits the total it can receive per day across all habits. Once it's hit, completions still count toward streaks but earn nothing.
- A goal fills exactly to its target and is marked **Completed**. Anything extra overflows to the pinned goal, within that goal's own cap. If the pinned goal is the one completing, the pin moves to the oldest active goal.
- Every deposit is logged, which is what makes the daily cap and un-checking possible.

The rules live in `viewmodel/EarningsPlanner.kt` (a pure function, `planEarnings`) and are applied in `SavingsGoalViewModel.recordEarningsForHabitCompletion`.

## Build setup

- A JDK, version 21 or newer, with `JAVA_HOME` pointing at it. For example, with Homebrew on macOS:

```bash
export JAVA_HOME=/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

- The Android SDK path set in `local.properties`:

```properties
sdk.dir=/Users/el/Library/Android/sdk
```

- The app targets Android 15 (SDK 35) and supports Android 7.0 (SDK 24) and up.

### Common commands

Build and install on a connected device or emulator:

```bash
./gradlew :app:installDebug
```

Run the unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Run lint:

```bash
./gradlew :app:lint
```

Builds print long `D8: An error occurred when parsing kotlin metadata` warnings. These come from Kotlin 2.4 being newer than the bundled Android build tools. They're only warnings, and the build still succeeds.

## Project structure

| Package | Contents |
|---|---|
| `model` | Room entities: `Habit`, `SavingsGoal`, `Deposit` |
| `data` | Repository interfaces and their Room implementations |
| `data.local` | Database, DAOs, type converters and migrations |
| `viewmodel` | `HabitViewModel`, `SavingsGoalViewModel` and the earnings planner |
| `ui` | App shell, navigation and shared habit actions |
| `ui.home`, `ui.habits`, `ui.goals` | The three screens |
| `ui.components` | Reusable pieces (piggy bank card, habit card, progress bar, goal picker, dialogs) |
| `ui.theme` | Colours, typography and shapes |

## Database and migrations

- The Room database is at **version 5**. Each version's schema is exported to `app/schemas/`, and those files are committed.
- **Every schema change needs a `Migration`** in `data/local/Migrations.kt`, added to the database builder in `HabitTrackerDatabase.kt`. The database is never wiped and recreated on a version mismatch: without a migration, the app fails on launch rather than losing data.
- Migration tests in `app/src/androidTest/.../MigrationTest.kt` build each old version, migrate it, and let Room validate the result against the exported schema.

## Testing

- **Unit tests** (`app/src/test`) cover the earnings rules, the ViewModel's goal logic (using a fake repository) and the list ordering. They run on your computer, with no device needed.
- **Instrumented tests** (`app/src/androidTest`) cover migrations and DAO queries, and need an emulator or device. `./gradlew connectedAndroidTest` uninstalls the app afterwards, which deletes its data. To keep your data, install and run the tests directly:

```bash
./gradlew :app:installDebug :app:installDebugAndroidTest
```

```bash
adb shell am instrument -w com.example.habittracker.test/androidx.test.runner.AndroidJUnitRunner
```

## Test data

`scripts/seed-emulator.sh` replaces the app's data on a connected emulator with the test data in `scripts/seed.sql`. It backs up the current database to `build/db-backups/` first, and refuses to run if the database version doesn't match the seed. The test data covers:

- goals at different fill levels, including one past its target and one archived;
- assigned, unassigned and untracked habits;
- habits already done today;
- scenarios that trigger the daily cap and goal completion.

Dates are relative to today, so the data stays valid whenever you run it. The app must be installed and opened at least once first.

```bash
scripts/seed-emulator.sh
```

## Roadmap

Planned work is tracked in [ROADMAP.md](ROADMAP.md).

## Artwork

The piggy bank and coin artwork, including the app icon and launch screen, are original drawings made for this project.
