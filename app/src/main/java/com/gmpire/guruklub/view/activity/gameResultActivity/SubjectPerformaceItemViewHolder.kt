package com.gmpire.guruklub.view.activity.gameResultActivity

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.game.SubjectBasedPerformance
import com.gmpire.guruklub.databinding.ItemSubjectBasedPerformanceShortBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import java.text.SimpleDateFormat
import java.util.*


class SubjectPerformaceItemViewHolder(itemVIew: ViewDataBinding, context: Context) :
    BaseViewHolder(itemVIew.root) {

    var mContext: Context = context
    var binding = itemVIew as ItemSubjectBasedPerformanceShortBinding

    override fun <T> onBind(position: Int, model: T, mCallback: IAdapterListener) {
        model as SubjectBasedPerformance
        binding.subjectNameTv.text = model.subject_name
        binding.successRateTv.text = "${model.subject_percent.toInt()}"
        binding.timeTv.text = SimpleDateFormat("mm:ss").format(Date(model.subject_game_time.toLong()))

        if(position % 2 ==0){
            val drawableTop = binding.linearlayout18.background as GradientDrawable
            var color = ColorUtil.getColorForColumn("1st")
            drawableTop.setColor(ContextCompat.getColor(mContext, color))
        }else if(position % 2 ==1){
            val drawableTop = binding.linearlayout18.background as GradientDrawable
            var color = ColorUtil.getColorForColumn("2nd")
            drawableTop.setColor(ContextCompat.getColor(mContext, color))
        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}