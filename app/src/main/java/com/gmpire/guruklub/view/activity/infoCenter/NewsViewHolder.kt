package com.gmpire.guruklub.view.activity.infoCenter

import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.data.model.infocenter.News
import com.gmpire.guruklub.databinding.ItemNewsBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.bumptech.glide.Glide
import com.gmpire.guruklub.R

class NewsViewHolder(itemView: ViewDataBinding, context: BaseFragment) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemNewsBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as News

        itemModel.image_list?.let {
            if (it.size > 0) {
                Glide.with(mContext).load(BuildConfig.SERVER_URL + it[0].picture)
                    .fitCenter()
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .into(binding.newsImageIv)
            } else {
                Glide.with(mContext).load(R.drawable.ic_image)
                    .fitCenter()
                    .placeholder(R.drawable.ic_image)
                    .into(binding.newsImageIv)
            }
        }


        if (itemModel.title.isNotEmpty())
            binding.newsTitleTv.text = itemModel.title
        else
            binding.newsTitleTv.text = "No title"
        binding.newsDateTv.text = itemModel.date

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