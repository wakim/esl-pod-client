package br.com.wakim.eslpodclient.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import br.com.wakim.eslpodclient.presenter.Presenter

open class BasePresenterFragment<T : Presenter<*>> : Fragment() {

    lateinit var presenter : T

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onViewCreated(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
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