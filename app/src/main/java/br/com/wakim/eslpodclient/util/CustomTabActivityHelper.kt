package br.com.wakim.eslpodclient.util

import android.app.Activity
import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsClient
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsServiceConnection
import android.support.customtabs.CustomTabsSession
import android.support.v4.content.ContextCompat
import br.com.wakim.eslpodclient.R
import org.jetbrains.anko.browse

/**
 * This is a helper class to manage the connection to the Custom Tabs Service and
 */
class CustomTabActivityHelper {

    private var mCustomTabsSession: CustomTabsSession? = null
    private var mClient: CustomTabsClient? = null
    private var mConnection: CustomTabsServiceConnection? = null
    private var mConnectionCallback: ConnectionCallback? = null

    /**
     * Unbinds the Activity from the Custom Tabs Service
     * @param activity the activity that is connected to the service
     */
    fun unbindCustomTabsService(activity: Activity) {
        if (mConnection == null) return

        activity.unbindService(mConnection)

        mClient = null
        mCustomTabsSession = null
    }

    /**
     * Creates or retrieves an exiting CustomTabsSession

     * @return a CustomTabsSession
     */
    val session: CustomTabsSession?
        get() {
            if (mClient == null) {
                mCustomTabsSession = null
            } else if (mCustomTabsSession == null) {
                mCustomTabsSession = mClient!!.newSession(null)
            }

            return mCustomTabsSession
        }

    /**
     * Register a Callback to be called when connected or disconnected from the Custom Tabs Service
     * @param connectionCallback
     */
    fun setConnectionCallback(connectionCallback: ConnectionCallback) {
        this.mConnectionCallback = connectionCallback
    }

    /**
     * Binds the Activity to the Custom Tabs Service
     * @param activity the activity to be binded to the service
     */
    fun bindCustomTabsService(activity: Activity) {
        if (mClient != null) return

        val packageName = CustomTabsHelper.getPackageNameToUse(activity) ?: return

        mConnection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                mClient = client
                mClient!!.warmup(0L)

                if (mConnectionCallback != null) mConnectionCallback!!.onCustomTabsConnected()
                //Initialize a session as soon as possible.
                session
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mClient = null

                if (mConnectionCallback != null) mConnectionCallback!!.onCustomTabsDisconnected()
            }
        }

        CustomTabsClient.bindCustomTabsService(activity, packageName, mConnection)
    }

    /**
     * @see {@link CustomTabsSession.mayLaunchUrl
     * @return true if call to mayLaunchUrl was accepted
     */
    fun mayLaunchUrl(uri: Uri, extras: Bundle, otherLikelyBundles: List<Bundle>): Boolean {
        if (mClient == null) return false

        val session = session ?: return false

        return session.mayLaunchUrl(uri, extras, otherLikelyBundles)
    }

    /**
     * A Callback for when the service is connected or disconnected. Use those callbacks to
     * handle UI changes when the service is connected or disconnected
     */
    interface ConnectionCallback {
        /**
         * Called when the service is connected
         */
        fun onCustomTabsConnected()

        /**
         * Called when the service is disconnected
         */
        fun onCustomTabsDisconnected()
    }

    companion object {

        /**
         * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView

         * @param activity The host activity
         * *
         * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available
         * *
         * @param uri the Uri to be opened
         * *
         * @param fallback a CustomTabFallback to be used if Custom Tabs is not available
         */
        fun openCustomTab(activity: Activity, customTabsIntent: CustomTabsIntent, uri: Uri, fallback: ((Activity, Uri) -> Unit)?) {
            val packageName = CustomTabsHelper.getPackageNameToUse(activity)

            //If we cant find a package name, it means theres no browser that supports
            //Chrome Custom Tabs installed. So, we fallback to the webview
            if (packageName == null) {
                fallback?.invoke(activity, uri)
            } else {
                customTabsIntent.intent.`package` = packageName
                customTabsIntent.launchUrl(activity, uri)
            }
        }
    }
}

fun Activity.browseWithCustomTabs(url: String) {
    try {
        CustomTabsIntent.Builder()
                .setShowTitle(true)
                .enableUrlBarHiding()
                .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .addDefaultShareMenuItem()
                .build()
                .apply {
                    CustomTabActivityHelper.openCustomTab(this@browseWithCustomTabs, this, Uri.parse(url)) { activity, uri ->
                        browse(url)
                    }
                }
    } catch (ignored: Exception) {
        browse(url)
    }
}