package com.ehDev.imHere.vm

import android.Manifest
import android.app.Activity
import android.app.Application
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.db.UrfuRoomDatabase
import com.ehDev.imHere.db.entity.ScheduleEntity
import com.ehDev.imHere.repository.InterviewRepository
import com.ehDev.imHere.repository.ScheduleRepository

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

    suspend fun getSchedule(): List<ScheduleEntity> = scheduleRepository.getSchedule()

    suspend fun changeState(date: String) = scheduleRepository.changeState(date)

    fun requestLocationPermission(activity: Activity) = ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        1
    )

    fun checkLocationPermission() = ActivityCompat.checkSelfPermission(
        app,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}