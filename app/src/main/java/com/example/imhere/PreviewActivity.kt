package com.example.imhere

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
            delay(2000)
            withContext(Dispatchers.Main){
                val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)
                if (!sp.contains("authentication") || !sp.getBoolean("authentication", true)) {
                    startActivity(Intent(this@PreviewActivity, LoginActivity::class.java))
                    super@PreviewActivity.finish()
                } else super@PreviewActivity.finish()
            }
        }
    }
}