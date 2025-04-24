package com.gmpire.guruklub.view.customView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import android.widget.ScrollView


/**
 * Created by Tahsin Rahman on 23/4/21.
 */


class CustomScrollView : ScrollView {

    private lateinit var onScrollTouchCancelListener: OnScrollTouchCancelListener

    constructor(
        context: Context
    )
            : super(context)

    constructor(
        context: Context,
        attrs: AttributeSet? = null
    )
            : super(context, attrs) {
        onScrollTouchCancelListener = context as OnScrollTouchCancelListener
    }


    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            onScrollTouchCancelListener.onTouchCancel()
        }
        return super.onTouchEvent(ev)
    }

    interface OnScrollTouchCancelListener {
        fun onTouchCancel()
    }

}