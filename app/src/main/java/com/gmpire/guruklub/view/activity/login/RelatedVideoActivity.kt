package com.gmpire.guruklub.view.activity.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
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
import com.gmpire.guruklub.data.model.leaderboard.LeaderBoard
import com.gmpire.guruklub.data.model.leaderboard.LeaderBoardResponse
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.library.Library
import com.gmpire.guruklub.data.model.library.Videos
import com.gmpire.guruklub.databinding.ActivityLeaderDetailsBinding
import com.gmpire.guruklub.databinding.ActivityRelatedVideoBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.library.LibraryViewmodel
import com.gmpire.guruklub.view.activity.library.RelatedVideoThumbViewHolder
import com.gmpire.guruklub.view.activity.library.VideoThumbViewHolder
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_related_video.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception
import java.util.ArrayList

class RelatedVideoActivity : BaseActivity() {
    private var videosData: ArrayList<Videos> = arrayListOf()
    private lateinit var filterValues: FilterValues
    private var filterlist = ArrayList<Videos>()
    private var copiedVideos = arrayListOf<Videos>()
    private lateinit var binding: ActivityRelatedVideoBinding
    private lateinit var viewmodel: LibraryViewmodel
    private var library: MutableLiveData<Library> = MutableLiveData()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_related_video)

    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "", true)
        binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.toolbar.appCompatTextViewLogo.text = (Html.fromHtml(text))
        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewmodel::class.java)

        filterValues = Gson().fromJson(
            intent.getStringExtra("filterValues"),
            FilterValues::class.java
        )
        Log.d("filtervalues", filterValues.toString())

        viewmodel.apiGetLibrary(
            dataManager.mPref.prefGetUserInfo().id,
            filterValues, this
        )

        initSearchEditText()
        binding.addVideoLayout.setOnClickListener(this)
        binding.addVideoLayout2.setOnClickListener(this)
    }

    private fun initSearchEditText() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                fitler(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                copyVideoDatas()
            }

        })
    }

    private fun copyVideoDatas() {
        var sizeofCopiedBatchData = this.copiedVideos.size - 1
        this.videosData.clear()
        for (i in 0..sizeofCopiedBatchData) {
            var copyDatas = copiedVideos[i].copy(
                copiedVideos[i].id,
                copiedVideos[i].library_id,
                copiedVideos[i].news_id,
                copiedVideos[i].video_title,
                copiedVideos[i].video_url,
                copiedVideos[i].created_at
            )
            this.videosData.add(copyDatas)
        }
    }

    private fun fitler(userInput: String) {
        this.filterlist.clear()
        for (item in videosData) {
            if (item.video_title.toLowerCase().contains(userInput.toLowerCase())) {
                this.filterlist.add(item)
            }
        }

        var sizeoffilterlist = this.filterlist.size - 1
        if (sizeoffilterlist >= 0) {
            binding.videoViewerRecycler.visibility = View.VISIBLE
            binding.emptyMessage.visibility = View.GONE
            this.videosData.clear()
            this.videosData.addAll(filterlist)
            binding.videoViewerRecycler.adapter?.notifyDataSetChanged()
        } else if (sizeoffilterlist < 0) {
            binding.emptyMessage.visibility = View.VISIBLE
            binding.videoViewerRecycler.visibility = View.GONE
        }

    }

    override fun navigateToHome() {

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetLibrary" -> {
                val type = object : TypeToken<BaseModel<Library>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<Library>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                if (baseData.data?.videos != null && baseData.data?.videos?.isNotEmpty()!!) {
                                    videosData =
                                        baseData.data?.videos as java.util.ArrayList<Videos>
                                    initLibraryVideo(videosData)
                                    initCopyVideosData()
                                } else {
                                    manageButtonVisibility()
                                }
                            } else {
                                //showToast(this, baseData.message[0])
                                manageButtonVisibility()
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        //showToast(this, baseData.message[0])
                        manageButtonVisibility()
                    }
                }
            }
        }
    }

    override fun onErrorCode(
        code: Int,
        message: String,
        result: LiveDataResult<Response<ResponseBody>>
    ) {
        super.onErrorCode(code, message, result)
        manageButtonVisibility()
    }

    private fun manageButtonVisibility() {
        binding.addVideoLayout2.visibility = View.VISIBLE
        binding.emptyMessage.visibility = View.VISIBLE
        binding.addVideoLayout.visibility = View.GONE
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.addVideoLayout -> {
                startActivity(Intent(this, AddVideoActivity::class.java))
            }
            binding.addVideoLayout2 -> {
                startActivity(Intent(this, AddVideoActivity::class.java))
            }
        }
    }

    private fun initLibraryVideo(libraryVideos: ArrayList<Videos>) {
        binding.videoViewerRecycler.layoutManager = LinearLayoutManager(this)
        binding.videoViewerRecycler.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }
                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return RelatedVideoThumbViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                this@RelatedVideoActivity
                            ), R.layout.item_related_video_thumbs, parent, false
                        ), this@RelatedVideoActivity
                    )
                }

                override fun loadMoreItem() {
                }
            }, videosData)
    }

    private fun initCopyVideosData() {
        var sizeofBatchData = this.videosData.size - 1
        this.copiedVideos.clear()
        for (i in 0..sizeofBatchData) {
            var copyDatas = this.videosData[i].copy(
                this.videosData[i].id,
                this.videosData[i].library_id,
                this.videosData[i].news_id,
                this.videosData[i].video_title,
                this.videosData[i].video_url,
                this.videosData[i].created_at
            )
            this.copiedVideos.add(copyDatas)
        }
    }

}