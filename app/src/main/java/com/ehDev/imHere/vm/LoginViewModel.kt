package com.ehDev.imHere.vm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.db.UrfuRoomDatabase
import com.ehDev.imHere.db.entity.AccountEntity
import com.ehDev.imHere.repository.UrfuRepository

class LoginViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repository: UrfuRepository

    init {

        val accountDao = UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        ).accountDao()

        repository = UrfuRepository(accountDao)
    }

    suspend fun getAccountByLogin(login: String) = repository.getAccountByLogin(login)

    fun saveAccountToSharedPrefs(account: AccountEntity) {

        val sp = app.getSharedPreferences("authentication", Context.MODE_PRIVATE)

        with(sp.edit()) {
            putBoolean("authentication", true)
            putString("status", account.status)
            putString("filter", account.filter)
            apply()
        }
    }
}