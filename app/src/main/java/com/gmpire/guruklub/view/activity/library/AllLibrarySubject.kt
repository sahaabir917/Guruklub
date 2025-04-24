package com.gmpire.guruklub.view.activity.library

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.SectionDTO
import com.gmpire.guruklub.data.local_db.dto.SubjectDTO
import com.gmpire.guruklub.data.local_db.dto.TopicDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.SubjectSectionTopicModel
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.game.GameChallengeItem
import com.gmpire.guruklub.data.model.library.*
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ActivityAllLibrarySubjectBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.gameActivity.GameActivity
import com.gmpire.guruklub.view.activity.login.RelatedVideoActivity
import com.gmpire.guruklub.view.adapter.AllSubjectViewHolder
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.adapter.QuestionsViewHolderForAllLibrary
import com.gmpire.guruklub.view.base.*
import com.gmpire.guruklub.view.fragment.library.RecentlyLearnViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import kotlinx.android.synthetic.main.dialog_box.view.*
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.lang.Exception
import java.util.ArrayList
import java.util.HashMap

class AllLibrarySubject : BaseActivity(), IDatabaseCallBack {
    private var subject_id: String = ""
    private val subjectTopicLists: ArrayList<BaseItem> = ArrayList<BaseItem>()
    private var subjectTopicList: ArrayList<SubjectSectionTopicModel> =
        ArrayList<SubjectSectionTopicModel>()
    private var isBlur: Boolean = false
    private var passingFilterValues: FilterValues? = null
    private lateinit var viewmodel: LibraryViewmodel
    private var filterValues: FilterValues? = FilterValues()
    private var currenttab: Int = 0
    private lateinit var questionFragment: QuestionsFragment
    private lateinit var studyFragment: StudyFragment
    lateinit var binding: ActivityAllLibrarySubjectBinding
    private var library: MutableLiveData<Library> = MutableLiveData()
    private var questions: MutableLiveData<ArrayList<Question>> = MutableLiveData()
    private var spinnerAdapterSubject: CustomSpinnerAdapter? = null
    private var spinnerAdapterSection: CustomSpinnerAdapter? = null
    private var spinnerAdapterTopic: CustomSpinnerAdapter? = null
    private var spinnerAdapterBatch: CustomSpinnerAdapter? = null
    private var spinnerAdapterDifficulty: CustomSpinnerAdapter? = null
    var subject_name: String = ""
    var page: Int = 1
    var isFilterDataPresent = false
    var firstTimeLoad = true

    private var favouritePosition: Int = -1
    private var libraryQuestions: ArrayList<Question> = arrayListOf()
    private var categoryId: String = "1"
    var questionPosition: Int = -1
    private var hasNextPage: Boolean = false
    private var gistText = ""
    private var isSubject: String? = null
    private var isSection: String? = null
    private var isTopic: String? = null
    private var sectionList: ArrayList<SubjectSectionTopicModel> =
        ArrayList<SubjectSectionTopicModel>()
    private var topicList: ArrayList<SubjectSectionTopicModel> =
        ArrayList<SubjectSectionTopicModel>()
    private var section_id: String = ""

    override fun viewRelatedTask() {
        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewmodel::class.java)
        filterValues?.category_id = ""

        binding.makeChoiceLayout.setOnClickListener(this)
        binding.btnMakeChoice.setOnClickListener(this)
        binding.btnTestExam.setOnClickListener(this)
        binding.showSectionlayout.setOnClickListener(this)


        if (intent.hasExtra("isSubject")) {
            setToolbar(this, binding.toolbar, "All Subjects", true)
            isSubject = intent.getStringExtra("isSubject")
            if (isSubject == "yes") {
                viewmodel.apiGetSubjectByCategoryLibraryForShow(
                    "1",
                    this
                )
            }
        }

