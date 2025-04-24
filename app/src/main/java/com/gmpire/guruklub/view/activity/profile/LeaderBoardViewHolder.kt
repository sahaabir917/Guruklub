package com.gmpire.guruklub.view.activity.profile

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.leaderboard.LeaderBoard
import com.gmpire.guruklub.databinding.ItemLeaderBoardBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder

 class LeaderBoardViewHolder (itemView: ViewDataBinding, context: Context?) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemLeaderBoardBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as LeaderBoard

        binding.tvPosition.text = (position+1).toString()
        binding.tvName.text = itemModel.name
        binding.tvScore.text = "${itemModel.get_point}/${itemModel.total_point}"

        binding.root.setOnClickListener {
            listener.callBack(position,itemModel,binding.root)
        }

    }

     override fun  onBind(position: Int, mCallback: IAdapterListener) {

     }
}