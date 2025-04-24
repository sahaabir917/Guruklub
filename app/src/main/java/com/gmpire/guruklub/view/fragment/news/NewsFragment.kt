package com.gmpire.guruklub.view.fragment.news

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.infoCenter.InfoCenterViewModel
import com.gmpire.guruklub.view.activity.infoCenter.NewsViewHolder
import com.gmpire.guruklub.view.activity.newsDetails.NewsDetailsActivity
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.base.DATA_VIEW_TYPE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response

class NewsFragment(

) : BaseFragment() {

    open fun NewsFragment() {
        // doesn't do anything special
    }

    private var previousPage: Int? = null
    private lateinit var binding: FragmentNewsBinding
    private lateinit var viewmodel: InfoCenterViewModel
    private var news = ArrayList<News>()
    lateinit var spinnerAdapterCategory: CustomSpinnerAdapter
    val filterValues = FilterValues()
    private var page: Int = 1
    private var selectedDateTo = ""
    private var selectedDateFrom = ""
    private var searchText = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_news, container, false)
        return binding.root
    }

    fun populateView(newsResponse: NewsResponse, page: Int) {
        newsResponse.data?.let {
            Log.d("Test->", "Live data working!")
            initQuestion(newsResponse.data.news as ArrayList<News>)
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
        page = 1
        showShimmer()
        viewmodel.apiGetNews(page.toString(), "", "", "", "", this)
    }



    private fun insertEmptyNews(newsList: ArrayList<News>): ArrayList<News> {
        val newsListSize = newsList.size
        if (newsListSize > 5) {
            newsList.add(5, News())
        }

        return newsList
    }

    private fun initQuestion(news: ArrayList<News>) {
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
                        Intent(activity, NewsDetailsActivity::class.java)
                            .putExtra(
                                "news",
                                model
                            )
                            .putExtra("page",previousPage.toString())
                            .putExtra("newsType", 1)
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
                            ), this@NewsFragment
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
                        viewmodel.apiGetNews(
                            page.toString(), filterValues.category_id ?: searchText,
                            searchText, selectedDateFrom, selectedDateTo,
                            this@NewsFragment
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
        selectedDateTo = selectedDateTo

        if (::viewmodel.isInitialized) {
//            progressBarChildListener = parentFragment as ProgressBarChildListener
            viewmodel =
                ViewModelProviders.of(this, viewModelFactory).get(InfoCenterViewModel::class.java)
        }

        page = 1

        showShimmer()
        viewmodel.apiGetNews(
            page.toString(),
            categoryId,
            searchText,
            dateFrom,
            dateTo,
            this@NewsFragment
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

    override fun onLoading(isLoader: Boolean, key: String) {
        when (key) {
            "apiGetNews" -> {
                /*if (isLoader)
                    progressBarChildListener?.manageVisibility(true)
                else
                    progressBarChildListener?.manageVisibility(false)*/
            }
        }
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetNews" -> {
                hideShimmer()
                val type = object : TypeToken<BaseModel<NewsResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<NewsResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200 && baseData.data != null) {
                        populateView(baseData.data!!, 1)
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

//    fun fitler(userInput: String) {
//        this.filterlist.clear()
//        for (item in news) {
//            if (item.title.toLowerCase().contains(userInput.toLowerCase())) {
//                this.filterlist.add(item)
//            }
//    }
//    }
//
//    fun copyPlayedModelTest() {
//        var sizeofCopiedBatchData = this.copiedbatch.size - 1
//        this.news.clear()
//        for (i in 0..sizeofCopiedBatchData) {
//            var copyDatas = copiedbatch[i].copy(
//                this.copiedbatch[i].category_id,
//                this.copiedbatch[i].category_name,
//                this.copiedbatch[i].cover_image,
//                this.copiedbatch[i].created_at,
//                this.copiedbatch[i].date,
//                this.copiedbatch[i].details,
//                this.copiedbatch[i].id,
//                this.copiedbatch[i].is_popular,
//                this.copiedbatch[i].status,
//                this.copiedbatch[i].title,
//                this.copiedbatch[i].image_list,
//                this.copiedbatch[i].video_list
//            )
//            this.news.add(copyDatas)
//        }
//    }

    interface ProgressBarChildListener {
        fun manageVisibility(show: Boolean)
    }
}