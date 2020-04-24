package com.example.imhere

import android.content.ContentValues
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
    lateinit var spinnerFormRefund: Spinner
    lateinit var spinnerStudentsUnion: Spinner
    lateinit var buttonAddInterview: Button
    var reference: String? = null
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_interview)
        //region Property Initializing
        editTextReference = findViewById(R.id.editTextReference)
        switchAllStudents = findViewById(R.id.switchAllStudents)
        spinnerCourses = findViewById(R.id.switchAllStudents)
        spinnerInstitution = findViewById(R.id.spinnerInstitutions)
        spinnerFormRefund = findViewById(R.id.spinnerFormRefund)
        spinnerStudentsUnion = findViewById(R.id.spinnerStudentsUnion)
        buttonAddInterview = findViewById(R.id.buttonAddInterview)
        //endregion
        setStateSpinner(false)
        switchAllStudents.setOnCheckedChangeListener(this)
        buttonAddInterview.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
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
        with(cv){
            put("interview", reference)
            put("filter", getStudentFilter())
        }
        db.insert("interviewTable",null, cv)
        dbh.close()
        Toast.makeText(this, "Опрос успешно добавлен", Toast.LENGTH_LONG).show()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) = if(isChecked) setStateSpinner(false) else setStateSpinner(true)

    private fun setStateSpinner(state: Boolean){
        spinnerCourses.isEnabled = state
        spinnerFormRefund.isEnabled = state
        spinnerInstitution.isEnabled = state
        spinnerStudentsUnion.isEnabled = state
    } //Вкл - выкл спиннеры

    //Проверка ссылки на форму
    private fun tryReference(): Boolean {
        //TODO: реализовать проверку адресса
        return true
    }
    //Получение фильтра для выбора получателей
    private fun getStudentFilter(): String {
        if (switchAllStudents.isChecked) return "6822"
        val filter = StringBuilder()
        filter.append(spinnerCourses.selectedItemPosition)
        filter.append(spinnerInstitution.selectedItemPosition)
        filter.append(spinnerFormRefund.selectedItemPosition)
        filter.append(spinnerStudentsUnion.selectedItemPosition)
        return filter.toString()
    }
}
