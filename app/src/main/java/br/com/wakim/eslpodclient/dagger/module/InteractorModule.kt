package br.com.wakim.eslpodclient.dagger.module

import android.app.DownloadManager
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.interactor.DownloadDbInteractor
import br.com.wakim.eslpodclient.interactor.FavoritesInteractor
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class InteractorModule {

    @Provides @Singleton
    open fun providesPodcastListInteractor(app: Application) : PodcastInteractor = PodcastInteractor(app)

    @Provides @Singleton
    open fun providesStorageInteractor(downloadManager: DownloadManager, downloadDbInteractor: DownloadDbInteractor, app: Application) : StorageInteractor =
            StorageInteractor(downloadManager, downloadDbInteractor, app)

    @Provides @Singleton
    open fun providesFavoritesInteractor(app: Application): FavoritesInteractor = FavoritesInteractor(app)

    @Provides @Singleton
    open fun providesDownloadDbInteractor(app: Application) = DownloadDbInteractor(app)
}