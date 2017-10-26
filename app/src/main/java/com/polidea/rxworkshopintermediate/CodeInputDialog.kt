package com.polidea.rxworkshopintermediate

import android.content.Context
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


class CodeInputDialog(context: Context, message: String = "Enter code:", newCodeCallback: (String) -> Unit) : AlertDialog(context, false, null) {

    private val editText = EditText(context).apply {
        inputType = InputType.TYPE_CLASS_NUMBER
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                newCodeCallback.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                this.post {
                    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
    }

    init {
        setView(editText)
        setMessage(message)
    }

    override fun onStart() {
        super.onStart()
        editText.requestFocus()
    }
}