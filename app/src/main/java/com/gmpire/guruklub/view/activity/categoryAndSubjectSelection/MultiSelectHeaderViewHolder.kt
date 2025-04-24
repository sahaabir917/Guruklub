package com.gmpire.guruklub.view.activity.categoryAndSubjectSelection

import android.view.View
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.categoryAndSubject.HeaderFilter
import com.gmpire.guruklub.databinding.ItemMultiselectHeaderBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder

class MultiSelectHeaderViewHolder(itemView: ViewDataBinding) :
    BaseViewHolder(itemView.root) {
    var binding = itemView as ItemMultiselectHeaderBinding

    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {
        itemModel as HeaderFilter
        binding.headerText.text = itemModel.name

        binding.itemAll.isChecked = itemModel.isAllSelected

        if (position == 0) {
            binding.viewSpace.visibility = View.GONE
        } else {
            binding.viewSpace.visibility = View.VISIBLE
        }

        binding.itemAll.setOnClickListener {
            listener.callBack(position, itemModel, binding.itemAll)
        }
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {
    }
}