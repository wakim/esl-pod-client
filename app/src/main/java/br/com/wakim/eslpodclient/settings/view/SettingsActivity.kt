package br.com.wakim.eslpodclient.settings.view

import android.os.Bundle
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.loadAds
import br.com.wakim.eslpodclient.view.BaseActivity
import butterknife.bindView
import com.google.android.gms.ads.NativeExpressAdView

class SettingsActivity : BaseActivity() {
    val adView: NativeExpressAdView by bindView(R.id.ad_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createActivityComponent()
        setContentView(R.layout.activity_settings)
        showNavigationArrow()

        adView.loadAds()
    }
}