package com.example.imhere

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import java.lang.StringBuilder

class AddInterviewActivity : AppCompatActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    //region Property Declaration
    var editTextReference: EditText? = null
    lateinit var switchAllStudents: Switch
    lateinit var spinnerCourses: Spinner
    lateinit var spinnerInstitution: Spinner
    lateinit var spinnerStudentsUnion: Spinner
    lateinit var buttonAddInterview: Button
    lateinit var buttonExit: Button
    var reference: String? = null
    //endregion

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_interview)
        //region Property Initializing
        editTextReference = findViewById(R.id.editTextReference)
        switchAllStudents = findViewById(R.id.switchAllStudents)
        spinnerCourses = findViewById(R.id.spinnerCourses)
        spinnerCourses.setSelection(6)
        spinnerInstitution = findViewById(R.id.spinnerInstitutions)
        spinnerInstitution.setSelection(8)
        spinnerStudentsUnion = findViewById(R.id.spinnerStudentsUnion)
        spinnerStudentsUnion.setSelection(2)
        buttonAddInterview = findViewById(R.id.buttonAddInterview)
        buttonExit = findViewById(R.id.buttonExit2)
        //endregion
        setStateSpinner(false)
        switchAllStudents.setOnCheckedChangeListener(this)
        buttonAddInterview.setOnClickListener(this)
        buttonExit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.buttonAddInterview -> {
                reference = editTextReference!!.text.toString()
                when {
                    reference.isNullOrEmpty() -> {
                        Toast.makeText(this, "Укажите ссылку", Toast.LENGTH_LONG).show()
                        return@onClick
                    }
                    !tryReference() -> {
                        Toast.makeText(this, "Ссылка некорректна", Toast.LENGTH_LONG).show()
                        return@onClick
                    }
                }
                val dbh = DataBaseHelper(this)
                val db = dbh.writableDatabase
                val cv = ContentValues()
                with(cv) {
                    put("interview", reference)
                    put("filter", getStudentFilter())
                }
                db.insert("interviewTable", null, cv)
                dbh.close()
                Toast.makeText(this, "Опрос успешно добавлен", Toast.LENGTH_LONG).show()
            }
            R.id.buttonExit2 -> {
                val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)
                sp.edit().clear().apply()
                startActivity(Intent(this, LoginActivity::class.java))
                super@AddInterviewActivity.finish()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) = if(isChecked) setStateSpinner(false) else setStateSpinner(true)

    private fun setStateSpinner(state: Boolean){
        spinnerCourses.isEnabled = state
        spinnerInstitution.isEnabled = state
        spinnerStudentsUnion.isEnabled = state
    } //Вкл - выкл спиннеры

    //Проверка ссылки на форму
    private fun tryReference(): Boolean {
        val regexShort = Regex("""https://forms\.gle/.+""")
        val regexLong = Regex("""https://docs\.google\.com/forms/d/e/.+/viewform\?usp=sf_link""")
        return reference!!.matches(regexShort) || reference!!.matches(regexLong)
    }
    //Получение фильтра для выбора получателей
    private fun getStudentFilter(): String {
        if (switchAllStudents.isChecked) return "6822"
        val filter = StringBuilder()
        filter.append(spinnerCourses.selectedItemPosition)
        filter.append(spinnerInstitution.selectedItemPosition)
        filter.append(spinnerStudentsUnion.selectedItemPosition)
        return filter.toString()
    }
}
