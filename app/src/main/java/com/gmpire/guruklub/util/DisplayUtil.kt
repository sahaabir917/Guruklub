package com.gmpire.guruklub.util

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

object DisplayUtil {
    public fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics = context.resources?.displayMetrics
        return (dp * (displayMetrics!!.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}