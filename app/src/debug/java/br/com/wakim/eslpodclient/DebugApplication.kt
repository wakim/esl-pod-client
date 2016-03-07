package br.com.wakim.eslpodclient

import com.facebook.stetho.Stetho

class DebugApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}