# Habit Piggy

An Android habit tracker where keeping your streaks fills a piggy bank. Every time you check off a habit, you earn money toward a savings goal: the habit's reward rate (from $0.25 to $20, $1 by default) times its current streak.

Built with Kotlin, Jetpack Compose (Material 3) and Room.

## Features

- **Habits:** create, edit, track or untrack, and check off habits once a day. A habit is daily or custom ("at least once every N days"); its streak continues as long as it's done within that window and restarts otherwise. Custom habits show when they're next due. A habit checked by mistake can be un-checked the same day, which restores its streak and takes back the money it earned.
- **Savings goals:** several goals at once, each with a target and an optional daily earnings cap. One goal is always **pinned**.
- **Home carousel:** swipe, or tap the arrows, between active goals: the pinned goal first, then the rest in your order. The habit list below follows whichever goal is on screen.
- **Goals screen:** sort by your order, A–Z or progress (tap A–Z or Progress again to reverse it), and hide completed goals. In "My order", drag goals by their handles to set the carousel order.

## How the money works

- A check-in pays the habit's **reward rate** × the streak it reaches, so a $1.25 habit on day 4 pays $5. Home shows what each habit's next check-in will pay.
- A habit pays into its assigned goal. Unassigned habits, and habits whose goal is completed or gone, pay into the pinned goal.
- A goal's **daily cap** limits the total it can receive per day across all habits. Once it's hit, completions still count toward streaks but earn nothing.
- A goal fills exactly to its target and is marked **Completed**. Anything extra overflows to the pinned goal, within that goal's own cap. If the pinned goal is the one completing, the pin moves to the first active goal in your order.
- Every deposit is logged, which is what makes the daily cap and un-checking possible.
- A goal's balance can be corrected by hand under **More options** on its edit screen, after a confirmation. This isn't logged as a deposit, so it doesn't use up the daily cap; reaching or dropping below the target completes or reopens the goal.

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

### Release builds

Release builds are signed with a key kept outside the repo. Copy `keystore.properties.example` to `keystore.properties` (it's gitignored) and fill in the passwords. Without that file, release builds are unsigned and won't install.

```bash
./gradlew :app:assembleRelease
```

The signed APK is written to `app/build/outputs/apk/release/app-release.apk`. Install it on a phone with USB debugging enabled:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

- Every update must be signed with the same key. A lost key means uninstalling, and losing the app's data, before a new build will install.
- Bump `versionCode` in `app/build.gradle.kts` for each update you install over an existing one.
- Debug and release builds share an app ID but not a signature, so a device holds one or the other. Uninstall one before installing the other.

## Project structure

| Package | Contents |
|---|---|
| `model` | Room entities: `Habit`, `SavingsGoal`, `Deposit` |
| `data` | Repository interfaces and their Room implementations |
| `data.local` | Database, DAOs and type converters |
| `viewmodel` | `HabitViewModel`, `SavingsGoalViewModel` and the earnings planner |
| `ui` | App shell, navigation and shared habit actions |
| `ui.home`, `ui.habits`, `ui.goals` | The three screens |
| `ui.components` | Reusable pieces (piggy bank card, habit card, progress bar, goal picker, dialogs) |
| `ui.theme` | Colours, typography and shapes |

## Database and migrations

- The Room database is at **version 2**. Each version's schema is exported to `app/schemas/`, and those files are committed.
- **Every schema change needs a `Migration`**: bump the version, add the migration in `data/local/` and register it on the database builder in `HabitTrackerDatabase.kt`. The database is never wiped and recreated on a version mismatch: without a migration, the app fails on launch rather than losing data.
- Each migration should have an instrumented test that uses `MigrationTestHelper` to build the old version, migrate it, and let Room validate the result against the exported schema.

## Testing

- **Unit tests** (`app/src/test`) cover the earnings rules, the ViewModel's goal logic (using a fake repository) and the list ordering. They run on your computer, with no device needed.
- **Instrumented tests** (`app/src/androidTest`) cover DAO queries, and need an emulator or device. `./gradlew connectedAndroidTest` uninstalls the app afterwards, which deletes its data. To keep your data, install and run the tests directly:

```bash
./gradlew :app:installDebug :app:installDebugAndroidTest
```

```bash
adb shell am instrument -w io.github.marshallhelenm.habitpiggy.test/androidx.test.runner.AndroidJUnitRunner
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
