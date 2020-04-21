package com.example.imhere;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.concurrent.TimeUnit;

public class PreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        ScreenDelayTask screenDelayTask = new ScreenDelayTask();
        screenDelayTask.execute();
    }

    public class ScreenDelayTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            SharedPreferences sp = getSharedPreferences("authentication", MODE_PRIVATE);
            if (!sp.contains("authentication") || !sp.getBoolean("authentication", true)){
                startActivity(new Intent(PreviewActivity.this, LoginActivity.class));
                PreviewActivity.super.finish();
            }
            else {
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("button_lock", true);

                PreviewActivity.super.finish();
            }
        }
    }
}
