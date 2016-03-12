package br.com.wakim.eslpodclient.service

import android.app.DownloadManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.interactor.StorageInteractor
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
    lateinit var downloadManager : DownloadManager

    @Inject
    lateinit var storageInteractor : StorageInteractor

    val binder = StorageLocalBinder(this)

    var fileServer : FileServer? = null

    var lastDownloadRequest : StorageInteractor.DownloadRequest? = null

    override fun onCreate() {
        super.onCreate()

        (applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent?)?.inject(this)

//        if (fileServer == null) {
//            fileServer = FileServer(storageInteractor.getBaseDir())
//            fileServer!!.start()
//        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()

        lastDownloadRequest?.let {
            storageInteractor.cancelDownloadIfPending(it)
        }

//        fileServer?.stop()
    }

    fun startDownloadIfNeeded(podcastItem: PodcastItem) : String  {
        lastDownloadRequest = storageInteractor.startDownloadIfNeeded(podcastItem)
        return lastDownloadRequest!!.downloadUrl
    }
}

class StorageLocalBinder : TypedBinder<StorageService> {

    lateinit var reference : WeakReference<StorageService>

    constructor(storageService : StorageService) : super() {
        reference = WeakReference(storageService)
    }

    override fun getService(): StorageService? = reference.get()
}