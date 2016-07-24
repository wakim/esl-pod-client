package br.com.wakim.eslpodclient.data.interactor.rx

import android.net.Uri
import br.com.wakim.eslpodclient.data.interactor.DownloadDbInteractor
import br.com.wakim.eslpodclient.data.model.DownloadStatus
import br.com.wakim.eslpodclient.util.extensions.toFile
import rx.Single
import rx.SingleSubscriber

class DownloadStatusOnSubscribe(private val remoteId: Long, private val localPath: Uri, private val downloadDbInteractor: DownloadDbInteractor): Single.OnSubscribe<DownloadStatus> {
    override fun call(subscriber: SingleSubscriber<in DownloadStatus>) {
        val download = downloadDbInteractor.getDownloadByRemoteId(remoteId)
        val file = localPath.toFile()

        if (subscriber.isUnsubscribed) {
            return
        }

        if (!file.exists() || file.length() == 0L) {
            // File was deleted and not synchronized
            if (download != null) {
                downloadDbInteractor.deleteDownloadByRemoteId(remoteId)
            }

            subscriber.onSuccess(DownloadStatus(localPath = file.absolutePath, remoteId = remoteId, downloadId = 0, status = DownloadStatus.NOT_DOWNLOADED))
            return
        }

        // Not managed by app or database was cleared
        if (download == null) {
            downloadDbInteractor.insertDownload(remoteId = remoteId, filename = file.nameWithoutExtension, downloadId = 0, status = DownloadStatus.DOWNLOADED)
            subscriber.onSuccess(DownloadStatus(localPath = file.absolutePath, remoteId = remoteId, downloadId = 0, status = DownloadStatus.DOWNLOADED))
        } else {
            with (download) {
                subscriber.onSuccess(DownloadStatus(localPath = file.absolutePath, remoteId = remoteId, downloadId = downloadId, status = status))
            }
        }
    }
}