package com.gmpire.guruklub.view.BottomSheet

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.infocenter.News
import com.gmpire.guruklub.databinding.ItemFilterSelectedBinding
import com.gmpire.guruklub.databinding.ItemNewsBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_quick_exam.view.*


/**
 * Created by Tahsin Rahman on 4/2/21.
 */


class FilterValueViewHolder(itemView: ViewDataBinding, context: Context) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemFilterSelectedBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {
        itemModel as String
        binding.textViewFilterText.text = itemModel
        val drawableTop = binding.relativeLayoutFilterText.background as GradientDrawable
        var color = ColorUtil.getColorForFilter(position)
        drawableTop.setColor(ContextCompat.getColor(mContext, color))
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {
    }

    override fun onLastPosition() {
    }
}