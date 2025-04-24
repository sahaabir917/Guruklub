package com.gmpire.guruklub.view.activity.viewSolutions

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ItemViewSolutionsQuestionBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.util.SubscriptUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ViewSolutionQuestionsViewHolder(
    itemView: ViewDataBinding,
    context: Context?,
    fragmentName: String
) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemViewSolutionsQuestionBinding
    var mContext = context
    var fragmentName = fragmentName
    var positionAsSerial = 0

    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as Question
        //Clear views
        Log.d("answertype", itemModel.toString())
        binding.tvTitle.text = ""
        binding.tvAnswer.text = ""

        positionAsSerial = if (position > 5)
            position - 1
        else
            position


        if (fragmentName == "Error") {
            binding.rightIcon2.visibility = View.VISIBLE
            binding.tvAnswer.visibility = View.VISIBLE
            binding.wrongAnswerView.visibility = View.VISIBLE
            binding.wrongAnswerText.visibility = View.VISIBLE
            binding.tvAnswer.typeface = Typeface.DEFAULT_BOLD
        } else if (fragmentName == "Bookmark") {
            binding.rightIcon2.visibility = View.GONE
            binding.tvAnswer.visibility = View.VISIBLE
            binding.wrongAnswerView.visibility = View.GONE
            binding.wrongAnswerText.visibility = View.GONE
            binding.viewColorBorder.visibility = View.VISIBLE
            binding.viewColorBorder.setBackgroundColor(
                ContextCompat.getColor(
                    binding.viewColorBorder.context,
                    ColorUtil.getColorByPosition(position)
                )
            )
            binding.tvAnswer.typeface = Typeface.DEFAULT_BOLD
//           set the margin
        } else if (fragmentName == "Search") {
            binding.rightIcon2.visibility = View.GONE
            binding.tvAnswer.visibility = View.VISIBLE
            binding.wrongAnswerView.visibility = View.GONE
            binding.wrongAnswerText.visibility = View.GONE
            binding.viewColorBorder.visibility = View.VISIBLE
            binding.viewColorBorder.setBackgroundColor(
                ContextCompat.getColor(
                    binding.viewColorBorder.context,
                    ColorUtil.getColorByPosition(position)
                )
            )
            binding.tvAnswer.typeface = Typeface.DEFAULT_BOLD
        } else {
            binding.rightIcon2.visibility = View.VISIBLE
            binding.tvAnswer.visibility = View.VISIBLE
            binding.wrongAnswerView.visibility = View.VISIBLE
            binding.wrongAnswerText.visibility = View.VISIBLE
            binding.wrongAnswerText.text = ""
            binding.tvAnswer.typeface = Typeface.DEFAULT_BOLD
            if (itemModel.answer_type == "wrong") {
                binding.wrongAnswerText.text = "Wrong"
                binding.wrongAnswerView.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.wrongAnswerView.context,
                        ColorUtil.getRightWrongNoAnswer("Wrong")
                    )
                )
            } else if (itemModel.answer_type == "correct") {
                binding.wrongAnswerText.text = "Correct"
                binding.wrongAnswerView.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.wrongAnswerView.context,
                        ColorUtil.getRightWrongNoAnswer("Right")
                    )
                )
            } else {
                binding.wrongAnswerText.text = "Unanswered"
                binding.wrongAnswerView.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.wrongAnswerView.context,
                        ColorUtil.getRightWrongNoAnswer("No answer")
                    )
                )
            }
        }

        if (Build.VERSION.SDK_INT < 18) {
            binding.tvTitleMath.clearView()
            binding.tvAnswerMath.clearView()
        } else {
            binding.tvTitleMath.loadUrl("javascript:document.open();document.close();")
            binding.tvAnswerMath.loadUrl("javascript:document.open();document.close();")
        }

        if (itemModel.is_math == 0) {
            // Handle title
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

            // Handle answer
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
                }
            } else {
                binding.tvAnswer.text = "Answer: No correct answer"
            }
        } else {
            binding.tvTitleMath.text = "${positionAsSerial + 1}. ${itemModel.title}"
            if (itemModel.answer <= 3) {
                binding.tvAnswerMath.text =
                    "<b> Answer: ${itemModel.options[itemModel.answer]} </b>"
            } else {
                binding.tvAnswerMath.text = "<b>Answer: No correct answer</b>"
            }
        }


        if (itemModel.is_bookmarked == 1) {
            binding.bookmarkIv.setImageDrawable(
                mContext?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.ic_bookmarked_png
                    )
                }
            )
        } else {
            binding.bookmarkIv.setImageDrawable(
                mContext?.let {
                    ContextCompat.getDrawable(
                        it,
                        R.drawable.ic_bookmark_white
                    )
                }
            )
        }


        if (itemModel.answer_type != null) {
            binding.statusIv.visibility = View.GONE
            when (itemModel.answer_type) {
                "noanswer" -> {
                    binding.statusIv.setImageDrawable(
                        mContext?.let {
                            ContextCompat.getDrawable(
                                it,
                                R.drawable.ic_unanswered
                            )
                        }
                    )
                }
                "correct" -> {
                    binding.statusIv.setImageDrawable(
                        mContext?.let {
                            ContextCompat.getDrawable(
                                it,
                                R.drawable.ic_correct_answer
                            )
                        }
                    )
                }
                "wrong" -> {
                    binding.statusIv.setImageDrawable(
                        mContext?.let {
                            ContextCompat.getDrawable(
                                it,
                                R.drawable.ic_wrong_answer
                            )
                        }
                    )
                }
            }
        } else {
            binding.statusIv.visibility = View.GONE
        }

        binding.questionRootLayout.setOnClickListener {
            listener.callBack(position, itemModel, binding.questionRootLayout)
        }

        binding.bookmarkIv.setOnClickListener {
            try {
                listener.callBack(position, itemModel, binding.bookmarkIv)
            } catch (ex: IndexOutOfBoundsException) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

    override fun onLastPosition() {
        // binding.view.visibility = View.GONE
    }
}