package com.gmpire.guruklub.view.activity.library

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.library.Videos
import com.gmpire.guruklub.databinding.ItemVideoThumbsBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import kotlinx.android.synthetic.main.custom_play_youtube.view.*


/**
 * Created by Tahsin Rahman on 8/11/20.
 */

class VideoThumbViewHolder(itemView: ViewDataBinding, var context: Context) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemVideoThumbsBinding
    var mContext = context
    override fun <T> onBind(position: Int, itemModel: T, listener: IAdapterListener) {

        itemModel as Videos

        binding.youtubePlayerViewThumb.play_pause_button.setOnClickListener {
            Toast.makeText(context, "Working!!", Toast.LENGTH_SHORT).show()
        }

        /*binding.youtubePlayerViewThumb.addYouTubePlayerListener(object :
            AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                var splits = itemModel.video_url?.split("=")
                val videoId = splits?.get(splits.size - 1)
                if (videoId != null) {
                    youTubePlayer.cueVideo(videoId, 0f)
                }
                val customPlayerUiController = CustomUiController(customView, context, videoId)
                youTubePlayer.addListener(customPlayerUiController)
                binding.youtubePlayerViewThumb.addFullScreenListener(customPlayerUiController);
            }
        })*/

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
            )
        }

    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

    override fun onLastPosition() {
        // binding.view.visibility = View.GONE
    }
}