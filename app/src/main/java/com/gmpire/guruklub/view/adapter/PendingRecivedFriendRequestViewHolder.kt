package com.gmpire.guruklub.view.adapter

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.ItemPendingRecivedFriendRequestBinding
import com.gmpire.guruklub.databinding.ItemPendingSentFriendRequestBinding
import com.gmpire.guruklub.view.base.BaseViewHolder

class PendingRecivedFriendRequestViewHolder(itemView: ViewDataBinding,
                                         context: Context?): BaseViewHolder(itemView.root) {

    var binding = itemView as ItemPendingRecivedFriendRequestBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {
        itemModel as UserInfo
        binding.userName.text = itemModel.name
        binding.textView21.text =  "level : " + itemModel.user_game_level

        binding.button3.setOnClickListener{
            binding.button3.text = "accepted!"
            listener.callBack(position,itemModel,binding.button3)
        }

        binding.button4.setOnClickListener {
            binding.button4.text = "Deleted"
            listener.callBack(position,itemModel,binding.button4)
        }

        if (itemModel.picture != null) {
            Glide.with(mContext!!)
                .load(BuildConfig.SERVER_URL+itemModel.picture)
                .into(binding.imageView14)
        } else {
            binding.imageView14.setImageResource(R.drawable.ic_placeholder_user)
        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

}