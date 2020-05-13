package com.ehDev.imHere.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.db.UrfuRoomDatabase
import com.ehDev.imHere.repository.UrfuRepository

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UrfuRepository

    init {

        val accountDao = UrfuRoomDatabase.getDatabase(
            context = application,
            scope = viewModelScope
        ).accountDao()

        repository = UrfuRepository(accountDao)
    }

    suspend fun getAccountByLogin(login: String) = repository.getAccountByLogin(login)
}