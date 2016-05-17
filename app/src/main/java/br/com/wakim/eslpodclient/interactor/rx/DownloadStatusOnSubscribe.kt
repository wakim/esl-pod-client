package br.com.wakim.eslpodclient.interactor.rx

import br.com.wakim.eslpodclient.extensions.getFileName
import br.com.wakim.eslpodclient.interactor.DownloadDbInteractor
import br.com.wakim.eslpodclient.model.DownloadStatus
import rx.Single
import rx.SingleSubscriber
import java.io.File

class DownloadStatusOnSubscribe(private val remoteId: Long, private val localPath: String, private val downloadDbInteractor: DownloadDbInteractor): Single.OnSubscribe<DownloadStatus> {
    override fun call(subscriber: SingleSubscriber<in DownloadStatus>) {
        val download = downloadDbInteractor.getDownloadByRemoteId(remoteId)
        val file = File(localPath)

        if (subscriber.isUnsubscribed) {
            return
        }

        if (!file.exists()) {
            // File was deleted and not synchronized
            if (download != null) {
                downloadDbInteractor.deleteDownloadByRemoteId(remoteId)
            }

            subscriber.onSuccess(DownloadStatus(localPath = localPath, remoteId = remoteId, downloadId = 0, status = DownloadStatus.NOT_DOWNLOADED))
        }

        // Not managed by app or database was cleared
        if (download == null) {
            downloadDbInteractor.insertDownload(remoteId = remoteId, filename = localPath.getFileName(), downloadId = 0, status = DownloadStatus.DOWNLOADED)
            subscriber.onSuccess(DownloadStatus(localPath = localPath, remoteId = remoteId, downloadId = 0, status = DownloadStatus.DOWNLOADED))
        } else {
            with (download) {
                subscriber.onSuccess(DownloadStatus(localPath = localPath, remoteId = remoteId, downloadId = downloadId, status = status))
            }
        }
    }
}