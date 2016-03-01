package br.com.wakim.eslpodclient

import android.app.Application
import com.facebook.stetho.Stetho

/**
 * Created by wakim on 3/1/16.
 */
class DebugApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}