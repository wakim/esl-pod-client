package br.com.wakim.eslpodclient.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.IBinder
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.extensions.ofIOToMainThread
import br.com.wakim.eslpodclient.interactor.PodcastInteractor
import br.com.wakim.eslpodclient.interactor.StorageInteractor
import br.com.wakim.eslpodclient.model.DownloadStatus
import br.com.wakim.eslpodclient.model.PodcastItem
import rx.Subscription
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PlayerService : Service() {

    companion object {
        final const val ID = 42
        final const val TAG = "PlayerService Session"
    }

    val localBinder = PlayerLocalBinder(this)

    val mediaPlayer : MediaPlayer by lazy {
        val mp = MediaPlayer()

        mp.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mp.setOnPreparedListener {
            initalized = true
            preparing = false

            play()
        }

        mp.setOnBufferingUpdateListener { mediaPlayer, buffer ->
            if (isPlaying()) {
                callback?.onDurationAvailabilityChanged(((buffer.toFloat() * getDuration()) / 100F).toInt())
            }
        }

        mp.setOnCompletionListener {
            mp.stop()
            mp.reset()
        }

        mp
    }

    val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()
            play()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            skipToNext()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
            skipToPrevious()
        }

        override fun onPause() {
            super.onPause()
            pause()
        }

        override fun onStop() {
            super.onStop()
            stop()
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            seek(pos.toInt())
        }
    }

    val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> play()
            else -> pause()
        }
    }

    val noisyReceiver = object : SmartReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            pause()
        }
    }

    @Inject
    lateinit var audioManager: AudioManager

    @Inject
    lateinit var storageInteractor: StorageInteractor

    @Inject
    lateinit var podcastInteractor: PodcastInteractor

    @Inject
    lateinit var notificationManagerCompat: NotificationManagerCompat

    private var session: MediaSessionCompat? = null
    private var controller: MediaControllerCompat? = null

    private var podcastItem: PodcastItem? = null
    private var initialPosition: Int = 0

    private var task: DurationUpdatesTask? = null

    private var downloadStatusSubscription: Subscription? = null

    val playlistManager: PlaylistManager by lazy {
        PlaylistManager()
    }

    var callback: PlayerCallback? = null
        set(value) {
            field = value
            startTaskIfNeeded()
        }

    var initalized: Boolean = false
        private set

    private var preparing: Boolean = false

    override fun onCreate() {
        super.onCreate()
        (applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent?)?.inject(this)

        session = session ?: initMediaSession()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return localBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            MediaButtonReceiver.handleIntent(session, intent)
            handleIntent(it)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun handleIntent(intent : Intent) {
        val action = intent.action

        if (Intent.ACTION_MEDIA_BUTTON != action) {
            return
        }

        val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

        controller?.let {
            when (event?.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY     -> it.transportControls.play()
                KeyEvent.KEYCODE_MEDIA_PAUSE    -> it.transportControls.pause()
                KeyEvent.KEYCODE_MEDIA_STOP     -> it.transportControls.stop()
                KeyEvent.KEYCODE_MEDIA_NEXT     -> it.transportControls.skipToNext()
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> it.transportControls.skipToPrevious()
            }
        }
    }

    private fun initMediaSession() : MediaSessionCompat {
        val session = MediaSessionCompat(this, TAG)
        val flags = MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS

        controller = MediaControllerCompat(this, session.sessionToken)

        session.setFlags(flags)
        session.setCallback(mediaSessionCallback)

        return session
    }

    fun play(podcastItem: PodcastItem, position: Int) {
        if (!((Application.INSTANCE?.connected) ?: false)) {
            callback?.onConnectivityError()
            return
        }

        if (initalized || preparing) {
            reset()
        }

        this.podcastItem = podcastItem
        this.initialPosition = position

        play()
    }

    fun reset() {
        if (!initalized && !preparing) {
            return
        }

        if (isPlaying()) {
            pause()
        }

        mediaPlayer.reset()

        initialPosition = 0

        preparing = false
        initalized = false
    }

    private fun play() {
        if (isPlaying()) {
            return
        }

        if (!initalized) {
            prepareMediaPlayer()
            preparing = true

            return
        }

        if (!requestAudioFocus()) {
            callback?.onAudioFocusFailed()
            return
        }

        session?.isActive = true

        registerNoisyReceiver()

        startAndSeek()
        startTaskIfNeeded()
        setupSessionState(true)

        callback?.let {
            it.onDurationChanged(mediaPlayer.duration)
            it.onPlayerStarted()
        }

        startForeground(podcastItem!!.title, generateAction(R.drawable.ic_pause_white_36dp, R.string.pause, KeyEvent.KEYCODE_MEDIA_PAUSE))
    }

    private fun setupSessionState(playing: Boolean) {
        session?.isActive = playing
    }

    private fun prepareMediaPlayer() {
        podcastItem?.let {
            downloadStatusSubscription?.unsubscribe()

            downloadStatusSubscription =
                    storageInteractor.getDownloadStatus(it)
                            .zipWith(podcastInteractor.getLastSeekPos(it.remoteId)) {
                                downloadStatus, lastSeekPos -> downloadStatus to lastSeekPos
                            }
                            .ofIOToMainThread()
                            .subscribe { pair ->
                                prepare(pair.first, it)
                                initialPosition = pair.second ?: initialPosition
                            }
        }
    }

    private fun prepare(downloadStatus: DownloadStatus, podcastItem: PodcastItem) {
        val url: String

        if (downloadStatus.status == DownloadStatus.DOWNLOADED) {
            url = downloadStatus.localPath!!
        } else {
            url = podcastItem.mp3Url
        }

        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
    }

    private fun startAndSeek() {
        mediaPlayer.start()
        mediaPlayer.seekTo(initialPosition)
    }

    private fun startTaskIfNeeded() {
        task?.cancel(true)

        if (!isPlaying()) {
            return
        }

        callback?.let{
            task = DurationUpdatesTask(this).execute() as DurationUpdatesTask
        }
    }

    private fun registerNoisyReceiver() {
        if (noisyReceiver.registered) {
            return
        }

        registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        noisyReceiver.registered = true
    }

    private fun unregisterNoisyReceiver() {
        if (!noisyReceiver.registered) {
            return
        }

        unregisterReceiver(noisyReceiver)
        noisyReceiver.registered = false
    }

    private fun requestAudioFocus() : Boolean {
        val result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    fun skipToNext() {
        val nextItem = playlistManager.nextOrNull(podcastItem!!)

        nextItem?.let {
            play(it, 0)
            callback?.onSkippedToNext(it)
        }
    }

    fun skipToPrevious() {
        val previousItem = playlistManager.previousOrNull(podcastItem!!)

        previousItem?.let {
            play(it, 0)
            callback?.onSkippedToPrevious(it)
        }
    }

    fun dispose() {
        stopSelf()
    }

    fun stop() {
        innerStop()

        if (podcastItem != null) {
            podcastInteractor.insertLastSeekPos(podcastItem!!.remoteId, 0)
        }
    }

    private fun innerStop() {
        downloadStatusSubscription?.unsubscribe()
        initialPosition = 0

        mediaPlayer.stop()
        mediaPlayer.reset()

        session?.isActive = false

        unregisterNoisyReceiver()

        callback?.onPlayerStopped()
        task?.cancel(true)
    }

    fun pause() {
        if (isPlaying()) {
            initialPosition = mediaPlayer.currentPosition

            podcastInteractor.insertLastSeekPos(podcastItem!!.remoteId, initialPosition)

            mediaPlayer.pause()
            mediaPlayer.reset()

            session?.isActive = false

            unregisterNoisyReceiver()

            callback?.onPlayerPaused()
            task?.cancel(true)

            startForeground(podcastItem!!.title, generateAction(R.drawable.ic_play_arrow_white_36dp, R.string.play, KeyEvent.KEYCODE_MEDIA_PLAY))
        }
    }

    fun seek(pos : Int) {
        initialPosition = pos

        podcastInteractor.insertLastSeekPos(podcastItem!!.remoteId, pos)

        if (isPlaying()) {
            mediaPlayer.seekTo(pos)
        }

        callback?.onPositionChanged(pos)
    }

    private fun whenFromPosition() : Long {
        var now = System.currentTimeMillis()

        if (isPlaying())
            now -= mediaPlayer.currentPosition

        return now
    }

    fun isPlaying() = initalized && mediaPlayer.isPlaying

    private fun getDuration() : Float {
        if (isPlaying()) {
            return mediaPlayer.duration.toFloat()
        }

        return 0F
    }

    override fun onDestroy() {
        super.onDestroy()

        release()
        notificationManagerCompat.cancel(ID)
    }

    private fun release() {
        if (isPlaying()) {
            innerStop()
        }

        mediaPlayer.reset()
        mediaPlayer.release()

        initalized = false
        preparing = false

        audioManager.abandonAudioFocus(audioFocusChangeListener)

        session?.release()
    }

    private fun startForeground(mediaTitle: String, action: NotificationCompat.Action) {
        val builder = android.support.v7.app.NotificationCompat.Builder(this)
        val style = android.support.v7.app.NotificationCompat.MediaStyle()

        val stopIntent = getActionIntent(KeyEvent.KEYCODE_MEDIA_STOP)

        style   .setMediaSession(session!!.sessionToken)
                .setShowActionsInCompactView(0, 1, 2, 3)
                .setCancelButtonIntent(stopIntent)
                .setShowCancelButton(true)

        builder.mStyle = style

        builder
                .setUsesChronometer(isPlaying())
                .setWhen(whenFromPosition())
                .setContentIntent(controller!!.sessionActivity)
                .setDeleteIntent(stopIntent)
                .setContentTitle(mediaTitle)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon((ContextCompat.getDrawable(this, R.mipmap.ic_launcher) as BitmapDrawable).bitmap)
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(generateAction(R.drawable.ic_skip_previous_white_36dp, R.string.previous, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
                .addAction(action)
                .addAction(generateAction(R.drawable.ic_stop_white_36dp, R.string.stop, KeyEvent.KEYCODE_MEDIA_STOP))
                .addAction(generateAction(R.drawable.ic_skip_next_white_36dp, R.string.next, KeyEvent.KEYCODE_MEDIA_NEXT))

        notificationManagerCompat.notify(ID, builder.build())
    }

    private fun generateAction(@DrawableRes icon : Int, @StringRes titleResId : Int, intentAction : Int) : NotificationCompat.Action =
        NotificationCompat.Action.Builder(icon, getString(titleResId), getActionIntent(intentAction)).build()

    private fun getActionIntent(intentAction : Int) : PendingIntent {
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)

        intent.`package` = packageName
        intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, intentAction))

        return PendingIntent.getService(this, intentAction, intent, 0)
    }
}

