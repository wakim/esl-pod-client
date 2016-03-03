package br.com.wakim.eslpodclient.podcastlist.presenter

import android.os.Bundle
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.model.PodcastList
import br.com.wakim.eslpodclient.podcastlist.interactor.PodcastListInteractor
import br.com.wakim.eslpodclient.podcastlist.view.PodcastListView
import br.com.wakim.eslpodclient.presenter.Presenter
import rx.SingleSubscriber
import java.util.*

class PodcastListPresenter(val interactor: PodcastListInteractor) : Presenter<PodcastListView>() {

    companion object {
        private final val PAGES_EXTRA = "PAGES"
    }

    var pages : ArrayList<PodcastList>? = null

    override fun onRestoreInstanceState(savedInstanceState : Bundle?) {
        savedInstanceState?.let {
            pages = it.getParcelableArrayList(PAGES_EXTRA)
        }

        pages?.forEach {
            view?.addItems(it.list)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(PAGES_EXTRA, pages)
    }

    override fun onResume() {
        if (pages?.isEmpty() ?: true) {
            loadNextPage()
        }
    }

    fun loadNextPage() {
        addSubscription {
            interactor.getPodcasts(pages?.last())
                    .ofIOToMainThread()
                    .subscribe(object : SingleSubscriber<PodcastList>(){
                        override fun onSuccess(podcastList: PodcastList) {
                            view?.addItems(podcastList.list)
                            pages!!.add(podcastList)
                        }

                        override fun onError(e: Throwable?) {
                        }
                    })
        }

        pages = pages ?: ArrayList()
    }
}
