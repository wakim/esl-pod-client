package br.com.wakim.eslpodclient.ui.settings.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import br.com.wakim.eslpodclient.R
import org.jetbrains.anko.find

class LicenseListAdapter : RecyclerView.Adapter<LicenseListAdapter.ViewHolder> {

    companion object {
        const val LOADING_TYPE = 0
        const val ITEM_TYPE = 1
    }

    val list : MutableList<Pair<String, String>> = mutableListOf()
    val layoutInflater : LayoutInflater

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

    override fun onCreateViewHolder(viewGroup: ViewGroup?, viewType: Int): ViewHolder? {
        return if (viewType == ITEM_TYPE)
             ViewHolder(layoutInflater.inflate(R.layout.list_item_license, viewGroup, false))
        else ViewHolder(layoutInflater.inflate(R.layout.list_item_loading, viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, position: Int) {
        if (viewHolder!!.itemViewType == LOADING_TYPE) {
            val lp = viewHolder.itemView.layoutParams as? RecyclerView.LayoutParams
            lp?.height = if (list.size == 0) RecyclerView.LayoutParams.MATCH_PARENT else RecyclerView.LayoutParams.WRAP_CONTENT
        } else {
            val item = list[position]

            viewHolder.title.text = item.first
            viewHolder.body.text = item.second
        }
    }

    override fun getItemCount(): Int = list.size + if (loading) 1 else 0

    fun addAll(addition: List<Pair<String, String>>) {
        val previousSize = list.size

        list.addAll(addition)

        notifyItemRangeInserted(previousSize, addition.size)
    }

    class ViewHolder : RecyclerView.ViewHolder {
        val title: TextView
        val body: TextView

        constructor(view: View) : super(view) {
            title = view.find(R.id.tv_title)
            body = view.find(R.id.tv_body)
        }
    }
}
