package com.gmpire.guruklub.view.fragment.library

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.library.MostPopularAndRecentLearning
import com.gmpire.guruklub.databinding.ItemRecentlyLearnBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder

class RecentlyLearnViewHolder(itemView: ViewDataBinding, context: Context?) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemRecentlyLearnBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as MostPopularAndRecentLearning
        binding.tvValue.text = "${itemModel.topic_name}, ${itemModel.subject_name}"
        binding.totalViews.text = itemModel.total_views.toString()
        binding.root.setOnClickListener {
            listener.callBack(position, itemModel, binding.root)
        }

        when (position) {
            0 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.palePurple
                    )
                )
            }
            1 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.paleLime
                    )
                )
            }
            2 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.lightRed
                    )
                )
            }
            3 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.green
                    )
                )
            }
            4 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.yellow
                    )
                )
            }

            5 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.palePurple
                    )
                )
            }

            6 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.paleLime
                    )
                )
            }

            7 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.lightRed
                    )
                )
            }

            8 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.green
                    )
                )
            }

            9 -> {
                binding.cardViewPopular.setCardBackgroundColor(
                    ContextCompat.getColor(
                        mContext!!,
                        R.color.yellow
                    )
                )
            }



        }
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {
    }
}