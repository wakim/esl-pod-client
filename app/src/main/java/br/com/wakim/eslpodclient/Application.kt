package br.com.wakim.eslpodclient

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.support.annotation.VisibleForTesting
import br.com.wakim.eslpodclient.android.receiver.ConnectivityBroadcastReceiver
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.dagger.DaggerAppComponent
import br.com.wakim.eslpodclient.dagger.module.AppModule
import br.com.wakim.eslpodclient.ui.rx.ConnectivityPublishSubject
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.jakewharton.threetenabp.AndroidThreeTen
import io.fabric.sdk.android.Fabric
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import javax.inject.Inject

open class Application : android.app.Application() {

    lateinit var appComponent : AppComponent

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val connectivityReceiver = ConnectivityBroadcastReceiver()

    var connected: Boolean = false
        set(value) {
            field = value
            ConnectivityPublishSubject.INSTANCE.onNext(value)
        }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        AndroidThreeTen.init(this)

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics(), Answers())
        }

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )

        createComponent()

        appComponent.inject(this)

        connected = ConnectivityBroadcastReceiver.isNetworkConnected(connectivityManager)

        registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onTerminate() {
        super.onTerminate()

        unregisterReceiver(connectivityReceiver)
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
        const val PLAYER_WRITE_STORAGE_PERMISSION = 12
        const val LIST_DOWNLOAD_WRITE_STORAGE_PERMISSION = 13
        const val LIST_REMOVE_DOWNLOAD_WRITE_STORAGE_PERMISSION = 14
        const val LIST_DELETE_DOWNLOAD_WRITE_STORAGE_PERMISSION = 15
        const val PICK_FOLDER_READ_STORAGE_PERMISSION = 16

        var INSTANCE: Application? = null
            private set
    }
}