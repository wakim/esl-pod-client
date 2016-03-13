package br.com.wakim.eslpodclient.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.annotation.RequiresPermission
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.model.DownloadStatus
import br.com.wakim.eslpodclient.model.PodcastItem
import java.lang.ref.WeakReference
import javax.inject.Inject

class StorageService : Service() {

    companion object {
        final const val MIME_TYPE = "audio/mpeg"
    }

    @Inject
    lateinit var app : Application

    @Inject
    lateinit var storageInteractor : StorageInteractor

    val binder = StorageLocalBinder(this)

    override fun onCreate() {
        super.onCreate()

        (applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent?)?.inject(this)

        storageInteractor.bindReceiverForUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()

        storageInteractor.unbindReceiverForUpdates()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    @RequiresPermission(allOf = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE))
    fun startDownloadIfNeeded(podcastItem: PodcastItem) {
        val currentStatus = podcastItem.downloadStatus.status

        if (currentStatus == DownloadStatus.DOWNLOADING || currentStatus == DownloadStatus.DOWNLOADED) {
            return
        }

        podcastItem.downloadStatus = storageInteractor.startDownloadIfNeeded(podcastItem)
    }
}

class StorageLocalBinder : TypedBinder<StorageService> {

    lateinit var reference : WeakReference<StorageService>

    constructor(storageService : StorageService) : super() {
        reference = WeakReference(storageService)
    }

    override fun getService(): StorageService? = reference.get()
}