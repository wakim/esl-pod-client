package br.com.wakim.eslpodclient.ui.settings.view

import android.os.Bundle
import com.nononsenseapps.filepicker.FilePickerFragment
import java.io.File

class CustomFilePickerFragment: FilePickerFragment() {

    var rootDir: File? = null

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putString("${context.packageName}.CustomFilePickerFragment.FILE", rootDir?.absolutePath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootDirPath = savedInstanceState?.getString("${context.packageName}.CustomFilePickerFragment.FILE")

        if (rootDirPath != null) {
            rootDir = File(rootDirPath)
        }
    }

    override fun getRoot() = rootDir ?: super.getRoot()

    fun onBackPressed(): Boolean {
        if (mCurrentPath == null) {
            return true
        }

        if (!mCurrentPath.path.equals(rootDir?.path)) {
            goUp()
            return false
        }

        return true
    }
}