package br.com.wakim.eslpodclient.ui.podcastlist.view

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.data.model.PodcastItem
import br.com.wakim.eslpodclient.util.extensions.makeHidden
import br.com.wakim.eslpodclient.util.extensions.makeVisible
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import pl.charmas.android.tagview.TagView

class PodcastListItemView : CardView {

    @BindView(R.id.iv_category)
    lateinit var ivCategory: ImageView

    @BindView(R.id.fl_category)
    lateinit var flCategory: FrameLayout

    @BindView(R.id.tv_title)
    lateinit var tvTitle: TextView

    @BindView(R.id.tv_subtitle)
    lateinit var tvSubtitle: TextView

    @BindView(R.id.tv_tags)
    lateinit var tvTags: TagView

    var overflowMenuClickListener: ((PodcastItem, View) -> Unit)? = null

    var podcastItem: PodcastItem? = null

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    override fun onFinishInflate() {
        super.onFinishInflate()

        ButterKnife.bind(this)
    }

    @OnClick(R.id.ib_overflow)
    fun onOverflowClick(btnOverflow: ImageButton) {
        overflowMenuClickListener?.invoke(podcastItem!!, btnOverflow)
    }

    fun bind(podcastItem : PodcastItem) {
        this.podcastItem = podcastItem

        with (podcastItem) {
            tvTitle.text = userFriendlyTitle

            if (isEnglishCafe()) {
                tvSubtitle.makeHidden()
            } else {
                tvSubtitle.makeVisible()
                tvSubtitle.text = podcastName
            }

            tvTags.let {
                it.visibility = if (tags == null) View.GONE else View.VISIBLE
                it.setTags(tagList)
            }

            val isCafe = type == PodcastItem.ENGLISH_CAFE

            val drawableResId = if (isCafe) R.drawable.ic_local_cafe_white_24dp else R.drawable.ic_chat_white_24dp
            val colorResId = if (isCafe) R.color.category_cafe else R.color.category_podcast

            ivCategory.setImageResource(drawableResId)
            flCategory.setBackgroundColor(ContextCompat.getColor(context, colorResId))
        }
    }
}