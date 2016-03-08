package br.com.wakim.eslpodclient.podcastplayer.presenter

import android.content.ServiceConnection
import android.os.Bundle
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.bindService
import br.com.wakim.eslpodclient.extensions.startService
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.podcastplayer.view.PlayerView
import br.com.wakim.eslpodclient.presenter.Presenter
import br.com.wakim.eslpodclient.rx.PermissionPublishSubject
import br.com.wakim.eslpodclient.service.PlayerCallback
import br.com.wakim.eslpodclient.service.PlayerService
import br.com.wakim.eslpodclient.service.StorageService
import br.com.wakim.eslpodclient.service.TypedBinder
import br.com.wakim.eslpodclient.view.PermissionRequester
import rx.Observable

class PlayerPresenter(private val app : Application, private val permissionRequester: PermissionRequester) : Presenter<PlayerView>() {

    companion object {
        private final val PODCAST_ITEM = "PODCAST_ITEM"
        private final val WRITE_STORAGE_PERMISSION = 12
    }

    var playPending : Boolean = false
    var podcastItem : PodcastItem? = null
    var playerService : PlayerService? = null
    var storageService : StorageService? = null

    val playerCallback = object : PlayerCallback {

        override fun onAudioFocusFailed() {
            // TODO
        }

        override fun onDurationAvailable(duration: Int) {
            view?.let {
                it.setMaxProgress(duration)
            }
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
                it.showPlayButton()
                it.setProgressValue(0)
            }

            unbindToService()
        }
    }

    var playerServiceConnection : ServiceConnection? = null
    var storageServiceConnection : ServiceConnection? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(PODCAST_ITEM, podcastItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        podcastItem = savedInstanceState?.getParcelable<PodcastItem>(PODCAST_ITEM)
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
        playerService?.stop()
    }

    fun onPauseClicked() {
        playerService?.pause()
    }

    fun onPlayClicked() {
        bindServiceIfNeeded()
        playPending = true

        doPlay()
    }

    private fun doPlay() {
        if (!playPending) return

        if (playerService == null || storageService == null) {
            return
        }

        if (!hasPermission(app, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionRequester.requestPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_STORAGE_PERMISSION)

            return
        }

        val item = podcastItem as PodcastItem
        val remotePath = storageService!!.startDownloadIfNeeded(item)

        playerService!!.let {
            it.reset()
            it.play(remotePath, item.title, view?.getProgressValue() ?: 0)

            playPending = false
        }
    }

    fun seekTo(pos : Int) {
        playerService?.seek(pos)
    }

    fun play(podcastItem: PodcastItem) {
        this.podcastItem = podcastItem

        view!!.setProgressValue(0)

        onPlayClicked()
    }

    fun isPlaying(): Boolean {
        return playerService?.isPlaying() ?: false
    }

    fun isPrepared() : Boolean = playerService?.initalized ?: false
}