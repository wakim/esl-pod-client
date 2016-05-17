package br.com.wakim.eslpodclient.podcastlist.downloaded.view

import br.com.wakim.eslpodclient.view.View

interface DownloadedListView: View {
    fun setSynchronizeMenuVisible(visible: Boolean)
    fun showAppBarLoading(loading: Boolean)
}