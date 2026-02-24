package com.multichoice.app.data

data class ChoiceOption(val text: String, val isCorrect: Boolean)
data class Question(
    val id: Long,
    val prompt: String,
    val options: List<ChoiceOption>,
    val explanation: String = ""
)
data class Section(
    val id: Long,
    val title: String,
    val description: String,
    val questions: List<Question> = emptyList(),
    val highScore: Int = 0,
    val totalAttempts: Int = 0,
    val totalCorrect: Int = 0,
    val lastStudiedAt: Long = 0L
) {
    val accuracyPercent: Int
        get() = if (totalAttempts == 0) 0 else (totalCorrect * 100) / totalAttempts
}
