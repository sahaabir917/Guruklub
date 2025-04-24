package com.gmpire.guruklub.util

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.SubscriptSpan

object SubscriptUtil {
    fun checkIfContainsSubscript(text: String): Boolean {
        return text.contains("<sub>") || text.contains("<sup>")
    }

    fun getSubscriptSpan(text: Spanned): SpannableStringBuilder {
        val sb = SpannableStringBuilder(text)
        sb.setSpan(
            SubscriptSpan(),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return sb
    }
}