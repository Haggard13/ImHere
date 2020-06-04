package com.ehDev.imHere.utils.location

import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient

class ConnectionCallbacksImpl(private val onConnectedAction: () -> Unit) : GoogleApiClient.ConnectionCallbacks {

    override fun onConnected(p0: Bundle?) = onConnectedAction.invoke()

    override fun onConnectionSuspended(p0: Int) = Unit
}