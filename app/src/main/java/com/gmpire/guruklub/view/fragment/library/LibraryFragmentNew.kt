package com.gmpire.guruklub.view.fragment.library

import android.content.Context
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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
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
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.game.GameChallengeItem
import com.gmpire.guruklub.data.model.library.*
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.FragmentLibraryNewBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.gameActivity.GameActivity
import com.gmpire.guruklub.view.activity.library.*
import com.gmpire.guruklub.view.activity.login.RelatedVideoActivity
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.base.DATA_VIEW_TYPE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import kotlinx.android.synthetic.main.dialog_box.view.*
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.util.*


class LibraryFragmentNew : BaseFragment(), IDatabaseCallBack,
    QuestionsFragment.LoadMoreQuestionListener {

    public fun LibraryFragmentNew() {}

    private var firstTimeLoads: Boolean = false
    private var hasNextPages: Boolean = false
    private var isBlur: Boolean = false
    private var passingFilterValues: FilterValues? = null
    lateinit var binding: FragmentLibraryNewBinding
    private lateinit var viewmodel: LibraryViewmodel
    private var filterValues: FilterValues? = FilterValues()
    private var passingfilterValue: FilterValues? = FilterValues()
    private var library: MutableLiveData<Library> = MutableLiveData()
    private var questions: MutableLiveData<ArrayList<Question>> = MutableLiveData()
    private var spinnerAdapterSubject: CustomSpinnerAdapter? = null
    private var spinnerAdapterSection: CustomSpinnerAdapter? = null
    private var spinnerAdapterTopic: CustomSpinnerAdapter? = null
    private var spinnerAdapterBatch: CustomSpinnerAdapter? = null
    private var spinnerAdapterDifficulty: CustomSpinnerAdapter? = null
    private var gistListener: GistListener? = null
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

    override fun viewRelatedTask() {
        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewmodel::class.java)

        filterValues?.category_id = ""

        binding.makeChoiceLayout.setOnClickListener(this)
        binding.btnMakeChoice.setOnClickListener(this)
        binding.btnTestExam.setOnClickListener(this)
        binding.showSectionlayout.setOnClickListener(this)
        binding.showAllTopics.setOnClickListener(this)
        binding.resumeableLayout.setOnClickListener(this)

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

        viewmodel.fetchMostPopular(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )

        val fm: FragmentManager? = fragmentManager
        val ft: FragmentTransaction? = fm?.beginTransaction()

        val arguments = Bundle()
        //questionFragment = QuestionsFragment()
        arguments.putBoolean("shouldYouCreateAChildFragment", true)

        binding.relativeLayoutGist.setOnClickListener {
            if (!gistText.isEmpty()) {
                showMyAlert(gistText)
            }
        }

        binding.buttonRelatedVideo.setOnClickListener {
            val intent = Intent(activity, RelatedVideoActivity::class.java)
            intent.putExtra("filterValues", Gson().toJson(filterValues))
            activity?.startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.rvMostPopular.isVisible) {
            viewmodel.apiShowResumeAbleState(this)
        } else {
            binding.resumeableLayout.visibility = View.GONE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GistListener) {
            gistListener = context
            gistListener?.showGist("")
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "apiGetQuestionLibrary") {
            if (firstTimeLoad) {
                firstTimeLoad = false
                return
            }
            if (isLoader) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        }

        if (key == "apiGetQuestionLibraryPageForResumeable") {
            if (firstTimeLoads) {
                firstTimeLoads = false
                return
            }
            if (isLoader) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        }

        if (key == "apiUnBookmarkQuestion" || key == "apiBookmarkQuestion") {
            if (isLoader) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_library_new, container, false)
        return binding.root
    }

    fun initMakeChoiceItemSpinner(sp: View, hint: String, item: ArrayList<BaseItem>) {
        sp as AppCompatSpinner
        val sa: CustomSpinnerAdapter
        sa =
            CustomSpinnerAdapter(
                requireActivity(),
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
                            filterValues?.subject_id =
                                spinnerAdapterSubject?.getSelectedId().toString()
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
                                    this@LibraryFragmentNew
                                )
                            }

                        }
                    }
                    binding.spSection -> {
                        if (!spinnerAdapterSection?.getSelectedId().isNullOrEmpty()) {
                            filterValues?.section_id =
                                spinnerAdapterSection?.getSelectedId().toString()
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
                                    this@LibraryFragmentNew
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
                        filterValues?.difficulty_id =
                            spinnerAdapterDifficulty?.getSelectedId().toString()
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

            "apiShowResumeAbleState" -> {
                val type = object : TypeToken<BaseModel<LastSeenLibrary>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<LastSeenLibrary>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data == null) {
                            HideRecentItem()
                        } else {
                            showRecentItem(baseData.data!!)
                        }
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
//                        showToast(context, baseData.message[0])
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

            "apiGetQuestionLibraryPageForResumeable" -> {
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
                                hasNextPages = baseData.data?.next_page != 0
                                page = baseData.data?.next_page ?: 0
                                binding.textView6.visibility = View.VISIBLE
                                binding.textView6.text = "All Questions"
                                binding.emptyView.visibility = View.GONE
                                binding.rvSentence.visibility = View.VISIBLE
                                binding.resumeableLayout.visibility = View.GONE
                            } else {
                                binding.emptyView.visibility = View.VISIBLE
                                binding.rvSentence.visibility = View.GONE
                                binding.resumeableLayout.visibility = View.GONE
                            }
                        } else {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.rvSentence.visibility = View.GONE
                            binding.resumeableLayout.visibility = View.GONE
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
                        showToast(activity, baseData.message[0])
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
                        showToast(activity, baseData.message[0])
                        libraryQuestions[favouritePosition].is_bookmarked = 0
                        binding.rvSentence.adapter?.notifyItemChanged(favouritePosition)
                    }
                }
            }
        }
    }

    private fun showRecentItem(lastSeenData: LastSeenLibrary) {
        binding.resumeableLayout.visibility = View.VISIBLE
        binding.tvValue1.text = lastSeenData.topic_name + ", " + lastSeenData.subject_name
        passingfilterValue?.subject_id = lastSeenData.subject_id
        passingfilterValue?.section_id = lastSeenData.section_id
        passingfilterValue?.topic_id = lastSeenData.topic_id
    }

    private fun HideRecentItem() {
        binding.resumeableLayout.visibility = View.GONE
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
        binding.rvMostPopular.layoutManager = LinearLayoutManager(activity)

        binding.rvMostPopular.adapter = BaseRecyclerAdapter(activity, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                model as MostPopularAndRecentLearning

                filterValues?.subject_id = model.subject_id.toString()
                filterValues?.section_id = model.section_id.toString()
                filterValues?.topic_id = model.topic_id.toString()

                val intent = Intent(activity, LibraryActivity::class.java)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("subject_name", model.subject_name)
                activity?.startActivity(intent)
                //clearFilterValues()
            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return RecentlyLearnViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_recently_learn,
                        parent,
                        false
                    ), activity
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
                    showToast(activity, "Select topic to continue")
                    return
                }
                //currenttab = binding.viewPager.currentItem
                subject_name = spinnerAdapterSubject?.getSelectedItem() ?: "No subject"

                if (filterValues != null) {
                    val intent = Intent(activity, LibraryActivity::class.java)
                    intent.putExtra("filterValues", Gson().toJson(filterValues))
                    intent.putExtra("subject_name", subject_name)
                    activity?.startActivity(intent)

                    viewmodel.apiGetResumeable(
                        filterValues?.subject_id,
                        filterValues?.section_id,
                        filterValues?.topic_id,
                        categoryId,
                        this
                    )
                    binding.spSectionlayout.visibility = View.GONE
                    binding.spTopicLayout.visibility = View.GONE
                    binding.btnlayout.visibility = View.GONE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)
                }
            }

            binding.resumeableLayout -> {
                val intent = Intent(activity, LibraryActivity::class.java)
                intent.putExtra("filterValues", Gson().toJson(passingfilterValue))
                activity?.startActivity(intent)
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

            binding.showAllTopics -> {
                val intent = Intent(activity, AllLibrarySubject::class.java)
                intent.putExtra("isSubject", "yes")
                startActivity(intent)
            }

            binding.btnTestExam -> {
                //viewmodel.checkTest()
                //return
                if (filterValues?.topic_id == "0") {
                    showToast(activity, "Select topic to continue")
                    return
                }
                if (filterValues != null) {
                    val intent = Intent(activity, GameActivity::class.java)
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

//            binding.frameLayoutOpenVideo.setOnClickListener {
//                if (binding.videoViewerLayout.visibility == View.GONE) {
//                    toggle(true)
//                    binding.imageViewPlay.setImageResource(R.drawable.ic_cross)
//                } else {
//                    toggle(false)
//                    binding.imageViewPlay.setImageResource(R.drawable.ic_play_png_icon)
//                }
//                Log.d("filter", filterValues.toString())
//            }
        } else {
            binding.relativeLayoutGist.visibility = View.GONE
        }
    }

//    private fun toggle(show: Boolean) {
//        val transition: Transition = Slide(Gravity.RIGHT)
//        transition.duration = 500
//        transition.addTarget(binding.videoViewerLayout)
//        TransitionManager.beginDelayedTransition(
//            binding.relativeContainerLinraryFragNew,
//            transition
//        )
//        binding.videoViewerLayout.visibility = if (show) View.VISIBLE else View.GONE
//    }

    private fun initLibraryVideo(libraryVideos: ArrayList<Videos>) {
        binding.videoViewerRecycler.adapter =
            BaseRecyclerAdapter(context, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return VideoThumbViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                context
                            ), R.layout.item_video_thumbs, parent, false
                        ), context!!
                    )
                }

                override fun loadMoreItem() {
                }
            }, libraryVideos)
    }

    companion object {
        @JvmStatic
        private var f = LibraryFragmentNew()
        fun newInstance(title: String): LibraryFragmentNew {
            val args = Bundle()
            args.putString(ConstantField.ACCESS_TITLE, title)
            f.arguments = args
            Log.d("TAG", f.toString())
            return f
        }
    }

    interface GistListener {
        fun showGist(gist: String)
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "checkFilterValuesAvailable" -> {
                val subCount = result as Int
                if (subCount > 0) {
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
                val intent = Intent(activity, QuestionDetailsActivity::class.java)
                intent.putExtra("page", page)
                intent.putExtra("position", questionPosition)
                intent.putExtra("filterValues", Gson().toJson(passingFilterValues))
                intent.putExtra("question_type", ConstantField.QUESTION_TYPE_LIBRARY)
                intent.putExtra("hasNextPage", hasNextPage.toString())
                startActivityForResult(intent, 2)
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
        Log.d("Test->", result.toString())
    }

    override fun onLoadMore(page: Int) {
        viewmodel.apiGetQuestionLibraryPage(
            dataManager.mPref.prefGetUserInfo().id,
            filterValues!!, page.toString(), this
        )
    }

    private fun insertEmptyQuestion(questionList: ArrayList<Question>): ArrayList<Question> {
        val questionSize = questionList.size
        if (questionSize > 5) {
            val emptyQues = Question()
            emptyQues.answer = 101 // To mark as ad question
            questionList.add(5, emptyQues)
        }
        return questionList
    }

    private fun initQuestion(libraryQuestionsLoc: ArrayList<Question>) {
        if (page != 1) {
            binding.rvSentence.post {
                this.libraryQuestions.addAll(libraryQuestionsLoc)
                binding.rvSentence.adapter?.notifyItemInserted(this.libraryQuestions.size)
            }
        } else {
            this.libraryQuestions.addAll(insertEmptyQuestion(libraryQuestionsLoc))
            binding.rvSentence.layoutManager = LinearLayoutManager(activity)
            binding.rvSentence.adapter = BaseRecyclerAdapter(context, object : IAdapterListener {
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
                                    this@LibraryFragmentNew
                                )
                            } else {
                                viewmodel.apiUnBookmarkQuestion(
                                    model.id,
                                    categoryId,
                                    this@LibraryFragmentNew
                                )
                            }
                        }
                    }
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    if (viewType == DATA_VIEW_TYPE) {
                        return QuestionsViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context),
                                R.layout.item_question_linear_flat,
                                parent,
                                false
                            ), this@LibraryFragmentNew
                        )
                    } else {
                        return NativeAdViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context),
                                R.layout.item_empty_native_ads,
                                parent,
                                false
                            ), context
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
                            filterValues!!, page.toString(), this@LibraryFragmentNew
                        )
                    } else if (hasNextPages) {
                        viewmodel.apiGetQuestionLibraryPageForResumeable(
                            dataManager.mPref.prefGetUserInfo().id,
                            passingfilterValue!!, page.toString(), this@LibraryFragmentNew
                        )
                    }
                }
            })
        }
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
            var builder = AlertDialog.Builder(requireActivity().window.context)
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