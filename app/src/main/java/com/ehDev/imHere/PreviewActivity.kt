package com.ehDev.imHere

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
    }

    override fun onResume() {
        super.onResume()

        GlobalScope.launch {

            delay(1000)

            // TODO: разбить логику

            withContext(Dispatchers.Main) {

                val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)

                when (sp.contains("authentication") && sp.getBoolean("authentication", false)) {

                    false -> startActivity(Intent(this@PreviewActivity, LoginActivity::class.java))

                    true -> when (sp.getString("status", "0") == "0") {
                        true -> startActivity(Intent(this@PreviewActivity, StudentActivity::class.java))
                        else -> startActivity(Intent(this@PreviewActivity, AddInterviewActivity::class.java))
                    }
                }

                super@PreviewActivity.finish()
            }
        }
    }
}