package com.gmpire.guruklub.view.activity.gameheart

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.gameheartpackages.GameHeartPackage
import com.gmpire.guruklub.databinding.ItemSubscribeDetailsBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder

class UnLimitedHeartAdapter(
    itemView: ViewDataBinding,
    context: Context?
) : BaseViewHolder(itemView.root) {

    var selectedPosition: Int = 0
    var binding = itemView as ItemSubscribeDetailsBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {
        if (itemModel is GameHeartPackage) {
            binding.textView25.text = """${itemModel.days} days - ${itemModel.price} Tk"""
            binding.textView27.text = "Unlimited Hearts for " + itemModel.days + " days"

            val drawableTop = binding.subscribeBg.background as GradientDrawable
            var color = ColorUtil.getSubscriptionColorByPosition(position)
            drawableTop.setColor(ContextCompat.getColor(mContext!!, color))

            if (itemModel.switchOnOff) {
                binding.imageView33.setImageResource(R.drawable.subscribe_switch_on_btn)
            } else {
                binding.imageView33.setImageResource(R.drawable.subscribe_switch_off_btn)
            }

            binding.imageView33.setOnClickListener {
                selectedPosition = position
                if (!itemModel.switchOnOff) {
                    binding.imageView33.setImageResource(R.drawable.subscribe_switch_on_btn)
                    itemModel.switchOnOff = false
                    listener.callBack(position, itemModel, itemView)
                } else {
                    binding.imageView33.setImageResource(R.drawable.subscribe_switch_off_btn)
                    itemModel.switchOnOff = true
                    listener.callBack(position, itemModel, itemView)
                }
            }
        }
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

}