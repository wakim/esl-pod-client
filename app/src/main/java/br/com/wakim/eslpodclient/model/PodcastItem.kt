package br.com.wakim.eslpodclient.model

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntDef
import org.threeten.bp.LocalDate

data class PodcastItem(val title: String, val remoteId: Long, val blurb: String, val mp3Url: String, val date: LocalDate?, val tags: String?, @Type val type : Long = PODCAST) : Parcelable {

    @IntDef(ENGLISH_CAFE, PODCAST)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type()

    constructor(source: Parcel): this(source.readString(), source.readLong(), source.readString(), source.readString(), source.readSerializable() as LocalDate, source.readString(), source.readLong())

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0!!.writeString(title)
        p0.writeLong(remoteId)
        p0.writeString(blurb)
        p0.writeString(mp3Url)
        p0.writeSerializable(date)
        p0.writeString(tags)
        p0.writeLong(type)
    }

    override fun hashCode(): Int {
        return remoteId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        val casted = other as? PodcastItem
        return remoteId == casted?.remoteId ?: super.equals(other)
    }

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