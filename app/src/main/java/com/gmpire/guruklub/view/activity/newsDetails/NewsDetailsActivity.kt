package com.gmpire.guruklub.view.activity.newsDetails

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.SingleNewsResponse
import com.gmpire.guruklub.data.model.infocenter.News
import com.gmpire.guruklub.data.model.infocenter.NewsResponse
import com.gmpire.guruklub.databinding.ActivityNewsDetailsBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.dialog.NewsDetailsGestureDialog
import com.gmpire.guruklub.view.fragment.notification.NotificationViewModel
import com.google.android.gms.ads.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response

class NewsDetailsActivity : BaseActivity() {

    private var page: String = ""
    private lateinit var binding: ActivityNewsDetailsBinding
    private lateinit var news: News
    private var adView: AdView? = null
    private var newsId: String = ""
    private lateinit var viewModel: NotificationViewModel
    private var isAdsFree: Boolean = false
    private var adapter: NewsListPagerAdapter? = null
    private var interstitialCount = 20
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var newsDetailsGestureDialog: NewsDetailsGestureDialog
    private var gestureShowCount :Int = 0

    private var newsList = arrayListOf<News>()

    override fun onStart() {
        super.onStart()
        var getgestureCount = dataManager.mPref.prefGetIsFirstTimeGesture()
        if (getgestureCount != null) {
            gestureShowCount = getgestureCount.toInt()
        }
        gestureShowCount++
        if(gestureShowCount == 1){
            dataManager.mPref.prefSetIsFirstTimeGesture(gestureShowCount.toString())
            newsDetailsGestureDialog = NewsDetailsGestureDialog.newInstance()
            newsDetailsGestureDialog.show(supportFragmentManager,newsDetailsGestureDialog.tag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_news_details)
        //Load Ad
        isAdsFree = dataManager.mPref.prefGetIsAdFree() ?: false
        if (!isAdsFree) {
            adView = AdView(this);
            binding.rlAdContainerNewsDet.addView(adView);
            loadBanner()
        }
    }



    @RequiresApi(Build.VERSION_CODES.N)
    override fun viewRelatedTask() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(NotificationViewModel::class.java)

        if (intent.getIntExtra("newsType", 0) == 1) {
            page = intent.getStringExtra("page")
            viewModel.apiGetNews("", "", "", "", "", this)
        } else if (intent.getIntExtra("newsType", 0) == 2) {
            viewModel.apiGePopularNews(
                "",
                "",
                "",
                "",
                "",
                this
            )
        }

        try {
            if (intent.extras?.containsKey("news") == true) {
                news = intent.getSerializableExtra("news") as News
                configureToolbar()
                viewModel.apiGetNewsById(news.id, this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        buildInterstitialAd()
    }

    private fun buildInterstitialAd() {
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = this.getString(R.string.ad_unit_id_interstitial_test)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    private fun configureToolbar() {
        setToolbar(this, binding.toolbar, "", true)
        binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.toolbar.appCompatTextViewLogo.text = (Html.fromHtml(text))
    }

    private fun loadBanner() {
        adView?.adUnitId = getString(R.string.ad_unit_id_banner_test)
        adView?.adSize = adSize
        val adRequest = AdRequest
            .Builder()
            .build()
        adView?.loadAd(adRequest)

        adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                binding.viewBorderTop.setBackgroundColor(
                    ContextCompat.getColor(
                        this@NewsDetailsActivity,
                        R.color.white
                    )
                )
                binding.addSpaceQuesDet.visibility = View.GONE
            }

            override fun onAdFailedToLoad(errorCode: Int) {
            }

            override fun onAdOpened() {
            }

            override fun onAdClicked() {

            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
    }

    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.rlAdContainerNewsDet.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    override fun onLoading(isLoader: Boolean, key: String) {

    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetNewsById" -> {
                val type = object : TypeToken<BaseModel<SingleNewsResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<SingleNewsResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        val singleNews = baseData.data?.news
                        if(singleNews != null) {
                            newsList.add(singleNews)
                            initNewsPager()
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
            "apiGetNews" -> {
                //hideShimmer()
                val type = object : TypeToken<BaseModel<NewsResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<NewsResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200 && baseData.data != null) {
                        newsList = baseData.data?.data?.news as ArrayList<News>
                        initNewsPager()
                    } else {
                        showToast(this, "No data found!!")
                    }
                }
            }
            "apiGePopularNews" -> {
                val type = object : TypeToken<BaseModel<NewsResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<NewsResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200 && baseData.data != null) {
                        newsList = baseData.data?.data?.news as ArrayList<News>
                        initNewsPager()
                    } else {
                        showToast(this@NewsDetailsActivity, "No data found!!")
                    }
                }
            }
        }
    }

    private fun initNewsPager() {
        adapter = NewsListPagerAdapter(this, newsList)
        binding.newsViewpager.adapter = adapter
        getCurrentItemPosition()
        binding.newsViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (position == 0)
                    return
                if (position % interstitialCount == 0) {
                    if (!isAdsFree) {
                        interstitialCount += 20
                        if (mInterstitialAd.isLoaded) {
                            mInterstitialAd.show()
                            buildInterstitialAd()
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.")
                        }
                    }
                }
            }
        })
    }

    private fun getCurrentItemPosition() {
        val item = newsList.find {
            it.id == news.id
        }
        var position = newsList.indexOf(item) ?: 0
        binding.newsViewpager.currentItem = position
    }

    override fun onError(err: Throwable, key: String) {
        super.onError(err, key)
        err.printStackTrace()
    }


    override fun onClick(v: View?) {

    }
}
