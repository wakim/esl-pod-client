package br.com.wakim.eslpodclient.data.model

data class PublishSubjectItem<T>(val type: Int, val t: T? = null) {
    companion object {
        const val PODCAST_SYNC_TYPE = 0;
        const val PODCAST_SYNC_ENDED_TYPE = 1;
    }
}