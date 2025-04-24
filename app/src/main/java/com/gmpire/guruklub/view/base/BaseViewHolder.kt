package com.gmpire.guruklub.view.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gmpire.guruklub.view.adapter.IAdapterListener

abstract class BaseViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun<T> onBind(position: Int, model:T, mCallback : IAdapterListener)
    abstract fun onBind(position: Int, mCallback : IAdapterListener)
    open fun onLastPosition(){

    }

}