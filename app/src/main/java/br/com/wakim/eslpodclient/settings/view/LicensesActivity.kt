package br.com.wakim.eslpodclient.settings.view

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.loadAds
import br.com.wakim.eslpodclient.extensions.readRawString
import br.com.wakim.eslpodclient.extensions.resolveRawResIdentifier
import br.com.wakim.eslpodclient.settings.adapter.LicenseListAdapter
import br.com.wakim.eslpodclient.view.BaseActivity
import butterknife.bindView
import com.google.android.gms.ads.NativeExpressAdView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject

class LicensesActivity: BaseActivity() {

    val recyclerView: RecyclerView by bindView(R.id.recycler_view)
    val adView: NativeExpressAdView by bindView(R.id.ad_view)

    var adapter: LicenseListAdapter? = null
    var licenses: MutableList<Pair<String, String>>? = mutableListOf()

    // Keeping reference to prevent being stripped by Proguard
    val licensesRes = arrayOf(R.raw.androidtagview, R.raw.anko, R.raw.ahbottomnavigation, R.raw.calligraphy,
            R.raw.dagger2, R.raw.rxandroid, R.raw.rxjava, R.raw.filepicker, R.raw.threetenabp,
            R.raw.stetho, R.raw.support_libraries, R.raw.kotlin_stdlib, R.raw.kotterknife,
            R.raw.androidvideocache)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createActivityComponent()
        setContentView(R.layout.activity_licenses)

        showNavigationArrow()

        adapter = LicenseListAdapter(this)

        recyclerView.adapter = adapter

        adView.loadAds()

        doAsync {
            val licensesJson = JSONObject(readRawString(R.raw.libraries))

            licensesJson
                    .keys()
                    .withIndex()
                    .forEach { indexed ->
                        val title = licensesJson.getString(indexed.value)
                        val body = readRawString(resolveRawResIdentifier(indexed.value))

                        licenses?.add(title to body)
                    }

            uiThread {
                adapter?.addAll(licenses!!)
            }
        }
    }
}