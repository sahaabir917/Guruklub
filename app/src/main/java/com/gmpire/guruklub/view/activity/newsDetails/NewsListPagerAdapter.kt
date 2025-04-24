package com.gmpire.guruklub.view.activity.newsDetails

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.infocenter.News
import com.gmpire.guruklub.data.model.library.Images
import com.gmpire.guruklub.data.model.library.Videos
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.view.adapter.ImagesAdapter
import com.gmpire.guruklub.view.adapter.VideoPagerAdapter
import com.rd.PageIndicatorView

class NewsListPagerAdapter(
    private val mContext: Context,
    private val newsList: List<News>
) : PagerAdapter() {

    lateinit var imageSliderLayout: View
    lateinit var videoSliderLayout: View
    lateinit var imageViewerViewPager: ViewPager
    lateinit var videoViewerViewPager: ViewPager
    lateinit var imageViewerPageIndicatorView: PageIndicatorView
    lateinit var videoViewerPageIndicatorView: PageIndicatorView


    override fun getPageTitle(position: Int): CharSequence? {
        return (position + 1).toString()
    }

    override fun getCount(): Int {
        return newsList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    fun getNews(pos: Int): News {
        return newsList[pos]
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext).inflate(
            R.layout.item_news_layout,
            collection,
            false
        ) as ViewGroup

        imageSliderLayout = layout.findViewById(R.id.image_slider_layout)
        videoSliderLayout = layout.findViewById(R.id.video_slider_layout)
        videoViewerViewPager = layout.findViewById(R.id.videoViewerViewPager)
        imageViewerViewPager = layout.findViewById(R.id.imageViewerViewPager)
        imageViewerPageIndicatorView = layout.findViewById(R.id.imageViewerPageIndicatorView)
        videoViewerPageIndicatorView = layout.findViewById(R.id.videoViewerPageIndicatorView)

        showDetails(layout, newsList[position])
        if (getNews(position).image_list?.size ?: 0 > 0) {
            initImages(getNews(position).image_list)
        } else {
            imageSliderLayout.visibility = View.GONE
        }

        if (getNews(position).video_list?.size ?: 0 > 0) {
            initVideos(getNews(position).video_list)
        } else {
            videoSliderLayout.visibility = View.GONE
        }
        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    private fun initImages(newsImages: List<Images>?) {
        imageSliderLayout.visibility = View.VISIBLE
        imageViewerViewPager.adapter = ImagesAdapter(mContext, newsImages)

        imageViewerViewPager.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                imageViewerPageIndicatorView.selection = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    private fun initVideos(videos: List<Videos>?) {
        videoSliderLayout.visibility = View.VISIBLE
        videoViewerViewPager.adapter = VideoPagerAdapter(mContext, videos)
        videoViewerViewPager.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                videoViewerPageIndicatorView.selection = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }


    private fun showDetails(view: View, news: News?) {
        val tvNewsTitle: TextView = view.findViewById(R.id.tv_news_title)
        val tvNewsDetails: WebView = view.findViewById(R.id.tv_news_details)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        val newsParentLayout: View = view.findViewById(R.id.news_parent_layout)

        tvNewsTitle.text = news?.title

        tvNewsDetails.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, progress: Int) {
                progressBar.visibility = View.VISIBLE
                if (progress == 100) progressBar.visibility = View.GONE
            }
        }


        var text = "<html><body style=\"text-align:justify\">"
        text += news?.details
        text += "</body></html>"
        val settings = tvNewsDetails.settings
        settings.defaultTextEncodingName = "utf-8"
        tvNewsDetails.loadData(text, "text/html; charset=utf-8", "utf-8")

        newsParentLayout.visibility = View.VISIBLE
    }
}