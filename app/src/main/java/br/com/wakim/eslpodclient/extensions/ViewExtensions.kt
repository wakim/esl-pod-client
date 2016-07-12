package br.com.wakim.eslpodclient.extensions

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.widget.Toast
import br.com.wakim.eslpodclient.BuildConfig
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

private val interpolator = FastOutSlowInInterpolator()

fun View.isVisible() : Boolean = visibility == View.VISIBLE

fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeHidden() {
    visibility = View.GONE
}

fun View.hideAnimated() {
    if (isVisible()) {
        alpha = 1F
    } else {
        return
    }

    ViewCompat.animate(this)
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setInterpolator(interpolator)
            .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                override fun onAnimationEnd(view: View) {
                    view.visibility = View.GONE
                }
            })

}

fun View.showAnimated() {
    makeVisible()

    alpha = 0F

    ViewCompat.animate(this)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setInterpolator(interpolator)
            .setListener(null)
}

fun BottomSheetBehavior<*>.toggleState(@BottomSheetBehavior.State state1 : Int, @BottomSheetBehavior.State state2 : Int) {
    when (this.state) {
        state1 -> state = state2
        state2 -> state = state1
    }
}

fun Toast.isVisible(): Boolean {
    if (view == null) {
        return false
    }

    if (view.windowToken == null) {
        return false
    }

    return true
}

fun Fragment.logContentView() {
    if (!BuildConfig.DEBUG) {
        val contentView = ContentViewEvent()
                .putContentName(javaClass.name)

        Answers.getInstance().logContentView(contentView)
    }
}

fun AdView.loadAds() {
    val extras = Bundle().apply {
        putBoolean("is_designed_for_families", true)
    }

    val adRequest = AdRequest.Builder()
            .setIsDesignedForFamilies(true)
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .addTestDevice("1FE3D6ABBB54E8FED73AA3582F320467")
            .build()

    loadAd(adRequest)
}