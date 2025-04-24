package com.gmpire.guruklub.view.activity.leaderDetailsActivity

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.leaderboard.LeaderBoard
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.util.DisplayUtil
import kotlinx.android.synthetic.main.item_leader_board.view.*
import kotlin.math.roundToInt


class LeaderDetailsAdapter(
    private val mContext: Context?,
    private val leader: ArrayList<LeaderBoard>
) :
    PagerAdapter() {

    var positions = 0
    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext)
            .inflate(R.layout.item_leader_board, collection, false) as ViewGroup
        layout.tvName.text = leader?.get(position).name
        positions++

        Log.d("positon is ", position.toString())

        if (positions == 1) {
            layout.tvPosition.text = positions.toString() + "st position"
        } else if (positions == 2) {
            layout.tvPosition.text = positions.toString() + "nd position"
        } else if (positions == 3) {
            layout.tvPosition.text = positions.toString() + "rd position"

        }

        if (leader?.get(position).profile_pic != null) {
            Glide.with(mContext!!)
                .load(leader?.get(position).profile_pic)
                .placeholder(R.drawable.ic_placeholder_user)
                .error(R.drawable.ic_placeholder_user)
                .override(100, 100)
                .into(layout.profile_pic)
        } else {
            layout.profile_pic.setImageResource(R.drawable.ic_placeholder_user)
        }

        layout.examdate.text = leader?.get(position).submit_date
        layout.tvScore.text =
            leader?.get(position).get_point + "/" + leader?.get(position).total_point


        val drawableTop = layout.cardView9.background as GradientDrawable
        val drawableText = layout.rlPosition.background as GradientDrawable

        var color = ColorUtil.getColorByPosition(position)

        drawableTop.setStroke(
            DisplayUtil.dpToPx(4, mContext!!),
            ContextCompat.getColor(mContext, color)
        )
        drawableText.setColor(ContextCompat.getColor(mContext, color))

        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return leader?.size ?: 0
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

}
