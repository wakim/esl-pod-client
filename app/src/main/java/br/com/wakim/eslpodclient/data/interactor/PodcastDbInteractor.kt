package br.com.wakim.eslpodclient.data.interactor

import android.database.sqlite.SQLiteDatabase
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.data.db.DatabaseOpenHelper
import br.com.wakim.eslpodclient.data.db.database
import br.com.wakim.eslpodclient.data.model.*
import br.com.wakim.eslpodclient.util.extensions.LongAnyParser
import br.com.wakim.eslpodclient.util.extensions.toContentValues
import org.jetbrains.anko.db.*
import org.threeten.bp.ZonedDateTime

class PodcastDbInteractor(private val app: Application) {

    fun insertPodcast(podcastItem: PodcastItem) {
        insertPodcasts(arrayListOf(podcastItem))
    }

    fun insertPodcasts(podcasts: List<PodcastItem>) {
        app.database
                .use {
                    podcasts.asSequence().forEach { podcastItem ->
                        with (podcastItem) {
                            insertWithOnConflict(
                                    DatabaseOpenHelper.PODCASTS_TABLE_NAME,
                                    null,
                                    arrayOf(
                                            "remote_id" to remoteId,
                                            "title" to title,
                                            "mp3_url" to mp3Url,
                                            "blurb" to blurb,
                                            "date" to date?.toEpochDay(),
                                            "tags" to tags,
                                            "type" to type
                                    ).toContentValues(),
                                    SQLiteDatabase.CONFLICT_IGNORE)
                        }
                    }
                }
    }

    fun getPodcasts(page: Int, limit: Int): List<PodcastItem> =
        app.database
                .use {
                    select(DatabaseOpenHelper.PODCASTS_TABLE_NAME)
                            .columns(
                                    "remote_id",
                                    "title",
                                    "mp3_url",
                                    "blurb",
                                    "date",
                                    "tags",
                                    "type"
                            )
                            .orderBy("_id", SqlOrderDirection.ASC)
                            .limit(page * limit, limit)
                            .exec {
                                parseList(PodcastItemRowParser())
                            }
                }

    fun insertLastSeekPos(remoteId: Long, seekPos: Int): Boolean =
            app.database
                    .use {
                        update(DatabaseOpenHelper.PODCASTS_TABLE_NAME, "last_seek_pos" to seekPos)
                                .where("remote_id = {remoteId}", "remoteId" to remoteId)
                                .exec() > 0
                    }

    fun getLastSeekPos(remoteId: Long): Any? =
            app.database
                    .use {
                        select(DatabaseOpenHelper.PODCASTS_TABLE_NAME)
                                .column("last_seek_pos")
                                .where("remote_id = {remoteId}", "remoteId" to remoteId)
                                .exec {
                                    parseOpt(LongAnyParser())
                                }
                    }

    fun getPodcastDetail(podcastItem: PodcastItem): PodcastItemDetail? =
            app.database
                    .use {
                        select(DatabaseOpenHelper.PODCASTS_TABLE_NAME)
                                .columns("remote_id", "title", "script", "type", "slow_index", "explanation_index", "normal_index")
                                .where("remote_id = {remoteId} AND script IS NOT NULL", "remoteId" to podcastItem.remoteId)
                                .exec {
                                    parseOpt(PodcastItemDetailRowParser())
                                }
                    }

    fun insertPodcastDetail(podcastItemDetail: PodcastItemDetail): Boolean =
            app.database
                    .use {
                        with (podcastItemDetail) {
                            update(
                                    DatabaseOpenHelper.PODCASTS_TABLE_NAME,
                                    arrayOf(
                                            "script" to script,
                                            "slow_index" to podcastItemDetail.seekPos?.slow,
                                            "explanation_index" to podcastItemDetail.seekPos?.explanation,
                                            "normal_index" to podcastItemDetail.seekPos?.normal
                                    ).toContentValues(),
                                    "remote_id = ?",
                                    arrayOf(remoteId.toString())
                            ) > 0
                        }
                    }

    fun addFavorite(podcastItem: PodcastItem): Boolean =
            app.database
                    .use {
                        val favoritedDate = ZonedDateTime.now().toEpochSecond()

                        with (podcastItem) {
                            update(DatabaseOpenHelper.PODCASTS_TABLE_NAME, "favorited_date" to favoritedDate)
                                    .where("remote_id = {remoteId}", "remoteId" to remoteId)
                                    .exec() > 0
                        }
                    }

    fun removeFavorite(podcastItem: PodcastItem): Boolean =
            app.database
                    .use {
                        update(DatabaseOpenHelper.PODCASTS_TABLE_NAME,
                                arrayOf("favorited_date" to null).toContentValues(),
                                "remote_id = ?",
                                arrayOf(podcastItem.remoteId.toString())
                        ) > 0
                    }

    fun getFavorites(page: Int, limit: Int): List<PodcastItem> =
                app.database
                        .use {
                            select(DatabaseOpenHelper.PODCASTS_TABLE_NAME)
                                    .columns(
                                            "remote_id",
                                            "title",
                                            "mp3_url",
                                            "blurb",
                                            "date",
                                            "tags",
                                            "type"
                                    )
                                    .where("favorited_date IS NOT NULL")
                                    .orderBy("date", SqlOrderDirection.DESC)
                                    .limit(page * limit, limit)
                                    .exec {
                                        parseList(PodcastItemRowParser())
                                    }
                        }

    fun getDownloaded(page: Int, limit: Int): List<PodcastItem> =
            app.database
                    .use {
                        select("${DatabaseOpenHelper.PODCASTS_TABLE_NAME} p")
                                .columns(
                                        "remote_id",
                                        "title",
                                        "mp3_url",
                                        "blurb",
                                        "date",
                                        "tags",
                                        "type"
                                )
                                .where("EXISTS (SELECT 1 FROM ${DatabaseOpenHelper.DOWNLOADS_TABLE_NAME} d WHERE d.remote_id = p.remote_id AND d.status = ${DownloadStatus.DOWNLOADED})")
                                .orderBy("date", SqlOrderDirection.DESC)
                                .limit(page * limit, limit)
                                .exec {
                                    parseList(PodcastItemRowParser())
                                }
                    }

    fun clearPodcasts() {
        app.database.use {
            delete(DatabaseOpenHelper.PODCASTS_TABLE_NAME)
        }
    }
}