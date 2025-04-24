package com.gmpire.guruklub.view.activity.gamelevel

import android.animation.Animator
import com.gmpire.guruklub.databinding.ItemLevelPageBinding


import android.content.Context
import android.view.View
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.gamelevel.Level
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder

class LevelPageViewHolder(
    itemView: ViewDataBinding,
    context: Context?,
    myLevel: Level?,
    user: UserInfo
) : BaseViewHolder(itemView.root) {

    var binding = itemView as ItemLevelPageBinding
    var mContext = context
    var userInfo = user
    private var myLevelPos = myLevel?.position ?: 0
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {
        if (itemModel is Level) {
            binding.constraintLayoutSingleLevel.setOnClickListener {
                listener.callBack(position, itemModel, binding.constraintLayoutSingleLevel)
            }
            binding.tvLevel.text = itemModel.level

            if (binding.rlUserIcon.visibility == View.VISIBLE)
                binding.rlUserIcon.visibility = View.GONE

            if (itemModel.position <= myLevelPos) {
                binding.ivLockSymbol.visibility = View.GONE
            } else {
                binding.ivLockSymbol.visibility = View.VISIBLE
            }

            if (itemModel.position == myLevelPos) {
                Glide.with(mContext!!)
                    .load(BuildConfig.SERVER_URL + userInfo.picture)
                    .placeholder(R.drawable.ic_placeholder_user)
                    .error(R.drawable.ic_placeholder_user)
                    .into(binding.icvUserIcon)
                binding.ivLockSymbol.visibility = View.GONE
                binding.rlUserIcon.visibility = View.VISIBLE
                binding.icvUserIcon
                    .animate()
                    .scaleX(2f)
                    .scaleY(2f)
                    .setDuration(2000)
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(2000)
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            binding.icvUserIcon
                                .animate()
                                .scaleX(1f)
                                .scaleY(1f)
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                    })
            }

        }

    }


    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

}