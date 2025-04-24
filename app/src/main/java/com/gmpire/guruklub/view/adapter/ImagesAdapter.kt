package com.gmpire.guruklub.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.library.Images
import com.gmpire.guruklub.view.activity.photoViewer.PhotoViewerActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_image.view.*
import java.lang.Exception

class ImagesAdapter(private val mContext: Context?, private val images: List<Images>?) :
    PagerAdapter() {


    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext)
            .inflate(R.layout.layout_image, collection, false) as ViewGroup
        if (mContext != null) {
            Glide.with(mContext).load(BuildConfig.SERVER_URL + images?.get(position)?.picture)
                .into(layout.ivImage)
        }
        collection.addView(layout)

        try {
            if (images?.get(position)?.slide_picture_title?.isNotEmpty()!!) {
                layout.image_caption_tv.visibility = View.VISIBLE
                layout.image_caption_tv.text = images[position].slide_picture_title
            } else {
                layout.image_caption_tv.visibility = View.GONE
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        layout.setOnClickListener {
            mContext?.startActivity(
                Intent(
                    mContext,
                    PhotoViewerActivity::class.java
                ).putExtra("images", images as ArrayList<Images>).putExtra("position", position)
            )
        }

        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return images?.size ?: 0
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

}
