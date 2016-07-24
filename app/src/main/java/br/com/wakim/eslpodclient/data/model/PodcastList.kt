package br.com.wakim.eslpodclient.data.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class PodcastList(var currentPageToken : String? = null, var nextPageToken : String? = null) : Parcelable {

    var list : ArrayList<PodcastItem> = ArrayList()

    constructor(source: Parcel) : this() {
        source.readList(list, PodcastList::class.java.classLoader)

        currentPageToken = source.readString()
        nextPageToken = source.readString()
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeTypedList(list)
        p0.writeString(currentPageToken)
        p0.writeString(nextPageToken)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<PodcastList> = object : Parcelable.Creator<PodcastList> {
            override fun createFromParcel(source: Parcel): PodcastList {
                return PodcastList(source)
            }

            override fun newArray(size: Int): Array<PodcastList?> {
                return arrayOfNulls(size)
            }
        }
    }
}
