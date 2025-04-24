package com.gmpire.guruklub.view.customView

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.viewpager.widget.ViewPager

class CustomScrollableViewPager : ViewPager {
    constructor(@NonNull context: Context?) : super(context!!) {}
    constructor(
        @NonNull context: Context?,
        @Nullable attrs: AttributeSet?
    ) : super(context!!, attrs) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        try {
            val numChildren = childCount
            for (i in 0 until numChildren) {
                val child = getChildAt(i)
                if (child != null) {
                    child.measure(
                        widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    )
                    val h = child.measuredHeight
                    heightMeasureSpec = Math.max(
                        heightMeasureSpec,
                        MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}