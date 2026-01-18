package com.utfpr.psil.cartrack.ui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val formattedText = buildString {
            for (i in originalText.indices) {
                if (i == 0) append("(")
                append(originalText[i])
                if (i == 1) append(") ")
                if (originalText.length <= 10) {
                    if (i == 5) append("-")
                } else {
                    if (i == 6) append("-")
                }
            }
        }

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val transformedOffset = when {
                    offset <= 2 -> offset + 1
                    offset <= 6 -> offset + 3
                    originalText.length <= 10 -> offset + 4
                    offset <= 7 -> offset + 3
                    else -> offset + 4
                }
                return transformedOffset.coerceAtMost(formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val originalOffset = when {
                    offset <= 1 -> 0
                    offset <= 4 -> offset - 1
                    offset <= 9 -> offset - 3
                    else -> offset - 4
                }
                return originalOffset.coerceIn(0, originalText.length)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetTranslator)
    }
}