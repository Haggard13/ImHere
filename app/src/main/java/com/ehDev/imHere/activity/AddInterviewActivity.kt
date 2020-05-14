package com.ehDev.imHere.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.R
import com.ehDev.imHere.db.entity.InterviewEntity
import com.ehDev.imHere.extensions.textAsString
import com.ehDev.imHere.vm.AddInterviewViewModel
import kotlinx.android.synthetic.main.activity_add_interview.*
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class AddInterviewActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private lateinit var addInterviewViewModel: AddInterviewViewModel
    private var interviewReference: String? = null

    // TODO: разнести логику
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_interview)

        addInterviewViewModel = ViewModelProvider(this).get(AddInterviewViewModel::class.java)

        spinnerCourses.setSelection(6)
        spinnerInstitutions.setSelection(8)
        spinnerStudentsUnion.setSelection(2)
        setStateSpinner(false)

        switchAllStudents.setOnCheckedChangeListener(this)
    }

    fun onAddInterviewBtnClick(v: View) {
        addInterviewViewModel.viewModelScope.launch {
            val interviewReference = editTextReference.textAsString

            when {
                interviewReference.isNullOrEmpty() -> {
                    showToast("Укажите ссылку")
                    return@launch
                }
                interviewReference.isReferenceValid().not() -> {
                    showToast("Ссылка некорректна")
                    return@launch
                }
            }

            val interview = InterviewEntity(
                interviewReference = interviewReference,
                interviewer = editTextAuthor.textAsString,
                interviewee = editTextName.textAsString,
                filter = getStudentFilter(),
                time = editTextTime.textAsString
            )

            try {
                addInterviewViewModel.insertInterview(interview)
            } catch (error: SQLiteConstraintException) {
                showToast("Опрос уже существует")
                return@launch
            }
            showToast("Опрос успешно добавлен")
        }
    }

    fun onExitInterviewBtnClick(v: View) {

        val sp = getSharedPreferences("authentication", Context.MODE_PRIVATE)
        sp.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        super@AddInterviewActivity.finish()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) = when (isChecked) {

        true -> setStateSpinner(false)
        false -> setStateSpinner(true)
    }

    private fun setStateSpinner(state: Boolean){
        spinnerCourses.isEnabled = state
        spinnerInstitutions.isEnabled = state
        spinnerStudentsUnion.isEnabled = state
    }

    //Проверка ссылки на форму
    // TODO: порефачить
    private fun String.isReferenceValid(): Boolean {
        val regexShort = Regex("""https://forms\.gle/.+""") // fixme
        val regexLong = Regex("""https://docs\.google\.com/forms/d/e/.+/viewform\?usp=sf_link""") // fixme:
        // паттерн не всегда корректно срабатывает
        // пример - https://docs.google.com/forms/d/e/1FAIpQLSf_Z1OkM1lFStBPrQP1lNNv6KnvEyQVNRz61HbeQ-l8unafLw/viewform
        return (matches(regexShort) || matches(regexLong)) && URLUtil.isValidUrl(interviewReference)
    }

    //Получение фильтра для выбора получателей
    private fun getStudentFilter() = when (switchAllStudents.isChecked) {

        true -> "682"
        else -> "${spinnerCourses.selectedItemPosition}" +
                "${spinnerInstitutions.selectedItemPosition}" +
                "${spinnerStudentsUnion.selectedItemPosition}"
    }

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}