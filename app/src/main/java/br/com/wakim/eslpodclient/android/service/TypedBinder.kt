package br.com.wakim.eslpodclient.android.service

import android.os.Binder

abstract class TypedBinder<out T> : Binder() {
    abstract val service: T?
}