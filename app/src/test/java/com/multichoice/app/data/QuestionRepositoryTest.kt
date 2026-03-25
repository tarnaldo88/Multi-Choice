package com.multichoice.app.data

import com.multichoice.app.data.db.*
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuestionRepositoryTest {

    @Mock
    private lateinit var dao: MultiChoiceDao

    private lateinit var repository: QuestionRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = QuestionRepository(dao)
    }

    @Test
    fun `getSections should map entities to domain models correctly`() = runTest {
        val sectionEntity = SectionEntity(
            id = 1L,
            title = "Kotlin",
            description = "Kotlin basics",
            highScore = 10,
            totalAttempts = 15,
            totalCorrect = 12,
            lastStudiedAt = 1234567890L
        )

        val questionEntity = QuestionEntity(
            id = 1L,
            sectionId = 1L,
            prompt = "What is Kotlin?",
            explanation = "Programming language"
        )

        val optionEntities = listOf(
            OptionEntity(1L, "Language", true),
            OptionEntity(1L, "Framework", false)
        )

        val sectionWithQuestions = SectionWithQuestions(
            section = sectionEntity,
            questions = listOf(
                QuestionWithOptions(questionEntity, optionEntities)
            )
        )

        `when`(dao.getSectionsWithQuestions()).thenReturn(listOf(sectionWithQuestions))

        val result = repository.getSections()

        assertEquals(1, result.size)
        val section = result[0]
        assertEquals(1L, section.id)
        assertEquals("Kotlin", section.title)
        assertEquals("Kotlin basics", section.description)
        assertEquals(10, section.highScore)
        assertEquals(15, section.totalAttempts)
        assertEquals(12, section.totalCorrect)
        assertEquals(1234567890L, section.lastStudiedAt)
        
        assertEquals(1, section.questions.size)
        val question = section.questions[0]
        assertEquals(1L, question.id)
        assertEquals("What is Kotlin?", question.prompt)
        assertEquals("Programming language", question.explanation)
        assertEquals(2, question.options.size)
        assertTrue(question.options[0].isCorrect)
        assertEquals("Language", question.options[0].text)
        assertEquals("Framework", question.options[1].text)
    }

    @Test
    fun `updateHighScore should call dao updateHighScore`() = runTest {
        val sectionId = 1L
        val highScore = 15

        repository.updateHighScore(sectionId, highScore)

        verify(dao).updateHighScore(sectionId, highScore)
    }

    @Test
    fun `recordAttempt should call dao recordAttempt with correct delta`() = runTest {
        val sectionId = 1L
        val studiedAt = 1234567890L

        repository.recordAttempt(sectionId, true, studiedAt)
        verify(dao).recordAttempt(sectionId, 1, studiedAt)

        repository.recordAttempt(sectionId, false, studiedAt)
        verify(dao).recordAttempt(sectionId, 0, studiedAt)
    }

    @Test
    fun `addSection should call dao insertSection`() = runTest {
        val title = "Android"
        val description = "Android development"

        repository.addSection(title, description)

        verify(dao).insertSection(argThat { section ->
            section.title == title && section.description == description
        })
    }

    @Test
    fun `addQuestion should insert question and options`() = runTest {
        val sectionId = 1L
        val prompt = "What is Android?"
        val options = listOf("OS", "Language", "Framework", "Database")
        val correctIndex = 0
        val explanation = "Mobile operating system"
        val questionId = 42L

        `when`(dao.insertQuestion(any())).thenReturn(questionId)

        repository.addQuestion(sectionId, prompt, options, correctIndex, explanation)

        verify(dao).insertQuestion(argThat { question ->
            question.sectionId == sectionId &&
            question.prompt == prompt &&
            question.explanation == explanation
        })

        val expectedOptionEntities = options.mapIndexed { index, text ->
            OptionEntity(
                questionId = questionId,
                text = text,
                isCorrect = index == correctIndex
            )
        }

        verify(dao).insertOptions(expectedOptionEntities)
    }

    @Test
    fun `seedIfEmpty should create new sections when they don't exist`() = runTest {
        val seedJson = """
        [
            {
                "title": "Kotlin",
                "description": "Kotlin basics",
                "questions": [
                    {
                        "prompt": "What is Kotlin?",
                        "explanation": "Programming language",
                        "options": [
                            {"text": "Language", "isCorrect": true},
                            {"text": "Framework", "isCorrect": false}
                        ]
                    }
                ]
            }
        ]
        """.trimIndent()

        val sectionId = 1L
        val questionId = 2L

        `when`(dao.findSectionIdByTitle("Kotlin")).thenReturn(null)
        `when`(dao.insertSection(any())).thenReturn(sectionId)
        `when`(dao.findQuestionId(sectionId, "What is Kotlin?")).thenReturn(null)
        `when`(dao.insertQuestion(any())).thenReturn(questionId)

        repository.seedIfEmpty(seedJson)

        verify(dao).insertSection(argThat { it.title == "Kotlin" && it.description == "Kotlin basics" })
        verify(dao).insertQuestion(argThat { 
            it.sectionId == sectionId && 
            it.prompt == "What is Kotlin?" && 
            it.explanation == "Programming language" 
        })
        
        val expectedOptions = listOf(
            OptionEntity(questionId, "Language", true),
            OptionEntity(questionId, "Framework", false)
        )
        verify(dao).insertOptions(expectedOptions)
    }

    @Test
    fun `seedIfEmpty should update existing questions`() = runTest {
        val seedJson = """
        [
            {
                "title": "Kotlin",
                "description": "Kotlin basics",
                "questions": [
                    {
                        "prompt": "What is Kotlin?",
                        "explanation": "Updated explanation",
                        "options": [
                            {"text": "Language", "isCorrect": true},
                            {"text": "Framework", "isCorrect": false}
                        ]
                    }
                ]
            }
        ]
        """.trimIndent()

        val sectionId = 1L
        val existingQuestionId = 2L

        `when`(dao.findSectionIdByTitle("Kotlin")).thenReturn(sectionId)
        `when`(dao.findQuestionId(sectionId, "What is Kotlin?")).thenReturn(existingQuestionId)

        repository.seedIfEmpty(seedJson)

        verify(dao, never()).insertSection(any())
        verify(dao, never()).insertQuestion(any())
        verify(dao).updateQuestionExplanation(existingQuestionId, "Updated explanation")
        verify(dao).deleteOptionsForQuestion(existingQuestionId)
        
        val expectedOptions = listOf(
            OptionEntity(existingQuestionId, "Language", true),
            OptionEntity(existingQuestionId, "Framework", false)
        )
        verify(dao).insertOptions(expectedOptions)
    }
}
