package br.com.wakim.eslpodclient.data.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.data.db.DatabaseOpenHelper
import br.com.wakim.eslpodclient.data.db.database
import br.com.wakim.eslpodclient.data.model.Download
import br.com.wakim.eslpodclient.data.model.DownloadParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.parseOpt
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update

class DownloadDbInteractor(private val app: Application) {

    fun insertDownload(filename: String, remoteId: Long, downloadId: Long, status: Long) {
        app.database
                .use {
                    insert(
                            DatabaseOpenHelper.DOWNLOADS_TABLE_NAME,
                            "remote_id" to remoteId,
                            "filename" to filename,
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
                            .columns("remote_id", "filename", "download_id", "status")
                            .where("remote_id = {remote_id}", "remote_id" to remoteId)
                            .exec {
                                parseOpt(DownloadParser())
                            }
                }

    fun getDownloadByFilename(filename: String): Download? =
            app
                    .database
                    .use {
                        select(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME)
                                .columns("remote_id", "filename", "download_id", "status")
                                .where("filename = {filename}", "filename" to filename)
                                .exec {
                                    parseOpt(DownloadParser())
                                }
                    }

    fun updateDownloadStatusByDownloadId(download_id: Long, status: Long): Boolean =
            app
                    .database
                    .use {
                        update(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME, "status" to status)
                        .where("download_id = {download_id}", "download_id" to download_id)
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

    fun clearDatabase() {
        app
                .database
                .use {
                    delete(DatabaseOpenHelper.DOWNLOADS_TABLE_NAME, "", emptyArray())
                }
    }
}