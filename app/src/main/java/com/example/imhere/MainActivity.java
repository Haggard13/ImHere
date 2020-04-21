package com.example.imhere;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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
    //region Arrays For List View
    String[] name = {"Как дела", "Универ", "Преподы", "Кто ты", "За путина", "Опрос", "Радик", "Радик"};
    String[] type = {"временный", "постоянный", "временный", "постоянный", "временный", "постоянный", "временный", "временный"};
    String[] who = {"радик", "Универ", "Преподы", "союз", "деканат", "декан", "преподы", "преподы"};
    String[] time = {"12:00", "не ограничено", "12:00", "не ограничено", "12:00", "не ограничено", "12:00", "12:00"};
    //endregion
    //region Field Declaration
    ListView listViewInterview;

    Button checkButton;
    Button exitButton;

    TextView locationText;
    TextView wifiText;

    ImageView classImage;
    TextView classNameText;
    TextView classNumberText;
    TextView classTypeText;
    TextView auditoryText;
    TextView lecturerText;
    TextView timeText;

    LocationManager locationManager;
    Location locationStudent;
    Location locationRTF;
    LocationListener locationListener = new LocationListener() {
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

    WifiInfo wifiInfo;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region Field Initializing
        checkButton = findViewById(R.id.checkButton); //Инициализация основных View.
        exitButton = findViewById(R.id.exitButton);
        listViewInterview = findViewById(R.id.listViewInteview);
        wifiText = findViewById(R.id.wifiText);
        locationText = findViewById(R.id.locationText);

        classImage = findViewById(R.id.classImage); //Инициализация элементов карточки в информацией
        classNameText = findViewById(R.id.classNameText);//о ближайшей паре. Для нее нужно спрасить
        classNumberText = findViewById(R.id.classNumberText);//расписание из ЛК.
        classTypeText = findViewById(R.id.classTypeText);
        auditoryText = findViewById(R.id.auditoryText);
        lecturerText = findViewById(R.id.lecturerText);
        timeText = findViewById(R.id.timeText);
        //endregion

        checkButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);

        //region Location Block
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
        //region WiFi Block
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiInfo = wifiMgr.getConnectionInfo();
        //endregion

        classCardCreate();//Заполнение всех View
        tabHostCreate();
        listViewCreate();

        startActivity(new Intent(this, PreviewActivity.class)); //Запуск превью
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("authentication", MODE_PRIVATE);
        if (sp.contains("button_lock") && sp.getBoolean("button_lock", true)) {
            ButtonLockTask mt = new ButtonLockTask();
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
                SharedPreferences sh = getSharedPreferences("authentication", MODE_PRIVATE);
                SharedPreferences.Editor e = sh.edit();
                e.putBoolean("authentication", false);
                e.apply();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }

    //region Methods For Filling Layout
    private void tabHostCreate() {
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
    }

    private void listViewCreate() {
        ArrayList<Map<String, Object>> data = new ArrayList<>(name.length);
        Map<String, Object> m;
        String[] from = { getString(R.string.attribute_name_name),
                getString(R.string.attribute_name_type),
                getString(R.string.attribute_name_who),
                getString(R.string.attribute_name_time),
                getString(R.string.attribute_name_img)};
        int[] to = {R.id.interviewNameText,
                R.id.interviewTypeText,
                R.id.interviewWhoText,
                R.id.interviewTimeText,
                R.id.interviewImage};
        for (int i = 0; i < name.length; i++) {
            m = new HashMap<>();
            m.put(from[0], name[i]);
            m.put(from[1], type[i]);
            m.put(from[2], who[i]);
            m.put(from[3], time[i]);
            m.put(from[4], R.mipmap.ic_launcher);
            data.add(m);
        }
        listViewInterview.setAdapter(new SimpleAdapter(this, data, R.layout.interview_card, from, to));
    }

    public void classCardCreate() {
        classNameText.setText("Математика");
        classNumberText.setText("3");
        classTypeText.setText("Лекция");
        auditoryText.setText("ГУК-404");
        lecturerText.setText("Рыжкова Н. Г.");
        timeText.setText("12:00");
    }
    //endregion
    //region Auxiliary Methods
    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format("lat = %1$.6f, lon = %2$.6f", location.getLatitude(), location.getLongitude());
    }

    private class ButtonLockTask extends AsyncTask<Void, Void, Void> {

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


