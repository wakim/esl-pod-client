package br.com.wakim.eslpodclient.ui.view

interface PermissionRequester {
    fun requestPermissions(requestCode: Int, vararg permissions: String)
    fun hasPermission(permission: String): Boolean
}