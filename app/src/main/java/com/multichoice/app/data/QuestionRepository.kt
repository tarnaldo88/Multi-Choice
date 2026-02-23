package com.multichoice.app.data

import com.multichoice.app.data.db.MultiChoiceDao
import com.multichoice.app.data.db.OptionEntity
import com.multichoice.app.data.db.QuestionEntity
import com.multichoice.app.data.db.SectionEntity
import org.json.JSONArray

class QuestionRepository(private val dao: MultiChoiceDao) {

    suspend fun getSections(): List<Section> {
        return dao.getSectionsWithQuestions().map { swq ->
            Section(
                id = swq.section.id,
                title = swq.section.title,
                description = swq.section.description,
                questions = swq.questions.map { qwo ->
                    Question(
                        id = qwo.question.id,
                        prompt = qwo.question.prompt,
                        options = qwo.options.map { ChoiceOption(it.text, it.isCorrect) }
                    )
                }
            )
        }
    }

    suspend fun addSection(title: String, description: String) {
        dao.insertSection(SectionEntity(title = title, description = description))
    }

    suspend fun addQuestion(sectionId: Long, prompt: String, options: List<String>, correctIndex: Int) {
        val questionId = dao.insertQuestion(QuestionEntity(sectionId = sectionId, prompt = prompt))
        val optionEntities = options.mapIndexed { index, text ->
            OptionEntity(questionId = questionId, text = text, isCorrect = index == correctIndex)
        }
        dao.insertOptions(optionEntities)
    }

    suspend fun seedIfEmpty(seedJson: String) {
        if (dao.countSections() > 0) return

        val root = JSONArray(seedJson)
        for (i in 0 until root.length()) {
            val sectionObj = root.getJSONObject(i)
            val sectionId = dao.insertSection(
                SectionEntity(
                    title = sectionObj.getString("title"),
                    description = sectionObj.getString("description")
                )
            )

            val questions = sectionObj.getJSONArray("questions")
            for (j in 0 until questions.length()) {
                val qObj = questions.getJSONObject(j)
                val questionId = dao.insertQuestion(
                    QuestionEntity(sectionId = sectionId, prompt = qObj.getString("prompt"))
                )

                val opts = qObj.getJSONArray("options")
                val optionEntities = mutableListOf<OptionEntity>()
                for (k in 0 until opts.length()) {
                    val o = opts.getJSONObject(k)
                    optionEntities.add(
                        OptionEntity(
                            questionId = questionId,
                            text = o.getString("text"),
                            isCorrect = o.getBoolean("isCorrect")
                        )
                    )
                }
                dao.insertOptions(optionEntities)
            }
        }
    }
}
