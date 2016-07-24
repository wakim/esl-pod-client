package br.com.wakim.eslpodclient.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.dagger.AppComponent
import javax.inject.Inject

class ConnectivityBroadcastReceiver: BroadcastReceiver() {

    companion object {
        fun isNetworkConnected(connectivityManager: ConnectivityManager): Boolean = connectivityManager.activeNetworkInfo?.isConnected ?: false
    }

    @Inject
    lateinit var app: Application

    override fun onReceive(context: Context, intent: Intent?) {
        (context.applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent).inject(this)
        app.connected = isNetworkConnected()
    }

    fun isNetworkConnected(): Boolean {
        return isNetworkConnected(app.connectivityManager)
    }
}