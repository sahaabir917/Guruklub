package com.gmpire.guruklub.view.activity.gameActivity

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.game.GameChallengeItem
import com.gmpire.guruklub.data.model.game.GameResponse
import com.gmpire.guruklub.data.model.game.GameResultSubmitRequestItem
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ActivityGameBinding
import com.gmpire.guruklub.util.ConnectivityUtil
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.appEvents.AppEvent
import com.gmpire.guruklub.view.activity.gameResultActivity.GameResultActivity
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
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
import java.util.concurrent.TimeUnit


class GameActivity : BaseActivity(), GameQuestionsAdapter.OnActionListener,
    RewardedVideoAdListener {

    private var isFromDialog: Boolean = false
    private var currentPosition: Int = 0
    private var interstitialCount = 20
    private var isFinished: Boolean = false
    private var timer: CountDownTimer? = null
    private lateinit var adaper: GameQuestionsAdapter
    private lateinit var gameResponse: GameResponse
    private lateinit var viewModel: GameActivityViewModel
    private lateinit var binding: ActivityGameBinding
    private var questions: ArrayList<Question> = arrayListOf()
    private var gameResponseQuestions: ArrayList<GameResponseQuestion> = arrayListOf()
    private lateinit var gameMode: GameChallengeItem
    private lateinit var gameResultSubmitRequestItem: GameResultSubmitRequestItem
    private var lastTime: Long = 0L
    private var totlaAddedTime: Long = 0L
    private var millisUntilFInished: Long = 0L
    private var isTimerRunning = false
    private var adView: AdView? = null
    private var customTime: String? = null
    var swipeRightCount = 0
    var isAdsFree: Boolean = false
    private var heartCosting = false

    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(GameActivityViewModel::class.java)
        gameResultSubmitRequestItem = GameResultSubmitRequestItem()
        isAdsFree = dataManager.mPref.prefGetIsAdFree() ?: false

        //buildInterstitialAd()
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this

        if (intent.extras?.containsKey("heartCosting")!!)
            heartCosting = intent.getBooleanExtra("heartCosting", false)

        loadRewardedVideoAd()
        //loadInterstitialAd()
        //loadBanner()
    }

    private fun buildInterstitialAd() {
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = this.getString(R.string.ad_unit_id_interstitial_test)
        mInterstitialAd.loadAd(AdRequest.Builder().build())
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
                if (isFinished) {
                    if (ConnectivityUtil.isOnline(this@GameActivity)) {
                        finishGame()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mRewardedVideoAd.pause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRewardedVideoAd.destroy(this)
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(
            getString(R.string.ad_unit_id_reward_test),
            AdRequest.Builder().build()
        )
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

            var adWidthPixels = binding.rlAdContainerGame.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    override fun viewRelatedTask() {
        gameMode = intent.getSerializableExtra("game_mode") as GameChallengeItem
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmerAnimation()

        if (gameMode.type == "custom") {
            val quesNum: String? = intent.extras?.getString("number_of_ques")
            val subjectIds: String? = intent.extras?.getString("subject_ids")
            customTime = intent.extras?.getString("time")

            dataManager.mPref.prefGetUserInfo().category_id?.let {
                viewModel.apiGetCustomGameQuestions(
                    it,
                    gameMode.id,
                    quesNum?.toInt() ?: 0,
                    subjectIds ?: "",
                    this
                )
            }
        } else if (gameMode.type == "test") {
            val filterValuesString = intent.getStringExtra("filter_values")
            if (filterValuesString != null) {
                val filterValues =
                    Gson().fromJson<FilterValues>(filterValuesString, FilterValues::class.java)
                viewModel.apiGetQuestionLibrary(
                    dataManager.mPref.prefGetUserInfo().id,
                    filterValues, this
                )
            }
        } else {
            dataManager.mPref.prefGetUserInfo().category_id?.let {
                viewModel.apiGetGameQuestions(
                    it,
                    gameMode.id,
                    this
                )
            }
        }

        initFinishButton()

        binding.Examtitle.text = gameMode.name

        setToolbar(this, binding.toolbar, "", true)
        binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.toolbar.appCompatTextViewLogo.text = (Html.fromHtml(text))
        binding.toolbar.finishBtn.visibility = View.VISIBLE

        binding.playPauseBtn.setOnClickListener {
            if (isTimerRunning) {
                setTimerPause()
            } else {
                setTimerResume()
            }
        }
    }

    private fun initFinishButton() {
        binding.finishBtn2.setOnClickListener {
            if (mRewardedVideoAd != null) {
                if (!isAdsFree && !heartCosting) {
                    if (mRewardedVideoAd.isLoaded) {
                        isFinished = true
                        mRewardedVideoAd.show()
                    } else {
                        retryAfterFewSec(true)
                    }
                } else {
                    if (ConnectivityUtil.isOnline(this))
                        finishGame()
                }
            } else {
                if (ConnectivityUtil.isOnline(this))
                    finishGame()
            }
        }
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun setTimerPause() {
        timer?.cancel()
        binding.textViewPlayPause.text = "Resume"
        isTimerRunning = false
        binding.imageViewPlayPause.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_play_png_icon
            )
        )
        binding.imageViewPlayPause.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white));
        binding.countdownTimerTv.setTextColor(ContextCompat.getColor(this, R.color.red))
    }

    private fun setTimerResume() {
        timerResume()
        binding.textViewPlayPause.text = "Pause"
        isTimerRunning = true
        binding.imageViewPlayPause.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_pause
            )
        )
        binding.countdownTimerTv.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.colorPrimary
            )
        )
    }

    override fun onResume() {
        super.onResume()
        if (timer != null && isTimerRunning)
            timerResume()
        mRewardedVideoAd.resume(this)
    }

    override fun onStop() {
        super.onStop()
        timerPause()
    }

    private fun initTablayout(gameResponse: GameResponse?) {
        AppEvent.getInstance(this)
            .logLiveExamParticipantEvent(
                dataManager.mPref.prefGetUserInfo().name ?: "",
                gameResponse?.game_time ?: "00:00:00"
            )

        questions = gameResponse?.question as ArrayList<Question>
        val tempQuestion = ArrayList<Question>()

        tempQuestion.addAll(questions)

        for (it in questions) {
            if (it.section_id == null) {
                tempQuestion.remove(it)
                continue
            }

            if (it.subject_id == null) {
                tempQuestion.remove(it)
                continue
            }

            if (it.topic_id == null) {
                tempQuestion.remove(it)
                continue
            }
        }

        questions.clear()
        questions.addAll(tempQuestion)

        questions.forEach {
            it.answered_position = -1
            val gameResponseQuestion = GameResponseQuestion(
                -1,
                it.id,
                it.topic_id,
                it.section_id,
                it.subject_id,
                0L,
                it.answered_position,
                it.answer
            )
            gameResponseQuestions.add(gameResponseQuestion)
        }

        adaper = GameQuestionsAdapter(this, questions, this)
        binding.questionsViewpager.adapter = adaper
        binding.questionTablayout.setViewPager(binding.questionsViewpager)

        if (customTime?.isEmpty() != false)
            startTimer((gameResponse.game_time.toLong() * 60 * 1000))
        else
            startTimer(((customTime?.toLong() ?: 0) * 60 * 1000))

        updateAnsweredCount(questions)

        // To start scroll from center
        binding.questionTablayout.viewTreeObserver.addOnGlobalLayoutListener {
            val padding = (resources.displayMetrics.widthPixels / 2.2).toInt()
            binding.questionTablayout.setPadding(padding, 0, padding, 0)
            binding.questionTablayout.clipToPadding = false
        }

        binding.questionsViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                gameResponseQuestions[currentPosition].time += lastTime
                totlaAddedTime += lastTime
                currentPosition = position

                if (position == 0)
                    return
            }
        })


        binding.toolbar.finishBtn.setOnClickListener {
            timer?.onFinish()
            if (!isAdsFree && !heartCosting) {
                if (mRewardedVideoAd.isLoaded) {
                    mRewardedVideoAd.show()
                }
            } else {
                retryAfterFewSec(true)
            }
        }
    }

    private fun retryAfterFewSec(isFinished: Boolean) {
        Handler().postDelayed({
            if (mRewardedVideoAd.isLoaded) {
                if (isFinished)
                    this.isFinished = true
                if (!isAdsFree && !heartCosting) {
                    mRewardedVideoAd.show()
                }
            } else {
                if (isFinished) {
                    if (ConnectivityUtil.isOnline(this)) {
                        binding.progressBarGame.visibility = View.VISIBLE
                        finishGame()
                    }
                }
                Log.d("TAG", "The interstitial wasn't loaded yet.")
            }
        }, 3500)
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
                lastTime =
                    (gameResponse.game_time.toLong() * 60 * 1000) - totlaAddedTime - millisUntilFinished
                millisUntilFInished = millisUntilFinished
            }

            override fun onFinish() {
                if (mRewardedVideoAd != null) {
                    if (!isAdsFree && !heartCosting) {
                        if (mRewardedVideoAd.isLoaded) {
                            isFinished = true
                            mRewardedVideoAd.show()
                        } else {
                            retryAfterFewSec(true)
                        }
                    } else {
                        if (ConnectivityUtil.isOnline(this@GameActivity))
                            finishGame()
                    }
                } else {
                    if (ConnectivityUtil.isOnline(this@GameActivity))
                        finishGame()
                }
                timer?.cancel()
            }
        }.start()
        isTimerRunning = true
    }

    private fun finishGame() {
        if (gameResponseQuestions.isNotEmpty())
            gameResponseQuestions[currentPosition].time += lastTime
        totlaAddedTime += lastTime
        gameResultSubmitRequestItem.questions = gameResponseQuestions
        // gameResultSubmitRequestItem.time = SimpleDateFormat("mm:ss").format(Date(totlaAddedTime))
        gameResultSubmitRequestItem.time = totlaAddedTime
        gameResultSubmitRequestItem.type = gameMode.id
        gameResultSubmitRequestItem.exam_status = true

        Log.e("game_submit_model", Gson().toJson(gameResultSubmitRequestItem).toString())

        val fromActivity = if (gameMode.type == "test") {
            "test"
        } else {
            "game"
        }

        val intent = Intent(this, GameResultActivity::class.java)
            .putExtra("from_activity", fromActivity)
            .putExtra(
                "result",
                gameResultSubmitRequestItem
            )
        if (gameMode.type == "test") {
            intent.putExtra("questions", Gson().toJson(questions))
        }
        startActivity(intent)
        finish()
    }

    private fun updateAnsweredCount(questions: ArrayList<Question>) {
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
            "apiGetGameQuestions" -> {
                binding.shimmerViewContainer.stopShimmerAnimation()
                binding.shimmerViewContainer.visibility = View.GONE
                val type = object : TypeToken<BaseModel<GameResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            try {
                                gameResponse = baseData.data!!
                                gameResponse.current_hearts?.let { it1 ->
                                    dataManager.mPref.prefSetUserHeart(
                                        it1
                                    )
                                }
                                initTablayout(gameResponse)
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else if (baseData.status_code == 500) {
                        showHeartBuyDialog()
//                        showToast(this, baseData.message[0])
//                        val intent = Intent(this, MainActivity::class.java).putExtra("goto",
//                            MyFirebaseMessagingService.GAME_SCREEN
//                        )
//                        finishAffinity()
//                        startActivity(intent)
                    }
                }
            }
            "apiGetQuestionLibrary" -> {
                binding.shimmerViewContainer.stopShimmerAnimation()
                binding.shimmerViewContainer.visibility = View.GONE
                val type = object : TypeToken<BaseModel<java.util.ArrayList<Question>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Question>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        val tenRandomQuestions = getRandomTenQuestions(baseData.data!!)
                        gameResponse = GameResponse("6", tenRandomQuestions, "10")
                        initTablayout(gameResponse)
                    }
                }
            }
        }
    }

    private fun showHeartBuyDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("You have not enough hearts, Do You want to buy hearts")
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            startActivity(
                Intent(this, HeartAddActivity::class.java).putExtra(
                    "is_from_game",
                    "no"
                )
            )
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
            isFromDialog = true
            onBackPressed()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onError(err: Throwable, key: String) {
        super.onError(err, key)
        binding.shimmerViewContainer.stopShimmerAnimation()
        binding.shimmerViewContainer.visibility = View.GONE
    }

    private fun getRandomTenQuestions(data: ArrayList<Question>): ArrayList<Question> {
        data.shuffle()
        return if (data.size > 10)
            ArrayList(data.subList(0, 10))
        else
            data
    }


    override fun onClick(v: View?) {
    }

    override fun onSubmitAnswer(position: Int, answered_position: Int) {
        if (!isTimerRunning) {
            showToast(this, "Please resume your exam.")
            return
        }
        swipeRightCount++
        if (questions[position].answer == answered_position) {
            gameResponseQuestions[position].status = 1
        } else {
            gameResponseQuestions[position].status = 0
        }
        gameResponseQuestions[position].answer = answered_position + 1


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

    }

    override fun onImageClicked(position: Int) {
        val dialogImageView =
            ImageViewDialog(this, questions[position].picture)
        dialogImageView.show()
    }

    override fun loadMore() {}

    override fun onBackPressed() {
        if (!isFromDialog)
            showAlertDialog()
        else
            super.onBackPressed()
    }

    private fun showAlertDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Do you want to quit exam ?")
            .setPositiveButton(
                "Yes"
            ) { dialog, which ->
                super.onBackPressed()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }.show()
    }

    fun timerPause() {
        timer?.cancel()
    }

    private fun timerResume() {
        startTimer(millisUntilFInished)
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
        // finishGame()
    }

    override fun onRewarded(p0: RewardItem?) {

    }

    override fun onRewardedVideoStarted() {

    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {

    }

}
