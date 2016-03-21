package br.com.wakim.eslpodclient.podcastlist.downloaded.view

import br.com.wakim.eslpodclient.view.View

interface DownloadedListView: View {
    fun setSynchronizeMenuVisibible(visible: Boolean)
    fun showAppBarLoading(loading: Boolean)
}