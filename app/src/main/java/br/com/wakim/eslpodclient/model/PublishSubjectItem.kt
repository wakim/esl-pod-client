package br.com.wakim.eslpodclient.model

data class PublishSubjectItem<T>(val type: Int, val t: T? = null) {
    companion object {
        final const val PODCAST_SYNC_TYPE = 0;
        final const val PODCAST_SYNC_ENDED_TYPE = 1;
    }
}