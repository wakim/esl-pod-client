package br.com.wakim.eslpodclient.podcastlist.view

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.extensions.makeHidden
import br.com.wakim.eslpodclient.extensions.makeVisible
import br.com.wakim.eslpodclient.model.PodcastItem
import butterknife.bindView
import pl.charmas.android.tagview.TagView

class PodcastListItemView : CardView {

    val ivCategory: ImageView by bindView(R.id.iv_category)
    val flCategory: FrameLayout by bindView(R.id.fl_category)
    val tvTitle: TextView by bindView(R.id.tv_title)
    val tvSubtitle: TextView by bindView(R.id.tv_subtitle)
    val tvTags: TagView by bindView(R.id.tv_tags)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    fun bind(podcastItem : PodcastItem) {
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