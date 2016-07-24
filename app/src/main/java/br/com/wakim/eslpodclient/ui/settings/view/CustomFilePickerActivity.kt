package br.com.wakim.eslpodclient.ui.settings.view

import android.os.Environment
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.Toast
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.util.extensions.isVisible
import com.nononsenseapps.filepicker.AbstractFilePickerFragment
import com.nononsenseapps.filepicker.FilePickerActivity
import java.io.File

class CustomFilePickerActivity: FilePickerActivity() {

    var fragment: CustomFilePickerFragment? = null
    var toast: Toast? = null

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

    override fun getFragment(startPath: String?, mode: Int, allowMultiple: Boolean, allowCreateDir: Boolean, allowExistingFile: Boolean, singleClick: Boolean): AbstractFilePickerFragment<File> {
        val fragment = CustomFilePickerFragment()

        fragment.setArgs(startPath ?: Environment.getExternalStorageDirectory().path, mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick)
        fragment.rootDir = Environment.getExternalStorageDirectory()

        this.fragment = fragment

        return fragment
    }

    override fun onBackPressed() {
        fragment?.let {
            if (!it.onBackPressed()) {
                return
            }

            if (toast?.isVisible() ?: false) {
                toast!!.cancel()
                super.onBackPressed()
            } else {
                toast = Toast.makeText(this, R.string.press_back_again_to_leave, Toast.LENGTH_LONG)
                toast!!.show()
            }

            return
        }

        super.onBackPressed()
    }
}