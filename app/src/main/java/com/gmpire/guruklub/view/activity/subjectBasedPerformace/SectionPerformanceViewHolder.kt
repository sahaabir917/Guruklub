package com.gmpire.guruklub.view.activity.subjectBasedPerformace

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.game.SectionPerformance
import com.gmpire.guruklub.data.model.game.TopicPerformance
import com.gmpire.guruklub.databinding.ItemSectionPerformanceBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import java.text.SimpleDateFormat
import java.util.*


class SectionPerformanceViewHolder(itemVIew: ViewDataBinding, context: Context) :
        BaseViewHolder(itemVIew.root) {


    var mContext: Context = context
    var binding = itemVIew as ItemSectionPerformanceBinding

    override fun <T> onBind(position: Int, model: T, mCallback: IAdapterListener) {
        model as SectionPerformance
        binding.nameTv.text = model.section_name
        binding.successRateTv.text = "${model.section_percent.toInt()}(${model.correct_ans_by_section}/${model.question_per_section})"
        binding.timeTv.text = SimpleDateFormat("mm:ss").format(Date(model.section_game_time.toLong()))


        binding.topicPerformanceRecview.layoutManager = LinearLayoutManager(mContext)
        binding.topicPerformanceRecview.setHasFixedSize(true)
        binding.topicPerformanceRecview.adapter =
                BaseRecyclerAdapter(mContext, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {

                    }

                    override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                        return TopicPerformanceViewHolder(
                                DataBindingUtil.inflate(
                                        LayoutInflater.from(
                                                mContext
                                        ), R.layout.item_topic_performance, parent, false
                                ), mContext
                        )
                    }

                    override fun loadMoreItem() {
                    }

                }, model.topic_performance as ArrayList<TopicPerformance>)


        binding.root.setOnClickListener {
            if (binding.topicPerformanceRecview.isVisible) {
                binding.topicPerformanceRecview.visibility = View.GONE
                binding.view7.visibility = View.GONE
//                binding.ivArrow.rotation = 0f
                binding.ivArrow.setImageResource(R.drawable.ic_right_arrow4_png)
            } else {
                binding.topicPerformanceRecview.visibility = View.VISIBLE
                binding.view7.visibility = View.VISIBLE
//                binding.ivArrow.rotation = 90f
                binding.ivArrow.setImageResource(R.drawable.ic_down_arrow_blue_small)

            }

        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }


}