package br.com.wakim.eslpodclient.podcastplayer.view

import android.content.Context
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.StringRes
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.PopupMenu
import android.text.Html
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.PodcastPlayerComponent
import br.com.wakim.eslpodclient.extensions.*
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.podcastplayer.presenter.PlayerPresenter
import br.com.wakim.eslpodclient.service.PlayerService
import br.com.wakim.eslpodclient.service.StorageService
import br.com.wakim.eslpodclient.service.TypedBinder
import br.com.wakim.eslpodclient.view.BaseActivity
import br.com.wakim.eslpodclient.widget.LoadingFloatingActionButton
import butterknife.bindView
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import pl.charmas.android.tagview.TagView
import rx.Observable
import rx.Subscription
import javax.inject.Inject

open class ListPlayerView : LinearLayout, PlayerView {

    companion object {
        const val SUPER_STATE_KEY = "SUPER_STATE"
        const val SEEK_MAX_STATE_KEY = "SEEK_MAX_STATE"
        const val SEEK_PROGRESS_STATE_KEY = "SEEK_PROGRESS_STATE"
        const val PODCAST_ITEM_KEY = "PODCAST_ITEM"
        const val PODCAST_ITEM_DETAIL_KEY = "PODCAST_ITEM_DETAIL"
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    val callback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) { }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> showFabs()
                BottomSheetBehavior.STATE_EXPANDED -> hideFabs()
                BottomSheetBehavior.STATE_HIDDEN    -> hideFabs()
            }
        }
    }

    private val clickListener = { view: View ->
        when (view.id) {
            R.id.ib_play     -> presenter.onPlayClicked()
            R.id.ib_pause    -> presenter.onPauseClicked()
            R.id.ib_stop     -> presenter.onStopClicked()
            R.id.ib_next     -> presenter.onNextClicked()
            R.id.ib_previous -> presenter.onPreviousClicked()
            R.id.ib_overflow -> showOverflow()
        }
    }

    private val menuClickListener = { menu: MenuItem ->
        when (menu.itemId) {
            R.id.download      -> presenter.startDownload()
            R.id.slow_dialog   -> presenter.seekToSlowDialog()
            R.id.explanation   -> presenter.seekToExplanation()
            R.id.normal_dialog -> presenter.seekToNormalDialog()
        }

        true
    }

    private val titleContainer : ViewGroup by bindView(R.id.ll_title)

    private val title : TextView by bindView(R.id.tv_title)
    private val subtitle: TextView by bindView(R.id.tv_subtitle)
    private val tagView: TagView by bindView(R.id.tv_tags)

    private val seekBar : SeekBar by bindView(R.id.seek_bar)

    private val loading : ProgressBar by bindView(R.id.pb_loading)
    private val script : TextView by bindView(R.id.tv_script)

    private val timer : TextView by bindView(R.id.tv_timer)
    private val duration : TextView by bindView(R.id.tv_duration)

    private val playButton: ImageButton by bindView(R.id.ib_play)
    private val pauseButton: ImageButton by bindView(R.id.ib_pause)
    private val stopButton: ImageButton by bindView(R.id.ib_stop)
    private val nextButton: ImageButton by bindView(R.id.ib_next)
    private val previousButton: ImageButton by bindView(R.id.ib_previous)
    private val overflowButton: ImageButton by bindView(R.id.ib_overflow)
    private val streamTypeText: TextView by bindView(R.id.tv_stream_type)

    private val popupMenu: PopupMenu by lazy {
        val menu = PopupMenu(context, overflowButton)

        menu.inflate(R.menu.player_overflow_menu)
        menu.setOnMenuItemClickListener(menuClickListener)

        menu
    }

    var playFab: FloatingActionButton? = null

    var pauseFab: FloatingActionButton? = null

    var loadingFab: LoadingFloatingActionButton? = null

    var podcastItem: PodcastItem? = null

    var progressLocked = false

    var playerServiceConnection: ServiceConnection? = null
    var storageServiceConnection: ServiceConnection? = null

    var serviceSubscription: Subscription? = null

    @Inject
    lateinit var baseActivity: BaseActivity

    private var bottomSheetBehavior : BottomSheetBehavior<*>? = null

    lateinit var presenter: PlayerPresenter

    @Inject
    fun injectPlayerPresenter(presenter : PlayerPresenter) {
        presenter.view = this
        this.presenter = presenter
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            setupBehaviorCallback()

            (context.getSystemService(PodcastPlayerComponent::class.java.simpleName) as PodcastPlayerComponent).inject(this)

            bindServices()

            presenter.onStart()
            presenter.onResume()
        }
    }

    fun bindServices() {
        var playerObservable : Observable<Pair<ServiceConnection, TypedBinder<PlayerService>?>> = Observable.empty()
        var storageObservable : Observable<Pair<ServiceConnection, TypedBinder<StorageService>?>> = Observable.empty()

        if (playerServiceConnection == null) {
            playerObservable = context.bindService<PlayerService>()
        }

        if (storageServiceConnection == null) {
            storageObservable = context.bindService<StorageService>(false)
        }

        serviceSubscription = Observable.combineLatest(playerObservable, storageObservable, { pairPlayer, pairStorage ->
            playerServiceConnection = pairPlayer.first
            storageServiceConnection = pairStorage.first

            presenter.setServices(pairPlayer.second!!.service!!, pairStorage.second!!.service!!)
        }).subscribe()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        unbindServices()

        with (presenter) {
            onStop()
            onDestroy()
        }
    }

    fun unbindServices() {
        playerServiceConnection?.let {
            context.unbindService(playerServiceConnection)
        }

        storageServiceConnection?.let {
            context.unbindService(storageServiceConnection)
        }

        playerServiceConnection = null
        storageServiceConnection = null

        serviceSubscription?.unsubscribe()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()

        bundle.putParcelable(SUPER_STATE_KEY, superState)

        with (seekBar) {
            bundle.putInt(SEEK_MAX_STATE_KEY, max)
            bundle.putInt(SEEK_PROGRESS_STATE_KEY, progress)
        }

        bundle.putParcelable(PODCAST_ITEM_KEY, presenter.podcastItem)
        bundle.putParcelable(PODCAST_ITEM_DETAIL_KEY, presenter.podcastDetail)

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var realState = state

        (context.getSystemService(PodcastPlayerComponent::class.java.simpleName) as PodcastPlayerComponent).inject(this)

        if (realState is Bundle) {
            presenter.onRestoreInstanceState(realState.getParcelable(PODCAST_ITEM_KEY), realState.getParcelable(PODCAST_ITEM_DETAIL_KEY))

            setProgressValue(realState.getInt(SEEK_PROGRESS_STATE_KEY))
            setMaxProgress(realState.getInt(SEEK_MAX_STATE_KEY))

            realState = realState.getParcelable(SUPER_STATE_KEY)
        }

        super.onRestoreInstanceState(realState)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        titleContainer.setOnClickListener {
            bottomSheetBehavior?.toggleState(BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.STATE_COLLAPSED)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progressLocked && fromUser && seekbar.secondaryProgress < progress){
                    seekBar.progress = seekbar.secondaryProgress
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
            }

            override fun onStopTrackingTouch(p0: SeekBar) {
                presenter.seekTo(p0.progress)
            }
        })

        playButton.setOnClickListener(clickListener)
        stopButton.setOnClickListener(clickListener)
        pauseButton.setOnClickListener(clickListener)

        previousButton.setOnClickListener(clickListener)
        nextButton.setOnClickListener(clickListener)

        overflowButton.setOnClickListener(clickListener)
    }

    fun setControls(playFab: FloatingActionButton, pauseFab: FloatingActionButton, loadingFab: LoadingFloatingActionButton) {
        this.playFab = playFab
        this.pauseFab = pauseFab
        this.loadingFab = loadingFab

        playFab.setOnClickListener {
            showLoadingButton()
            presenter.onPlayClicked()
        }

        pauseFab.setOnClickListener {
            presenter.onPauseClicked()
        }
    }

    open fun setupBehaviorCallback() {
        bottomSheetBehavior = BottomSheetBehavior.from(this)
        bottomSheetBehavior?.setBottomSheetCallback(callback)
    }

    fun showOverflow() {
        popupMenu.show()
    }

    fun play(podcastItem: PodcastItem) {
        if (this.podcastItem == podcastItem) {
            return
        }

        bindPodcastInfo(podcastItem)
        presenter.play(podcastItem)

        hideFabs()
        setVisible()

        nextButton.isEnabled = false
        previousButton.isEnabled = false

        loadingFab?.let {
            it.showAnimated()
            it.startAnimation()
        }
    }

    override fun setVisible() {
        visibility = View.VISIBLE

        if (bottomSheetBehavior!!.state == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    fun bindPodcastInfo(podcastItem: PodcastItem) {
        val formattedDate = podcastItem.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        val titleText = podcastItem.userFriendlyTitle

        this.podcastItem = podcastItem

        if (podcastItem.isEnglishCafe()) {
            title.text = podcastItem.title
            subtitle.text = formattedDate
        } else {
            title.text = titleText
            subtitle.text = context.getString(R.string.player_subtitle, podcastItem.podcastName, formattedDate)
        }

        tagView.setTags(podcastItem.tagList)

        script.text = null
    }

    fun setupOverflowMenu(podcastItemDetail: PodcastItemDetail) {
        val slowIndexMenu = popupMenu.menu.findItem(R.id.slow_dialog)
        val explanationIndexMenu = popupMenu.menu.findItem(R.id.explanation)
        val normalIndexMenu = popupMenu.menu.findItem(R.id.normal_dialog)
        val context = this.context

        if (podcastItemDetail.isEnglishCafe()) {
            slowIndexMenu.isVisible = false
            explanationIndexMenu.isVisible = false
            normalIndexMenu.isVisible = false

            return
        }

        val seekPost = podcastItemDetail.seekPos!!

        slowIndexMenu.isVisible = true
        explanationIndexMenu.isVisible = true
        normalIndexMenu.isVisible = true

        slowIndexMenu.title = context.getString(R.string.slow_dialog_index, seekPost.slow.secondsToElapsedTime())
        explanationIndexMenu.title = context.getString(R.string.explanation_index, seekPost.explanation.secondsToElapsedTime())
        normalIndexMenu.title = context.getString(R.string.normal_dialog_index, seekPost.normal.secondsToElapsedTime())
    }

    override fun setSecondaryProgressValue(position: Int) {
        seekBar.secondaryProgress = position
    }

    override fun setSeekEnabled(enabled: Boolean) {
        progressLocked = !enabled
    }

    override fun setProgressValue(position: Int) {
        seekBar.progress = position
        this.timer.text = position.millisToElapsedTime()
    }

    override fun setStreamType(@PodcastItem.StreamType streamType: Long) {
        when (streamType) {
            PodcastItem.LOCAL  -> streamTypeText.text = context.getString(R.string.local)
            PodcastItem.REMOTE -> streamTypeText.text = context.getString(R.string.remote)
            PodcastItem.CACHING -> streamTypeText.text = context.getString(R.string.saving)
        }
    }

    override fun getProgressValue(): Int = seekBar.progress

    override fun setMaxProgress(duration: Int) {
        seekBar.max = duration
        this.duration.text = duration.millisToElapsedTime()
    }

    override fun showMessage(@StringRes messageResId: Int): Snackbar =
            baseActivity.showMessage(messageResId)

    override fun showMessage(messageResId: Int, action: String, clickListener: (() -> Unit)?) {
        baseActivity.showMessage(messageResId, action, clickListener)
    }

    fun hideFabs() {
        playFab?.hideAnimated()
        pauseFab?.hideAnimated()
        loadingFab?.hideAnimated()
    }

    fun showFabs() {
        with (presenter) {
            if (isPlaying()) {
                showPauseButton()
            } else {
                showPlayButton()
            }
        }
    }

    override fun showPauseButton() {
        if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            playFab?.hideAnimated()
            pauseFab?.showAnimated()

            loadingFab?.let{
                it.stopAnimation()
                it.hideAnimated()
            }
        }

        enableAll()

        playButton.makeHidden()
        pauseButton.makeVisible()
    }

    override fun showPlayButton() {
        if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            playFab?.showAnimated()

            pauseFab?.hideAnimated()

            loadingFab?.let {
                it.stopAnimation()
                it.hideAnimated()
            }
        }

        enableAll()

        playButton.makeVisible()
        pauseButton.makeHidden()
    }

    fun enableAll() {
        playButton.isEnabled = true
        stopButton.isEnabled = true
        pauseButton.isEnabled = true
        nextButton.isEnabled = true

        previousButton.isEnabled = true
    }

    override fun showLoadingButton() {
        if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            playFab?.hideAnimated()
            pauseFab?.hideAnimated()

            loadingFab?.let {
                it.showAnimated()
                it.startAnimation()
            }
        }

        playButton.isEnabled = false
        stopButton.isEnabled = false
        pauseButton.isEnabled = false
    }

    fun explicitlyStop() {
        presenter.explicitlyStop()
    }

    override fun setLoading(loading: Boolean) {
        this.loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun bindPodcastItem(podcastItem: PodcastItem) {
        if (this.podcastItem != podcastItem)
            bindPodcastInfo(podcastItem)
    }

    @Suppress("DEPRECATION")
    override fun bindPodcastDetail(podcastItemDetail: PodcastItemDetail) {
        if (Build.VERSION.SDK_INT < 24) {
            script.text = Html.fromHtml(podcastItemDetail.script)
        } else {
            script.text = Html.fromHtml(podcastItemDetail.script, Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        }

        setupOverflowMenu(podcastItemDetail)
    }

    fun isExpanded() = bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED

    open fun isVisible() =
            visibility == View.VISIBLE &&
            bottomSheetBehavior?.state.let {
                it == BottomSheetBehavior.STATE_COLLAPSED || it == BottomSheetBehavior.STATE_EXPANDED
            }

    fun collapse() {
        if (isExpanded()) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    open fun hide() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun isPlaying() = presenter.isPlaying()
}