package br.com.wakim.eslpodclient.model

import android.os.Parcel
import android.os.Parcelable

data class PodcastItemDetail(var remoteId : Long = 0, var title : String? = null, var script: String? = null, @PodcastItem.Type val type: Long, var seekPos : SeekPos? = null) : Parcelable {
    constructor(source: Parcel): this(source.readLong(), source.readString(), source.readString(), source.readLong(), source.readParcelable(SeekPos::class.java.classLoader))

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeLong(remoteId)

        p0.writeString(title)
        p0.writeString(script)
        p0.writeLong(type)

        p0.writeParcelable(seekPos, p1)
    }

    override fun describeContents(): Int = 0

    companion object {
        final const val INVALID_SEEK_POS = -1

        @JvmField final val CREATOR: Parcelable.Creator<PodcastItemDetail> = object : Parcelable.Creator<PodcastItemDetail> {
            override fun createFromParcel(source: Parcel): PodcastItemDetail {
                return PodcastItemDetail(source)
            }

            override fun newArray(size: Int): Array<PodcastItemDetail?> {
                return arrayOfNulls(size)
            }
        }
    }

    fun isEnglishCafe(): Boolean = type == PodcastItem.ENGLISH_CAFE
}

data class SeekPos(val slow : Int = PodcastItemDetail.INVALID_SEEK_POS, val explanation : Int = PodcastItemDetail.INVALID_SEEK_POS, val normal : Int = PodcastItemDetail.INVALID_SEEK_POS) : Parcelable {
    constructor(source: Parcel): this(source.readInt(), source.readInt(), source.readInt())

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(slow)
        p0.writeInt(explanation)
        p0.writeInt(normal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        final const val INVALID_SEEK_POS = -1

        @JvmField final val CREATOR: Parcelable.Creator<SeekPos> = object : Parcelable.Creator<SeekPos> {
            override fun createFromParcel(source: Parcel): SeekPos {
                return SeekPos(source)
            }

            override fun newArray(size: Int): Array<SeekPos?> {
                return arrayOfNulls(size)
            }
        }
    }
}