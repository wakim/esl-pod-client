package br.com.wakim.eslpodclient.podcastlist.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.StringRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.dagger.ActivityComponent
import br.com.wakim.eslpodclient.extensions.*
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.model.PodcastItemDetail
import br.com.wakim.eslpodclient.podcastdetail.presenter.PodcastDetailPresenter
import br.com.wakim.eslpodclient.podcastdetail.view.PodcastDetailView
import br.com.wakim.eslpodclient.podcastplayer.presenter.PlayerPresenter
import br.com.wakim.eslpodclient.podcastplayer.view.PlayerView
import br.com.wakim.eslpodclient.view.BaseActivity
import br.com.wakim.eslpodclient.view.LoadingFloatingActionButton
import butterknife.bindView
import javax.inject.Inject

class ListPlayerView : AppBarLayout, PlayerView, PodcastDetailView {

    companion object {
        final const val PARENT_STATE_KEY = "STATE"
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    val callback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> hideFabs()
                BottomSheetBehavior.STATE_COLLAPSED -> showFabs()
            }
        }
    }

    private val seekBar : SeekBar by bindView(R.id.seek_bar)
    private val title : TextView by bindView(R.id.tv_title)

    private val loading : ProgressBar by bindView(R.id.pb_loading)
    private val script : TextView by bindView(R.id.tv_script)

    private val timer : TextView by bindView(R.id.tv_timer)
    private val duration : TextView by bindView(R.id.tv_duration)

    private var playFab : FloatingActionButton? = null
    private var pauseFab : FloatingActionButton? = null

    private var loadingFab : LoadingFloatingActionButton? = null

    private var bottomSheetBehavior : BottomSheetBehavior<*>? = null

    lateinit var playerPresenter : PlayerPresenter
    lateinit var detailPresenter : PodcastDetailPresenter

    var podcastItem : PodcastItem? = null

    @Inject
    fun injectPlayerPresenter(presenter : PlayerPresenter) {
        presenter.view = this
        this.playerPresenter = presenter
    }

    @Inject
    fun injectDetailPresenter(presenter: PodcastDetailPresenter) {
        presenter.view = this
        this.detailPresenter = presenter
    }

    @get:Inject
    var baseActivity: BaseActivity<*>? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupBehaviorCallback()

        playerPresenter.onStart()
        detailPresenter.onStart()

        title.setOnClickListener {
            bottomSheetBehavior?.toggleState(BottomSheetBehavior.STATE_EXPANDED, BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        playerPresenter.onStop()
        detailPresenter.onStop()

        playerPresenter.onDestroy()
        detailPresenter.onDestroy()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            (context.getSystemService(ActivityComponent::class.java.simpleName) as ActivityComponent?)?.inject(this)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
            }

            override fun onStopTrackingTouch(p0: SeekBar) {
                playerPresenter.seekTo(p0.progress)
            }
        })
    }

    fun setupBehaviorCallback() {
        bottomSheetBehavior = (layoutParams as CoordinatorLayout.LayoutParams?)?.behavior as BottomSheetBehavior?
        bottomSheetBehavior?.setBottomSheetCallback(callback)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()

        playerPresenter.onSaveInstanceState(bundle)
        detailPresenter.onSaveInstanceState(bundle)

        bundle.putParcelable(PARENT_STATE_KEY, superState)

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var parentState = state

        if (state is Bundle) {
            parentState = state.getParcelable(PARENT_STATE_KEY)

            playerPresenter.onRestoreInstanceState(state)
            detailPresenter.onRestoreInstanceState(state)
        }

        super.onRestoreInstanceState(parentState)
    }

    fun play(podcastItem: PodcastItem) {
        this.podcastItem = podcastItem

        title.text = podcastItem.title
        script.text = null

        playerPresenter.play(podcastItem)
        detailPresenter.loadDetail(podcastItem)

        this.loadingFab?.let {
            it.showAnimated()
            it.startAnimation()
        }
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

    override fun showMessage(@StringRes messageResId: Int) {
        baseActivity?.showMessage(messageResId) ?: context.snack(this, messageResId)
    }

    fun hideFabs() {
        playFab?.hideAnimated()
        pauseFab?.hideAnimated()
        loadingFab?.hideAnimated()
    }

    fun showFabs() {
        if (playerPresenter.isPlaying()) {
            showPauseButton()
        } else if (playerPresenter.isPrepared()) {
            showPlayButton()
        } else {
            showLoadingButton()
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
    }

    fun setControls(playFab: FloatingActionButton, pauseFab: FloatingActionButton, loadingFab: LoadingFloatingActionButton) {
        this.playFab = playFab
        this.pauseFab = pauseFab
        this.loadingFab = loadingFab

        playFab.setOnClickListener {
            playerPresenter.onPlayClicked()
        }

        pauseFab.setOnClickListener {
            playerPresenter.onPauseClicked()
        }
    }

    fun explicitlyStop() {
        playerPresenter.explicitlyStop()
    }

    override fun getPodcastItemParameter(): PodcastItem {
        return podcastItem!!
    }

    override fun setLoading(loading: Boolean) {
        this.loading.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun setPodcastDetail(podcastItemDetail: PodcastItemDetail) {
        script.text = Html.fromHtml(podcastItemDetail.script)
    }
}