package me.aluceps.practicedraw

import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation

object AnimationHelper {

    private val scaleUpAnimation
        get() = ScaleAnimation(
            1.0f, 1.3f, 1.0f, 1.3f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            interpolator = OvershootInterpolator()
            fillAfter = true
        }

    private val scaleDownAnimation
        get() = ScaleAnimation(
            1.3f, 1.0f, 1.3f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            interpolator = OvershootInterpolator()
        }

    fun scaleUp(view: View, finished: (() -> Unit)? = null) {
        scaleUpAnimation.apply {
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

    fun scaleDown(view: View, finished: (() -> Unit)? = null) {
        scaleDownAnimation.apply {
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