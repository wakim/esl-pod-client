package br.com.wakim.eslpodclient.view

interface PermissionRequester {
    fun requestPermissions(requestCode: Int, vararg permissions: String)
    fun hasPermission(permission: String): Boolean
}