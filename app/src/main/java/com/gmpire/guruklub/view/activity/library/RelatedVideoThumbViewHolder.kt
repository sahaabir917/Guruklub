package com.gmpire.guruklub.view.activity.library

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.gmpire.guruklub.data.model.library.Videos
import com.gmpire.guruklub.databinding.ItemRelatedVideoThumbsBinding
import com.gmpire.guruklub.databinding.ItemVideoThumbsBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.custom_play_youtube.view.*
import kotlinx.android.synthetic.main.item_quick_exam.view.*

class RelatedVideoThumbViewHolder(itemView: ViewDataBinding, var context: Context) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as  ItemRelatedVideoThumbsBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as Videos

      binding.videoTitle1.text = itemModel.video_title

        binding.youtubePlayerViewThumb.play_pause_button.setOnClickListener {
            Toast.makeText(context, "Working!!", Toast.LENGTH_SHORT).show()
        }


        var splits = itemModel.video_url?.split("=")
        val videoId = splits.get(splits.size - 1)
        val url = "https://img.youtube.com/vi/$videoId/0.jpg"
        Glide.with(context).load(url)
            .into(
                binding.imageViewYoutubeThumb
            )

        binding.root.setOnClickListener {
            listener.callBack(position, itemModel, binding.root)
            //Log.d("video_id", videoId)
            //Toast.makeText(context, videoId, Toast.LENGTH_SHORT).show()
            context.startActivity(
                Intent(context, SingleVideoActivity::class.java)
                    .putExtra("video_id", videoId)
                    .putExtra("video_title", itemModel.video_title)
                    .putExtra("videos_id",itemModel.id.toString())
            )
        }

        val drawableTop = binding.videodescriptionlayout.background as GradientDrawable
        var color = ColorUtil.getColorByPosition(position)
        drawableTop.setColor(ContextCompat.getColor(mContext!!, color))

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

    override fun onLastPosition() {
        // binding.view.visibility = View.GONE
    }
}