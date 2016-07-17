package br.com.wakim.eslpodclient.view

import android.os.Bundle
import android.support.v4.app.Fragment
import br.com.wakim.eslpodclient.extensions.logContentView
import br.com.wakim.eslpodclient.extensions.logFirebaseContentView
import br.com.wakim.eslpodclient.presenter.Presenter
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

open class BasePresenterFragment<T : Presenter<*>>: Fragment() {

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    lateinit var presenter : T

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        logContentView()
        firebaseAnalytics.logFirebaseContentView()
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