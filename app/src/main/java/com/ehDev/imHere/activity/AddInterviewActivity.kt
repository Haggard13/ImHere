package com.ehDev.imHere.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ehDev.imHere.R
import com.ehDev.imHere.data.filter.CourseType
import com.ehDev.imHere.data.filter.InstitutionType
import com.ehDev.imHere.data.filter.StudentInfo
import com.ehDev.imHere.data.filter.StudentUnionType
import com.ehDev.imHere.db.entity.InterviewEntity
import com.ehDev.imHere.extensions.textAsString
import com.ehDev.imHere.utils.AUTHENTICATION_SHARED_PREFS
import com.ehDev.imHere.utils.DateMaskedTextChangedListenerImpl
import com.ehDev.imHere.vm.AddInterviewViewModel
import kotlinx.android.synthetic.main.activity_add_interview.*
import kotlinx.coroutines.launch

class AddInterviewActivity : AppCompatActivity(),
    CompoundButton.OnCheckedChangeListener {

    private lateinit var addInterviewViewModel: AddInterviewViewModel

    // TODO: разнести логику
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_interview)

        addInterviewViewModel = ViewModelProvider(this).get(AddInterviewViewModel::class.java)

        val textChangedListener = DateMaskedTextChangedListenerImpl(interview_date_et)
        interview_date_et.addTextChangedListener(textChangedListener)
        interview_date_et.onFocusChangeListener = textChangedListener

        courses_spinner.setSelection(0)
        institutions_spinner.setSelection(0)
        students_union_spinner.setSelection(0)
        setStateSpinner(false)

        all_students_switch.setOnCheckedChangeListener(this)
    }

    fun onAddInterviewBtnClick(v: View) {
        addInterviewViewModel.viewModelScope.launch {
            val interviewReference = interview_reference_et.textAsString
            val interviewTime = interview_date_et.textAsString

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

            when {
                interviewTime.isValidTime().not() -> {
                    showToast("Неверный формат времени")
                    return@launch
                }
            }

            val filterInfo = getStudentFilter()

            val interview = InterviewEntity(
                interviewReference = interviewReference,
                interviewer = interview_author_et.textAsString,
                title = interview_title_et.textAsString,
                course = filterInfo.course.description,
                institution = filterInfo.institution.description,
                studentUnionInfo = filterInfo.studentUnionInfo.description,
                time = interview_date_et.textAsString
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

        val sp = getSharedPreferences(AUTHENTICATION_SHARED_PREFS, Context.MODE_PRIVATE)
        sp.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        super@AddInterviewActivity.finish()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) = when (isChecked) {

        true -> setStateSpinner(false)
        false -> setStateSpinner(true)
    }

    private fun setStateSpinner(state: Boolean) {

        courses_spinner.isEnabled = state
        institutions_spinner.isEnabled = state
        students_union_spinner.isEnabled = state
    }

    //Проверка ссылки на форму
    private fun String.isReferenceValid(): Boolean {

        val regexShort = Regex("""https://forms\.gle/.+""")
        val regexLong = Regex("""https://docs\.google\.com/forms/d/e/.+/viewform(\?usp=sf_link)?""")
        return ((matches(regexShort) || matches(
            regexLong
        ))) //&& URLUtil.isValidUrl(interviewReference)) Не пашет как надо
            .not() //fixme: убрать,  для тестов сделано так
    }

    private fun String.isValidTime(): Boolean {

        val regex = Regex("""\d\d/\d\d/\d\d \d\d:\d\d""")
        return matches(regex)
            .not()
    }

    //Получение фильтра для выбора получателей
    private fun getStudentFilter() = when (all_students_switch.isChecked) {

        true -> StudentInfo(
            course = CourseType.ALL_COURSES,
            institution = InstitutionType.ALL_INSTITUTIONS,
            studentUnionInfo = StudentUnionType.ALL_STUDENTS
        )
        else -> StudentInfo(
            course = CourseType.findCourseByDescription(courses_spinner.selectedItem.toString()),
            institution = InstitutionType.findInstituteByDescription(institutions_spinner.selectedItem.toString()),
            studentUnionInfo = StudentUnionType.findStudentUnionInfoByDescription(
                students_union_spinner.selectedItem.toString()
            )
        )
    }

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}