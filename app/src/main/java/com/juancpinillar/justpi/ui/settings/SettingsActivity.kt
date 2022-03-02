package com.juancpinillar.justpi.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.support.v7.preference.PreferenceFragmentCompat
import com.juancpinillar.justpi.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat(),
            SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            val startingDigitKey: String = resources.getString(R.string.key_starting_digit_pref)
            when (key) {
                startingDigitKey -> {
                    val startingDigitPreference = findPreference(key) as StartingDigitPreference
                    startingDigitPreference.updateStartingDigit()
                }
            }
        }

        override fun onDisplayPreferenceDialog(preference: Preference?) {
            var dialogFragment: PreferenceDialogFragmentCompat? = null
            if (preference is StartingDigitPreference) {
                dialogFragment = StartingDigitPreferenceDialogFragmentCompat.newInstance(preference.key)
            }
            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(fragmentManager, "android.support.v7.preference.PreferenceFragment.DIALOG")
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences
                    .registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences
                    .unregisterOnSharedPreferenceChangeListener(this)
        }
    }
}