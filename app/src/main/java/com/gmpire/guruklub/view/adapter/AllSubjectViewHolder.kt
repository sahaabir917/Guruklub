package com.gmpire.guruklub.view.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.SubjectSectionTopicModel
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.library.MostPopularAndRecentLearning
import com.gmpire.guruklub.databinding.ItemRecentlyLearnBinding
import com.gmpire.guruklub.databinding.ItemSubjectTopicListBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.view.base.BaseViewHolder

class AllSubjectViewHolder(itemView: ViewDataBinding, context: Context?) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemSubjectTopicListBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as SubjectSectionTopicModel
        binding.tvValue.text = "${itemModel.name}"

//        binding.relativeLayoutIcon.visibility = View.VISIBLE
        binding.root.setOnClickListener {
            listener.callBack(position, itemModel, binding.root)
        }


        val drawableTop = binding.roundedRectangle.background as GradientDrawable
        var color = ColorUtil.getColorByPosition(position)
        drawableTop.setColor(ContextCompat.getColor(mContext!!, color))

//        var color = ColorUtil.getColorForSubject(position)
//        binding.cardViewPopular.setCardBackgroundColor(
//            ContextCompat.getColor(
//                mContext!!,
//               color
//            )
//        )
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {
    }
}