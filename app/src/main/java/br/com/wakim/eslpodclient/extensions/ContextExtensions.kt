package br.com.wakim.eslpodclient.extensions

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.support.annotation.RawRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.TypedValue
import android.view.View
import br.com.wakim.eslpodclient.Application
import br.com.wakim.eslpodclient.service.TypedBinder
import br.com.wakim.eslpodclient.view.BasePresenterActivity
import rx.Observable
import rx.Subscriber
import java.io.BufferedReader
import java.io.InputStreamReader

inline fun <reified T: Activity> Context.startActivity(noinline callback : ((Intent) -> Unit)? = null) {
    val intent = Intent(this, T::class.java)

    intent.putExtra(BasePresenterActivity.PARENT_EXTRA, this.javaClass)

    callback?.invoke(intent)

    startActivity(intent)
}

inline fun <reified T: Service> Context.bindService(connection : ServiceConnection, noinline callback : ((Intent) -> Unit)? = null) {
    val intent = Intent(this, T::class.java)

    callback?.invoke(intent)

    bindService(intent, connection, Context.BIND_AUTO_CREATE)
}

inline fun <reified T: Service> Context.bindService(startBefore: Boolean = true, noinline callback: ((Intent) -> Unit)? = null) : Observable<Pair<ServiceConnection, TypedBinder<T>?>> {
    val intent = Intent(this, T::class.java)

    callback?.invoke(intent)

    return Observable.create({ subscriber: Subscriber<in Pair<ServiceConnection, TypedBinder<T>?>>? ->
        val connection = object: ServiceConnection {
            override fun onServiceDisconnected(p0: ComponentName?) {
                if (!(subscriber?.isUnsubscribed ?: false)) {
                    subscriber!!.onNext(this to null)
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
                if (!(subscriber?.isUnsubscribed ?: false)) {
                    subscriber!!.onNext(this to (binder as? TypedBinder<T>))
                }
            }
        }

        if (startBefore) {
            startService(intent)
        }

        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    })
}

inline fun <reified T: Service> Context.startService(noinline callback : ((Intent) -> Unit)? = null) {
    val intent = Intent(this, T::class.java)

    callback?.invoke(intent)

    startService(intent)
}

inline fun <reified T: Service> Context.stopService() {
    val intent = Intent(this, T::class.java)
    stopService(intent)
}

fun Fragment.dp(dpValue: Float): Float = context.dp(dpValue)

fun Context.dp(dpValue : Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, resources.displayMetrics)

fun Fragment.dp(dpValue: Int): Int = context.dp(dpValue)

fun Context.dp(dpValue : Int) : Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(), resources.displayMetrics).toInt()

fun snack(view: View, @StringRes message: Int, duration: Int = Snackbar.LENGTH_LONG) : Snackbar {
    val snack = Snackbar.make(view, message, duration)

    snack.show()

    return snack
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Intent.startActivity(activity: Activity): Boolean {
    if (resolveActivity(activity.packageManager) != null) {
        activity.startActivity(this)
        return true
    }

    return false
}

fun Intent.view(string: String): Intent {
    action = Intent.ACTION_VIEW
    data = Uri.parse(string)

    return this
}

fun Context.readRawString(@RawRes rawResId: Int): String =
        BufferedReader(InputStreamReader(resources.openRawResource(rawResId))).use {
            return it.lineSequence().joinToString("\n")
        }

fun Context.resolveRawResIdentifier(name: String): Int =
        resources.getIdentifier(name, "raw", packageName)

fun String.d() {
    Log.d(Application.INSTANCE?.packageName ?: "TAG", this)
}

fun Throwable.e() {
    Log.e(Application.INSTANCE?.packageName ?: "TAG", "Error", this)
}