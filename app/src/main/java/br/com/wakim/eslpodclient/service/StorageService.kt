package br.com.wakim.eslpodclient.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.IBinder
import android.support.annotation.RequiresPermission
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.interactor.PodcastDbInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.model.DownloadStatus
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PublishSubjectItem
import br.com.wakim.eslpodclient.notification.NotificationActivity
import rx.Single
import rx.subjects.PublishSubject
import java.lang.ref.WeakReference
import javax.inject.Inject

class StorageService : Service() {

    companion object {
        const val MIME_TYPE = "audio/mpeg"
        const val NOTIFICATION_ID = 323
    }

    @Inject
    lateinit var app : Application

    @Inject
    lateinit var storageInteractor : StorageInteractor

    @Inject
    lateinit var podcastDbInteractor: PodcastDbInteractor

    @Inject
    lateinit var publishSubject: PublishSubject<PublishSubjectItem<Any>>

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    val binder = StorageLocalBinder(this)

    var synchronizing = false

    var notificationBuilder: NotificationCompat.Builder? = null

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

        notifyProgress()

        storageInteractor.synchronizeDownloads()
                .ofIOToMainThread()
                .subscribe(
                        { pair ->
                            podcastDbInteractor.insertPodcast(pair.first)
                            podcastDbInteractor.insertPodcastDetail(pair.second)

                            publishSubject.onNext(PublishSubjectItem(PublishSubjectItem.PODCAST_SYNC_TYPE, pair.first))

                            notifyProgress(pair.first)
                        },
                        { e ->
                            publishSubject.onNext(PublishSubjectItem(PublishSubjectItem.PODCAST_SYNC_ENDED_TYPE))
                            synchronizing = false

                            notificationManager.cancel(NOTIFICATION_ID)
                        },
                        {
                            publishSubject.onNext(PublishSubjectItem(PublishSubjectItem.PODCAST_SYNC_ENDED_TYPE))
                            synchronizing = false

                            notifySynchronizationFinished()
                        }
                )
    }

    fun notifyProgress(podcastItem: PodcastItem? = null) {
        notificationBuilder = notificationBuilder ?: NotificationCompat.Builder(this)

        val pendingIntent = PendingIntent.getActivity(this, 1,
                Intent(this, NotificationActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder!!
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon((ContextCompat.getDrawable(this, R.drawable.ic_sync) as BitmapDrawable).bitmap)
                .setOnlyAlertOnce(true)
                .setProgress(0, 100, true)
                .setContentIntent(pendingIntent)
                .setContentTitle(getString(R.string.synchronizing_local_database))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

        if (podcastItem != null) {
            notificationBuilder!!.setContentText(getString(R.string.last_synchronized_podcast, podcastItem.userFriendlyTitle))
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder!!.build())
    }

    fun notifySynchronizationFinished() {
        notificationBuilder = notificationBuilder ?: NotificationCompat.Builder(this)

        val pendingIntent = PendingIntent.getActivity(this, 1,
                Intent(this, NotificationActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder!!
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(null)
                .setContentText(null)
                .setOngoing(false)
                .setOnlyAlertOnce(false)
                .setAutoCancel(true)
                .setProgress(0, 0, false)
                .setContentTitle(getString(R.string.synchronization_finished))
                .setDeleteIntent(pendingIntent)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

        notificationManager.cancel(NOTIFICATION_ID)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder!!.build())
    }

    fun  shouldCache() = storageInteractor.shouldCache()
}

class StorageLocalBinder : TypedBinder<StorageService> {

    lateinit var reference: WeakReference<StorageService>

    override val service: StorageService?
        get() = reference.get()

    constructor(storageService : StorageService) : super() {
        reference = WeakReference(storageService)
    }
}