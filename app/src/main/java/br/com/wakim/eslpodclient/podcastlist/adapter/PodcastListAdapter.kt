package br.com.wakim.eslpodclient.podcastlist.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.model.PodcastItem
import br.com.wakim.eslpodclient.podcastlist.view.PodcastListItemView
import java.util.*

class PodcastListAdapter : RecyclerView.Adapter<PodcastListAdapter.ViewHolder> {

    val list : MutableList<PodcastItem> = mutableListOf()
    val layoutInflater : LayoutInflater

    constructor(context: Context) : super() {
        layoutInflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup?, viewType: Int): PodcastListAdapter.ViewHolder? {
        return ViewHolder(layoutInflater.inflate(R.layout.podcast_list_item, viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: PodcastListAdapter.ViewHolder?, position: Int) {
        viewHolder!!.view().bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun addAll(addition: ArrayList<PodcastItem>) {
        val previousSize = list.size

        list.addAll(addition)

        notifyItemRangeInserted(previousSize, addition.size)
    }

    class ViewHolder : RecyclerView.ViewHolder {
        constructor(view: View) : super(view)

        fun view() : PodcastListItemView = itemView as PodcastListItemView
    }
}
