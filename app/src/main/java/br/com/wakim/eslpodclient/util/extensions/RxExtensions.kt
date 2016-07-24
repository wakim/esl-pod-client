package br.com.wakim.eslpodclient.util.extensions

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.android.receiver.ConnectivityException
import rx.Observable
import rx.Single
import rx.SingleSubscriber
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

fun <T> Observable<T>.ofIOToMainThread(): Observable<T> =
    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.ofIOToMainThread(): Single<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.ofComputationToMainThread(): Single<T> =
    subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.ofComputationToMainThread(): Observable<T> =
    subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.connected(): Single<T> {
    return Single.defer { ->
        if (Application.INSTANCE?.connected ?: false) {
            return@defer this
        }

        return@defer Single.error<T>(ConnectivityException.INSTANCE)
    }
}

fun <T> Observable<T>.connected(): Observable<T> {
    return Observable.defer { ->
        if (Application.INSTANCE?.connected ?: false) {
            return@defer this
        }

        return@defer Observable.error<T>(ConnectivityException.INSTANCE)
    }
}

fun <T> SingleSubscriber<T>.onSuccessIfSubscribed(t: T) {
    if (!isUnsubscribed) {
        onSuccess(t)
    }
}

fun <T> Subscriber<T>.onNextIfSubscribed(t: T) {
    if (!isUnsubscribed) {
        onNext(t)
    }
}