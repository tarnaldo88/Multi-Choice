package com.multichoice.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MultiChoiceApp(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var showCreateSection by remember { mutableStateOf(false) }
    var showCreateQuestion by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Multi-Choice", style = MaterialTheme.typography.headlineMedium)

            if (showCreateSection) {
                CreateSectionForm(
                    onSave = { t, d -> vm.addSection(t, d); showCreateSection = false },
                    onCancel = { showCreateSection = false }
                )
            } else {
                Button(onClick = { showCreateSection = true }) { Text("Create Section") }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.sections) { section ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(section.title, style = MaterialTheme.typography.titleMedium)
                            Text(section.description)
                            Text("Questions: ${section.questions.size}")
                        }
                        Button(onClick = { vm.selectSection(section.id) }) { Text("Study") }
                    }
                }
            }

            val selected = vm.currentSection()
            if (selected != null) {
                Text("Selected: ${selected.title}", style = MaterialTheme.typography.titleMedium)
                if (showCreateQuestion) {
                    CreateQuestionForm(
                        onSave = { prompt, opts, correct ->
                            vm.addQuestion(selected.id, prompt, opts, correct)
                            showCreateQuestion = false
                        },
                        onCancel = { showCreateQuestion = false }
                    )
                } else {
                    Button(onClick = { showCreateQuestion = true }) { Text("Add Question") }
                }

                val q = selected.questions.getOrNull(state.studyIndex)
                if (q != null) {
                    Text("Study Question", style = MaterialTheme.typography.titleMedium)
                    Text(q.prompt)
                    q.options.forEachIndexed { idx, option ->
                        val mark = if (option.isCorrect) " (Correct)" else ""
                        Text("${idx + 1}. ${option.text}$mark")
                    }
                    Button(onClick = { vm.nextStudyQuestion() }) { Text("Next Question") }
                }
            }
        }
    }
}

@Composable
private fun CreateSectionForm(onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Section title") })
        OutlinedTextField(description, { description = it }, label = { Text("Description") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (title.isNotBlank()) onSave(title.trim(), description.trim()) }) { Text("Save") }
            Button(onClick = onCancel) { Text("Cancel") }
        }
    }
}