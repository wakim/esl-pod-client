package br.com.wakim.eslpodclient.interactor

import android.app.DownloadManager
import android.net.Uri
import android.support.annotation.RequiresPermission
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.extensions.connected
import br.com.wakim.eslpodclient.extensions.getFileName
import br.com.wakim.eslpodclient.extensions.hasPermission
import br.com.wakim.eslpodclient.interactor.rx.DownloadStatusOnSubscribe
import br.com.wakim.eslpodclient.interactor.rx.DownloadSyncOnSubscribe
import br.com.wakim.eslpodclient.model.DownloadStatus
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.service.StorageService
import rx.Observable
import rx.Single
import java.io.File

class StorageInteractor(private var downloadManager: DownloadManager,
                        private val downloadDbInteractor: DownloadDbInteractor,
                        private val preferenceInteractor: PreferenceInteractor,
                        private val app: Application) {

    fun getBaseDir(): String =
            preferenceInteractor.getDownloadLocation()

//    fun getRelativePath(podcastItem: PodcastItem): String =
//            "${File.separator}${app.getString(R.string.app_name)}${File.separator}${podcastItem.mp3Url.getFileName() ?: podcastItem.remoteId}"

    fun getLocalPath(podcastItem: PodcastItem): String =
            "${getBaseDir()}${File.separator}${podcastItem.mp3Url.getFileName()}"

    fun getLocalUri(podcastItem: PodcastItem): Uri =
            Uri.fromFile(File(getLocalPath(podcastItem)))

    @RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    fun getDownloadStatus(podcastItem: PodcastItem) : Single<DownloadStatus> =
            Single.create(DownloadStatusOnSubscribe(podcastItem.remoteId, getLocalPath(podcastItem), downloadDbInteractor))

    @RequiresPermission(allOf = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE))
    fun startDownloadIfNeeded(podcastItem: PodcastItem) : Single<DownloadStatus> {
        return getDownloadStatus(podcastItem)
            .map { downloadStatus -> DownloadStatus
                if (downloadStatus.status != DownloadStatus.NOT_DOWNLOADED) {
                    return@map downloadStatus
                }

                if (!app.hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return@map downloadStatus.copy(status = DownloadStatus.NO_PERMISSION)
                }

                val id = downloadFile(podcastItem)

                insertIntoDb(podcastItem.remoteId, getLocalPath(podcastItem).getFileName(), id)

                return@map downloadStatus.copy(downloadId = id, status = DownloadStatus.DOWNLOADING)
            }
    }

    private fun insertIntoDb(remoteId: Long, filename: String, downloadId: Long) {
        downloadDbInteractor.insertDownload(remoteId = remoteId, filename = filename, downloadId = downloadId, status = DownloadStatus.DOWNLOADING)
    }

    @RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun downloadFile(podcastItem: PodcastItem) : Long {
        val request = DownloadManager.Request(Uri.parse(podcastItem.mp3Url))

        request
                .setDestinationUri(getLocalUri(podcastItem))
                .setMimeType(StorageService.MIME_TYPE)
                .setTitle(podcastItem.userFriendlyTitle)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setVisibleInDownloadsUi(true)

        if (!podcastItem.isEnglishCafe()) {
            request.setDescription(podcastItem.podcastName)
        }

        request.allowScanningByMediaScanner()

        return downloadManager.enqueue(request)
    }

    fun handleDownloadCompletion(downloadId: Long) {
        downloadDbInteractor.updateDownloadStatusByDownloadId(downloadId, DownloadStatus.DOWNLOADED)
    }

    fun handleDownloadFailed(downloadId: Long) {
        downloadDbInteractor.deleteDownloadByDownloadId(downloadId)
    }

    fun deleteDownload(podcastItem: PodcastItem) =
            cancelDownload(podcastItem)
                    .map { result -> if (result) File(getLocalPath(podcastItem)).delete() else false }

    fun cancelDownload(podcastItem: PodcastItem) =
        Single.defer<Boolean> {
            downloadDbInteractor.deleteDownloadByRemoteId(podcastItem.remoteId)
            Single.just(true)
        }

    fun cancelDownload(downloadStatus: DownloadStatus) {
        downloadStatus.downloadId.let {
            downloadManager.remove(it)
            downloadDbInteractor.deleteDownloadByDownloadId(it)
        }
    }

    fun synchronizeDownloads() =
            Observable.create(DownloadSyncOnSubscribe(this, downloadDbInteractor, BuildConfig.SEARCH_URL))
                    .connected()
}