package br.com.wakim.eslpodclient.interactor

import android.database.sqlite.SQLiteDatabase
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.db.DatabaseOpenHelper
import br.com.wakim.eslpodclient.db.database
import br.com.wakim.eslpodclient.extensions.LongAnyParser
import br.com.wakim.eslpodclient.extensions.toContentValues
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.model.PodcastItemDetailRowParser
import org.jetbrains.anko.db.parseOpt
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update

class PodcastDbInteractor(private val app: Application) {

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
                                            "blurb" to blurb,
                                            "mp3_url" to mp3Url,
                                            "date" to date?.toEpochDay(),
                                            "tags" to tags,
                                            "type" to type
                                    ).toContentValues(),
                                    SQLiteDatabase.CONFLICT_IGNORE)
                        }
                    }
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
}