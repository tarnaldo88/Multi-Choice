package com.multichoice.app.data

import android.content.Context

object SeedFileReader {
    fun read(context: Context, fileName: String = "seed_questions.json"): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
