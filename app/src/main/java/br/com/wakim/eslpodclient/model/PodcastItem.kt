package br.com.wakim.eslpodclient.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntDef
import org.threeten.bp.LocalDate

data class PodcastItem(val title: String, val remoteId: Long, val blurb: String, val mp3Url: String, val date: LocalDate?, val tags: String?, @Type val type : Long = PODCAST, val localPath : String? = null) : Parcelable {

    @IntDef(ENGLISH_CAFE, PODCAST)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type()

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

    constructor(source: Parcel): this(source.readString(), source.readLong(), source.readString(), source.readString(), source.readSerializable() as LocalDate, source.readString(), source.readLong(), source.readString())

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0!!.writeString(title)
        p0.writeLong(remoteId)
        p0.writeString(blurb)
        p0.writeString(mp3Url)
        p0.writeSerializable(date)
        p0.writeString(tags)
        p0.writeLong(type)
        p0.writeString(localPath)
    }

    override fun hashCode(): Int {
        return remoteId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        val casted = other as? PodcastItem
        return remoteId == casted?.remoteId ?: super.equals(other)
    }

    fun isEnglishCafe(): Boolean = ENGLISH_CAFE == type

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        const val ENGLISH_CAFE = 0L
        const val PODCAST = 1L

        @JvmField final val CREATOR: Parcelable.Creator<PodcastItem> = object : Parcelable.Creator<PodcastItem> {
            override fun createFromParcel(source: Parcel): PodcastItem {
                return PodcastItem(source)
            }

            override fun newArray(size: Int): Array<PodcastItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}