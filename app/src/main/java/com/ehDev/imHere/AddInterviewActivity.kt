package com.ehDev.imHere

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.*
import kotlinx.android.synthetic.main.activity_add_interview.*
import java.lang.StringBuilder

class AddInterviewActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private var reference: String? = null

    // TODO: разнести логику
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_interview)

        spinnerCourses.setSelection(6)
        spinnerInstitutions.setSelection(8)
        spinnerStudentsUnion.setSelection(2)
        setStateSpinner(false)
        switchAllStudents.setOnCheckedChangeListener(this)
    }

    fun onAddInterviewBtnClick(v: View) {

        reference = editTextReference!!.text.toString()
        when {
            reference.isNullOrEmpty() -> {
                Toast.makeText(this, "Укажите ссылку", Toast.LENGTH_LONG).show()
                return
            }
            !tryReference() -> {
                Toast.makeText(this, "Ссылка некорректна", Toast.LENGTH_LONG).show()
                return
            }
        }
        val dbh = DataBaseHelper(this)
        val db = dbh.writableDatabase
        val cv = ContentValues()
        with(cv) {
            put("interview", reference)
            put("filter", getStudentFilter())
            put("who", editTextAuthor!!.text.toString())
            put("name", editTextName!!.text.toString())
            put("time", editTextTime!!.text.toString())
        }
        if(db.insert("interviewTable", null, cv) == (-1).toLong()) {
            Toast.makeText(this, "Опрос уже существует", Toast.LENGTH_LONG).show()
            dbh.close()
            return
        }
        dbh.close()
        Toast.makeText(this, "Опрос успешно добавлен", Toast.LENGTH_LONG).show()
    }

    fun onExitInterviewBtnClick(v: View) {

        val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)
        sp.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        super@AddInterviewActivity.finish()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) = if(isChecked) setStateSpinner(false) else setStateSpinner(true)

    private fun setStateSpinner(state: Boolean){
        spinnerCourses.isEnabled = state
        spinnerInstitutions.isEnabled = state
        spinnerStudentsUnion.isEnabled = state
    }

    //Проверка ссылки на форму
    // TODO: порефачить
    private fun tryReference(): Boolean {
        val regexShort = Regex("""https://forms\.gle/.+""")
        val regexLong = Regex("""https://docs\.google\.com/forms/d/e/.+/viewform\?usp=sf_link""")
        return (reference!!.matches(regexShort) || reference!!.matches(regexLong)) && URLUtil.isValidUrl(reference)
    }

    //Получение фильтра для выбора получателей
    // todo: рефачить
    private fun getStudentFilter(): String {
        if (switchAllStudents.isChecked) return "682" // todo: const
        val filter = StringBuilder()
        filter.append(spinnerCourses.selectedItemPosition)
        filter.append(spinnerInstitutions.selectedItemPosition)
        filter.append(spinnerStudentsUnion.selectedItemPosition)
        return filter.toString()
    }
}
