package br.com.wakim.eslpodclient.extensions

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import br.com.wakim.eslpodclient.service.TypedBinder
import br.com.wakim.eslpodclient.view.BaseActivity
import rx.Observable
import rx.Subscriber

inline fun <reified T: Activity> Context.startActivity(noinline callback : ((Intent) -> Unit)? = null) {
    val intent = Intent(this, T::class.java)

    intent.putExtra(BaseActivity.PARENT_EXTRA, this.javaClass)

    callback?.invoke(intent)

    startActivity(intent)
}

inline fun <reified T: Service> Context.bindService(connection : ServiceConnection, noinline callback : ((Intent) -> Unit)? = null) {
    val intent = Intent(this, T::class.java)

    callback?.invoke(intent)

    bindService(intent, connection, Context.BIND_AUTO_CREATE)
}

inline fun <reified T: Service> Context.bindService(noinline callback : ((Intent) -> Unit)? = null) : Observable<Pair<ServiceConnection, TypedBinder<T>?>> {
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

fun Context.dp(dpValue : Float) : Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, resources.displayMetrics)

fun Context.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_LONG) : Toast {
    val toast = Toast.makeText(this, message, duration)

    toast.show()

    return toast
}

fun Context.snack(view: View, @StringRes message: Int, duration: Int = Snackbar.LENGTH_LONG) : Snackbar {
    val snack = Snackbar.make(view, message, duration)

    snack.show()

    return snack
}