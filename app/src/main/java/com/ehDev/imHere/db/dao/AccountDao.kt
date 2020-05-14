package com.ehDev.imHere.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ehDev.imHere.db.entity.AccountEntity

@Dao
interface AccountDao {

    @Query("SELECT * FROM account_table WHERE login = :login")
    suspend fun getAccountByLogin(login: String): AccountEntity

    @Query("SELECT * FROM account_table")
    suspend fun getAll(): List<AccountEntity>

    @Insert
    suspend fun insert(account: AccountEntity)
}