        if (intent.hasExtra("isSection")) {
            isSection = intent.getStringExtra("isSection")
            if (isSection == "yes") {
                setToolbar(this, binding.toolbar, "All Sections", true)
                subject_id = intent.getStringExtra("subject_id") ?: "1"
                subject_name = intent.getStringExtra("subject_name")
                viewmodel.apiGetSectionBySubjectLibraryForShow(
                    subject_id,
                    this
                )
            }
        }


        if (intent.hasExtra("isTopic")) {
            isTopic = intent.getStringExtra("isTopic")
            if (isTopic == "yes") {
                setToolbar(this, binding.toolbar, "All Topics", true)
                section_id = intent.getStringExtra("section_id") ?: "1"
                subject_id = intent.getStringExtra("subject_id") ?: "1"
                subject_name = intent.getStringExtra("subject_name")
                viewmodel.apiGetTopicBySectionLibraryForShow(
                    section_id,
                    this
                )
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

        binding.ivDropdownArrow.rotation = 360.0.toFloat()

        val item = ArrayList<Common>()
        item.add(Common("1", "Basic"))
        item.add(Common("2", "Intermediate"))
        item.add(Common("3", "Advanced"))

        viewmodel.iDatabaseCallBack = this
        viewmodel.checkFilterValuesAvailable()

        val arguments = Bundle()
        //questionFragment = QuestionsFragment()
        arguments.putBoolean("shouldYouCreateAChildFragment", true)

        binding.relativeLayoutGist.setOnClickListener {
            if (!gistText.isEmpty()) {
                showMyAlert(gistText)
            }
        }

        binding.buttonRelatedVideo.setOnClickListener {
            val intent = Intent(this, RelatedVideoActivity::class.java)
            intent.putExtra("filterValues", Gson().toJson(filterValues))
            this.startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun navigateToHome() {

    }


    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "apiGetQuestionLibrary") {
            if (firstTimeLoad) {
                firstTimeLoad = false
                return
            }
            if (isLoader) {
                showProgressDialog("Please Wait")
            } else {
                hideProgressDialog()
            }
        }

        if (key == "apiUnBookmarkQuestion" || key == "apiBookmarkQuestion") {
            if (isLoader) {
                showProgressDialog("please wait")
            } else {
                hideProgressDialog()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_all_library_subject)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_library_subject)
    }

    fun initMakeChoiceItemSpinner(sp: View, hint: String, item: ArrayList<BaseItem>) {
        sp as AppCompatSpinner
        val sa: CustomSpinnerAdapter = CustomSpinnerAdapter(
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
        Log.d("Check->", "Subject populated successful!")
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
                        if (!spinnerAdapterSubject?.getSelectedId().isNullOrEmpty()) {
                            filterValues?.clearValues()
                            filterValues?.subject_id = spinnerAdapterSubject?.getSelectedId().toString()
                            if (filterValues?.subject_id != "0") {
                                binding.sectionIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                                isBlur = true
                            } else {
                                binding.sectionIcon.setImageResource(R.drawable.blur_down_arrow)
                                isBlur = false
                            }
                            if (isFilterDataPresent) {
                                viewmodel.dataGetAllSectionListBySubject(
                                    filterValues?.subject_id ?: ""
                                )
                            } else {
                                viewmodel.apiGetSectionBySubject(
                                    spinnerAdapterSubject?.getSelectedId() ?: "",
                                    this@AllLibrarySubject
                                )
                            }

                        }
                    }
                    binding.spSection -> {
                        if (!spinnerAdapterSection?.getSelectedId().isNullOrEmpty()) {
                            filterValues?.section_id = spinnerAdapterSection?.getSelectedId().toString()
                            if (filterValues?.section_id == "0") {
                                binding.topicIcon.setImageResource(R.drawable.blur_down_arrow)
                                isBlur = false
                            } else {
                                binding.topicIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                                isBlur = true
                            }
                            if (isFilterDataPresent) {
                                viewmodel.dataGetAllTopicListBySection(
                                    filterValues?.section_id ?: ""
                                )
                            } else {
                                viewmodel.apiGetTopicBySection(
                                    spinnerAdapterSection?.getSelectedId() ?: "",
                                    this@AllLibrarySubject
                                )
                            }
                        }
                    }
                    binding.spTopic -> {
                        filterValues?.topic_id = spinnerAdapterTopic?.getSelectedId().toString()
                    }
                    binding.spBatch -> {
                        filterValues?.batch_id = spinnerAdapterBatch?.getSelectedId().toString()
                    }
                    binding.spDifficulty -> {
                        filterValues?.difficulty_id = spinnerAdapterDifficulty?.getSelectedId().toString()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        when (sp) {
            binding.spSubject -> spinnerAdapterSubject = sa
            binding.spSection -> spinnerAdapterSection = sa
            binding.spTopic -> spinnerAdapterTopic = sa
            binding.spBatch -> spinnerAdapterBatch = sa
            binding.spDifficulty -> spinnerAdapterDifficulty = sa
        }
    }

    fun blinkTestExamButton() {
        Handler(Looper.getMainLooper()).postDelayed({
            val anim: Animation = AlphaAnimation(0.2f, 1.0f)
            anim.duration = 100 //You can manage the blinking time with this parameter

            anim.startOffset = 200
            anim.repeatMode = Animation.REVERSE
            anim.repeatCount = Animation.RESTART
            binding.btnTestExam.startAnimation(anim)
        }, 500)
    }


    private fun initAllSections() {

        binding.rvMostPopular.layoutManager = LinearLayoutManager(this)

        binding.rvMostPopular.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                    val intent =
                        Intent(this@AllLibrarySubject, AllLibrarySubject::class.java)
                    intent.putExtra("section_id", sectionList[position].id)
                    intent.putExtra("subject_id", subject_id)
                    intent.putExtra("subject_name", subject_name)
                    intent.putExtra("isTopic", "yes")
                    startActivity(intent)
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return AllSubjectViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_subject_topic_list,
                            parent,
                            false
                        ), this@AllLibrarySubject
                    )
                }

