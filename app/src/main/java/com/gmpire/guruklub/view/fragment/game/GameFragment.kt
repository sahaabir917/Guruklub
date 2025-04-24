package com.gmpire.guruklub.view.fragment.game

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.SubjectDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.adsubscribepaymentresponse.LiveExamPaymentCompleteResponse
import com.gmpire.guruklub.data.model.adsubscribepaymentresponse.LiveExamPaymentResponse
import com.gmpire.guruklub.data.model.adsubscribepaymentresponse.PaymentInfo
import com.gmpire.guruklub.data.model.categoryAndSubject.Section
import com.gmpire.guruklub.data.model.categoryAndSubject.Subject
import com.gmpire.guruklub.data.model.categoryAndSubject.Topic
import com.gmpire.guruklub.data.model.game.GameChallengeItem
import com.gmpire.guruklub.data.model.modelTest.ModelTest
import com.gmpire.guruklub.data.model.modelTest.ModelTestRegistrationResponse
import com.gmpire.guruklub.data.model.modelTest.ModelTestResponseModel
import com.gmpire.guruklub.data.model.modelTest.TimeResponse
import com.gmpire.guruklub.databinding.FragmentGameBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.util.DateUtil.Companion.simpleDateFormat
import com.gmpire.guruklub.util.DateUtil.Companion.simpleDateFormatServer
import com.gmpire.guruklub.view.BottomSheet.CustomExamBottomSheet
import com.gmpire.guruklub.view.activity.gameActivity.GameActivity
import com.gmpire.guruklub.view.activity.gameHelp.ContentActivity
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
import com.gmpire.guruklub.view.activity.gamelevel.GAME_HEART_MINUS
import com.gmpire.guruklub.view.activity.modelTestActivity.ModelTestActivity
import com.gmpire.guruklub.view.activity.profileSetup.ProfileSetupActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.fragment.home.HomeFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener
import okhttp3.ResponseBody
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class GameFragment : BaseFragment(), View.OnClickListener, IDatabaseCallBack,
    CustomExamBottomSheet.IBottomSheetDialogClicked, LiveExamHolder.LiveExamListener,
    QuickExamAdapter.OnItemClick, SSLCTransactionResponseListener {

    public fun GameFragment() {}

    private lateinit var practiceExamTitle: String
    private lateinit var practiceExamTestId: String
    private var customGameTypeHeartCost: String = "0"
    private var elapsedTime: Long = 0L
    private var testDate: Date? = null
    private var timer: CountDownTimer? = null
    private var modelTestResponse: ModelTestResponseModel? = null
    private var recordedModelTestResponse: ModelTestResponseModel? = null
    private var modelTestRegistrationResponse: ModelTestRegistrationResponse? = null
    private var currentTime: Date? = null

    private var gameModes: ArrayList<GameChallengeItem> = arrayListOf()
    private var quickExam: List<GameChallengeItem> = listOf()
    private val filterGameModes = arrayListOf<GameChallengeItem>()
    private lateinit var paymentInfo: PaymentInfo
    private lateinit var viewModel: GameFragmentViewModel
    lateinit var binding: FragmentGameBinding
    private var title: String? = null
    private var selectedModeId: Int = -1

    private var showLoginDialogListener: HomeFragment.ShowLoginDialogListener? = null
    private var menuItemChangeListener: HomeFragment.MenuItemChangeListener? = null
    private var examStartListener: ExamStartListener? = null

    private var allSubjectList: ArrayList<Subject> = arrayListOf()
    private var liveExamHeartCost: Int = 0
    private var customGameTypeId = ""
    private var quickExamTypeId = ""
    private var subjectIds = ""
    private var modelTestId: String? = ""

    var allSubjectIds = ""
    var allSectionIds = ""
    var isFromEmptyModelTest = false
    var startOrResumeText: String = ""
    var singleModelTest: ModelTest? = null
    var selectedQuickExamPos = 0
    var examFees = 0.0


    private lateinit var customExamBottomSheet: CustomExamBottomSheet

    override fun onResume() {
        super.onResume()
        //getting the current time from server
        viewModel.apiGetTime(this)
        viewModel.apiGetPracticeLiveExam(dataManager.mPref.prefGetUserInfo().category_id, this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeFragment.ShowLoginDialogListener) {
            showLoginDialogListener = context
        }
        if (context is HomeFragment.MenuItemChangeListener) {
            menuItemChangeListener = context
        }
        if (context is ExamStartListener) {
            examStartListener = context
        }
    }

    companion object {
        private var f = GameFragment()
        fun newInstance(title: String): GameFragment {
            val args = Bundle()
            args.putString(ConstantField.ACCESS_TITLE, title)
            if (!f.isAdded)
                f.arguments = args
            Log.d("TAG", f.toString())
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false);
        return binding.root
    }

    override fun viewRelatedTask() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(GameFragmentViewModel::class.java)
        viewModel.iDatabaseCallBack = this

        viewModel.apiGetGameChallenges("game", this)

        viewModel.apiGetPracticeLiveExam(dataManager.mPref.prefGetUserInfo().category_id, this)

        binding.howToGetCoinTv.setOnClickListener(this)
        binding.startGameTv.setOnClickListener(this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.apiGetGameChallenges("game", this)
            viewModel.apiGetTime(this)
            viewModel.apiGetPracticeLiveExam(dataManager.mPref.prefGetUserInfo().category_id, this)
        }

        binding.imageViewCustomExam.setOnClickListener {
            if (dataManager.mPref.prefGetLoginMode()) {
                openCustomExamBottomSheet()
            } else {
                if (showLoginDialogListener != null)
                    showLoginDialogListener?.listenShowLoginDialog()
            }
        }

        binding.textViewFilter.setOnClickListener {
            if (dataManager.mPref.prefGetLoginMode()) {
                openCustomExamBottomSheet()
            } else {
                if (showLoginDialogListener != null)
                    showLoginDialogListener?.listenShowLoginDialog()
            }
        }

        binding.textViewQuickExam.setOnClickListener {
            if (dataManager.mPref.prefGetLoginMode()) {
                quickExam = gameModes.filter { it.id == quickExamTypeId }
                if (quickExam.isEmpty()) {
                    showToast(requireActivity(), "Something went wrong...")
                    return@setOnClickListener
                }

                var heartCost = quickExam[0].hearts_cost.toInt()
                if (dataManager.mPref.prefGetUserHeart()
                        ?.toInt() ?: 0 >= heartCost
                ) {
                    if (quickExam[0].hearts_cost.toInt() == 0) {
                        Handler().postDelayed({
                            examStartListener?.onExamStart()
                            startActivity(
                                Intent(activity, GameActivity::class.java).putExtra(
                                    "game_mode", quickExam[0]
                                ).putExtra("heartCosting", heartCost > 0)
                            )
                        }, 1000)
                    } else {
                        showAlertDialogCosting(heartCost, "quick_exam")
                    }
                } else {
                    showHeartBuyDialog(heartCost.toString())
                }
            } else {
                if (showLoginDialogListener != null)
                    showLoginDialogListener?.listenShowLoginDialog()
            }
        }
    }

    private fun openCustomExamBottomSheet() {
        customExamBottomSheet = CustomExamBottomSheet()
        activity?.supportFragmentManager?.let {
            customExamBottomSheet.show(
                it,
                customExamBottomSheet.tag
            )
        }
        customExamBottomSheet.setBottomDialogListener(this@GameFragment)
        viewModel.dataGetSubjectListByCategory()

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetGameChallenges" -> {
                val type = object : TypeToken<BaseModel<ArrayList<GameChallengeItem>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<GameChallengeItem>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            try {
                                gameModes.clear()
                                gameModes.addAll(baseData.data!!)
                                initGameModes()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        showToast(activity, baseData.message[0])
                    }
                }

            }
            "apiGetTime" -> {
                val type = object : TypeToken<BaseModel<TimeResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<TimeResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            //setting the current time
                            try {
                                currentTime =
                                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(baseData.data?.time)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            //api request for upcoming model test if available
                            viewModel.apiGetCheckModelTest(
                                dataManager.mPref.prefGetUserInfo().id,
                                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                                this
                            )
                        }
                    } else {
                        showToast(activity, baseData.message[0])
                    }
                }
            }
            "apiGetCheckModelTest" -> {
                val type = object : TypeToken<BaseModel<ModelTestResponseModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ModelTestResponseModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        //setting model test response
                        modelTestResponse = baseData.data
                        manageMultipleModelTestResponses(modelTestResponse?.model_test)
                        // Multiple live exam
                        singleModelTest = null
                        if (modelTestResponse?.model_test?.isNotEmpty()!!) {
                            val refinedModelTest = refinedList(modelTestResponse?.model_test)
                            // For multiple live test
                            if (refinedModelTest.size > 1) {
                                binding.cardView2.visibility = View.GONE
                                binding.relativeLayoutFreeTag.visibility = View.GONE
                                binding.recyclerViewLiveExam.visibility = View.VISIBLE
                                initLiveExamRecyclerView(refinedModelTest)
                                // No live test
                            } else if (refinedModelTest.isEmpty()) {
                                setNoModelTestView()
                                // For single live test
                            } else {
                                binding.relativeLayoutFreeTag.visibility = View.VISIBLE
                                binding.cardView2.visibility = View.VISIBLE
                                binding.recyclerViewLiveExam.visibility = View.GONE
                                singleModelTest = refinedModelTest[0]
                                elapsedTime =
                                    (currentTime!!.time - simpleDateFormatServer.parse(
                                        refinedModelTest[0].date
                                    ).time)
                                val startOrResumeText =
                                    if (refinedModelTest[0].is_participated == 1 && refinedModelTest[0].is_completed == 0)
                                        "Resume"
                                    else
                                        "Start Now"
                                setModelTestView(
                                    refinedModelTest[0], startOrResumeText
                                )
                                if (singleModelTest?.hearts_cost?.toInt() ?: 0 > 0) {
                                    binding.relativeLayoutFreeTag.visibility = View.VISIBLE
                                    binding.heartCostTextview.text =
                                        singleModelTest?.hearts_cost.toString()
                                } else {
                                    binding.relativeLayoutFreeTag.visibility = View.VISIBLE
                                    binding.heartCostTextview.text = "Free"
                                }
                            }
                        }
                    }
                }
            }
            "apiModelTestRegister" -> {
                val type = object : TypeToken<BaseModel<ModelTestRegistrationResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ModelTestRegistrationResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        modelTestRegistrationResponse = baseData.data
                        dataManager.mPref.prefSetUserHeart(modelTestRegistrationResponse?.current_hearts.toString())
                        dataManager.mPref.prefSetModelTestRegistrationResponseById(
                            modelTestRegistrationResponse,
                            modelTestRegistrationResponse?.model_test_id.toString()
                        )
                        if (!isFromEmptyModelTest) {
                            viewModel.apiGetTime(this)
                        } else {
                            notifyModelTest(
                                "Start",
                                modelTestRegistrationResponse?.model_test_id
                            )
                            isFromEmptyModelTest = false
                        }
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

                            allSubjectList.clear()
                            allSubjectList.addAll(baseData.data ?: listOf())
                            setAllSubjectIds(allSubjectList)
                            if (customExamBottomSheet != null) {
                                customExamBottomSheet.populateSubjectList(allSubjectList)
                            }
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
                        showToast(activity, baseData.message[0])
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
                        showToast(activity, baseData.message[0])
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
                        }
                    } else {
                        showToast(activity, baseData.message[0])
                    }
                }
            }

            "purchaseLiveExam" -> {
                val type = object : TypeToken<BaseModel<LiveExamPaymentResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<LiveExamPaymentResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            val response = baseData.data
                            response?.payment_info?.let {
                                paymentInfo = it
                                initSslPayment()
                            }
                        }
                    } else {
                        showToast(activity, baseData.message[0])
                    }
                }
            }

            "getPaymentExecution" -> {
                val type = object : TypeToken<BaseModel<LiveExamPaymentCompleteResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<LiveExamPaymentCompleteResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                val response: LiveExamPaymentCompleteResponse? = baseData.data
                                response?.model_test_id?.let { it1 ->
                                    viewModel.apiModelTestRegister(
                                        dataManager.mPref.prefGetUserInfo().id,
                                        it1.toString(),
                                        true,
                                        this
                                    )
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(activity, baseData.message[0])
                    }
                }
            }

            "getFailedTransaction" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        showToast(activity, baseData.message[0])
                    }
                }
            }

            "apiGetPracticeLiveExam" -> {
                val type = object : TypeToken<BaseModel<ModelTestResponseModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ModelTestResponseModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        //setting model test response
                        recordedModelTestResponse = baseData.data
                        // Multiple live exam
                        if (recordedModelTestResponse?.model_test?.isNotEmpty()!!) {
                            //recordedModelTestResponse?.model_test?.sortByDescending { it.id }
                            when {
                                // For multiple live test
                                recordedModelTestResponse?.model_test!!.size > 1 -> {
                                    binding.cardView3.visibility = View.GONE
                                    binding.relativeLayoutFreeTag1.visibility = View.GONE
                                    binding.rlRecordedLiveExam.visibility = View.VISIBLE
                                    initRecordedLiveExamRecyclerView(recordedModelTestResponse?.model_test!!)
                                }
                                // No live test
                                recordedModelTestResponse!!.model_test.isEmpty() -> {
                                    binding.recordedLiveExam.visibility = View.GONE
                                    binding.rlRecordedLiveExam.visibility = View.GONE
                                }
                                // For single live test
                                else -> {
                                    binding.relativeLayoutFreeTag1.visibility = View.VISIBLE
                                    binding.cardView3.visibility = View.VISIBLE
                                    binding.rlRecordedLiveExam.visibility = View.GONE
                                    practiceExamTestId =
                                        recordedModelTestResponse!!.model_test[0].id
                                    practiceExamTitle =
                                        recordedModelTestResponse!!.model_test[0].title
                                    val startOrResumeText = "Start Now"
                                    setRecordedModelTestView(
                                        recordedModelTestResponse!!.model_test[0], startOrResumeText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun setRecordedModelTestView(modelTestResponse: ModelTest, startOrResumeText: String) {
        testDate = simpleDateFormatServer.parse(modelTestResponse.date)
        //check if model test is already registered by user or not
        binding.btnJoinNowOrStartTest1.text = startOrResumeText
        binding.btnJoinNowOrStartTest1.isEnabled = false
        this.startOrResumeText = startOrResumeText
        restOfTheSetModelTest(startOrResumeText, modelTestResponse, true)
    }

    private fun notifyModelTest(startOrResumeText: String, modelTestId: String?) {
        modelTestResponse?.model_test?.forEachIndexed { i, modelTest ->
            if (modelTest.id == modelTestId) {
                modelTestResponse?.model_test!![i].updateFromMobileRes = 1
                modelTestResponse?.model_test!![i].tempStartOrResumeText = startOrResumeText
                binding.recyclerViewLiveExam.adapter?.notifyItemChanged(i)
            }
        }
    }

    private fun manageMultipleModelTestResponses(modelTests: ArrayList<ModelTest>?) {
        modelTests?.forEach {
            dataManager.mPref.prefSetModelTestResponseModelById(it, it.id)
        }
    }

    private fun initLiveExamRecyclerView(modelTests: ArrayList<ModelTest>?) {
        binding.recyclerViewLiveExam.adapter =
            BaseRecyclerAdapter(context, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return LiveExamHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                activity
                            ), R.layout.item_live_exam, parent, false
                        ), context!!, currentTime,
                        dataManager, this@GameFragment, false
                    )
                }

                override fun loadMoreItem() {
                }
            }, modelTests!!)
    }


    private fun initRecordedLiveExamRecyclerView(modelTests: ArrayList<ModelTest>) {
        binding.rlRecordedLiveExam.adapter =
            BaseRecyclerAdapter(context, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return LiveExamHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                activity
                            ), R.layout.item_live_exam, parent, false
                        ), context!!, currentTime,
                        dataManager, this@GameFragment, true
                    )
                }

                override fun loadMoreItem() {
                }
            }, modelTests)
    }


    private fun refinedList(modelTests: ArrayList<ModelTest>?): ArrayList<ModelTest> {
        val refinedList = arrayListOf<ModelTest>()
        modelTests?.forEach {
            val localElapsedTime =
                (currentTime!!.time - simpleDateFormatServer.parse(it.date).time)
            val duration =
                (it.duration.toInt() * 60 * 1000)
            if (localElapsedTime < duration) {
                refinedList.add(it)
            }
        }
        return refinedList
    }

    private fun setNoModelTestView() {
        if (timer != null) {
            timer?.cancel()
        }

        if (singleModelTest == null)
            binding.textViewExamName.text = "No exam available"
        else
            binding.textViewExamName.text = singleModelTest?.title

        binding.btnJoinNowOrStartTest.visibility = View.GONE
        binding.btnSyllabus.visibility = View.GONE
        binding.textViewExamTime.text = "Coming soon"
        binding.textViewCountdown.text = "00:00:00:00"
        binding.textViewCountdown.visibility = View.GONE
        binding.heartCostTextview.text = "Free"

        if (singleModelTest != null) {
            if (singleModelTest?.register == 0) {
                binding.textViewExamTime.text =
                    "Not registered"
            }
        }
    }


    private fun setModelTestView(
        modelTestResponse: ModelTest,
        startOrResumeText: String
    ) {
        testDate = simpleDateFormatServer.parse(modelTestResponse.date)
        binding.textViewExamName.text = modelTestResponse.title

        binding.btnJoinNowOrStartTest.visibility = View.VISIBLE
        binding.btnSyllabus.visibility = View.VISIBLE
        binding.textViewExamTime.text = "Time of test: ${simpleDateFormat.format(testDate)}"

        //check if model test is already registered by user or not
        if (modelTestResponse.register == 1) {
            binding.btnJoinNowOrStartTest.text = startOrResumeText
            binding.btnJoinNowOrStartTest.isEnabled = false
            modelTestRegistrationResponse =
                dataManager.mPref.prefGetModelTestRegistrationResponseById(modelTestResponse.id)
            if (modelTestRegistrationResponse == null) {
                //api request for registration for model test
                this.startOrResumeText = startOrResumeText
                isFromEmptyModelTest = true
                viewModel.apiModelTestRegister(
                    dataManager.mPref.prefGetUserInfo().id,
                    modelTestResponse.id,
                    true,
                    this
                )
                examFees = modelTestResponse.exam_fees.toDouble()
                return
            }
        } else {
            binding.btnJoinNowOrStartTest.text = "Register"
            binding.btnJoinNowOrStartTest.isEnabled = true
            binding.btnJoinNowOrStartTest.setOnClickListener {
                //api request for registration for model test
                if (!dataManager.mPref.prefGetUserInfo().name.isNullOrEmpty()) {
                    examFees = modelTestResponse.exam_fees.toDouble()
                    liveExamHeartCost = modelTestResponse.hearts_cost
                    this.liveExamHeartCost = modelTestResponse.hearts_cost
                    this.modelTestId = modelTestResponse.id
                    if (liveExamHeartCost > 0) {
                        if (dataManager.mPref.prefGetUserHeart()
                                ?.toInt() ?: 0 >= this.liveExamHeartCost
                        ) {
                            showAlertDialogCosting(liveExamHeartCost, "live_exam")
                        } else {
                            showHeartBuyDialog(this.liveExamHeartCost.toString())
                        }
                    } else {
                        completeRegistration()
                    }
                } else {
                    showAlertDialog()
                }
            }
        }
        restOfTheSetModelTest(startOrResumeText, modelTestResponse, false)
    }


    private fun initSslPayment() {
        val sslCommerzInitialization = SSLCommerzInitialization(
            "gurukluborglive",
            "607FA869551A380646",
            examFees,
            SSLCCurrencyType.BDT,
            paymentInfo.tnx_id,
            "education",
            SSLCSdkType.LIVE
        )
        IntegrateSSLCommerz
            .getInstance(activity)
            .addSSLCommerzInitialization(sslCommerzInitialization)
            .buildApiCall(this)
    }


    private fun showAlertDialog() {
        AlertDialog.Builder(activity)
            .setTitle("You need to update your profile before joining the exam.")
            .setPositiveButton(
                "UPDATE PROFILE"
            ) { _, _ ->
                val intent = Intent(activity, ProfileSetupActivity::class.java)
                intent.putExtra("isEdit", true)
                startActivity(intent)
            }.show()
    }

    private fun showAlertDialogCosting(heartCost: Int, type: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("This exam will cost $heartCost hearts. Will you continue ?")
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            menuItemChangeListener?.updateGameHeart(heartCost.toString(), GAME_HEART_MINUS)
            when (type) {
                "live_exam" -> {
                    completeRegistration()
                }
                "new_model_test" -> {
                    if (selectedModeId != -1) {
                        Handler().postDelayed({
                            examStartListener?.onExamStart()
                            startActivity(
                                Intent(activity, GameActivity::class.java).putExtra(
                                    "game_mode",
                                    filterGameModes.single { it.id.toInt() == selectedModeId })
                                    .putExtra("heartCosting", heartCost > 0)
                            )
                        }, 1000)
                    } else {
                        showToast(activity, "Select an exam type")
                    }
                }
                "quick_exam" -> {
                    Handler().postDelayed({
                        examStartListener?.onExamStart()
                        startActivity(
                            Intent(activity, GameActivity::class.java).putExtra(
                                "game_mode", quickExam[0]
                            ).putExtra("heartCosting", heartCost > 0)
                        )
                    }, 1000)
                }
                "practice_exam" -> {
                    Handler().postDelayed({
                        examStartListener?.onExamStart()
                        startActivity(
                            Intent(
                                context,
                                ModelTestActivity::class.java
                            ).putExtra("model_test_id", practiceExamTestId)
                                .putExtra("model_test_title", practiceExamTitle)
                                .putExtra("isFromPractice", true)
                                .putExtra("heartCosting", heartCost > 0)
                        )
                    }, 1000)
                }
            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun completeRegistration() {
        if (!isFromEmptyModelTest) {
            viewModel.apiGetTime(this)
            viewModel.apiModelTestRegister(
                dataManager.mPref.prefGetUserInfo().id,
                this.modelTestId!!,
                true,
                this
            )
        } else {
            notifyModelTest(
                "Start",
                modelTestRegistrationResponse?.model_test_id
            )
            isFromEmptyModelTest = false
        }
    }

    private fun restOfTheSetModelTest(
        startOrResumeText: String,
        modelTest: ModelTest,
        isPractice: Boolean
    ) {
        binding.btnSyllabus.setOnClickListener {
            startActivity(
                Intent(activity, ContentActivity::class.java).putExtra(
                    "syllabus",
                    modelTestResponse?.model_test?.get(0)?.syllabus
                ).putExtra(ContentActivity.ACTIVITY_TITLE, ContentActivity.TITLE_SYLLABUS)
            )
        }

        binding.btnSyllabus1.setOnClickListener {
            startActivity(
                Intent(activity, ContentActivity::class.java).putExtra(
                    "syllabus",
                    modelTestResponse?.model_test?.get(0)?.syllabus
                ).putExtra(ContentActivity.ACTIVITY_TITLE, ContentActivity.TITLE_SYLLABUS)
            )
        }

        //check current time is smaller then model test time, if current time is smaller then
        // start timer else init start test or set no model test view
        if (isPractice) {
            initPracticeStartTest(startOrResumeText, modelTest)
        } else if (!isPractice) {
            if ((testDate?.time ?: 0) - (currentTime?.time ?: 0) > 0) {
                startTimer((testDate?.time ?: 0) - (currentTime?.time ?: 0), modelTest)
            } else {
                if (singleModelTest?.register == 1) {
                    initStartTest(startOrResumeText, modelTest)
                } else {
                    setNoModelTestView()
                }
            }
        }
    }

    private fun initPracticeStartTest(startOrResumeText: String, modelTest: ModelTest) {
        binding.btnJoinNowOrStartTest1.text = startOrResumeText
        binding.btnJoinNowOrStartTest1.isEnabled = true

        binding.textViewExamName1.text = modelTest.title
        binding.textViewExamTime1.visibility = View.GONE
        binding.btnJoinNowOrStartTest1.visibility = View.VISIBLE
        binding.btnSyllabus1.visibility = View.VISIBLE

        if (modelTest.hearts_cost > 0)
            binding.heartCostTextview1.text = modelTest.hearts_cost.toString()

        binding.btnJoinNowOrStartTest1.setOnClickListener {
            if (modelTest.hearts_cost > 0) {
                if (dataManager.mPref.prefGetUserHeart()?.toInt() ?: 0 >= this.liveExamHeartCost) {
                    showAlertDialogCosting(modelTest.hearts_cost, "practice_exam")
                } else {
                    showHeartBuyDialog(modelTest.hearts_cost.toString())
                }
            } else {
                examStartListener?.onExamStart()
                startActivity(
                    Intent(activity, ModelTestActivity::class.java).putExtra(
                        "model_test_id",
                        practiceExamTestId
                    )
                        .putExtra("model_test_title", practiceExamTitle)
                        .putExtra("isFromPractice", true)
                        .putExtra("heartCosting", modelTest.hearts_cost > 0)
                )
            }
        }

        binding.textViewCountdown1.visibility = View.GONE
        binding.textViewCountdown1.text = "00:00:00:00"
    }

    private fun startTimer(l: Long, modelTest: ModelTest) {
        binding.textViewCountdown.visibility = View.VISIBLE
        if (timer != null) {
            timer?.cancel()
        }
        timer = object : CountDownTimer(l, 1000) {
            override fun onTick(mil: Long) {

                var millisUntilFinished = mil
                val days: Long = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                millisUntilFinished -= TimeUnit.DAYS.toMillis(days)

                val hours: Long = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)

                val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

                val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

                val f: NumberFormat = DecimalFormat("00")

                binding.textViewCountdown.text =
                    "$days day  ${f.format(hours)} hour  ${f.format(minutes)} min  ${
                    f.format(
                        seconds
                    )
                    } sec left"

            }

            override fun onFinish() {
                if (singleModelTest?.register == 1) {
                    initStartTest("Start Now", modelTest)
                } else {
                    setNoModelTestView()
                }
                timer?.cancel()
            }
        }.start()
    }

    private fun initStartTest(startOrResumeText: String, modelTest: ModelTest) {
        try {
            elapsedTime =
                (currentTime!!.time - simpleDateFormatServer.parse(singleModelTest?.date).time)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        modelTestRegistrationResponse?.let {
            val duration = (it.duration.toInt() * 60 * 1000)
            if (elapsedTime < duration) {
                // Already participated in this exam
                if (modelTest.is_completed == 1) {
                    binding.textViewCountdown.text = getString(R.string.already_participated)
                    binding.textViewCountdown.visibility = View.VISIBLE
                    return
                }
                binding.btnJoinNowOrStartTest.text = startOrResumeText
                binding.btnJoinNowOrStartTest.isEnabled = true

                binding.btnJoinNowOrStartTest.setOnClickListener {
                    startActivity(
                        Intent(
                            activity,
                            ModelTestActivity::class.java
                        ).putExtra("model_test_id", singleModelTest?.id.toString())
                            .putExtra("model_test_title", singleModelTest?.title)
                            .putExtra("heartCosting", modelTest.hearts_cost > 0)

                    )
                }
                binding.textViewCountdown.visibility = View.VISIBLE
                binding.textViewCountdown.text = "00:00:00:00"
            } else {
                setNoModelTestView()
                dataManager.mPref.prefSetModelTestResponseModel(null)
            }
        } ?: kotlin.run {
            showToast(activity, "registration response model data not found")
        }
    }


    private fun initGameModes() {
        selectedQuickExamPos = 0

        if (gameModes.size > 0) {
            selectedModeId = gameModes[0].id.toInt()
        }

        binding.viewPagerExam.removeAllViews()
        binding.viewPagerExam.clearOnPageChangeListeners()
        filterGameModes.clear()
        gameModes.forEach {
            when {
                it.name.contains("custom", true) -> {
                    customGameTypeId = it.id
                    customGameTypeHeartCost = it.hearts_cost
                }
                it.name.contains("quick", true) -> {
                    quickExamTypeId = it.id
                }
                else -> {
                    filterGameModes.add(it)
                }
            }
        }

        if (filterGameModes.size < 0) {
            binding.textViewNoQuickExam.visibility = View.VISIBLE
            binding.viewPagerExam.visibility = View.GONE
            return
        } else {
            binding.textViewNoQuickExam.visibility = View.GONE
            binding.viewPagerExam.visibility = View.VISIBLE
        }

        binding.viewPagerExam.adapter =
            QuickExamAdapter(activity, filterGameModes, this as QuickExamAdapter.OnItemClick)

        binding.viewPagerExam.offscreenPageLimit = gameModes.size

        binding.viewPagerExam.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.examPagerPageIndicatorView.selection = position
                if (position < binding.viewPagerExam.size) {
                    updateViewPagerOutline(position, ColorUtil.getColorByPosition(position))
                }

                updateViewPagerOutline(selectedQuickExamPos, R.color.lightAsh)
                selectedQuickExamPos = position
                Log.d("CurrentPos->", position.toString())
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })

        updateViewPagerOutline(0, ColorUtil.getColorByPosition(0))
        binding.viewPagerExam.currentItem = 0
        binding.viewPagerExam.pageMargin = DisplayUtil.dpToPx(16, requireActivity())
    }


    private fun updateViewPagerOutline(position: Int, color: Int) {
        val containerBg: GradientDrawable = binding.viewPagerExam[position]
            .findViewById<ConstraintLayout>(R.id.constraintLayoutContainer).background as GradientDrawable
        containerBg.setStroke(
            DisplayUtil.dpToPx(2, requireActivity()),
            ContextCompat.getColor(requireActivity(), color)
        )
    }


    override fun onLoading(isLoader: Boolean, key: String) {
        binding.swipeRefreshLayout.isRefreshing = false
        if (key == "apiModelTestRegister" || key == "apiGetCheckModelTest") {
            if (isLoader) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.howToGetCoinTv -> {
                startActivity(
                    Intent(activity, ContentActivity::class.java).putExtra(
                        ContentActivity.ACTIVITY_TITLE,
                        ContentActivity.TITLE_GAME_RULES
                    )
                )
            }
        }
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "dataGetSubjectListByCategory" -> {
                val subjectListDTO = result as List<SubjectDTO>
                if (subjectListDTO.isNotEmpty()) {
                    val subjectList = SubjectDTO.toSubjects(subjectListDTO)
                    allSubjectList.clear()
                    allSubjectList.addAll(subjectList)
                    setAllSubjectIds(allSubjectList)
                    if (customExamBottomSheet != null) {
                        customExamBottomSheet.populateSubjectList(allSubjectList)
                    }
                    Log.d("subjectlist->", subjectList.size.toString())
                } else {
                    viewModel.apiGetSubjectListByCategory(
                        dataManager.mPref.prefGetUserInfo().category_id.toString(),
                        this
                    )
                }
            }
        }
    }

    private fun setAllSubjectIds(items: ArrayList<Subject>) {
        items.forEach {
            subjectIds = if (subjectIds == "") {
                it.id.toString()
            } else {
                subjectIds + "," + it.id
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
        print(optName + result.toString())
    }

    override fun onSubjectLayoutClicked() {
        customExamBottomSheet.showSelectSubjectDialog(allSubjectList, true)
    }

    override fun onSubmitBtnClicked(subjectIds: String, time: String, questionNum: String) {
        customExamBottomSheet.dismiss()
        if (dataManager.mPref.prefGetUserHeart()?.toInt() ?: 0 >= customGameTypeHeartCost.toInt()) {
            if (customGameTypeHeartCost.toInt() == 0) {
                this.subjectIds = subjectIds
                startCustomExam(time, questionNum)
            } else {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("   This exam will cost $customGameTypeHeartCost hearts. Will you continue ?")
                builder.setPositiveButton("Yes") { _, _ ->
                    menuItemChangeListener?.updateGameHeart(
                        customGameTypeHeartCost,
                        GAME_HEART_MINUS
                    )
                    Handler().postDelayed({
                        this.subjectIds = subjectIds
                        startCustomExam(time, questionNum)
                    }, 1000)
                }
                builder.setNegativeButton("No") { dialogInterface, which ->
                    dialogInterface.dismiss()
                }

                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        } else {
            showHeartBuyDialog(customGameTypeHeartCost)
        }
    }

    private fun startCustomExam(time: String, questionNum: String) {
        examStartListener?.onExamStart()
        val intent = Intent(activity, GameActivity::class.java)
        val customGameMode =
            GameChallengeItem(customGameTypeId, "Custom Exam", "-1", "custom")
        if (subjectIds.isNotEmpty())
            this.subjectIds = subjectIds

        intent.putExtra("game_mode", customGameMode)
        intent.putExtra("subject_ids", this.subjectIds)
        intent.putExtra("time", time)
        intent.putExtra("number_of_ques", questionNum)
        intent.putExtra("custom_game_id", customGameTypeId)
        intent.putExtra("heartCosting", customGameTypeHeartCost.toInt() > 0)
        startActivity(intent)
    }

    override fun getModelRegData(
        modelTestId: String,
        isFromPractice: Boolean,
        title: String,
        heartsCost: Int
    ) {
        this.modelTestId = modelTestId
        Log.d("current-heart", dataManager.mPref.prefGetUserHeart().toString())

        if (!isFromPractice) {
            completeRegistration()
        } else {
            practiceExamTestId = modelTestId
            practiceExamTitle = title
            startActivity(
                Intent(
                    context,
                    ModelTestActivity::class.java
                ).putExtra("model_test_id", practiceExamTestId)
                    .putExtra("model_test_title", practiceExamTitle)
                    .putExtra("isFromPractice", true)
                    .putExtra("heartCosting", heartsCost > 0)
            )
        }
    }

    override fun purchaseExam(modelTestId: String, examFees: String, heart_cost: Int) {
        if (examFees.toDouble() > 0.0) {
            this.examFees = examFees.toDouble()
            viewModel.apiModelTestRegistrationPayment(modelTestId, this)
        } else {
            liveExamHeartCost = heart_cost
            showAlertDialogCosting(heart_cost, "live_exam")
            viewModel.apiModelTestRegister(
                dataManager.mPref.prefGetUserInfo().id,
                modelTestId,
                true,
                this
            )
        }
    }

    override fun initiateCostDialog(heartCost: Int, type: String, modelTestId: String) {
        this.modelTestId = modelTestId
        showAlertDialogCosting(heartCost, type)
    }

    override fun initiateCostDialogPractice(
        heartCost: Int,
        type: String,
        practiceExamId: String,
        practiceExamTitle: String
    ) {
        this.practiceExamTestId = practiceExamId
        this.practiceExamTitle = practiceExamTitle

        showAlertDialogCosting(heartCost, type)
    }

    override fun initiateHeartBuyDialog(heartCost: Int) {
        showHeartBuyDialog(heartCost.toString())
    }

    override fun examInitiated() {
        examStartListener?.onExamStart()
    }

    override fun onRootclicked(position: Int) {
        selectedModeId = filterGameModes[position].id.toInt()
        if (dataManager.mPref.prefGetLoginMode()) {
            var heartCosts = filterGameModes[position].hearts_cost.toInt()
            if (dataManager.mPref.prefGetUserHeart()?.toInt() ?: 0 >= heartCosts) {
                showHeartCostDialog(filterGameModes[position].hearts_cost, "quickexam", position)
            } else {
                showHeartBuyDialog(filterGameModes[position].hearts_cost)
            }
        } else {
            if (showLoginDialogListener != null)
                showLoginDialogListener?.listenShowLoginDialog()
        }
    }

    private fun showHeartCostDialog(heartsCost: String, examtype: String, position: Int) {
        if (heartsCost.toInt() == 0) {
            if (selectedModeId != -1) {
                examStartListener?.onExamStart()
                startActivity(
                    Intent(activity, GameActivity::class.java).putExtra(
                        "game_mode",
                        filterGameModes.single { it.id.toInt() == selectedModeId })
                        .putExtra("heartCosting", heartsCost.toInt() > 0)
                )
            } else {
                showToast(activity, "Select an exam type")
            }
        } else {
            showAlertDialogCosting(heartsCost.toInt(), "new_model_test")
        }
    }

    private fun showHeartBuyDialog(heart_cost: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("You don't have enough hearts, you need $heart_cost hearts to continue this exam. Do You want to buy hearts now?")
        builder.setPositiveButton("Yes") { _, _ ->
            startActivity(
                Intent(context, HeartAddActivity::class.java).putExtra(
                    "is_from_game",
                    "no"
                )
            )
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun transactionSuccess(p0: SSLCTransactionInfoModel?) {
        viewModel.getPaymentExecution(
            this,
            p0?.status,
            p0?.tranDate,
            paymentInfo.tnx_id,
            p0?.valId,
            p0?.amount,
            p0?.storeAmount,
            p0?.bankTranId,
            p0?.cardType,
            p0?.cardNo
        )
    }

    override fun transactionFail(p0: String?) {
        viewModel.getFailedTransaction("FAILED", p0.toString(), paymentInfo.tnx_id, this)
        hideProgressDialog()
    }

    override fun merchantValidationError(p0: String?) {
        viewModel.getFailedTransaction("INVALID", p0.toString(), paymentInfo.tnx_id, this)
        hideProgressDialog()
    }

    interface ExamStartListener {
        fun onExamStart()
    }


}