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
import android.os.Binder
import android.os.IBinder
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.AppComponent
import java.lang.ref.WeakReference
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
            prepared = true

            play()
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
    lateinit var audioManager : AudioManager

    var session : MediaSessionCompat? = null
    var controller : MediaControllerCompat? = null

    var prepared : Boolean = false
    var initalized : Boolean = false

    var url : String? = null
    var mediaTitle : String? = null
    var initialPosition : Int? = null

    var task : DurationUpdatesTask? = null
    var callback : PlayerCallback? = null
        set(value) {
            field = value
            startTaskIfNeeded()
        }

    override fun onCreate() {
        super.onCreate()
        (applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent?)?.inject(this)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return localBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        session = session ?: initMediaSession()

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
                KeyEvent.KEYCODE_MEDIA_PLAY  -> it.transportControls.play()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> it.transportControls.pause()
                KeyEvent.KEYCODE_MEDIA_STOP  -> it.transportControls.stop()
            }
        }
    }

    fun initMediaSession() : MediaSessionCompat {
        val session = MediaSessionCompat(this, TAG)
        val flags = MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS

        controller = MediaControllerCompat(this, session.sessionToken)

        session.setFlags(flags)
        session.setCallback(mediaSessionCallback)

        return session
    }

    fun play(url : String, mediaTitle: String, position: Int) {
        this.url = url
        this.mediaTitle = mediaTitle
        this.initialPosition = position

        if (prepared) {
            reset()
        }

        play()
    }

    fun reset() {
        if (!prepared) {
            return
        }

        pause()
        mediaPlayer.reset()

        initialPosition = 0
        prepared = false
    }

    private fun play() {
        if (isPlaying()) {
            return
        }

        if (!prepared) {
            prepareMediaPlayer()
            return
        }

        if (!requestAudioFocus()) {
            callback?.onAudioFocusFailed()
            return
        }

        registerNoisyReceiver()

        startAndSeek()
        startTaskIfNeeded()
        setupSessionState(true)

        callback?.let {
            it.onDurationAvailable(mediaPlayer.duration)
            it.onPlayerStarted()
        }

        startForeground(mediaTitle!!, generateAction(R.drawable.ic_pause_white_36dp, R.string.pause, KeyEvent.KEYCODE_MEDIA_PAUSE))
    }

    private fun setupSessionState(playing: Boolean) {
        session?.isActive = playing
    }

    private fun prepareMediaPlayer() {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
    }

    private fun startAndSeek() {
        mediaPlayer.start()
        mediaPlayer.seekTo(initialPosition ?: 0)
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

    fun registerNoisyReceiver() {
        unregisterNoisyReceiver()

        registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
        noisyReceiver.registered = true
    }

    fun unregisterNoisyReceiver() {
        if (!noisyReceiver.registered) {
            return
        }

        unregisterReceiver(noisyReceiver)
        noisyReceiver.registered = false
    }

    fun requestAudioFocus() : Boolean {
        val result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    fun stop() {
        stopSelf()
        stopForeground(true)

        callback?.onPlayerStopped()
    }

    fun pause() {
        isPlaying().let {
            initialPosition = mediaPlayer.currentPosition

            mediaPlayer.pause()

            unregisterNoisyReceiver()

            callback?.onPlayerPaused()
            task?.cancel(true)

            startForeground(mediaTitle!!, generateAction(R.drawable.ic_play_arrow_white_36dp, R.string.play, KeyEvent.KEYCODE_MEDIA_PLAY))
        }
    }

    fun seek(pos : Int) {
        initialPosition = pos

        mediaPlayer.isPlaying.let {
            mediaPlayer.seekTo(pos)
            callback?.onPositionChanged(pos)
        }
    }

    fun whenFromPosition() : Long {
        var now = System.currentTimeMillis()

        if (isPlaying())
            now -= mediaPlayer.currentPosition

        return now
    }

    fun isPlaying() : Boolean = initalized && mediaPlayer.isPlaying

    override fun onDestroy() {
        super.onDestroy()

        task?.cancel(true)

        isPlaying().let {
            initialPosition = 0

            mediaPlayer.stop()
            mediaPlayer.release()

            initalized = false
        }

        audioManager.abandonAudioFocus(audioFocusChangeListener)
        unregisterNoisyReceiver()

        session?.let {
            it.isActive = false
            it.release()
        }
    }

    private fun startForeground(mediaTitle: String, action: NotificationCompat.Action) {
        val builder = android.support.v7.app.NotificationCompat.Builder(this)
        val style = android.support.v7.app.NotificationCompat.MediaStyle()

        val stopIntent = getActionIntent(KeyEvent.KEYCODE_MEDIA_STOP)

        style   .setMediaSession(session!!.sessionToken)
                .setShowActionsInCompactView(0, 1)
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
                .addAction(action)
                .addAction(generateAction(R.drawable.ic_stop_white_24dp, R.string.stop, KeyEvent.KEYCODE_MEDIA_STOP))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

        startForeground(ID, builder.build())
    }

    fun generateAction(@DrawableRes icon : Int, @StringRes titleResId : Int, intentAction : Int) : NotificationCompat.Action =
        NotificationCompat.Action.Builder(icon, getString(titleResId), getActionIntent(intentAction)).build()

    fun getActionIntent(intentAction : Int) : PendingIntent {
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)

        intent.`package` = packageName
        intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, intentAction))

        return PendingIntent.getService(this, intentAction, intent, 0)
    }
}

class PlayerLocalBinder : Binder {

    lateinit var reference : WeakReference<PlayerService>

    constructor(playerService: PlayerService) : super() {
        reference = WeakReference(playerService)
    }

    fun getService() : PlayerService = reference.get()
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

interface PlayerCallback {
    fun onDurationAvailable(duration : Int)
    fun onPositionChanged(position : Int)
    fun onAudioFocusFailed()

    fun onPlayerStarted()
    fun onPlayerPaused()
    fun onPlayerStopped()
}

abstract class SmartReceiver : BroadcastReceiver() {
    var registered = false
}