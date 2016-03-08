package br.com.wakim.eslpodclient.service

import android.os.Binder

abstract class TypedBinder<T> : Binder() {
    abstract fun getService() : T?
}