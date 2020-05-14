package com.ehDev.imHere.vm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.db.UrfuRoomDatabase
import com.ehDev.imHere.db.entity.AccountEntity
import com.ehDev.imHere.db.entity.InterviewEntity
import com.ehDev.imHere.repository.AccountRepository
import com.ehDev.imHere.repository.InterviewRepository

class AddInterviewViewModel (private val app: Application) : AndroidViewModel(app) {

    private val interviewRepository: InterviewRepository

    init {

        val interviewDao = UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        ).interviewDao()

        interviewRepository = InterviewRepository(interviewDao)
    }

    suspend fun getAllInterviews() = interviewRepository.getAllInterviews()

    suspend fun insertInterview(interview: InterviewEntity) = interviewRepository.insertInterview(interview)

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