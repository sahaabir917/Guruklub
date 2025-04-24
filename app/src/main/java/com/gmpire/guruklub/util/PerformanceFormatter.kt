package com.gmpire.guruklub.util

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class PerformanceFormatter(var subjects : ArrayList<String>) : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return subjects.getOrNull(value.toInt()) ?: value.toString()
    }
}