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
import com.gmpire.guruklub.data.model.game.SubjectBasedPerformanceResponse
import com.gmpire.guruklub.databinding.ItemSubjectPerformanceBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class SubjectPerformanceViewHolder(itemVIew: ViewDataBinding, context: Context) :
    BaseViewHolder(itemVIew.root) {

    var mContext: Context = context
    var binding = itemVIew as ItemSubjectPerformanceBinding

    override fun <T> onBind(position: Int, model: T, mCallback: IAdapterListener) {
        model as SubjectBasedPerformanceResponse
        binding.nameTv.text = model.subject_name
        binding.successRateTv.text = "${model.subject_percent.toInt()}(${model.correct_ans_by_subject}/${model.question_per_subject})"
        binding.timeTv.text = SimpleDateFormat("mm:ss").format(Date(model.subject_game_time.toLong()))


        binding.sectionPerformanceRecview.layoutManager = LinearLayoutManager(mContext)
        binding.sectionPerformanceRecview.setHasFixedSize(true)

        binding.sectionPerformanceRecview.adapter =
            BaseRecyclerAdapter(mContext, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return SectionPerformanceViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                mContext
                            ), R.layout.item_section_performance, parent, false
                        ), mContext
                    )
                }

                override fun loadMoreItem() {
                }

            }, model.section_performance as ArrayList<SectionPerformance>)

        binding.root.setOnClickListener {
            if(binding.sectionPerformanceRecview.isVisible){
                binding.sectionPerformanceRecview.visibility = View.GONE
                binding.view9.visibility = View.GONE
                binding.ivArrow.rotation = 0f
            }else{
                binding.sectionPerformanceRecview.visibility = View.VISIBLE
                binding.view9.visibility = View.VISIBLE
                binding.ivArrow.rotation = 90f
            }

        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}