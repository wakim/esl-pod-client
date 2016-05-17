package br.com.wakim.eslpodclient.model

data class PublishSubjectItem<T>(val type: Int, val t: T) {
    companion object {
        final const val PODCAST_SYNC_TYPE = 0;
    }
}