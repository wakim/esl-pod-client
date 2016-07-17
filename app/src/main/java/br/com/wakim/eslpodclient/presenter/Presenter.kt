package br.com.wakim.eslpodclient.presenter

import br.com.wakim.eslpodclient.view.View
import rx.Subscription
import rx.subscriptions.CompositeSubscription

abstract class Presenter<T : View>() {

    var view : T? = null

    var compositeSubscription : CompositeSubscription? = CompositeSubscription()
        get() {
            if (field == null) {
                field = CompositeSubscription()
            }

            return field
        }

    open fun onDestroy() {
        view = null
    }

    open fun onStart() {
    }

    open fun onStop() {
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    open fun onResume() { }

    inline fun addSubscription(fn : () -> Subscription) = compositeSubscription?.add(fn())
}