package br.com.wakim.eslpodclient.podcastplayer.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.StringRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.FloatingActionButton
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.ActivityComponent
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

    init {
        (context.getSystemService(ActivityComponent::class.java.simpleName) as ActivityComponent?)?.inject(this)
    }

    val callback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> hideFabs()
                BottomSheetBehavior.STATE_COLLAPSED ->
                    showFabs()

            }
        }
    }

    val clickListener = { view: View ->
        when (view.id) {
            R.id.ib_play     -> presenter.onPlayClicked()
            R.id.ib_pause    -> presenter.onPauseClicked()
            R.id.ib_stop     -> presenter.onStopClicked()
            R.id.ib_next     -> presenter.onNextClicked()
            R.id.ib_previous -> presenter.onPreviousClicked()
        }
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

    private var playFab : FloatingActionButton? = null
    private var pauseFab : FloatingActionButton? = null

    private var loadingFab : LoadingFloatingActionButton? = null

    private var bottomSheetBehavior : BottomSheetBehavior<*>? = null

    lateinit var presenter: PlayerPresenter

    @set:Inject
    var baseActivity: BaseActivity<*>? = null

    @Inject
    fun injectPlayerPresenter(presenter : PlayerPresenter) {
        presenter.view = this
        this.presenter = presenter
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setupBehaviorCallback()

        presenter.onStart()
        presenter.onResume()
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
    }

    fun setupBehaviorCallback() {
        bottomSheetBehavior = BottomSheetBehavior.from(this)
        bottomSheetBehavior?.setBottomSheetCallback(callback)
    }

    fun play(podcastItem: PodcastItem) {
        bindPodcastInfo(podcastItem)
        presenter.play(podcastItem)

        hideFabs()

        nextButton.isEnabled = false
        previousButton.isEnabled = false

        this.loadingFab?.let {
            it.showAnimated()
            it.startAnimation()
        }
    }

    fun bindPodcastInfo(podcastItem: PodcastItem) {
        val formattedDate = podcastItem.date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        val titleText = podcastItem.userFriendlyTitle

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

    override fun setProgressValue(position: Int) {
        seekBar.progress = position
        this.timer.text = position.millisToElapsedTime()
    }

    override fun getProgressValue(): Int = seekBar.progress

    override fun setMaxProgress(duration: Int) {
        seekBar.max = duration
        this.duration.text = duration.millisToElapsedTime()
    }

    override fun setMaxAvailableProgress(available: Int) {
        seekBar.secondaryProgress = available
    }

    override fun showMessage(@StringRes messageResId: Int) {
        baseActivity?.showMessage(messageResId) ?: context.snack(this, messageResId)
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

    fun setControls(playFab: FloatingActionButton, pauseFab: FloatingActionButton, loadingFab: LoadingFloatingActionButton) {
        this.playFab = playFab
        this.pauseFab = pauseFab
        this.loadingFab = loadingFab

        playFab.setOnClickListener {
            presenter.onPlayClicked()
        }

        pauseFab.setOnClickListener {
            presenter.onPauseClicked()
        }
    }

    fun explicitlyStop() {
        presenter.explicitlyStop()
    }

    override fun setLoading(loading: Boolean) {
        this.loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun setPodcastItem(podcastItem: PodcastItem) {
        bindPodcastInfo(podcastItem)
    }

    override fun setPodcastDetail(podcastItemDetail: PodcastItemDetail) {
        script.text = Html.fromHtml(podcastItemDetail.script)
    }
}