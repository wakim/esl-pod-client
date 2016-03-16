package br.com.wakim.eslpodclient.interactor

import android.content.SharedPreferences
import android.os.Environment
import android.support.v7.preference.PreferenceManager
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.R
import java.io.File

class PreferenceInteractor(private val app: Application) {

    val sharedPreferences : SharedPreferences

    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
    }

    fun getDownloadLocation(): String =
            "${getDownloadLocationFor(sharedPreferences.getString(app.getString(R.string.base_folder_key), null) ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS).absolutePath)}"

    fun getDownloadLocationFor(base: String): String =
            "$base${File.separator}${app.getString(R.string.app_name)}"

}