package br.com.wakim.eslpodclient.android.receiver

class ConnectivityException: Exception {

    private constructor(): super()

    companion object {
        val INSTANCE = ConnectivityException()
    }
}