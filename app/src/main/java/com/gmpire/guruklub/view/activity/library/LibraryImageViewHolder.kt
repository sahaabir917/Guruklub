package com.gmpire.guruklub.view.activity.library

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.library.Images
import com.gmpire.guruklub.databinding.ItemLibraryImageBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.bumptech.glide.Glide

class LibraryImageViewHolder (itemView: ViewDataBinding, context: Context) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemLibraryImageBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as Images

        Glide.with(mContext).load("http://demo.gmpire.com/${itemModel.picture}").into(binding.ivLibrary)



    }

    override fun  onBind(position: Int, mCallback: IAdapterListener) {

    }
}