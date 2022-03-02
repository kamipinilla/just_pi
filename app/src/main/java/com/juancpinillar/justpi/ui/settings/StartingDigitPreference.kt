package com.juancpinillar.justpi.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.preference.DialogPreference
import android.util.AttributeSet
import com.juancpinillar.justpi.R

class StartingDigitPreference(context: Context, attrs: AttributeSet)
    : DialogPreference(context, attrs) {
    companion object {
        private const val DEFAULT_VALUE = 1
    }

    var startingDigit: Int = DEFAULT_VALUE
        set(value) {
            persistInt(value)
            field = value
        }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        updateStartingDigit(restorePersistedValue, defaultValue as Int?)
    }

    fun updateStartingDigit(restorePersistedValue: Boolean = true, defaultValue: Int? = DEFAULT_VALUE) {
        startingDigit = if (restorePersistedValue) getPersistedInt(DEFAULT_VALUE) else defaultValue!!
        val summaryPlaceholder: String = context.resources.getString(R.string.starting_digit_pref_summary_placeholder)
        val summary = String.format(summaryPlaceholder, startingDigit)
        this.summary = summary
    }

    override fun getDialogLayoutResource(): Int = R.layout.pref_starting_digit
}