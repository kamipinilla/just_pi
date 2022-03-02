package com.juancpinillar.justpi.ui.settings

import android.os.Bundle
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.view.View
import android.widget.NumberPicker
import com.juancpinillar.justpi.R

class StartingDigitPreferenceDialogFragmentCompat
    : PreferenceDialogFragmentCompat() {
    private lateinit var numberPicker: NumberPicker

    companion object {
        fun newInstance(key: String): StartingDigitPreferenceDialogFragmentCompat {
            val fragment = StartingDigitPreferenceDialogFragmentCompat()
            val bundle = Bundle()
            bundle.putString(ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        numberPicker = view.findViewById(R.id.edit)

        val digits: String = resources.getString(R.string.digits)
        val numberOfDigits: Int = digits.length
        numberPicker.minValue = 1
        numberPicker.maxValue = numberOfDigits

        val startingDigitPreference = preference as StartingDigitPreference
        val startingDigit: Int = startingDigitPreference.startingDigit
        numberPicker.value = startingDigit
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val newStartingDigit: Int = numberPicker.value
            val startingDigitPreference = preference as StartingDigitPreference
            if (startingDigitPreference.callChangeListener(newStartingDigit)) {
                startingDigitPreference.startingDigit = newStartingDigit
            }
        }
    }
}