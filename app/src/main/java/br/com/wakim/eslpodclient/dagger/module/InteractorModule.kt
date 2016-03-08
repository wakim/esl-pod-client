package br.com.wakim.eslpodclient.dagger.module

import android.app.DownloadManager
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class InteractorModule {
    @Provides @Singleton
    open fun providesPodcastListInteractor(storageInteractor: StorageInteractor) : PodcastInteractor =
            PodcastInteractor(storageInteractor)

    @Provides @Singleton
    open fun providesStorageInteractor(downloadManager: DownloadManager, app: Application) : StorageInteractor =
            StorageInteractor(downloadManager, app)
}