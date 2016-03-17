package br.com.wakim.eslpodclient.settings.view

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.readRawString
import br.com.wakim.eslpodclient.extensions.resolveRawResIdentifier
import br.com.wakim.eslpodclient.settings.adapter.LicenseListAdapter
import br.com.wakim.eslpodclient.view.BaseActivity
import butterknife.bindView
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import org.json.JSONObject

class LicensesActivity: BaseActivity() {

    val recyclerView: RecyclerView by bindView(R.id.recycler_view)

    var adapter: LicenseListAdapter? = null
    var licenses: MutableList<Pair<String, String>>? = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showNavigationArrow()

        setContentView(R.layout.activity_licenses)

        adapter = LicenseListAdapter(this)

        recyclerView.adapter = adapter

        async() {
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