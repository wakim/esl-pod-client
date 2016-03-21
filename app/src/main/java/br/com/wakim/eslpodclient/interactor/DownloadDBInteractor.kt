package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.db.DatabaseOpenHelper
import br.com.wakim.eslpodclient.db.database
import br.com.wakim.eslpodclient.model.Download
import br.com.wakim.eslpodclient.model.DownloadParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.parseOpt
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update

class DownloadDbInteractor(private val app: Application) {

    fun insertDownload(remoteId: Long, downloadId: Long, status: Long) {
        app.database
                .use {
                    insert(
                            DatabaseOpenHelper.DOWNLOADS_TABLE_NAME,
                            "remote_id" to remoteId,
                            "download_id" to downloadId,
                            "status" to status
                    )
                }
    }

    fun getDownloadByRemoteId(remoteId: Long): Download? =
        app
                .database
                .use {
                    select(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME)
                            .columns("remote_id", "download_id", "status")
                            .where("remote_id = {remote_id}", "remote_id" to remoteId)
                            .exec {
                                parseOpt(DownloadParser())
                            }
                }

    fun updateDownloadStatusByDownloadId(remoteId: Long, status: Long): Boolean =
            app
                    .database
                    .use {
                        update(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME, "status" to status)
                        .where("remote_id = {remoteId}", "remoteId" to remoteId)
                        .exec() > 0
                    }

    fun deleteDownloadByDownloadId(downloadId: Long) {
        app
                .database
                .use {
                    delete(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME, "download_id = ?", arrayOf(downloadId.toString()))
                }
    }

    fun deleteDownloadByRemoteId(remoteId: Long) {
        app
                .database
                .use {
                    delete(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME, "remote_id = ?", arrayOf(remoteId.toString()))
                }
    }
}