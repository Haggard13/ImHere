package com.ehDev.imHere.repository

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.ehDev.imHere.utils.location.ConnectionCallbacksImpl
import com.ehDev.imHere.utils.location.OnLocationFailedListenerImpl
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

private const val UPDATE_INTERVAL: Long = 5000
private const val FASTEST_INTERVAL: Long = 5000

class LocationFacade(

    private val activity: Activity?,
    private val context: Context,
    private val showToastCallback: (String) -> Unit

) {

    var googleApiClient: GoogleApiClient? = GoogleApiClient.Builder(context)
        .addApi(LocationServices.API)
        .addConnectionCallbacks(
            ConnectionCallbacksImpl { tryToConnectGPSServices() }
        )
        .addOnConnectionFailedListener(
            OnLocationFailedListenerImpl { showToastCallback("Обнаружены проблемы с интернетом") }
        )
        .build()

    private var locationRequest: LocationRequest? = null
    var studentLocation: Location? = null

    fun tryToConnectGPSServices() {

        if ((hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) && hasPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )).not()
        ) {
            return
        }
        // todo: порефакторить
        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult?) {
                    showToastCallback(
                        "location got? longitude: ${result?.lastLocation?.latitude}, latitude: ${result?.lastLocation?.latitude}"
                    )
                    result?.let { studentLocation = it.lastLocation }
                }
            }, null)

        if (studentLocation != null) {
            showToastCallback("longitude: ${studentLocation?.longitude}, latitude: ${studentLocation?.latitude}")
        }
        startLocationUpdates()
    }

    fun startLocationUpdates() {

        locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)
        val locationTask = LocationServices.getSettingsClient(context)
            .checkLocationSettings(builder.build())
        locationTask.addOnCompleteListener {

            try {
                val response: LocationSettingsResponse? = locationTask.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {

                        val resolvable: ResolvableApiException = exception as ResolvableApiException
                        // Показываем диалог с помощью startResolutionForResult()
                        resolvable.startResolutionForResult(activity, LocationRequest.PRIORITY_HIGH_ACCURACY)
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Unit
                }
            }
        }

        if (
            (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    && hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)).not()
        ) {
            showToastCallback("You need to enable permissions to display location !") // todo: порефакторить
        }

        LocationServices.getFusedLocationProviderClient(context).lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { it: Location ->
                    showToastCallback(
                        "location updated? longitude: ${it.longitude}, latitude: ${it.latitude}"
                    ) // todo: порефакторить
                    studentLocation = it
                } ?: run {
                    showToastCallback("location failed?") // todo: порефакторить
                }
            }
    }

    fun hasPermission(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}