package com.ehDev.imHere.vm

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.data.filter.CourseType
import com.ehDev.imHere.data.filter.InstitutionType
import com.ehDev.imHere.data.filter.StudentInfo
import com.ehDev.imHere.data.filter.StudentUnionType
import com.ehDev.imHere.db.UrfuRoomDatabase
import com.ehDev.imHere.db.entity.InstitutionEntity
import com.ehDev.imHere.db.entity.ScheduleEntity
import com.ehDev.imHere.repository.InstitutionRepository
import com.ehDev.imHere.repository.InterviewRepository
import com.ehDev.imHere.repository.LocationFacade
import com.ehDev.imHere.repository.ScheduleRepository
import com.ehDev.imHere.utils.STUDENT_INFO_SHARED_PREFS
import com.google.gson.Gson

class StudentViewModel(private val app: Application) : AndroidViewModel(app) {

    private val interviewRepository: InterviewRepository
    private val scheduleRepository: ScheduleRepository
    private val institutionRepository: InstitutionRepository

    fun getLocationFacade(activity: Activity) = LocationFacade(
        activity = activity,
        context = app,
        showToastCallback = { showToast(it) }
    )

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

        val institutionDao = UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        ).institutionDao()

        institutionRepository = InstitutionRepository(institutionDao)
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

    suspend fun getInstitution(prefix: String): InstitutionEntity = institutionRepository.getCoordinates(prefix).first()

    fun loadSavedStudentInfo(): StudentInfo {

        val sp = app.getSharedPreferences(STUDENT_INFO_SHARED_PREFS, Context.MODE_PRIVATE)
        val studentInfoJson = sp.getString(STUDENT_INFO_SHARED_PREFS, "")

        return when (studentInfoJson.isNullOrBlank()) {

            true -> {
                val studentInfo = getFakeStudentInfo()
                saveStudentInfo(studentInfo)
                studentInfo
            }
            false -> Gson().fromJson(studentInfoJson, StudentInfo::class.java)
        }
    }

    private fun saveStudentInfo(studentInfo: StudentInfo) {

        val sp = app.getSharedPreferences(STUDENT_INFO_SHARED_PREFS, Context.MODE_PRIVATE)
        with(sp.edit()) {
            val studentInfoJson = Gson().toJson(studentInfo)
            putString(STUDENT_INFO_SHARED_PREFS, studentInfoJson)
            apply()
        }
    }

    private fun getFakeStudentInfo() = StudentInfo(
        CourseType.FIRST,
        InstitutionType.InFO,
        StudentUnionType.NOT_IN_STUDENT_UNION
    )

    fun showToast(text: String) = Toast.makeText(app, text, Toast.LENGTH_LONG).show()}