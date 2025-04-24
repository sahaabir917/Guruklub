package com.gmpire.guruklub.view.adapter

import android.view.View
import android.view.ViewGroup
import com.gmpire.guruklub.view.base.BaseViewHolder

interface IAdapterListener {
    fun <T> callBack(position: Int, model: T, view: View)
    fun  getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder
    fun  loadMoreItem()
}