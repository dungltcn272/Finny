package com.ltcn272.finny.presentation.common.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class NumberCommaVisualTransformation : VisualTransformation {

    private val formatter = NumberFormat.getNumberInstance(Locale.getDefault())

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isBlank()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val formattedText = try {
            val number = originalText.toLong()
            formatter.format(number)
        } catch (e: Exception) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val commasBeforeOffset = formattedText.take(offset + formattedText.count { it == ',' || it == '.' }).count { !it.isDigit() }
                return (offset + commasBeforeOffset).coerceIn(0, formattedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val commasBeforeOffset = formattedText.take(offset).count { !it.isDigit() }
                return (offset - commasBeforeOffset).coerceIn(0, originalText.length)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
