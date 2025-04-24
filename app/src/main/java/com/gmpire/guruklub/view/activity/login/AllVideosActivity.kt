package com.gmpire.guruklub.view.activity.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.library.Videos
import com.gmpire.guruklub.databinding.ActivityAllVideosBinding
import com.gmpire.guruklub.util.GeneratDropdownItem
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.library.RelatedVideoThumbViewHolder
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.fragment.favourite.FavouriteViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response


class AllVideosActivity : BaseActivity(), IDatabaseCallBack {
    private var isBlur: Boolean = false
    private var mContext: Context? = null
    private var videosData = arrayListOf<Videos>()
    private lateinit var binding: ActivityAllVideosBinding
    val filterValues = FilterValues()
    lateinit var spinnerAdapterSubject: CustomSpinnerAdapter
    lateinit var spinnerAdapterSection: CustomSpinnerAdapter
    lateinit var spinnerAdapterTopic: CustomSpinnerAdapter
    private lateinit var viewModel: FavouriteViewModel
    private var copiedVideos = arrayListOf<Videos>()
    var subjectList = java.util.ArrayList<BaseItem>()
    private var filterlist = java.util.ArrayList<Videos>()
    var category_id: String = "1"
    lateinit var spinnerAdapterBatch: CustomSpinnerAdapter
    lateinit var spinnerAdapterDifficulty: CustomSpinnerAdapter

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "All Videos", true)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(FavouriteViewModel::class.java)

        filterValues.category_id = dataManager.mPref.prefGetUserInfo().category_id.toString()
        viewModel.iDatabaseCallBack = this

        initSearchEditText()

        viewModel.apiGetSubjectByCategory(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )

        viewModel.apiGetAllVideos(category_id, "", "", "", this)
        mContext = applicationContext
        binding.coordinatorlayout.setOnClickListener(this)

        binding.showSectionlayout.setOnClickListener(this)
        binding.appBar.setOnClickListener(this)

        val item = java.util.ArrayList<Common>()
        item.add(Common("1", "Basic"))
        item.add(Common("2", "Intermediate"))
        item.add(Common("3", "Advanced"))

        //initMakeChoiceItemSpinner(binding.spSubject, "Select Difficulty", item)

        viewModel.apiGetBatchByCategory(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )

        binding.relativeLayoutSubject.setOnClickListener {
            binding.spSubject.performClick()
        }

        binding.spSectionlayout.setOnClickListener {
            binding.spSection.performClick()
        }

        binding.spTopicLayout.setOnClickListener {
            binding.spTopic.performClick()
        }
        binding.btnMakeChoice.setOnClickListener(this)
        binding.addVideoLayout.setOnClickListener(this)
        binding.root.setOnClickListener(this)
    }

    private fun initSearchEditText() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                copyVideoData()
            }
        })
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (isLoader) {
            binding.progressBarAllVideos.visibility = View.VISIBLE
        } else {
            binding.progressBarAllVideos.visibility = View.GONE
        }
    }

    private fun copyVideoData() {
        var sizeofCopiedBatchData = this.copiedVideos.size - 1
        videosData.clear()
        for (i in 0..sizeofCopiedBatchData) {
            var copyDatas = copiedVideos[i].copy(
                copiedVideos[i].id,
                copiedVideos[i].library_id,
                copiedVideos[i].news_id,
                copiedVideos[i].video_title,
                copiedVideos[i].video_url,
                copiedVideos[i].created_at
            )
            videosData.add(copyDatas)
        }
    }

    private fun filter(userInput: String) {
        this.filterlist.clear()
        for (item in videosData) {
            if (item.video_title.toLowerCase().contains(userInput.toLowerCase())) {
                this.filterlist.add(item)
            }
        }

        var sizeoffilterlist = this.filterlist.size - 1
        if (sizeoffilterlist >= 0) {
            binding.videoViewerRecycler.visibility = View.VISIBLE
            this.videosData.clear()
            this.videosData.addAll(filterlist)
            binding.videoViewerRecycler.adapter?.notifyDataSetChanged()
        } else if (sizeoffilterlist < 0) {
            binding.videoViewerRecycler.visibility = View.GONE
        }

    }

    fun initMakeChoiceItemSpinner(sp: View, hint: String, item: ArrayList<Common>) {
        sp as AppCompatSpinner
        var sa = CustomSpinnerAdapter(
            this,
            GeneratDropdownItem.getDropdownItems(
                JSONArray(Gson().toJson(item)),
                "name",
                "id",
                hint
            )
        )
        if (hint == "Select Subject") {
            isBlur = true
        }
        sp.adapter = sa.getAdapter(isBlur)
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                sa.setPosition(position)
                when (sp) {
                    binding.spSubject -> {
                        hideKeyboard()
                        viewModel.apiGetSectionBySubject(
                            spinnerAdapterSubject.getSelectedId(),
                            this@AllVideosActivity
                        )
                        filterValues.subject_id = spinnerAdapterSubject.getSelectedId()
                        if (filterValues.subject_id != "0") {
                            binding.sectionIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                            isBlur = true
                        } else {
                            binding.sectionIcon.setImageResource(R.drawable.blur_down_arrow)
                            isBlur = false
                        }
                    }
                    binding.spSection -> {
                        hideKeyboard()
                        viewModel.apiGetTopicBySection(
                            spinnerAdapterSection.getSelectedId(),
                            this@AllVideosActivity
                        )
                        filterValues.section_id = spinnerAdapterSection.getSelectedId()
                        if (filterValues.section_id == "0") {
                            binding.topicIcon.setImageResource(R.drawable.blur_down_arrow)
                            isBlur = false
                        } else {
                            binding.topicIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                            isBlur = true
                        }
                    }
                    binding.spTopic -> {
                        hideKeyboard()
                        filterValues.topic_id = spinnerAdapterTopic.getSelectedId()
                    }
                    binding.spBatch -> {
                        hideKeyboard()
                        filterValues.batch_id = spinnerAdapterBatch.getSelectedId()
                    }
                    binding.spDifficulty -> {
                        hideKeyboard()
                        filterValues.difficulty_id = spinnerAdapterDifficulty.getSelectedId()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        try {
            when (sp) {
                binding.spSubject -> spinnerAdapterSubject = sa
                binding.spSection -> spinnerAdapterSection = sa
                binding.spTopic -> spinnerAdapterTopic = sa
                binding.spBatch -> spinnerAdapterBatch = sa
                binding.spDifficulty -> spinnerAdapterDifficulty = sa
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun navigateToHome() {

    }

    override fun onSuccessDB(result: Any, optName: String) {
    }

    override fun onFailedDB(result: Any, optName: String) {
        println("$optName -> $result")
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
                                this@AllVideosActivity
                            ), R.layout.item_related_video_thumbs, parent, false
                        ), this@AllVideosActivity
                    )
                }

                override fun loadMoreItem() {
                }
            }, videosData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_videos)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetSubjectByCategory" -> {
                val type = object : TypeToken<BaseModel<java.util.ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            initMakeChoiceItemSpinner(
                                binding.spSubject,
                                "Select Subject",
                                baseData.data ?: arrayListOf()
                            )
                        }
                    }
                }
            }
            "apiGetSectionBySubject" -> {
                val type = object : TypeToken<BaseModel<java.util.ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )

                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            initMakeChoiceItemSpinner(
                                binding.spSection,
                                "Select Section",
                                baseData.data ?: arrayListOf()
                            )
                        }
                    }
                }
            }
            "apiGetTopicBySection" -> {
                val type = object : TypeToken<BaseModel<java.util.ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            initMakeChoiceItemSpinner(
                                binding.spTopic,
                                "Select Topic",
                                baseData.data ?: arrayListOf()
                            )
                        }
                    }
                }
            }
            "apiGetBatchByCategory" -> {
                val type = object : TypeToken<BaseModel<java.util.ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            initMakeChoiceItemSpinner(
                                binding.spBatch,
                                "Select Batch",
                                baseData.data ?: arrayListOf()
                            )
                        }
                    }
                }
            }
            "apiGetAllVideos" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Videos>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Videos>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            binding.videoViewerRecycler.visibility = View.VISIBLE
                            videosData = baseData.data ?: arrayListOf()
                            if (videosData.size > 0) {
                                initLibraryVideo(videosData)
                                initCopyVideosData()
                            } else {
                                binding.videoViewerRecycler.visibility = View.GONE
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }
        }
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


    override fun onClick(v: View?) {
        when (v) {
            binding.showSectionlayout -> {
                if (binding.spSectionlayout.isVisible && binding.spTopicLayout.isVisible) {
                    binding.spSectionlayout.visibility = View.GONE
                    binding.spTopicLayout.visibility = View.GONE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)
                } else {
                    binding.spSectionlayout.visibility = View.VISIBLE
                    binding.spTopicLayout.visibility = View.VISIBLE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_substract)
                }
            }
            binding.appBar -> {
                hideKeyboard()
            }

            binding.btnMakeChoice -> {
                if (filterValues.subject_id.isNullOrEmpty() || filterValues.subject_id =="0") {
                    viewModel.apiGetAllVideos(category_id, "", "", "", this)
                } else {
                    viewModel.apiGetAllVideos(
                        category_id,
                        filterValues.subject_id.toString(),
                        filterValues.section_id.toString(),
                        filterValues.topic_id.toString(),
                        this
                    )
                }
                binding.spSectionlayout.visibility = View.GONE
                binding.spTopicLayout.visibility = View.GONE
                binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)
            }

            binding.addVideoLayout -> {
                val intent = Intent(this, AddVideoActivity::class.java)
                startActivity(intent)
            }

            binding.root -> {
                hideKeyboard()
            }

            binding.coordinatorlayout -> {
                hideKeyboard()
            }
        }
    }


}