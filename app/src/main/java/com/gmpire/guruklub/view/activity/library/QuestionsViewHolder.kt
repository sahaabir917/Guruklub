package com.gmpire.guruklub.view.activity.library

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ItemQuestionLinearFlatBinding
import com.gmpire.guruklub.util.SubscriptUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseViewHolder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class QuestionsViewHolder(itemView: ViewDataBinding, context: BaseFragment) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemQuestionLinearFlatBinding
    var mContext = context
    var positionAsSerial = 0
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {
        itemModel as Question
        //Clear views
        binding.tvTitle.text = ""
        binding.tvAnswer.text = ""

        positionAsSerial = if (position > 5)
            position - 1
        else
            position

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
                binding.tvAnswerMath.text = "Answer: No correct answer"
            }
            binding.tvTitleMath.text = "${positionAsSerial + 1}. ${itemModel.title}"
        } else {
            if (itemModel.answer <= 3) {
                if (itemModel.options.isNotEmpty()) {
                    val document: Document = Jsoup.parse(itemModel.options[itemModel.answer])
                    document.select("img").remove()
                    val answer = document.toString()

                    var textAnswer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(
                            answer,
                            Html.FROM_HTML_MODE_COMPACT
                        )
                    } else {
                        Html.fromHtml(answer)
                    }

                    textAnswer = TextUtils.concat("Answer: ", textAnswer) as Spanned

                    // check if contains subscripts
                    if (SubscriptUtil.checkIfContainsSubscript(itemModel.options[itemModel.answer])) {
                        binding.tvAnswer.setText(
                            SubscriptUtil.getSubscriptSpan(textAnswer),
                            TextView.BufferType.SPANNABLE
                        )
                    } else {
                        binding.tvAnswer.text = textAnswer
                    }

                    binding.tvAnswer.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(
                            "Answer: $answer",
                            Html.FROM_HTML_MODE_COMPACT
                        )
                    } else {
                        Html.fromHtml("Answer: $answer")
                    }
                }
            } else {
                binding.tvAnswer.text = "Answer: No correct answer"
            }

            // Remove image tag for question list
            val document: Document = Jsoup.parse(itemModel.title)
            document.select("img").remove()
            val title = document.toString()

            val textTitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml("${positionAsSerial + 1}. $title", Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml("${positionAsSerial + 1}. $title")
            }

            if (SubscriptUtil.checkIfContainsSubscript(itemModel.title)) {
                binding.tvTitle.setText(
                    SubscriptUtil.getSubscriptSpan(textTitle),
                    TextView.BufferType.SPANNABLE
                )
            } else {
                binding.tvTitle.text = textTitle
            }

        }

        if (itemModel.is_bookmarked == 1) {
            binding.bookmarkIv.setImageDrawable(
                mContext.activity?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.ic_bookmarked_png
                    )
                }
            )
        } else {
            binding.bookmarkIv.setImageDrawable(
                mContext.activity?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.ic_unbookmarked_png
                    )
                }
            )
        }

        val colorVal = position % 4

        when (colorVal) {
            0 -> {
                binding.viewQuestion.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.tvTitle.context,
                        R.color.lightOrange
                    )
                )
            }
            1 -> {
                binding.viewQuestion.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.tvTitle.context,
                        R.color.paleLime
                    )
                )
            }
            2 -> {
                binding.viewQuestion.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.tvTitle.context,
                        R.color.green
                    )
                )
            }
            3 -> {
                binding.viewQuestion.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.tvTitle.context,
                        R.color.indigo
                    )
                )
            }
        }


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