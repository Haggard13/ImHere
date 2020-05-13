package com.ehDev.imHere.db.dao

import androidx.room.Query
import com.ehDev.imHere.db.entity.AccountEntity

interface AccountDao {

    @Query("SELECT * FROM account_table WHERE login = :login")
    fun getAccountByLogin(login: String): AccountEntity
}