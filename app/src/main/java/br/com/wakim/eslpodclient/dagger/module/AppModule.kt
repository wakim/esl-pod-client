package br.com.wakim.eslpodclient.dagger.module

import android.app.DownloadManager
import android.content.Context
import android.media.AudioManager
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.interactor.PreferenceInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val app : Application) {

    @Provides @Singleton
    fun providesApplication() : Application {
        return app
    }

    @Provides @Singleton
    fun providesPreferenceManager() : PreferenceInteractor {
        return PreferenceInteractor(app)
    }

    @Provides @Singleton
    fun providesAudioManager() : AudioManager =
            app.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Provides @Singleton
    fun providesDownloadManager() : DownloadManager =
            app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
}