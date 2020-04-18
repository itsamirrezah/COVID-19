package com.itsamirrezah.covid19.util

import android.animation.AnimatorListenerAdapter
import android.view.View

class TransitionUtils {

    companion object {

        fun revealView(view: View, duration: Long, listener: AnimatorListenerAdapter) {
            val startAlpha = 0f
            val endAlpha = 1f
            crossFadeTransition(view, duration, listener, startAlpha, endAlpha)
        }

        fun hideView(view: View, duration: Long, listener: AnimatorListenerAdapter) {
            val startAlpha = 1f
            val endAlpha = 0f
            crossFadeTransition(view, duration, listener, startAlpha, endAlpha)
        }

        private fun crossFadeTransition(
            view: View,
            duration: Long,
            listener: AnimatorListenerAdapter,
            startAlpha: Float,
            endAlpha: Float
        ) {
            view.apply {
                view.alpha = startAlpha
                visibility = View.VISIBLE
                animate()
                    .alpha(endAlpha)
                    .setDuration(duration)
                    .setListener(listener)
            }
        }

    }
}