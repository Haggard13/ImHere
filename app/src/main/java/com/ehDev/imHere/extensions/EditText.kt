package com.ehDev.imHere.extensions

import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.Date

private const val DATE_PATTERN = "dd.MM.yyyy"

val EditText.textAsString
    get() = text.toString()

fun EditText.isEnteredDateLessThanCurrent(): Boolean {

    val pattern = DATE_PATTERN
    val dateFormat = SimpleDateFormat(pattern)
    val currentDate = dateFormat.parse(dateFormat.format(Date()))!!
    val etDate = dateFormat.parse(textAsString)!!

    return (currentDate >= etDate)
}