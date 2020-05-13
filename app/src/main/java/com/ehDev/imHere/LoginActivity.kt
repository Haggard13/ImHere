package com.ehDev.imHere

import android.content.Intent
import android.os.Bundle
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
    override fun onClick(v: View) {
        loginViewModel.viewModelScope.launch {

            val login = login_et.text.toString()
            val account = loginViewModel.getAccountByLogin(login)

            if (account.isAccountValid().not()) return@launch

            loginViewModel.saveAccountToSharedPrefs(account)

            val activity = when (account.status) {
                "1" -> AddInterviewActivity::class.java
                else -> StudentActivity::class.java
            }
            startActivityIntent(activity)
            super@LoginActivity.finish()
        }
    }

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private fun startActivityIntent(activity: Class<out Any>) = startActivity(Intent(this, activity))

    private fun AccountEntity?.isAccountValid(): Boolean = when {

        this == null -> {
            showToast("Неверный логин")
            false
        }

        this.password != getIntroducedPasswordHashCode() -> {
            showToast("Неверный пароль")
            false
        }

        else -> true
    }

    private fun getIntroducedPasswordHashCode() = password_et.text.toString().hashCode()
}