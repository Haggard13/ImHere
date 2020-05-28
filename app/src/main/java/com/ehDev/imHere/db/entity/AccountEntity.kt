package com.ehDev.imHere.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_table")
data class AccountEntity(

    @PrimaryKey
    val login: String,

    val password: Int,

    @ColumnInfo(name = "person_type")
    val personType: String
)