package br.com.wakim.eslpodclient.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class DatabaseOpenHelper(context: Context): ManagedSQLiteOpenHelper(context, DB_NAME, null, 1) {

    companion object {
        const val DB_NAME = "Database"
        const val DOWNLOADS_TABLE_NAME = "downloads"
        const val PODCASTS_TABLE_NAME = "podcasts"
    }

    override fun onCreate(database: SQLiteDatabase) {
        database
                .createTable(
                        DOWNLOADS_TABLE_NAME,
                        true,
                        "remote_id" to INTEGER + PRIMARY_KEY + UNIQUE,
                        "filename" to TEXT,
                        "download_id" to INTEGER,
                        "status" to INTEGER
                )

        database
                .createTable(
                        PODCASTS_TABLE_NAME,
                        true,
                        "_id" to INTEGER + PRIMARY_KEY,
                        "remote_id" to INTEGER + UNIQUE,
                        "title" to TEXT,
                        "blurb" to TEXT,
                        "mp3_url" to TEXT,
                        "date" to INTEGER,
                        "tags" to TEXT,
                        "type" to INTEGER,
                        "last_seek_pos" to INTEGER,
                        "script" to TEXT,
                        "slow_index" to INTEGER,
                        "explanation_index" to INTEGER,
                        "normal_index" to INTEGER,
                        "favorited_date" to INTEGER
                )
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }
}

val Context.database: DatabaseOpenHelper
    get() = DatabaseOpenHelper(applicationContext)