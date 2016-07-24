package br.com.wakim.eslpodclient.dagger.module

import android.app.DownloadManager
import android.content.Context
import android.media.AudioManager
import android.net.ConnectivityManager
import android.support.v4.app.NotificationManagerCompat
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.data.interactor.PreferenceInteractor
import br.com.wakim.eslpodclient.data.model.PublishSubjectItem
import dagger.Module
import dagger.Provides
import rx.subjects.PublishSubject
import javax.inject.Singleton

@Module
class AppModule(private val app: Application) {

    @Provides @Singleton
    fun providesApplication() = app

    @Provides @Singleton
    fun providesPreferenceManager() = PreferenceInteractor(app)

    @Provides @Singleton
    fun providesAudioManager() = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    @Provides @Singleton
    fun providesDownloadManager() = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    @Provides @Singleton
    fun providesConnectivityManager() = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides @Singleton
    fun providesNotificationManagerCompat(): NotificationManagerCompat = NotificationManagerCompat.from(app)

    @Provides @Singleton
    fun providesPublishSubject(): PublishSubject<PublishSubjectItem<Any>> =
            PublishSubject.create<PublishSubjectItem<Any>>()
}