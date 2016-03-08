package br.com.wakim.eslpodclient.presenter

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
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

    fun onDestroy() {
        view = null
    }

    open fun onSaveInstanceState(outState: Bundle) {
    }

    open fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    }

    open fun onStart() {
    }

    open fun onStop() {
        compositeSubscription?.unsubscribe()
        compositeSubscription = null
    }

    open fun onResume() {
    }

    inline fun addSubscription(fn : () -> Subscription) = compositeSubscription?.add(fn())

    protected fun hasPermission(context: Context, permission: String) : Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}