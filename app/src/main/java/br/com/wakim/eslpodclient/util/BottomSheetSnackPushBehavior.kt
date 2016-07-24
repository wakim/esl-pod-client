package br.com.wakim.eslpodclient.util

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator

class BottomSheetSnackPushBehavior<V : View> : BottomSheetBehavior<V> {

    companion object {
        val FAST_OUT_SLOW_IN_INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()
    }

    private var mTranslationY: Float = 0F

    constructor() { }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            updateViewTranslationForSnackbar(parent, child, dependency)
        }

        return false
    }

    private fun updateViewTranslationForSnackbar(parent: CoordinatorLayout, view: V, snackbar: View) {
        val translationY = this.getFabTranslationYForSnackbar(parent, view)

        if (translationY != this.mTranslationY) {
            ViewCompat.animate(view).cancel()

            if (Math.abs(translationY - this.mTranslationY) == snackbar.height.toFloat()) {
                ViewCompat.animate(view).translationY(translationY).setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR).setListener(null)
            } else {
                ViewCompat.setTranslationY(view, translationY)
            }

            mTranslationY = translationY
        }
    }

    private fun getFabTranslationYForSnackbar(parent: CoordinatorLayout, view: V): Float {
        val dependencies = parent.getDependencies(view)

        var minOffset = 0.0f
        var i = 0

        val z = dependencies.size

        while (i < z) {
            if (dependencies[i] is Snackbar.SnackbarLayout && parent.doViewsOverlap(view, dependencies[i])) {
                minOffset = Math.min(minOffset, ViewCompat.getTranslationY(dependencies[i]) - dependencies[i].height.toFloat())
            }

            ++i
        }

        return minOffset
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: V, dependency: View) {
        if (dependency is Snackbar.SnackbarLayout && ViewCompat.getTranslationY(child) != 0.0f) {
            updateViewTranslationForSnackbar(parent, child, dependency)
        }
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: V, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }
}
