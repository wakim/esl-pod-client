package br.com.wakim.eslpodclient.android.notification

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import br.com.wakim.eslpodclient.ui.podcastlist.view.PodcastListActivity

class NotificationActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, PodcastListActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        startActivity(intent)
    }
}
