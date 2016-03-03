package br.com.wakim.eslpodclient.podcastlist.view

import android.content.Context
import android.support.v7.widget.LinearLayoutCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.model.PodcastItem
import butterknife.bindView
import com.squareup.picasso.Picasso

class PodcastListItemView : LinearLayoutCompat {

    val ivCategory : ImageView? by bindView(R.id.iv_category)
    val tvTitle : TextView? by bindView(R.id.tv_title)
    val tvDescription : TextView? by bindView(R.id.tv_description)
    val tvDate : TextView? by bindView(R.id.tv_date)
    val tvTags : TextView? by bindView(R.id.tv_tags)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    fun bind(podcastItem : PodcastItem) {
        tvTitle!!.text = podcastItem.title
        tvDescription!!.text = podcastItem.blurb
        tvDate!!.text = podcastItem.date.toString()

        tvTags?.let {
            it.visibility = if (podcastItem.tags == null) View.GONE else View.VISIBLE
            it.text = podcastItem.tags ?: null
        }

        Picasso.with(context)
            .load(if (podcastItem.type == PodcastItem.ENGLISH_CAFE) R.drawable.ic_local_cafe_white_24dp else R.drawable.ic_chat_white_24dp)
            .into(ivCategory)
    }
}