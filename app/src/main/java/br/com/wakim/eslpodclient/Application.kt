package br.com.wakim.eslpodclient

import android.net.ConnectivityManager
import android.support.annotation.VisibleForTesting
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.dagger.DaggerAppComponent
import br.com.wakim.eslpodclient.dagger.module.AppModule
import br.com.wakim.eslpodclient.receiver.ConnectivityBroadcastReceiver
import br.com.wakim.eslpodclient.rx.ConnectivityPublishSubject
import com.jakewharton.threetenabp.AndroidThreeTen
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import javax.inject.Inject

open class Application : android.app.Application() {

    lateinit var appComponent : AppComponent

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    var connected: Boolean = false
        set(value) {
            field = value
            ConnectivityPublishSubject.INSTANCE.onNext(value)
        }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        AndroidThreeTen.init(this)

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )

        createComponent()

        appComponent.inject(this)

        connected = ConnectivityBroadcastReceiver.isNetworkConnected(connectivityManager)
    }

    @VisibleForTesting
    open fun createComponent() {
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    override fun getSystemService(name: String?): Any? {
        if (name == AppComponent::class.java.simpleName) {
            return appComponent
        }

        return super.getSystemService(name)
    }

    companion object {
        final val PLAYER_WRITE_STORAGE_PERMISSION = 12
        final val LIST_WRITE_STORAGE_PERMISSION = 13
        final val PICK_FOLDER_READ_STORAGE_PERMISSION = 14

        var INSTANCE: Application? = null
            private set
    }
}