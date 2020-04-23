package com.example.imhere

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class PreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        val screenDelayTask = ScreenDelayTask()
        screenDelayTask.execute()
    }

    inner class ScreenDelayTask : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                TimeUnit.SECONDS.sleep(1)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)
            if (!sp.contains("authentication") || !sp.getBoolean("authentication", true)) {
                startActivity(Intent(this@PreviewActivity, LoginActivity::class.java))
                super@PreviewActivity.finish()
            } else {
                val ed = sp.edit()
                ed.putBoolean("button_lock", true)
                super@PreviewActivity.finish()
            }
        }
    }
}