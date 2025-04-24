package com.gmpire.guruklub.view.BottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.answer_description_bottomsheet.view.*
import kotlinx.android.synthetic.main.item_question_details.view.*
import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern


class AnswerDescriptionBottomSheet(
    val question: Question,
    val ans_status: Boolean,
    val listener: Listener
) : BottomSheetDialogFragment() {

    private lateinit var contentView: View

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        contentView = View.inflate(context, R.layout.answer_description_bottomsheet, null)
        dialog.setContentView(contentView)

        dialog.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val scrollView = d.findViewById(R.id.descriptionScrollView) as NestedScrollView?
            val bsb = BottomSheetBehavior.from(bottomSheet)
            bsb.state = BottomSheetBehavior.STATE_EXPANDED

            scrollView?.viewTreeObserver
                ?.addOnScrollChangedListener {
                    bsb.isHideable = scrollView.scrollY == 0
                }

        }

        if(!question.batches.isNullOrEmpty())
            initRecyclerView(contentView.recyclerView, question.batches)

        if (ans_status) {

            val drawableTop = contentView.post_container_layout.background as GradientDrawable
            var color = ColorUtil.getColorForRightWrong("Right")
            drawableTop.setColor(ContextCompat.getColor(context!!, color))

            contentView.answer_status_tv.text = "Correct Answer"
            context?.let {
                ContextCompat.getColor(
                    it,
                    R.color.white
                )
            }?.let {
                contentView.answer_status_tv.setTextColor(
                    it
                )
            }

        } else {
            contentView.answer_status_tv.text = "Wrong Answer"

            val drawableTop = contentView.post_container_layout.background as GradientDrawable
            var color = ColorUtil.getColorForRightWrong("Wrong")
            Log.d("color", color.toString())
            drawableTop.setColor(ContextCompat.getColor(context!!, color))

            context?.let {
                ContextCompat.getColor(
                    it,
                    R.color.white
                )
            }?.let {
                contentView.answer_status_tv.setTextColor(
                    it
                )
            }
        }

        if (question.answer > 3) {
            contentView.answer_status_tv.text = "No Answer"
            context?.let {
                ContextCompat.getColor(
                    it,
                    android.R.color.darker_gray
                )
            }?.let {
                contentView.answer_status_tv.setTextColor(
                    it
                )
            }
        }

        if (question.is_math == 0 && question.has_image == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contentView.question_title_tv.text =
                    Html.fromHtml(question.title, Html.FROM_HTML_MODE_COMPACT)
                if (question.answer <= 3) {
                    contentView.question_answer_tv.text = Html.fromHtml(
                        "<b><font color=black>Answer: </font></b> <font color =black> ${question.options[question.answer]} </font>",
                        Html.FROM_HTML_MODE_COMPACT
                    )
                } else {
                    contentView.question_answer_tv.text = Html.fromHtml(
                        "<<b><font color=black>Correct Answer: </font></b> <font color =black> No correct answer. </font>",
                        Html.FROM_HTML_MODE_COMPACT
                    )
                }
            } else {
                contentView.question_title_tv.text = Html.fromHtml(question.title)
                if (question.answer <= 3) {
                    contentView.question_answer_tv.text =
                        Html.fromHtml("<b><font color=black>Correct Answer: </font></b> <font color =black> ${question.options[question.answer]} </font>")
                } else {
                    Html.fromHtml("<b><font color=black>Correct Answer: </font></b> <font color =black> No correct answer. </font>")
                }
            }

        } else {
            contentView.question_title_tv_math.text = "প্রশ্ন: " + question.title
            if (question.answer <= 3) {
                contentView.question_answer_tv_math.text =
                    "<b><font color=black>Correct Answer: </font></b> ${question.options[question.answer]}</font>"
            } else {
                contentView.question_answer_tv_math.text =
                    Html.fromHtml("<b><font color=black>Correct Answer: No correct answer.</font></b>")
                        .toString()
            }
        }

        // Explanation
        if (question.answer_explain.isEmpty()) {
            contentView.question_description_tv.text =
                "Sorry!! No explanation found for this question"
        } else {
            var head =
                "<head><style type=\"text/css\">" +
                        "@font-face {font-family: 'Times New Roman';src: url('file:///android_asset/fonts/times_new_roman.ttf');}" +
                        "body {font-family: 'Times New Roman';}" +
                        "</style></head>"
            var text = "<html>$head<body style=\"text-align:justify\">"

            val p: Pattern = Pattern.compile("background-color:.*?\"", Pattern.CASE_INSENSITIVE)
            val m: Matcher = p.matcher(question.answer_explain)
            val result: String = m.replaceAll("background-color:transparent\"")
            text += result

            text += "</body></html>"
            val settings = contentView.question_description_tv_math.settings
            settings.defaultTextEncodingName = "utf-8"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (question.is_math == 0) {
                    contentView.question_description_tv_math.loadDataWithBaseURL(
                        "file:///android_asset/",
                        text,
                        "text/html; charset=utf-8",
                        "utf-8", null
                    )
                } else {
                    contentView.question_description_tv_math.text = question.answer_explain
                }
            } else {
                if (question.is_math == 0) {
                    contentView.question_description_tv_math.loadDataWithBaseURL(
                        "file:///android_asset/",
                        text,
                        "text/html; charset=utf-8",
                        "utf-8", null
                    )
                } else {
                    contentView.question_description_tv_math.text = question.answer_explain
                }
            }
        }

        contentView.close_btn_iv1.setOnClickListener {
            dismiss()
        }

        contentView.ok_tv.setOnClickListener {
            listener.onOkClicked()
        }

        contentView.allVideoBtn.setOnClickListener{
            listener.onVideoBtnClicked(question)
        }

    }

    private fun initRecyclerView(recyclerView: RecyclerView?, batches: ArrayList<String>) {
        recyclerView?.adapter =
            BaseRecyclerAdapter(context, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }
                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return FilterValueViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context)
                            , R.layout.item_filter_selected
                            , parent, false
                        ), context!!)
                }
                override fun loadMoreItem() {
                }
            }, batches)
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
//        val layoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = layoutManager
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener.onDismissed()
    }


    interface Listener {
        fun onOkClicked()
        fun onDismissed()
        fun onVideoBtnClicked(question: Question)
    }

}