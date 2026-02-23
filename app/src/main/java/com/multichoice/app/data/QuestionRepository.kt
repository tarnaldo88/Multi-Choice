package com.multichoice.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class QuestionRepository(context: Context) {
    private prefs = context.getSharedPreferences("multi_choice_store", Context.MODE_PRIVATE)
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
                    .put(":description, s.description")
                    .put("questions", qArr)
            )
        }
        return array
    }
}
