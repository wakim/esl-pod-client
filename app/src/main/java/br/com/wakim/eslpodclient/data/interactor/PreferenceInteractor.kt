package br.com.wakim.eslpodclient.data.interactor

import android.content.SharedPreferences
import android.os.Environment
import android.support.v7.preference.PreferenceManager
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.util.extensions.isSAFEnabled
import java.io.File

class PreferenceInteractor(private val app: Application) {

    val sharedPreferences : SharedPreferences

    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
    }

    private val baseFolderKey: String by lazy {
        app.getString(R.string.base_folder_key)
    }

    private val shouldCacheKey: String by lazy {
        app.getString(R.string.should_cache_key)
    }

    fun hasStoredLocation() = sharedPreferences.contains(baseFolderKey)

    fun getDownloadLocation(): String {
        val storedLocation = getStorageLocation()

        if (app.isSAFEnabled() && storedLocation != null) {
            return storedLocation
        } else {
            return "${getDownloadLocationFor(storedLocation ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).absolutePath)}"
        }
    }

    fun getStorageLocation(): String? = sharedPreferences.getString(baseFolderKey, null)

    fun shouldCache() = sharedPreferences.getBoolean(shouldCacheKey, true)

    fun getDownloadLocationFor(base: String): String =
            "$base${File.separator}${app.getString(R.string.app_name)}"
}