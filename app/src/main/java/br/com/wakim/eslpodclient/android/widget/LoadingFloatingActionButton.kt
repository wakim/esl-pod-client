package br.com.wakim.eslpodclient.android.widget

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import br.com.wakim.eslpodclient.R

class LoadingFloatingActionButton : FloatingActionButton {

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    var animator: ObjectAnimator? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        setImageResource(R.drawable.ic_loop_white_24dp)
    }

    fun startAnimation() {
        animator?.let {
            return
        }

        animator = ObjectAnimator.ofFloat(this, "rotation", 0F, 360F)

        with (animator!!) {
            duration = 1000L

            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART

            interpolator = LinearInterpolator()

            start()
        }
    }

    fun stopAnimation() {
        animator?.let {
            animator!!.cancel()
            animator = null
        }
    }
}