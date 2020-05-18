package com.ehDev.imHere.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.TabHost
import android.widget.TabHost.TabSpec
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.R
import com.ehDev.imHere.adapter.ScheduleAdapter
import com.ehDev.imHere.location.StudentLocationListener
import com.ehDev.imHere.vm.StudentViewModel
import kotlinx.android.synthetic.main.student_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import java.util.HashMap

private const val MONTH = 0
private const val DAY = 1
private const val HOURS = 2
private const val MINUTES = 3

class StudentActivity : AppCompatActivity() {

    private lateinit var studentViewModel: StudentViewModel
    private var referenceList = mutableListOf("")

    var locationManager: LocationManager? = null
    var locationStudent: Location? = null
    var locationRTF: Location? = null
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
        locationRTF = Location("locationManager")
        with(locationRTF!!) {
            latitude = 56.840750
            longitude = 60.650750
        }

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

            if (ActivityCompat.checkSelfPermission(
                    this@StudentActivity, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@StudentActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
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

            if (ActivityCompat.checkSelfPermission(
                    this@StudentActivity, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@StudentActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
                return@launch
            }

            val toastText = when (locationStudent!!.distanceTo(locationRTF) > 100) {
                true -> "СРОЧНО НА ПАРУ"
                false -> "ЗНАНИЕ - СИЛА"
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
            val adapterData = ArrayList<Map<String, Any?>>()
            var map: MutableMap<String, Any?>

            val from = arrayOf(
                getString(R.string.attribute_name_name),
                getString(R.string.attribute_name_who),
                getString(R.string.attribute_name_time),
                getString(R.string.attribute_name_reference)
            )
            val to = intArrayOf(
                R.id.interviewNameText,
                R.id.interviewWhoText,
                R.id.interviewTimeText,
                R.id.referenceText
            )

            val allInterviews = studentViewModel.getAllInterviews()
                .filter { it.interviewReference.isValidUrl() }
                .filter { it.filter == filter }
                .filter { it.filter == "682" }

            // todo: переписать и сделать норм адаптер
            allInterviews.forEach {

                map = HashMap()
                map[from[0]] = it.interviewer
                map[from[1]] = it.interviewee
                map[from[2]] = it.time
                map[from[3]] = it.interviewReference
                adapterData.add(map)
            }

            val referencesList = allInterviews.map { it.interviewReference }
            referenceList.addAll(referencesList)

            // todo: переписать на норм адаптер
            listViewInterview.adapter = SimpleAdapter(
                studentViewModel.getApplication(),
                adapterData,
                R.layout.interview_card,
                from,
                to
            )

            listViewInterview.onItemClickListener =
                AdapterView.OnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
//                    startActivity(Intent(ACTION_VIEW, Uri.parse(referenceList[position]))) // fixme: неправильно реализован
                    showToast("тип переход по клику")
                }
        }
    }

    private fun classCardCreate() {
        studentViewModel.viewModelScope.launch {

            val schedule = studentViewModel.getSchedule()

            schedule.forEach {
                val date = it.date.replace(" ", "").split(',')
                var fakeDate = getFakeDate(date) // todo: исправить
            }

            schedule_rv.adapter = ScheduleAdapter(schedule)
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
        .not() // fixme: нужно убрать not(). Оставляю пока, чтобы тестить было легче

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private fun getFakeDate(date: List<String>): List<String> = when (Integer.parseInt(date[MINUTES]) < 30) {

        true -> listOf(
            date[MONTH],
            date[DAY],
            (Integer.parseInt(date[HOURS]) - 2).toString(),
            (Integer.parseInt(date[MINUTES]) + 30).toString()
        )
        else -> listOf(
            date[MONTH],
            date[DAY],
            (Integer.parseInt(date[HOURS]) - 1).toString(),
            (Integer.parseInt(date[MINUTES]) - 30).toString()
        )
    }
}
