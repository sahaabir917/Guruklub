package com.gmpire.guruklub.util

import android.util.Log
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter

/**
 * Created by Tahsin Rahman on 11/10/20.
 */


class PercentageExtFormatter(var quesDets: ArrayList<String>) : ValueFormatter() {

    override fun getBarLabel(barEntry: BarEntry?): String {
        Log.d("Bar->", "Executed!")
        return getCustomFormattedValue(barEntry?.y ?: 0f, barEntry?.x ?: 0f)
    }

    fun getCustomFormattedValue(value: Float, pos: Float): String {
        return "$value% (${quesDets.getOrNull(pos.toInt())})"
    }
}