package com.ehDev.imHere.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ehDev.imHere.R
import com.ehDev.imHere.data.PersonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

                    false -> startActivityIntent(LoginActivity::class.java)

                    true -> when (sp.getString("personType", PersonType.STUDENT.name) == PersonType.STUDENT.name) {
                        true -> startActivityIntent(StudentActivity::class.java)
                        else -> startActivityIntent(AddInterviewActivity::class.java)
                    }
                }

                super@PreviewActivity.finish()
            }
        }
    }

    private fun startActivityIntent(activity: Class<out Any>) = startActivity(Intent(this, activity))
}