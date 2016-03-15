package br.com.wakim.eslpodclient.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class DatabaseOpenHelper(context: Context): ManagedSQLiteOpenHelper(context, DB_NAME, null, 1) {

    companion object {
        const val DB_NAME = "Database"
        const val DOWNLOADS_TABLE_NAME = "downloads"
        const val FAVORITES_PODCASTS_TABLE_NAME = "favorite_podcasts"
        const val SEEK_POSITIONS_TABLE_NAME = "seek_positions"
    }

    override fun onCreate(database: SQLiteDatabase) {
        database
                .createTable(
                        DOWNLOADS_TABLE_NAME,
                        true,
                        "remote_id" to INTEGER + PRIMARY_KEY + UNIQUE,
                        "download_id" to INTEGER,
                        "status" to INTEGER
                )

        database
                .createTable(
                        FAVORITES_PODCASTS_TABLE_NAME,
                        true,
                        "remote_id" to INTEGER + PRIMARY_KEY + UNIQUE,
                        "title" to TEXT,
                        "blurb" to TEXT,
                        "mp3_url" to TEXT,
                        "date" to INTEGER,
                        "tags" to TEXT,
                        "type" to INTEGER,
                        "favorited_date" to INTEGER
                )

        database
                .createTable(
                        SEEK_POSITIONS_TABLE_NAME,
                        true,
                        "remote_id" to INTEGER + PRIMARY_KEY + UNIQUE,
                        "last_seek_pos" to INTEGER
                )
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}

val Context.database: DatabaseOpenHelper
    get() = DatabaseOpenHelper(applicationContext)