package com.gmpire.guruklub.view.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.util.SubscriptUtil
import com.gmpire.guruklub.view.BottomSheet.FilterValueViewHolder
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import io.github.kexanie.library.MathView
import kotlinx.android.synthetic.main.item_question_details.view.*


class QuestionDetailsAdapter(
    private val mContext: Context,
    private var question: ArrayList<Question>,
    val actionListener: OnItemClickedListener
):PagerAdapter() {
    lateinit var imageSliderLayout: View

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return question.size
    }


    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext).inflate(
            R.layout.item_question_details,
            collection,
            false
        ) as ViewGroup

        imageSliderLayout = layout.findViewById(R.id.options_iv)

        layout.tvTitle.text = ""
        layout.tvOptions.text = ""
        layout.tvAnswer.text = ""
        layout.tvExplanation.text = ""
        layout.option_ll.removeAllViews()


        if (Build.VERSION.SDK_INT < 18) {
            layout.tvTitleMath.clearView()
            layout.tvAnswerMath.clearView()
            layout.tvOptionsMath.clearView()
            layout.tvExplanationMath.clearView()
            layout.webExplanation.clearView()
        } else {
            layout.tvTitleMath.loadUrl("javascript:document.open();document.close();")
            layout.tvAnswerMath.loadUrl("javascript:document.open();document.close();")
            layout.tvOptionsMath.loadUrl("javascript:document.open();document.close();")
            layout.tvExplanationMath.loadUrl("javascript:document.open();document.close();")
            layout.webExplanation.loadUrl("javascript:document.open();document.close();")
        }

        layout.webExplanation.setOnLongClickListener { true }
        layout.webExplanation.isLongClickable = false
        layout.webExplanation.isHapticFeedbackEnabled = false

        layout.tvExplanationMath.setOnLongClickListener { true }
        layout.tvExplanationMath.isLongClickable = false
        layout.tvExplanationMath.isHapticFeedbackEnabled = false

        if(!question[position].batches.isNullOrEmpty())
            initRecyclerView(layout.recyclerViewBatchList, question[position].batches)


        if (question[position].is_math == 0 && question[position].has_image == 0) {
            layout.webExplanation.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                layout.tvTitle.text = Html.fromHtml("${position+1}. ${question[position].title}", Html.FROM_HTML_MODE_COMPACT)
            } else {
                layout.tvTitle.text = Html.fromHtml("${position+1}. ${question[position].title}")
            }
            // Explanation part
            var head =
                "<head><style>" +
                        "@font-face {font-family: 'Times New Roman';src: url('file:///android_asset/fonts/times_new_roman.ttf');}" +
                        "body {font-family: 'Times New Roman';}</style></head>"
            var text = "<html>$head<body style=\"text-align:justify\">"
            text += question[position].answer_explain
            text += "</body></html>"
            val settings = layout.webExplanation.settings
            settings.defaultTextEncodingName = "utf-8"
            layout.webExplanation.loadDataWithBaseURL(
                "file:///android_asset/",
                text,
                "text/html; charset=utf-8",
                "utf-8", null
            )
            if (question[position].answer <= 3) {
                var spannableAnswer: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(question[position].options[question[position].answer], Html.FROM_HTML_MODE_COMPACT)
                } else {
                    Html.fromHtml(question[position].options[question[position].answer])
                }
                if (SubscriptUtil.checkIfContainsSubscript(question[position].options[question[position].answer])) {
                    layout.tvAnswer.setText(
                        SubscriptUtil.getSubscriptSpan(spannableAnswer),
                        TextView.BufferType.SPANNABLE
                    )
                } else {
                    layout.tvAnswer.text = spannableAnswer
                }
            } else {
                layout.tvAnswer.text = "No correct answer"
            }

            layout.tvOptions.text = ""


            layout.option_ll.gravity = Gravity.CENTER


            question[position].options.forEach {
                var view: View
                if (question[position].is_math == 0 && question[position].has_image == 0) {
                    view = LayoutInflater.from(mContext)
                        .inflate(R.layout.layout_option_details, null)
                    var radioButton = view.findViewById<TextView>(R.id.radio_btn_text)
                    val rbText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
                    } else {
                        Html.fromHtml(it)
                    }

                    if (SubscriptUtil.checkIfContainsSubscript(it)) {
                        radioButton.setText(
                            SubscriptUtil.getSubscriptSpan(rbText),
                            TextView.BufferType.SPANNABLE
                        )
                    } else {
                        radioButton.text = rbText
                    }
                } else {
                    view = LayoutInflater.from(mContext)
                        .inflate(R.layout.layout_option_details_math, null)
                    var mathview = view.findViewById<MathView>(R.id.radio_btn_text)
                    mathview.text = it
                }
                layout.option_ll.addView(view)
            }
        } else {

            layout.webExplanation.visibility = View.GONE
            layout.tvExplanationMath.text = question[position].answer_explain
            layout.tvTitleMath.text = "<b><font color='white'>${position + 1}. ${question[position].title}</font></b>"
            if (question[position].answer <= 3)
                layout.tvAnswerMath.text = question[position].options[question[position].answer]
            else
                layout.tvAnswerMath.text = "No correct answer"
            var allOption = ""


            question[position].options.forEach {
                var view = LayoutInflater.from(mContext)
                    .inflate(R.layout.layout_option_details, null)
                var mathView = view.findViewById<MathView>(R.id.math_view)
                var textView = view.findViewById<TextView>(R.id.radio_btn_text)
                textView.visibility = View.GONE
                mathView.visibility = View.VISIBLE
                mathView.text = it
                layout.option_ll.addView(view)
            }    //question 1,2,3 coming from here........
            layout.tvOptionsMath.text = allOption
        }


        if (!question[position].picture.isNullOrEmpty()) {
            question[position].picture?.let {
                layout.ivQuestion.visibility = View.VISIBLE

                Glide.with(mContext).load(BuildConfig.SERVER_URL + question[position].picture)
                    .fitCenter()
                    .centerCrop()
                    .placeholder(R.drawable.ic_image)
                    .into(layout.ivQuestion)
            }
        }

        if (question[position].picture.isNullOrEmpty()) {
            if (layout.ivQuestion.visibility == View.VISIBLE)
                layout.ivQuestion.visibility = View.GONE
        }


        layout.buttonRelatedVideo.setOnClickListener {
            actionListener.onRelatedBtnClicked(position)
        }

        layout.options_iv.setOnClickListener {
            val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? =
            layoutInflater?.inflate(R.layout.item_popup_menu_question_details, null)

            val reportLayout = popupView?.findViewById<LinearLayout>(R.id.report_menu_ll)
            val bookmarkLayout = popupView?.findViewById<LinearLayout>(R.id.bookmark_menu_ll)
            val shareLayout = popupView?.findViewById<LinearLayout>(R.id.share_menu_ll)
            val downloadLayout = popupView?.findViewById<LinearLayout>(R.id.download_menu_ll)
            val iv_bookmark = popupView?.findViewById<ImageView>(R.id.iv_bookmark)
            val tv_bookmark = popupView?.findViewById<TextView>(R.id.bookmark_text_tv)

            if (question[position].is_bookmarked == 1) {
                tv_bookmark?.text = "Bookmarked"
                iv_bookmark?.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.ic_bookmarked_png
                    )
                )
            } else {
                tv_bookmark?.text = "Bookmark"
                iv_bookmark?.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        R.drawable.without_background_unbookmark_png
                    )
                )
            }

            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            popupWindow.setBackgroundDrawable(BitmapDrawable())
            popupWindow.isOutsideTouchable = true
            popupWindow.setOnDismissListener {
            }
            popupWindow.showAsDropDown(layout.options_iv)
            popupWindow.dimBehind()

            bookmarkLayout?.setOnClickListener {
                actionListener.onBookmarkitemClicked(position)
                popupWindow.dismiss()
            }

            shareLayout?.setOnClickListener {
                actionListener.onSharedItemClicked(position)
                popupWindow.dismiss()
            }

            downloadLayout?.setOnClickListener {
                actionListener.onDownloadItemClicked(position)
                popupWindow.dismiss()
            }

            reportLayout?.setOnClickListener {
                actionListener.onReportItemClicked(position,popupWindow)
            }
        }
        collection.addView(layout)
        return layout
    }

    private fun PopupWindow.dimBehind() {
        try {
            val container = contentView.rootView
            val context = contentView.context
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = container.layoutParams as WindowManager.LayoutParams
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            params.dimAmount = 0.5f
            wm.updateViewLayout(container, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initRecyclerView(recyclerView: RecyclerView?, items: ArrayList<String>) {
        recyclerView?.adapter =
            BaseRecyclerAdapter(mContext, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }
                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return FilterValueViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context)
                            , R.layout.item_filter_selected
                            , parent, false
                        ), mContext)
                }
                override fun loadMoreItem() {
                }
            }, items)
        val layoutManager = FlexboxLayoutManager(mContext)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView!!.layoutManager = layoutManager
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    interface OnItemClickedListener{
        fun onBookmarkitemClicked(position: Int)
        fun onSharedItemClicked(position: Int)
        fun onDownloadItemClicked(position: Int)
        fun onReportItemClicked(position: Int, popupWindow: PopupWindow)
        fun onRelatedBtnClicked(position: Int)

    }

}