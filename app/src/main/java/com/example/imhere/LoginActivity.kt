package com.example.imhere

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var loginButton: Button
    private var loginText: EditText? = null
    private var passwordText: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginButton = findViewById(R.id.loginButton)
        loginText = findViewById(R.id.loginText)
        passwordText = findViewById(R.id.passwordText)
        loginButton.setOnClickListener(this)
    }

    /*По клику - проверка данных с данными в базе данных
    * Механизм на случай, если не выйдет с ЛК
    * + готовая форма аутентификации*/
    override fun onClick(v: View) {
        val loginStr = loginText!!.text.toString()
        val passwordHashCode = passwordText!!.text.toString().hashCode()
        val dbHelper = DataBaseHelper(this)
        val db = dbHelper.writableDatabase
        val c = db.query("accountTable", null, "login == ?", arrayOf(loginStr), null, null, null)
        if (c == null || c.count == 0) {
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
            return
        }
        c.moveToFirst()
        val rightPasswordHashCode = c.getInt(c.getColumnIndex("password"))
        if (passwordHashCode != rightPasswordHashCode) {
            Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
            return
        }
        c.close()
        dbHelper.close()
        val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("authentication", true)
        ed.apply()
        super.finish()
    }

    override fun onBackPressed() {
    }
}