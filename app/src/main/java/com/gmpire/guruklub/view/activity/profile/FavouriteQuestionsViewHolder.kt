package com.gmpire.guruklub.view.activity.profile

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ItemQuestionLinearBinding
import com.gmpire.guruklub.util.SubscriptUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseViewHolder


class FavouriteQuestionsViewHolder(itemView: ViewDataBinding, context: BaseFragment) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemQuestionLinearBinding
    var mContext = context
    var positionAsSerial = 0

    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as Question

        positionAsSerial = if (position > 5)
            position - 1
        else
            position

        //Clear views
        binding.tvTitle.text = ""
        binding.tvAnswer.text = ""

        if (Build.VERSION.SDK_INT < 18) {
            binding.tvTitleMath.clearView()
            binding.tvAnswerMath.clearView()
        } else {
            binding.tvTitleMath.loadUrl("javascript:document.open();document.close();")
            binding.tvAnswerMath.loadUrl("javascript:document.open();document.close();")
        }

        if (itemModel.is_math == 1) {
            if (itemModel.answer <= 3) {
                binding.tvAnswerMath.text = "Answer: ${itemModel.options[itemModel.answer]}"
            } else {
                binding.tvAnswer.text = "Answer: No correct answer"
            }
            binding.tvTitleMath.text = "${positionAsSerial + 1}. ${itemModel.title}"
        } else {
            // Handle title
            val textTitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(
                    "${positionAsSerial + 1}. ${itemModel.title}",
                    Html.FROM_HTML_MODE_COMPACT
                )
            } else {
                Html.fromHtml("${positionAsSerial + 1}. ${itemModel.title}")
            }
            // check if contains subscripts, also works for superscripts
            if (SubscriptUtil.checkIfContainsSubscript(itemModel.title)) {
                binding.tvTitle.setText(
                    SubscriptUtil.getSubscriptSpan(
                        textTitle
                    ),
                    TextView.BufferType.SPANNABLE
                )
            } else {
                binding.tvTitle.text = textTitle
            }

            // Handle answer
            if (itemModel.answer <= 3) {

                val textAnswer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(
                        "${itemModel.options[itemModel.answer]}",
                        Html.FROM_HTML_MODE_COMPACT
                    )
                } else {
                    Html.fromHtml("${itemModel.options[itemModel.answer]}")
                }

                // check if contains subscripts, also works for superscripts
                if (SubscriptUtil.checkIfContainsSubscript(itemModel.options[itemModel.answer])) {
                    binding.tvAnswer.setText(
                        SubscriptUtil.getSubscriptSpan(
                            TextUtils.concat(
                                "Answer: ",
                                textAnswer
                            ) as Spanned
                        ),
                        TextView.BufferType.SPANNABLE
                    )
                } else {
                    binding.tvAnswer.text = "Answer: $textAnswer"
                }

            } else {
                binding.tvAnswer.text = "Answer: No correct answer"
            }
        }

        binding.bookmarkIv.setImageDrawable(
            mContext.activity?.let {
                ContextCompat.getDrawable(
                    it,
                    R.drawable.ic_bookmarked_round
                )
            }
        )

        binding.questionRootLayout.setOnClickListener {
            listener.callBack(position, itemModel, binding.questionRootLayout)
        }

        binding.bookmarkIv.setOnClickListener {
            listener.callBack(position, itemModel, binding.bookmarkIv)
        }
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {
    }

    override fun onLastPosition() {
        // binding.view.visibility = View.GONE
    }
}