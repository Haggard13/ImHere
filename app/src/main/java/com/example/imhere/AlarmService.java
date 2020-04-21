package com.example.imhere;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

public class AlarmService extends Service {
    public AlarmService() {
    }

    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    long time = SystemClock.elapsedRealtime() + 5000;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
