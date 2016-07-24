package br.com.wakim.eslpodclient.ui.settings.view

import android.os.Bundle
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.ui.view.BaseActivity
import br.com.wakim.eslpodclient.util.extensions.loadAds
import butterknife.BindView
import com.google.android.gms.ads.NativeExpressAdView

class SettingsActivity : BaseActivity() {

    @BindView(R.id.ad_view)
    lateinit var adView: NativeExpressAdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createActivityComponent()
        setContentView(R.layout.activity_settings)
        showNavigationArrow()

        adView.loadAds()
    }
}