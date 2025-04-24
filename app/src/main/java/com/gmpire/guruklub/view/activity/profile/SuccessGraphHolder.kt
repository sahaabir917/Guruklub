package com.gmpire.guruklub.view.activity.profile


/**
 * Created by Tahsin Rahman on 25/1/21.
 */


import android.R
import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.profile.performance.SubjectWisePerformance
import com.gmpire.guruklub.databinding.ItemSuccessGraphBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.util.DisplayUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder


class SuccessGraphHolder(itemView: ViewDataBinding, context: Context?) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemSuccessGraphBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {


        itemModel as SubjectWisePerformance

        val percentage = itemModel.performance_percent ?: 0.0
        val correct = itemModel.correct_answer ?: "0"
        val total = itemModel.total_question ?: "0"

        binding.progressBarSubject.progress = percentage.toInt()
        binding.textViewSubjectName.text = itemModel.name
        binding.textViewPercentageValue.text =
            "$percentage ($correct/$total)";

        var color = ColorUtil.getColorSuccessGraph(position)

        val layerDrawable = binding.progressBarSubject.progressDrawable as LayerDrawable
        val backgeround = layerDrawable.findDrawableByLayerId(R.id.background) as GradientDrawable
        backgeround.setStroke(
            DisplayUtil.dpToPx(2, mContext!!),
            ContextCompat.getColor(mContext!!, color)
        )

        val progressLayout =
            layerDrawable.findDrawableByLayerId(R.id.progress) as ClipDrawable
        progressLayout.setTint(ContextCompat.getColor(mContext!!, color))
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}