package com.multichoice.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.multichoice.app.data.ChoiceOption
import com.multichoice.app.data.Question
import com.multichoice.app.data.Section
import org.junit.Rule
import org.junit.Test

class MultiChoiceAppUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homePage_displaysSectionsCorrectly() {
        val sections = listOf(
            Section(
                id = 1L,
                title = "Kotlin Basics",
                description = "Basic Kotlin concepts",
                questions = emptyList(),
                highScore = 10,
                totalAttempts = 15,
                totalCorrect = 12,
                lastStudiedAt = 1234567890L
            ),
            Section(
                id = 2L,
                title = "Android Development",
                description = "Android app development",
                questions = emptyList(),
                highScore = 5,
                totalAttempts = 8,
                totalCorrect = 6,
                lastStudiedAt = 0L
            )
        )

        composeTestRule.setContent {
            HomePage(
                sections = sections,
                onCreateSection = { },
                onOpenSection = { }
            )
        }

        // Verify sections are displayed
        composeTestRule.onNodeWithText("Kotlin Basics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Basic Kotlin concepts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Android Development").assertIsDisplayed()
        composeTestRule.onNodeWithText("Android app development").assertIsDisplayed()

        // Verify stats are displayed
        composeTestRule.onNodeWithText("Questions: 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Attempts: 15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Accuracy: 80%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Attempts: 8").assertIsDisplayed()
        composeTestRule.onNodeWithText("Accuracy: 75%").assertIsDisplayed()

