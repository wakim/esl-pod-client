package br.com.wakim.eslpodclient.podcastplayer.presenter

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.extensions.bindService
import br.com.wakim.eslpodclient.extensions.startService
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.podcastplayer.view.PlayerView
import br.com.wakim.eslpodclient.presenter.Presenter
import br.com.wakim.eslpodclient.service.*

class PlayerPresenter(private val app : Application) : Presenter<PlayerView>() {

    companion object {
        private final val PODCAST_ITEM = "PODCAST_ITEM"
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

    var playerConnection = object : ServiceConnection {

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val service = (p1!! as PlayerLocalBinder).getService()

            service.callback = playerCallback

            playerService = service

            setupInitialState()

            if (storageService != null) {
                doPlay()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            playerService = null
        }
    }

    var storageConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val service = (p1!! as StorageLocalBinder).getService()

            storageService = service

            if (playerService != null) {
                doPlay()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            storageService = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(PODCAST_ITEM, podcastItem)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        podcastItem = savedInstanceState?.getParcelable<PodcastItem>(PODCAST_ITEM)
    }

    fun unbindToService() {
        playerService?.let {
            it.callback = null
            app.unbindService(playerConnection)
        }

        storageService?.let {
            app.unbindService(storageConnection)
        }

        storageService = null
        playerService = null
    }

    fun bindServiceIfNeeded() {
        if (playerService == null) {
            app.startService<PlayerService> { }
            app.bindService<PlayerService>(playerConnection)
        }

        if (storageService == null) {
            app.bindService<StorageService>(storageConnection)
        }
    }

    fun setupInitialState() {
        if (playerService?.isPlaying() ?: false) {
            view!!.showPauseButton()
        } else {
            view!!.showPlayButton()
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
}