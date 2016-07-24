package br.com.wakim.eslpodclient.ui.podcastlist.presenter

import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.BuildConfig
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.android.receiver.ConnectivityException
import br.com.wakim.eslpodclient.android.service.PlaylistManager
import br.com.wakim.eslpodclient.data.interactor.FavoritedPodcastItemInteractor
import br.com.wakim.eslpodclient.data.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.data.interactor.StorageInteractor
import br.com.wakim.eslpodclient.data.model.DownloadStatus
import br.com.wakim.eslpodclient.data.model.PodcastItem
import br.com.wakim.eslpodclient.data.model.PublishSubjectItem
import br.com.wakim.eslpodclient.ui.podcastlist.view.PodcastListView
import br.com.wakim.eslpodclient.ui.presenter.Presenter
import br.com.wakim.eslpodclient.ui.rx.ConnectivityPublishSubject
import br.com.wakim.eslpodclient.ui.rx.PermissionPublishSubject
import br.com.wakim.eslpodclient.ui.view.PermissionRequester
import br.com.wakim.eslpodclient.util.extensions.ofIOToMainThread
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import java.util.*

open class PodcastListPresenter(protected val app: Application,
                                protected val publishSubject: PublishSubject<PublishSubjectItem<Any>>,
                                private   val interactor: PodcastInteractor,
                                private   val permissionRequester: PermissionRequester,
                                private   val playlistManager: PlaylistManager,
                                protected val storageInteractor: StorageInteractor,
                                private   val favoritedPodcastItemInteractor: FavoritedPodcastItemInteractor) : Presenter<PodcastListView>() {

    var items : ArrayList<PodcastItem> = ArrayList()

    var nextPageToken : String? = null

    var downloadPodcastItem: PodcastItem? = null

    var usingCache: Boolean = true

    var loaded = false

    fun onRestoreInstanceState(items: ArrayList<PodcastItem>, nextPageToken: String?, downloadPodcastItem: PodcastItem?, usingCache: Boolean) {
            this.items = items
            this.nextPageToken = nextPageToken
            this.downloadPodcastItem = downloadPodcastItem
            this.usingCache = usingCache
    }

    override fun onStart() {
        super.onStart()

        if (items.isNotEmpty()) {
            playlistManager.setItems(items)
        }

        loadFirstPageIfNeeded()

        addSubscription {
            PermissionPublishSubject
                    .INSTANCE
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe { permission ->
                        if (permission.requestCode == Application.LIST_DOWNLOAD_WRITE_STORAGE_PERMISSION) {
                            if (downloadPodcastItem != null && permission.isGranted()) {
                                download(downloadPodcastItem!!)
                            } else {
                                view!!.showMessage(R.string.write_external_storage_permission_needed_to_download)
                            }
                        }

                        if (permission.requestCode == Application.LIST_REMOVE_DOWNLOAD_WRITE_STORAGE_PERMISSION) {
                            if (downloadPodcastItem != null && permission.isGranted()) {
                                removeDownload(downloadPodcastItem!!)
                            } else {
                                view!!.showMessage(R.string.write_external_storage_permission_needed_to_remove_download)
                            }
                        } else if (permission.requestCode == Application.LIST_DELETE_DOWNLOAD_WRITE_STORAGE_PERMISSION) {
                            if (downloadPodcastItem != null && permission.isGranted()) {
                                removeAndDeleteDownload(downloadPodcastItem!!)
                            } else {
                                view!!.showMessage(R.string.write_external_storage_permission_needed_to_remove_download)
                            }
                        }
                    }
        }

        addSubscription {
            ConnectivityPublishSubject
                    .INSTANCE
                    .subscribe { connected ->
                        if (connected && !loaded) {
                            loadFirstPageIfNeeded()
                        }
                    }
        }
    }

    override fun onResume() {
        super.onResume()
        playlistManager.setItems(items)
    }

    override fun onStop() {
        super.onStop()
    }

    fun loadFirstPageIfNeeded() {
        if (items.isEmpty()) {
            loadNextPage()
        } else {
            view?.let {
                it.setItems(items)
                it.hasMore = nextPageToken != null
            }
        }
    }

    fun loadNextPage() {
        loaded = true
        view!!.setLoading(true)

        addSubscription {
            val single = if (usingCache)  {
                interactor.getCachedPodcasts(nextPageToken)
                        .flatMap { podcastList ->
                            if (podcastList.list.isEmpty()) {
                                usingCache = false
                                return@flatMap interactor.getPodcasts(nextPageToken)
                            }

                            return@flatMap Single.just(podcastList)
                        }
            } else
                interactor.getPodcasts(nextPageToken)

            single.ofIOToMainThread()
                    .subscribe (
                            { podcastList ->
                                items.addAll(podcastList.list)
                                nextPageToken = podcastList.nextPageToken

                                view?.let {
                                    it.setLoading(false)
                                    it.addItems(podcastList.list)

                                    it.hasMore = podcastList.nextPageToken != null

                                    playlistManager.setItems(items)
                                }
                            },
                            { e: Throwable ->
                                if (e is ConnectivityException) {
                                    view?.showMessage(R.string.no_connectivity)
                                }

                                view?.setLoading(false)
                            }
                    )
        }
    }

    open fun onRefresh() {
        usingCache = false

        items.clear()
        playlistManager.clearItems()

        nextPageToken = null

        loadNextPage()
    }

    fun download(podcastItem: PodcastItem) {
        if (!permissionRequester.hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionRequester.requestPermissions(Application.LIST_DOWNLOAD_WRITE_STORAGE_PERMISSION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)

            downloadPodcastItem = podcastItem

            return
        }

        val downloadStatus = storageInteractor.startDownloadIfNeeded(podcastItem)

        addSubscription {
            downloadStatus.ofIOToMainThread()
                    .subscribe{ downloadStatus ->
                        when (downloadStatus.status) {
                            DownloadStatus.DOWNLOADED -> view?.showMessage(R.string.podcast_already_downloaded)
                            DownloadStatus.DOWNLOADING -> view?.showMessage(R.string.podcast_download_started, app.getString(R.string.cancel)) {
                                storageInteractor.cancelDownload(downloadStatus)
                            }
                        }
                    }
        }
    }

    fun removeDownload(podcastItem: PodcastItem) {
        if (!permissionRequester.hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionRequester.requestPermissions(Application.LIST_REMOVE_DOWNLOAD_WRITE_STORAGE_PERMISSION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)

            downloadPodcastItem = podcastItem

            return
        }

        addSubscription {
            storageInteractor.cancelDownload(podcastItem)
                    .ofIOToMainThread()
                    .subscribe(
                            {
                                items.remove(podcastItem)

                                view?.remove(podcastItem)
                                view?.showMessage(R.string.podcast_removed_from_downloaded)
                            },
                            { e ->
                                view?.showMessage(R.string.error_removing_downloaded)
                            }
                    )
        }
    }

    fun removeAndDeleteDownload(podcastItem: PodcastItem) {
        if (!permissionRequester.hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionRequester.requestPermissions(Application.LIST_DELETE_DOWNLOAD_WRITE_STORAGE_PERMISSION, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)

            downloadPodcastItem = podcastItem

            return
        }

        storageInteractor.deleteDownload(podcastItem)
                .ofIOToMainThread()
                .subscribe(
                        {
                            items.remove(podcastItem)
                            view?.remove(podcastItem)

                            view?.showMessage(R.string.podcast_removed_and_deleted)
                        },
                        { e ->
                            view?.showMessage(R.string.error_removing_and_deleting)
                        }
                )
    }

    fun share(podcastItem: PodcastItem) {
        val url = BuildConfig.DETAIL_URL.format(podcastItem.remoteId.toString())
        val text = app.getString(R.string.share_text, podcastItem.userFriendlyTitle, url)

        view?.share(text)
    }

    fun openWith(podcastItem: PodcastItem) {
        view?.openUrlOnBrowser(BuildConfig.DETAIL_URL.format(podcastItem.remoteId))
    }

    fun favorite(podcastItem: PodcastItem) {
        addSubscription {
            favoritedPodcastItemInteractor.addFavorite(podcastItem)
                    .ofIOToMainThread()
                    .subscribe {
                        view?.showMessage(R.string.podcast_favorited)
                    }
        }
    }

    fun removeFavorite(podcastItem: PodcastItem) {
        addSubscription {
            favoritedPodcastItemInteractor.removeFavorite(podcastItem)
                    .ofIOToMainThread()
                    .subscribe { removed ->
                        items.remove(podcastItem)

                        view?.showMessage(if (removed) R.string.podcast_removed_from_favorites else R.string.error_removing_favorites)
                        view?.remove(podcastItem)
                    }
        }
    }
}
