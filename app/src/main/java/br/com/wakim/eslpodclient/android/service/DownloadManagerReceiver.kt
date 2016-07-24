package br.com.wakim.eslpodclient.android.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.wakim.eslpodclient.dagger.AppComponent
import br.com.wakim.eslpodclient.data.interactor.StorageInteractor
import javax.inject.Inject
import kotlin.concurrent.thread

class DownloadManagerReceiver: BroadcastReceiver() {

    @Inject
    lateinit var storageInteractor: StorageInteractor

    @Inject
    lateinit var downloadManager: DownloadManager

    override fun onReceive(context: Context, intent: Intent?) {
        (context.applicationContext.getSystemService(AppComponent::class.java.simpleName) as AppComponent).inject(this)

        intent?.let {
            if (it.action == DownloadManager.ACTION_NOTIFICATION_CLICKED) {
                handleNotificationClicked(context)
            }

            if (it.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                verifyCompletedDownload(it)
            }
        }
    }

    fun handleNotificationClicked(context: Context) {
        context.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun verifyCompletedDownload(intent: Intent) {
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)

        if (id == -1L) {
            return
        }

        thread(start = true) {
            var status = DownloadManager.STATUS_SUCCESSFUL or DownloadManager.STATUS_FAILED

            val query = DownloadManager.Query()
                    .setFilterById(id)
                    .setFilterByStatus(status)

            downloadManager.query(query)
                    .apply {
                        val statusIndex = getColumnIndex(DownloadManager.COLUMN_STATUS)

                        if (moveToFirst()) {
                            status = getInt(statusIndex)

                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                storageInteractor.handleDownloadCompletion(id)
                            } else {
                                storageInteractor.handleDownloadFailed(id)
                            }
                        } else {
                            storageInteractor.handleDownloadFailed(id)
                        }
                    }.close()
        }
    }
}