package com.gmpire.guruklub.view.activity.modelTestActivity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.game.GameResultSubmitRequestItem
import com.gmpire.guruklub.data.model.modelTest.ModelTestQuestion
import com.gmpire.guruklub.data.model.modelTest.ModelTestRegistrationResponse
import com.gmpire.guruklub.data.model.modelTest.TimeResponse
import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import com.gmpire.guruklub.databinding.ActivityModelTestBinding
import com.gmpire.guruklub.util.ConnectivityUtil
import com.gmpire.guruklub.util.DateUtil
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.appEvents.AppEvent
import com.gmpire.guruklub.view.activity.gameResultActivity.GameResultActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.dialog.ImageViewDialog
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class ModelTestActivity : BaseActivity(), ModelTestQuestionAdapter.OnActionListener,
    RewardedVideoAdListener {

    private var timeleft: Long = 0L
    private var elapsedTime: Long = 0L

    private var currentPosition: Int = 0
    private var timer: CountDownTimer? = null
    private lateinit var adaper: ModelTestQuestionAdapter
    private lateinit var viewModel: ModelTestViewModel
    private lateinit var binding: ActivityModelTestBinding
    private lateinit var questions: ArrayList<ModelTestQuestion>
    private var gameResponseQuestions: ArrayList<GameResponseQuestion> = ArrayList()
    private var gameResultSubmitRequestItem: GameResultSubmitRequestItem? = null
    private var currentTime: Date? = null
    private var adView: AdView? = null
    private var isAdsFree: Boolean? = false

    private lateinit var mInterstitialAd: InterstitialAd
    private var modelTestId = ""
    private var isFinishGameCalled = false
    private var modelTestTitle = ""
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private var isFromPractice = false
    private var heartCosting = false


    private lateinit var modelTestRegistrationResponse: ModelTestRegistrationResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_model_test)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(ModelTestViewModel::class.java)

        //Load Ad
        //MobileAds.initialize(this) {}
        isAdsFree = dataManager.mPref.prefGetIsAdFree()
        modelTestId = intent.extras?.get("model_test_id").toString()
        modelTestTitle = intent.extras?.get("model_test_title").toString()

        if (intent.extras?.containsKey("isFromPractice")!!)
            isFromPractice = true
        if (intent.extras?.containsKey("heartCosting")!!)
            heartCosting = intent.getBooleanExtra("heartCosting", false)

        viewModel.apiModelTestParticipation(
            modelTestId,
            dataManager.mPref.prefGetUserInfo().id,
            this
        )

        binding.examTitle.text = modelTestTitle

        //loadInterstitialAd()
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this

        if (isFromPractice)
            viewModel.apiModelTestRegister(
                dataManager.mPref.prefGetUserInfo().id,
                modelTestId,
                false,
                this
            )

        loadRewardedVideoAd()
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(
            getString(R.string.ad_unit_id_reward_test),
            AdRequest.Builder().build()
        )
    }

    private fun loadInterstitialAd() {
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.ad_unit_id_interstitial_test)
        mInterstitialAd.loadAd(AdRequest.Builder().build())

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                if (ConnectivityUtil.isOnline(this@ModelTestActivity)) {
                    if (!isFinishGameCalled) {
                        finishGame()
                        isFinishGameCalled = true
                    }
                }
            }
        }
    }

    private fun loadBanner() {
        val adRequest =
            AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build()
        adView?.adSize = adSize
        adView?.loadAd(adRequest)
    }


    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.rlAdContainerModel.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    override fun onPause() {
        super.onPause()
        mRewardedVideoAd.pause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRewardedVideoAd.destroy(this)
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Live Exam", true)
        binding.toolbar.finishBtn.visibility = View.GONE

    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        if (!isFromPractice)
            viewModel.apiGetTime(this)
        mRewardedVideoAd.resume(this)
    }

    private fun initTablayout(modelTestRegistrationResponse: ModelTestRegistrationResponse) {
        AppEvent.getInstance(this)
            .logModelTestStartEvent(
                dataManager.mPref.prefGetUserInfo().name ?: "",
                "Model Test"
            )

        adaper = ModelTestQuestionAdapter(this, questions, this)
        binding.questionsViewpager.adapter = adaper
        binding.questionTablayout.setViewPager(binding.questionsViewpager)

        startTimer(timeleft)

        updateAnsweredCount(questions)

        // To start scroll from center
        binding.questionTablayout.viewTreeObserver.addOnGlobalLayoutListener {
            val padding = (resources.displayMetrics.widthPixels / 2.2).toInt()
            binding.questionTablayout.setPadding(padding, 0, padding, 0)
            binding.questionTablayout.clipToPadding = false
        }

        binding.questionsViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                currentPosition = position
                modelTestRegistrationResponse.current_position = currentPosition
                dataManager.mPref.prefSetModelTestRegistrationResponseById(
                    modelTestRegistrationResponse,
                    modelTestId
                )
            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                modelTestRegistrationResponse.current_position = currentPosition
                dataManager.mPref.prefSetModelTestRegistrationResponseById(
                    modelTestRegistrationResponse,
                    modelTestId
                )
            }
        })

        binding.questionsViewpager.currentItem = modelTestRegistrationResponse.current_position

        binding.finishBtn1.setOnClickListener {
            showAlertDialogFinish()
        }

        questions.forEach {
            if (it.answered) {
                binding.questionTablayout.getTabAt(questions.indexOf(it)).background =
                    ContextCompat.getDrawable(this, R.drawable.bg_green_dot)
            }
        }
    }

    private fun startTimer(l: Long) {
        timer?.let {
            it.cancel()
            binding.countdownTimerTv.text = ""
        }

        timer = object : CountDownTimer(l, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.countdownTimerTv.text = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )
                            )
                )
                questions[currentPosition].time += 1000
                Log.e("MT_ISSUE", "${currentPosition} time: ${questions[currentPosition].time}")
                dataManager.mPref.prefSetModelTestRegistrationResponseById(
                    modelTestRegistrationResponse,
                    modelTestId
                )
            }

            override fun onFinish() {
                if (mRewardedVideoAd != null) {
                    if (isAdsFree == false && !heartCosting) {
                        if (mRewardedVideoAd.isLoaded) {
                            mRewardedVideoAd.show()
                        } else {
                            retryAfterFewSec()
                        }
                    } else {
                        if (!isFinishGameCalled) {
                            if (ConnectivityUtil.isOnline(this@ModelTestActivity)) {
                                finishGame()
                                isFinishGameCalled = true
                            }
                        }
                    }
                } else {
                    if (!isFinishGameCalled) {
                        if (ConnectivityUtil.isOnline(this@ModelTestActivity)) {
                            finishGame()
                            isFinishGameCalled = true
                        }
                    }
                }
                timer?.cancel()
            }
        }.start()
    }

    private fun finishGame() {
        gameResponseQuestions.clear()
        //dataManager.mPref.setModelTestAttendanceById(true, modelTestId)
        var totalTime = 0L
        questions.forEach {
            val gameResponseQuestion: GameResponseQuestion
            if (!it.answered) {
                it.answered_position = "-1"
                gameResponseQuestion = GameResponseQuestion(
                    -1,
                    it.question_id,
                    it.topic_id,
                    it.section_id,
                    it.subject_id,
                    it.time,
                    it.answered_position?.toInt() ?: -1
                )
            } else {
                var status: Int
                if (it.answer == it.answered_position?.toInt()) {
                    status = 1
                } else {
                    status = 0
                }
                gameResponseQuestion = GameResponseQuestion(
                    status,
                    it.question_id,
                    it.topic_id,
                    it.section_id,
                    it.subject_id,
                    it.time,
                    it.answered_position?.toInt() ?: -1 + 1
                )
            }

            totalTime += it.time
            Log.e("MT_ISSUE", "TIME : ${questions.indexOf(it)} : ${it.time / 1000 * 60}")
            Log.e("MT_ISSUE", "TOTAL TIME : ${totalTime / 1000 * 60}")
            gameResponseQuestions.add(gameResponseQuestion)
        }

        if (!isFromPractice) {
            gameResultSubmitRequestItem = GameResultSubmitRequestItem()
            gameResultSubmitRequestItem?.questions = gameResponseQuestions
            gameResultSubmitRequestItem?.time = totalTime
            gameResultSubmitRequestItem?.type = modelTestRegistrationResponse.model_type_id
            gameResultSubmitRequestItem?.model_test_id = modelTestRegistrationResponse.model_test_id
            gameResultSubmitRequestItem?.exam_status = true
            gameResultSubmitRequestItem?.slug = "model_test"
        } else {
            gameResultSubmitRequestItem = GameResultSubmitRequestItem()
            gameResultSubmitRequestItem?.questions = gameResponseQuestions
            gameResultSubmitRequestItem?.time = totalTime
            gameResultSubmitRequestItem?.type = modelTestRegistrationResponse.model_type_id
            gameResultSubmitRequestItem?.model_test_id = modelTestRegistrationResponse.model_test_id
            gameResultSubmitRequestItem?.exam_status = false
            gameResultSubmitRequestItem?.slug = "model_test"
        }


        //use sharedpreference for save model_test_id

        dataManager.mPref.setModeltestid(gameResultSubmitRequestItem!!.model_test_id!!)

        Log.e("game_submit_model", Gson().toJson(gameResultSubmitRequestItem).toString())

        startActivity(
            Intent(this, GameResultActivity::class.java)
                .putExtra("from_activity", "model_test")
                .putExtra(
                    "result",
                    gameResultSubmitRequestItem
                )
        )

        dataManager.mPref.prefSetModelTestResponseModelById(null, modelTestId)
        dataManager.mPref.prefSetModelTestRegistrationResponseById(null, modelTestId)

        finish()
    }

    private fun updateAnsweredCount(questions: ArrayList<ModelTestQuestion>) {
        var i = 0
        questions.forEach {
            if (it.answered) {
                i++
            }
        }
        binding.answeredQuestionCountTv.text = "$i/${questions.size}"
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
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
                            if (!modelTestId.isNullOrEmpty() && modelTestId != "null") {
                                modelTestRegistrationResponse =
                                    dataManager.mPref.prefGetModelTestRegistrationResponseById(
                                        modelTestId
                                    ) ?: ModelTestRegistrationResponse(
                                        "", "",
                                        "", listOf(), "", -1
                                    )
                            }
                            if (!modelTestRegistrationResponse.question.isNullOrEmpty()) {
                                questions =
                                    modelTestRegistrationResponse.question as ArrayList<ModelTestQuestion>
                            }

                            try {
                                currentTime =
                                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(baseData.data?.time)
                                val modelTestStartTime =
                                    dataManager.mPref.prefGetModelTestResponseModelById(modelTestId).date
                                elapsedTime =
                                    (currentTime!!.time - DateUtil.simpleDateFormatServer.parse(
                                        modelTestStartTime
                                    ).time)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            timeleft =
                                (modelTestRegistrationResponse.duration.toInt() * 60 * 1000) - elapsedTime
                            if (timeleft > 0) {
                                initTablayout(modelTestRegistrationResponse)
                            } else {
                                if (ConnectivityUtil.isOnline(this)) {
                                    if (!isFinishGameCalled) {
                                        finishGame()
                                        isFinishGameCalled = true
                                    }
                                }
                            }
                        }
                    } else {
                        showToast(this, baseData.message[0])
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
                        modelTestRegistrationResponse = baseData.data!!
                        if (!modelTestRegistrationResponse.question.isNullOrEmpty()) {
                            questions =
                                modelTestRegistrationResponse.question as ArrayList<ModelTestQuestion>
                        }
                        elapsedTime = 0
                        timeleft =
                            (modelTestRegistrationResponse.duration.toInt() * 60 * 1000) - elapsedTime
                        initTablayout(modelTestRegistrationResponse)
                    }
                }
            }

        }
    }

    override fun onClick(v: View?) {}

    override fun onSubmitAnswer(position: Int, answered_position: String) {
        questions[position].run {
            answered = true
            this.answered_position = answered_position
        }
        adaper.notifyDataSetChanged()
        updateAnsweredCount(questions)

        if (binding.questionsViewpager.currentItem != questions.size - 1) {
            binding.questionsViewpager.currentItem = position + 1
        }

        binding.questionTablayout.getTabAt(position).background =
            ContextCompat.getDrawable(this, R.drawable.bg_green_dot)

        modelTestRegistrationResponse.current_position = currentPosition
        dataManager.mPref.prefSetModelTestRegistrationResponseById(
            modelTestRegistrationResponse,
            modelTestId
        )
    }

    override fun onImageClicked(position: Int) {
        val dialogImageView =
            ImageViewDialog(this, questions[position].picture)
        dialogImageView.show()
    }

    override fun loadMore() {

    }

    override fun onBackPressed() {
        showAlertDialog()
    }

    private fun showAlertDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("You can not quit a live exam until finished")
            .setPositiveButton(
                "Finish Live Exam"
            ) { dialog, which ->
                timer?.onFinish()
            }.setNegativeButton(
                "Cancel"
            ) { dialog, which ->
                dialog.dismiss()
            }.show()
    }

    private fun showAlertDialogFinish() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Are you sure want to finish the live exam?")
            .setPositiveButton(
                "Yes"
            ) { dialog, which ->
                timer?.onFinish()
                if (isAdsFree == false && !heartCosting) {
                    if (mRewardedVideoAd.isLoaded) {
                        mRewardedVideoAd.show()
                    } else {
                        retryAfterFewSec()
                    }
                } else {
                    if (ConnectivityUtil.isOnline(this)) {
                        if (!isFinishGameCalled) {
                            finishGame()
                            isFinishGameCalled = true
                        }
                    }
                }
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }.show()
    }

    private fun retryAfterFewSec() {
        binding.progressBarModelTest.visibility = View.VISIBLE
        Handler().postDelayed({
            if (mRewardedVideoAd.isLoaded) {
                if (isAdsFree == false && !heartCosting) {
                    mRewardedVideoAd.show()

                }
            } else {
                if (ConnectivityUtil.isOnline(this)) {
                    if (!isFinishGameCalled) {
                        finishGame()
                        isFinishGameCalled = true
                    }
                }
            }
            binding.progressBarModelTest.visibility = View.VISIBLE
        }, 3500)
    }

    override fun onRewardedVideoAdClosed() {
        if (ConnectivityUtil.isOnline(this))
            finishGame()
    }

    override fun onRewardedVideoAdLeftApplication() {

    }

    override fun onRewardedVideoAdLoaded() {

    }

    override fun onRewardedVideoAdOpened() {

    }

    override fun onRewardedVideoCompleted() {

    }

    override fun onRewarded(p0: RewardItem?) {

    }

    override fun onRewardedVideoStarted() {

    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {

    }


}
