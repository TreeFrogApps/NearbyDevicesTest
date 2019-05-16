package com.treefrogapps.nearbydevicestest.graphics

import android.graphics.drawable.Animatable2.AnimationCallback
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.support.annotation.UiThread
import com.treefrogapps.nearbydevicestest.graphics.VectorAnimator.Repeat.INFINITE
import com.treefrogapps.nearbydevicestest.graphics.VectorAnimator.Repeat.ONE_SHOT

/**
 * Utility Helper class for playing [AnimatedVectorDrawable] objects and handling repeating and callbacks
 */
class VectorAnimator private constructor(drawables: Array<Drawable?>,
                                         private val repeatType: Repeat,
                                         private val repeatDelay: Long,
                                         private var onEndListener: OnAnimationEnd?) {

    companion object {
        /**
         * Factory Methods for creating [VectorAnimator]
         */
        @JvmStatic fun of() =
                VectorAnimator(arrayOf(),
                               ONE_SHOT,
                               0L,
                               null)

        @JvmStatic fun of(repeatType: Repeat, vararg drawables: Drawable?) =
                VectorAnimator(arrayOf(*drawables),
                               repeatType,
                               0L,
                               null)

        @JvmStatic fun of(repeatType: Repeat, repeatDelay: Long, vararg drawables: Drawable?) =
                VectorAnimator(arrayOf(*drawables),
                               repeatType,
                               repeatDelay,
                               null)

        @JvmStatic fun of(repeatType: Repeat, repeatDelay: Long, onEndListener: OnAnimationEnd?, vararg drawables: Drawable?) =
                VectorAnimator(
                        arrayOf(*drawables),
                        repeatType,
                        repeatDelay,
                        onEndListener)
    }

    enum class Repeat {
        ONE_SHOT, INFINITE
    }

    interface OnAnimationEnd {
        fun onEnd(drawable: Drawable?)
    }

    /**
     * Handler for posting delayed runnables onto
     */
    private val handler = Handler()

    /**
     * keep internal set of AnimatedVectorDrawables
     */
    private val animatedDrawables: MutableSet<AnimatedVectorDrawable> =
            drawables
                    .filterNotNull()
                    .filter { it is AnimatedVectorDrawable }
                    .map { it as AnimatedVectorDrawable }
                    .toMutableSet()

    /**
     * Internal Listener - calls optional [OnAnimationEnd.onEnd] for each [AnimatedVectorDrawable]
     * then removes from the set, until empty, then removes the listener
     */
    private var callback: AnimationCallback = object : AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            if (repeatType == INFINITE) {
                handler.postDelayed({ (drawable as AnimatedVectorDrawable).start() }, repeatDelay)
            } else {
                onEndListener?.onEnd(drawable)
                animatedDrawables.remove(drawable)
                if (animatedDrawables.size == 0) onEndListener = null
                (drawable as AnimatedVectorDrawable).unregisterAnimationCallback(this)
            }
        }
    }

    /**
     * Start animations, check not already running first
     */
    @UiThread
    fun start() {
        animatedDrawables.forEach {
            if (!it.isRunning) {
                it.registerAnimationCallback(callback)
                it.start()
            }
        }
    }

    @UiThread
    fun clear() {
        animatedDrawables.forEach {
            handler.removeCallbacksAndMessages(null)
            it.unregisterAnimationCallback(callback)
            onEndListener = null
            if (it.isRunning) {
                it.stop()
                it.reset()
            }
        }
        animatedDrawables.clear()
    }

    /**
     * Stop animations and do clean up on listeners
     */
    @UiThread
    fun stop() {
        animatedDrawables.forEach {
            handler.removeCallbacksAndMessages(null)
            it.unregisterAnimationCallback(callback)
            if (it.isRunning) it.stop()
            onEndListener = null
        }
    }

    @UiThread
    fun isStarted(): Boolean = animatedDrawables.any { it.isRunning }
}