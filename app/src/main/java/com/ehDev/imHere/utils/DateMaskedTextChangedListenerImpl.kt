package com.ehDev.imHere.utils

import android.widget.EditText
import com.redmadrobot.inputmask.MaskedTextChangedListener
import java.text.SimpleDateFormat
import java.util.Date

class DateMaskedTextChangedListenerImpl(
    editText: EditText
) : MaskedTextChangedListener(
    primaryFormat = "[00]{.}[00]{.}[0000]",
    autocomplete = true,
    autoskip = false,
    field = editText,
    valueListener = object : ValueListener {

        override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {

            if (maskFilled) {
                val pattern = "dd.MM.yyyy"
                val dateFormat = SimpleDateFormat(pattern)
                val date = dateFormat.format(Date())
            }
        }
    }
)