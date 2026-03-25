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
import org.junit.Assert.*

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

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        `when`(database.dao()).thenReturn(dao)
        seedFileReaderStatic = mockStatic(SeedFileReader::class.java)
        seedFileReaderStatic.`when`<String> { SeedFileReader.read(any()) }.thenReturn("[]")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        seedFileReaderStatic.close()
    }

    @Test
    fun `initial state should be empty`() = runTest {
        // Mock empty database
        `when`(dao.getSectionsWithQuestions()).thenReturn(emptyList())
        `when`(dao.countSections()).thenReturn(0)

        val viewModel = AppViewModel(application)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(emptyList(), state.sections)
        assertNull(state.selectedSectionId)
        assertEquals(0, state.studyIndex)
        assertEquals(0, state.sessionCorrect)
    }

    @Test
    fun `selectSection should update state`() = runTest {
        // Mock empty database initially
        `when`(dao.getSectionsWithQuestions()).thenReturn(emptyList())
        `when`(dao.countSections()).thenReturn(0)

        val viewModel = AppViewModel(application)
        testDispatcher.scheduler.advanceUntilIdle()

        val sectionId = 42L
        viewModel.selectSection(sectionId)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(sectionId, state.selectedSectionId)
        assertEquals(0, state.studyIndex)
        assertEquals(0, state.sessionCorrect)
    }

    @Test
    fun `nextStudyQuestion should handle empty questions`() = runTest {
        `when`(dao.getSectionsWithQuestions()).thenReturn(emptyList())
        `when`(dao.countSections()).thenReturn(0)

        val viewModel = AppViewModel(application)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should not crash when no questions exist
        viewModel.nextStudyQuestion()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(0, state.studyIndex)
    }

    @Test
    fun `currentSection should return null when no section selected`() = runTest {
        `when`(dao.getSectionsWithQuestions()).thenReturn(emptyList())
        `when`(dao.countSections()).thenReturn(0)

        val viewModel = AppViewModel(application)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.currentSection())
    }

    @Test
    fun `submitAnswer should handle no current section`() = runTest {
        `when`(dao.getSectionsWithQuestions()).thenReturn(emptyList())
        `when`(dao.countSections()).thenReturn(0)

        val viewModel = AppViewModel(application)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should not crash when no section is selected
        viewModel.submitAnswer(1L, true)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(0, state.sessionCorrect)
    }

    @Test
    fun `addSection should call repository`() = runTest {
        `when`(dao.getSectionsWithQuestions()).thenReturn(emptyList())
        `when`(dao.countSections()).thenReturn(0)

        val viewModel = AppViewModel(application)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addSection("Test Section", "Test Description")
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify that insertSection was called (through the repository)
        verify(dao, atLeastOnce()).insertSection(any())
    }

    @Test
    fun `addQuestion should call repository`() = runTest {
        `when`(dao.getSectionsWithQuestions()).thenReturn(emptyList())
        `when`(dao.countSections()).thenReturn(0)
        `when`(dao.insertQuestion(any())).thenReturn(1L)

        val viewModel = AppViewModel(application)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addQuestion(
            sectionId = 1L,
            prompt = "Test Question",
            options = listOf("Option 1", "Option 2", "Option 3", "Option 4"),
            correctIndex = 0,
            explanation = "Test explanation"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify that insertQuestion was called
        verify(dao, atLeastOnce()).insertQuestion(any())
    }
}
