package br.com.wakim.eslpodclient.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class PodcastList(var currentPageUrl : String? = null, var nextPageUrl : String? = null) : Parcelable {

    var list : ArrayList<PodcastItem> = ArrayList()

    constructor(source: Parcel) : this() {
        source.readList(list, PodcastList::class.java.classLoader)

        currentPageUrl = source.readString()
        nextPageUrl = source.readString()
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeTypedList(list)
        p0?.writeString(currentPageUrl)
        p0?.writeString(nextPageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField final val CREATOR: Parcelable.Creator<PodcastList> = object : Parcelable.Creator<PodcastList> {
            override fun createFromParcel(source: Parcel): PodcastList {
                return PodcastList(source)
            }

            override fun newArray(size: Int): Array<PodcastList?> {
                return arrayOfNulls(size)
            }
        }
    }
}
