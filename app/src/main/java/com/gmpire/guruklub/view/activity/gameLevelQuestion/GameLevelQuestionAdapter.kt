package com.gmpire.guruklub.view.activity.gameLevelQuestion


/**
 * Created by Tahsin Rahman on 14/3/21.
 */


import android.os.Build
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.gamequestions.GameQuestionSet
import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import com.gmpire.guruklub.util.SubscriptUtil
import com.gmpire.guruklub.view.base.BaseActivity
import io.github.kexanie.library.MathView
import kotlinx.android.synthetic.main.item_game_question.view.*
import kotlinx.android.synthetic.main.item_question_layout_game_level.view.*
import kotlin.math.abs


class GameLevelQuestionAdapter(
    private val mContext: BaseActivity,
    private val questions: List<GameQuestionSet>,
    private val actionListener: OnActionListener
) : PagerAdapter() {

    private var isSubmitButtonClicked = false

    override fun getPageTitle(position: Int): CharSequence? {
        return (position + 1).toString()
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext).inflate(
            R.layout.item_question_layout_game_level,
            collection,
            false
        ) as ViewGroup

        layout.tag = "root$position"

        val skipQuestion = layout.findViewById<TextView>(R.id.tvSkipQuestion)
        val backPressed = layout.findViewById<TextView>(R.id.tvBackPressed)
        val reportQuestion = layout.findViewById<TextView>(R.id.tvReportQuestion)
        val linearLayoutCont = layout.findViewById<LinearLayout>(R.id.linearLayoutCont)


        if (questions[position].is_math == "0" && questions[position].has_image == "0") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                layout.question_title_tv_game_question.text =
                    Html.fromHtml(questions[position].title, Html.FROM_HTML_MODE_COMPACT)
            } else {
                layout.question_title_tv_game_question.text =
                    Html.fromHtml(questions[position].title)
            }
        } else {
            if (questions[position].has_image == "0") {
                layout.question_title_tv_math_game_question.text =
                    "<b><font color='white'>${questions[position].title}</font></b>"
            } else {
                layout.question_title_tv_math_game_question.text = questions[position].title
            }
        }

        if (!questions[position].picture.isNullOrEmpty()) {
            try {
                Glide.with(mContext).load(BuildConfig.SERVER_URL + questions[position].picture)
                    .into(layout.question_image_iv)
                layout.question_image_iv_game_question.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                layout.question_image_iv_game_question.visibility = View.GONE
            }
        } else {
            layout.question_image_iv_game_question.visibility = View.GONE
        }

        var i = 0
        val qs = questions[position]
        layout.linearLayoutOptions.removeAllViews()
        layout.linearLayoutOptions.gravity = Gravity.CENTER
        questions[position].options.forEach {
            val view: View
            if (questions[position].is_math == "0" && questions[position].has_image == "0") {
                view = LayoutInflater.from(mContext)
                    .inflate(R.layout.option_layout_game, null)
                val optionTxt = view.findViewById<TextView>(R.id.textViewOptionGame)
                val rbText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    Html.fromHtml(it)
                }

                if (SubscriptUtil.checkIfContainsSubscript(it)) {
                    optionTxt.setText(
                        SubscriptUtil.getSubscriptSpan(rbText),
                        TextView.BufferType.SPANNABLE
                    )
                } else {
                    optionTxt.text = rbText
                }
            } else {
                view = LayoutInflater.from(mContext)
                    .inflate(R.layout.option_layout_game, null)
                val mathView = view.findViewById<MathView>(R.id.mathViewOptionGame)
                if (questions[position].has_image == "0")
                    mathView.text = "<b><font color='white'>$it</font></b>"
                else
                    mathView.text = it
            }

            view.tag = questions[position].options.indexOf(it)

            val optionTxt = view.findViewById<TextView>(R.id.textViewOptionGame)
            optionTxt.tag = it
            optionTxt.id = i

            var isCorrectAnswer = false

            view.setOnClickListener { v ->
                skipQuestion.isEnabled = false
                skipQuestion.alpha = 0.5f
                val inputAns = v.tag as Int
                val answer = questions[position].answer
                if (inputAns == answer) {
                    view.background.level = 1
                    isCorrectAnswer = true
                } else {
                    view.background.level = 2
                    layout.linearLayoutOptions.children.forEach { childView ->
                        if (childView.tag == answer) {
                            childView.background.level = 1
                        }
                    }
                }

                actionListener.onSubmitAnswer(
                    position,
                    isCorrectAnswer,
                    GameResponseQuestion(
                        if (isCorrectAnswer) 1 else 0,
                        qs.id,
                        qs.topic_id,
                        qs.section_id,
                        qs.subject_id,
                        0,
                        inputAns,
                        qs.answer
                    )
                )
                disableAllAnswerClick(layout)
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 20, 0, 0)
            view.layoutParams = layoutParams
            layout.linearLayoutOptions.addView(view)
            i++
        }


        skipQuestion.setOnClickListener {
            actionListener.onQuestionSkipped(
                position, GameResponseQuestion(
                    -1,
                    qs.id,
                    qs.topic_id,
                    qs.section_id,
                    qs.subject_id,
                    0,
                    -1,
                    qs.answer
                )
            )
        }

        backPressed.setOnClickListener {
            actionListener.onBackClicked(position)
        }

        reportQuestion.setOnClickListener {
            actionListener.onReportProblemClicked(position)
        }

        if (position == questions.size - 2) {
            actionListener.loadMore()
        } else if (position == questions.size - 1) {
            actionListener.questionFinished()
        }

        // Manage touch to hold action
        var x = 0F
        var y = 0F
        linearLayoutCont.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    actionListener.onTouchHold()
                    Log.d("TouchEvent->", "Down")
                    x = event.x
                    y = event.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Log.d("TouchEvent->", "MoveOther")
                    if (abs(x - event.x) > 150 || abs(y - event.y) > 150) {
                        actionListener.onTouchRelease()
                        Log.d("TouchEvent->", "Move")
                        false
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    Log.d("TouchEvent->", "Cancel")
                    //actionListener.onTouchRelease()
                    false
                }
                MotionEvent.ACTION_UP -> {
                    actionListener.onTouchRelease()
                    Log.d("TouchEvent->", "Up")
                    false
                }
                else -> {
                    Log.d("TouchEvent->", event.actionMasked.toString())
                    true
                }
            }
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

    fun showRightAnswer(viewPager: ViewPager, position: Int) {
        val view = viewPager.findViewWithTag<View>("root$position")
        val skipQuestion = view.findViewById<TextView>(R.id.tvSkipQuestion)
        skipQuestion.isEnabled = false
        skipQuestion.alpha = 0.5f
        for (item in view.linearLayoutOptions.children) {
            val inputAns = item.tag as Int
            val answer = questions[position].answer
            if (inputAns == answer) {
                item.background.level = 1
                break
            }
        }
    }

    fun enableReportClick(viewPager: ViewPager, position: Int) {
        val view = viewPager.findViewWithTag<View>("root$position")
        val reportQuestion = view.findViewById<TextView>(R.id.tvReportQuestion)
        reportQuestion.isEnabled = true
        reportQuestion.isClickable = true
        reportQuestion.alpha = 1f
    }

    fun disableAllAnswerClick(view: View) {
        for (item in view.linearLayoutOptions.children) {
            item.isEnabled = false
            item.isClickable = false
        }
    }


    interface OnActionListener {
        fun onSubmitAnswer(position: Int, isCorrect: Boolean, answerResponse: GameResponseQuestion)
        fun onReportProblemClicked(position: Int)
        fun onBackClicked(position: Int)
        fun onQuestionSkipped(position: Int, answerResponse: GameResponseQuestion)
        fun loadMore()
        fun questionFinished()
        fun onTouchHold()
        fun onTouchRelease()
    }
}
