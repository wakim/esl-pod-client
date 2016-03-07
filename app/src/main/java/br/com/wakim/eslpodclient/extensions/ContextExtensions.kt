package br.com.wakim.eslpodclient.extensions

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.TypedValue
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

inline fun <reified T: Service> Context.bindService(noinline callback : ((Intent) -> Unit)? = null) : Observable<Pair<ServiceConnection, Binder?>> {
    val intent = Intent(this, T::class.java)

    callback?.invoke(intent)

    return Observable.create({ subscriber: Subscriber<in Pair<ServiceConnection, Binder?>>? ->
        val connection = object: ServiceConnection {
            override fun onServiceDisconnected(p0: ComponentName?) {
                if (!(subscriber?.isUnsubscribed ?: false)) {
                    subscriber!!.onNext(this to null)
                }
            }

            override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
                if (!(subscriber?.isUnsubscribed ?: false)) {
                    subscriber!!.onNext(this to (binder as Binder))
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