package com.multichoice.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class QuestionRepository(context: Context) {
    private val prefs = context.getSharedPreferences("multi_choice_store", Context.MODE_PRIVATE)
    private val key = "sections"

    fun getSections(): List<Section> {
        val raw = prefs.getString(key, "[]") ?: "[]"
        return parseSections(JSONArray(raw))
    }

    fun saveSections(sections: List<Section>) {
        prefs.edit().putString(key, toJson(sections).toString()).apply()
    }

    private fun toJson(sections: List<Section>): JSONArray {
        val array = JSONArray()
        sections.forEach { s ->
            val qArr = JSONArray()
            s.questions.forEach { q ->
                q.options.forEach { o ->
                    oArr.put(JSONObject().put("text", o.text).put("isCorrect", o.isCorrect))
                }
                qArr.put(
                    JSONObject().put("id", q.id).put("prompt", q.prompt).put("options", oArr)
                )
            }
            arr.put(
                JSONObject()
                    .put("id", s.id)
                    .put("title", s.title)
                    .put("description", s.description)
                    .put("questions", qArr)
            )
        }
        return array
    }

    private  fun parseSections(arr: JSONArray): List<Section> {
        val out = mutableListOf<Section>()

        for(i in 0 until arr.length()) {
            val s = arr.getJSONObject(i)
            val qArr = s.getJSONArray("questions") ?: JSONArray()
            val questions = mutableListOf<Question>()

            for(j in 0 until qArr.length()) {
                val q = qArr.getJSONObject(j)
                val oArr = q.getJSONArray("options") ?: JSONArray()
                val options = mutableListOf<ChoiceOption>()
                for(k in 0 until oArr.length()) {
                    val o = oArr.getJSONObject(k)
                    options.add(ChoiceOption(o.getString("text"), o.getBoolean("isCorrect")))
                }
                questions.add(Question(q.getLong("id"), q.getString("prompt"), options))
            }

            out.add(
                Section(
                    id = s.getLong("id"),
                    title = s.getString("title"),
                    description = s.getString("description"),
                    questions = questions
                    )
            )
        }
        return out
    }
}
