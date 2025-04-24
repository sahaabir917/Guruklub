package com.gmpire.guruklub.view.activity.profile

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.profile.PerformanceHistory
import com.gmpire.guruklub.databinding.ItemPerformanceHistoryBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.answer_description_bottomsheet.view.*
import java.text.SimpleDateFormat

class PerformanceHistoryViewHolder(itemView: ViewDataBinding, context: Context) :
    BaseViewHolder(itemView.root) {


    private var dateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm a")
    private var dateFormatServer = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    var binding = itemView as ItemPerformanceHistoryBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as PerformanceHistory

        binding.tvTotalQuestion.text = "Total Questions : ${itemModel.total_question}"
        binding.tvAnswered.text = "Questions Answered : ${itemModel.answred_question}"
//        binding.tvCorrectAnswer.text = "Correct Answer : ${itemModel.correct_question}"
        binding.tvPerformanceScore.text = "${itemModel.acquired_point}/${itemModel.total_question}"
        binding.tvPerformanceTitle.text = itemModel.title
        binding.tvPerformanceDate.text =
            dateFormat.format(dateFormatServer.parse(itemModel.game_date))

        if (itemModel.title == "Good Performance") {
            val drawableTop = binding.bgPerformanceHistory.background as GradientDrawable
            var color = ColorUtil.getColorForPerformance("Best")
            Log.d("color", color.toString())
            drawableTop.setColor(ContextCompat.getColor(mContext, color))
        } else if (itemModel.title == "Super Performance") {
            val drawableTop = binding.bgPerformanceHistory.background as GradientDrawable
            var color = ColorUtil.getColorForPerformance("Super")
            Log.d("color", color.toString())
            drawableTop.setColor(ContextCompat.getColor(mContext, color))
        } else if (itemModel.title == "Average Performance") {
            val drawableTop = binding.bgPerformanceHistory.background as GradientDrawable
            var color = ColorUtil.getColorForPerformance("Average")
            Log.d("color", color.toString())
            drawableTop.setColor(ContextCompat.getColor(mContext, color))
        } else if (itemModel.title == "Weak Performance") {
            val drawableTop = binding.bgPerformanceHistory.background as GradientDrawable
            var color = ColorUtil.getColorForPerformance("Weak")
            Log.d("color", color.toString())
            drawableTop.setColor(ContextCompat.getColor(mContext, color))
        }

        binding.root.setOnClickListener {
            listener.callBack(position, itemModel, binding.root)
        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}