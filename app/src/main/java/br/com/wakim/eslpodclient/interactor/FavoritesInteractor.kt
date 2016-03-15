package br.com.wakim.eslpodclient.interactor

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.db.DatabaseOpenHelper
import br.com.wakim.eslpodclient.db.database
import br.com.wakim.eslpodclient.extensions.insertIgnoringConflict
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemRowParser
import org.jetbrains.anko.db.parseOpt
import org.jetbrains.anko.db.select
import org.threeten.bp.ZonedDateTime
import rx.Single

class FavoritesInteractor(private val app: Application) {
    fun addFavorite(podcastItem: PodcastItem): Single<Long> =
            Single.create<Long> { subscriber ->
                app.database
                    .use {
                        val db = this

                        val favoritedDate = ZonedDateTime.now().toEpochSecond()

                        with (podcastItem) {
                            val id = db.insertIgnoringConflict(
                                    DatabaseOpenHelper.FAVORITES_PODCASTS_TABLE_NAME,
                                    "remote_id" to remoteId,
                                    "title" to title,
                                    "blurb" to blurb,
                                    "mp3_url" to mp3Url,
                                    "date" to date?.toEpochDay(),
                                    "tags" to tags,
                                    "type" to type,
                                    "favorited_date" to favoritedDate)

                            if (!subscriber.isUnsubscribed) {
                                subscriber.onSuccess(id)
                            }
                        }
                    }
            }

    fun removeFavorite(podcastItem: PodcastItem): Single<Boolean>  =
            Single.create<Boolean> { subscriber ->
                app.database
                        .use {
                            val deleted = delete(
                                    DatabaseOpenHelper.FAVORITES_PODCASTS_TABLE_NAME,
                                    "remote_id = ?", arrayOf(podcastItem.remoteId.toString())
                            ) > 0

                            if (!subscriber.isUnsubscribed) {
                                subscriber.onSuccess(deleted)
                            }
                        }
            }

    fun getFavorites(page: Int, limit: Int): Single<List<PodcastItem>> =
            Single.create<List<PodcastItem>> { subscriber ->
                app.database
                        .readableDatabase
                        .select(
                                DatabaseOpenHelper.FAVORITES_PODCASTS_TABLE_NAME,
                                "remote_id",
                                "title",
                                "blurb",
                                "mp3_url",
                                "date",
                                "tags",
                                "type"
                        )
                        .limit(page * limit, limit)
                        .exec {
                                parseOpt(PodcastItemRowParser())
                        }
            }
}