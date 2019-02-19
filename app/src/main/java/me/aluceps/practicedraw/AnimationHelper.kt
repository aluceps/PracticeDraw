package me.aluceps.practicedraw

import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation

object AnimationHelper {

    private val scaleAnimation
        get() = ScaleAnimation(
            1.0f, 1.2f, 1.0f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            interpolator = OvershootInterpolator()
        }

    fun setAnimation(view: View, finished: (() -> Unit)? = null) {
        scaleAnimation.apply {
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    view.isEnabled = true
                    finished?.invoke()
                }

                override fun onAnimationStart(animation: Animation?) {
                    view.isEnabled = false
                }
            })
        }.let { view.startAnimation(it) }
    }
}