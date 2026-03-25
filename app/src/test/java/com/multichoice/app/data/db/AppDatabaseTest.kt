package com.multichoice.app.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: MultiChoiceDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.dao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetSection() = runTest {
        val section = SectionEntity(
            title = "Kotlin Basics",
            description = "Basic Kotlin concepts",
            highScore = 10,
            totalAttempts = 15,
            totalCorrect = 12,
            lastStudiedAt = 1234567890L
        )
        val sectionId = dao.insertSection(section)

        val sectionsWithQuestions = dao.getSectionsWithQuestions()
        assertEquals(1, sectionsWithQuestions.size)
        
        val retrievedSection = sectionsWithQuestions[0].section
        assertEquals(sectionId, retrievedSection.id)
        assertEquals("Kotlin Basics", retrievedSection.title)
        assertEquals("Basic Kotlin concepts", retrievedSection.description)
        assertEquals(10, retrievedSection.highScore)
        assertEquals(15, retrievedSection.totalAttempts)
        assertEquals(12, retrievedSection.totalCorrect)
        assertEquals(1234567890L, retrievedSection.lastStudiedAt)
    }

    @Test
    fun insertAndGetSectionWithQuestionsAndOptions() = runTest {
        val sectionId = dao.insertSection(
            SectionEntity(
                title = "Android",
                description = "Android development"
            )
        )

        val questionId = dao.insertQuestion(
            QuestionEntity(
                sectionId = sectionId,
                prompt = "What is Android?",
                explanation = "Mobile operating system"
            )
        )

        val options = listOf(
            OptionEntity(0L, questionId, "Mobile OS", true),
            OptionEntity(0L, questionId, "Programming Language", false),
            OptionEntity(0L, questionId, "Framework", false),
            OptionEntity(0L, questionId, "Database", false)
        )
        dao.insertOptions(options)

        val sectionsWithQuestions = dao.getSectionsWithQuestions()
        assertEquals(1, sectionsWithQuestions.size)

        val sectionWithQuestions = sectionsWithQuestions[0]
        assertEquals(1, sectionWithQuestions.questions.size)

        val questionWithOptions = sectionWithQuestions.questions[0]
        assertEquals(questionId, questionWithOptions.question.id)
        assertEquals("What is Android?", questionWithOptions.question.prompt)
        assertEquals("Mobile operating system", questionWithOptions.question.explanation)

        assertEquals(4, questionWithOptions.options.size)
        val correctOption = questionWithOptions.options.find { it.isCorrect }
        assertNotNull(correctOption)
        assertEquals("Mobile OS", correctOption!!.text)
    }

    @Test
    fun findSectionIdByTitle() = runTest {
        val sectionId = dao.insertSection(
            SectionEntity(
                title = "Unique Title",
                description = "Test description"
            )
        )

        val foundId = dao.findSectionIdByTitle("Unique Title")
        assertEquals(sectionId, foundId)

        val notFoundId = dao.findSectionIdByTitle("Nonexistent Title")
        assertNull(notFoundId)
    }

    @Test
    fun findQuestionId() = runTest {
        val sectionId = dao.insertSection(
            SectionEntity(title = "Test Section", description = "Test")
        )

        val questionId = dao.insertQuestion(
            QuestionEntity(
                sectionId = sectionId,
                prompt = "Test Question",
                explanation = "Test explanation"
            )
        )

        val foundId = dao.findQuestionId(sectionId, "Test Question")
        assertEquals(questionId, foundId)

        val notFoundId = dao.findQuestionId(sectionId, "Different Question")
        assertNull(notFoundId)
    }

    @Test
    fun updateHighScore() = runTest {
        val sectionId = dao.insertSection(
            SectionEntity(
                title = "Test Section",
                description = "Test",
                highScore = 5
            )
        )

        dao.updateHighScore(sectionId, 15)

        val sectionsWithQuestions = dao.getSectionsWithQuestions()
        val updatedSection = sectionsWithQuestions[0].section
        assertEquals(15, updatedSection.highScore)
    }

    @Test
    fun recordAttempt() = runTest {
        val sectionId = dao.insertSection(
            SectionEntity(
                title = "Test Section",
                description = "Test",
                totalAttempts = 10,
                totalCorrect = 7,
                lastStudiedAt = 1000L
            )
        )

        val studiedAt = 2000L
        dao.recordAttempt(sectionId, 1, studiedAt) // Correct answer

        val sectionsWithQuestions = dao.getSectionsWithQuestions()
        val updatedSection = sectionsWithQuestions[0].section
        assertEquals(11, updatedSection.totalAttempts)
        assertEquals(8, updatedSection.totalCorrect)
        assertEquals(studiedAt, updatedSection.lastStudiedAt)

        dao.recordAttempt(sectionId, 0, 3000L) // Incorrect answer
        val finalSection = dao.getSectionsWithQuestions()[0].section
        assertEquals(12, finalSection.totalAttempts)
        assertEquals(8, finalSection.totalCorrect) // Should remain 8
        assertEquals(3000L, finalSection.lastStudiedAt)
    }

    @Test
    fun updateQuestionExplanation() = runTest {
        val sectionId = dao.insertSection(
            SectionEntity(title = "Test Section", description = "Test")
        )

        val questionId = dao.insertQuestion(
            QuestionEntity(
                sectionId = sectionId,
                prompt = "Test Question",
                explanation = "Original explanation"
            )
        )

        dao.updateQuestionExplanation(questionId, "Updated explanation")

        val sectionsWithQuestions = dao.getSectionsWithQuestions()
        val questionWithOptions = sectionsWithQuestions[0].questions[0]
        assertEquals("Updated explanation", questionWithOptions.question.explanation)
    }

    @Test
    fun deleteOptionsForQuestion() = runTest {
        val sectionId = dao.insertSection(
            SectionEntity(title = "Test Section", description = "Test")
        )

        val questionId = dao.insertQuestion(
            QuestionEntity(
                sectionId = sectionId,
                prompt = "Test Question"
            )
        )

        val options = listOf(
            OptionEntity(0L, questionId, "Option 1", true),
            OptionEntity(0L, questionId, "Option 2", false)
        )
        dao.insertOptions(options)

        // Verify options were inserted
        var sectionsWithQuestions = dao.getSectionsWithQuestions()
        assertEquals(2, sectionsWithQuestions[0].questions[0].options.size)

        // Delete options
        dao.deleteOptionsForQuestion(questionId)

        // Verify options were deleted
        sectionsWithQuestions = dao.getSectionsWithQuestions()
        assertEquals(0, sectionsWithQuestions[0].questions[0].options.size)
    }

    @Test
    fun countSections() = runTest {
        assertEquals(0, dao.countSections())

        dao.insertSection(SectionEntity(title = "Section 1", description = "Test"))
        assertEquals(1, dao.countSections())

        dao.insertSection(SectionEntity(title = "Section 2", description = "Test"))
        assertEquals(2, dao.countSections())
    }

    @Test
    fun getSectionsWithQuestions_orderedByTitle() = runTest {
        val section3Id = dao.insertSection(SectionEntity(title = "Zebra", description = "Test"))
        val section1Id = dao.insertSection(SectionEntity(title = "Apple", description = "Test"))
        val section2Id = dao.insertSection(SectionEntity(title = "Banana", description = "Test"))

        val sectionsWithQuestions = dao.getSectionsWithQuestions()
        assertEquals(3, sectionsWithQuestions.size)
        
        // Should be ordered alphabetically by title
        assertEquals("Apple", sectionsWithQuestions[0].section.title)
        assertEquals("Banana", sectionsWithQuestions[1].section.title)
        assertEquals("Zebra", sectionsWithQuestions[2].section.title)
    }
}
