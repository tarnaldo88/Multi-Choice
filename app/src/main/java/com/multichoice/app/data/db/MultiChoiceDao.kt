package com.multichoice.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MultiChoiceDao {
    @Transaction
    @Query("SELECT * FROM sections ORDER BY title")
    suspend fun getSectionsWithQuestions(): List<SectionWithQuestions>

    @Insert
    suspend fun insertSection(section: SectionEntity): Long

    @Insert
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert
    suspend fun insertOptions(options: List<OptionEntity>)

    @Query("SELECT id FROM sections WHERE title = :title LIMIT 1")
    suspend fun findSectionIdByTitle(title: String): Long?

    @Query("SELECT id FROM questions WHERE sectionId = :sectionId AND prompt = :prompt LIMIT 1")
    suspend fun findQuestionId(sectionId: Long, prompt: String): Long?

    @Query("UPDATE questions SET explanation = :explanation WHERE id = :questionId")
    suspend fun updateQuestionExplanation(questionId: Long, explanation: String)

    @Query("DELETE FROM options WHERE questionId = :questionId")
    suspend fun deleteOptionsForQuestion(questionId: Long)

    @Query("SELECT COUNT(*) FROM sections")
    suspend fun countSections(): Int

    @Query("UPDATE sections SET highScore = :highScore WHERE id = :sectionId")
    suspend fun updateHighScore(sectionId: Long, highScore: Int)

    @Query(
        """
        UPDATE sections
        SET totalAttempts = totalAttempts + 1,
            totalCorrect = totalCorrect + :correctDelta,
            lastStudiedAt = :studiedAt
        WHERE id = :sectionId
        """
    )
    suspend fun recordAttempt(sectionId: Long, correctDelta: Int, studiedAt: Long)

}
