package com.juancpinillar.justpi

import android.content.Context
import java.util.regex.Pattern

private val digitPattern: Pattern = Pattern.compile("""\d""")

fun getDigitDrawableId(context: Context, digit: String): Int {
    return if (isDigit(digit)) {
        context.resources.getIdentifier("digit$digit", "drawable", context.packageName)
    } else {
        throw IllegalArgumentException("Must be a single digit: $digit")
    }
}

fun getDigitId(context: Context, digit: String): Int {
    return if (isDigit(digit)) {
        context.resources.getIdentifier("digit$digit", "id", context.packageName)
    } else {
        throw IllegalArgumentException("Must be a single digit: $digit")
    }
}

private fun isDigit(digit: String) = digitPattern.matcher(digit).matches()
