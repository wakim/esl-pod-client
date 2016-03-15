package br.com.wakim.eslpodclient

import android.support.annotation.VisibleForTesting
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.dagger.DaggerAppComponent
import br.com.wakim.eslpodclient.dagger.module.AppModule
import com.jakewharton.threetenabp.AndroidThreeTen
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

open class Application : android.app.Application() {

    lateinit var appComponent : AppComponent

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        )

        createComponent()
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

    }
}