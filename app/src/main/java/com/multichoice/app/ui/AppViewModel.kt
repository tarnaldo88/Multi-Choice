package com.multichoice.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.multichoice.app.data.ChoiceOption
import com.multichoice.app.data.Question
import com.multichoice.app.data.QuestionRepository
import com.multichoice.app.data.Section
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppState(
    val sections: List<Section> = emptyList(),
    val selectedSectionId: Long? = null,
    val studyIndex: Int = 0
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = QuestionRepository(app)
    private val _state = MutableStateFlow(AppState(sections = repo.getSections()))
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun addSection(title: String, description: String) {
        val section = Section(System.currentTimeMillis(), title, description)
        val next = _state.value.sections + section
        save(next)
    }

    fun selectSection(sectionId: Long) {
        _state.value = _state.value.copy(selectedSectionId = sectionId, studyIndex = 0)
    }

    fun addQuestion(sectionId: Long, prompt: String, options: List<String>, correctIndex: Int) {
        val question = Question(
            id = System.currentTimeMillis(),
            prompt = prompt,
            options = options.mapIndexed { idx, text -> ChoiceOption(text, idx == correctIndex) }
        )
        val next = _state.value.sections.map { s ->
            if (s.id == sectionId) s.copy(questions = s.questions + question) else s
        }
        save(next)
    }

    fun nextStudyQuestion() {
        val section = currentSection() ?: return
        if (section.questions.isEmpty()) return
        val next = (_state.value.studyIndex + 1) % section.questions.size
        _state.value = _state.value.copy(studyIndex = next)
    }

    fun currentSection(): Section? =
        _state.value.sections.firstOrNull { it.id == _state.value.selectedSectionId }

    private fun save(sections: List<Section>) {
        _state.value = _state.value.copy(sections = sections)
        viewModelScope.launch { repo.saveSections(sections) }
    }
}
