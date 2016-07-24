package br.com.wakim.eslpodclient.dagger

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.android.receiver.ConnectivityBroadcastReceiver
import br.com.wakim.eslpodclient.android.service.DownloadManagerReceiver
import br.com.wakim.eslpodclient.android.service.PlayerService
import br.com.wakim.eslpodclient.android.service.StorageService
import br.com.wakim.eslpodclient.dagger.module.ActivityModule
import br.com.wakim.eslpodclient.dagger.module.AppModule
import br.com.wakim.eslpodclient.dagger.module.InteractorModule
import br.com.wakim.eslpodclient.data.interactor.PreferenceInteractor
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(InteractorModule::class, AppModule::class))
interface AppComponent {
    fun inject(app: Application)

    fun inject(playerService: PlayerService)
    fun inject(storageService: StorageService)
    fun inject(downloadManagerReceiver: DownloadManagerReceiver)
    fun inject(connectivityBroadcastReceiver: ConnectivityBroadcastReceiver)

    fun preferencesManager() : PreferenceInteractor
    fun plus(activityModule: ActivityModule) : ActivityComponent
}