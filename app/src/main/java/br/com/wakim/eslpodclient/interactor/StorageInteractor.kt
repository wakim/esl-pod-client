package br.com.wakim.eslpodclient.interactor

import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import android.support.annotation.RequiresPermission
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.getFileName
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.service.StorageService
import rx.Observable
import rx.schedulers.Schedulers
import java.io.File

class StorageInteractor(private var downloadManager: DownloadManager, private val app: Application) {

    fun getBaseDir() : String =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)}${File.separator}${app.getString(R.string.app_name)}"

    fun getRelativePath(podcastItem: PodcastItem) : String =
            "${File.separator}${app.getString(R.string.app_name)}${File.separator}${podcastItem.mp3Url.getFileName() ?: podcastItem.remoteId}"

    fun getLocalPath(podcastItem: PodcastItem) : String =
            "${getBaseDir()}${File.separator}${podcastItem.mp3Url.getFileName() ?: podcastItem.remoteId}"

    fun getLocalPathIfExists(podcastItem: PodcastItem) : String? {
        val file = File(getLocalPath(podcastItem))

        if (file.exists()) {
            return file.absolutePath
        }

        return null
    }

    @RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun startDownloadIfNeeded(podcastItem: PodcastItem) : DownloadRequest {
        val file = File(getLocalPath(podcastItem))
        var id : Long? = null

        if (!file.exists()) {
            id = downloadFile(podcastItem)
        }

        return DownloadRequest(id, getVirtualPath(file))
    }

    fun cancelDownloadIfPending(downloadRequest: DownloadRequest) {
        downloadRequest.downloadId?.let {
            val id = it

            Observable.create<Any> { subscriber ->
                val query = DownloadManager.Query()
                        .setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
                        .setFilterById(id)

                val c = downloadManager.query(query)

                if (!c.moveToFirst()) {
                    downloadManager.remove(id)
                }

                c.close()

                subscriber.onCompleted()
            }.observeOn(Schedulers.io())
             .subscribe()
        }
    }

    private fun getVirtualPath(file : File) : String {
        return "http://localhost:${BuildConfig.SERVER_PORT}${File.separator}${file.name}"
    }

    @RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun downloadFile(podcastItem: PodcastItem) : Long {
        val request = DownloadManager.Request(Uri.parse(podcastItem.mp3Url))

        request
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, getRelativePath(podcastItem))
                .setMimeType(StorageService.MIME_TYPE)
                .setDescription(podcastItem.blurb)
                .setTitle(podcastItem.title)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setVisibleInDownloadsUi(true)

        request.allowScanningByMediaScanner()

        return downloadManager.enqueue(request)
    }

    data class DownloadRequest(val downloadId: Long? = null, val downloadUrl: String)
}