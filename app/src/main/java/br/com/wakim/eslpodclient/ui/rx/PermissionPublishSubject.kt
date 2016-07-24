package br.com.wakim.eslpodclient.ui.rx

import android.content.pm.PackageManager
import rx.subjects.PublishSubject

class PermissionPublishSubject {

    companion object {
        val INSTANCE: PublishSubject<Permission> = PublishSubject.create<Permission>()
    }

    class Permission(val requestCode: Int, val permissions: Array<out String>, val grantResults: IntArray) {
        fun isGranted() : Boolean {

            for (result: Int in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }

            return true
        }
    }
}