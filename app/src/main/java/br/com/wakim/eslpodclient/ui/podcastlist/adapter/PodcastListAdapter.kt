package br.com.wakim.eslpodclient.ui.podcastlist.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.data.model.PodcastItem
import br.com.wakim.eslpodclient.ui.podcastlist.view.PodcastListItemView
import java.util.*

class PodcastListAdapter : RecyclerView.Adapter<PodcastListAdapter.ViewHolder>,
        View.OnClickListener {

    companion object {
        const val LOADING_TYPE = 0
        const val ITEM_TYPE = 1
    }

    val list : MutableList<PodcastItem> = mutableListOf()
    val layoutInflater : LayoutInflater

    var clickListener : ((PodcastItem) -> Unit)? = null
    var overflowMenuClickListener : ((PodcastItem, View) -> Unit)? = null

    var loading : Boolean = false
        set(value) {
            val old = field

            field = value

            if (old != value) {
                val size = list.size

                if (value)
                    notifyItemInserted(size)
                else
                    notifyItemRemoved(size)
            }
        }

    constructor(context: Context) : super() {
        layoutInflater = LayoutInflater.from(context)
    }

    override fun getItemViewType(position: Int): Int =
            if (loading && position == list.size) LOADING_TYPE else ITEM_TYPE

    override fun onCreateViewHolder(viewGroup: ViewGroup?, viewType: Int): PodcastListAdapter.ViewHolder? {
        return if (viewType == ITEM_TYPE) {
            val view = layoutInflater.inflate(R.layout.list_item_podcast, viewGroup, false) as PodcastListItemView

            view.setOnClickListener(this)
            view.overflowMenuClickListener = overflowMenuClickListener

            ViewHolder(view)
        } else ViewHolder(layoutInflater.inflate(R.layout.list_item_loading, viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: PodcastListAdapter.ViewHolder?, position: Int) {
        if (viewHolder!!.itemViewType == LOADING_TYPE) {
            val lp = viewHolder.itemView.layoutParams as? RecyclerView.LayoutParams
            lp?.height = if (list.size == 0) RecyclerView.LayoutParams.MATCH_PARENT else RecyclerView.LayoutParams.WRAP_CONTENT
        } else {
            val item = list[position]

            viewHolder.view()?.bind(item)
        }
    }

    override fun getItemCount(): Int = list.size + if (loading) 1 else 0

    fun setItems(list: ArrayList<PodcastItem>) {
        this.list.clear()
        this.list.addAll(list)

        notifyDataSetChanged()
    }

    fun add(podcastItem: PodcastItem) {
        if (list.contains(podcastItem)) {
            return
        }

        list.add(podcastItem)
        notifyItemInserted(list.size - 1)
    }

    fun addAll(addition: ArrayList<PodcastItem>) {
        val previousSize = list.size

        list.addAll(addition)

        notifyItemRangeInserted(previousSize, addition.size)
    }

    fun removeAll() {
        val size = list.size

        list.clear()

        notifyItemRangeRemoved(0, size)
    }

    fun remove(podcastItem: PodcastItem) {
        val indexOf = list.indexOf(podcastItem)

        if (indexOf > -1) {
            list.removeAt(indexOf)
            notifyItemRemoved(indexOf)
        }
    }

    override fun onClick(view: View) {
        if (view is PodcastListItemView) {
            clickListener?.invoke(view.podcastItem!!)
        }
    }

    class ViewHolder : RecyclerView.ViewHolder {
        constructor(view: View) : super(view)
        fun view() : PodcastListItemView? = itemView as? PodcastListItemView
    }
}
