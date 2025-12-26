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
            return TransformedText(text, OffsetMapping.Identity)
        }

        val number = originalText.toLongOrNull() ?: return TransformedText(text, OffsetMapping.Identity)
        val formattedText = formatter.format(number)


        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val originalTextBeforeCursor = originalText.take(offset)
                val longBeforeCursor = originalTextBeforeCursor.toLongOrNull() ?: 0
                val commasBeforeCursor = formatter.format(longBeforeCursor).count { !it.isDigit() }

                return offset + commasBeforeCursor
            }

            override fun transformedToOriginal(offset: Int): Int {
                val commasBefore = formattedText.take(offset).count { !it.isDigit() }
                return (offset - commasBefore).coerceAtLeast(0)
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
