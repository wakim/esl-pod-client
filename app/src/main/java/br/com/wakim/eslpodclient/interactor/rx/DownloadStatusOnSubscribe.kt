package br.com.wakim.eslpodclient.interactor.rx

import android.net.Uri
import br.com.wakim.eslpodclient.extensions.toFile
import br.com.wakim.eslpodclient.interactor.DownloadDbInteractor
import br.com.wakim.eslpodclient.model.DownloadStatus
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

            subscriber.onSuccess(DownloadStatus(localPath = localPath.toString(), remoteId = remoteId, downloadId = 0, status = DownloadStatus.NOT_DOWNLOADED))
            return
        }

        // Not managed by app or database was cleared
        if (download == null) {
            downloadDbInteractor.insertDownload(remoteId = remoteId, filename = file.name, downloadId = 0, status = DownloadStatus.DOWNLOADED)
            subscriber.onSuccess(DownloadStatus(localPath = localPath.toString(), remoteId = remoteId, downloadId = 0, status = DownloadStatus.DOWNLOADED))
        } else {
            with (download) {
                subscriber.onSuccess(DownloadStatus(localPath = localPath.toString(), remoteId = remoteId, downloadId = downloadId, status = status))
            }
        }
    }
}