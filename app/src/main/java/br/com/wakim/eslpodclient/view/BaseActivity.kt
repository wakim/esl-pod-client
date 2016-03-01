package br.com.wakim.eslpodclient.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.com.wakim.eslpodclient.dagger.ActivityComponent
import br.com.wakim.eslpodclient.dagger.DaggerActivityComponent
import br.com.wakim.eslpodclient.presenter.Presenter

/**
 * Created by wakim on 2/29/16.
 */
open class BaseActivity<T : Presenter<*>> : AppCompatActivity() {

    lateinit var activityComponent : ActivityComponent
    var presenter : T? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent = DaggerActivityComponent.builder()
                .build()
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
}