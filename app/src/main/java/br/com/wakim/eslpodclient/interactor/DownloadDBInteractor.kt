package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.db.DatabaseOpenHelper
import br.com.wakim.eslpodclient.db.database
import br.com.wakim.eslpodclient.model.Download
import br.com.wakim.eslpodclient.model.DownloadParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.parseOpt
import org.jetbrains.anko.db.select

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

    fun getDownloadByRemoteId(remoteId: Long): Download? {
        return app
                .database
                .readableDatabase
                .select(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME, "remote_id", "download_id", "status")
                .where("remote_id = {remote_id}", "remote_id" to remoteId)
                .exec {
                    parseOpt(DownloadParser())
                }
    }

    fun getDownloadByDownloadId(downloadId: Long): Download? {
        return app
                .database
                .readableDatabase
                .select(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME, "remote_id", "download_id", "status")
                .where("download_id = {download_id}", "download_id" to downloadId)
                .exec {
                    parseOpt(DownloadParser())
                }
    }

    fun deleteDownloadByDownloadId(downloadId: Long) {
        app.database
            .use {
                delete(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME, "download_id = ?", arrayOf(downloadId.toString()))
            }
    }
}