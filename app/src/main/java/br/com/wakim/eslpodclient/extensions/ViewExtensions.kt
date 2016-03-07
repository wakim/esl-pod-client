package br.com.wakim.eslpodclient.extensions

import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View

private val interpolator = FastOutSlowInInterpolator()

fun View.isVisible() : Boolean = visibility == View.VISIBLE

fun View.hideAnimated() {
    this.alpha = 1F

    ViewCompat.animate(this)
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setInterpolator(interpolator)
            .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                override fun onAnimationEnd(view: View) {
                    view.visibility = View.GONE
                }
            });

}

fun View.showAnimated() {
    visibility = View.VISIBLE

    this.setAlpha(0f);

    ViewCompat.animate(this)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setInterpolator(interpolator)
            .setListener(null);
}