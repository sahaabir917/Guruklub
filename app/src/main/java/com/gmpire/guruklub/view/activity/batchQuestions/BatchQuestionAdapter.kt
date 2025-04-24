package com.gmpire.guruklub.view.activity.gameActivity

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.question.Question
import com.bumptech.glide.Glide
import com.gmpire.guruklub.util.SubscriptUtil
import io.github.kexanie.library.MathView
import kotlinx.android.synthetic.main.item_game_question.view.oprion_ll
import kotlinx.android.synthetic.main.item_game_question.view.options_iv
import kotlinx.android.synthetic.main.item_game_question.view.question_image_iv
import kotlinx.android.synthetic.main.item_game_question.view.question_title_tv
import kotlinx.android.synthetic.main.item_game_question.view.question_title_tv_math
import kotlinx.android.synthetic.main.item_game_question.view.submit_tv


class BatchQuestionAdapter(
    private val mContext: Context,
    private val questions: List<Question>,
    val actionListener: OnActionListener
) : PagerAdapter() {

    override fun getPageTitle(position: Int): CharSequence? {
        return (position + 1).toString()
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext).inflate(
            R.layout.item_game_question,
            collection,
            false
        ) as ViewGroup

        layout.options_iv.visibility = View.VISIBLE

        // clear views
        layout.question_title_tv.text = ""
        if (Build.VERSION.SDK_INT < 18) {
            layout.question_title_tv_math.clearView()
        } else {
            layout.question_title_tv_math.loadUrl("javascript:document.open();document.close();")
        }

        /* if (questions[position].is_math == 0) {
             layout.question_title_tv.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                 Html.fromHtml(questions[position].title, Html.FROM_HTML_MODE_COMPACT)
             } else {
                 Html.fromHtml(questions[position].title)
             }
         } else {*/
        layout.question_title_tv_math.text =
            "<b><font color='white'>${questions[position].title}</font></b>"
        // }

        if (!questions[position].picture.isNullOrEmpty()) {
            Glide.with(mContext).load(BuildConfig.SERVER_URL + questions[position].picture)
                .into(layout.question_image_iv)
            layout.question_image_iv.visibility = View.VISIBLE

            layout.question_image_iv.setOnClickListener {
                actionListener.onImageClicked(position)
            }
        } else {
            layout.question_image_iv.visibility = View.GONE
        }

        var i = 0

        layout.oprion_ll.removeAllViews()

        questions[position].options.forEach {
            var view: View
            /*if (questions[position].is_math == 0) {
                view = LayoutInflater.from(mContext).inflate(R.layout.radio_btn_layout, null)
                var radioButton = view.findViewById<RadioButton>(R.id.radio_btn)
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
                //textView.text = it
            } else {*/

            //}


            if (questions[position].is_math == 0 && questions[position].has_image == 0) {
                view = LayoutInflater.from(mContext).inflate(R.layout.radio_btn_layout2, null)
                var options = view.findViewById<TextView>(R.id.radio_btn_text)
                val rbText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    Html.fromHtml(it)
                }

                if (SubscriptUtil.checkIfContainsSubscript(it)) {
                    options.setText(
                        SubscriptUtil.getSubscriptSpan(rbText),
                        TextView.BufferType.SPANNABLE
                    )
                } else {
                    options.text = rbText
                }
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.radio_btn_layout_math, null)
                var mathview = view.findViewById<MathView>(R.id.radio_btn_text)
                mathview.text = it
            }



            view.tag = questions[position].options.indexOf(it)

            var radioBtn = view.findViewById<RadioButton>(R.id.radio_btn)
            radioBtn.tag = it
            radioBtn.id = i

            radioBtn.setOnClickListener {
                for (inc in questions[position].options.indices) {
                    layout.oprion_ll.getChildAt(inc)
                        .findViewWithTag<RadioButton>(questions[position].options[inc]).isChecked =
                        false
                    questions[position].answered_position = -1
                }
                radioBtn.isChecked = true
                questions[position].answered_position = view.tag.toString().toInt()
            }

            view.setOnClickListener {
                for (inc in 0..questions[position].options.size - 1) {
                    layout.oprion_ll.getChildAt(inc)
                        .findViewWithTag<RadioButton>(questions[position].options[inc]).isChecked =
                        false
                    questions[position].answered_position = -1
                }
                radioBtn.isChecked = true
                questions[position].answered_position = view.tag.toString().toInt()
            }

            if (questions[position].answered) {
                if (radioBtn.id == questions[position].answered_position) {
                    radioBtn.isChecked = true
                }
                layout.submit_tv.isEnabled = false
                radioBtn.isEnabled = false
                view.isEnabled = false
            }
            layout.oprion_ll.addView(view)
            i++
        }

        layout.options_iv.setOnClickListener {
            val layoutInflater =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? =
                layoutInflater?.inflate(R.layout.item_popup_menu_question_details, null)

            val filterLayout = popupView?.findViewById<LinearLayout>(R.id.filter_menu_ll)
            val bookmarkLayout = popupView?.findViewById<LinearLayout>(R.id.bookmark_menu_ll)
            val shareLayout = popupView?.findViewById<LinearLayout>(R.id.share_menu_ll)
            val downloadLayout = popupView?.findViewById<LinearLayout>(R.id.download_menu_ll)
            val iv_bookmark = popupView?.findViewById<ImageView>(R.id.iv_bookmark)
            val tv_bookmark = popupView?.findViewById<TextView>(R.id.bookmark_text_tv)
            val reportLayout = popupView?.findViewById<LinearLayout>(R.id.report_menu_ll)

            if (questions[position].is_bookmarked == 1) {
                tv_bookmark?.text = "Bookmarked"
                iv_bookmark?.setImageDrawable(
                    mContext.let { it1 ->
                        ContextCompat.getDrawable(
                            it1,
                            R.drawable.ic_bookmarked_png
                        )
                    }
                )
            } else {
                tv_bookmark?.text = "Bookmark"
                iv_bookmark?.setImageDrawable(
                    mContext.let { it1 ->
                        ContextCompat.getDrawable(
                            it1,
                            R.drawable.without_background_unbookmark_png
                        )
                    }
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

            filterLayout?.setOnClickListener {
                popupWindow.dismiss()
            }
            bookmarkLayout?.setOnClickListener {
                actionListener.onBookmarkClicked(position)
                popupWindow.dismiss()
            }
            shareLayout?.setOnClickListener {
                actionListener.onShareClicked(position)
                popupWindow.dismiss()
            }
            downloadLayout?.setOnClickListener {
                actionListener.onDownloadClicked(position)
                popupWindow.dismiss()
            }

            reportLayout?.setOnClickListener {
                actionListener.onReportProblemClicked(position)
            }
        }

        layout.submit_tv.setOnClickListener {
            if (questions[position].answered_position != -1) {
                actionListener.onSubmitAnswer(position, questions[position].answered_position)
            } else {
                Toast.makeText(mContext, "choose an option", Toast.LENGTH_SHORT).show()
            }
        }

        if (position == questions.size - 2) {
            actionListener.loadMore()
        }

        collection.addView(layout)
        return layout
    }


    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return questions.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    interface OnActionListener {
        fun onSubmitAnswer(position: Int, answered_position: Int)
        fun onImageClicked(position: Int)
        fun loadMore()
        fun onReportProblemClicked(position: Int)
        fun onShareClicked(position: Int)
        fun onDownloadClicked(position: Int)
        fun onBookmarkClicked(position: Int)
    }
}