package com.gmpire.guruklub.view

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.databinding.EmptyPageBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder

 class EmptyViewHolder (itemView: ViewDataBinding, context: Context, val message:String) :
    BaseViewHolder(itemView.root) {
    var binding = itemView as EmptyPageBinding

    override fun  onBind(position: Int, listener: IAdapterListener) {

        binding.tvMessage.text = message
    }

     override fun <T> onBind(position: Int,model:T, mCallback: IAdapterListener) {

     }
}