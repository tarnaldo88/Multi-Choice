package com.multichoice.app.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.multichoice.app.data.Question
import com.multichoice.app.data.QuestionRepository
import com.multichoice.app.data.Section
import com.multichoice.app.data.SeedFileReader
import com.multichoice.app.data.db.AppDatabase
import com.multichoice.app.data.db.MultiChoiceDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class AppViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var database: AppDatabase

    @Mock
    private lateinit var dao: MultiChoiceDao

    @Mock
    private lateinit var seedFileReaderStatic: MockedStatic<SeedFileReader>

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AppViewModel
    private lateinit var repository: QuestionRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        `when`(database.dao()).thenReturn(dao)
        repository = QuestionRepository(dao)
        seedFileReaderStatic = mockStatic(SeedFileReader::class.java)
        seedFileReaderStatic.`when`<String> { SeedFileReader.read(any()) }.thenReturn("[]")

        // Create ViewModel with mocked dependencies
        viewModel = object : AppViewModel(application) {
            override fun getRepository(): QuestionRepository = repository
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        seedFileReaderStatic.close()
    }

    @Test
    fun `initial state should be empty`() = runTest {
        val state = viewModel.state.first()
        assertEquals(emptyList(), state.sections)
        assertNull(state.selectedSectionId)
        assertEquals(0, state.studyIndex)
        assertEquals(0, state.sessionCorrect)
    }

    @Test
    fun `addSection should add new section and refresh`() = runTest {
        val initialSections = listOf(
            Section(1L, "Section 1", "Description 1")
        )
        val updatedSections = listOf(
            Section(1L, "Section 1", "Description 1"),
            Section(2L, "Section 2", "Description 2")
        )

        `when`(dao.getSectionsWithQuestions()).thenReturn(emptyList())
            .thenReturn(listOf(createSectionWithQuestions(1L, "Section 1", "Description 1")))
            .thenReturn(listOf(
                createSectionWithQuestions(1L, "Section 1", "Description 1"),
                createSectionWithQuestions(2L, "Section 2", "Description 2")
            ))

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addSection("Section 2", "Description 2")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(dao).insertSection(argThat { section ->
            section.title == "Section 2" && section.description == "Description 2"
        })

        val state = viewModel.state.first()
        assertEquals(2, state.sections.size)
        assertEquals("Section 2", state.sections[1].title)
    }

    @Test
    fun `addQuestion should add question to section`() = runTest {
        val sectionId = 1L
        val prompt = "Test Question"
        val options = listOf("Option 1", "Option 2", "Option 3", "Option 4")
        val correctIndex = 1
        val explanation = "Test explanation"

        `when`(dao.insertQuestion(any())).thenReturn(10L)

        viewModel.addQuestion(sectionId, prompt, options, correctIndex, explanation)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(dao).insertQuestion(argThat { question ->
            question.sectionId == sectionId &&
            question.prompt == prompt &&
            question.explanation == explanation
        })

        val expectedOptions = options.mapIndexed { index, text ->
            com.multichoice.app.data.db.OptionEntity(
                questionId = 10L,
                text = text,
                isCorrect = index == correctIndex
            )
        }
        verify(dao).insertOptions(expectedOptions)
    }

    @Test
    fun `selectSection should update state and clear answered questions`() = runTest {
        val sectionId = 42L
        
        viewModel.selectSection(sectionId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(sectionId, state.selectedSectionId)
        assertEquals(0, state.studyIndex)
        assertEquals(0, state.sessionCorrect)
    }

    @Test
    fun `nextStudyQuestion should increment study index and wrap around`() = runTest {
        val questions = listOf(
            Question(1L, "Q1", emptyList()),
            Question(2L, "Q2", emptyList()),
            Question(3L, "Q3", emptyList())
        )
        val section = Section(1L, "Test", "Desc", questions)

        // Mock current section
        `when`(dao.getSectionsWithQuestions()).thenReturn(listOf(createSectionWithQuestions(1L, "Test", "Desc", questions)))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectSection(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.state.first().studyIndex)

        viewModel.nextStudyQuestion()
        assertEquals(1, viewModel.state.first().studyIndex)

        viewModel.nextStudyQuestion()
        assertEquals(2, viewModel.state.first().studyIndex)

        viewModel.nextStudyQuestion()
        assertEquals(0, viewModel.state.first().studyIndex) // Should wrap around
    }

    @Test
    fun `submitAnswer should record first attempt only`() = runTest {
        val questionId = 1L
        val sectionId = 42L
        val questions = listOf(Question(questionId, "Q1", emptyList()))
        val section = Section(sectionId, "Test", "Desc", questions, highScore = 5)

        `when`(dao.getSectionsWithQuestions()).thenReturn(listOf(createSectionWithQuestions(sectionId, "Test", "Desc", questions)))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectSection(sectionId)
        testDispatcher.scheduler.advanceUntilIdle()

        // First correct answer
        viewModel.submitAnswer(questionId, true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(dao).recordAttempt(sectionId, 1, any())
        assertEquals(1, viewModel.state.first().sessionCorrect)

        // Second attempt on same question should be ignored
        viewModel.submitAnswer(questionId, true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only be called once
        verify(dao, times(1)).recordAttempt(sectionId, 1, any())
        assertEquals(1, viewModel.state.first().sessionCorrect)
    }

    @Test
    fun `submitAnswer should update high score when beating previous high`() = runTest {
        val questionId = 1L
        val sectionId = 42L
        val questions = listOf(Question(questionId, "Q1", emptyList()))
        val section = Section(sectionId, "Test", "Desc", questions, highScore = 5)

        `when`(dao.getSectionsWithQuestions()).thenReturn(listOf(createSectionWithQuestions(sectionId, "Test", "Desc", questions, highScore = 5)))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectSection(sectionId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Submit 6 correct answers to beat high score of 5
        repeat(6) { i ->
            viewModel.submitAnswer((i + 1).toLong(), true)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        verify(dao).updateHighScore(sectionId, 6)
    }

    @Test
    fun `currentSection should return selected section`() = runTest {
        val sectionId = 42L
        val questions = listOf(Question(1L, "Q1", emptyList()))

        `when`(dao.getSectionsWithQuestions()).thenReturn(listOf(createSectionWithQuestions(sectionId, "Test", "Desc", questions)))
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.currentSection())

        viewModel.selectSection(sectionId)
        testDispatcher.scheduler.advanceUntilIdle()

        val currentSection = viewModel.currentSection()
        assertNotNull(currentSection)
        assertEquals(sectionId, currentSection.id)
        assertEquals("Test", currentSection.title)
    }

    private fun createSectionWithQuestions(
        id: Long,
        title: String,
        description: String,
        questions: List<Question> = emptyList(),
        highScore: Int = 0
    ) = com.multichoice.app.data.db.SectionWithQuestions(
        section = com.multichoice.app.data.db.SectionEntity(
            id = id,
            title = title,
            description = description,
            highScore = highScore
        ),
        questions = questions.map { question ->
            com.multichoice.app.data.db.QuestionWithOptions(
                question = com.multichoice.app.data.db.QuestionEntity(
                    id = question.id,
                    sectionId = id,
                    prompt = question.prompt,
                    explanation = question.explanation
                ),
                options = question.options.map { option ->
                    com.multichoice.app.data.db.OptionEntity(
                        questionId = question.id,
                        text = option.text,
                        isCorrect = option.isCorrect
                    )
                }
            )
        }
    )
}
