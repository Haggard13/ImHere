package com.ehDev.imHere.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.TabHost
import android.widget.TabHost.TabSpec
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.R
import com.ehDev.imHere.adapter.InterviewRecyclerViewAdapter
import com.ehDev.imHere.adapter.ScheduleRecyclerViewAdapter
import com.ehDev.imHere.data.VisitState
import com.ehDev.imHere.vm.StudentViewModel
import kotlinx.android.synthetic.main.student_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.GregorianCalendar

private const val MONTH = 0
private const val DAY = 1
private const val HOURS = 2
private const val MINUTES = 3

class StudentActivity : AppCompatActivity() {

    private lateinit var studentViewModel: StudentViewModel

    var locationManager: LocationManager? = null
    var locationStudent: Location? = null
    var requestLocationUpdateMade = false
    var wifiMgr: WifiManager? = null

    // TODO: вынести логику
//    private val locationListener = StudentLocationListener(locationStudent, baseContext, locationManager)

    private var locationListener: LocationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            locationStudent = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

        override fun onProviderEnabled(provider: String) {
            if (ActivityCompat.checkSelfPermission(
                    baseContext, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) return
            locationManager!!.getLastKnownLocation(provider)
        }

        override fun onProviderDisabled(provider: String) {}
    }

    //TODO: разнести
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_main)

        studentViewModel = ViewModelProvider(this).get(StudentViewModel::class.java)

        progressBar.visibility = View.INVISIBLE

        //region Location Block
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        studentViewModel.requestLocationPermission(this)

        when (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            PackageManager.PERMISSION_GRANTED -> {
                with(locationManager!!) {
                    requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                    requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
                }
            }

            else -> studentViewModel.requestLocationPermission(this)
        }

        //endregion
        wifiMgr = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        classCardCreate()
        tabHostCreate()
        listViewCreate()
    }

    fun onCheckBtnClick(v: View) {
        studentViewModel.viewModelScope.launch {

            if (studentViewModel.checkLocationPermission() != PackageManager.PERMISSION_GRANTED) {
                studentViewModel.requestLocationPermission(this@StudentActivity)
                return@launch
            }

            if (requestLocationUpdateMade.not()) {
                makeRequestLocationUpdate()
            }

            progressBar.visibility = View.VISIBLE
            check_btn.isClickable = false

            withContext(Dispatchers.IO) {
                while (locationStudent == null)
                    delay(50)
            }

            if (studentViewModel.checkLocationPermission() != PackageManager.PERMISSION_GRANTED) {
                studentViewModel.requestLocationPermission(this@StudentActivity)
                return@launch
            }

            lateinit var toastText : String

            val nowDateGC = GregorianCalendar() // дата как класс
            val nowDateLS = getDateAsListOfString(GregorianCalendar()) // дата как list строк
            val fakeDate = getFakeDate(nowDateLS)
            val schedule = studentViewModel.getSchedule()
            val nextPairs = schedule.filter {
                getDateAsGregorianCalendar(it.date) > getDateAsGregorianCalendar(fakeDate)
                        && filterForScheduleOnThisDay(getSplitForStringDate(it.date), nowDateLS)
            }//Оставшиеся пары на день

            val nowPair = if (nextPairs.isEmpty()) null
                    else nextPairs.first()

            toastText = if (nowPair == null) {
                "На сегодня пар больше нет"
            } else if (getDateAsGregorianCalendar(nowPair.date) > nowDateGC) {
                "Пара еще не началась"
            } else if (nowPair.visit == VisitState.VISITED.name){
                "Вы уже отметились"
            } else {
                val nameOfInstitution = getNameOfInstitution(nowPair.auditorium)
                val institution = studentViewModel.getInstitution(nameOfInstitution)
                val locationInst = Location("locationManager")
                with(locationInst) {
                    latitude = institution.latitude
                    longitude = institution.longitude
                }
                when (locationStudent!!.distanceTo(locationInst) <= 100) {
                    true -> {
                        studentViewModel.changeState(nowPair.date)
                        "ЗНАНИЕ - СИЛА"
                    }
                    false -> "СРОЧНО НА ПАРУ"
                }
            }

            showToast(toastText)

            location_tv.text = formatLocation(locationStudent)
            wifi_tv.text = wifiMgr?.connectionInfo?.ssid
            progressBar.visibility = View.INVISIBLE
            check_btn.isClickable = true
        }
    }

    fun onExitBtnClick(v: View) {

        val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)
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

    private fun listViewCreate() {
        studentViewModel.viewModelScope.launch {

//            val filter = getSharedPreferences("authentication", MODE_PRIVATE).getString("filter", "682") // fixme
            val filter = "682" // fixme: это для тестов, потом заменить на строку выше

            val allInterviews = studentViewModel.getAllInterviews()
                .filter { it.interviewReference.isValidUrl() }
                .filter { it.filter == filter || it.filter == "682" }

            interview_rv.adapter = InterviewRecyclerViewAdapter(allInterviews) {
                startActivity(Intent(ACTION_VIEW, Uri.parse(it.interviewReference)))//fixme проверить работу
                //showToast("тип переход по клику")
            }
        }
    }

    private fun classCardCreate() {
        studentViewModel.viewModelScope.launch {

            val schedule = studentViewModel.getSchedule()
            val todayDate = GregorianCalendar()
            val todayDateList = getDateAsListOfString(todayDate)
            val scheduleOnThisDay = schedule.filter {
                filterForScheduleOnThisDay(getSplitForStringDate(it.date), todayDateList)//Отбирает пары только на этот день
            }

            schedule_rv.adapter = ScheduleRecyclerViewAdapter(scheduleOnThisDay)
        }
    }

    private fun formatLocation(location: Location?) = when (location) {

        null -> ""
        else -> String.format(
            "lat = %1$.6f, lon = %2$.6f", location.latitude, location.longitude
        ) // todo: обдумать как переписать
    }

    @SuppressLint("MissingPermission")
    private fun makeRequestLocationUpdate() {
        with(locationManager!!) {
            requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        }
        requestLocationUpdateMade = true
    }

    private fun String.isValidUrl() = URLUtil.isValidUrl(this)
        //.not() // fixme: нужно убрать not(). Оставляю пока, чтобы тестить было легче

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private fun getFakeDate(date: List<String>): String = when (Integer.parseInt(date[MINUTES]) < 30) {

        true ->
            date[MONTH] + ", " +
            date[DAY] + ", " +
            (Integer.parseInt(date[HOURS]) - 2).toString() + ", " +
            (Integer.parseInt(date[MINUTES]) + 30).toString()
        else ->
                date[MONTH] + ", " +
                date[DAY] + ", " +
                (Integer.parseInt(date[HOURS]) + 1).toString() + ", " +
                (Integer.parseInt(date[MINUTES]) - 30).toString()
    }

    private fun filterForScheduleOnThisDay(date: List<String>, todayDate: List<String>): Boolean =
        date[MONTH] == todayDate[MONTH] && date[DAY] == todayDate[DAY]

    private fun getDateAsListOfString(date: GregorianCalendar) : List<String> = listOf(
            (date.get(Calendar.MONTH) + 1).toString(),
            date.get(Calendar.DAY_OF_MONTH).toString(),
            date.get(Calendar.HOUR_OF_DAY).toString(),
            date.get(Calendar.MINUTE).toString()
    )

    private fun getSplitForStringDate(date: String) : List<String> = date.replace(" ", "").split(",")

    private fun getDateAsGregorianCalendar(date: String) : GregorianCalendar {
        val listDate = getSplitForStringDate(date)
        val year = GregorianCalendar().get(Calendar.YEAR)
        return GregorianCalendar(
                year,
                listDate[MONTH].toInt() - 1,
                listDate[DAY].toInt(),
                listDate[HOURS].toInt(),
                listDate[MINUTES].toInt()
        )
    }

    private fun getNameOfInstitution(auditorium: String) = auditorium.split('-')[0]
}