package br.com.wakim.eslpodclient.view

import android.os.Bundle
import br.com.wakim.eslpodclient.presenter.Presenter

open class BasePresenterActivity<T : Presenter<*>> : BaseActivity(), PermissionRequester {

    companion object {
        const val PARENT_EXTRA = "PARENT_EXTRA"
    }

    lateinit var presenter : T

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        presenter.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }
}