package com.ehDev.imHere.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat

class StudentLocationListener(

    var locationStudent: Location?,
    val baseContext: Context,
    val locationManager: LocationManager?

) : LocationListener {

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