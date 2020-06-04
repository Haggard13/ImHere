package com.ehDev.imHere.utils.location

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient

class OnLocationFailedListenerImpl(private val onFailedAction: () -> Unit) : GoogleApiClient.OnConnectionFailedListener {

    override fun onConnectionFailed(result: ConnectionResult) = onFailedAction.invoke()
}