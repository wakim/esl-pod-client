package br.com.wakim.eslpodclient.rx

import rx.subjects.PublishSubject

class DownloadPublishSubject {

    companion object {
        val INSTANCE = DownloadPublishSubject(PublishSubject.create())
    }

    val publishSubject: PublishSubject<Long>

    private constructor(publishSubject: PublishSubject<Long>) {
        this.publishSubject = publishSubject
    }
}