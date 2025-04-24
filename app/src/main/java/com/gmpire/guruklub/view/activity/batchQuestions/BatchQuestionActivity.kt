package com.gmpire.guruklub.view.activity.batchQuestions

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ActivityBatchQuestionsBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.RxBusEvents
import com.gmpire.guruklub.util.UpdateClass
import com.gmpire.guruklub.view.BottomSheet.AnswerDescriptionBottomSheet
import com.gmpire.guruklub.view.BottomSheet.ReportBottomSheet
import com.gmpire.guruklub.view.activity.gameActivity.BatchQuestionAdapter
import com.gmpire.guruklub.view.activity.infoCenter.InfoCenterViewModel
import com.gmpire.guruklub.view.activity.login.RelatedVideoActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.questionShare.QuestionShareActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.dialog.ImageViewDialog
import com.google.android.gms.ads.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception
import java.lang.NullPointerException


class BatchQuestionActivity : BaseActivity(), BatchQuestionAdapter.OnActionListener,
    AnswerDescriptionBottomSheet.Listener, ReportBottomSheet.IBottomSheetDialogClicked {

    private lateinit var answerDescriptionBottomSheet: AnswerDescriptionBottomSheet
    private lateinit var batch: Common
    private var filterValues : FilterValues? =null
    private var currentPosition: Int = 0
    private lateinit var adaper: BatchQuestionAdapter
    private lateinit var binding: ActivityBatchQuestionsBinding
    private lateinit var viewmodel: InfoCenterViewModel
    private var questions: ArrayList<Question> = arrayListOf()
    private var adView: AdView? = null
    var swipeRightCount = 0
    private var interstitialCount = 20
    private var mCurrentFragmentPosition: Int = 0
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var reportBottomSheet: ReportBottomSheet
    private var isAdsFree: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_batch_questions)

        batch = intent.getSerializableExtra("batch") as Common

        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmerAnimation()
        isAdsFree = dataManager.mPref.prefGetIsAdFree()
        if (!isAdsFree) {
            adView = AdView(this)
            binding.rlAdContainerBatchQuestion.addView(adView)
            loadBanner()
        }
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
                        this@BatchQuestionActivity,
                        R.color.lightAsh
                    )
                )
                binding.viewBorderBottom.setBackgroundColor(
                    ContextCompat.getColor(
                        this@BatchQuestionActivity,
                        R.color.lightAsh
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

    override fun onLoading(isLoader: Boolean, key: String) {
//        if (isLoader) {
//            showProgressDialog("Please Wait")
//        } else {
//            hideProgressDialog()
//        }
    }

    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.rlAdContainerBatchQuestion.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }


    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, batch.name, true)

        viewmodel =
            ViewModelProviders.of(this, viewModelFactory).get(InfoCenterViewModel::class.java)

        viewmodel.apiGetQuestionListByBatch(
            dataManager.mPref.prefGetUserInfo().id,
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            "",
            "",
            "",
            batch.id,
            this
        )
            buildInterstitialAd()
    }

    private fun buildInterstitialAd() {
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = this.getString(R.string.ad_unit_id_interstitial_test)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun initTablayout() {
        adaper = BatchQuestionAdapter(this, questions, this)
        binding.questionsViewpager.adapter = adaper
        binding.questionTablayout.setViewPager(binding.questionsViewpager)

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
                    Log.d("CurrentQuesNo->", swipeRightCount.toString())
                } else {
                    // user is going to the left page
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
                }
                currentPosition = position;
            }
        })

        // To start scroll from center
        binding.questionTablayout.viewTreeObserver.addOnGlobalLayoutListener {
            val padding = (resources.displayMetrics.widthPixels / 2.2).toInt()
            binding.questionTablayout.setPadding(padding, 0, padding, 0)
            binding.questionTablayout.clipToPadding = false
        }

    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetQuestionListByBatch" -> {
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
                            try {
                                questions = baseData.data!!
                                if (questions.size > 0) {
                                    initTablayout()
                                    binding.emptyMessage.visibility = View.GONE
                                    binding.questionsViewpager.visibility = View.VISIBLE
                                } else {
                                    binding.emptyMessage.visibility = View.VISIBLE
                                    binding.questionsViewpager.visibility = View.GONE
                                }
                            } catch (ex: NullPointerException) {
                                ex.printStackTrace()
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
            "apiGetMoreQuestionListByBatch" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Question>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Question>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            questions.addAll(baseData.data ?: arrayListOf())
                            binding.questionsViewpager.adapter?.notifyDataSetChanged()
                        }
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
                if (this::reportBottomSheet.isInitialized)
                    reportBottomSheet.dismiss()
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
                        showToast(this, baseData.message[0])
                        binding.questionsViewpager.adapter?.notifyDataSetChanged()
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
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                        showToast(this, baseData.message[0])
                        binding.questionsViewpager.adapter?.notifyDataSetChanged()
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }
    }

    override fun onError(err: Throwable, key: String) {
        super.onError(err, key)
        binding.shimmerViewContainer.stopShimmerAnimation()
        binding.shimmerViewContainer.visibility = View.GONE
    }


    override fun onClick(v: View?) {

    }

    override fun onSubmitAnswer(position: Int, answered_position: Int) {
        swipeRightCount++
        if (swipeRightCount % 20 == 0) {
            if (isAdsFree == false) {
                Log.d("Adloadingfrombatch", "Adloadingfrombatch")
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                }
            }
        }
        if (questions[position].answer == answered_position) {
            answerDescriptionBottomSheet =
                AnswerDescriptionBottomSheet(questions[position], true, this)
        } else {
            answerDescriptionBottomSheet =
                AnswerDescriptionBottomSheet(questions[position], false, this)
        }
        answerDescriptionBottomSheet.show(supportFragmentManager, answerDescriptionBottomSheet.tag)
    }

    override fun onImageClicked(position: Int) {
        if (!questions[position].picture.isNullOrBlank()) {
            val dialogImageView =
                ImageViewDialog(this, questions[position].picture)
            dialogImageView.show()
        }
    }


    override fun loadMore() {

    }

    override fun onReportProblemClicked(position: Int) {
        reportBottomSheet = ReportBottomSheet(questions[position].id)
        reportBottomSheet.setBottomDialogListener(this, false,false)
        supportFragmentManager.let {
            reportBottomSheet.show(
                it,
                reportBottomSheet.tag
            )
        }
    }

    override fun onShareClicked(position: Int) {
        startActivity(
            Intent(this, QuestionShareActivity::class.java).putExtra(
                "question",
                questions[position]
            ).putExtra("action", "share")
        )
        overridePendingTransition(R.anim.scale_up, R.anim.hold)
    }

    override fun onDownloadClicked(position: Int) {
        startActivity(
            Intent(this, QuestionShareActivity::class.java).putExtra(
                "question",
                questions[position]
            ).putExtra("action", "download")
        )
        overridePendingTransition(R.anim.scale_up, R.anim.hold)
    }

    override fun onBookmarkClicked(position: Int) {
        if (questions[position].is_bookmarked == 0) {
            viewmodel.apiBookmarkQuestion(
                questions[position].id,
                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                this
            )
            questions[position].is_bookmarked = 1
        } else {
            viewmodel.apiUnBookmarkQuestion(
                questions[position].id,
                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                this
            )
            questions[position].is_bookmarked = 0
        }
    }

    override fun onOkClicked() {
        binding.questionsViewpager.currentItem++
        answerDescriptionBottomSheet.dismiss()
    }

    override fun onDismissed() {
    }

    override fun onVideoBtnClicked(question:Question) {
        filterValues = FilterValues()
        filterValues?.category_id = "1"
        filterValues?.subject_id = question.subject_id
        filterValues?.topic_id = question.topic_id
        filterValues?.section_id = question.section_id
        Log.d("filter",filterValues.toString())
        val intent = Intent(this, RelatedVideoActivity::class.java)
        intent.putExtra("filterValues", Gson().toJson(filterValues))
        this.startActivity(intent)
    }

    override fun onReportSubmitted(question_id: String, type: String, details: String) {
        viewmodel.apiReportAboutQuestion(question_id, type, details, this)
    }

    override fun onVideoReportSubmitted(question_id: String, type: String, details: String) {

    }

    override fun onReportDialogDismiss() {
    }
}
