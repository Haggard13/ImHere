package com.ehDev.imHere.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "institution_table")
data class InstitutionEntity(

    @PrimaryKey val institution: String,
    val latitude: Double,
    val longitude: Double
)