package br.com.wakim.eslpodclient.podcastplayer.presenter

import android.content.ServiceConnection
import android.os.Bundle
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.bindService
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.extensions.startService
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.podcastplayer.view.PlayerView
import br.com.wakim.eslpodclient.presenter.Presenter
import br.com.wakim.eslpodclient.rx.PermissionPublishSubject
import br.com.wakim.eslpodclient.service.PlayerCallback
import br.com.wakim.eslpodclient.service.PlayerService
import br.com.wakim.eslpodclient.service.StorageService
import br.com.wakim.eslpodclient.service.TypedBinder
import br.com.wakim.eslpodclient.view.PermissionRequester
import rx.Observable
import rx.SingleSubscriber

class PlayerPresenter(private val app : Application,
                      private val permissionRequester: PermissionRequester,
                      private val podcastInteractor: PodcastInteractor) : Presenter<PlayerView>() {

    companion object {
        private final val PODCAST_ITEM = "PODCAST_ITEM"
        private final val PODCAST_DETAIL = "PODCAST_DETAIL"
        private final val WRITE_STORAGE_PERMISSION = 12
    }

    var podcastItem : PodcastItem? = null
    var podcastDetail : PodcastItemDetail? = null

    var playerService : PlayerService? = null
    var storageService : StorageService? = null

    var playPending : Boolean = false

    val playerCallback = object : PlayerCallback {

        override fun onAudioFocusFailed() {
        }

        override fun onDurationChanged(duration: Int) {
            view?.setMaxProgress(duration)
        }

        override fun onDurationAvailabilityChanged(durationAvailable: Int) {
            view?.setMaxAvailableProgress(durationAvailable)
        }

        override fun onPositionChanged(position: Int) {
            view?.setProgressValue(position)
        }

        override fun onPlayerStarted() {
            view?.showPauseButton()
        }

        override fun onPlayerPaused() {
            view?.showPlayButton()
        }

        override fun onPlayerStopped() {
            view?.let {
                it.setProgressValue(0)
                it.showPlayButton()
            }
        }

        override fun onSkippedToPrevious(podcastItem: PodcastItem) {
            view?.setPodcastItem(podcastItem)
            loadDetail(podcastItem)
        }

        override fun onSkippedToNext(podcastItem: PodcastItem) {
            view?.setPodcastItem(podcastItem)
            loadDetail(podcastItem)
        }
    }

    var playerServiceConnection : ServiceConnection? = null
    var storageServiceConnection : ServiceConnection? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(PODCAST_DETAIL, podcastDetail)
        outState.putParcelable(PODCAST_ITEM, podcastItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        podcastItem = savedInstanceState?.getParcelable<PodcastItem>(PODCAST_ITEM)
        podcastDetail = savedInstanceState?.getParcelable<PodcastItemDetail>(PODCAST_DETAIL)
    }

    override fun onStart() {
        super.onStart()

        addSubscription {
            PermissionPublishSubject.INSTANCE
                    .publishSubject
                    .subscribe { permission ->
                        (permission.requestCode == WRITE_STORAGE_PERMISSION).let {
                            if (permission.isGranted()) {
                                doPlay()
                            } else {
                                view!!.showMessage(R.string.write_external_storage_permission_needed_to_play)
                            }
                        }
                    }
        }
    }

    override fun onStop() {
        super.onStop()
        unbindToService()
    }

    fun unbindToService() {
        playerService?.let {
            it.callback = null
            app.unbindService(playerServiceConnection)
        }

        storageService?.let {
            app.unbindService(storageServiceConnection)
        }
    }

    fun bindServiceIfNeeded() {
        var playerObservable : Observable<Pair<ServiceConnection, TypedBinder<PlayerService>?>> = Observable.empty()
        var storageObservable : Observable<Pair<ServiceConnection, TypedBinder<StorageService>?>> = Observable.empty()

        if (playerService == null) {
            app.startService<PlayerService> { }

            playerObservable = app.bindService<PlayerService>()

            playerObservable = playerObservable
                .doOnNext { pair ->
                    pair?.let {
                        playerServiceConnection = pair.first
                        playerService = pair.second?.getService()

                        playerService?.callback = playerCallback
                    }
                }
        }

        if (storageService == null) {
            storageObservable = app.bindService<StorageService>()

            storageObservable = storageObservable
                .doOnNext { pair ->
                    pair?.let {
                        storageServiceConnection = pair.first
                        storageService = pair.second?.getService()
                    }
                }
        }

        addSubscription {
            Observable.combineLatest(playerObservable, storageObservable, { t1, t2 ->
                setupInitialState()
                doPlay()
            }).subscribe()
        }
    }

    fun setupInitialState() {
        if (playerService?.isPlaying() ?: false) {
            view!!.showPauseButton()
        }
    }

    fun explicitlyStop() {
        playerService?.dispose()
    }

    fun onPauseClicked() {
        playerService?.pause()
    }

    fun onPlayClicked() {
        bindServiceIfNeeded()
        playPending = true

        doPlay()
    }

    fun onStopClicked() {
        playerService?.stop()
    }

    fun onNextClicked() {
        playerService?.skipToNext()
    }

    fun onPreviousClicked() {
        playerService?.skipToPrevious()
    }

    private fun doPlay() {
        if (!playPending) return

        if (playerService == null || storageService == null) {
            return
        }

        val item = podcastItem as PodcastItem

        playerService!!.let {
            it.reset()
            it.play(item, view?.getProgressValue() ?: 0)

            playPending = false
        }
    }

    fun startDownload() {
//        podcastItem?.let {
//            if (it.downloadStatus.isFinished()) {
//                return
//            }
//
//            if (!hasPermission(app, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                permissionRequester.requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_STORAGE_PERMISSION)
//
//                return
//            }
//
//            storageService!!.startDownloadIfNeeded(it)
//        }
    }

    fun seekTo(pos : Int) {
        playerService?.seek(pos)
    }

    fun play(podcastItem: PodcastItem) {
        this.podcastItem = podcastItem

        view!!.setProgressValue(0)

        onPlayClicked()
        loadDetail(podcastItem)
    }

    fun isPlaying(): Boolean {
        return playerService?.isPlaying() ?: false
    }

    fun isPrepared() : Boolean = playerService?.initalized ?: false

    // Details

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

    fun seekToSlowDialog() {
        podcastDetail?.seekPos?.let {
            playerService?.seek(it.slow * 1000)
        }
    }

    fun seekToExplanation() {
        podcastDetail?.seekPos?.let {
            playerService?.seek(it.explanation * 1000)
        }
    }

    fun seekToNormalDialog() {
        podcastDetail?.seekPos?.let {
            playerService?.seek(it.normal * 1000)
        }
    }
}