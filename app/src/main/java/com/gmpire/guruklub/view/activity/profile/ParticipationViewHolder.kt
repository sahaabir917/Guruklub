package com.gmpire.guruklub.view.activity.profile

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.leaderboard.PlayedModelTestModel
import com.gmpire.guruklub.databinding.ItemParticipationNameBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.android.play.core.splitinstall.d

class ParticipationViewHolder(itemView: ViewDataBinding, context: Context?) :
        BaseViewHolder(itemView.root) {

    var binding = itemView as ItemParticipationNameBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as PlayedModelTestModel

        if (itemModel.isSelected) {
            binding.ivParticipationSelection.background =
                    mContext?.let { ContextCompat.getDrawable(it, R.drawable.ic_selected) }
        } else {
            binding.ivParticipationSelection.background =
                    mContext?.let { ContextCompat.getDrawable(it, R.drawable.ic_not_selected) }
        }

        binding.tvModelTestName.text = itemModel.title
        binding.tvTotalParticipants.text = "Total Participants - ${itemModel.total_participate}"

        var sizeofTopScorer = itemModel.top_scorer.size - 1
        //
        if (sizeofTopScorer >= 0) {
            if (itemModel.top_scorer[0].name != null) {
                binding.topScorerName.text = itemModel.top_scorer[0].name
            } else {
                binding.topScorerName.text = "No Name"
            }

            if (itemModel.top_scorer[0].profile_pic != null) {
                Glide.with(mContext!!)
                        .load(itemModel.top_scorer[0].profile_pic)
                        .into(binding.topScorerImage)
            } else {
                binding.topScorerImage.setImageResource(R.drawable.ic_placeholder_user)
                Log.d("bydefault", "bydefaultImage")
            }

        } else if (sizeofTopScorer < 0) {
            binding.topScorerName.text = "No Top Scorer Here"
            binding.topScorerImage.setImageResource(R.drawable.ic_placeholder_user)
        }

        binding.root.setOnClickListener {
            listener.callBack(position, itemModel, binding.root)
        }

        binding.yourPosition.setOnClickListener {
            listener.callBack(position, itemModel, binding.root)
        }

        val colorVal = position % 4

        when (colorVal) {
            0 -> {
                binding.viewparticipation.setBackgroundColor(
                        ContextCompat.getColor(
                                binding.tvModelTestName.context,
                                R.color.lightOrange
                        )
                )
            }
            1 -> {
                binding.viewparticipation.setBackgroundColor(
                        ContextCompat.getColor(
                                binding.tvModelTestName.context,
                                R.color.paleLime
                        )
                )
            }
            2 -> {
                binding.viewparticipation.setBackgroundColor(
                        ContextCompat.getColor(
                                binding.tvModelTestName.context,
                                R.color.green
                        )
                )
            }
            3 -> {
                binding.viewparticipation.setBackgroundColor(
                        ContextCompat.getColor(
                                binding.tvModelTestName.context,
                                R.color.indigo
                        )
                )
            }
        }


    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}