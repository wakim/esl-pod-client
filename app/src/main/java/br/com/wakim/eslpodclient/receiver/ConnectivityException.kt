package br.com.wakim.eslpodclient.receiver

class ConnectivityException: Exception {

    private constructor(): super()

    companion object {
        val INSTANCE = ConnectivityException()
    }
}