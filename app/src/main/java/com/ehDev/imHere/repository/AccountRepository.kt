package com.ehDev.imHere.repository

import com.ehDev.imHere.db.dao.AccountDao

class AccountRepository(
    private val accountDao: AccountDao
) {

    suspend fun getAccountByLogin(login: String) = accountDao.getAccountByLogin(login)
}