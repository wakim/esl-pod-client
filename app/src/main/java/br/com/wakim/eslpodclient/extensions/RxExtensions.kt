package br.com.wakim.eslpodclient.extensions

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.receiver.ConnectivityException
import rx.Observable
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

fun <T> Observable<T>.ofIOToMainThread() : Observable<T> =
    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.ofIOToMainThread() : Single<T> =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.ofComputationToMainThread() : Single<T> =
    subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

fun <T> Observable<T>.ofComputationToMainThread() : Observable<T> =
    subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

fun <T> Single<T>.connected(): Single<T> {
    return Single.defer { ->
        if (Application.INSTANCE?.connected ?: false) {
            return@defer this
        }

        return@defer Single.error<T>(ConnectivityException.INSTANCE)
    }
}