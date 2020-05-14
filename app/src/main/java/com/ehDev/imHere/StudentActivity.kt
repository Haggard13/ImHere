package com.ehDev.imHere

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
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.TabHost
import android.widget.TabHost.TabSpec
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.student_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import java.util.HashMap

class StudentActivity : AppCompatActivity() {

    private var referenceList = mutableListOf("")

    var locationManager: LocationManager? = null
    var locationStudent: Location? = null
    var locationRTF: Location? = null
    var requestLocationUpdateMade = false
    var wifiMgr: WifiManager? = null

    // TODO: вынести логику
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

    //endregion
    //TODO: разнести
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_main)

        progressBar.visibility = View.INVISIBLE

        //region Location Block
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationRTF = Location("locationManager")
        with(locationRTF!!) {
            latitude = 56.840750
            longitude = 60.650750
        }
        ActivityCompat.requestPermissions(this@StudentActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        if (
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            with(locationManager!!) {
                requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            }
            requestLocationUpdateMade = true
        }
        else ActivityCompat.requestPermissions(
            this@StudentActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
        )
        //endregion
        wifiMgr = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        classCardCreate() //Заполнение всех View
        tabHostCreate()
        listViewCreate()
    }

    fun onCheckBtnClick(v: View) {

        GlobalScope.launch(Dispatchers.Main) {
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

            locationText.text = formatLocation(locationStudent)
            wifiText.text = wifiMgr?.connectionInfo?.ssid
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

    //region Methods For Filling Layout
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

    // TODO: жду Room и переписываю
    private fun listViewCreate() {
        val filter = getSharedPreferences("authentication", MODE_PRIVATE).getString("filter", "682")
        val dbh = DataBaseHelper(this)
        val db = dbh.writableDatabase
        val c = db.query("interviewTable", null, null, null, null, null, null)
        val data = ArrayList<Map<String, Any?>>(c.count)
        var m: MutableMap<String, Any?>
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
        while (c.moveToNext()) {
            if (!URLUtil.isValidUrl(c.getString(c.getColumnIndex("interview")))) continue
            if (c.getString(c.getColumnIndex("filter")) != filter && c.getString(
                    c.getColumnIndex("filter")
                ) != "682"
            ) continue
            m = HashMap()
            m[from[0]] = c.getString(c.getColumnIndex("name"))
            m[from[1]] = c.getString(c.getColumnIndex("who"))
            m[from[2]] = c.getString(c.getColumnIndex("time"))
            m[from[3]] = c.getString(c.getColumnIndex("interview"))
            data.add(m)
            if (referenceList[0].isEmpty()) referenceList[0] = c.getString(c.getColumnIndex("interview"))
            else referenceList.add(c.getString(c.getColumnIndex("interview")))
        }
        listViewInterview.adapter = SimpleAdapter(this, data, R.layout.interview_card, from, to)
        listViewInterview.onItemClickListener =
            AdapterView.OnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
                run {
                    startActivity(Intent(ACTION_VIEW, Uri.parse(referenceList[position])))
                }
            }
        c.close()
        dbh.close()
    }

    private fun classCardCreate() {
        classNameText.text = "Математика"
        classNumberText.text = "3"
        classTypeText.text = "Лекция"
        auditoryText.text = "ГУК-404"
        lecturerText.text = "Рыжкова Н. Г."
        timeText.text = "12:00"
    }

    //endregion
    //region Auxiliary Methods
    private fun formatLocation(location: Location?): String {
        return if (location == null) ""
        else String.format(
            "lat = %1$.6f, lon = %2$.6f", location.latitude, location.longitude
        )
    }

    @SuppressLint("MissingPermission")
    private fun makeRequestLocationUpdate() {
        with(locationManager!!) {
            requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        }
        requestLocationUpdateMade = true
    }
    //endregion

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}
