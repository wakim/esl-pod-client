package br.com.wakim.eslpodclient.podcastplayer.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.StringRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.PopupMenu
import android.text.Html
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.PodcastPlayerComponent
import br.com.wakim.eslpodclient.extensions.*
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.podcastplayer.presenter.PlayerPresenter
import br.com.wakim.eslpodclient.view.BaseActivity
import br.com.wakim.eslpodclient.widget.LoadingFloatingActionButton
import butterknife.bindView
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import pl.charmas.android.tagview.TagView
import javax.inject.Inject

class ListPlayerView : AppBarLayout, PlayerView {

    companion object {
        final const val SUPER_STATE_KEY = "SUPER_STATE"
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    val callback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> showFabs()
                BottomSheetBehavior.STATE_EXPANDED  -> hideFabs()
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
            (context.getSystemService(PodcastPlayerComponent::class.java.simpleName) as PodcastPlayerComponent).inject(this)

            setupBehaviorCallback()

            presenter.onStart()
            presenter.onResume()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        with (presenter) {
            onStop()
            onDestroy()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()

        bundle.putParcelable(SUPER_STATE_KEY, superState)

        presenter.onSaveInstanceState(bundle)

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var realState = state

        if (realState is Bundle) {
            presenter.onRestoreInstanceState(realState)
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
            override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
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

    fun setupBehaviorCallback() {
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

    override fun setProgressValue(position: Int) {
        seekBar.progress = position
        this.timer.text = position.millisToElapsedTime()
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
            } else if (isPrepared()) {
                showPlayButton()
            } else {
                showLoadingButton()
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

    fun showLoadingButton() {
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

    fun stop() {
        presenter.onStopClicked()
    }

    override fun setLoading(loading: Boolean) {
        this.loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun bindPodcastItem(podcastItem: PodcastItem) {
        if (this.podcastItem != podcastItem)
            bindPodcastInfo(podcastItem)
    }

    override fun bindPodcastDetail(podcastItemDetail: PodcastItemDetail) {
        script.text = Html.fromHtml(podcastItemDetail.script)
        setupOverflowMenu(podcastItemDetail)
    }

    fun isExpanded() = bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED

    fun isVisible() =
            bottomSheetBehavior?.state.let {
                it == BottomSheetBehavior.STATE_COLLAPSED || it == BottomSheetBehavior.STATE_COLLAPSED
            }

    fun collapse() {
        if (isExpanded()) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    fun hide() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun isPlaying() = presenter.isPlaying()
}