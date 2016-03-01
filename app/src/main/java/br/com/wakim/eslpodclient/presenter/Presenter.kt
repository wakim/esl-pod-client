package br.com.wakim.eslpodclient.presenter

import br.com.wakim.eslpodclient.view.View
import rx.Subscription
import rx.subscriptions.CompositeSubscription

/**
 * Created by wakim on 2/29/16.
 */
abstract class Presenter<T : View>() {

    var view : T? = null

    var compositeSubscription : CompositeSubscription? = CompositeSubscription()
        get() {
            if (field == null) {
                field = CompositeSubscription()
            }

            return field
        }

    fun onDestroy() {
        view = null
    }

    fun onStop() {
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    fun addSubscription(subscription: Subscription) {
        compositeSubscription?.add(subscription)
    }

    fun onResume() {
        // TODO
    }
}