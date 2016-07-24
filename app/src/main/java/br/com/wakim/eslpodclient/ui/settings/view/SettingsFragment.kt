package br.com.wakim.eslpodclient.ui.settings.view

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.ActivityComponent
import br.com.wakim.eslpodclient.data.interactor.StorageInteractor
import br.com.wakim.eslpodclient.ui.rx.PermissionPublishSubject
import br.com.wakim.eslpodclient.ui.view.BaseActivity
import br.com.wakim.eslpodclient.util.browseWithCustomTabs
import br.com.wakim.eslpodclient.util.extensions.logContentView
import br.com.wakim.eslpodclient.util.extensions.logFirebaseContentView
import br.com.wakim.eslpodclient.util.extensions.startActivity
import com.google.firebase.analytics.FirebaseAnalytics
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var storageInteractor: StorageInteractor

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    var pickFolderPreference: PickFolderPreference? = null

    var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        (context.getSystemService(ActivityComponent::class.java.simpleName) as ActivityComponent).inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logContentView()
        firebaseAnalytics.logFirebaseContentView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subscription?.unsubscribe()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        PermissionPublishSubject
                .INSTANCE
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

            it.summary = storageInteractor.getBaseDir().absolutePath
            it.setDefaultValue(it.summary)

            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, value ->
                it.summary = storageInteractor.getBaseDir().absolutePath
                true
            }
        }

        findPreference(getString(R.string.version_key))?.summary = "${BuildConfig.BUILD_TYPE.capitalize()} ${BuildConfig.VERSION_NAME}"

        findPreference(getString(R.string.oss_licenses_key))?.setOnPreferenceClickListener {
            context.startActivity<LicensesActivity>()
            false
        }

        findPreference(getString(R.string.about_developer_key))?.setOnPreferenceClickListener {
            activity.browseWithCustomTabs("https://github.com/wakim")
            false
        }

        findPreference(getString(R.string.source_key))?.setOnPreferenceClickListener {
            activity.browseWithCustomTabs("https://github.com/wakim/esl-pod-client")
            false
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