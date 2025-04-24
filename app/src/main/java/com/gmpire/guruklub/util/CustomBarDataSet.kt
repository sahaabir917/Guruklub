package com.gmpire.guruklub.util

import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry


class CustomBarDataSet(
    yVals: List<BarEntry?>?,
    label: String?
) :
    BarDataSet(yVals, label) {
    override fun getColor(index: Int): Int {
        return when {
            getEntryForIndex(index).y < 50 // less than 95 green
            -> mColors[0]
            getEntryForIndex(index).y >= 75 // less than 100 orange
            -> mColors[1]
            else  // greater or equal than 100 red
            -> mColors[2]
        }
    }

    override fun getEntryIndex(e: BarEntry?): Int {
        super.getEntryIndex(e)
        return 0
    }
}