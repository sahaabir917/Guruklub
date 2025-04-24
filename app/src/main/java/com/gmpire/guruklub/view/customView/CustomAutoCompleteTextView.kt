package com.gmpire.guruklub.view.customView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatAutoCompleteTextView

import android.widget.ProgressBar

class CustomAutoCompleteTextView(context: Context?, attrs: AttributeSet?) :
    AppCompatAutoCompleteTextView(context!!, attrs) {
    private var mLoadingIndicator: ProgressBar? = null

    fun setLoadingIndicator(progressBar: ProgressBar?) {
        mLoadingIndicator = progressBar
    }

    override fun performFiltering(text: CharSequence, keyCode: Int) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator?.visibility = View.VISIBLE
        }
        super.performFiltering(text, keyCode)
    }

    override fun onFilterComplete(count: Int) {
        if (mLoadingIndicator != null) {
            mLoadingIndicator?.visibility = View.GONE
        }
        super.onFilterComplete(count)
    }

    override fun replaceText(text: CharSequence?) {
      //  this.dismissDropDown()
    }

}