class PlayerLocalBinder : TypedBinder<PlayerService> {

    lateinit var reference : WeakReference<PlayerService>

    constructor(playerService: PlayerService) : super() {
        reference = WeakReference(playerService)
    }

    override fun getService(): PlayerService? = reference.get()
}

class DurationUpdatesTask(var service: PlayerService) : AsyncTask<Void , Int, Void>() {

    val interval = TimeUnit.SECONDS.toMillis(1)

    override fun doInBackground(vararg p0: Void): Void? {
        while (!isCancelled) {
            publishProgress(service.mediaPlayer.currentPosition)

            try {
                Thread.sleep(interval)
            } catch (ignored: InterruptedException) {
                break;
            }
        }

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        values[0]?.let {
            service.callback?.onPositionChanged(it)
        }
    }
}

class PlaylistManager {
    private var items : List<PodcastItem> = listOf()

    fun setItems(items: ArrayList<PodcastItem>) {
        this.items = items
    }

    fun nextOrNull(podcastItem: PodcastItem) : PodcastItem? {
        val indexOf = items.indexOf(podcastItem)

        return if (indexOf == -1) null else items.getOrNull(indexOf + 1)
    }

    fun previousOrNull(podcastItem: PodcastItem) : PodcastItem? {
        val indexOf = items.indexOf(podcastItem)

        return if (indexOf == -1) null else items.getOrNull(indexOf - 1)
    }

    fun clearItems() {
        items = listOf()
    }
}

interface PlayerCallback {
    fun onDurationChanged(duration : Int)
    fun onDurationAvailabilityChanged(durationAvailable: Int)
    fun onPositionChanged(position : Int)
    fun onAudioFocusFailed()

    fun onPlayerStarted()
    fun onPlayerPaused()
    fun onPlayerStopped()

    fun onSkippedToPrevious(podcastItem: PodcastItem)
    fun onSkippedToNext(podcastItem: PodcastItem)

    fun onConnectivityError()
}

abstract class SmartReceiver : BroadcastReceiver() {
    var registered = false
}