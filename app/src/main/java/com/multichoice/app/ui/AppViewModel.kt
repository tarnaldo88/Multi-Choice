package com.multichoice.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.multichoice.app.data.QuestionRepository
import com.multichoice.app.data.Section
import com.multichoice.app.data.SeedFileReader
import com.multichoice.app.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppState(
    val sections: List<Section> = emptyList(),
    val selectedSectionId: Long? = null,
    val studyIndex: Int = 0,
    val sessionCorrect: Int = 0
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = QuestionRepository(AppDatabase.getInstance(app).dao())
    private val answeredQuestionIds = mutableSetOf<Long>()
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
    viewModelScope.launch {
        runCatching {
            repo.seedIfEmpty(SeedFileReader.read(app))
            refreshSections()
        }.onFailure { e ->
            android.util.Log.e("AppViewModel", "Startup failed", e)
        }
    }
}


    fun addSection(title: String, description: String) {
        viewModelScope.launch {
            repo.addSection(title, description)
            refreshSections()
        }
    }

    fun addQuestion(sectionId: Long, prompt: String, options: List<String>, correctIndex: Int) {
        viewModelScope.launch {
            repo.addQuestion(sectionId, prompt, options, correctIndex)
            refreshSections()
        }
    }

    private suspend fun refreshSections() {
        val sections = repo.getSections()
        _state.value = _state.value.copy(sections = sections)
    }

    fun nextStudyQuestion() {
        val section = currentSection() ?: return
        if (section.questions.isEmpty()) return
        val next = (_state.value.studyIndex + 1) % section.questions.size
        _state.value = _state.value.copy(studyIndex = next)
    }

    fun currentSection(): Section? =
        _state.value.sections.firstOrNull { it.id == _state.value.selectedSectionId }
    
    fun selectSection(sectionId: Long) {
        answeredQuestionIds.clear()
        _state.value = _state.value.copy(
            selectedSectionId = sectionId,
            studyIndex = 0,
            sessionCorrect = 0
        )
    }
    fun submitAnswer(questionId: Long, isCorrect: Boolean) {
        if (!answeredQuestionIds.add(questionId)) return // count first attempt only

        if (isCorrect) {
            val newScore = _state.value.sessionCorrect + 1
            _state.value = _state.value.copy(sessionCorrect = newScore)

            val section = currentSection() ?: return
            if (newScore > section.highScore) {
                viewModelScope.launch {
                    repo.updateHighScore(section.id, newScore)
                    refreshSections()
                }
            }
        }
    }

}
