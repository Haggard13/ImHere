package com.ehDev.imHere.activity

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.TabHost.TabSpec
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.R
import com.ehDev.imHere.adapter.InterviewRecyclerViewAdapter
import com.ehDev.imHere.adapter.ScheduleRecyclerViewAdapter
import com.ehDev.imHere.data.VisitState
import com.ehDev.imHere.data.filter.StudentInfo
import com.ehDev.imHere.db.entity.InterviewEntity
import com.ehDev.imHere.extensions.asInt
import com.ehDev.imHere.utils.AUTHENTICATION_SHARED_PREFS
import com.ehDev.imHere.utils.GpsSwitchBroadcastReceiver
import com.ehDev.imHere.vm.StudentViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationRequest
import kotlinx.android.synthetic.main.student_main.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar

private const val MONTH = 0
private const val DAY = 1
private const val HOURS = 2
private const val MINUTES = 3

private const val ALL_PERMISSIONS_RESULT = 1011
private const val PLAY_SERVICES_RESOLUTION_REQUEST = 1234

class StudentActivity : AppCompatActivity() {

    private val gpsBroadcastReceiver = GpsSwitchBroadcastReceiver(
        onGPSEnabledAction = { locationFacade.tryToConnectGPSServices() },
        onGPSDisabledAction = { studentViewModel.showToast("без GPS приложение может работать неверно") }
    )

    private val permissions: MutableList<String> = mutableListOf()
    private val permissionsRejected: MutableList<String> = mutableListOf()

    private val locationFacade by lazy {
        studentViewModel.getLocationFacade(this)
    }

    private lateinit var studentViewModel: StudentViewModel

    private val currentDate by lazy { GregorianCalendar() }

