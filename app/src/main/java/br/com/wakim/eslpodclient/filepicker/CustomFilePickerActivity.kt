package br.com.wakim.eslpodclient.filepicker

import android.os.Environment
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.nononsenseapps.filepicker.AbstractFilePickerFragment
import com.nononsenseapps.filepicker.FilePickerActivity
import java.io.File

class CustomFilePickerActivity: FilePickerActivity() {

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)

        val ab = supportActionBar ?: return

        ab.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun getFragment(startPath: String?, mode: Int, allowMultiple: Boolean, allowCreateDir: Boolean): AbstractFilePickerFragment<File>? {
        val fragment = CustomFilePickerFragment()

        fragment.setArgs(startPath ?: Environment.getExternalStorageDirectory().path, mode, allowMultiple, allowCreateDir)
        fragment.rootDir = Environment.getExternalStorageDirectory()

        return fragment
    }
}