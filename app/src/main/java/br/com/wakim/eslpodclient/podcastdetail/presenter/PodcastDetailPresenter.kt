package br.com.wakim.eslpodclient.podcastdetail.presenter

import android.os.Bundle
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.extensions.bindService
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.extensions.startService
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.podcastdetail.view.PodcastDetailView
import br.com.wakim.eslpodclient.presenter.Presenter
import br.com.wakim.eslpodclient.service.PlayerService
import rx.SingleSubscriber

class PodcastDetailPresenter(private val podcastInteractor: PodcastInteractor, private val app : Application) : Presenter<PodcastDetailView>() {

    companion object {
        private final val PODCAST_DETAIL = "PODCAST_DETAIL"
        private final val PODCAST_ITEM = "PODCAST_ITEM"
    }

    var podcastDetail : PodcastItemDetail? = null
    var podcastItem : PodcastItem? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(PODCAST_DETAIL, podcastDetail)
        outState.putParcelable(PODCAST_ITEM, podcastItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        podcastDetail = savedInstanceState?.getParcelable<PodcastItemDetail>(PODCAST_DETAIL)
        podcastItem = savedInstanceState?.getParcelable<PodcastItem>(PODCAST_ITEM)
    }

    fun loadDetail(podcastItem: PodcastItem) {
        view!!.setLoading(true)

        addSubscription() {
            podcastInteractor.getPodcastDetail(podcastItem)
                    .ofIOToMainThread()
                    .subscribe(object : SingleSubscriber<PodcastItemDetail>(){
                        override fun onSuccess(detail: PodcastItemDetail) {
                            podcastDetail = detail
                            bindDetail()
                        }

                        override fun onError(e: Throwable?) {
                        }
                    })
        }
    }

    fun bindDetail() {
        view?.let {
            it.setLoading(false)
            it.setPodcastDetail(podcastDetail!!)
        }
    }
}