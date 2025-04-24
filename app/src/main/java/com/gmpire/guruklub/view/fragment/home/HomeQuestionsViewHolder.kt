package com.gmpire.guruklub.view.fragment.home

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.view.BottomSheet.AnswerDescriptionBottomSheet
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.bumptech.glide.Glide
import com.gmpire.guruklub.databinding.ItemQuestionLayoutHomeNewBinding
import io.github.kexanie.library.MathView

class HomeQuestionsViewHolder(itemVIew: ViewDataBinding, context: Context) :
    BaseViewHolder(itemVIew.root) {
    private lateinit var answerDescriptionBottomSheet: AnswerDescriptionBottomSheet
    var mContext: Context = context
    var binding = itemVIew as ItemQuestionLayoutHomeNewBinding
    override fun <T> onBind(position: Int, model: T, mCallback: IAdapterListener) {
        model as Question

        if (model.is_math == 0) {
            binding.questionTitleTv.text = model.title
        } else {
            binding.questionTitleTvMath.text = model.title
        }

        if (!model.picture.isNullOrEmpty()) {
            Glide.with(mContext).load("http://demo.gmpire.com/" + model.picture)
                .into(binding.questionImageIv)
            binding.questionImageIv.visibility = View.VISIBLE

            binding.questionImageIv.setOnClickListener {
                mCallback.callBack(position, model, binding.questionImageIv)
            }
        } else {
            binding.questionImageIv.visibility = View.GONE
        }

        var i = 0

        //   binding.oprionLl.clearCheck()
        binding.oprionLl.removeAllViews()
        model.options.forEach {
            var view: View
            if (model.is_math == 0) {
                view = LayoutInflater.from(mContext).inflate(R.layout.radio_btn_layout, null)
                var textView = view.findViewById<TextView>(R.id.radio_btn_text)
                view.tag = model.options.indexOf(it)
                textView.text = it

            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.radio_btn_layout_math, null)
                var mathview = view.findViewById<MathView>(R.id.radio_btn_text)
                view.tag = model.options.indexOf(it)
                mathview.text = it
            }


            var radioBtn = view.findViewById<RadioButton>(R.id.radio_btn)
            radioBtn.tag = it
            radioBtn.id = i

            radioBtn.setOnClickListener {

                for (inc in 0..model.options.size - 1) {
                    binding.oprionLl.getChildAt(inc)
                        .findViewWithTag<RadioButton>(model.options[inc]).isChecked = false
                    model.answered_position = -1
                }

                radioBtn.isChecked = true

                model.answered_position = view.tag.toString().toInt()
            }

            view.setOnClickListener {

                for (inc in 0..model.options.size - 1) {
                    binding.oprionLl.getChildAt(inc)
                        .findViewWithTag<RadioButton>(model.options[inc]).isChecked = false
                    model.answered_position = -1
                }

                radioBtn.isChecked = true

                model.answered_position = view.tag.toString().toInt()

            }

            binding.oprionLl.addView(view)
            i++
        }


        binding.submitTv.setOnClickListener {
            if (model.answered_position != -1) {
                mCallback.callBack(position, model, binding.submitTv)
            } else {
                Toast.makeText(mContext, "choose an option", Toast.LENGTH_SHORT).show()
            }
        }


//        binding.playLayout.setOnClickListener {
//            mCallback.callBack(
//                position,
//                model,
//                binding.playLayout
//            )
//        }

//        binding.btnAddQuestion.setOnClickListener {
//            mCallback.callBack(position, model, binding.btnAddQuestion)
//        }

        binding.optionsIv.setOnClickListener {
            val layoutInflater =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = layoutInflater?.inflate(R.layout.item_popup_menu, null)

            val filterLayout = popupView?.findViewById<LinearLayout>(R.id.filter_menu_ll)
            val bookmarkLayout = popupView?.findViewById<LinearLayout>(R.id.bookmark_menu_ll)
            val shareLayout = popupView?.findViewById<LinearLayout>(R.id.share_menu_ll)
            val iv_bookmark = popupView?.findViewById<ImageView>(R.id.iv_bookmark)


            if (model.is_bookmarked == 1) {
                iv_bookmark?.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.red));
            } else {
                iv_bookmark?.imageTintList = null
            }

            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            popupWindow.setBackgroundDrawable(BitmapDrawable())
            popupWindow
            popupWindow.isOutsideTouchable = true
            popupWindow.setOnDismissListener {
            }
            popupWindow.showAsDropDown(binding.optionsIv)
//            popupWindow.isFocusable = true
//            popupWindow.animationStyle = R.style.DialogStyle1
            filterLayout?.setOnClickListener {
                mCallback.callBack(position, model, filterLayout)
                popupWindow.dismiss()
            }
            bookmarkLayout?.setOnClickListener {
                mCallback.callBack(position, model, bookmarkLayout)
                popupWindow.dismiss()
            }
            shareLayout?.setOnClickListener {
                mCallback.callBack(position, model, shareLayout)
                popupWindow.dismiss()
            }
        }
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}