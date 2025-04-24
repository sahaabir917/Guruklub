package com.gmpire.guruklub.view.activity.friendrequest

import android.content.Context
import android.util.Log
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.gameheartpackages.GameHeartPackage
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.ItemBuyLifeLayoutBinding
import com.gmpire.guruklub.databinding.ItemFriendSuggestionBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport

class FriendSuggestionViewHolder(itemView: ViewDataBinding,
                     context: Context?): BaseViewHolder(itemView.root) {

    var binding = itemView as ItemFriendSuggestionBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {
        itemModel as UserInfo
        binding.userName.text = itemModel.name
        binding.textView21.text =  "level : " + itemModel.user_game_level

        binding.button3.setOnClickListener{
            binding.button3.text = "Request Sent"
            listener.callBack(position,itemModel,binding.root)
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