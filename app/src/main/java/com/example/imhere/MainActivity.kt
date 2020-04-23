package com.example.imhere

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.TabHost.TabSpec
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), View.OnClickListener {
    //region Arrays For List View
    var name = arrayOf("Как дела", "Универ", "Преподы", "Кто ты", "За путина", "Опрос", "Радик", "Радик")
    var type = arrayOf("временный", "постоянный", "временный", "постоянный", "временный", "постоянный", "временный", "временный")
    var who = arrayOf("радик", "Универ", "Преподы", "союз", "деканат", "декан", "преподы", "преподы")
    var time = arrayOf("12:00", "не ограничено", "12:00", "не ограничено", "12:00", "не ограничено", "12:00", "12:00")

    //endregion
    //region Field Declaration
    var listViewInterview: ListView? = null
    lateinit var checkButton: Button
    lateinit var exitButton: Button
    var locationText: TextView? = null
    var wifiText: TextView? = null
    var classImage: ImageView? = null
    var classNameText: TextView? = null
    var classNumberText: TextView? = null
    var classTypeText: TextView? = null
    var auditoryText: TextView? = null
    var lecturerText: TextView? = null
    var timeText: TextView? = null
    var locationManager: LocationManager? = null
    var locationStudent: Location? = null
    var locationRTF: Location? = null
    var locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationStudent = location
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {
            if (ActivityCompat.checkSelfPermission(baseContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            locationManager!!.getLastKnownLocation(provider)
        }

        override fun onProviderDisabled(provider: String) {}
    }
    var wifiInfo: WifiInfo? = null

    //endregion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //region Field Initializing
        checkButton = findViewById(R.id.checkButton) //Инициализация основных View.
        exitButton = findViewById(R.id.exitButton)
        listViewInterview = findViewById(R.id.listViewInteview)
        wifiText = findViewById(R.id.wifiText)
        locationText = findViewById(R.id.locationText)
        classImage = findViewById(R.id.classImage) //Инициализация элементов карточки в информацией
        classNameText = findViewById(R.id.classNameText) //о ближайшей паре. Для нее нужно спрасить
        classNumberText = findViewById(R.id.classNumberText) //расписание из ЛК.
        classTypeText = findViewById(R.id.classTypeText)
        auditoryText = findViewById(R.id.auditoryText)
        lecturerText = findViewById(R.id.lecturerText)
        timeText = findViewById(R.id.timeText)
        //endregion
        checkButton.setOnClickListener (this)
        exitButton.setOnClickListener(this)

        //region Location Block
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationRTF = Location("locationManager")
        locationRTF!!.latitude = 56.840750
        locationRTF!!.longitude = 60.650750
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
        }
        //endregion
        //region WiFi Block
        val wifiMgr = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiInfo = wifiMgr.connectionInfo
        //endregion
        classCardCreate() //Заполнение всех View
        tabHostCreate()
        listViewCreate()
        startActivity(Intent(this, PreviewActivity::class.java)) //Запуск превью
    }

    override fun onResume() {
        super.onResume()
        val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)
        if (sp.contains("button_lock") && sp.getBoolean("button_lock", true)) {
            val mt = ButtonLockTask()
            mt.execute()
        }
        val ed = sp.edit()
        ed.putBoolean("task", false)
        ed.apply()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.checkButton -> {
                if (wifiInfo == null) {
                    Toast.makeText(this, "Нет подключения", Toast.LENGTH_LONG).show()
                    return
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                    return
                }
                if (locationStudent != null && locationStudent!!.distanceTo(locationRTF) > 100) Toast.makeText(this, "СРОЧНО НА ПАРУ", Toast.LENGTH_LONG).show() else if (locationStudent != null) Toast.makeText(this, "ЗНАНИЕ - СИЛА", Toast.LENGTH_LONG).show()
                locationText!!.text = formatLocation(locationStudent)
                wifiText!!.text = wifiInfo!!.ssid
            }
            R.id.exitButton -> {
                val sh = getSharedPreferences("authentication", Context.MODE_PRIVATE)
                val e = sh.edit()
                e.putBoolean("authentication", false)
                e.apply()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    //region Methods For Filling Layout
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
        val data = ArrayList<Map<String, Any?>>(name.size)
        var m: MutableMap<String, Any?>
        val from = arrayOf(getString(R.string.attribute_name_name),
                getString(R.string.attribute_name_type),
                getString(R.string.attribute_name_who),
                getString(R.string.attribute_name_time),
                getString(R.string.attribute_name_img))
        val to = intArrayOf(R.id.interviewNameText,
                R.id.interviewTypeText,
                R.id.interviewWhoText,
                R.id.interviewTimeText,
                R.id.interviewImage)
        for (i in name.indices) {
            m = HashMap()
            m[from[0]] = name[i]
            m[from[1]] = type[i]
            m[from[2]] = who[i]
            m[from[3]] = time[i]
            m[from[4]] = R.mipmap.ic_launcher
            data.add(m)
        }
        listViewInterview!!.adapter = SimpleAdapter(this, data, R.layout.interview_card, from, to)
    }

    fun classCardCreate() {
        classNameText!!.text = "Математика"
        classNumberText!!.text = "3"
        classTypeText!!.text = "Лекция"
        auditoryText!!.text = "ГУК-404"
        lecturerText!!.text = "Рыжкова Н. Г."
        timeText!!.text = "12:00"
    }

    //endregion
    //region Auxiliary Methods
    private fun formatLocation(location: Location?): String {
        return if (location == null) "" else String.format("lat = %1$.6f, lon = %2$.6f", location.latitude, location.longitude)
    }

    private inner class ButtonLockTask : AsyncTask<Void?, Void?, Void?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            checkButton!!.setBackgroundColor(resources.getColor(R.color.colorOff))
            checkButton!!.isEnabled = false
        }

        override fun doInBackground(vararg params: Void?): Void? {
            while (locationStudent == null) {
                try {
                    TimeUnit.SECONDS.sleep(1)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            checkButton!!.setBackgroundColor(resources.getColor(R.color.colorOn))
            checkButton!!.isEnabled = true
        }
    } //endregion
}
