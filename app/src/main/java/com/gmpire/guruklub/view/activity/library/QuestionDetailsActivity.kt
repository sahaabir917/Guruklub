package com.gmpire.guruklub.view.activity.library

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.QuestionDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.data.model.viewSolution.GameSolutionResponse
import com.gmpire.guruklub.databinding.ActivityQuestionDetailsBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.BottomSheet.ReportBottomSheet
import com.gmpire.guruklub.view.activity.login.RelatedVideoActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.questionShare.QuestionShareActivity
import com.gmpire.guruklub.view.adapter.QuestionDetailsAdapter
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.dialog.NewsDetailsGestureDialog
import com.gmpire.guruklub.view.fragment.home.HomeFragmentViewModel
import com.google.android.gms.ads.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import okhttp3.ResponseBody
import retrofit2.Response

class QuestionDetailsActivity : BaseActivity(), ReportBottomSheet.IBottomSheetDialogClicked,
    IDatabaseCallBack, QuestionDetailsAdapter.OnItemClickedListener {

    private var gameId: String? = ""
    private var isViewSolution: String = ""
    private var isFavourite: String = ""
    private var hasNextPage: Boolean = false
    private var page: Int? = 1
    private var isLibraryQuestion: String = ""
    private var questionType = 0
    private var isError: String = ""
    private lateinit var binding: ActivityQuestionDetailsBinding
    private lateinit var reportBottomSheet: ReportBottomSheet
    private lateinit var questions: ArrayList<Question>
    private var position = 0
    private lateinit var viewModel: HomeFragmentViewModel
    private var bookmarkMap: HashMap<Int, Int> = hashMapOf()
    private var isAdsFree: Boolean = false
    private var filterValues: FilterValues? = null
    private var passingFilterValues: FilterValues? = FilterValues()
    private var adapter: QuestionDetailsAdapter? = null
    private var adView: AdView? = null
    private var interstitialCount = 20
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var newsDetailsGestureDialog: NewsDetailsGestureDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_question_details)

        //Load Ad
        //MobileAds.initialize(this) {}
        isAdsFree = dataManager.mPref.prefGetIsAdFree() ?: false
        if (!isAdsFree) {
            adView = AdView(this);
            binding.rlAdContainerQuesDet.addView(adView);
            loadBanner()
            buildInterstitialAd()
        }

        if (dataManager.mPref.prefGetIsFirstTimeGestureQuestionDetails()) {
            newsDetailsGestureDialog = NewsDetailsGestureDialog.newInstance()
            newsDetailsGestureDialog.show(supportFragmentManager, newsDetailsGestureDialog.tag)
        }

    }

    private fun buildInterstitialAd() {
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = this.getString(R.string.ad_unit_id_interstitial_test)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
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
                Log.d("LoadedAd->", "Successful")
                binding.viewBorderTop.setBackgroundColor(
                    ContextCompat.getColor(
                        this@QuestionDetailsActivity,
                        R.color.white
                    )
                )
                binding.viewBorderBottom.setBackgroundColor(
                    ContextCompat.getColor(
                        this@QuestionDetailsActivity,
                        R.color.white
                    )
                )
                binding.addSpaceQuesDet.visibility = View.GONE
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                Log.d("ErrorAd->", errorCode.toString())
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
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

            var adWidthPixels = binding.rlAdContainerQuesDet.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "", true)
        binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.toolbar.appCompatTextViewLogo.text = (Html.fromHtml(text))
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(HomeFragmentViewModel::class.java)
        viewModel.iDatabaseCallBack = this

        viewModel.dataGetAllPassedTempQuestions()
        position = 0
        position = intent.getIntExtra("position", 0)
        filterValues = Gson().fromJson(
            intent.getStringExtra("filterValues"),
            FilterValues::class.java
        )

        questionType = intent.getIntExtra("question_type", 0)
        hasNextPage = intent.getBooleanExtra("hasNextPage", false)
        page = intent.getIntExtra("page", 1)
        gameId = intent.getStringExtra("game_id")

        binding.relativeLayoutRightArrow.setOnClickListener(this)
        binding.relativeLayoutLeftArrow.setOnClickListener(this)
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun showHidePreviousAndNextButton(position: Int) {
        if (position == 0) {
            binding.relativeLayoutLeftArrow.alpha = 0.5f
            binding.relativeLayoutLeftArrow.isEnabled = false
            var questionsize = questions.size - 1
            if (questionsize > 0) {
                binding.relativeLayoutRightArrow.alpha = 1.0f
                binding.relativeLayoutRightArrow.isEnabled = true
            } else if (questionsize <= 0) {
                binding.relativeLayoutRightArrow.alpha = 0.5f
                binding.relativeLayoutRightArrow.isEnabled = false
            }
        } else {
            //right left two enabled
            rightLeftArrowEnabled()
        }

        if (position == questions.size - 1) {
            rightArrowDisabled()
        } else {
            binding.relativeLayoutRightArrow.alpha = 1.0f
            binding.relativeLayoutRightArrow.isEnabled = true
        }

    }

    private fun rightLeftArrowEnabled() {
        binding.relativeLayoutRightArrow.alpha = 0.5f
        binding.relativeLayoutRightArrow.isEnabled = false
        binding.relativeLayoutLeftArrow.alpha = 1.0f
        binding.relativeLayoutLeftArrow.isEnabled = true
    }

    private fun rightArrowDisabled() {
        binding.relativeLayoutRightArrow.alpha = 0.5f
        binding.relativeLayoutRightArrow.isEnabled = false
        binding.relativeLayoutLeftArrow.alpha = 1.0f
        binding.relativeLayoutLeftArrow.isEnabled = true
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiBookmarkQuestion" -> {
                val type = object : TypeToken<BaseModel<ArrayList<EmptyModel>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<EmptyModel>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        showToast(this, baseData.message[0])
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                    } else {
                        showToast(this, baseData.message[0])
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
                        showToast(this, baseData.message[0])
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                    } else {
                        showToast(this, baseData.message[0])
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
                        showToast(this, baseData.message[0])
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
                reportBottomSheet.dismiss()
            }
            "apiGetQuestionLibrary" -> {
                val type =
                    object :
                        TypeToken<BaseModel<PaginationModel<java.util.ArrayList<Question>>>>() {}.type
                result.data?.body()?.let {
                    if (result.data.body() != null) {
                        val baseData =
                            Gson().fromJson<BaseModel<PaginationModel<java.util.ArrayList<Question>>>>(
                                result.data.body()?.string(),
                                type
                            )
                        if (baseData.status_code == 200 && baseData.data?.data != null) {
                            if (baseData.data?.data?.size ?: 0 > 0) {
                                hasNextPage = baseData.data?.next_page != 0
                                questions.addAll(baseData.data!!.data!!)
                                page = baseData.data?.next_page ?: 0
                            }
                        } else {
                            showToast(this, baseData.message[0])
                        }
                    }
                }
            }

            "apiGetGameSolution" -> {
                val type = object : TypeToken<BaseModel<GameSolutionResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameSolutionResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            if (baseData.data?.data != null && baseData.data?.data?.size ?: 0 > 0) {
                                hasNextPage = baseData.data?.next_page != 0
                                page = baseData.data?.next_page ?: 0
                                questions.addAll(baseData.data!!.data as ArrayList<Question>)
                                binding.questionDetailsViewerpager.adapter?.notifyDataSetChanged()
                            }
                        } else {
                            showToast(this, baseData.message[0])
                        }
                    }
                }
            }
            "apiGetFavouriteQuestion" -> {
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
                                questions.addAll(baseData.data!!.data as ArrayList<Question>)
                                binding.questionDetailsViewerpager.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.relativeLayoutRightArrow -> {
                if (!ConnectivityUtil.isOnline(this)) {
                    NoInternetDialogs()
                } else {
                    position++
                    showHidePreviousAndNextButton(position)
                }
            }
            binding.relativeLayoutLeftArrow -> {
                position--
                showHidePreviousAndNextButton(position)
            }
        }
    }

    private fun PopupWindow.dimBehind() {
        try {
            val container = contentView.rootView
            val context = contentView.context
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = container.layoutParams as WindowManager.LayoutParams
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            params.dimAmount = 0.5f
            wm.updateViewLayout(container, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showReportBottomSheet(popupWindow: PopupWindow, position: Int) {
        reportBottomSheet = ReportBottomSheet(questions[position].id)
        reportBottomSheet.setBottomDialogListener(this, false, false)
        reportBottomSheet.show(
            supportFragmentManager,
            reportBottomSheet.tag
        )
        popupWindow.dismiss()
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (isLoader) {
            if (key == "apiGetQuestionLibrary") {
                hideProgressDialog()
            } else if (key == "apiGetGameSolution") {
                hideProgressDialog()
            } else if (key == "apiGetFavouriteQuestion") {
                hideProgressDialog()
            } else {
                showProgressDialog("Please wait")
            }
        } else {
            hideProgressDialog()
        }
    }

    override fun onReportSubmitted(question_id: String, type: String, details: String) {
        viewModel.apiReportAboutQuestion(question_id, type, details, this)
    }

    override fun onVideoReportSubmitted(question_id: String, type: String, details: String) {

    }

    override fun onReportDialogDismiss() {
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "dataGetAllPassedTempQuestions" -> {
                val gotPassedQuesDTO = result as List<QuestionDTO>
                if (gotPassedQuesDTO.isNotEmpty()) {
                    questions = QuestionDTO.toQuestions(gotPassedQuesDTO) as ArrayList<Question>
                    initQuestionDetailsViewPager(position)
                }
            }
        }
    }

    private fun initQuestionDetailsViewPager(position: Int) {
        adapter = QuestionDetailsAdapter(
            this,
            questions,
            this as QuestionDetailsAdapter.OnItemClickedListener
        )
        binding.questionDetailsViewerpager.adapter = adapter
        binding.questionDetailsViewerpager.currentItem = position
        binding.questionDetailsViewerpager.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (position == questions.size - 4)
                    if (hasNextPage) {
                        when (questionType) {
                            ConstantField.QUESTION_TYPE_ERROR -> {
                                viewModel.apiGetGameSolution(
                                    page.toString(),
                                    dataManager.mPref.prefGetUserInfo().id,
                                    "",
                                    "profile",
                                    filterValues!!,
                                    this@QuestionDetailsActivity
                                )
                            }
                            ConstantField.QUESTION_TYPE_FAVOURITE -> {
                                viewModel.apiGetFavouriteQuestion(
                                    page.toString(),
                                    filterValues!!,
                                    dataManager.mPref.prefGetUserInfo().id,
                                    this@QuestionDetailsActivity
                                )
                            }
                            ConstantField.QUESTION_TYPE_VIEW_SOLUTION -> {
                                gameId?.let {
                                    viewModel.apiGetGameSolution(
                                        page.toString(),
                                        dataManager.mPref.prefGetUserInfo().id,
                                        it,
                                        "game",
                                        filterValues!!,
                                        this@QuestionDetailsActivity
                                    )
                                }
                            }
                            ConstantField.QUESTION_TYPE_LIBRARY -> {
                                viewModel.apiGetQuestionLibraryPage(
                                    dataManager.mPref.prefGetUserInfo().id,
                                    filterValues!!,
                                    page.toString(),
                                    this@QuestionDetailsActivity
                                )
                            }
                        }
                    }

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
                dataManager.mPref.prefSetIsFirstTimeGestureQuestionDetails(false)
            }
        })

    }

    override fun onFailedDB(result: Any, optName: String) {
        print(result.toString())
    }

    override fun onBackPressed() {
        // super.onBackPressed()
        val intent = Intent()
        intent.putExtra("bookmark_map", bookmarkMap)
        setResult(2, intent)
        finish()

    }

    override fun onBookmarkitemClicked(position: Int) {
        if (questions[position].is_bookmarked == 0) {
            viewModel.apiBookmarkQuestion(
                questions[position].id,
                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                this
            )
            questions[position].is_bookmarked = 1
            bookmarkMap[position] = 1
        } else {
            viewModel.apiUnBookmarkQuestion(
                questions[position].id,
                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                this
            )
            questions[position].is_bookmarked = 0
            bookmarkMap[position] = 0
        }
    }

    override fun onSharedItemClicked(position: Int) {
        startActivity(
            Intent(this, QuestionShareActivity::class.java).putExtra(
                "question",
                questions[position]
            ).putExtra("action", "share")
        )
        this.overridePendingTransition(R.anim.scale_up, R.anim.hold)

    }

    override fun onDownloadItemClicked(position: Int) {
        startActivity(
            Intent(this, QuestionShareActivity::class.java).putExtra(
                "question",
                questions[position]
            ).putExtra("action", "download")
        )
        this.overridePendingTransition(R.anim.scale_up, R.anim.hold)
    }

    override fun onReportItemClicked(position: Int, popupWindow: PopupWindow) {
        showReportBottomSheet(popupWindow, position)
        Log.d("Report_Pos->", position.toString())
    }

    override fun onRelatedBtnClicked(position: Int) {
        filterValues = FilterValues()
        filterValues?.category_id = "1"
        filterValues?.subject_id = questions[position].subject_id
        filterValues?.topic_id = questions[position].topic_id
        filterValues?.section_id = questions[position].section_id
        filterValues?.batch_id = ""
        filterValues?.difficulty_id = ""
        Log.d("filter", filterValues.toString())
        startActivity(
            Intent(this, RelatedVideoActivity::class.java).putExtra(
                "filterValues", Gson().toJson(filterValues)
            )
        )
    }


}