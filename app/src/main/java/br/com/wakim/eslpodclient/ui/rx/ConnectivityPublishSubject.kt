package br.com.wakim.eslpodclient.ui.rx

import rx.subjects.PublishSubject

class ConnectivityPublishSubject {

    companion object {
        val INSTANCE: PublishSubject<Boolean> = PublishSubject.create<Boolean>()
    }
}