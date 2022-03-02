package com.juancpinillar.justpi.model

import java.util.regex.Pattern

class PiGame(private val digits: String, startPosition: Int = 0) {
    init {
        val pattern: Pattern = Pattern.compile("""\d+""")
        val correctPattern: Boolean = pattern.matcher(digits).matches()
        if (!correctPattern) throw IllegalArgumentException("The digits don't have the correct format")
    }
    val numberOfDigits: Int = digits.length
    var startPosition: Int = 0
        set(value) {
            if (value in 0 until numberOfDigits) {
                field = value
            } else {
                throw IndexOutOfBoundsException("Start position must be in [0-${numberOfDigits - 1}]: $value")
            }
        }
    init {
        this.startPosition = startPosition
    }
    var position: Int = startPosition
        set(value) {
            if (value in startPosition..numberOfDigits) {
                field = value
            } else {
                throw IndexOutOfBoundsException("Position must be in [$startPosition-$numberOfDigits]: $value")
            }
        }
    private val digitPattern: Pattern = Pattern.compile("""\d""")

    val hasDigitsAvailable: Boolean
        get() = position != numberOfDigits

    fun restart() {
        position = startPosition
    }

    fun guessDigit(digit: String): Boolean {
        if (!hasDigitsAvailable) {
            throw IllegalStateException("No more digits to guess, $numberOfDigits digits available")
        }
        if (!digitPattern.matcher(digit).matches()) {
            throw IllegalArgumentException("Must be a single digit: $digit")
        }
        val correctGuess: Boolean = isCorrect(digit)
        if (correctGuess) {
            position++
        }
        return correctGuess
    }

    private fun isCorrect(digit: String): Boolean {
        if (!hasDigitsAvailable) {
            throw IllegalStateException("No more digits to guess, $numberOfDigits digits available")
        }
        return digit == digits[position].toString()
    }

    operator fun get(index: Int): String = digits[index].toString()
}