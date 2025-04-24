package com.gmpire.guruklub.view.activity.subjectBasedPerformace

import android.content.Context
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.game.TopicPerformance
import com.gmpire.guruklub.databinding.*
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import java.text.SimpleDateFormat
import java.util.*

class TopicPerformanceViewHolder(itemVIew: ViewDataBinding, context: Context) :
    BaseViewHolder(itemVIew.root) {

    var mContext: Context = context
    var binding = itemVIew as ItemTopicPerformanceBinding

    override fun <T> onBind(position: Int, model: T, mCallback: IAdapterListener) {
        model as TopicPerformance
        binding.nameTv.text = model.topic_name
        binding.successRateTv.text = "${model.topic_percent.toInt()}(${model.correct_ans_by_topic}/${model.question_per_topic})"
        binding.timeTv.text = SimpleDateFormat("mm:ss").format(Date(model.topic_game_time.toLong()))

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}