package br.com.wakim.eslpodclient.rx

import rx.subjects.PublishSubject

class ConnectivityPublishSubject {

    companion object {
        val INSTANCE = PublishSubject.create<Boolean>()
    }
}