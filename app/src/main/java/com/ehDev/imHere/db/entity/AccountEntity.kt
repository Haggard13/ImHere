package com.ehDev.imHere.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_table")
data class AccountEntity(

    @PrimaryKey val login: String,
    val password: Int,
    val status: String,
    val filter: String
)