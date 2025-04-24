package com.gmpire.guruklub.util

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View

class AnimationManager private constructor() {

    fun expand(view: View) {
        //set Visible
        view.visibility = View.VISIBLE

        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)

        val mAnimator = slideAnimator(0, view.measuredHeight, view)
        mAnimator.start()
    }

    fun collapse(view: View) {
        val finalHeight = view.height

        val mAnimator = slideAnimator(finalHeight, 0, view)

        mAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                //Height=0, but it set visibility to GONE
                view.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        mAnimator.start()
    }

    fun slideAnimator(start: Int, end: Int, view: View): ValueAnimator {

        val animator = ValueAnimator.ofInt(start, end)
        animator.addUpdateListener { valueAnimator ->
            //Update Height
            val value = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.height = value
            view.layoutParams = layoutParams
        }
        return animator
    }

    companion object {
        internal var instance: AnimationManager = AnimationManager()
        fun getInstance(): AnimationManager {
            return if (instance == null) {
                AnimationManager()
            } else {
                return instance
            }
        }
    }
}