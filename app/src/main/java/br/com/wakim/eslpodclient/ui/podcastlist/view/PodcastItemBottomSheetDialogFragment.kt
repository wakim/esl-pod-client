package br.com.wakim.eslpodclient.ui.podcastlist.view

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import br.com.wakim.eslpodclient.R
import br.com.wakim.eslpodclient.data.model.PodcastItem

class PodcastItemBottomSheetDialogFragment(private var podcastItem: PodcastItem): BottomSheetDialogFragment(), View.OnClickListener {

    var callback: ((Int, PodcastItem) -> Unit)? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("PODCAST_ITEM", podcastItem)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState?.let {
            podcastItem = it.getParcelable("PODCAST_ITEM")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback = null
    }

    override fun onDestroy() {
        super.onDestroy()
        callback = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setContentView(R.layout.bottomsheet_podcast_item)

        dialog.findViewById(R.id.bt_share).setOnClickListener(this)
        dialog.findViewById(R.id.bt_favorite).setOnClickListener(this)
        dialog.findViewById(R.id.bt_download).setOnClickListener(this)
        dialog.findViewById(R.id.bt_open_with).setOnClickListener(this)

        return dialog
    }

    override fun onClick(view: View) {
        dismiss()
        callback?.invoke(view.id, podcastItem)
    }
}