package com.gmpire.guruklub.view.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.categoryAndSubject.HeaderFilter
import com.gmpire.guruklub.view.base.BaseViewHolder

const val TYPE_HEADER = 0
const val TYPE_ITEM = 1

class HeaderRecyclerViewAdapter(var mListener: IAdapterListener, var list: ArrayList<BaseItem>) :
    RecyclerView.Adapter<BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return mListener.getViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionHeader(position)) TYPE_HEADER else TYPE_ITEM
    }

    private fun isPositionHeader(position: Int): Boolean {
        return list[position] is HeaderFilter
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position, list[position], mListener)
    }

    fun notifyDataChange(list: ArrayList<BaseItem>) {
        list.clear()
        list.addAll(list)
        notifyDataSetChanged()
    }
}