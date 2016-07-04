package br.com.wakim.eslpodclient.service

import android.os.Binder

abstract class TypedBinder<out T> : Binder() {
    abstract fun getService() : T?
}