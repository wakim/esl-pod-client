package br.com.wakim.eslpodclient.preference

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager {

    companion object {
        final const val PREFERENCES = "PREF";
        final const val DOWNLOAD_LOCATION_KEY = "DOWNLOAD_LOCATION"
    }

    val sharedPreferences : SharedPreferences

    constructor(context : Context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }

    fun getDownloadLocation(): String? = sharedPreferences.getString(DOWNLOAD_LOCATION_KEY, null)
}