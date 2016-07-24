package br.com.wakim.eslpodclient.data.model

import org.jetbrains.anko.db.RowParser

data class Download(val remoteId: Long, val filename: String, val downloadId: Long, val status: Long)

class DownloadParser : RowParser<Download> {
    override fun parseRow(columns: Array<Any?>): Download =
            Download(columns[0] as Long, columns[1] as String, columns[2] as Long, columns[3] as Long)
}