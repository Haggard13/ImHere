package com.ehDev.imHere.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interview_table")
data class InterviewEntity(

    @PrimaryKey val interview: String,
    val interviewer: String, // fixme?
    val interviewee: String, // fixme?
    val filter: String,
    val time: String
)