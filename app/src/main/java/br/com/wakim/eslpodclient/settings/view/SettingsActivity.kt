package br.com.wakim.eslpodclient.settings.view

import android.os.Bundle
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.view.BaseActivity

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showNavigationArrow()

        setContentView(R.layout.activity_settings)
    }
}