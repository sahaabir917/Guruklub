package com.gmpire.guruklub.view.fragment.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.*
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.categoryAndSubject.*
import com.gmpire.guruklub.data.model.heartsettings.HeartVariation
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.FragmentHomeBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.BottomSheet.AnswerDescriptionBottomSheet
import com.gmpire.guruklub.view.BottomSheet.FilterBottomSheet
import com.gmpire.guruklub.view.BottomSheet.ReportBottomSheet
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import com.gmpire.guruklub.view.activity.login.RelatedVideoActivity
import com.gmpire.guruklub.view.activity.question.QuestionAddActivity
import com.gmpire.guruklub.view.activity.questionShare.QuestionShareActivity
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.dialog.ImageViewDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import okhttp3.ResponseBody
import org.apache.http.impl.cookie.DateParseException
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList

const val GAME_HEART_PLUS = 1

class HomeFragment : BaseFragment(), IDatabaseCallBack,
    HomeQuestionsAdapter.OnActionListener,
    FilterBottomSheet.IBottomSheetDialogClicked, ReportBottomSheet.IBottomSheetDialogClicked,
    AnswerDescriptionBottomSheet.Listener {

    public fun HomeFragment() {}

    companion object {
        private var f = HomeFragment()
        fun newInstance(title: String): HomeFragment {
            val args = Bundle()
            args.putString(ConstantField.ACCESS_TITLE, title)
            if (!f.isAdded) {
                f.arguments = args
                Log.d("TAG", f.toString())
            }
            return f
        }

        fun newInstance(title: String, questionString: String): HomeFragment {
            val args = Bundle()
            args.putString(ConstantField.ACCESS_TITLE, title)
            args.putString("question_object", questionString)
            f.arguments = args
            Log.d("TAG", f.toString())
            return f
        }
    }

    private var filterValues: FilterValues? = null
    private var mCurrentFragmentPosition: Int = 0
    private var adaper: HomeQuestionsAdapter? = null
    private lateinit var answerDescriptionBottomSheet: AnswerDescriptionBottomSheet

    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var filterBottomsheet: FilterBottomSheet
    private lateinit var reportBottomSheet: ReportBottomSheet
    lateinit var binding: FragmentHomeBinding
    var questions = ArrayList<Question>()
    var questionFromNotification: Question? = null

    private var showLoginDialogListener: ShowLoginDialogListener? = null
    private var menuItemChangeListener: MenuItemChangeListener? = null

    var allSubjectIds = ""
    var allSectionIds = ""
    var savedSubjectIds = ""
    var savedSectionIds = ""
    var savedTopicIDs = ""
    var moreAPICallCount = 0
    var swipeRightCount = 0
    private var isAdsFree: Boolean? = false

    private var questionFromIntent: Question? = null
    private var openFilterAfterApiCall = false
    private var interstitialCount = 20
    private var inviteFriendsCount = 34
    private var reviewCount = 49
    private var answeredQuesIdList: ArrayList<String> = arrayListOf()

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var reviewInfo: ReviewInfo
    private lateinit var manager: ReviewManager
    private var notifyToolbarHeartAdd = ""
    private var isOnline = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ShowLoginDialogListener) {
            showLoginDialogListener = context
        }
        if (context is MenuItemChangeListener) {
            menuItemChangeListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }

    override fun viewRelatedTask() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(HomeFragmentViewModel::class.java)
        viewModel.iDatabaseCallBack = this

        val userInfo = dataManager.mPref.prefGetUserInfo()
        binding.btnAddQuestion1.setOnClickListener(this)
        binding.playLayout1.setOnClickListener(this)

        questions.clear()
        moreAPICallCount = 0
        binding.shimmerViewContainer.startShimmerAnimation()

        try {
            if (arguments?.containsKey("question_object") == true) {
                val questionString = arguments?.getString("question_object")
                if (questionString?.isNotEmpty()!!) {
                    questionFromIntent = Gson().fromJson(questionString, Question::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (ConnectivityUtil.isOnline(activity)) {
            isOnline = true
            if (questionFromIntent == null) {
                viewModel.apiGetQuestionListForHome(
                    userInfo.id,
                    userInfo.category_id.toString(),
                    userInfo.subject_id,
                    userInfo.section_id,
                    userInfo.topic_id,
                    this
                )
                viewModel.apiGetOfflineQuestionListForHome(
                    userInfo.category_id.toString(),
                    userInfo.subject_id,
                    userInfo.section_id,
                    userInfo.topic_id,
                    this
                )
            } else {
                binding.questionsViewpager.visibility = View.GONE
                binding.shimmerViewContainer.visibility = View.VISIBLE
                binding.shimmerViewContainer.startShimmerAnimation()

                viewModel.apiGetQuestionById(
                    dataManager.mPref.prefGetUserInfo().id, "1",
                    questionFromIntent?.id ?: "", this
                )
            }
        } else {
            isOnline = false
            viewModel.dataGetAllOfflineQuestions()
        }

        try {
            savedSubjectIds = userInfo.subject_id ?: ""
            savedSectionIds = userInfo.section_id ?: ""
            savedTopicIDs = userInfo.topic_id ?: ""
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }

        viewModel.apiGetSubjectListByCategory(userInfo.category_id.toString(), this)
        isAdsFree = dataManager.mPref.prefGetIsAdFree() ?: false

        buildInterstitialAd()

        // Review info
        manager = ReviewManagerFactory.create(requireActivity())
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                reviewInfo = request.result
            }
        }
    }

    private fun getCalculatedPosition(): Int {
        var range = dataManager.mPref.getUserGlobalSetting()?.practice_hearts_range?.toInt() ?: 0
        if (range > 0)
            return ThreadLocalRandom.current().nextInt(3, range + 1);
        return 0
    }

    fun populateNotificationQuestion(questionString: String?) {
        questionFromIntent = Gson().fromJson(questionString, Question::class.java)
        if (ConnectivityUtil.isOnline(activity)) {
            binding.questionsViewpager.visibility = View.GONE
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.shimmerViewContainer.startShimmerAnimation()
            viewModel.apiGetQuestionById(
                dataManager.mPref.prefGetUserInfo().id, "1",
                questionFromIntent?.id ?: "", this
            )
        }
    }

    private fun buildInterstitialAd() {
        mInterstitialAd = InterstitialAd(activity)
        mInterstitialAd.adUnitId = context?.getString(R.string.ad_unit_id_interstitial_test)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetQuestionListForHome" -> {
                binding.questionsViewpager.visibility = View.VISIBLE
                binding.shimmerViewContainer.stopShimmerAnimation()
                binding.shimmerViewContainer.visibility = View.GONE
                val type = object : TypeToken<BaseModel<ArrayList<Question>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Question>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            questions = baseData.data ?: arrayListOf()
                            if (questions.size > 0) {
                                initTablayout()
                                if (menuItemChangeListener != null) {
                                    menuItemChangeListener?.updateNotificationCount(
                                        baseData.new_notification,
                                        false
                                    )
                                }
                                binding.questionsViewpager.visibility = View.VISIBLE
                            } else {
                                binding.emptyLayout.visibility = View.VISIBLE
                                binding.questionsViewpager.visibility = View.GONE
                                binding.filterBtn.setOnClickListener {
                                    onFilterClicked(0)
                                }
                            }
                        }
                    } else {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiGetMoreQuestionListForHome" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Question>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Question>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            if (menuItemChangeListener != null) {
                                menuItemChangeListener?.updateNotificationCount(
                                    baseData.new_notification,
                                    true
                                )
                            }

                            moreAPICallCount++
                            questions.addAll(baseData.data ?: arrayListOf())
                            binding.questionsViewpager.adapter?.notifyDataSetChanged()
                        }
                    } else {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiGetOfflineQuestionListForHome" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Question>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Question>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            val offlineQuestions = baseData.data ?: arrayListOf()
                            if (offlineQuestions.size > 0) {
                                offlineQuestions.forEach {
                                    it.question_category = ConstantField.QUESTION_TYPE_OFFLINE
                                }
                                viewModel.dataDeleteOfflineQuestions()
                                viewModel.dataInsertOfflineQuestions(offlineQuestions)
                            }
                        }
                    } else {
                        Log.d("OfflineQuestionList->", baseData.message[0])
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiGetSubjectListByCategory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Subject>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Subject>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            //filterBottomsheet.showSelectSubjectDialog(baseData.data!!)
                            viewModel.dataDeleteAllSubjectList()
                            viewModel.dataInsertAllSubjectList(baseData.data ?: listOf())
                            baseData.data?.forEach {
                                allSubjectIds += if (allSubjectIds.isEmpty()) {
                                    it.id
                                } else {
                                    "," + it.id
                                }
                            }
                            viewModel.apiGetSectionsBySubject(allSubjectIds, this)
                        }
                    } else {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiGetSectionsBySubject" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Section>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Section>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            viewModel.dataDeleteAllSectionList()
                            viewModel.dataInsertAllSectionList(baseData.data ?: listOf())
                            baseData.data?.forEach {
                                allSectionIds += if (allSectionIds.isEmpty()) {
                                    it.id
                                } else {
                                    "," + it.id
                                }
                            }
                            viewModel.apiGetTopicBySection(allSectionIds, this)
                        }
                    } else {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiGetTopicBySection" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Topic>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Topic>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            viewModel.dataDeleteAllTopicList()
                            viewModel.dataInsertAllTopicList(baseData.data ?: listOf())
                            if (openFilterAfterApiCall) {
                                openFilter()
                                openFilterAfterApiCall = false
                            }
                            // filterBottomsheet.showSelectTopicDialog(baseData.data!! as ArrayList<BaseItem>)
                        }
                    } else {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiBookmarkQuestion" -> {
                val type = object : TypeToken<BaseModel<ArrayList<EmptyModel>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<EmptyModel>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                        showToast(activity, baseData.message[0])
                        binding.questionsViewpager.adapter?.notifyDataSetChanged()
                    } else {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiUnBookmarkQuestion" -> {
                val type = object : TypeToken<BaseModel<ArrayList<EmptyModel>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<EmptyModel>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                        binding.questionsViewpager.adapter?.notifyDataSetChanged()
                    } else {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }

            "apiReportAboutQuestion" -> {
                val type = object : TypeToken<BaseModel<ArrayList<EmptyModel>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<EmptyModel>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    } else {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
                reportBottomSheet.dismiss()
            }
            "apiGetQuestionById" -> {
                binding.questionsViewpager.visibility = View.VISIBLE
                binding.shimmerViewContainer.stopShimmerAnimation()
                binding.shimmerViewContainer.visibility = View.GONE
                val type = object : TypeToken<BaseModel<Question>>() {}.type
                result.data?.body()?.let {
                    questions.clear()
                    binding.questionsViewpager.adapter?.notifyDataSetChanged()
                    val baseData =
                        Gson().fromJson<BaseModel<Question>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        val question = baseData.data
                        val userInfo = dataManager.mPref.prefGetUserInfo()
                        if (question != null) {
                            questionFromNotification = question
                            viewModel.apiGetQuestionListForHome(
                                userInfo.id,
                                userInfo.category_id.toString(),
                                userInfo.subject_id,
                                userInfo.section_id,
                                userInfo.topic_id,
                                this
                            )
                            viewModel.apiGetOfflineQuestionListForHome(
                                userInfo.category_id.toString(),
                                userInfo.subject_id,
                                userInfo.section_id,
                                userInfo.topic_id,
                                this
                            )
                        }
                    } else {
                        showToast(
                            activity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }

                }
            }
            "apiPracticeRewardHeart" -> {
                val type = object : TypeToken<BaseModel<HeartVariation>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<HeartVariation>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        viewModel.deleteLastGameHeartData()
                        dataManager.mPref.prefSetUserHeart(baseData.data!!.current_hearts)
                        dataManager.mPref.setGameUserSetting(baseData?.data?.user_settings!!)
                        adaper?.updateHeartAddShow(baseData.data?.user_settings?.user_practice_hearts_limit.toString())
                    } else {
                        showToast(activity, baseData.message[0])
                    }
                }
            }
        }
    }

    private fun storeDateOfInvite() {
        val date = Date()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy")
        val dateString = dateFormat.format(date)
        dataManager.mPref.saveDateOfInvite(dateString)
    }

    private fun checkIfShownWithin7Days(): Boolean {
        try {
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("dd-MM-yyyy")
            val datePrevious = dateFormat.parse(dataManager.mPref.getDateOfInvite())
            val day7 = 7L * 24 * 60 * 60 * 1000
            return currentDate.before(Date(datePrevious.time + day7))
        } catch (ex: DateParseException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return false
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "dataGetSubjectListByCategory" -> {
                val subjectListDTO = result as List<SubjectDTO>
                if (subjectListDTO.isNotEmpty()) {
                    val subjectList = SubjectDTO.toSubjects(subjectListDTO)
                    Log.d("subjectlist->", subjectList.size.toString())
                    filterBottomsheet.showSelectSubjectDialog(
                        subjectList as ArrayList<BaseItem>,
                        true
                    )
                }
            }
            "dataInsertAllSubjectList" -> {
                Log.d("subjectlist->", "Subject inserted successfully")
            }
            "dataGetAllSectionListBySubject" -> {
                val sectionListDTO = result as List<SectionDTO>
                val sectionList = SectionDTO.toSections(sectionListDTO)
                Log.d("sectionList->", sectionList.size.toString())
                val finalList = groupBySubjects(sectionList)
                filterBottomsheet.showSelectSectionDialog(finalList, true)
            }
            "dataGetAllTopicListBySection" -> {
                val topicListDTO = result as List<TopicDTO>
                val topicList = TopicDTO.toTopics(topicListDTO)
                Log.d("sectionList->", topicList.size.toString())
                val finalList = groupBySections(topicList)
                filterBottomsheet.showSelectTopicDialog(finalList, true)
            }
            "dataGetAllOfflineQuestions" -> {
                val questionListDTO = result as List<QuestionDTO>
                val questionList = QuestionDTO.toQuestions(questionListDTO)
                initTablayout()
                questions.clear()
                questions.addAll(questionList)
                binding.questionsViewpager.adapter?.notifyDataSetChanged()
            }
            "checkFilterValuesAvailable" -> {
                val subCount = result as Int
                if (subCount > 0) {
                    openFilter()
                } else {
                    viewModel.apiGetSubjectListByCategory(
                        dataManager.mPref.prefGetUserInfo().category_id.toString(),
                        this
                    )
                    openFilterAfterApiCall = true
                }
            }
        }
    }


    private fun gameHeartDTO(practice: String): GameHeartDTO {
        val userInfo = dataManager.mPref.prefGetUserInfo()
        return GameHeartDTO(
            user_id = userInfo.id.toInt(),
            heart_type = GAME_HEART_PLUS,
            practice = practice
        )
    }

    private fun groupBySubjects(sectionList: List<Section>): ArrayList<BaseItem> {
        val map = mutableMapOf<String, ArrayList<Section>>()
        val refinedList = arrayListOf<BaseItem>()
        sectionList.forEach {
            if (map.containsKey(it.subject_name)) {
                map[it.subject_name]?.add(it)
            } else {
                map[it.subject_name.toString()] = ArrayList()
                map[it.subject_name]?.add(it)
            }
        }

        var parentPos = 0
        var counter = 0
        map.forEach {
            refinedList.add(HeaderFilter(map[it.key]?.get(0)?.subject_name.toString()))
            counter++
            it.value.forEach {
                it.parentPos = parentPos
                refinedList.add(it)
                counter++
            }
            parentPos = counter
        }
        return refinedList
    }

    private fun groupBySections(topicList: List<Topic>): ArrayList<BaseItem> {
        val map = mutableMapOf<String, ArrayList<Topic>>()
        val refinedList = arrayListOf<BaseItem>()
        topicList.forEach {
            if (map.containsKey(it.section_name)) {
                map[it.section_name]?.add(it)
            } else {
                map[it.section_name.toString()] = ArrayList()
                map[it.section_name]?.add(it)
            }
        }

        var parentPos = 0
        var counter = 0
        map.forEach {
            refinedList.add(HeaderFilter(map[it.key]?.get(0)?.section_name.toString()))
            counter++
            it.value.forEach {
                it.parentPos = parentPos
                refinedList.add(it)
                counter++
            }
            parentPos = counter
        }
        return refinedList
    }

    override fun onFailedDB(result: Any, optName: String) {
        println("$optName -> $result")
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnAddQuestion1 -> {
                if (dataManager.mPref.prefGetLoginMode()) {
                    dataManager.mPref.prefNavigateFromGame(false)
                    startActivity(
                        Intent(
                            this@HomeFragment.activity,
                            QuestionAddActivity::class.java
                        )
                    )

                } else {
                    if (showLoginDialogListener != null)
                        showLoginDialogListener?.listenShowLoginDialog()
                }
            }
            binding.playLayout1 -> {
                if (dataManager.mPref.prefGetLoginMode()) {
                    startActivity(
                        Intent(
                            activity,
                            GameLevelActivity::class.java
                        )
                    )
                } else {
                    if (showLoginDialogListener != null)
                        showLoginDialogListener?.listenShowLoginDialog()
                }
            }
        }
    }

    private fun initTablayout() {
        adaper = null
        var isAdsFree = dataManager.mPref.prefGetIsAdFree() ?: false
        Log.d("adfreeinhome", isAdsFree.toString())
        Log.d("QuestionSize->", questions.size.toString())
        if (questionFromNotification != null) {
            questions.add(0, questionFromNotification!!)
        }
        var heartPosition =
            if (dataManager.mPref.getGameUserSetting()?.corr_ans_consecutive_count == "0") 0
            else getCalculatedPosition()
        Log.d("Reset Heart->", heartPosition.toString())

        Log.d("Current Item Heart->", binding.questionsViewpager.currentItem.toString())
        adaper = HomeQuestionsAdapter(
            requireActivity(),
            questions,
            this,
            heartPosition,
            dataManager.mPref.prefGetUserInfo().id.isNotEmpty()
                    && dataManager.mPref.getGameUserSetting()?.user_practice_hearts_limit != "0" && isOnline,
            savedSubjectIds.isNotEmpty()
        )

        binding.questionsViewpager.adapter = adaper

        binding.questionsViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val isGoingToRightPage = position == mCurrentFragmentPosition
                if (isGoingToRightPage) {
                    // user is going to the right page
                    swipeRightCount++
                }
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
                } else if (position % inviteFriendsCount == 0) {
                    // Don't show again
                    inviteFriendsCount += 200000
                    if (!checkIfShownWithin7Days())
                        storeDateOfInvite()
                } else if (position % reviewCount == 0) {
                    // Don't show again
                    reviewCount += 2000000
                    if (::reviewInfo.isInitialized)
                        manager.launchReviewFlow(activity!!, reviewInfo)
                }
            }
        })
    }

    override fun onSubjectLayoutClicked() {
        viewModel.dataGetSubjectListByCategory()
    }

    override fun onSectionLayoutClicked(subjectIds: String) {
        viewModel.dataGetAllSectionListBySubject(subjectIds)
    }

    override fun onTopicLayoutClicked(sectionIds: String) {
        viewModel.dataGetAllTopicListBySection(sectionIds)
    }

    override fun onSubmitBtnClicked(
        subjectsIds: String, sectionIds: String, topicIds: String, subjectNames: String,
        sectionNames: String, topicNames: String
    ) {
        binding.emptyLayout.visibility = View.GONE
        binding.questionsViewpager.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmerAnimation()

        questionFromNotification = null
        val userInfo = dataManager.mPref.prefGetUserInfo()
        viewModel.apiGetQuestionListForHome(
            userInfo.id,
            userInfo.category_id.toString(),
            subjectsIds,
            sectionIds,
            topicIds,
            this
        )
        userInfo.let {
            it.subject_id = subjectsIds
            it.section_id = sectionIds
            it.topic_id = topicIds
        }

        userInfo.let {
            it.subject_names = subjectNames
            it.section_names = sectionNames
            it.topic_names = topicNames
        }

        savedSubjectIds = subjectsIds
        savedSectionIds = sectionIds
        savedTopicIDs = topicIds

        if (userInfo.category_id.isNullOrEmpty()) {
            userInfo.category_id = "1"
        }
        dataManager.mPref.prefSetUserInfo(userInfo)
        adaper?.updateFilterUseValue(savedSubjectIds.isNotEmpty())
        filterBottomsheet.dismiss()
    }

    override fun onLoading(isLoader: Boolean, key: String) {

    }

    override fun onSubmitAnswer(position: Int, answered_position: Int) {
        if (questions[position].answered_position == questions[position].answer) {
            if (dataManager.mPref.prefGetLoginMode() &&
                dataManager.mPref.getGameUserSetting()?.user_practice_hearts_limit != "0" && savedSubjectIds.isEmpty()
            ) {
                if (dataManager.mPref.getGameUserSetting()?.corr_ans_consecutive_count == "0") {
                    if (!answeredQuesIdList.any { it == questions[position].id }) {
                        notifyToolbarHeartAdd =
                            dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_hearts
                                ?: "0"
                        dataManager.mPref.prefSetRewardFirstTime("1")
                        viewModel.insertGameHeart(gameHeartDTO("0"))
                        viewModel.apiPracticeRewardHeart("1", "0", this)
                    }
                } else {
                    if (!answeredQuesIdList.any { it == questions[position].id }) {
                        notifyToolbarHeartAdd =
                            dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_random_hearts
                                ?: "0"
                        viewModel.insertGameHeart(gameHeartDTO("1"))
                        viewModel.apiPracticeRewardHeart("1", "1", this)
                    }
                }
            }
            answerDescriptionBottomSheet =
                AnswerDescriptionBottomSheet(questions[position], true, this)
        } else {
            answerDescriptionBottomSheet =
                AnswerDescriptionBottomSheet(questions[position], false, this)
        }
        // Add answered to list
        answeredQuesIdList.add(questions[position].id)
        fragmentManager?.let {
            answerDescriptionBottomSheet.show(
                it,
                answerDescriptionBottomSheet.tag
            )
        }
    }


    override fun onImageClicked(position: Int) {
        val dialogImageView =
            context?.let { ImageViewDialog(it, questions[position].picture) }
        dialogImageView?.show()
    }

    override fun loadMore() {
        val userInfo = dataManager.mPref.prefGetUserInfo()
        if (ConnectivityUtil.isOnline(activity)) {
            viewModel.apiGetMoreQuestionListForHome(
                userInfo.id,
                userInfo.category_id.toString(),
                userInfo.subject_id,
                userInfo.section_id,
                userInfo.topic_id,
                this@HomeFragment
            )
        }
    }

    override fun onReportSubmitted(question_id: String, type: String, details: String) {
        viewModel.apiReportAboutQuestion(question_id, type, details, this)
    }

    override fun onVideoReportSubmitted(question_id: String, type: String, details: String) {

    }

    override fun onReportDialogDismiss() {
    }

    override fun onReportProblemClicked(position: Int) {
        if (dataManager.mPref.prefGetLoginMode()) {
            reportBottomSheet = ReportBottomSheet(questions[position].id)
            reportBottomSheet.setBottomDialogListener(this@HomeFragment, false, false)
            activity?.supportFragmentManager?.let {
                reportBottomSheet.show(
                    it,
                    reportBottomSheet.tag
                )
            }
        } else {
            if (showLoginDialogListener != null)
                showLoginDialogListener?.listenShowLoginDialog()
        }
    }

    override fun onShareClicked(position: Int) {
        startActivity(
            Intent(activity, QuestionShareActivity::class.java).putExtra(
                "question",
                questions[position]
            ).putExtra("action", "share")
        )
        activity?.overridePendingTransition(R.anim.scale_up, R.anim.hold)
    }

    override fun onDownloadClicked(position: Int) {
        startActivity(
            Intent(activity, QuestionShareActivity::class.java).putExtra(
                "question",
                questions[position]
            ).putExtra("action", "download")
        )
        activity?.overridePendingTransition(R.anim.scale_up, R.anim.hold)
    }

    override fun onBookmarkClicked(position: Int) {
        val userInfo = dataManager.mPref.prefGetUserInfo()
        if (dataManager.mPref.prefGetLoginMode()) {
            if (questions[position].is_bookmarked == 0) {
                viewModel.apiBookmarkQuestion(
                    questions[position].id,
                    userInfo.category_id.toString(),
                    this@HomeFragment
                )
                questions[position].is_bookmarked = 1
            } else {
                viewModel.apiUnBookmarkQuestion(
                    questions[position].id,
                    userInfo.category_id.toString(),
                    this@HomeFragment
                )
                questions[position].is_bookmarked = 0
            }
        } else {
            if (showLoginDialogListener != null)
                showLoginDialogListener?.listenShowLoginDialog()
        }
    }

    override fun onFilterClicked(position: Int) {
        checkShouldOpenFilter()
    }

    fun checkShouldOpenFilter() {
        if (this::viewModel.isInitialized) {
            viewModel.checkFilterValuesAvailable()
        }
    }

    fun openFilter() {
        filterBottomsheet = FilterBottomSheet(viewModel)
        filterBottomsheet.setBottomDialogListener(this@HomeFragment)
        val userInfo = dataManager.mPref.prefGetUserInfo()

        if (savedSubjectIds.isNotEmpty() && !userInfo.subject_names.isNullOrEmpty()) {
            filterBottomsheet.setSavedFilterValues(
                savedSubjectIds,
                userInfo.subject_names.toString(),
                "Subject"
            )
        }

        if (savedSectionIds.isNotEmpty() && !userInfo.section_names.isNullOrEmpty()) {
            filterBottomsheet.setSavedFilterValues(
                savedSectionIds,
                userInfo.section_names.toString(),
                "Section"
            )
        }
        if (savedTopicIDs.isNotEmpty() && !userInfo.topic_names.isNullOrEmpty()) {
            filterBottomsheet.setSavedFilterValues(
                savedTopicIDs,
                userInfo.topic_names.toString(),
                "Topic"
            )
        }
        activity?.supportFragmentManager?.let {
            filterBottomsheet.show(
                it,
                filterBottomsheet.tag
            )
        }
    }

    override fun onQuestionAddBtnClicked() {
    }

    override fun onPlaybtnClicked() {

    }

    override fun isFirstTimeLoad(isFirstTime: Boolean) {
        if (isFirstTime) {
            binding.shine1.visibility = View.VISIBLE
            shineAnimation(binding.shine1.context, binding.shine1)
        } else {
            binding.shine1.visibility = View.GONE
        }
    }

    override fun triggerUpdateHeartCount() {
        adaper?.updateHeartBonusPosition(getCalculatedPosition())
    }


    private fun shineAnimation(context: Context, shine: View) {
        var count = 0
        val anim = AnimationUtils.loadAnimation(context, R.anim.shine_left_right)
        shine.startAnimation(anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(p0: Animation?) {
                if (count < 3)
                    shine.startAnimation(anim)
                else {
                    shine.visibility = View.GONE
                }
                count++
            }

            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
        })
    }


    override fun onOkClicked() {
        answerDescriptionBottomSheet.dismiss()
        binding.questionsViewpager.currentItem++
    }

    override fun onDismissed() {
        adaper?.resetSubmitFlag()
        if (notifyToolbarHeartAdd.isNotEmpty()) {
            menuItemChangeListener?.updateGameHeart(notifyToolbarHeartAdd, GAME_HEART_PLUS)
            notifyToolbarHeartAdd = ""
        }
    }

    override fun onVideoBtnClicked(question: Question) {
        if (!dataManager.mPref.prefGetLoginMode()) {
            if (showLoginDialogListener != null)
                showLoginDialogListener?.listenShowLoginDialog()
        } else {
            filterValues = FilterValues()
            filterValues?.category_id = "1"
            filterValues?.subject_id = question.subject_id
            filterValues?.topic_id = question.topic_id
            filterValues?.section_id = question.section_id
            filterValues?.batch_id = ""
            filterValues?.difficulty_id = ""
            Log.d("filter", filterValues.toString())
            startActivity(
                Intent(activity, RelatedVideoActivity::class.java).putExtra(
                    "filterValues", Gson().toJson(filterValues)
                )
            )
        }
    }

    interface ShowLoginDialogListener {
        fun listenShowLoginDialog()
    }

    interface MenuItemChangeListener {
        fun updateNotificationCount(count: Int, isFromMore: Boolean)
        fun updateGameHeart(addAmount: String, type: Int)
    }

    override fun onDetach() {
        super.onDetach()
        binding.questionsViewpager.adapter = null
    }
}