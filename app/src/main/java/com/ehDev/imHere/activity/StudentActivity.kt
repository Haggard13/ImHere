package com.ehDev.imHere.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.TabHost
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
import com.ehDev.imHere.vm.StudentViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.student_main.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar

private const val MONTH = 0
private const val DAY = 1
private const val HOURS = 2
private const val MINUTES = 3

class StudentActivity : AppCompatActivity(),
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private var locationRequest: LocationRequest? = null
    private var location: Location? = null
    private val permissions: MutableList<String> = mutableListOf()
    private val permissionsToRequest: MutableList<String> = mutableListOf()
    private val permissionsRejected: MutableList<String> = mutableListOf()

    private var googleApiClient: GoogleApiClient? = null

    private lateinit var studentViewModel: StudentViewModel

//    var locationManager: LocationManager? = null
//    var locationStudent: Location? = null
//    var requestLocationUpdateMade = false
//    var wifiMgr: WifiManager? = null

    private val currentDate by lazy { GregorianCalendar() }

    // TODO: вынести логику
//    private val locationListener = StudentLocationListener(locationStudent, baseContext, locationManager)

//    private var locationListener: LocationListener = object : LocationListener {
//
//        override fun onLocationChanged(location: Location) {
//            locationStudent = location
//        }
//
//        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
//
//        override fun onProviderEnabled(provider: String) {
//            if (studentViewModel.checkLocationPermission() != PackageManager.PERMISSION_GRANTED) return
//            locationManager!!.getLastKnownLocation(provider)
//        }
//
//        override fun onProviderDisabled(provider: String) {}
//    }

    //TODO: разнести
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_main)

        studentViewModel = ViewModelProvider(this).get(StudentViewModel::class.java)

        location_progress_bar.visibility = View.INVISIBLE

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        permissionsToRequest.addAll(permissionsToRequest(permissions))

        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), ALL_PERMISSIONS_RESULT)
        }


        googleApiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).build()

        //region Location Block
//        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

//        studentViewModel.requestLocationPermission(this)
//
//        when (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//            PackageManager.PERMISSION_GRANTED -> {
//                with(locationManager!!) {
//                    requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
//                    requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
//                }
//            }
//
//            else -> studentViewModel.requestLocationPermission(this)
//        }

        //endregion
//        wifiMgr = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        classCardCreate()
        tabHostCreate()

        listViewCreate(studentInfo = studentViewModel.loadSavedStudentInfo())
    }

    // todo: разнести логику
    fun onCheckBtnClick(v: View) {
        studentViewModel.viewModelScope.launch {

//            if (studentViewModel.checkLocationPermission() != PackageManager.PERMISSION_GRANTED) {
//                studentViewModel.requestLocationPermission(this@StudentActivity)
//                return@launch
//            }
//
//            if (requestLocationUpdateMade.not()) {
//                makeRequestLocationUpdate()
//            }
//
//            location_progress_bar.visibility = View.VISIBLE
//            check_btn.isClickable = false
//
//            withContext(Dispatchers.IO) {
//                while (locationStudent == null)
//                    delay(50)
//            }
//
//            if (studentViewModel.checkLocationPermission() != PackageManager.PERMISSION_GRANTED) {
//                studentViewModel.requestLocationPermission(this@StudentActivity)
//                return@launch
//            }

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
//                    val locationInst = Location("locationManager")
//                    with(locationInst) {
//                        latitude = institution.latitude
//                        longitude = institution.longitude
//                    }
//                    when (locationStudent!!.distanceTo(locationInst) <= 100) {
//                        true -> {
//                            studentViewModel.changeState(currentPair.date)
//                            "ЗНАНИЕ - СИЛА"
//                        }
//                        false -> "СРОЧНО НА ПАРУ"
//                    }
                }
            }

//            showToast(toastText)

//            location_tv.text = formatLocation(locationStudent)
//            wifi_tv.text = wifiMgr?.connectionInfo?.ssid
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
        val tabHost = findViewById<TabHost>(R.id.tabhost)
        tabHost.setup()
        var tabSpec: TabSpec
        tabSpec = tabHost.newTabSpec("tag1")
        tabSpec.setIndicator("Отметка")
        tabSpec.setContent(R.id.tab1)
        tabHost.addTab(tabSpec)
        tabSpec = tabHost.newTabSpec("tag2")
        tabSpec.setIndicator("Опросы")
        tabSpec.setContent(R.id.tab2)
        tabHost.addTab(tabSpec)
        tabHost.setCurrentTabByTag("tag1")
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

//    @SuppressLint("MissingPermission")
//    private fun makeRequestLocationUpdate() {
//        with(locationManager!!) {
//            requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
//            requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
//        }
//        requestLocationUpdateMade = true
//    }

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

    fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            showToast("Permission granted ${locationSettingsResponse.locationSettingsStates}")
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                showToast("попал в if")

                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    val REQUEST_CHECK_SETTINGS = 1488

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {

                for (perm in permissionsToRequest) {
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

        if (checkPlayServices().not()) {
            showToast("Нужно установить Google Play Services")
        }
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location?) {
        location ?: return
        showToast("longitude: ${location.longitude}, latitude: ${location.latitude}")
    }

    override fun onConnected(bundle: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        if (location != null) {
            showToast("longitude: ${location?.longitude}, latitude: ${location?.latitude}")
        }
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = UPDATE_INTERVAL
        locationRequest?.fastestInterval = FASTEST_INTERVAL
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            showToast("You need to enable permissions to display location !")
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
    }
}

private const val ALL_PERMISSIONS_RESULT = 1011
private const val PLAY_SERVICES_RESOLUTION_REQUEST = 1234

private const val UPDATE_INTERVAL: Long = 5000
private const val FASTEST_INTERVAL: Long = 5000