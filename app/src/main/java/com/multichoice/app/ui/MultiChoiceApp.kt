package com.multichoice.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.multichoice.app.R
import com.multichoice.app.data.Question
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ButtonDefaults
import com.multichoice.app.ui.theme.MultiChoiceTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.ContentScale




private object Routes {
    const val HOME = "home"
    const val CREATE_SECTION = "create_section"
    const val SECTION = "section/{sectionId}"
    const val ADD_QUESTION = "add_question/{sectionId}"

    fun section(sectionId: Long) = "section/$sectionId"
    fun addQuestion(sectionId: Long) = "add_question/$sectionId"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MultiChoiceApp(vm: AppViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val nav = rememberNavController()

    MultiChoiceTheme  {
        Box(
            modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117)) // GitHub dark
        ) {
        Image(
            painter = painterResource(id = R.drawable.bg), // your png name
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.30f
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            NavHost(navController = nav, startDestination = Routes.HOME) {
                composable(Routes.HOME) {
                    HomePage(
                        sections = state.sections,
                        onCreateSection = { nav.navigate(Routes.CREATE_SECTION) },
                        onOpenSection = { sectionId ->
                            vm.selectSection(sectionId)
                            nav.navigate(Routes.section(sectionId))
                        }
                    )
                }

                composable(Routes.CREATE_SECTION) {
                    CreateSectionPage(
                        onSave = { title, desc ->
                            vm.addSection(title, desc)
                            nav.popBackStack()
                        },
                        onCancel = { nav.popBackStack() }
                    )
                }

                composable(
                    route = Routes.SECTION,
                    arguments = listOf(navArgument("sectionId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val sectionId =
                        backStackEntry.arguments?.getLong("sectionId") ?: return@composable
                    val section =
                        state.sections.firstOrNull { it.id == sectionId } ?: return@composable

                    SectionPage(
                        sectionTitle = section.title,
                        questions = section.questions,
                        sessionCorrect = state.sessionCorrect,
                        highScore = section.highScore,
                        onAnswer = { questionId, isCorrect -> vm.submitAnswer(questionId, isCorrect) },
                        onRetrySession = { vm.selectSection(sectionId) },
                        onBack = { nav.popBackStack() },
                        onAddQuestion = { nav.navigate(Routes.addQuestion(sectionId)) }
                    )

                }

                composable(
                    route = Routes.ADD_QUESTION,
                    arguments = listOf(navArgument("sectionId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val sectionId =
                        backStackEntry.arguments?.getLong("sectionId") ?: return@composable
                    AddQuestionPage(
                        onSave = { prompt, options, correctIndex, explanation ->
                            vm.addQuestion(sectionId, prompt, options, correctIndex, explanation)
                            nav.popBackStack()
                        },
                        onCancel = { nav.popBackStack() }
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun HomePage(
    sections: List<com.multichoice.app.data.Section>,
    onCreateSection: () -> Unit,
    onOpenSection: (Long) -> Unit
) {
    MultiChoiceTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "App icon"
                )
                Image(
                    painter = painterResource(id = R.drawable.multichoice),
                    contentDescription = "App logo"
                )
            }

            Button(onClick = onCreateSection, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )) {
                Text("Create New Section")
            }

            Text("Your Sections", style = MaterialTheme.typography.titleMedium)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sections) { section ->
                    Card(onClick = { onOpenSection(section.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(section.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                            Text(section.description)
                            Text("Questions: ${section.questions.size}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateSectionPage(onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Create Section", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (title.isNotBlank()) onSave(title.trim(), description.trim()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )) { Text("Save") }
            Button(onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )) { Text("Cancel") }
        }
    }
}

@Composable
private fun SectionPage(
    sectionTitle: String,
    questions: List<Question>,
    sessionCorrect: Int,
    highScore: Int,
    onAnswer: (Long, Boolean) -> Unit,
    onRetrySession: () -> Unit,
    onBack: () -> Unit,
    onAddQuestion: () -> Unit
) {
    var sessionVersion by remember { mutableIntStateOf(0) }
    // Shuffle once per section screen entry so each open gets a random question order.
    val randomizedQuestions = remember(questions, sessionVersion) { questions.shuffled() }
    // Track whether each answered question was correct (first attempt only).
    val sessionAnswers = remember(randomizedQuestions) { mutableStateMapOf<Long, Boolean>() }
    val unansweredQuestion = randomizedQuestions.firstOrNull { !sessionAnswers.containsKey(it.id) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(sectionTitle, style = MaterialTheme.typography.headlineSmall)
        Text("Questions: ${randomizedQuestions.size}")
        Text("Correct this session: $sessionCorrect")
        Text("All-time high: $highScore")


        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAddQuestion,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )) { Text("Add Question") }
            Button(onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )) { Text("Back") }
        }

        if (randomizedQuestions.isEmpty()) {
            Text("No questions in this section yet.")
            return@Column
        }

        if (unansweredQuestion != null) {
            Text("Remaining: ${randomizedQuestions.size - sessionAnswers.size}")
            StudyQuestionCard(
                question = unansweredQuestion,
                onAnswered = { isCorrect ->
                    // Record first answer result for summary and high score handling.
                    sessionAnswers[unansweredQuestion.id] = isCorrect
                    onAnswer(unansweredQuestion.id, isCorrect)
                }
            )
        } else {
            Text("Session Complete", style = MaterialTheme.typography.headlineSmall)
            Text("Final Score: $sessionCorrect / ${randomizedQuestions.size}")

            val incorrectQuestions = randomizedQuestions.filter { q -> sessionAnswers[q.id] == false }
            if (incorrectQuestions.isEmpty()) {
                Text("Perfect score. You answered all questions correctly.")
            } else {
                Text("Incorrectly Answered Questions", style = MaterialTheme.typography.titleMedium)
                incorrectQuestions.forEachIndexed { index, q ->
                    val correctAnswer = q.options.firstOrNull { it.isCorrect }?.text ?: "N/A"
                    Text("${index + 1}. ${q.prompt}")
                    Text("Correct answer: $correctAnswer", color = Color(0xFF22C55E))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onRetrySession()
                        sessionVersion++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF08C0B0),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text("Retry Session") }

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text("Back to Sections") }
            }
        }
    }
}

@Composable
private fun AddQuestionPage(
    onSave: (String, List<String>, Int, String) -> Unit,
    onCancel: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var o1 by remember { mutableStateOf("") }
    var o2 by remember { mutableStateOf("") }
    var o3 by remember { mutableStateOf("") }
    var o4 by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Add Question", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = prompt, onValueChange = { prompt = it }, label = { Text("Prompt") })
        OutlinedTextField(value = o1, onValueChange = { o1 = it }, label = { Text("Option 1 (Correct)") })
        OutlinedTextField(value = o2, onValueChange = { o2 = it }, label = { Text("Option 2") })
        OutlinedTextField(value = o3, onValueChange = { o3 = it }, label = { Text("Option 3") })
        OutlinedTextField(value = o4, onValueChange = { o4 = it }, label = { Text("Option 4") })
        OutlinedTextField(
            value = explanation,
            onValueChange = { explanation = it },
            label = { Text("Explanation (optional)") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val options = listOf(o1, o2, o3, o4).map { it.trim() }
                if (prompt.isNotBlank() && options.none { it.isBlank() }) {
                    onSave(prompt.trim(), options, 0, explanation.trim()) // option 1 treated as correct for now
                }
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )) { Text("Save") }

            Button(onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )) { Text("Cancel") }
        }
    }    
}

