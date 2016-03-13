package br.com.wakim.eslpodclient.interactor

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.support.annotation.RequiresPermission
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.getFileName
import br.com.wakim.eslpodclient.extensions.hasPermission
import br.com.wakim.eslpodclient.model.DownloadStatus
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.rx.DownloadPublishSubject
import br.com.wakim.eslpodclient.service.StorageService
import java.io.File
import kotlin.concurrent.thread

class StorageInteractor(private var downloadManager: DownloadManager, private val app: Application) {

    fun getBaseDir(): String =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)}${File.separator}${app.getString(R.string.app_name)}"

    fun getRelativePath(podcastItem: PodcastItem): String =
            "${File.separator}${app.getString(R.string.app_name)}${File.separator}${podcastItem.mp3Url.getFileName() ?: podcastItem.remoteId}"

    fun getLocalPath(podcastItem: PodcastItem): String =
            "${getBaseDir()}${File.separator}${podcastItem.mp3Url.getFileName()}"

    fun getCompletionMarkFilePath(remoteId: Long): String =
            "${getBaseDir()}${File.separator}.${remoteId}_completed"

    fun getStartedMarkFilePath(remoteId: Long): String =
            "${getBaseDir()}${File.separator}.${remoteId}_started"

    val downloadSet: MutableSet<DownloadStatus> = mutableSetOf()

    val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

    val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            intent?.let {
                if (it.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    return
                }

                val id = it.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)

                if (id == -1L) {
                    return
                }

                thread(start = true) {
                    val query = DownloadManager.Query()
                            .setFilterById(id)
                            .setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)

                    val cursor = downloadManager.query(query)

                    if (cursor.moveToFirst()) {
                        publishDownloadCompletion(id)
                    }

                    cursor.close()
                }
            }
        }
    }

    @RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    fun getDownloadStatus(podcastItem: PodcastItem) : DownloadStatus {
        if (!app.hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            return DownloadStatus(localPath = getLocalPath(podcastItem), status = DownloadStatus.NO_PERMISSION)
        }

        val remoteId = podcastItem.remoteId
        val startedMark = File(getStartedMarkFilePath(remoteId))
        val completedMark = File(getCompletionMarkFilePath(remoteId))

        val localPath = getLocalPath(podcastItem)

        if (completedMark.exists()) {
            return DownloadStatus(localPath = localPath, remoteId = remoteId, status = DownloadStatus.DOWNLOADED)
        } else if (startedMark.exists()) {
            return DownloadStatus(localPath = localPath, remoteId = remoteId, status = DownloadStatus.DOWNLOADING)
        }

        return DownloadStatus(remoteId = remoteId, localPath = localPath)
    }

    @RequiresPermission(allOf = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE))
    fun startDownloadIfNeeded(podcastItem: PodcastItem) : DownloadStatus {
        var downloadStatus = getDownloadStatus(podcastItem)

        if (downloadStatus.status != DownloadStatus.NOT_DOWNLOADED) {
            return downloadStatus
        }

        if (!app.hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return downloadStatus.copy(status = DownloadStatus.NO_PERMISSION)
        }

        val id = downloadFile(podcastItem)

        createMarkFile(podcastItem.remoteId)

        downloadStatus = downloadStatus.copy(downloadId = id, status = DownloadStatus.DOWNLOADING)

        downloadSet.add(downloadStatus)

        return downloadStatus
    }

    fun createMarkFile(remoteId: Long) {
        createFile(getStartedMarkFilePath(remoteId))
    }

    fun createFile(path: String) {
        val startedMark = File(path)

        if (startedMark.exists()) {
            return
        }

        if (!startedMark.parentFile.exists()) {
            startedMark.mkdirs()
        }

        startedMark.createNewFile()
    }

    @RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun downloadFile(podcastItem: PodcastItem) : Long {
        val request = DownloadManager.Request(Uri.parse(podcastItem.mp3Url))

        request
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, getRelativePath(podcastItem))
                .setMimeType(StorageService.MIME_TYPE)
                .setDescription(podcastItem.blurb)
                .setTitle(podcastItem.userFriendlyTitle)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setVisibleInDownloadsUi(true)

        if (!podcastItem.isEnglishCafe()) {
            request.setDescription(podcastItem.podcastName)
        }

        request.allowScanningByMediaScanner()

        return downloadManager.enqueue(request)
    }

    fun bindReceiverForUpdates() {
        app.registerReceiver(receiver, intentFilter)
    }

    fun unbindReceiverForUpdates() {
        app.unregisterReceiver(receiver)
    }

    private fun publishDownloadCompletion(id: Long) {
        val item = downloadSet.firstOrNull { downloadStatus ->
            downloadStatus.downloadId == id
        }

        item?.let {
            val remoteId = it.remoteId

            val startedMarkFile = File(getStartedMarkFilePath(remoteId))

            createFile(getCompletionMarkFilePath(remoteId))
            startedMarkFile.delete()

            DownloadPublishSubject.INSTANCE.publishSubject.onNext(remoteId)

            downloadSet.remove(it)
        }
    }
}