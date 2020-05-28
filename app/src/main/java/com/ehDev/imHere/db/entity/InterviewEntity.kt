package com.ehDev.imHere.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interview_table")
data class InterviewEntity(

    @PrimaryKey
    @ColumnInfo(name = "interview_reference")
    val interviewReference: String,

    val interviewer: String,
    val title: String,
    val course: String,
    val institution: String,
    val studentUnionInfo: String,
    val time: String
)