@Composable
private fun StudyQuestionCard(
    question: Question,
    onAnswered: (Boolean) -> Unit
) {
    var selectedIndex by remember(question.id) { mutableIntStateOf(-1) }

    // Randomize answer order for this question display
    val shuffledOptions = remember(question.id) {
        question.options.shuffled()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(question.prompt, style = MaterialTheme.typography.titleMedium)

        shuffledOptions.forEachIndexed { index, option ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (selectedIndex == -1) {
                            selectedIndex = index
                            onAnswered(option.isCorrect) // count only first answer
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(option.text)
                }
            }
        }

        if (selectedIndex >= 0) {
            val isCorrect = shuffledOptions[selectedIndex].isCorrect
            val correctAnswer = shuffledOptions.firstOrNull { it.isCorrect }?.text ?: "N/A"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isCorrect) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Correct",
                        tint = Color(0xFF22C55E)
                    )
                    Text(
                        "Correct",
                        color = Color(0xFF22C55E),
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Incorrect",
                        tint = Color(0xFFEF4444)
                    )
                    Text(
                        "Incorrect",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            if (!isCorrect) {
                Text(
                    "Correct answer: $correctAnswer",
                    color = Color(0xFF22C55E),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (question.explanation.isNotBlank()) {
                Text(
                    "Explanation: ${question.explanation}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

