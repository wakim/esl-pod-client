package br.com.wakim.eslpodclient.view

interface PermissionRequester {
    fun requestPermission(permission: String, requestCode: Int)
}