                override fun loadMoreItem() {
                }
            }, sectionList)
    }


    private fun initAllTopics() {

        binding.rvMostPopular.layoutManager = LinearLayoutManager(this)

        binding.rvMostPopular.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                    filterValues?.clearValues()
                    filterValues?.subject_id = subject_id
                    filterValues?.section_id = section_id
                    filterValues?.topic_id = topicList[position].id


//                    binding.shimmerViewContainerLibrary.visibility = View.VISIBLE
//                    binding.shimmerViewContainerLibrary.startShimmerAnimation()
//                    viewmodel.apiGetQuestionLibraryPage(
//                        dataManager.mPref.prefGetUserInfo().id,
//                        filterValues!!, page.toString(), this@AllLibraryTopicActivity
//                    )

                    viewmodel.apiGetResumeable(
                        filterValues?.subject_id,
                        filterValues?.section_id,
                        filterValues?.topic_id,
                        categoryId,
                        this@AllLibrarySubject
                    )

                    val intent = Intent(this@AllLibrarySubject, LibraryActivity::class.java)
                    intent.putExtra("filterValues", Gson().toJson(filterValues))
                    intent.putExtra("subject_name", subject_name)
                    startActivity(intent)

//                    binding.rvMostPopular.visibility = View.GONE
//                    binding.textView6.visibility = View.VISIBLE
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return AllSubjectViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_subject_topic_list,
                            parent,
                            false
                        ), this@AllLibrarySubject
                    )
                }

                override fun loadMoreItem() {
                }
            }, topicList)
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


            "apiGetTopicBySectionLibraryForShow" -> {
                Log.d("Check->", "Subject fetched successful!")
                val type =
                    object : TypeToken<BaseModel<ArrayList<SubjectSectionTopicModel>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<SubjectSectionTopicModel>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            topicList = baseData.data as ArrayList<SubjectSectionTopicModel>
                            initAllTopics()
                        }
                    }
                }
            }

            "apiGetSectionBySubjectLibraryForShow" -> {
                Log.d("Check->", "Subject fetched successful!")
                val type =
                    object : TypeToken<BaseModel<ArrayList<SubjectSectionTopicModel>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<SubjectSectionTopicModel>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            sectionList = baseData.data as ArrayList<SubjectSectionTopicModel>
                            initAllSections()
                        }
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
                binding.shimmerViewContainerLibrary.stopShimmerAnimation()
                binding.shimmerViewContainerLibrary.visibility = View.GONE
                val type =
                    object : TypeToken<BaseModel<PaginationModel<ArrayList<Question>>>>() {}.type
                result.data?.body()?.let {
                    if (result.data.body() != null) {
                        val baseData =
                            Gson().fromJson<BaseModel<PaginationModel<ArrayList<Question>>>>(
                                result.data.body()?.string(),
                                type
                            )
                        if (baseData.status_code == 200 && baseData.data?.data != null) {
                            if (baseData.data?.data?.size ?: 0 > 0) {
                                initQuestion(baseData.data?.data!!)
                                hasNextPage = baseData.data?.next_page != 0
                                page = baseData.data?.next_page ?: 0
                                binding.textView6.text = "Questions"
                                binding.emptyView.visibility = View.GONE
                                binding.rvSentence.visibility = View.VISIBLE
                            } else {
                                binding.emptyView.visibility = View.VISIBLE
                                binding.rvSentence.visibility = View.GONE
                                //binding.makeChoiceLayout.visibility = View.GONE
                            }
                        } else {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.rvSentence.visibility = View.GONE
                            // binding.makeChoiceLayout.visibility = View.GONE
                        }
                    }
                    binding.progressBarLibrary.visibility = View.GONE
                }
            }
            "apiGetSubjectByCategoryLibrary" -> {
                Log.d("Check->", "Subject fetched successful!")
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

            "apiGetSubjectByCategoryLibraryForShow" -> {
                Log.d("Check->", "Subject fetched successful!")
                val type =
                    object : TypeToken<BaseModel<ArrayList<SubjectSectionTopicModel>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<SubjectSectionTopicModel>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            subjectTopicList = baseData.data as ArrayList<SubjectSectionTopicModel>
                            initAllSubject()

                        }
                    }
                }
            }

            "apiGetBatchByCategory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            /* initMakeChoiceItemSpinner(
                                 binding.spBatch,
                                 "Select Batch",
                                 baseData.data ?: arrayListOf()
                             )*/
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
                    }
                }
            }
            "fetchMostPopular" -> {
                val type =
                    object : TypeToken<BaseModel<ArrayList<MostPopularAndRecentLearning>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<MostPopularAndRecentLearning>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data?.size ?: 0 > 0) {
                            initMostPopulars(baseData.data!!)
                            binding.mostPopularLayout.visibility = View.VISIBLE
                        }
                    }
                }
            }
            "apiBookmarkQuestion" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                        showToast(this, baseData.message[0])
                        libraryQuestions[favouritePosition].is_bookmarked = 1
                        binding.rvSentence.adapter?.notifyItemChanged(favouritePosition)
                    }
                }
            }
            "apiUnBookmarkQuestion" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                        UpdateClass()
                    )
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        showToast(this, baseData.message[0])
                        libraryQuestions[favouritePosition].is_bookmarked = 0
                        binding.rvSentence.adapter?.notifyItemChanged(favouritePosition)
                    }
                }
            }

            "apiGetQuestionLibrary" -> {
                binding.shimmerViewContainerLibrary.stopShimmerAnimation()
                binding.shimmerViewContainerLibrary.visibility = View.GONE
                val type =
                    object : TypeToken<BaseModel<PaginationModel<ArrayList<Question>>>>() {}.type
                result.data?.body()?.let {
                    if (result.data.body() != null) {
                        val baseData =
                            Gson().fromJson<BaseModel<PaginationModel<ArrayList<Question>>>>(
                                result.data.body()?.string(),
                                type
                            )
                        if (baseData.status_code == 200 && baseData.data?.data != null) {
                            if (baseData.data?.data?.size ?: 0 > 0) {
                                hasNextPage = baseData.data?.next_page != 0
                                page = baseData.data?.next_page ?: 0
                                binding.textView6.visibility = View.VISIBLE
                                binding.textView6.text = "All Questions"
                                binding.emptyView.visibility = View.GONE
                                binding.rvSentence.visibility = View.VISIBLE
                            } else {
                                binding.emptyView.visibility = View.VISIBLE
                                binding.rvSentence.visibility = View.GONE
                                //binding.makeChoiceLayout.visibility = View.GONE
                            }
                        } else {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.rvSentence.visibility = View.GONE
                            // binding.makeChoiceLayout.visibility = View.GONE
                        }
                    }
                    binding.progressBarLibrary.visibility = View.GONE
                }
            }
        }
    }

    private fun initAllSubject() {

        binding.rvMostPopular.layoutManager = LinearLayoutManager(this)

        binding.rvMostPopular.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                    val intent =
                        Intent(this@AllLibrarySubject, AllLibrarySubject::class.java)
                    intent.putExtra("subject_id", subjectTopicList[position].id)
                    intent.putExtra("subject_name", subjectTopicList[position].name)
                    intent.putExtra("isSection", "yes")
                    startActivity(intent)
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return AllSubjectViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_subject_topic_list,
                            parent,
                            false
                        ), this@AllLibrarySubject
                    )
                }

                override fun loadMoreItem() {
                }
            }, subjectTopicList)
    }

    private fun initQuestion(libraryQuestionsLoc: ArrayList<Question>) {
        if (page != 1) {
            binding.rvSentence.post {
                this.libraryQuestions.addAll(libraryQuestionsLoc)
                binding.rvSentence.adapter?.notifyItemInserted(this.libraryQuestions.size)
            }

        } else {
            this.libraryQuestions.addAll(insertEmptyQuestion(libraryQuestionsLoc))
            binding.rvSentence.layoutManager = LinearLayoutManager(this)
            binding.rvSentence.adapter = BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                    model as Question
                    when (view.id) {
                        R.id.question_root_layout -> {
                            Log.d("Test->", "Clicked!!")
                            viewmodel.dataDeleteAllTempQuestions()
                            viewmodel.insertAllTemp(libraryQuestions.filter { it.answer != 101 })
                            questionPosition = if (position > ConstantField.NATIVE_AD_INTERVAL) {
                                position - 1
                            } else {
                                position
                            }
                        }
                        R.id.bookmark_iv -> {
                            favouritePosition = position
                            if (model.is_bookmarked == 0) {
                                viewmodel.apiBookmarkQuestion(
                                    model.id,
                                    categoryId,
                                    this@AllLibrarySubject
                                )
                            } else {
                                viewmodel.apiUnBookmarkQuestion(
                                    model.id,
                                    categoryId,
                                    this@AllLibrarySubject
                                )
                            }
                        }
                    }
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    if (viewType == DATA_VIEW_TYPE) {
                        return QuestionsViewHolderForAllLibrary(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context),
                                R.layout.item_question_linear_flat,
                                parent,
                                false
                            ), this@AllLibrarySubject
                        )
                    } else {
                        return NativeAdViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context),
                                R.layout.item_empty_native_ads,
                                parent,
                                false
                            ), this@AllLibrarySubject
                        )
                    }
                }

                override fun loadMoreItem() {

                }
            }, this.libraryQuestions)

            binding.rvSentence.addOnScrollListener(object :
                EndlessRecyclerOnScrollListener(binding.rvSentence.layoutManager as LinearLayoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    if (hasNextPage) {
                        viewmodel.apiGetQuestionLibraryPage(
                            dataManager.mPref.prefGetUserInfo().id,
                            filterValues!!, page.toString(), this@AllLibrarySubject
                        )
                    }
                }
            })
        }
    }

    override fun onErrorCode(
        code: Int,
        message: String,
        result: LiveDataResult<Response<ResponseBody>>
    ) {
        super.onErrorCode(code, message, result)
        Log.d("Check->", message)
        //binding.progressBarLibrary.visibility = View.GONE
        binding.shimmerViewContainerLibrary.stopShimmerAnimation()
        binding.shimmerViewContainerLibrary.visibility = View.GONE
    }

    private fun initMostPopulars(mostPopulars: ArrayList<MostPopularAndRecentLearning>) {

        binding.rvMostPopular.minimumHeight = mostPopulars.size * 100
        binding.rvMostPopular.layoutManager = LinearLayoutManager(this)

        binding.rvMostPopular.adapter = BaseRecyclerAdapter(this, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                model as MostPopularAndRecentLearning

                filterValues?.subject_id = model.subject_id.toString()
                filterValues?.section_id = model.section_id.toString()
                filterValues?.topic_id = model.topic_id.toString()

                val intent = Intent(this@AllLibrarySubject, LibraryActivity::class.java)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("subject_name", model.subject_name)
                startActivity(intent)
                //clearFilterValues()
            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return RecentlyLearnViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_recently_learn,
                        parent,
                        false
                    ), this@AllLibrarySubject
                )
            }

            override fun loadMoreItem() {

            }
        }, mostPopulars)
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.btnMakeChoice -> {
                firstTimeLoad = true
                binding.emptyView.visibility = View.GONE
                if (filterValues?.topic_id == "0") {
                    showToast(this, "Select topic to continue")
                    return
                }
                //currenttab = binding.viewPager.currentItem
                subject_name = spinnerAdapterSubject?.getSelectedItem() ?: "No subject"



                if (filterValues != null) {
//                    hideQuestionList()
//                    binding.shimmerViewContainerLibrary.visibility = View.VISIBLE
//                    binding.shimmerViewContainerLibrary.startShimmerAnimation()
//                    binding.showAllTopics.visibility = View.GONE
//                    page = 1
//                    viewmodel.apiGetQuestionLibraryPage(
//                        dataManager.mPref.prefGetUserInfo().id,
//                        filterValues!!, page.toString(), this
//                    )

                    val intent = Intent(this, LibraryActivity::class.java)
                    intent.putExtra("filterValues", Gson().toJson(filterValues))
                    intent.putExtra("subject_name", subject_name)
                    startActivity(intent)

//                    viewmodel.apiGetLibrary(
//                        dataManager.mPref.prefGetUserInfo().id,
//                        filterValues!!, this
//                    )

                    viewmodel.apiGetResumeable(
                        filterValues?.subject_id,
                        filterValues?.section_id,
                        filterValues?.topic_id,
                        categoryId,
                        this
                    )

//                    binding.textViewFilter.visibility = View.GONE
//                    binding.rvMostPopular.visibility = View.GONE
//
//                    binding.videoViewerLayout.visibility = View.GONE
//                    binding.buttonRelatedVideo.visibility = View.GONE
//
                    binding.spSectionlayout.visibility = View.GONE
                    binding.spTopicLayout.visibility = View.GONE
                    binding.btnlayout.visibility = View.GONE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)

                }
            }

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

                if (filterValues?.topic_id == "0") {
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
                                this@AllLibrarySubject
                            ), R.layout.item_video_thumbs, parent, false
                        ), this@AllLibrarySubject
                    )
                }

                override fun loadMoreItem() {
                }
            }, libraryVideos)
    }


    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "checkFilterValuesAvailable" -> {
                val subCount = result as Int
                if (subCount > 0) {
                    //initMakeChoiceItemSpinner()
                    isFilterDataPresent = true
                    viewmodel.dataGetSubjectListByCategoryLibrary()
                } else {
                    Log.d("Check->", "No local value, fetching....")
                    viewmodel.apiGetSubjectByCategoryLibrary(
                        "1",
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

            }
            "dataGetAllTopicListBySection" -> {
                val topicListDTO = result as List<TopicDTO>
                val topicList = TopicDTO.toTopics(topicListDTO)
                initMakeChoiceItemSpinner(
                    binding.spTopic,
                    "Select Topic",
                    topicList as ArrayList<BaseItem>
                )
            }
            "checkTest" -> {
                Log.d("Test->", result.toString())
            }
            "checkSecTest" -> {
                val sectionListDTO = result as List<SectionDTO>
                val sectionList = SectionDTO.toSections(sectionListDTO)
                sectionList.forEach {
                    Log.d("Test->", it.name.toString())
                }
            }
            "insertAllTemp" -> {
                val intent = Intent(
                    this,
                    QuestionDetailsActivity::class.java
                )
                intent.putExtra("page", page.toString())
                intent.putExtra("position", questionPosition)
                intent.putExtra(
                    "filterValues", Gson().toJson(passingFilterValues)
                )
                intent.putExtra("isLibraryQuestion", "yes")
                intent.putExtra("hasNextPage", hasNextPage.toString())
                //startActivity(intent)
                startActivityForResult(intent, 2)
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
        Log.d("Test->", result.toString())
    }


    fun loadQuestions(questions: ArrayList<Question>?) {
        if (binding != null) {
            //this.loadMoreQuestionListener = loadMoreQuestionListener
            if (questions!!.isNotEmpty()) {
//                initQuestion(questions)
                binding.emptyView.visibility = View.GONE
                binding.rvSentence.visibility = View.VISIBLE
            } else {
                binding.rvSentence.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }

    private fun insertEmptyQuestion(questionList: ArrayList<Question>): ArrayList<Question> {
        /*var placement = ConstantField.NATIVE_AD_INTERVAL
        var increment = 0
        if (libraryQuestions.isNotEmpty())
            placement =
                ConstantField.NATIVE_AD_INTERVAL - (libraryQuestions.size % (ConstantField.NATIVE_AD_INTERVAL + 1))*/

        val questionSize = questionList.size
        if (questionSize > 5) {
            val emptyQues = Question()
            emptyQues.answer = 101 // To mark as ad question
            questionList.add(5, emptyQues)
        }


        /* while (questionSize > placement) {
             val emptyQues = Question()
             emptyQues.answer = 101 // To mark as ad question
             questionList.add(placement + increment, emptyQues)
             increment++
             placement += ConstantField.NATIVE_AD_INTERVAL
         }*/
        return questionList
    }


    fun hideQuestionList() {
        binding.emptyView.visibility = View.GONE
        binding.rvSentence.visibility = View.GONE
        libraryQuestions.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            try {
                val bookmarkMap = data!!.getSerializableExtra("bookmark_map") as HashMap<Int, Int>
                refineQuestionList(bookmarkMap)
            } catch (ex: Exception) {
                print("$ex----Bookmark Map isnull")
            }
        }
    }

    private fun refineQuestionList(bookmarkMap: HashMap<Int, Int>) {
        if (bookmarkMap.isNotEmpty()) {
            bookmarkMap.forEach {
                val pos = if (it.key >= ConstantField.NATIVE_AD_INTERVAL) {
                    it.key + 1
                } else {
                    it.key
                }
                libraryQuestions[pos].is_bookmarked = it.value
                binding.rvSentence.adapter?.notifyItemChanged(pos)
            }
        }
    }

    private fun showMyAlert(gist: String) {
        try {
            var builder = AlertDialog.Builder(this!!.window.context)
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
}