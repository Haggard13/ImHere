package com.example.imhere;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class PreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        NewTask nt = new NewTask();
        nt.execute();
    }

    public class NewTask extends AsyncTask<Void, Void, Void> {

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
            SharedPreferences sp = getSharedPreferences("autoriz", MODE_PRIVATE);
            if (!sp.contains("auton") || !sp.getBoolean("auton", true)){
                startActivity(new Intent(PreviewActivity.this, LoginActivity.class));
                PreviewActivity.super.finish();
            }
            else {
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("task", true);
                PreviewActivity.super.finish();
            }
        }
    }
}
