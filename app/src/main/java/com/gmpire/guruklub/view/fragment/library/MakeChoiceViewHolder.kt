package com.gmpire.guruklub.view.fragment.library

import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.databinding.ItemMakeChoiceBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseViewHolder

abstract class MakeChoiceViewHolder (itemView: ViewDataBinding, context: BaseFragment) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemMakeChoiceBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as ArrayList<BaseItem>
        listener.callBack(position, itemModel,binding.spMakeChoiceItem)

    }
}