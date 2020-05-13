package com.ehDev.imHere.repository

import com.ehDev.imHere.db.dao.AccountDao

class UrfuRepository(
    private val accountDao: AccountDao
) {

    suspend fun getAccountByLogin(login: String) = accountDao.getAccountByLogin(login)
}