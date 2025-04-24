package com.gmpire.guruklub.view.activity.categoryAndSubjectSelection

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.databinding.ItemMultiselectionRecviewBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.answer_description_bottomsheet.view.*

class MultiselectionItemViewHolder(itemView: ViewDataBinding, context: Context, title :String) :
    BaseViewHolder(itemView.root) {
    var binding = itemView as ItemMultiselectionRecviewBinding
    var mContext: Context = context
    var title = title
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {
        itemModel as BaseItem

        binding.itemName.text = itemModel.name

        binding.itemName.isChecked = itemModel.isSelected

        binding.itemName.setOnClickListener {
            listener.callBack(position, itemModel, binding.itemName)
        }



        Log.d("title",title.toString())
        if(title.contains("Section")){

//            val drawableTop = binding.itemName.background as GradientDrawable
//            var color = ColorUtil.getColorForFilter()
//            drawableTop.setColor(ContextCompat.getColor(mContext, color))
//        binding.itemName.background = R.drawable.bg_rounded_checkbox_blue
//            val value = TypedValue()
//            mContext.theme.resolveAttribute(R.drawable.bg_rounded_checkbox_blue, value, true)
//            binding.itemName.setBackgroundResource(value.resourceId)
//            binding.itemName.setFocusable(true)
        }
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}