package com.example.imhere;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //region field
    String[] name = {"Как дела", "Универ", "Преподы", "Кто ты", "За путина", "Опрос", "Радик", "Радик"};
    String[] type = {"временный", "постоянный", "временный", "постоянный", "временный", "постоянный", "временный", "временный"};
    String[] who = {"радик", "Универ", "Преподы", "союз", "деканат", "декан", "преподы", "преподы"};
    String[] time = {"12:00", "не ограничено", "12:00", "не ограничено", "12:00", "не ограничено", "12:00", "12:00"};

    final String ATTRIBUTE_NAME_NAME = "textName";
    final String ATTRIBUTE_NAME_TYPE = "textType";
    final String ATTRIBUTE_NAME_WHO = "textWho";
    final String ATTRIBUTE_NAME_TIME = "textTime";
    final String ATTRIBUTE_NAME_IMG = "image";

    ListView listViewInterview;

    public Button checkButton;
    public Button exitButton;

    public TextView locationText;
    public TextView wifiText;

    public ImageView classImage;
    public TextView classNameText;
    public TextView classNumberText;
    public TextView classTypeText;
    public TextView auditoryText;
    public TextView lecturerText;
    public TextView timeText;

    private LocationManager locationManager;
    public Location locationStudent;
    private Location locationRTF;

    WifiInfo wifiInfo;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region Initializing
        checkButton = findViewById(R.id.checkButton);
        exitButton = findViewById(R.id.exitButton);

        checkButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);
        //endregion

        //region Location
        locationText = findViewById(R.id.locationText);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationRTF = new Location("locationManager");
        locationRTF.setLatitude(56.840750);
        locationRTF.setLongitude(60.650750);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
//endregion

        //region classCard
        classImage = findViewById(R.id.classImage);
        classNameText = findViewById(R.id.classNameText);
        classNumberText = findViewById(R.id.classNumberText);
        classTypeText = findViewById(R.id.classTypeText);
        auditoryText = findViewById(R.id.auditoryText);
        lecturerText = findViewById(R.id.lecturerText);
        timeText = findViewById(R.id.timeText);
        classFiling();
        //endregion

        //region Tab
        TabHost tabHost = findViewById(R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec;
        tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setIndicator("Отметка");
        tabSpec.setContent(R.id.tab1);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator("Опросы");
        tabSpec.setContent(R.id.tab2);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTabByTag("tag1");
        //endregion

        //region List
        ArrayList<Map<String, Object>> data = new ArrayList<>(name.length);
        Map<String, Object> m;
        for (int i = 0; i < name.length; i++) {
            m = new HashMap<>();
            m.put(ATTRIBUTE_NAME_NAME, name[i]);
            m.put(ATTRIBUTE_NAME_TYPE, type[i]);
            m.put(ATTRIBUTE_NAME_WHO, who[i]);
            m.put(ATTRIBUTE_NAME_TIME, time[i]);
            m.put(ATTRIBUTE_NAME_IMG, R.mipmap.ic_launcher);
            data.add(m);
        }

        String[] from = {ATTRIBUTE_NAME_NAME, ATTRIBUTE_NAME_TYPE, ATTRIBUTE_NAME_WHO, ATTRIBUTE_NAME_TIME, ATTRIBUTE_NAME_IMG};
        int[] to = {R.id.interviewNameText, R.id.interviewTypeText, R.id.interviewWhoText, R.id.interviewTimeText, R.id.interviewImage};

        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.interview_card, from, to);

        listViewInterview = findViewById(R.id.listViewInteview);
        listViewInterview.setAdapter(sAdapter);
        //endregion

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }

        wifiText = findViewById(R.id.wifiText);
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiInfo = wifiMgr.getConnectionInfo();

        startActivity(new Intent(this, PreviewActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("autoriz", MODE_PRIVATE);
        if (sp.contains("task") && sp.getBoolean("task", true)) {
            MyTask mt = new MyTask();
            mt.execute();
        }
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("task", false);
        ed.apply();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkButton:
                if (wifiInfo == null){
                    Toast.makeText(this, "Нет подключения", Toast.LENGTH_LONG).show();
                    return;
                }
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }
                if (locationStudent != null && locationStudent.distanceTo(locationRTF) > 100)
                    Toast.makeText(this, "СРОЧНО НА ПАРУ", Toast.LENGTH_LONG).show();
                else if (locationStudent != null)
                    Toast.makeText(this, "ЗНАНИЕ - СИЛА", Toast.LENGTH_LONG).show();
                locationText.setText(formatLocation(locationStudent));
                wifiText.setText(wifiInfo.getSSID());
                break;
            case R.id.exitButton:
                SharedPreferences sh = getSharedPreferences("autoriz", MODE_PRIVATE);
                SharedPreferences.Editor e = sh.edit();
                e.putBoolean("auton", false);
                e.apply();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }

    //region methods
    public void classFiling() {
        classNameText.setText("Математика");
        classNumberText.setText("3");
        classTypeText.setText("Лекция");
        auditoryText.setText("ГУК-404");
        lecturerText.setText("Рыжкова Н. Г.");
        timeText.setText("12:00");
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            locationStudent = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.getLastKnownLocation(provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format("lat = %1$.6f, lon = %2$.6f", location.getLatitude(), location.getLongitude());
    }

    public class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            checkButton.setBackgroundColor(getResources().getColor(R.color.colorOff));
            checkButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while(locationStudent == null){
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            checkButton.setBackgroundColor(getResources().getColor(R.color.colorOn));
            checkButton.setEnabled(true);
        }
    }
//endregion
}


