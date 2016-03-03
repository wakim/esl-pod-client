package br.com.wakim.eslpodclient

import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.dagger.DaggerAppComponent
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Created by wakim on 3/1/16.
 */
open class Application : android.app.Application() {

    lateinit var appComponent : AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.create()
        AndroidThreeTen.init(this)
    }

    override fun getSystemService(name: String?): Any? {
        if (name == AppComponent::class.java.simpleName) {
            return appComponent
        }

        return super.getSystemService(name)
    }
}