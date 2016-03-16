package br.com.wakim.eslpodclient.settings.view

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.ActivityComponent
import br.com.wakim.eslpodclient.extensions.hasPermission
import br.com.wakim.eslpodclient.interactor.PreferenceInteractor
import br.com.wakim.eslpodclient.preference.PickFolderPreference
import br.com.wakim.eslpodclient.rx.PermissionPublishSubject
import br.com.wakim.eslpodclient.view.BaseActivity
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var preferencesInteractor: PreferenceInteractor

    var pickFolderPreference: PickFolderPreference? = null

    var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        (context.getSystemService(ActivityComponent::class.java.simpleName) as ActivityComponent).inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscription?.unsubscribe()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PermissionPublishSubject.INSTANCE
                .publishSubject
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { permission ->
                    pickFolderPreference?.onPermissionResult(permission)
                }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        pickFolderPreference = findPreference(getString(R.string.base_folder_key)) as PickFolderPreference

        pickFolderPreference?.let {
            it.setFragment(this)

            it.summary = preferencesInteractor.getDownloadLocation()
            it.setDefaultValue(it.summary)

            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, value ->
                it.summary = preferencesInteractor.getDownloadLocationFor(value.toString())
                true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pickFolderPreference?.onActivityResult(requestCode, resultCode, data)
    }

    fun hasPermission(permission: String): Boolean =
            (activity as? BaseActivity)?.hasPermission(permission) ?: false

    fun requestPermissions(requestCode: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }
}