package com.ehDev.imHere.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.db.UrfuRoomDatabase
import com.ehDev.imHere.db.entity.ScheduleEntity
import com.ehDev.imHere.repository.InterviewRepository
import com.ehDev.imHere.repository.ScheduleRepository
import java.util.*

class StudentViewModel(private val app: Application) : AndroidViewModel(app) {

    private val interviewRepository: InterviewRepository
    private val scheduleRepository: ScheduleRepository

    init {

        val interviewDao = UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        ).interviewDao()

        interviewRepository = InterviewRepository(interviewDao)

        val scheduleDao = UrfuRoomDatabase.getDatabase(
                context = app,
                scope = viewModelScope
        ).scheduleDao()

        scheduleRepository = ScheduleRepository(scheduleDao)
    }

    suspend fun getAllInterviews() = interviewRepository.getAllInterviews()

    suspend fun getSchedule() : List<ScheduleEntity> = scheduleRepository.getSchedule()
}