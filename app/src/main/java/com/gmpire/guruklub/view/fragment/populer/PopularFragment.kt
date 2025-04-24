package com.gmpire.guruklub.view.fragment.populer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.infocenter.News
import com.gmpire.guruklub.data.model.infocenter.NewsResponse
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.databinding.FragmentNewsBinding
import com.gmpire.guruklub.util.ConstantField
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.infoCenter.InfoCenterViewModel
import com.gmpire.guruklub.view.activity.infoCenter.NewsViewHolder
import com.gmpire.guruklub.view.activity.newsDetails.NewsDetailsActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.base.DATA_VIEW_TYPE
import com.gmpire.guruklub.view.fragment.news.NewsFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response

class PopularFragment(

) : BaseFragment() {

    fun PopularFragment() {
        // doesn't do anything special
    }

    private var hasNextPage: Boolean = false
    private lateinit var binding: FragmentNewsBinding
    private lateinit var viewmodel: InfoCenterViewModel
    private var news = ArrayList<News>()
    val filterValues = FilterValues()
    var page: Int = 1
    private var popularNewsResponseLive: MutableLiveData<NewsResponse>? = null
    private var pageLive: MutableLiveData<Int>? = null

    private var selectedDateTo = ""
    private var selectedDateFrom = ""
    private var searchText = ""
    private var previousPage: Int? = null

    private var progressBarChildListener: NewsFragment.ProgressBarChildListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_news, container, false);
        return binding.root
    }

    fun populateView(newsResponse: NewsResponse) {
        newsResponse.data?.let {
            hasNextPage = newsResponse.next_page != 0
            initNews(newsResponse.data.news as ArrayList<News>)
            binding.emptyView.visibility = View.GONE
            binding.rvNews.visibility = View.VISIBLE
        } ?: kotlin.run {
            binding.emptyView.visibility = View.VISIBLE
            binding.rvNews.visibility = View.GONE
        }
    }

    override fun viewRelatedTask() {
        viewmodel =
            ViewModelProviders.of(this, viewModelFactory).get(InfoCenterViewModel::class.java)
//        progressBarChildListener = parentFragment as NewsFragment.ProgressBarChildListener
        showShimmer()

        page = 1
        viewmodel.apiGePopularNews(
            "1",
            "",
            "",
            "",
            "",
            this
        )
    }

    private fun showShimmer() {
        binding.shimmerViewContainerNews.visibility = View.VISIBLE
        binding.shimmerViewContainerNews.startShimmerAnimation()
        binding.rvNews.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
    }

    private fun hideShimmer() {
        binding.shimmerViewContainerNews.stopShimmerAnimation()
        binding.shimmerViewContainerNews.visibility = View.GONE
        binding.rvNews.visibility = View.VISIBLE
    }

    private fun insertEmptyNews(newsList: ArrayList<News>): ArrayList<News> {
        /* var placement = ConstantField.NATIVE_AD_INTERVAL
         var increment = 0

         if (news.isNotEmpty())
             placement =
                 ConstantField.NATIVE_AD_INTERVAL - (news.size % (ConstantField.NATIVE_AD_INTERVAL + 1))*/

        val newsListSize = newsList.size
        if (newsListSize > ConstantField.NATIVE_AD_INTERVAL) {
            newsList.add(ConstantField.NATIVE_AD_INTERVAL, News())
        }

        /*val newsListSize = newsList.size
        while (newsListSize > placement) {
            newsList.add(placement + increment, News())
            increment++
            placement += ConstantField.NATIVE_AD_INTERVAL
        }*/
        return newsList
    }

    private fun initNews(news: ArrayList<News>) {
        if (page != 1) {
            this.news.addAll(news)
            binding.rvNews.adapter?.notifyDataSetChanged()
        } else {
            this.news.clear()
            this.news = insertEmptyNews(news)
            binding.rvNews.layoutManager = LinearLayoutManager(activity)
            binding.rvNews.adapter = BaseRecyclerAdapter(activity, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                    model as News
                    startActivity(
                        Intent(
                            activity,
                            NewsDetailsActivity::class.java
                        )
                            .putExtra("page",previousPage.toString())
                            .putExtra("news", model)
                            .putExtra("newsType", 2)
                    )
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    if (viewType == DATA_VIEW_TYPE) {
                        return NewsViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context),
                                R.layout.item_news,
                                parent,
                                false
                            ), this@PopularFragment
                        )
                    } else {
                        return NativeAdViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context),
                                R.layout.item_empty_native_ads,
                                parent,
                                false
                            ), activity
                        )
                    }
                }

                override fun loadMoreItem() {
                    if (page != 0) {
                        viewmodel.apiGePopularNews(
                            page.toString(), filterValues.category_id ?: searchText,
                            searchText, selectedDateFrom, selectedDateTo,
                            this@PopularFragment
                        )
                    }
                }
            }, news)
        }

    }

    fun updateFilterCategories(
        categoryId: String,
        searchText: String,
        dateFrom: String,
        dateTo: String
    ) {
        filterValues.category_id = categoryId
        this.searchText = searchText
        selectedDateFrom = dateFrom
        selectedDateTo = dateTo

        if (::viewmodel.isInitialized) {
//            progressBarChildListener = parentFragment as NewsFragment.ProgressBarChildListener
            viewmodel =
                ViewModelProviders.of(this, viewModelFactory).get(InfoCenterViewModel::class.java)
            page = 1
        }

        viewmodel.apiGePopularNews(
            page.toString(),
            categoryId,
            searchText,
            dateFrom,
            dateTo,
            this@PopularFragment
        )
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        when (key) {
            "apiGePopularNews" -> {
                if (isLoader)
                    showProgressDialog()
                else
                    hideProgressDialog()
            }
        }
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGePopularNews" -> {
                hideShimmer()
                val type = object : TypeToken<BaseModel<NewsResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<NewsResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200 && baseData.data != null) {
                        populateView(baseData.data!!)
                        previousPage = page
                        page = baseData?.data?.next_page!!
                    } else {
                        showToast(activity, "No data found!!")
                    }
                }
            }
        }
    }



    override fun onError(err: Throwable, key: String) {
        super.onError(err, key)
        hideShimmer()
    }


    override fun onClick(v: View?) {


    }

}