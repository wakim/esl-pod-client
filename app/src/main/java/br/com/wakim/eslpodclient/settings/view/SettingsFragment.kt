package br.com.wakim.eslpodclient.settings.view

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import br.com.wakim.eslpodclient.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.settings)
    }
}