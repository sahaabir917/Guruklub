package com.gmpire.guruklub.view.activity.leaderDetailsActivity

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.leaderboard.LeaderBoard
import com.gmpire.guruklub.databinding.ItemLeaderBoardBinding
import com.gmpire.guruklub.databinding.ItemLeaderBoardHorizontalBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_leader_board.view.*

class HorizontalLeaderBoardViewHolder (itemView: ViewDataBinding, context: Context?) :
        BaseViewHolder(itemView.root) {

    var binding = itemView as ItemLeaderBoardHorizontalBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as LeaderBoard

        var i :Int  =4

        binding.candidateName.text = itemModel.name
        binding.examdate1.text = itemModel.submit_date.toString()
        binding.serialNumber.text = (position+4).toString()
        binding.scorebutton.text = "${itemModel.get_point}/${itemModel.total_point}"

        if(itemModel.profile_pic!=null){
            Glide.with(mContext!!)
                    .load(itemModel.profile_pic)
                    .override(100,100)
                    .into(binding.profilePicture1)
        }
        else{
            binding.profilePicture1.setImageResource(R.drawable.ic_placeholder_user)
        }

        binding.root.setOnClickListener {
            listener.callBack(position,itemModel,binding.root)
        }

    }

    override fun  onBind(position: Int, mCallback: IAdapterListener) {

    }
}