package com.gmpire.guruklub.view.activity.modelTestActivity

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.modelTest.ModelTestQuestion
import com.bumptech.glide.Glide
import io.github.kexanie.library.MathView
import kotlinx.android.synthetic.main.item_game_question.view.*
import java.lang.Exception
import java.lang.NumberFormatException

class ModelTestQuestionAdapter(
    private val mContext: Context,
    private val questions: List<ModelTestQuestion>,
    val actionListener: OnActionListener
) : PagerAdapter() {

    override fun getPageTitle(position: Int): CharSequence? {
        return (position + 1).toString()
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext).inflate(
            R.layout.item_game_question2,
            collection,
            false
        ) as ViewGroup

        // clear views
        layout.question_title_tv.text = ""
        if (Build.VERSION.SDK_INT < 18) {
            layout.question_title_tv_math.clearView()
        } else {
            layout.question_title_tv_math.loadUrl("javascript:document.open();document.close();")
        }

        if (questions[position].is_math == "0" && questions[position].has_image == "0") {
            layout.question_title_tv.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(questions[position].title, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(questions[position].title)
            }
        } else {
            layout.question_title_tv_math.text =
                "<b><font color='white'>${questions[position].title}</font></b>"
        }


        if (questions[position].picture != null) {
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
            if (questions[position].is_math == "0" && questions[position].has_image == "0") {
                view = LayoutInflater.from(mContext).inflate(R.layout.radio_btn_layout, null)

                var radioButton = view.findViewById<RadioButton>(R.id.radio_btn)
                radioButton.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    Html.fromHtml(it)
                }
                //textView.text = it
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
                    questions[position].answered_position = "-1"
                }

                radioBtn.isChecked = true

                questions[position].answered_position = view.tag.toString()
            }


            view.setOnClickListener {
                for (inc in 0..questions[position].options.size - 1) {
                    layout.oprion_ll.getChildAt(inc)
                        .findViewWithTag<RadioButton>(questions[position].options[inc]).isChecked =
                        false
                    questions[position].answered_position = "-1"
                }
                radioBtn.isChecked = true
                questions[position].answered_position = view.tag.toString()
            }

            if (questions[position].answered) {
                if (questions[position].answered_position != null && questions[position].answered_position != "null") {
                    try {
                        if (radioBtn.id == questions[position].answered_position?.toInt()) {
                            radioBtn.isChecked = true
                        }
                    } catch (ex: NumberFormatException) {
                        ex.printStackTrace()
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }

                }
                layout.submit_tv.isEnabled = false
                radioBtn.isEnabled = false
                view.isEnabled = false
            }

            layout.oprion_ll.addView(view)
            i++
        }



        layout.submit_tv.setOnClickListener {
            if (questions[position].answered_position != null && questions[position].answered_position != "-1") {
                actionListener.onSubmitAnswer(
                    position,
                    questions[position].answered_position.toString()
                )
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
        fun onSubmitAnswer(position: Int, answered_position: String)
        fun onImageClicked(position: Int)
        fun loadMore()
    }
}
