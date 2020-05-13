package com.ehDev.imHere

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.db.entity.AccountEntity
import com.ehDev.imHere.vm.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var loginViewModel: LoginViewModel
    var account: AccountEntity? = AccountEntity("", 1, "", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        loginButton.setOnClickListener(this)
    }

    /**
     * По клику - проверка данных с данными в базе данных
     * Механизм на случай, если не выйдет с ЛК
     * + готовая форма аутентификации
     * */
    // TODO: переписать
    // TODO: затащить Room
    // TODO: разнести логику для бд в отдельный файл
    override fun onClick(v: View) {

        val loginStr = login_et.text.toString()
        val passwordHashCode = password_et.text.toString().hashCode()
        val status: Int
        val filter: String
        val dbHelper = DataBaseHelper(this)
        val db = dbHelper.writableDatabase

        loginViewModel.viewModelScope.launch {

            account = loginViewModel.getAccountByLogin(loginStr)
            Log.i("my tag", "account: $account")

            when {

                account == null -> {
                    showToast("Неверный логин")
                    return@launch
                }

                account?.password != passwordHashCode -> {
                    showToast("Неверный пароль")
                    return@launch
                }

                else -> {
                    showToast("ебать, сработало")
                    return@launch
                }
            }
        }

        // ищем в бд чет по логину
        val c = db.query("accountTable", null, "login == ?", arrayOf(loginStr), null, null, null)
        if (c == null || c.count == 0) {
            showToast("Неверный логин или пароль") // fixme: тут ток логин проверяется
            return
        }
        c.moveToFirst()
        val rightPasswordHashCode = c.getInt(c.getColumnIndex("password"))
        if (passwordHashCode != rightPasswordHashCode) {
            showToast("Неверный логин или пароль")
            return
        }
        status = c.getInt(c.getColumnIndex("status"))
        filter = c.getString(c.getColumnIndex("filter"))
        c.close()
        dbHelper.close()

        // записываем инфу с логином в шаредпрефы
        val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)
        val ed = sp.edit()
        with(ed) {
            putBoolean("authentication", true)
            putInt("status", status)
            putString("filter", filter)
            apply()
        }

        // в зависимости от статуса переходим дальше
        val activity = when (status) {
            1 -> AddInterviewActivity::class.java
            else -> StudentActivity::class.java
        }
        startActivityIntent(activity)
        super@LoginActivity.finish()
    }

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private fun startActivityIntent(activity: Class<out Any>) = startActivity(Intent(this, activity))
}