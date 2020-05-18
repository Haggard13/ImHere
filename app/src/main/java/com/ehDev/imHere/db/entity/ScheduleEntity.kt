package com.ehDev.imHere.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "schedule_table")
data class ScheduleEntity(

        @PrimaryKey
        val date: String,

        val number: Int,

        val lecturer: String,

        val auditorium: String,

        val type: String,

        val name: String,

        val visit: String
)