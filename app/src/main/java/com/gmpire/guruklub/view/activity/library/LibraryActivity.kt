package com.gmpire.guruklub.view.activity.library

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Filter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.SectionDTO
import com.gmpire.guruklub.data.local_db.dto.SubjectDTO
import com.gmpire.guruklub.data.local_db.dto.TopicDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.game.GameChallengeItem
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.library.Library
import com.gmpire.guruklub.data.model.library.Videos
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ActivityLibraryBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.activity.gameActivity.GameActivity
import com.gmpire.guruklub.view.activity.login.RelatedVideoActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.main.ProgressBarListener
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.adapter.TabsPagerAdapter
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBusBuilder
import com.michaelflisar.rxbus2.rx.RxBusMode
import kotlinx.android.synthetic.main.activity_library.*
import kotlinx.android.synthetic.main.dialog_box.view.*
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.util.*

class LibraryActivity : BaseActivity(), ProgressBarListener, IDatabaseCallBack,
    QuestionsFragment.LoadMoreQuestionListener {

    private val categoryId: String = "1"
    private var isBlur: Boolean = false
    private var currenttab: Int = 0
    private lateinit var questionFragment: QuestionsFragment
    private lateinit var binding: ActivityLibraryBinding
    private lateinit var viewmodel: LibraryViewmodel
    private lateinit var filterValues: FilterValues
    private var library: MutableLiveData<Library> = MutableLiveData()
    private var questions: MutableLiveData<ArrayList<Question>> = MutableLiveData()
    lateinit var spinnerAdapterSubject: CustomSpinnerAdapter
    lateinit var spinnerAdapterSection: CustomSpinnerAdapter
    lateinit var spinnerAdapterTopic: CustomSpinnerAdapter
    lateinit var spinnerAdapterBatch: CustomSpinnerAdapter
    lateinit var spinnerAdapterDifficulty: CustomSpinnerAdapter
    var isFilterDataPresent = false
    private lateinit var passedFilterValues: FilterValues
    var isFirstTimeLoad = 0
    private var gistText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_library)
        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewmodel::class.java)

        filterValues = Gson().fromJson(
            intent.getStringExtra("filterValues"),
            FilterValues::class.java
        )

        passedFilterValues = FilterValues()
        passedFilterValues.category_id = "1"
        passedFilterValues.subject_id = filterValues.subject_id
        passedFilterValues.section_id = filterValues.section_id
        passedFilterValues.topic_id = filterValues.topic_id

        viewmodel.iDatabaseCallBack = this
        viewmodel.apiGetLibrary(
            dataManager.mPref.prefGetUserInfo().id,
            filterValues, this
        )
        binding.textViewFilter.visibility = View.GONE
        binding.videoViewerLayout.visibility = View.GONE
        binding.frameLayoutOpenVideo.visibility = View.GONE
        binding.buttonRelatedVideo.visibility = View.VISIBLE
        viewmodel.checkFilterValuesAvailable()

        binding.btnTestExam.setOnClickListener(this)
        binding.btnMakeChoice.setOnClickListener(this)
        binding.showSectionlayout.setOnClickListener(this)
        binding.relativeLayoutSubject.visibility = View.VISIBLE

        viewmodel.apiGetQuestionLibrary(dataManager.mPref.prefGetUserInfo().id, filterValues, this)
        viewmodel.apiGetResumeable(
            filterValues.subject_id,
            filterValues.section_id,
            filterValues.topic_id,
            categoryId,
            this
        )
        binding.shimmerViewContainerLibraryActivity.visibility = View.VISIBLE
        binding.shimmerViewContainerLibraryActivity.startShimmerAnimation()

        val item = ArrayList<Common>()
        item.add(Common("1", "Basic"))
        item.add(Common("2", "Intermediate"))
        item.add(Common("3", "Advanced"))
    }

    override fun viewRelatedTask() {
        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewmodel::class.java)
        setToolbar(this, binding.toolbarLibrary, "Questions", true)

        val fm: FragmentManager? = supportFragmentManager
        val ft: FragmentTransaction? = fm?.beginTransaction()

        val arguments = Bundle()
        questionFragment = QuestionsFragment()
        arguments.putBoolean("shouldShowFilter", true)

        questionFragment.arguments = arguments
        ft?.add(R.id.frame_layout_library_activity, questionFragment)
        ft?.commit()

        binding.relativeLayoutGist.setOnClickListener {
            if (gistText.isNotEmpty()) {
                showMyAlert(gistText)
            }
        }

        binding.relativeLayoutSubject.setOnClickListener {
            binding.spSubject.performClick()
        }

        binding.spSectionlayout.setOnClickListener {
            binding.spSection.performClick()
        }

        binding.spTopicLayout.setOnClickListener {
            binding.spTopic.performClick()
        }

        binding.buttonRelatedVideo.setOnClickListener {
            Log.d("filter", filterValues.toString())
            val intent = Intent(this, RelatedVideoActivity::class.java)
            intent.putExtra("filterValues", Gson().toJson(passedFilterValues))
            this.startActivity(intent)

        }
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetLibrary" -> {
                val type = object : TypeToken<BaseModel<Library>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<Library>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200 && baseData.data != null) {
                        library.value = baseData.data
                        showDetails(baseData.data)
                    } else {
                        showDetails(null)
                    }
                }
            }

            "apiGetResumeable" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
