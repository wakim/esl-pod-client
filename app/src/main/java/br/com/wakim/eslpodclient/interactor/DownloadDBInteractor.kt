package br.com.wakim.eslpodclient.interactor

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.model.Download
import br.com.wakim.eslpodclient.model.DownloadParser
import org.jetbrains.anko.db.*

class DownloadDbInteractor(private val app: Application) {

    fun insertDownload(remoteId: Long, downloadId: Long, status: Long) {
        app.downloadDatabase
                .use {
                    insert(DownloadDatabaseOpenHelper.DOWNLOAD_TABLE_NAME,
                            "remote_id" to remoteId,
                            "download_id" to downloadId,
                            "status" to status
                    )
                }
    }

    fun getDownloadByRemoteId(remoteId: Long): Download? {
        return app
                .downloadDatabase
                .readableDatabase
                .select(DownloadDatabaseOpenHelper.DOWNLOAD_TABLE_NAME, "remote_id", "download_id", "status")
                .where("remote_id = {remote_id}", "remote_id" to remoteId)
                .exec {
                    parseOpt(DownloadParser())
                }
    }

    fun getDownloadByDownloadId(downloadId: Long): Download? {
        return app
                .downloadDatabase
                .readableDatabase
                .select(DownloadDatabaseOpenHelper.DOWNLOAD_TABLE_NAME, "remote_id", "download_id", "status")
                .where("download_id = {download_id}", "download_id" to downloadId)
                .exec {
                    parseOpt(DownloadParser())
                }
    }

    fun deleteDownloadByDownloadId(downloadId: Long) {
        app.downloadDatabase
            .use {
                delete(DownloadDatabaseOpenHelper.DOWNLOAD_TABLE_NAME, "download_id = ?", arrayOf(downloadId.toString()))
            }
    }
}

class DownloadDatabaseOpenHelper(context: Context): ManagedSQLiteOpenHelper(context, DB_NAME, null, 1) {

    companion object {
        const val DB_NAME = "DownloadDatabase"
        const val DOWNLOAD_TABLE_NAME = "Downloads"

        private var instance: DownloadDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(context: Context): DownloadDatabaseOpenHelper {
            if (instance == null) {
                instance = DownloadDatabaseOpenHelper(context.applicationContext)
            }

            return instance!!
        }
    }

    override fun onCreate(database: SQLiteDatabase) {
        database
                .createTable(
                    DOWNLOAD_TABLE_NAME,
                    true,
                    "_id" to INTEGER + PRIMARY_KEY + UNIQUE,
                    "remote_id" to INTEGER + UNIQUE,
                    "download_id" to INTEGER,
                    "status" to INTEGER
                )
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}

// Access property for Context
val Context.downloadDatabase: DownloadDatabaseOpenHelper
    get() = DownloadDatabaseOpenHelper.getInstance(applicationContext)