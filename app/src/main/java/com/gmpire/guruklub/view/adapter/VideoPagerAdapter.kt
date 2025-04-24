package com.gmpire.guruklub.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.viewpager.widget.PagerAdapter
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.library.Videos
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.android.synthetic.main.layout_video.view.*


class VideoPagerAdapter(private val mContext: Context?, private val videos: List<Videos>?) :
    PagerAdapter() {


    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext)
            .inflate(R.layout.layout_video, collection, false) as ViewGroup

        layout.youtube_player_view.addYouTubePlayerListener(object :
            AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                var splits = videos?.get(position)?.video_url?.split("=")
                val videoId = splits?.get(splits.size - 1)
                if (videoId != null) {
                    youTubePlayer.cueVideo(videoId, 0f)
                }
                youTubePlayer.pause()
            }
        })

        //  layout.video_view.setVideoPath(videos[position].video_url)
        // layout.video_view.resume()


        collection.addView(layout)

        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return videos?.size ?: 0
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

}
