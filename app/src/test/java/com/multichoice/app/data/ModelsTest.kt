package com.multichoice.app.data

import org.junit.Test
import org.junit.Assert.*

class ModelsTest {

    @Test
    fun `ChoiceOption should store text and correctness`() {
        val option = ChoiceOption(text = "Test Option", isCorrect = true)
        
        assertEquals("Test Option", option.text)
        assertTrue(option.isCorrect)
    }

    @Test
    fun `Question should store id, prompt, options, and explanation`() {
        val options = listOf(
            ChoiceOption("Option 1", false),
            ChoiceOption("Option 2", true),
            ChoiceOption("Option 3", false)
        )
        val question = Question(
            id = 1L,
            prompt = "What is 2+2?",
            options = options,
            explanation = "Basic arithmetic"
        )
        
        assertEquals(1L, question.id)
        assertEquals("What is 2+2?", question.prompt)
        assertEquals(3, question.options.size)
        assertEquals("Basic arithmetic", question.explanation)
        assertTrue(question.options[1].isCorrect)
    }

    @Test
    fun `Section should calculate accuracy percent correctly`() {
        val section = Section(
            id = 1L,
            title = "Test Section",
            description = "Test Description",
            questions = emptyList(),
            highScore = 0,
            totalAttempts = 10,
            totalCorrect = 7
        )
        
        assertEquals(70, section.accuracyPercent)
    }

    @Test
    fun `Section accuracy percent should return 0 when no attempts`() {
        val section = Section(
            id = 1L,
            title = "Test Section",
            description = "Test Description",
            questions = emptyList(),
            highScore = 0,
            totalAttempts = 0,
            totalCorrect = 0
        )
        
        assertEquals(0, section.accuracyPercent)
    }

    @Test
    fun `Section accuracy percent should handle integer division correctly`() {
        val section = Section(
            id = 1L,
            title = "Test Section",
            description = "Test Description",
            questions = emptyList(),
            highScore = 0,
            totalAttempts = 3,
            totalCorrect = 1
        )
        
        assertEquals(33, section.accuracyPercent) // 1*100/3 = 33.33 -> truncated to 33
    }

    @Test
    fun `Section should store all properties correctly`() {
        val question = Question(
            id = 1L,
            prompt = "Test Question",
            options = listOf(ChoiceOption("Answer", true)),
            explanation = "Test explanation"
        )
        
        val section = Section(
            id = 42L,
            title = "Kotlin Basics",
            description = "Basic Kotlin concepts",
            questions = listOf(question),
            highScore = 15,
            totalAttempts = 20,
            totalCorrect = 15,
            lastStudiedAt = 1234567890L
        )
        
        assertEquals(42L, section.id)
        assertEquals("Kotlin Basics", section.title)
        assertEquals("Basic Kotlin concepts", section.description)
        assertEquals(1, section.questions.size)
        assertEquals(question, section.questions[0])
        assertEquals(15, section.highScore)
        assertEquals(20, section.totalAttempts)
        assertEquals(15, section.totalCorrect)
        assertEquals(1234567890L, section.lastStudiedAt)
    }
}
