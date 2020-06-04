package com.ehDev.imHere.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager

class GpsSwitchBroadcastReceiver(

    private val onGPSEnabledAction: () -> Unit,
    private val onGPSDisabledAction: () -> Unit

) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action!!.matches(Regex("android.location.PROVIDERS_CHANGED"))) {

            val locationManager: LocationManager =
                context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            when (isGpsEnabled) {

                true -> onGPSEnabledAction.invoke()
                false -> onGPSDisabledAction.invoke()
            }
        }
    }
}