# HabitTracker

Basic Kotlin Android app structure for a habit tracker.

## Build setup
- Java 21 is required for this Android Gradle Plugin version.
- Set JAVA_HOME to Homebrew OpenJDK 21 on macOS:

```bash
export JAVA_HOME=/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

- Ensure Android SDK path is configured in local.properties:

```properties
sdk.dir=/Users/el/Library/Android/sdk
```

## Gradle wrapper commands
- List available tasks:

```bash
./gradlew tasks --all
```

- Build debug variant:

```bash
./gradlew :app:assembleDebug
```

## Domain models
- `Habit`
- `SavingsGoal`

## Persistence
- Room entities are defined on the domain models.
- DAOs and database are available for habits and savings goals.
- Repository interfaces are implemented with Room-backed repositories.

## Packages
- `com.example.habittracker.model` for core entities
- `com.example.habittracker.data` for repository contracts and Room implementations
- `com.example.habittracker.viewmodel` for screen state and CRUD actions
- `com.example.habittracker.ui` for the tabbed app shell and screens
- `com.example.habittracker.ui.theme` for Compose theme setup

## Next steps
- Add edit/update flows for habits and savings goals
- Add progress tracking actions (complete habit, increment savings)



Coin icon for now:
Image by <a href="https://www.magnific.com/free-vector/golden-coin-money-dollar-icon_169504354.htm">studiogstock on Magnific</a>