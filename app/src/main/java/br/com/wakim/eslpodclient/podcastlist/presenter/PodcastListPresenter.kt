package br.com.wakim.eslpodclient.podcastlist.presenter

import android.os.Bundle
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.model.PodcastList
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.podcastlist.view.PodcastListView
import br.com.wakim.eslpodclient.presenter.Presenter
import rx.SingleSubscriber
import java.util.*

class PodcastListPresenter(private val interactor: PodcastInteractor) : Presenter<PodcastListView>() {

    companion object {
        private final val PAGES_EXTRA = "PAGES"
    }

    var pages : ArrayList<PodcastList>? = null

    override fun onRestoreInstanceState(savedInstanceState : Bundle?) {
        savedInstanceState?.let {
            pages = it.getParcelableArrayList(PAGES_EXTRA)
        }

        view?.let {
            val that = it

            pages?.forEach {
                that.addItems(it.list)
            }

            it.hasMore = pages?.lastOrNull()?.nextPageUrl != null
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
        view!!.setLoading(true)

        addSubscription {
            interactor.getPodcasts(pages?.lastOrNull())
                    .ofIOToMainThread()
                    .subscribe(object : SingleSubscriber<PodcastList>(){
                        override fun onSuccess(podcastList: PodcastList) {
                            pages!!.add(podcastList)

                            view?.let {
                                it.setLoading(false)
                                it.addItems(podcastList.list)

                                it.hasMore = podcastList.nextPageUrl != null
                            }
                        }

                        override fun onError(e: Throwable?) {
                        }
                    })
        }

        pages = pages ?: ArrayList()
    }
}