//                        showToast(this, baseData.message[0])
                        Log.d("message", baseData.message[0])
                    }
                }
            }

            "apiGetQuestionLibrary" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Question>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Question>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        questions.value = baseData.data
                        if (questionFragment != null) {
                            questionFragment.loadQuestions(baseData.data)
                        }
                    }
                    binding.shimmerViewContainerLibraryActivity.stopShimmerAnimation()
                    binding.shimmerViewContainerLibraryActivity.visibility = View.GONE
                    //binding.progressBarLibrary.visibility = View.GONE
                }
            }
            "apiGetSubjectByCategoryLibrary" -> {
                val type = object : TypeToken<BaseModel<ArrayList<BaseItem>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<BaseItem>>>(
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
            "apiGetBatchByCategory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<BaseItem>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<BaseItem>>>(
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
            "apiGetSectionBySubject" -> {
                val type = object : TypeToken<BaseModel<ArrayList<BaseItem>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<BaseItem>>>(
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
                val type = object : TypeToken<BaseModel<ArrayList<BaseItem>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<BaseItem>>>(
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
                        if (isFirstTimeLoad < 3) {
                            presetValues()
                            isFirstTimeLoad++
                        }
                    }
                }
            }

        }

    }


    private fun showDetails(library: Library?) {
        if (library != null) {
            if (library.gist.isNotEmpty()) {
                gistText = library.gist
                binding.relativeLayoutGist.visibility = View.VISIBLE
            } else {
                binding.relativeLayoutGist.visibility = View.GONE
            }

            if (library.videos.size > 0) {
                initLibraryVideo(library.videos)
                binding.buttonRelatedVideo.visibility = View.VISIBLE
            }
        } else {
            binding.relativeLayoutGist.visibility = View.GONE
        }
    }

    private fun initLibraryVideo(libraryVideos: ArrayList<Videos>) {
        binding.videoViewerRecycler.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return VideoThumbViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                this@LibraryActivity
                            ), R.layout.item_video_thumbs, parent, false
                        ), this@LibraryActivity
                    )
                }

                override fun loadMoreItem() {
                }

            }, libraryVideos)
    }


    private fun showMyAlert(gist: String) {
        try {
            var builder = AlertDialog.Builder(this.window.context)
            val alert: AlertDialog? = builder.create()
            val my_view = layoutInflater.inflate(R.layout.dialog_box, null)
            alert?.setView(my_view)
            alert?.setCancelable(false)

            my_view.ivCross.setOnClickListener {
                alert?.dismiss()
            }

            my_view.tvGist.text = Html.fromHtml(gist)
            my_view.tvGist.movementMethod = LinkMovementMethod()

            alert?.show()
            alert?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun initMakeChoiceItemSpinner(sp: View, hint: String, item: ArrayList<BaseItem>) {
        sp as AppCompatSpinner
        val sa: CustomSpinnerAdapter
        sa =
            CustomSpinnerAdapter(
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
        sp.setAdapter(sa.getAdapter(isBlur))
        sp.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                sa.setPosition(position)
                when (sp) {
                    binding.spSubject -> {
                        if (!spinnerAdapterSubject.getSelectedId().isNullOrEmpty()) {
                            filterValues.clearValues()
                            if (filterValues.subject_id != "0") {
                                binding.sectionIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                                isBlur = true
                            } else {
                                binding.sectionIcon.setImageResource(R.drawable.blur_down_arrow)
                                isBlur = false
                            }
                            filterValues.subject_id = spinnerAdapterSubject.getSelectedId()
                            if (isFilterDataPresent) {
                                viewmodel.dataGetAllSectionListBySubject(
                                    filterValues.subject_id ?: ""
                                )
                            } else {
                                viewmodel.apiGetSectionBySubject(
                                    spinnerAdapterSubject.getSelectedId() ?: "",
                                    this@LibraryActivity
                                )
                            }
                        }
                    }
                    binding.spSection -> {
                        if (!spinnerAdapterSection.getSelectedId().isNullOrEmpty()) {
                            filterValues.section_id = spinnerAdapterSection.getSelectedId()
                            if (filterValues.section_id == "0") {
                                binding.topicIcon.setImageResource(R.drawable.blur_down_arrow)
                                isBlur = false
                            } else {
                                binding.topicIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                                isBlur = true
                            }
                            if (isFilterDataPresent) {
                                viewmodel.dataGetAllTopicListBySection(
                                    filterValues.section_id ?: ""
                                )
                            } else {
                                viewmodel.apiGetTopicBySection(
                                    spinnerAdapterSection.getSelectedId() ?: "",
                                    this@LibraryActivity
                                )
                            }
                        }
                    }
                    binding.spTopic -> {
                        filterValues.topic_id = spinnerAdapterTopic.getSelectedId()
                    }
                    binding.spBatch -> {
                        filterValues.batch_id = spinnerAdapterBatch.getSelectedId()
                    }
                    binding.spDifficulty -> {
                        filterValues.difficulty_id = spinnerAdapterDifficulty.getSelectedId()
                    }

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        })
        when (sp) {
            binding.spSubject -> spinnerAdapterSubject = sa
            binding.spSection -> spinnerAdapterSection = sa
            binding.spTopic -> spinnerAdapterTopic = sa
            binding.spBatch -> spinnerAdapterBatch = sa
            binding.spDifficulty -> spinnerAdapterDifficulty = sa
        }

    }


    override fun onLoading(isLoader: Boolean, key: String) {
        when (key) {
            "apiGetLibrary" -> {
            }
            "apiGetQuestionLibrary" -> {
                if (!isLoader) {
                    hideProgressDialog()
                }
            }
        }
    }

    override fun onError(err: Throwable, key: String) {
        super.onError(err, key)
        showToast(this, err.message.toString())
        binding.shimmerViewContainerLibraryActivity.stopShimmerAnimation()
        binding.shimmerViewContainerLibraryActivity.visibility = View.GONE
        onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v) {

            binding.showSectionlayout -> {
                if (binding.spSectionlayout.isVisible && binding.spTopicLayout.isVisible && binding.btnlayout.isVisible) {
                    binding.spSectionlayout.visibility = View.GONE
                    binding.spTopicLayout.visibility = View.GONE
                    binding.btnlayout.visibility = View.GONE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)

                } else if (!binding.spTopicLayout.isVisible && !binding.spSectionlayout.isVisible && !binding.btnlayout.isVisible) {
                    binding.spSectionlayout.visibility = View.VISIBLE
                    binding.spTopicLayout.visibility = View.VISIBLE
                    binding.btnlayout.visibility = View.VISIBLE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_substract)
                }
            }

            binding.btnTestExam -> {
                if (filterValues.topic_id == "0") {
                    showToast(this, "Select topic to continue")
                    return
                }
                if (filterValues != null) {
                    val intent = Intent(this, GameActivity::class.java)
                    val customGameMode =
                        GameChallengeItem("-2", "Topic Exam", "-2", "test")
                    intent.putExtra("game_mode", customGameMode)
                    intent.putExtra("filter_values", Gson().toJson(filterValues))
                    startActivity(intent)
                }
            }
            binding.btnMakeChoice -> {
                if (filterValues.topic_id == "0") {
                    showToast(this, "Select topic to continue")
                    return
                }

                binding.llMakeChoice.visibility = View.VISIBLE
                binding.shimmerViewContainerLibraryActivity.visibility = View.VISIBLE
                binding.shimmerViewContainerLibraryActivity.startShimmerAnimation()

                if (questionFragment != null) {
                    questionFragment.hideQuestionList()
                }

                binding.ivDropdownArrow.rotation = 360.0.toFloat()
                currenttab = binding.viewPager.currentItem
                viewmodel.apiGetQuestionLibrary(
                    dataManager.mPref.prefGetUserInfo().id,
                    filterValues, this
                )

                viewmodel.apiGetLibrary(
                    dataManager.mPref.prefGetUserInfo().id,
                    filterValues, this
                )

                viewmodel.apiGetResumeable(
                    filterValues.subject_id,
                    filterValues.section_id,
                    filterValues.topic_id,
                    categoryId,
                    this
                )

                binding.btnlayout.visibility = View.GONE
                binding.spSectionlayout.visibility = View.GONE
                binding.spTopicLayout.visibility = View.GONE
                binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)

            }
        }
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "checkFilterValuesAvailable" -> {
                val subCount = result as Int
                if (subCount > 0) {
                    isFilterDataPresent = true
                    viewmodel.dataGetSubjectListByCategoryLibrary()
                } else {
                    viewmodel.apiGetSubjectByCategoryLibrary(
                        dataManager.mPref.prefGetUserInfo().category_id.toString(),
                        this
                    )
                }
            }
            "dataGetSubjectListByCategoryLibrary" -> {
                val subjectListDTO = result as List<SubjectDTO>
                val subjectList = SubjectDTO.toSubjects(subjectListDTO)

                initMakeChoiceItemSpinner(
                    binding.spSubject,
                    "Select Subject",
                    subjectList as ArrayList<BaseItem>
                )
            }
            "dataGetAllSectionListBySubject" -> {
                val sectionListDTO = result as List<SectionDTO>
                val sectionList = SectionDTO.toSections(sectionListDTO)
                initMakeChoiceItemSpinner(
                    binding.spSection,
                    "Select Section",
                    sectionList as ArrayList<BaseItem>
                )
                if (isFirstTimeLoad == 0) {
                    presetValues()
                    isFirstTimeLoad++
                }
            }
            "dataGetAllTopicListBySection" -> {
                val topicListDTO = result as List<TopicDTO>
                val topicList = TopicDTO.toTopics(topicListDTO)
                initMakeChoiceItemSpinner(
                    binding.spTopic,
                    "Select Topic",
                    topicList as ArrayList<BaseItem>
                )
                if (isFirstTimeLoad < 3) {
                    presetValues()
                    isFirstTimeLoad++
                }
            }
        }
    }

    private fun presetValues() {
        if (!passedFilterValues.subject_id.isNullOrEmpty()) {
            try {
                val pos =
                    spinnerAdapterSubject.getPositionById(passedFilterValues.subject_id)
                binding.spSubject.setSelection(pos)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        if (!passedFilterValues.section_id.isNullOrEmpty()) {
            try {
                val pos =
                    spinnerAdapterSection.getPositionById(passedFilterValues.section_id)
                binding.spSection.setSelection(pos)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        if (!passedFilterValues.topic_id.isNullOrEmpty()) {
            try {
                val pos = spinnerAdapterTopic.getPositionById(passedFilterValues.topic_id)
                binding.spTopic.setSelection(pos)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    override fun onFailedDB(result: Any, optName: String) {

    }

    override fun showProgress() {
        binding.progressBarLibrary.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        binding.progressBarLibrary.visibility = View.GONE
    }

    override fun onLoadMore(page: Int) {
        viewmodel.apiGetQuestionLibraryPage(
            dataManager.mPref.prefGetUserInfo().id,
            filterValues, page.toString(), this
        )
        showProgress()
    }

}
