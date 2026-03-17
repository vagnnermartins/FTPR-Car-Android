package com.example.myapitest.utils

import android.text.Editable
import android.text.TextWatcher

class MaskWatcher(private val mask: String) : TextWatcher {
    private var isUpdating = false
    private var oldText = ""

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val str = unmask(s.toString())
        var masked = ""
        if (isUpdating) {
            oldText = str
            isUpdating = false
            return
        }
        var i = 0
        for (m in mask.toCharArray()) {
            if (m != '#' && m != 'A' && m != '*') {
                if (str.length > oldText.length) {
                    masked += m
                    continue
                }
            }
            try {
                masked += str[i]
            } catch (e: Exception) {
                break
            }
            i++
        }
        isUpdating = true
        // Return text to the field
        if (s is Editable) {
            s.replace(0, s.length, masked)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable?) {}

    companion object {
        fun unmask(s: String): String {
            return s.replace("[^A-Za-z0-9]".toRegex(), "").uppercase()
        }
    }
}
