package br.com.wakim.eslpodclient.settings.view

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.readRawString
import br.com.wakim.eslpodclient.extensions.resolveRawResIdentifier
import br.com.wakim.eslpodclient.view.BaseActivity
import butterknife.bindView
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import org.json.JSONObject

class LicensesActivity: BaseActivity() {

    val recyclerView: RecyclerView by bindView(R.id.recycler_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showNavigationArrow()

        setContentView(R.layout.activity_licenses)

        async() {
            val licensesJson = JSONObject(readRawString(R.raw.libraries))

            val entries = arrayOfNulls<String>(licensesJson.length())

            licensesJson
                    .keys()
                    .withIndex()
                    .forEach { indexed ->
                        val title = licensesJson.getString(indexed.value)
                        val body = readRawString(resolveRawResIdentifier(indexed.value))

                        entries[indexed.index] = "<b>$title</b><br />$body"
                    }

            uiThread {
                // TODO
            }
        }
    }
}