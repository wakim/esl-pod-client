package br.com.wakim.eslpodclient.podcastlist.presenter

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.extensions.bindService
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastList
import br.com.wakim.eslpodclient.podcastlist.view.PodcastListView
import br.com.wakim.eslpodclient.presenter.Presenter
import br.com.wakim.eslpodclient.service.PlayerService
import br.com.wakim.eslpodclient.service.TypedBinder
import rx.SingleSubscriber
import java.util.*

class PodcastListPresenter(private val app: Application, private val interactor: PodcastInteractor) : Presenter<PodcastListView>() {

    companion object {
        private final val ITEMS_EXTRA = "ITEMS"
        private final val NEXT_PAGE_URL_EXTRA = "NEXT_PAGE_URL"
    }

    var items : ArrayList<PodcastItem> = ArrayList()

    var nextPageUrl : String? = null
    var playerSevice: PlayerService? = null

    val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {
            playerSevice = null
        }

        @Suppress("UNCHECKED_CAST")
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            playerSevice = (binder as? TypedBinder<PlayerService>)?.getService()

            if (items.isNotEmpty()) {
                playerSevice!!.playlistManager.setItems(items)
            }

            loadFirstPageIfNeeded()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState : Bundle?) {
        savedInstanceState?.let {
            items = it.getParcelableArrayList(ITEMS_EXTRA)
            nextPageUrl = it.getString(NEXT_PAGE_URL_EXTRA)
        }

        view?.let {
            it.addItems(items)
            it.hasMore = nextPageUrl != null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(ITEMS_EXTRA, items)
        outState.putString(NEXT_PAGE_URL_EXTRA, nextPageUrl)
    }

    override fun onStart() {
        super.onStart()
        app.bindService<PlayerService>(serviceConnection)
    }

    override fun onStop() {
        super.onStop()
        app.unbindService(serviceConnection)
    }

    fun loadFirstPageIfNeeded() {
        if (items.isEmpty()) {
            loadNextPage()
        }
    }

    fun loadNextPage() {
        view!!.setLoading(true)

        addSubscription {
            interactor.getPodcasts(nextPageUrl)
                    .ofIOToMainThread()
                    .subscribe(object : SingleSubscriber<PodcastList>(){
                        override fun onSuccess(podcastList: PodcastList) {
                            items.addAll(podcastList.list)
                            nextPageUrl = podcastList.nextPageUrl

                            view?.let {
                                it.setLoading(false)
                                it.addItems(podcastList.list)

                                it.hasMore = podcastList.nextPageUrl != null

                                playerSevice!!.playlistManager.setItems(items)
                            }
                        }

                        override fun onError(e: Throwable?) {
                        }
                    })
        }
    }
}