    //TODO: разнести
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_main)

        studentViewModel = ViewModelProvider(this).get(StudentViewModel::class.java)

        location_progress_bar.visibility = View.INVISIBLE

        permissions.add(ACCESS_FINE_LOCATION)
        permissions.add(ACCESS_COARSE_LOCATION)

        if (permissions.size > 0) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), ALL_PERMISSIONS_RESULT)
        }

        classCardCreate()
        tabHostCreate()

        listViewCreate(studentInfo = studentViewModel.loadSavedStudentInfo())
    }

    // todo: разнести логику
    fun onCheckBtnClick(v: View) {
        studentViewModel.viewModelScope.launch {

            val currentDateStringList = currentDate.asStringList()
            val fakeDate = getFakeDate(currentDateStringList)
            val schedule = studentViewModel.getSchedule()
            val nextPairs = schedule.filter {
                it.date.toGregorianCalendar() > fakeDate.toGregorianCalendar()
                        && isItCurrentDay(getSplitForStringDate(it.date), currentDateStringList)
                        && it.type != "Онлайн-курс"
            }//Оставшиеся пары на день

            val currentPair = when (nextPairs.isEmpty()) {

                true -> null
                false -> nextPairs.first()
            }

            val toastText = when {

                currentPair == null -> "На сегодня пар больше нет"

                currentPair.visit == VisitState.VISITED.name -> "Вы уже отметились"

                currentPair.date.toGregorianCalendar() > currentDate -> "Пара еще не началась"

                else -> {
                    val institutionName = parseInstitutionName(currentPair.auditorium)
                    val institution = studentViewModel.getInstitution(institutionName)
                    val locationInst = Location("locationManager")
                    with(locationInst) {
                        latitude = institution.latitude
                        longitude = institution.longitude
                    }
                    when (locationFacade.studentLocation!!.distanceTo(locationInst) <= 100) {
                        true -> {
//                            studentViewModel.changeState(currentPair.date) // fixme
                            "Вы отметились"
                        }
                        false -> "Вы находитесь далеко от института"
                    }
                }
            }

            studentViewModel.showToast(toastText)

            location_tv.text = formatLocation(locationFacade.studentLocation)
            location_progress_bar.visibility = View.INVISIBLE
            check_btn.isClickable = true
        }
    }

    fun onExitBtnClick(v: View) {

        val sp = getSharedPreferences(AUTHENTICATION_SHARED_PREFS, Context.MODE_PRIVATE)
        sp.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        super@StudentActivity.finish()
    }

    // TODO: ну тут явно чет не так
    private fun tabHostCreate() {

        tabhost.setup()

        var tabSpec: TabSpec = tabhost.newTabSpec("tag1")
        tabSpec.setIndicator("Отметка")
        tabSpec.setContent(R.id.tab1)
        tabhost.addTab(tabSpec)

        tabSpec = tabhost.newTabSpec("tag2")
        tabSpec.setIndicator("Опросы")
        tabSpec.setContent(R.id.tab2)
        tabhost.addTab(tabSpec)

        tabhost.setCurrentTabByTag("tag1")
    }

    private fun listViewCreate(studentInfo: StudentInfo) {
        studentViewModel.viewModelScope.launch {

            val allInterviews = studentViewModel.getAllInterviews()
                .filter { it.interviewReference.isValidUrl() }
                .filter { filterStudentInfo(it, studentInfo) }
                .filter { filterInterviewDate(it) }

            interview_rv.adapter = InterviewRecyclerViewAdapter(allInterviews) {
                startActivity(Intent(ACTION_VIEW, Uri.parse(it.interviewReference)))
            }
        }
    }

    private fun classCardCreate() {
        studentViewModel.viewModelScope.launch {

            val schedule = studentViewModel.getSchedule()
            val scheduleOnThisDay = schedule.filter {
                isItCurrentDay(
                    getSplitForStringDate(it.date), currentDate.asStringList()
                )
            }

            schedule_rv.adapter = ScheduleRecyclerViewAdapter(scheduleOnThisDay)
        }
    }

    private fun formatLocation(location: Location?) = when (location) {
        null -> ""
        else -> "lat = %1$.6f, lon = %2$.6f".format(location.latitude, location.longitude)
    }

    private fun String.isValidUrl() = URLUtil.isValidUrl(this)
    //.not() // fixme: нужно убрать not(). Оставляю пока, чтобы тестить было легче

    private fun getFakeDate(date: List<String>): String = when ((date[MINUTES]).asInt() < 30) {

        true -> "${date[MONTH]}, ${date[DAY]}, ${date[HOURS].asInt() - 2}, ${date[MINUTES].asInt() + 30}"
        else -> "${date[MONTH]}, ${date[DAY]}, ${date[HOURS].asInt() + 1}, ${date[MINUTES].asInt() - 30}"
    }

    private fun isItCurrentDay(date: List<String>, todayDate: List<String>): Boolean =
        date[MONTH] == todayDate[MONTH] && date[DAY] == todayDate[DAY]

    private fun GregorianCalendar.asStringList(): List<String> = listOf(
        (get(Calendar.MONTH) + 1).toString(),
        get(Calendar.DAY_OF_MONTH).toString(),
        get(Calendar.HOUR_OF_DAY).toString(),
        get(Calendar.MINUTE).toString()
    )

    private fun getSplitForStringDate(date: String): List<String> = date.replace(" ", "").split(",")

    private fun String.toGregorianCalendar(): GregorianCalendar {

        val listDate = getSplitForStringDate(this)
        val year = currentDate.get(Calendar.YEAR)

        return GregorianCalendar(
            year,
            listDate[MONTH].toInt() - 1,
            listDate[DAY].toInt(),
            listDate[HOURS].toInt(),
            listDate[MINUTES].toInt()
        )
    }

    private fun parseInstitutionName(auditorium: String) = auditorium.split('-').first()

    private fun filterInterviewDate(interview: InterviewEntity): Boolean {
        // Тут по индексации из-за того что есть год идет смещение на одну позицию.
        // Решил не переделывать, потому что переделывание монструозно слишком
        val dateList = interview.time.split(':', '/', ' ')
        val dateInterview = GregorianCalendar(
            2000 + dateList[0].toInt(),
            dateList[MONTH + 1].toInt(),
            dateList[DAY + 1].toInt(),
            dateList[HOURS + 1].toInt(),
            dateList[MINUTES + 1].toInt()
        )
        return dateInterview > currentDate
    }

    private fun filterStudentInfo(interview: InterviewEntity, studentInfo: StudentInfo) =
        (studentInfo.course.isCourseCorrect(interview.course))
                && (studentInfo.institution.isInstituteCorrect(interview.institution))
                && (studentInfo.studentUnionInfo.isStudentUnionInfoCorrect(interview.studentUnionInfo))

    override fun onRequestPermissionsResult(requestCode: Int, perms: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {

                for (perm in permissions) {
                    if (hasPermission(perm).not()) {
                        permissionsRejected.add(perm)
                    }
                }

                if (permissionsRejected.size > 0) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRejected.first())) {

                        AlertDialog.Builder(this).setMessage(
                            "Без этих разрешений приложение будет неработоспособным"
                        ).setPositiveButton("OK") { _, _ ->
                            ActivityCompat.requestPermissions(
                                this, permissionsRejected.toTypedArray(), ALL_PERMISSIONS_RESULT
                            )
                        }.setNegativeButton("Cancel", null).create().show()

                        return
                    }
                }
                else {
                    if (locationFacade.googleApiClient != null) {
                        locationFacade.googleApiClient!!.connect()
                    }
                }
            }
        }
    }

    private fun checkPlayServices(): Boolean {
        val apiAvailability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode: Int = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
            }
            else {
                finish()
            }
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()

        registerReceiver(gpsBroadcastReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        if (checkPlayServices().not()) {
            studentViewModel.showToast("Нужно установить Google Play Services")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> when (resultCode) {
                Activity.RESULT_OK -> studentViewModel.showToast("onActivityResult: GPS Enabled by user")
                Activity.RESULT_CANCELED -> studentViewModel.showToast("onActivityResult: User rejected GPS request")
                else -> {
                }
            }
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}