        // Verify create button
        composeTestRule.onNodeWithText("Create New Section").assertIsDisplayed()
    }

    @Test
    fun homePage_displaysWeakTopics() {
        val sections = listOf(
            Section(
                id = 1L,
                title = "Weak Topic",
                description = "Low accuracy",
                questions = emptyList(),
                totalAttempts = 10,
                totalCorrect = 3 // 30% accuracy
            ),
            Section(
                id = 2L,
                title = "Medium Topic",
                description = "Medium accuracy",
                questions = emptyList(),
                totalAttempts = 10,
                totalCorrect = 6 // 60% accuracy
            ),
            Section(
                id = 3L,
                title = "Strong Topic",
                description = "High accuracy",
                questions = emptyList(),
                totalAttempts = 10,
                totalCorrect = 9 // 90% accuracy
            )
        )

        composeTestRule.setContent {
            HomePage(
                sections = sections,
                onCreateSection = { },
                onOpenSection = { }
            )
        }

        // Verify weak topics section appears
        composeTestRule.onNodeWithText("Weak Topics").assertIsDisplayed()
        
        // Verify weak topics are ordered by accuracy (lowest first)
        composeTestRule.onNodeWithText("- Weak Topic: 30% (3/10)").assertIsDisplayed()
        composeTestRule.onNodeWithText("- Medium Topic: 60% (6/10)").assertIsDisplayed()
        
        // Strong topic should not be in weak topics (only top 3 weakest shown, but this is the strongest)
        composeTestRule.onNodeWithText("- Strong Topic: 90% (9/10)").assertDoesNotExist()
    }

    @Test
    fun createSectionPage_inputValidation() {
        var savedTitle = ""
        var savedDescription = ""

        composeTestRule.setContent {
            CreateSectionPage(
                onSave = { title, description ->
                    savedTitle = title
                    savedDescription = description
                },
                onCancel = { }
            )
        }

        // Verify initial state
        composeTestRule.onNodeWithText("Create Section").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()

        // Try to save with empty title
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should not save (title is blank)
        assert(savedTitle.isEmpty())

        // Enter title and description
        composeTestRule.onNodeWithText("Title").performTextInput("Test Section")
        composeTestRule.onNodeWithText("Description").performTextInput("Test Description")

        // Save now
        composeTestRule.onNodeWithText("Save").performClick()

        // Verify save was called with correct data
        assert(savedTitle == "Test Section")
        assert(savedDescription == "Test Description")
    }

    @Test
    fun addQuestionPage_inputValidation() {
        var savedPrompt = ""
        var savedOptions: List<String> = emptyList()

        composeTestRule.setContent {
            AddQuestionPage(
                onSave = { prompt, options, _, _ ->
                    savedPrompt = prompt
                    savedOptions = options
                },
                onCancel = { }
            )
        }

        // Verify initial state
        composeTestRule.onNodeWithText("Add Question").assertIsDisplayed()

        // Try to save with empty prompt
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should not save
        assert(savedPrompt.isEmpty())

        // Fill in prompt but leave options empty
        composeTestRule.onNodeWithText("Prompt").performTextInput("Test question")
        composeTestRule.onNodeWithText("Save").performClick()
        
        // Should not save (options are blank)
        assert(savedPrompt.isEmpty())

        // Fill in all options
        composeTestRule.onNodeWithText("Option 1 (Correct)").performTextInput("Correct Answer")
        composeTestRule.onNodeWithText("Option 2").performTextInput("Wrong Answer 1")
        composeTestRule.onNodeWithText("Option 3").performTextInput("Wrong Answer 2")
        composeTestRule.onNodeWithText("Option 4").performTextInput("Wrong Answer 3")

        // Save now
        composeTestRule.onNodeWithText("Save").performClick()

        // Verify save was called
        assert(savedPrompt == "Test question")
        assert(savedOptions.size == 4)
        assert(savedOptions[0] == "Correct Answer")
        assert(savedOptions[1] == "Wrong Answer 1")
    }

    @Test
    fun studyQuestionCard_displaysQuestionAndOptions() {
        val question = Question(
            id = 1L,
            prompt = "What is 2 + 2?",
            options = listOf(
                ChoiceOption("3", false),
                ChoiceOption("4", true),
                ChoiceOption("5", false),
                ChoiceOption("6", false)
            ),
            explanation = "Basic arithmetic"
        )

        composeTestRule.setContent {
            StudyQuestionCard(
                question = question,
                onAnswered = { }
            )
        }

        // Verify question is displayed
        composeTestRule.onNodeWithText("What is 2 + 2?").assertIsDisplayed()

        // Verify all options are displayed (order may be shuffled)
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        composeTestRule.onNodeWithText("4").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("6").assertIsDisplayed()
    }

    @Test
    fun studyQuestionCard_answerFeedback() {
        var answeredCorrectly = false
        var answeredIncorrectly = false

        val question = Question(
            id = 1L,
            prompt = "What is 2 + 2?",
            options = listOf(
                ChoiceOption("3", false),
                ChoiceOption("4", true),
                ChoiceOption("5", false),
                ChoiceOption("6", false)
            ),
            explanation = "Basic arithmetic"
        )

        composeTestRule.setContent {
            StudyQuestionCard(
                question = question,
                onAnswered = { isCorrect ->
                    if (isCorrect) answeredCorrectly = true
                    else answeredIncorrectly = true
                }
            )
        }

        // Answer correctly
        composeTestRule.onNodeWithText("4").performClick()

        // Verify correct feedback is shown
        composeTestRule.onNodeWithText("Correct").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Correct").assertIsDisplayed()
        
        // Verify callback was called
        assert(answeredCorrectly)
        assert(!answeredIncorrectly)

        // Reset for next test
        answeredCorrectly = false
        answeredIncorrectly = false

        composeTestRule.setContent {
            StudyQuestionCard(
                question = question,
                onAnswered = { isCorrect ->
                    if (isCorrect) answeredCorrectly = true
                    else answeredIncorrectly = true
                }
            )
        }

        // Answer incorrectly
        composeTestRule.onNodeWithText("3").performClick()

        // Verify incorrect feedback is shown
        composeTestRule.onNodeWithText("Incorrect").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Incorrect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Correct answer: 4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Explanation: Basic arithmetic").assertIsDisplayed()
        
        // Verify callback was called
        assert(!answeredCorrectly)
        assert(answeredIncorrectly)
    }

    @Test
    fun studyQuestionCard_onlyFirstAnswerCounts() {
        var answerCount = 0

        val question = Question(
            id = 1L,
            prompt = "What is 2 + 2?",
            options = listOf(
                ChoiceOption("3", false),
                ChoiceOption("4", true)
            )
        )

        composeTestRule.setContent {
            StudyQuestionCard(
                question = question,
                onAnswered = { answerCount++ }
            )
        }

        // First answer
        composeTestRule.onNodeWithText("3").performClick()
        assertEquals(1, answerCount)

        // Try to answer again - should not trigger callback
        composeTestRule.onNodeWithText("4").performClick()
        assertEquals(1, answerCount) // Should still be 1
    }

    @Test
    fun sectionPage_displaysStatsCorrectly() {
        composeTestRule.setContent {
            SectionPage(
                sectionTitle = "Test Section",
                questions = emptyList(),
                sessionCorrect = 5,
                highScore = 10,
                totalAttempts = 15,
                accuracyPercent = 67,
                lastStudiedAt = 1234567890L,
                onAnswer = { _, _ -> },
                onRetrySession = { },
                onBack = { },
                onAddQuestion = { }
            )
        }

        // Verify section info
        composeTestRule.onNodeWithText("Test Section").assertIsDisplayed()
        composeTestRule.onNodeWithText("Questions: 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Correct this session: 5").assertIsDisplayed()
        composeTestRule.onNodeWithText("All-time high: 10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Attempts: 15").assertIsDisplayed()
        composeTestRule.onNodeWithText("Accuracy: 67%").assertIsDisplayed()

        // Verify buttons
        composeTestRule.onNodeWithText("Add Question").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back").assertIsDisplayed()

        // Should show no questions message
        composeTestRule.onNodeWithText("No questions in this section yet.").assertIsDisplayed()
    }
}
