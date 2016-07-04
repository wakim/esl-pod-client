package br.com.wakim.eslpodclient.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import br.com.wakim.eslpodclient.extensions.logContentView
import br.com.wakim.eslpodclient.presenter.Presenter

open class BasePresenterFragment<T : Presenter<*>>: Fragment() {

    lateinit var presenter : T

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState!!)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.onRestoreInstanceState(savedInstanceState)
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

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logContentView()
    }
}