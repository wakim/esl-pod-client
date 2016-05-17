package br.com.wakim.eslpodclient.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.annotation.RequiresPermission
import android.support.v4.app.NotificationManagerCompat
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.interactor.PodcastDbInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.model.DownloadStatus
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PublishSubjectItem
import rx.Single
import rx.subjects.PublishSubject
import java.lang.ref.WeakReference
import javax.inject.Inject

class StorageService : Service() {

    companion object {
        final const val MIME_TYPE = "audio/mpeg"
    }

    @Inject
    lateinit var app : Application

    @Inject
    lateinit var notificationManagerCompat: NotificationManagerCompat

    @Inject
    lateinit var storageInteractor : StorageInteractor

    @Inject
    lateinit var podcastDbInteractor: PodcastDbInteractor

    @Inject
    lateinit var publishSubject: PublishSubject<PublishSubjectItem<Any>>

    val binder = StorageLocalBinder(this)

    var synchronizing = false

    override fun onCreate() {
        super.onCreate()
        (applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent?)?.inject(this)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    @RequiresPermission(allOf = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE))
    fun startDownloadIfNeeded(podcastItem: PodcastItem): Single<DownloadStatus> =
            storageInteractor.startDownloadIfNeeded(podcastItem)
                    .ofIOToMainThread()

    fun cancelDownload(downloadStatus: DownloadStatus) = storageInteractor.cancelDownload(downloadStatus)

    fun synchronize() {
        if (synchronizing) {
            return
        }

        synchronizing = true

        storageInteractor.synchronizeDownloads()
                .ofIOToMainThread()
                .subscribe(
                        { pair ->
                            podcastDbInteractor.insertPodcast(pair.first)
                            podcastDbInteractor.insertPodcastDetail(pair.second)

                            publishSubject.onNext(PublishSubjectItem(PublishSubjectItem.PODCAST_SYNC_TYPE, pair.first))
                        },
                        { e ->
                            synchronizing = false
                        },
                        {
                            synchronizing = false
                        }
                )
    }
}

class StorageLocalBinder : TypedBinder<StorageService> {

    lateinit var reference : WeakReference<StorageService>

    constructor(storageService : StorageService) : super() {
        reference = WeakReference(storageService)
    }

    override fun getService(): StorageService? = reference.get()
}