# Multi-Choice

Multi-Choice is an Android app built with Kotlin and Jetpack Compose for creating and studying multiple choice questions by section.  
It is designed for interview prep and topic based study with offline support.

## Overview

The app lets users:

- Create sections (for example Kotlin, Android, System Design)
- Add multiple choice questions to each section
- Study questions with randomized answer order
- Track correct answers for the current section session
- Store and update an all time high score per section
- Persist all data locally so the app works offline
- Start with seeded question banks from a local JSON file

## Core Features

### Section Management
- Create new study sections
- View all existing sections on the home page
- Open a section to study and add questions

### Question Authoring
- Add custom questions per section
- Add 4 answer options for each question
- Mark one option as the correct answer

### Study Experience
- Questions shown per section
- Answer options are shuffled when displayed so correct answers are not always in the same position
- Immediate feedback after answering:
  - Correct with green check icon
  - Incorrect with red X icon
- Next Question navigation inside the section

### Scoring
- **Session Correct Count**: Resets when opening a section and tracks correct first attempts during that session
- **All Time High Score**: Stored in the database per section and updated when a session beats the previous high

### Local Offline Data
- Room database (SQLite) for persistent local storage
- No network dependency for core app usage
- Seed data loaded from `assets/seed_questions.json` on first run when database is empty

### UI and Theming
- Jetpack Compose based UI
- Multi page navigation with Navigation Compose
- Global dark theme inspired by GitHub background style
- Purple action buttons and white text styling

## Tech Stack

- Kotlin
- Android SDK
- Jetpack Compose
- Navigation Compose
- Android Architecture Components (ViewModel, StateFlow)
- Room (SQLite) with KSP
- Coroutines

## Project Structure

```text
app/
  src/main/java/com/multichoice/app/
    MainActivity.kt
    data/
      Models.kt
      QuestionRepository.kt
      SeedFileReader.kt
      db/
        AppDatabase.kt
        Entities.kt
        MultiChoiceDao.kt
        Relations.kt
    ui/
      AppViewModel.kt
      MultiChoiceApp.kt
      theme/
        Theme.kt
  src/main/assets/
    seed_questions.json
```

## Data Model

### Section
- `id`
- `title`
- `description`
- `highScore`
- `questions[]`

### Question
- `id`
- `prompt`
- `options[]`

### Option
- `text`
- `isCorrect`

## Getting Started

### Requirements
- Android Studio (recent stable version)
- JDK 17
- Android SDK (compileSdk 35)
- Gradle wrapper from project

### Run
1. Open project in Android Studio
2. Sync Gradle
3. Run on emulator or physical device

Or from terminal:

```powershell
.\gradlew.bat clean :app:assembleDebug
```

## Seed Data

Seed data file:

- `app/src/main/assets/seed_questions.json`

Behavior:

- Seeds only when the database has zero sections
- If database already has data, seed does not re-run

To apply new seed content during development:

1. Uninstall app from emulator/device  
or  
2. Clear app data for the app

## Troubleshooting

### Build fails with locked `R.jar` on Windows
Symptom:
- `The process cannot access the file ... R.jar`

Fix:
1. Stop running app and close Compose Preview
2. Run:

```powershell
.\gradlew.bat --stop
Remove-Item -Recurse -Force .\app\build
.\gradlew.bat :app:assembleDebug --stacktrace
```

3. If still locked, close Android Studio and retry command

### App crashes on startup after schema changes
Use destructive migration in development or clear app data if schema changed without migration.

## Roadmap Ideas

- Timed quiz mode
- Per question explanations
- Import and export sections
- Search and filter across sections
- Section stats history over time
- Better preview only UI composables for design workflow

## License

Add your preferred license here.
