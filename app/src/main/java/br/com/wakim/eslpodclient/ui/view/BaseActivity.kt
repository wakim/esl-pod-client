package br.com.wakim.eslpodclient.ui.view

import android.content.Context
import android.content.pm.PackageManager
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NavUtils
import android.support.v4.app.TaskStackBuilder
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.ActivityComponent
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.dagger.module.ActivityModule
import br.com.wakim.eslpodclient.ui.rx.PermissionPublishSubject
import br.com.wakim.eslpodclient.util.extensions.snack
import butterknife.ButterKnife
import butterknife.Unbinder
import org.jetbrains.anko.find
import org.jetbrains.anko.findOptional
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

abstract class BaseActivity: AppCompatActivity(), PermissionRequester {

    companion object {
        const val PARENT_EXTRA = "PARENT_EXTRA"
    }

    var unbinder: Unbinder? = null

    lateinit var activityComponent: ActivityComponent

    var toolbar: Toolbar? = null

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()
        unbinder?.unbind()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        toolbar = findOptional(R.id.toolbar)
        unbinder = ButterKnife.bind(this)

        toolbar?.let {
            setSupportActionBar(toolbar)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (intent.hasExtra(PARENT_EXTRA)) {
            supportFinishAfterTransition()
            return true
        }

        val parentIntent = NavUtils.getParentActivityIntent(this)

        if (parentIntent == null) {
            supportFinishAfterTransition()
            return true
        }

        if (NavUtils.shouldUpRecreateTask(this, parentIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(parentIntent)
                    .startActivities()

            supportFinishAfterTransition()
            return true
        } else {
            startActivity(parentIntent)
            supportFinishAfterTransition()

            return true
        }
    }

    fun getAppComponent() : AppComponent {
        return applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent
    }

    override fun getSystemService(name: String?): Any? {
        if (ActivityComponent::class.java.simpleName == name) {
            return activityComponent
        }

        return super.getSystemService(name)
    }

    fun createActivityComponent() {
        activityComponent = getAppComponent().plus(ActivityModule(this))
    }

    fun showNavigationArrow() {
        val ab = supportActionBar ?: return
        ab.setDisplayHomeAsUpEnabled(true)
    }

    open fun showMessage(messageResId: Int): Snackbar {
        val view = findOptional<CoordinatorLayout>(R.id.coordinator_layout) ?: find(android.R.id.content)

        return snack(view, message = messageResId)
    }

    open fun showMessage(messageResId: Int, action: String, clickListener: (() -> Unit)?) {
        showMessage(messageResId).setAction(action) {
            clickListener?.invoke()
        }
    }

    override fun requestPermissions(requestCode: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun hasPermission(permission: String) = ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionPublishSubject
                .INSTANCE
                .onNext(PermissionPublishSubject.Permission(requestCode, permissions, grantResults))
    }
}