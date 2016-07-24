package br.com.wakim.eslpodclient.data.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntDef
import org.jetbrains.anko.db.RowParser
import org.threeten.bp.LocalDate

data class PodcastItem(val remoteId: Long, val title: String, val mp3Url: String, val blurb: String? = null, val date: LocalDate?, val tags: String?, @Type val type : Long = PODCAST) : Parcelable {

    @IntDef(ENGLISH_CAFE, PODCAST)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type()

    @IntDef(LOCAL, REMOTE, CACHING)
    @Retention(AnnotationRetention.SOURCE)
    annotation class StreamType()

    val userFriendlyTitle: String by lazy {
        if (type == ENGLISH_CAFE) {
            return@lazy title
        }

        val indexOf = title.indexOf(" – ")

        if (indexOf > -1) {
            return@lazy title.substring(indexOf + 3)
        }

        title
    }

    val podcastName: String by lazy {
        if (type == ENGLISH_CAFE) {
            return@lazy title
        }

        val indexOf = title.indexOf(" – ")

        if (indexOf > -1) {
            return@lazy title.substring(0, indexOf)
        }

        title
    }

    val tagList: List<String> by lazy {
        tags?.split(",") ?: emptyList<String>()
    }

    constructor(source: Parcel): this(
            source.readLong(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readString(),
            source.readLong())

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeLong(remoteId)
        p0.writeString(title)
        p0.writeString(blurb)
        p0.writeString(mp3Url)
        p0.writeSerializable(date)
        p0.writeString(tags)
        p0.writeLong(type)
    }

    override fun hashCode(): Int  = remoteId.hashCode()

    override fun equals(other: Any?): Boolean {
        val casted = other as? PodcastItem
        return remoteId == casted?.remoteId ?: super.equals(other)
    }

    fun isEnglishCafe(): Boolean = ENGLISH_CAFE == type

    override fun describeContents(): Int = 0

    companion object {
        const val ENGLISH_CAFE = 0L
        const val PODCAST = 1L

        const val LOCAL = 0L
        const val REMOTE = 1L
        const val CACHING = 2L

        @JvmField val CREATOR: Parcelable.Creator<PodcastItem> = object : Parcelable.Creator<PodcastItem> {
            override fun createFromParcel(source: Parcel): PodcastItem {
                return PodcastItem(source)
            }

            override fun newArray(size: Int): Array<PodcastItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}

data class DownloadStatus(val remoteId: Long = -1, val localPath: String? = null, val downloadId: Long = -1, @Status var status: Long = DownloadStatus.NOT_DOWNLOADED) : Parcelable {

    @IntDef(DownloadStatus.DOWNLOADING, DownloadStatus.DOWNLOADED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Status()

    constructor(source: Parcel): this(source.readLong(), source.readString(), source.readLong(), source.readLong())

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeLong(remoteId)
        p0.writeString(localPath)
        p0.writeLong(downloadId)
        p0.writeLong(status)
    }

    override fun describeContents(): Int = 0

    companion object {
        const val NO_PERMISSION = -1L
        const val NOT_DOWNLOADED = 0L
        const val DOWNLOADING = 1L
        const val DOWNLOADED = 2L

        @JvmField val CREATOR: Parcelable.Creator<DownloadStatus> = object : Parcelable.Creator<DownloadStatus> {
            override fun createFromParcel(source: Parcel): DownloadStatus {
                return DownloadStatus(source)
            }

            override fun newArray(size: Int): Array<DownloadStatus?> {
                return arrayOfNulls(size)
            }
        }
    }
}

class PodcastItemRowParser: RowParser<PodcastItem> {
    override fun parseRow(columns: Array<Any?>): PodcastItem {
        val date = columns[4] as Long
        return PodcastItem(columns[0] as Long, columns[1] as String, columns[2] as String, columns[3] as? String, LocalDate.ofEpochDay(date), columns[5] as String, columns[6] as Long)
    }
}