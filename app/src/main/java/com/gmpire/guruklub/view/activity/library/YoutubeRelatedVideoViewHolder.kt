package com.gmpire.guruklub.view.activity.library

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.gmpire.guruklub.data.model.library.YoutubeObject
import com.gmpire.guruklub.databinding.ItemRelatedVideosBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder

/**
 * Created by Tahsin Rahman on 10/11/20.
 */


class YoutubeRelatedVideoViewHolder(itemView: ViewDataBinding, var context: Context) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemRelatedVideosBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as YoutubeObject

        Glide.with(context).load(itemModel.thumbnailsUrl)
            .into(
                binding.imageViewYoutubeThumbSuggest
            )

        binding.textViewYoutubeTitle.text = itemModel.title
        binding.textViewYoutubeChannelName.text = itemModel.channelTitle
        binding.textViewYoutubeTimestamp.text = itemModel.publishTime

        binding.root.setOnClickListener {
            listener.callBack(position, itemModel, binding.root)
        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

    override fun onLastPosition() {
        // binding.view.visibility = View.GONE
    }
}