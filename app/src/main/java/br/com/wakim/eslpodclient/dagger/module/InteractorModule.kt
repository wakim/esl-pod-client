package br.com.wakim.eslpodclient.dagger.module

import android.app.DownloadManager
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.data.interactor.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class InteractorModule {

    @Provides @Singleton
    open fun providesPodcastListInteractor(podcastDbInteractor: PodcastDbInteractor, app: Application) = PodcastInteractor(podcastDbInteractor, app)

    @Provides @Singleton
    open fun providesStorageInteractor(downloadManager: DownloadManager,
                                       downloadDbInteractor: DownloadDbInteractor,
                                       preferenceInteractor: PreferenceInteractor,
                                       app: Application) =
            StorageInteractor(downloadManager, downloadDbInteractor, preferenceInteractor, app)

    @Provides @Singleton
    open fun providesFavoritedInteractor(podcastDbInteractor: PodcastDbInteractor, app: Application) = FavoritedPodcastItemInteractor(podcastDbInteractor, app)

    @Provides @Singleton
    open fun providesDownloadedInteractor(podcastDbInteractor: PodcastDbInteractor, app: Application) = DownloadedPodcastItemInteractor(podcastDbInteractor, app)

    @Provides @Singleton
    open fun providesDownloadDbInteractor(app: Application) = DownloadDbInteractor(app)

    @Provides @Singleton
    open fun providesPodcastDbInteractor(app: Application) = PodcastDbInteractor(app)
}