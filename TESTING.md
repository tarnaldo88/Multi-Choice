# Testing Guide for Multi-Choice App

This document provides an overview of the testing setup and how to run tests for the Multi-Choice Android application.

## Test Structure

The project includes comprehensive tests organized into the following categories:

### Unit Tests (`src/test/`)
- **Data Models Tests** (`data/ModelsTest.kt`): Tests for data classes like `Section`, `Question`, and `ChoiceOption`
- **Repository Tests** (`data/QuestionRepositoryTest.kt`): Tests for the `QuestionRepository` business logic
- **ViewModel Tests** (`ui/AppViewModelTest.kt`): Tests for the `AppViewModel` state management
- **Database Tests** (`data/db/AppDatabaseTest.kt`): Tests for Room database operations

### Instrumentation Tests (`src/androidTest/`)
- **UI Tests** (`ui/MultiChoiceAppUiTest.kt`): Tests for Compose UI components and user interactions

## Running Tests

### Unit Tests
```bash
# Run all unit tests
.\gradlew.bat test

# Run specific test class
.\gradlew.bat test --tests "*ModelsTest"
```

### Instrumentation Tests
```bash
# Run all instrumentation tests
.\gradlew.bat connectedAndroidTest

# Run specific UI test
.\gradlew.bat connectedAndroidTest --tests "*MultiChoiceAppUiTest"
```

### All Tests
```bash
# Run both unit and instrumentation tests
.\gradlew.bat check
```

## Test Coverage

### Data Layer
- ✅ Model validation and calculations
- ✅ Repository CRUD operations
- ✅ JSON seeding logic
- ✅ Room database operations

### Business Logic
- ✅ ViewModel state management
- ✅ Section selection and navigation
- ✅ Question answering logic
- ✅ Score tracking and high scores

### UI Layer
- ✅ Home page section display
- ✅ Weak topics calculation
- ✅ Form validation (create section, add question)
- ✅ Question card interactions
- ✅ Answer feedback display

## Key Testing Features

### Mockito Integration
- Mock dependencies for isolated unit testing
- Verify method calls and behavior
- Test error handling scenarios

### Coroutines Testing
- Test suspend functions with `runTest`
- Control coroutine execution with test dispatchers
- Verify asynchronous operations

### Compose Testing
- UI component interaction testing
- State verification
- User flow testing

### Room Database Testing
- In-memory database for fast, isolated tests
- Entity relationship testing
- Query verification

## Test Data

Tests use realistic test data that mirrors the app's actual data structure:
- Sample sections with questions and options
- Various accuracy percentages for weak topic testing
- Edge cases (empty sections, no attempts, etc.)

## Best Practices Followed

1. **Isolation**: Each test is independent and doesn't rely on test order
2. **Descriptive Naming**: Test names clearly describe what they verify
3. **Arrange-Act-Assert**: Clear structure in each test
4. **Mocking**: External dependencies are mocked for unit tests
5. **Coverage**: Tests cover happy paths, edge cases, and error scenarios

## Adding New Tests

When adding new features:
1. Add unit tests for business logic in `src/test/`
2. Add UI tests for new components in `src/androidTest/`
3. Update this documentation if adding new test categories
4. Ensure tests cover both positive and negative scenarios

## Troubleshooting

### Common Issues
- **Android Test Dependencies**: Make sure Android SDK is properly configured
- **Mockito Version Conflicts**: Ensure compatible versions are used
- **Compose Test Setup**: Verify `compose-bom` alignment between main and test dependencies

### Running Tests on CI/CD
The test setup is compatible with standard Android CI/CD pipelines. Tests can be run in parallel for faster execution.
