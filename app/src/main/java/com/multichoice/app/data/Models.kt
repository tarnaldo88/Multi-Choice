package com.multichoice.app.data

data class ChoiceOption(val text: String, val isCorrect: Boolean)
data class Question(val id: Long, val prompt: String, val options: List<ChoiceOption>)
data class Section(
    val id: Long,
    val title: String,
    val description: String,
    val questions: List<Question> = emptyList(),
    val highScore: Int = 0
)