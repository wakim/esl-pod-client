package br.com.wakim.eslpodclient.ui.podcastlist.downloaded.view

import br.com.wakim.eslpodclient.ui.view.View

interface DownloadedListView: View {
    fun setSynchronizeMenuVisible(visible: Boolean)
    fun showAppBarLoading(loading: Boolean)
}