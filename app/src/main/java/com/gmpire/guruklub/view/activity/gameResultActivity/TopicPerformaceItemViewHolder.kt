package com.gmpire.guruklub.view.activity.gameResultActivity

import android.content.Context
import android.content.Intent
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.data.model.game.TopicBasedPerformance
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.databinding.ItemTopicBasedPerformanceShortBinding
import com.gmpire.guruklub.view.activity.library.LibraryActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*


class TopicPerformaceItemViewHolder(itemVIew: ViewDataBinding, context: Context) :
    BaseViewHolder(itemVIew.root) {

    var mContext: Context = context
    var binding = itemVIew as ItemTopicBasedPerformanceShortBinding
    private lateinit var filterValues: FilterValues


    override fun <T> onBind(position: Int, model: T, mCallback: IAdapterListener) {
        model as TopicBasedPerformance
        binding.subjectNameTv.text = model.topic_name
        binding.successRateTv.text = "${model.topic_percent.toInt()}(${model.correct_ans_by_topic}/${model.question_per_topic})"
        binding.timeTv.text = SimpleDateFormat("mm:ss").format(Date(model.topic_game_time.toLong()))

        binding.goToTopicTv.setOnClickListener {
            filterValues = FilterValues()
            filterValues.category_id = (mContext as GameResultActivity).dataManager.mPref.prefGetUserInfo().category_id.toString()
            filterValues.subject_id = model.subject_id
            filterValues.section_id = model.section_id
            filterValues.topic_id = model.topic_id
            mContext.startActivity(Intent(mContext as GameResultActivity,LibraryActivity::class.java).putExtra("filterValues",
                Gson().toJson(filterValues)).putExtra("subject_name",model.topic_name))
        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}