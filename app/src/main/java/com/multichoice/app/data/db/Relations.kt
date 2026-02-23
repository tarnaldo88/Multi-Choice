package com.multichoice.app.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class QuestionWithOptions(
    @Embedded val question: QuestionEntity,
    @Relation(parentColumn = "id", entityColumn = "questionId")
    val options: List<OptionEntity>
)

data class SectionWithQuestions(
    @Embedded val section: SectionEntity,
    @Relation(
        entity = QuestionEntity::class,
        parentColumn = "id",
        entityColumn = "sectionId"
    )
    val questions: List<QuestionWithOptions>
)
