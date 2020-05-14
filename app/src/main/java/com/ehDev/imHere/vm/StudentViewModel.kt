package com.ehDev.imHere.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.db.UrfuRoomDatabase
import com.ehDev.imHere.repository.InterviewRepository

class StudentViewModel(private val app: Application) : AndroidViewModel(app) {

    private val interviewRepository: InterviewRepository

    init {

        val interviewDao = UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        ).interviewDao()

        interviewRepository = InterviewRepository(interviewDao)
    }

    suspend fun getAllInterviews() = interviewRepository.getAllInterviews()
}