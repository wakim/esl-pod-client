package br.com.wakim.eslpodclient.preference

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.support.v7.preference.Preference
import android.util.AttributeSet
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.filepicker.CustomFilePickerActivity
import br.com.wakim.eslpodclient.rx.PermissionPublishSubject
import br.com.wakim.eslpodclient.settings.view.SettingsFragment
import com.nononsenseapps.filepicker.FilePickerActivity

class PickFolderPreference: Preference {

    companion object {
        final const val RC_PICK_FOLDER = 56
    }

    private var settingsFragment: SettingsFragment? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    override fun onClick() {
        super.onClick()

        openFolderPicker()
    }

    fun openFolderPicker() {
        settingsFragment?.let {
            if (!it.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                it.requestPermissions(Application.PICK_FOLDER_READ_STORAGE_PERMISSION, Manifest.permission.READ_EXTERNAL_STORAGE)
                return
            }

            val intent = Intent(it.context, CustomFilePickerActivity::class.java)
            val currentPath = Environment.getExternalStorageDirectory().absolutePath

            intent  .putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                    .putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false)
                    .putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR)
                    .putExtra(FilePickerActivity.EXTRA_START_PATH, currentPath)

            it.startActivityForResult(intent, RC_PICK_FOLDER)
        }
    }

    fun setFragment(settingsFragment: SettingsFragment) {
        this.settingsFragment = settingsFragment
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            val newValue = data!!.data.path

            if (shouldPersist() && callChangeListener(newValue)) {
                persistString(newValue)
            }
        }
    }

    fun onPermissionResult(permission: PermissionPublishSubject.Permission) {
        if (permission.requestCode != Application.PICK_FOLDER_READ_STORAGE_PERMISSION) {
            return
        }

        if (!permission.isGranted()) {
            return
        }

        openFolderPicker()
    }
}