package com.gmpire.guruklub.view.activity.library

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.library.Videos
import com.gmpire.guruklub.databinding.ItemLibraryVideoBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder

 class LibraryVideoViewHolder (itemView: ViewDataBinding, context: Context) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemLibraryVideoBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as Videos


    }

     override fun  onBind(position: Int, mCallback: IAdapterListener) {

     }
}