package br.com.wakim.eslpodclient.extensions

fun String.getFileName() : String = split("/").last().substringBeforeLast(".")
fun String.getFileNameWithExtension() : String = split("/").last()
