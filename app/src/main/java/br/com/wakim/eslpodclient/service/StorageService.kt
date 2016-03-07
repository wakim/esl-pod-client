package br.com.wakim.eslpodclient.service

import android.app.DownloadManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.extensions.getFileName
import br.com.wakim.eslpodclient.model.PodcastItem
import java.io.File
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

    val binder = StorageLocalBinder(this)

    var fileServer : FileServer? = null

    override fun onCreate() {
        super.onCreate()

        (applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent?)?.inject(this)

        if (fileServer == null) {
            fileServer = FileServer(getBaseDir())
            fileServer!!.start()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    private fun getBaseDir() : String =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)}${File.separator}${app.getString(R.string.app_name)}"

    private fun getRelativePath(podcastItem: PodcastItem) : String =
            "${File.separator}${app.getString(R.string.app_name)}${File.separator}${podcastItem.mp3Url.getFileName() ?: podcastItem.remoteId}"

    private fun getLocalPath(podcastItem: PodcastItem) : String =
            "${getBaseDir()}${File.separator}${podcastItem.mp3Url.getFileName() ?: podcastItem.remoteId}"

    fun startDownloadIfNeeded(podcastItem: PodcastItem) : String {
        val file = File(getLocalPath(podcastItem))

        if (!file.exists()) {
            downloadFile(podcastItem)
        }

        return getVirtualPath(file)
    }

    private fun getVirtualPath(file : File) : String {
        return "http://localhost:${fileServer!!.listeningPort}${File.separator}${file.name}"
    }

    private fun downloadFile(podcastItem: PodcastItem) {
        val request = DownloadManager.Request(Uri.parse(podcastItem.mp3Url))

        request.allowScanningByMediaScanner()
        request
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, getRelativePath(podcastItem))
                .setMimeType(MIME_TYPE)
                .setDescription(podcastItem.blurb)
                .setTitle(podcastItem.title)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setVisibleInDownloadsUi(true)

        downloadManager.enqueue(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        fileServer?.stop()
    }
}

class StorageLocalBinder : Binder {

    lateinit var weakReference : WeakReference<StorageService>

    constructor(storageService : StorageService) : super() {
        weakReference = WeakReference(storageService)
    }

    fun getService() : StorageService? =
            weakReference.get()
}