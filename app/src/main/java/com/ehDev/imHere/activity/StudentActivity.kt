package com.ehDev.imHere.activity

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.TabHost.TabSpec
import android.widget.Toast
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
import com.ehDev.imHere.utils.location.OnLocationFailedListenerImpl
import com.ehDev.imHere.vm.StudentViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
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

private const val UPDATE_INTERVAL: Long = 5000
private const val FASTEST_INTERVAL: Long = 5000

class StudentActivity : AppCompatActivity(),
    GoogleApiClient.ConnectionCallbacks {

    private var locationRequest: LocationRequest? = null
    private var studentLocation: Location? = null
    private val permissions: MutableList<String> = mutableListOf()
    private val permissionsRejected: MutableList<String> = mutableListOf()

    private var googleApiClient: GoogleApiClient? = null

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

        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(
                OnLocationFailedListenerImpl { showToast("Обнаружены проблемы с интернетом") }
            )
            .build()

        classCardCreate()
        tabHostCreate()

        listViewCreate(studentInfo = studentViewModel.loadSavedStudentInfo())
    }

    // todo: разнести логику
    fun onCheckBtnClick(v: View) {
        studentViewModel.viewModelScope.launch {

//
//            location_progress_bar.visibility = View.VISIBLE
//            check_btn.isClickable = false

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
                    when (studentLocation!!.distanceTo(locationInst) <= 100) {
                        true -> {
                            studentViewModel.changeState(currentPair.date)
                            "ЗНАНИЕ - СИЛА"
                        }
                        false -> "СРОЧНО НА ПАРУ"
                    }
                }
            }

            showToast(toastText)

            location_tv.text = formatLocation(studentLocation)
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

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

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

    // Не работает с runtime permissions. Лень чекать на какой версии надо проверять,
    // так что пусть пока будет unused
    private fun permissionsToRequest(wantedPermissions: List<String>): List<String> {
        val result: ArrayList<String> = ArrayList()
        for (perm in wantedPermissions) {
            if (hasPermission(perm).not()) {
                result.add(perm)
            }
        }
        return result
    }

    private fun hasPermission(permission: String): Boolean {

        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, perms: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {

                for (perm in permissions) {
                    if (hasPermission(perm).not()) {
                        permissionsRejected.add(perm)
                    }
                }

                if (permissionsRejected.size > 0) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRejected[0])) {

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
                    if (googleApiClient != null) {
                        googleApiClient!!.connect()
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

        registerReceiver(mGpsSwitchStateReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        if (checkPlayServices().not()) {
            showToast("Нужно установить Google Play Services")
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        showToast("suspended")
    }

    override fun onConnected(bundle: Bundle?) = tryToConnectGPSServices()

    private fun tryToConnectGPSServices() {

        if ((hasPermission(ACCESS_FINE_LOCATION) && hasPermission(ACCESS_COARSE_LOCATION)).not()) {
            return
        }
// todo: порефакторить
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult?) {
                    showToast("location got? longitude: ${result?.lastLocation?.latitude}, latitude: ${result?.lastLocation?.latitude}")
                    result?.let { studentLocation = it.lastLocation }
                }
            }, null)

        if (studentLocation != null) {
            showToast("longitude: ${studentLocation?.longitude}, latitude: ${studentLocation?.latitude}")
        }
        startLocationUpdates()
    }

    private fun startLocationUpdates() {

        locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
        }


        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)
// todo: порефакторить
        val locationTask = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        locationTask.addOnCompleteListener {

            try {
                val response: LocationSettingsResponse? = locationTask.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                             // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable: ResolvableApiException = exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                this,
                                LocationRequest.PRIORITY_HIGH_ACCURACY
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
        }

        if ((hasPermission(ACCESS_FINE_LOCATION) && hasPermission(ACCESS_COARSE_LOCATION)).not()) {
            showToast("You need to enable permissions to display location !") // todo: порефакторить
        }

        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location: Location? ->
            location?.let { it: Location ->
                showToast("location updated? longitude: ${it.longitude}, latitude: ${it.latitude}") // todo: порефакторить
                studentLocation = it
            } ?: run {
                showToast("location failed?") // todo: порефакторить
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> when (resultCode) {
                Activity.RESULT_OK ->                 // All required changes were successfully made
                    showToast("onActivityResult: GPS Enabled by user")
                Activity.RESULT_CANCELED ->                 // The user was asked to change settings, but chose not to
                    showToast("onActivityResult: User rejected GPS request")
                else -> {}
            }
        }
    }

    private val mGpsSwitchStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action!!.matches(Regex("android.location.PROVIDERS_CHANGED"))) {

                val locationManager: LocationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                if (isGpsEnabled) {
                    tryToConnectGPSServices()
                } else {
                    showToast("без GPS приложение может работать неверно")
                }
            }
        }
    }
}