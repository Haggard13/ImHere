package com.ehDev.imHere.vm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.db.UrfuRoomDatabase
import com.ehDev.imHere.db.entity.AccountEntity
import com.ehDev.imHere.repository.AccountRepository

class LoginViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repository: AccountRepository

    private val db: UrfuRoomDatabase by lazy {

        UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        )
    }

    init {

        val accountDao = db.accountDao()

        repository = AccountRepository(accountDao)
    }

    /**
     * В Room колбек onCreate дергается только после обращения к бд
     * с действием на чтение/запись. Поэтому, имитируем обращение, чтобы
     * дать заполниться фейковы данным. Если этого не сделать, то к моменту самого первого
     * обращения к бд (например - с помощью [getAccountByLogin]) мы получим null, так как
     * бд будет пустой, и только после этого самого обращения бд заполнится значениями.
     *
     * https://stackoverflow.com/questions/48280941/room-database-force-oncreate-callback
     */
    fun fillDatabaseWithFakeInfo() {
        db.runInTransaction {
            // имитируем работу с бд, так как она заполняется фейковыми данными только после обращения к ней
        }
    }

    suspend fun getAccountByLogin(login: String) = repository.getAccountByLogin(login)

    fun saveAccountToSharedPrefs(account: AccountEntity) {

        val sp = app.getSharedPreferences("authentication", Context.MODE_PRIVATE)

        with(sp.edit()) {
            putBoolean("authentication", true)
            putString("personType", account.personType)
            putString("filter", account.filter)
            apply()
        }
    }
}