package br.com.wakim.eslpodclient.filepicker

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

    override fun getRoot(): File? {
        return rootDir ?: super.getRoot()
    }
}