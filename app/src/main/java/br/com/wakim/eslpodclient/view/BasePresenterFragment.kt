package br.com.wakim.eslpodclient.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import br.com.wakim.eslpodclient.extensions.logContentView
import br.com.wakim.eslpodclient.extensions.logFirebaseContentView
import br.com.wakim.eslpodclient.presenter.Presenter
import butterknife.ButterKnife
import butterknife.Unbinder
import com.google.firebase.analytics.FirebaseAnalytics
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

open class BasePresenterFragment<T : Presenter<*>>: Fragment() {

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    lateinit var presenter : T

    var unbinder: Unbinder? = null

    val compositeSubscription: CompositeSubscription = CompositeSubscription()

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        logContentView()
        firebaseAnalytics.logFirebaseContentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        unbinder = ButterKnife.bind(this, view)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        unbinder?.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()

        compositeSubscription.clear()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    inline fun addSubscription(fn : () -> Subscription) = compositeSubscription.add(fn())
}