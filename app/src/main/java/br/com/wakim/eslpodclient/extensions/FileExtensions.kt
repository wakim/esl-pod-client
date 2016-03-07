package br.com.wakim.eslpodclient.extensions

fun String.getFileName() : String? =
        split("/").lastOrNull()
