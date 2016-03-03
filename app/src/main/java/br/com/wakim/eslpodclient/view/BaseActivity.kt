package br.com.wakim.eslpodclient.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.com.wakim.eslpodclient.dagger.ActivityComponent
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.presenter.Presenter

open class BaseActivity<T : Presenter<*>> : AppCompatActivity() {

    lateinit var activityComponent : ActivityComponent
    var presenter : T? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent = getAppComponent()!!.plus()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        presenter?.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        presenter?.onSaveInstanceState(outState!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter?.onResume()
    }

    override fun onStop() {
        super.onStop()
        presenter?.onStop()
    }

    fun getAppComponent() : AppComponent? {
        return applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent?
    }
}