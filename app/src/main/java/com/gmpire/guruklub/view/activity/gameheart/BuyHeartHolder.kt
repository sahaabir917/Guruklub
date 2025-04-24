package com.gmpire.guruklub.view.activity.gameheart

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.gameheartpackages.GameHeartPackage
import com.gmpire.guruklub.databinding.ItemBuyLifeLayoutBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder

class BuyHeartHolder(itemView: ViewDataBinding,
                     context: Context?): BaseViewHolder(itemView.root) {

    var binding = itemView as ItemBuyLifeLayoutBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {
        itemModel as GameHeartPackage
        binding.costingamount.text = itemModel.price + " tk"
        binding.lifeamount.text = itemModel.hearts

        binding.root.setOnClickListener{
            listener.callBack(position,itemModel,binding.root)
        }
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

}