package uz.oybek.wedriveevaluation.presentation.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.math.min

class CardNumberVisualTransformation(
    private val separator: String = " "
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {

        val trimmed = text.text.filter { it.isDigit() }.take(16)


        val formatted = buildString {
            for (i in trimmed.indices) {
                append(trimmed[i])

                if (i % 4 == 3 && i != trimmed.lastIndex) {
                    append(separator)
                }
            }
        }
        val transformedText = AnnotatedString(formatted)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val separatorsBeforeOffset = (offset - 1).coerceAtLeast(0) / 4
                val transformedOffset = offset + separatorsBeforeOffset

                return transformedOffset.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val separatorsUpToOffset = (offset - 1).coerceAtLeast(0) / 5 // 4 digits + 1 space
                val originalOffset = offset - separatorsUpToOffset
                return originalOffset.coerceIn(0, trimmed.length)
            }
        }

        return TransformedText(transformedText, offsetMapping)
    }
}

class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.filter { it.isDigit() }.take(4)
        val out = buildString {
            if (trimmed.isNotEmpty()) { append(trimmed.substring(0, minOf(2, trimmed.length))) }
            if (trimmed.length >= 2) { append("/"); if (trimmed.length > 2) { append(trimmed.substring(2)) } }
        }
        val transformedText = AnnotatedString(out)
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = (if (offset >= 2) offset + 1 else offset).coerceIn(0, out.length)
            override fun transformedToOriginal(offset: Int): Int = (if (offset >= 3) offset - 1 else offset).coerceIn(0, trimmed.length)
        }
        return TransformedText(transformedText, offsetMapping)
    }
}