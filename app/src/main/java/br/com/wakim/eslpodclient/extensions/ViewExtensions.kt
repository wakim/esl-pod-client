package br.com.wakim.eslpodclient.extensions

import android.support.design.widget.BottomSheetBehavior
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.widget.Toast

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
            });

}

fun View.showAnimated() {
    makeVisible()

    alpha = 0F

    ViewCompat.animate(this)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setInterpolator(interpolator)
            .setListener(null);
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

    if (view.getWindowToken() == null) {
        return false
    }

    return true;
}