package com.gmpire.guruklub.view.activity.library

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.databinding.ItemPreviousQuestionsBatchItemBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseViewHolder

class BatchViewHolder(itemView: ViewDataBinding, context: Context) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemPreviousQuestionsBatchItemBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as Common

        binding.batchNameTv.text = "Exam Year : "+ itemModel.exam_year
        binding.batchTitle.text = itemModel.name
        binding.progress.setProgress(itemModel.total_views_percentage.toInt())
        binding.totalCount.text = itemModel.total_views_percentage + "%  of users have read the question "

        binding.root.setOnClickListener {
            listener.callBack(position, itemModel, binding.root)
        }


        val drawableTop = binding.roundedRectangle.background as GradientDrawable
        var color = ColorUtil.getColorByPosition(position)
        drawableTop.setColor(ContextCompat.getColor(mContext, color))

    }

     override fun  onBind(position: Int, mCallback: IAdapterListener) {

     }

     override fun onLastPosition() {
       // binding.view.visibility = View.GONE
    }
}