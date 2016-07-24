package br.com.wakim.eslpodclient.ui.presenter

import br.com.wakim.eslpodclient.ui.view.View
import rx.Subscription
import rx.subscriptions.CompositeSubscription

abstract class Presenter<T : View>() {

    var view: T? = null

    val compositeSubscription: CompositeSubscription = CompositeSubscription()

    open fun onDestroy() {
        view = null
    }

    open fun onStart() {
    }

    open fun onStop() {
        compositeSubscription.clear()
    }

    open fun onResume() { }

    inline fun addSubscription(fn: () -> Subscription) = compositeSubscription.add(fn())
}