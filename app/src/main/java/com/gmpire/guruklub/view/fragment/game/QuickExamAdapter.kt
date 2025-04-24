package com.gmpire.guruklub.view.fragment.game


/**
 * Created by Tahsin Rahman on 14/1/21.
 */


import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.game.GameChallengeItem
import com.gmpire.guruklub.util.ColorUtil
import kotlinx.android.synthetic.main.item_quick_exam.view.*
import kotlin.math.roundToInt


class QuickExamAdapter(
    private val mContext: Context?,
    private val exams: List<GameChallengeItem>?,
    private val mItemClick : OnItemClick
) : PagerAdapter() {

    private lateinit var itemClick:QuickExamAdapter.OnItemClick

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        itemClick = mItemClick
        val layout = LayoutInflater.from(mContext)
            .inflate(R.layout.item_quick_exam, collection, false) as ViewGroup
        layout.cardView2.setOnClickListener {
            itemClick.onRootclicked(position)
        }
        layout.textView_total_ques.text = exams?.get(position)?.question_number.toString()
        layout.textView_quick_exam_title.text = exams?.get(position)?.name.toString()
        layout.textView_exam_time.text = exams?.get(position)?.game_time.toString() + " min"
        layout.textView_marks.text = exams?.get(position)?.total_point.toString()
        layout.textView_negative_marks.text = exams?.get(position)?.wrong.toString()
        layout.heart_cost.text = exams?.get(position)?.hearts_cost.toString()
        val drawableTop = layout.relativelayout_live_exam_top.background as GradientDrawable

        var color = ColorUtil.getColorByPosition(position)
        drawableTop.setColor(ContextCompat.getColor(mContext!!, color))

        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return exams?.size ?: 0
    }

    override fun getPageWidth(position: Int): Float {
        return 0.7f
    }


    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    private fun dpToPx(dp: Int): Int {
        val displayMetrics = mContext?.resources?.displayMetrics
        return (dp * (displayMetrics!!.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    interface OnItemClick{
        fun onRootclicked(position: Int)
    }

}
