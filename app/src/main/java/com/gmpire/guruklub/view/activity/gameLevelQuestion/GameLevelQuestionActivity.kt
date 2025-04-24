package com.gmpire.guruklub.view.activity.gameLevelQuestion

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.gameChallenge.GameChallenge
import com.gmpire.guruklub.data.model.gameheartpackages.GameLevelUpResponse
import com.gmpire.guruklub.data.model.gamelevel.GameUserState
import com.gmpire.guruklub.data.model.gamelevel.Level
import com.gmpire.guruklub.data.model.gamequestions.GameQuestionSet
import com.gmpire.guruklub.data.model.gamequestions.GameQuestions
import com.gmpire.guruklub.data.model.heartsettings.HeartVariation
import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import com.gmpire.guruklub.databinding.ActivityGameLevelQuestionBinding
import com.gmpire.guruklub.util.GameServiceHelper
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.appEvents.AppEvent
import com.gmpire.guruklub.view.BottomSheet.ReportBottomSheet
import com.gmpire.guruklub.view.activity.gameChallenge.GameChallengeActivity
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
import com.gmpire.guruklub.view.activity.gamelevel.GAME_HEART_MINUS
import com.gmpire.guruklub.view.activity.gamelevel.LEVEL_UP_CHALLENGE
import com.gmpire.guruklub.view.activity.gamelevel.SCORE_BASED_CHALLENGE
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.customView.CustomScrollView
import com.gmpire.guruklub.view.dialog.gameLevel.GameChallengeDialog
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelInfoDialog
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelOverDialog
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelUpDialog
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_game.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.concurrent.TimeUnit
import kotlin.math.abs


private const val QUESTION_JUMP_DELAY: Long = 1000

class GameLevelQuestionActivity : BaseActivity(),
    GameLevelQuestionAdapter.OnActionListener,
    GameChallengeDialog.ActionListener,
    GameLevelUpDialog.ActionListener,
    GameLevelInfoDialog.GameLevelInfoDialogListener,
    GameLevelOverDialog.IPurchaseDialogClicked,
    IDatabaseCallBack,
    ReportBottomSheet.IBottomSheetDialogClicked, CustomScrollView.OnScrollTouchCancelListener {

    private var currentHearts = 0
    private var currentRewardHearts = 0
    private var currentLevelScore: Float = 0f
    private var targetLevelScore = 0f
    private var answerElapsedTime: Long = 0
    private var interstitialCount = 29
    private var pagerPosition = 0

    private var answeredQuestions = mutableListOf<GameResponseQuestion>()
    private lateinit var binding: ActivityGameLevelQuestionBinding
    private var adapter: GameLevelQuestionAdapter? = null
    private var questions = arrayListOf<GameQuestionSet>()
    private lateinit var viewModel: GameLevelQuestionViewModel
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaPlayerCountDown: MediaPlayer
    private var timer: CountDownTimer? = null

    private var gameLevel: Level? = null
    private var currentLevel: Level? = null
    private var nextLevel: Level? = null
    private var challenge: GameChallenge? = null
    private var challenges: ArrayList<GameChallenge> = arrayListOf()

    private var gameChallengeDialog: GameChallengeDialog? = null
    private val gameLevelOverDialog = GameLevelOverDialog.newInstance()
    private var gameLevelUpDialog: GameLevelUpDialog? = null
    private lateinit var gameLevelInfoDialog: GameLevelInfoDialog
    private lateinit var challengeDetailsDialog: GameLevelInfoDialog
    private lateinit var reportBottomSheet: ReportBottomSheet
    private lateinit var manager: ReviewManager
    private lateinit var reviewInfo: ReviewInfo
    private lateinit var mInterstitialAd: InterstitialAd

    private var holdUsed = false
    private var backToLevelList = false
    private var isActivityChanging: Boolean = false
    private var isTransitionTime: Boolean = false
    private var isAnimationStarted = false
    private var gameSound: Boolean = false
    private var isCurrentLevelStop = false
    private var gameResumable = false
    private var doNotLoadMore = false
    private var pauseMode = true
    private var isSkipped = false
    private lateinit var timerInstance: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game_level_question)
    }

    override fun viewRelatedTask() {
        getIntentExtra()
        resumeUserState()

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(GameLevelQuestionViewModel::class.java)

        viewModel.iDatabaseCallBack = this

        gameLevelInfoDialog = GameLevelInfoDialog.newInstance(
            "Level Completed",
            getString(R.string.levle_complete_text),
            false,
            true
        )

        challengeDetailsDialog = GameLevelInfoDialog.newInstance(
            "Challenge Details",
            challenge?.description ?: "",
            false,
            true
        )

        targetLevelScore = (gameLevel?.target_points ?: "10000").toFloat()
        bindUiData()
        binding.ivHeartAdd.setOnClickListener {
            stopCountDownSound()
            checkGameSound()
            gameResumable = true
            isActivityChanging = true
            goToHeartAddPage()
        }
        bindScore()

        // Review info
        manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                reviewInfo = request.result
            }
        }

        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        buildInterstitialAd()

    }

    private fun buildInterstitialAd() {
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
                isCurrentLevelStop = false
                isTransitionTime = false
                holdUsed = false
            }
        }
    }

    private fun startHeartBeatAnimation() {
        binding.ivHeartAdd.setImageResource(R.drawable.heart_animation)
        val heartBeatAnimation: AnimationDrawable = binding.ivHeartAdd.drawable as AnimationDrawable
        heartBeatAnimation.isOneShot = true
        heartBeatAnimation.start()
    }

    private fun bindQuestions() {
        adapter = null
        Log.d("QuestionSize->", questions.size.toString())
        adapter = GameLevelQuestionAdapter(this, questions, this)
        binding.questionsViewpager.adapter = adapter
        binding.questionsViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (!isCurrentLevelStop) {
                    startGameCountDown(getTime(questions[position].is_math == "1"))
                }
            }

            override fun onPageSelected(position: Int) {
                pagerPosition = position
            }
        })
    }

    private fun clearTimerTxt() {
        binding.textViewTime.text = "00.00"
        binding.textViewTime.setTextColor(Color.parseColor("#FFD904"))
        binding.textViewTime.clearAnimation()
    }

    private fun startGameCountDown(l: Long) {
        timer?.let {
            it.cancel()
            clearTimerTxt()
        }
        timer = object : CountDownTimer(l, 1000) {
            override fun onFinish() {
                timer?.let {
                    if (!pauseMode && !isCurrentLevelStop) timeExpired()
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                timerInstance = this
                answerElapsedTime += 1
                binding.textViewTime.text = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )
                            )
                )
                if (millisUntilFinished < 10000 && !isAnimationStarted) {
                    blinkTimerTxt()
                    playCountDownSound()
                }
            }
        }.start()
    }

    private fun cancelTimer() {
        if (this::timerInstance.isInitialized) {
            timerInstance.cancel()
        }
    }

    private fun timeExpired() {
        val qs = questions[questions_viewpager.currentItem]
        adapter?.disableAllAnswerClick(
            binding.questionsViewpager.findViewWithTag("root" + binding.questionsViewpager.currentItem)
        )
        stopCountDownSound()

        addQuestionAnswer(
            GameResponseQuestion(
                -1,
                qs.id,
                qs.topic_id,
                qs.section_id,
                qs.subject_id,
                0,
                -1,
                qs.answer
            )
        )
        clearTimerTxt()
        isAnimationStarted = false
        isTransitionTime = true
        // Show right answer & wait 5 sec to show user
        adapter?.showRightAnswer(
            binding.questionsViewpager,
            binding.questionsViewpager.currentItem
        )
        answerElapsedTime = 0

        // show game hold info message
        if (dataManager.mPref.getGameMsgShow())
            showSnackBarMsg()

        heartsUpdate()

        if (binding.questionsViewpager.currentItem < questions.size - 1) {
            Handler().postDelayed({
                if (isTransitionTime && !holdUsed)
                    jumpToNextQuestion(binding.questionsViewpager.currentItem + 1)
                holdUsed = false
            }, QUESTION_JUMP_DELAY * 3)
        }
    }

    private fun blinkTimerTxt() {
        if (hasAnyVisibleDialog()) {
            clearTimerTxt()
            return
        }
        binding.textViewTime.setTextColor(Color.RED)
        val anim: Animation = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 200 //You can manage the blinking time with this parameter
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        binding.textViewTime.startAnimation(anim)
        isAnimationStarted = true
    }

    private fun bindUiData() {
        val levelTxt = "Level: ${gameLevel?.level}"
        binding.tvLevelTxt.text = levelTxt
    }

    private fun bindHearts() {
        binding.tvCurrentHearts.text = currentHearts.toString()
    }

    private fun getIntentExtra() {
        try {
            gameLevel = intent.getSerializableExtra("game_level") as Level
            currentLevel = intent.getSerializableExtra("my_level") as Level
            val challengeList = intent.getSerializableExtra("challenges")
            if (challengeList != null) {
                challenges = challengeList as ArrayList<GameChallenge>
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun navigateToHome() {
        cancelTimer()
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (isLoader) {
            binding.progressBarGameLevelQuestion.visibility = View.VISIBLE
        } else {
            binding.progressBarGameLevelQuestion.visibility = View.GONE
        }
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetQuestionListForGame" -> {
                binding.questionsViewpager.visibility = View.VISIBLE
                val type = object : TypeToken<BaseModel<GameQuestions>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameQuestions>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            Log.d("basedata", baseData.toString())
                            var qs = arrayListOf<GameQuestionSet>()
                            questions =
                                baseData.data?.questions as ArrayList<GameQuestionSet>

                            if (!answeredQuestions.isNullOrEmpty()) {
                                val ids = arrayListOf<Int>()
                                answeredQuestions.forEach {
                                    ids.add(it.id.toInt())
                                }

                                qs = ArrayList(questions.filter {
                                    !ids.contains(it.id.toInt())
                                })
                                questions = qs
                                answeredQuestions.clear()
                            }

                            if (questions.size > 0) {
                                if (questions.size <= 2)
                                    doNotLoadMore = true

                                bindQuestions()
                                binding.questionsViewpager.visibility = View.VISIBLE
                            } else {
                                binding.questionsViewpager.visibility = View.GONE
                                showToast(this@GameLevelQuestionActivity, "Something went wrong")
                            }
                        }
                    } else {
                        showToast(
                            this@GameLevelQuestionActivity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiGameLevelOver" -> {
                val type = object : TypeToken<BaseModel<Any>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<Any>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        gameResumable = false
                        answeredQuestions.clear()
                        currentHearts = 0
                        dataManager.mPref.prefSetUserHeart(currentHearts.toString())
                        dataManager.mPref.prefSetUserGameState(null)
                        wipeHeartsFromLocal()
                    } else {
                        showToast(
                            this@GameLevelQuestionActivity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiGameLevelUp" -> {
                val type = object : TypeToken<BaseModel<GameLevelUpResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameLevelUpResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        gameResumable = false
                        answeredQuestions.clear()
                        nextLevel = baseData.data?.new_level
                        Log.d("Hearts->", currentHearts.toString())
                        dataManager.mPref.prefSetUserHeart(baseData.data?.current_hearts.toString())
                        viewModel.getAllGameHeartData()
                        dataManager.mPref.prefSetUserGameState(null)
                        dataManager.mPref.prefSetGameCurrentLevel(nextLevel)
                        challenge = challenges.find {
                            it.milestone_type == LEVEL_UP_CHALLENGE
                        }
                        challenges.clear()
                        challenges.addAll(baseData?.data?.challenge ?: arrayListOf())
                        showLevelUpDialog(baseData.data?.milestone_bonus_hearts)
                    } else {
                        showToast(
                            this@GameLevelQuestionActivity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiGameState" -> {
                val type = object : TypeToken<BaseModel<Any>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<Any>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        gameResumable = true
                        answeredQuestions.clear()
                    } else {
                        showToast(
                            this@GameLevelQuestionActivity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiHeartsUpdate" -> {
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
                        viewModel.getAllGameHeartData()
                    } else {
                        showToast(
                            this@GameLevelQuestionActivity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
        }
    }

    private fun wipeHeartsFromLocal() {
        viewModel.deleteAllGameHeartData()
    }


    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "insertGameHeart" -> {
                animateHeartUpdate("-1")
                if (!pauseMode) {
                    viewModel.apiHeartsUpdate(GAME_HEART_MINUS.toString(), this)
                }
                if (currentHearts == 0) {
                    return
                } else if (currentHearts > 0) {
                    viewModel.getAllGameHeartData()
                }
                if (isSkipped) {
                    jumpToNextQuestion(binding.questionsViewpager.currentItem + 1)
                    isSkipped = false
                }
            }
            "deleteAllGameHeartData" -> {
                viewModel.getAllGameHeartData()
            }
            "getAllGameHeartData" -> {
                currentHearts = dataManager.mPref.prefGetUserHeart()?.toInt() ?: 0
                val hearts = result as List<GameHeartDTO>
                hearts.forEach {
                    if (it.heart_type == GAME_HEART_MINUS) {
                        currentHearts -= 1
                    } else {
                        if(!it.practice.isNullOrEmpty()) {
                            currentHearts += if(it.practice == "0")
                                dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_hearts?.toInt() ?: 0
                            else
                                dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_random_hearts?.toInt() ?: 0
                        }
                    }
                }
                bindHearts()
                if(pauseMode)
                    restOfTheOnResumeFunctionality()
            }
            "dbInsertGameQuestions" -> {
            }
            "dbGetGameQuestions" -> {
            }
        }
    }

    private fun showLevelOverDialog() {
        try {
            stopCountDownSound()
            val transaction = supportFragmentManager.beginTransaction()
            if (!gameLevelOverDialog.isAdded)
                gameLevelOverDialog.show(transaction, "LevelOverDialog")
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onFailedDB(result: Any, optName: String) {

    }


    override fun onSubmitAnswer(
        position: Int,
        isCorrect: Boolean,
        answerResponse: GameResponseQuestion
    ) {
        stopCountDownSound()
        checkGameSound()
        adapter?.enableReportClick(questions_viewpager, position)
        answerResponse.time = answerElapsedTime
        addQuestionAnswer(answerResponse)
        answerElapsedTime = 0
        isTransitionTime = true
        isAnimationStarted = false
        cancelTimer()
        updateScore(isCorrect)
        if (!isCorrect) {
            heartsUpdate()
        }

        // show game hold info message
        if (dataManager.mPref.getGameMsgShow())
            showSnackBarMsg()


        binding.textViewTime.clearAnimation()
        isCurrentLevelStop = false
        if (binding.questionsViewpager.currentItem < questions.size - 1) {
            Handler().postDelayed({
                if (!holdUsed)
                    jumpToNextQuestion(position + 1)
                holdUsed = false
            }, QUESTION_JUMP_DELAY * 2)
        } else {
            showToast(this@GameLevelQuestionActivity, "No More Questions")
        }
    }

    private fun showSnackBarMsg() {
        val snackBar = Snackbar.make(
            binding.clParent, "Hold anywhere to pause question",
            2000
        )
        val view: View = snackBar.getView()
        val tv: TextView =
            view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        tv.setTextColor(Color.WHITE)
        tv.textSize = 14f
        snackBar.setAction("Dismiss") {
            dataManager.mPref.setGameMsgShow()
        }

        val tvAction: TextView =
            view.findViewById<View>(com.google.android.material.R.id.snackbar_action) as TextView
        tvAction.setTypeface(null, Typeface.BOLD)
        tvAction.textSize = 14f
        snackBar.show()
    }

    private fun jumpToNextQuestion(position: Int) {
        // Show level over pop-up if heart is 0 and no Unlimited Hearts Subscription
        if (currentHearts <= 0 && shouldHeartDeduct()) {
            gameLevelOver()
            return
        }

        // Show level end success pop-up
        if (currentLevelScore >= targetLevelScore) {
            if (isPreviousLevel())
                gameLevelInfoDialog.show(supportFragmentManager, "")
            else
                gameLevelUp()
            return
        }

        // Show challenge pop-up after transition delay
        if (hasAnyChallenge() && !isPreviousLevel()) {
            isCurrentLevelStop = true
            initChallengeData()
            return
        }

        if (!dataManager.mPref.prefGetIsAdFree()) {
            if (pagerPosition != 0 && pagerPosition % interstitialCount == 0) {
                interstitialCount += 30
                if (mInterstitialAd.isLoaded) {
                    isCurrentLevelStop = true
                    mInterstitialAd.show()
                    buildInterstitialAd()
                    return
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.")
                }
            }
        }

        if (!isCurrentLevelStop) {
            binding.questionsViewpager.setCurrentItem(position, true)
            isTransitionTime = false
            holdUsed = false
        }
    }

    private fun correctPoint(): Float {
        return (gameLevel?.correct_points ?: "0").toFloat()
    }

    private fun wrongPoint(): Float {
        return abs((gameLevel?.wrong_points ?: "0").toFloat())
    }

    private fun updateScore(correct: Boolean) {
        if (correct) {
            currentLevelScore += correctPoint()
        } else {
            if (currentLevelScore >= 0) {
                currentLevelScore -= wrongPoint()
            }
            if (currentLevelScore < 0) currentLevelScore = 0f
            if (currentLevelScore > targetLevelScore) currentLevelScore = targetLevelScore
        }
        pauseUserState()
        bindScore()
    }

    private fun hasAnyChallenge(): Boolean {
        if (challenges.isNotEmpty()) {
            val nChallenge = challenges.find {
                var mileStoneScore = it.milestone_score
                    ?.toFloat() ?: 0f
                it.milestone_type == SCORE_BASED_CHALLENGE && (currentLevelScore >= mileStoneScore)
            }
            if (nChallenge != null) {
                challenge = nChallenge
                return true
            }
        }
        return false
    }

    private fun initChallengeData() {
        challenge?.let {
            gameChallengeDialog = GameChallengeDialog.newInstance(it, false)
            showChallengeDialog()
            pauseUserState()
        }
    }

    override fun onReportProblemClicked(position: Int) {
        checkGameSound()
        stopCountDownSound()
        reportBottomSheet = ReportBottomSheet(questions[position].id)
        reportBottomSheet.setBottomDialogListener(this, true, false)
        reportBottomSheet.show(
            supportFragmentManager,
            reportBottomSheet.tag
        )
        isCurrentLevelStop = true
    }

    override fun onPause() {
        if (!isActivityChanging)
            GameServiceHelper.stopMusic(this)
        super.onPause()
        pauseMode = true
        if (gameResumable) {
            pauseUserState()
        }

        if (currentLevelScore < targetLevelScore) {
            if (answerElapsedTime != 0L && !isCurrentLevelStop) {
                heartsUpdate()
            }
        }

        stopCountDownSound()
        binding.tvUpdateHeart.visibility = View.GONE
        cancelTimer()
    }

    private fun pauseUserState() {
        gameLevel?.id?.let {
            runOnUiThread {
                var challengeId: String? = if (challenge?.id == null) null else challenge?.id.toString()
                viewModel.apiGamePauseUserState(
                    it,
                    currentLevelScore.toString(),
                    currentHearts.toString(),
                    challengeId,
                    ArrayList(answeredQuestions),
                    this
                )
                setUserStateToLocal(it)
            }
        }
    }


    private fun checkGameSound() {
        if (gameSound) {
            playBtnSound()
        }
    }


    private fun playBtnSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.button_press)
        mediaPlayer.isLooping = false
        mediaPlayer.start()
    }


    private fun playCountDownSound() {
        if (hasAnyVisibleDialog()) {
            stopCountDownSound()
            return
        }
        stopCountDownSound()
        try {
            if (gameSound) {
                mediaPlayerCountDown = MediaPlayer.create(this, R.raw.ticking)
                if (mediaPlayerCountDown != null && !mediaPlayerCountDown.isPlaying) {
                    mediaPlayerCountDown.isLooping = false
                    mediaPlayerCountDown.start()
                }
            }
        } catch (ex: java.lang.IllegalStateException) {
            ex.printStackTrace()
        }
    }

    private fun stopCountDownSound() {
        try {
            if (this::mediaPlayerCountDown.isInitialized) {
                if (mediaPlayerCountDown.isPlaying) {
                    mediaPlayerCountDown.stop()
                    mediaPlayerCountDown.release()
                }
            }
        } catch (ex: java.lang.IllegalStateException) {
            ex.printStackTrace()
        }
    }

    private fun setUserStateToLocal(id: String) {
        dataManager.mPref.prefSetUserGameState(
            GameUserState(
                null,
                currentHearts.toString(),
                id,
                currentLevelScore.toString(),
                getUserId().toString(),
                gameLevel,
                ArrayList(answeredQuestions)
            )
        )
    }


    override fun onResume() {
        super.onResume()

        if (hasAnyVisibleDialog())
            return

        binding.tvUpdateHeart.visibility = View.GONE

        isActivityChanging = false
        viewModel.getAllGameHeartData()
    }

    private fun restOfTheOnResumeFunctionality() {
        dataManager.mPref.prefGetUserGameState()?.answeredQuestions?.let {
            answeredQuestions = it
        }

        if (currentHearts <= 0 && shouldHeartDeduct()) {
            showLevelOverDialog()
        } else {
            if (gameResumable) resumeUserState()
            if (!isCurrentLevelStop) {
                if (questions.size == 0) {
                    loadGameQuestions()
                } else {
                    if (binding.questionsViewpager.currentItem < questions.size - 1) {
                        jumpToNextQuestion(binding.questionsViewpager.currentItem + 1)
                        isAnimationStarted = false
                        binding.textViewTime.clearAnimation()
                        binding.textViewTime.setTextColor(Color.parseColor("#FFD904"))
                    }
                }
            } else {
                gameResumable = false
                onBackPressed()
            }
        }
        pauseMode = false
    }

    private fun hasAnyVisibleDialog(): Boolean {
        return gameLevelUpDialog?.isAdded == true
                || gameChallengeDialog?.isAdded == true
                || gameLevelOverDialog.isAdded
                || gameLevelInfoDialog.isAdded
                || challengeDetailsDialog.isAdded

    }

    private fun resumeUserState() {
        val userState = dataManager.mPref.prefGetUserGameState()
        if (userState != null && !isPreviousLevel()) {
            currentLevel = userState.level
            currentLevelScore = userState.score.toFloat()
            answeredQuestions = userState.answeredQuestions
            if (challenge != null) {
                val msScore = challenge?.milestone_score?.toFloat() ?: 0f
                if (msScore < currentLevelScore) {
                    clearChallenge()
                }
            }
        }
        bindUiData()
        bindScore()
        bindHearts()
    }


    override fun onBackClicked(position: Int) {
        //timer?.cancel()
        checkGameSound()
        stopCountDownSound()
        onBackPressed()
    }

    override fun onQuestionSkipped(position: Int, answerResponse: GameResponseQuestion) {
        checkGameSound()
        stopCountDownSound()
        isAnimationStarted = false
        binding.textViewTime.clearAnimation()
        answerResponse.time = answerElapsedTime
        answerElapsedTime = 0
        addQuestionAnswer(answerResponse)
        cancelTimer()
        isSkipped = true
        if (binding.questionsViewpager.currentItem < questions.size - 1) {
            if (!shouldHeartDeduct()) {
                jumpToNextQuestion(binding.questionsViewpager.currentItem + 1)
                isSkipped = false
            } else {
                heartsUpdate()
            }
        }
    }

    private fun addQuestionAnswer(answerResponse: GameResponseQuestion) {
        if (answeredQuestions == null) answeredQuestions = mutableListOf<GameResponseQuestion>()
        val answer = answeredQuestions.find {
            it.id.toInt() == answerResponse.id.toInt()
        }
        if (answer == null)
            answeredQuestions.add(answerResponse)
    }

    private fun heartsUpdate() {
        if (!shouldHeartDeduct())
            return
        if (currentHearts == 0) {
            // Do nothing
        } else {
            startHeartBeatAnimation()
            viewModel.insertGameHeart(gameHeartDTO(GAME_HEART_MINUS))
        }
    }

    private fun getTime(isMath: Boolean): Long {
        return if (isMath) {
            gameMathQuestionTime()
        } else {
            gameQuestionTime()
        }
    }

    private fun gameQuestionTime(): Long {
        val gameLevelDuration = (gameLevel?.question_time ?: "10000").toLong()
        return gameLevelDuration * 1000
    }

    private fun gameMathQuestionTime(): Long {
        val gameLevelDuration = (gameLevel?.math_question_time ?: "10000").toLong()
        return gameLevelDuration * 1000
    }


    private fun bindScore() {
        binding.tvTargetScore.text = targetLevelScore.toString()
        binding.tvCurrentLevelScore.text = currentLevelScore.toString()
        bounceScoreTxt()
    }

    private fun bounceScoreTxt() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.game_score_bounce)
        anim.duration = 200 //You can manage the blinking time with this parameter
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        binding.tvCurrentLevelScore.startAnimation(anim)
    }

    private fun animateHeartUpdate(text: String) {
        val anim = AnimationUtils.loadAnimation(this, R.anim.game_heart_update)
        anim.duration = 2000 //You can manage the blinking time with this parameter
        anim.startOffset = 20
        binding.tvUpdateHeart.text = text
        binding.tvUpdateHeart.visibility = View.VISIBLE
        binding.tvUpdateHeart.startAnimation(anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })
    }

    private fun gameLevelUp() {
        isCurrentLevelStop = true
        gameLevel?.id?.let {
            setUserStateToLocal(it)
            runOnUiThread {
                var challengeId: String? = if (challenge?.id == null) null else challenge?.id.toString()
                viewModel.apiGameLevelUp(
                    challengeId,
                    ArrayList(answeredQuestions),
                    this
                )
            }
        }
    }

    private fun gameLevelOver() {
        isCurrentLevelStop = true
        gameResumable = false
        runOnUiThread {
            var challengeId: String? = if (challenge?.id == null) null else challenge?.id.toString()
            viewModel.apiGameLevelOver(
                challengeId,
                ArrayList(answeredQuestions),
                this
            )
        }
        showLevelOverDialog()
    }

    private fun showLevelUpDialog(milestoneBonusHearts: Int?) {
        try {
            stopCountDownSound()
            gameLevelUpDialog =
                GameLevelUpDialog.newInstance(
                    gameLevel!!,
                    nextLevel!!,
                    milestoneBonusHearts ?: 0
                )
            val transaction = supportFragmentManager.beginTransaction()
            if (gameLevelUpDialog?.isAdded == false)
                gameLevelUpDialog!!.show(transaction, "GameLevelUpDialog")
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun loadGameQuestions() {
        val userInfo = dataManager.mPref.prefGetUserInfo()
        gameLevel?.id?.let {
            viewModel.apiGetQuestionListForGame(
                userInfo.category_id.toString(),
                it,
                this
            )
        }
    }

    override fun loadMore() {
        if (!doNotLoadMore) {
            answeredQuestions.clear()
            loadGameQuestions()
        }
    }

    override fun questionFinished() {
    }


    override fun onTouchHold() {
        if (answerElapsedTime == 0L && isTransitionTime) {
            isCurrentLevelStop = true
            holdUsed = true
        }
    }

    override fun onTouchRelease() {
        if (isTransitionTime) {
            isTransitionTime = false
            isCurrentLevelStop = false
            Handler().postDelayed({
                jumpToNextQuestion(binding.questionsViewpager.currentItem + 1)
            }, QUESTION_JUMP_DELAY * 2)
        }
    }

    private fun gameHeartDTO(type: Int): GameHeartDTO {
        val userInfo = dataManager.mPref.prefGetUserInfo()
        return GameHeartDTO(user_id = userInfo.id.toInt(), heart_type = type, practice = null)
    }

    private fun getUserId(): Int {
        val userInfo = dataManager.mPref.prefGetUserInfo()
        return userInfo.id.toInt()
    }

    private fun isPreviousLevel() = currentLevel?.position ?: 0 > gameLevel?.position ?: 0

    override fun onBackPressed() {
        isActivityChanging = true
        gameResumable =
            currentLevelScore < targetLevelScore && !isPreviousLevel()
        super.onBackPressed()
    }

    override fun backToLevelList() {
        backToLevelList = true
        gameLevelUpDialog?.dismiss()
        if (challenge != null) {
            challenge?.let {
                gameChallengeDialog = GameChallengeDialog.newInstance(it, false)
                showChallengeDialog()
            }
        } else {
            isActivityChanging = true
            finish()
        }
        /* if (::reviewInfo.isInitialized) {
               manager.launchReviewFlow(this, reviewInfo)
         }*/
    }

    override fun playNextLevel() {
        gameLevelUpDialog?.dismiss()
        if (challenge != null) {
            challenge?.let {
                // If the challenge timing is while level up
                if (it.milestone_type == LEVEL_UP_CHALLENGE) {
                    gameChallengeDialog = GameChallengeDialog.newInstance(it, true)
                    showChallengeDialog()
                }
            }
        } else {
            arrangeNextLevel()
        }
        /*if (::reviewInfo.isInitialized) {
            manager.launchReviewFlow(this, reviewInfo)
        }*/
    }

    private fun showChallengeDialog() {
        try {
            stopCountDownSound()
            val transaction = supportFragmentManager.beginTransaction()
            if (gameChallengeDialog?.isAdded == false)
                gameChallengeDialog?.show(transaction, "GameChallengeDialog")
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun clearChallenge() {
        challenges.remove(challenge)
        challenge = null
    }

    override fun declineChallenge(playNextLevel: Boolean) {
        clearChallenge()
        gameChallengeDialog?.dismiss()

        // If backToLevelList selected in GameLevelUpDialog
        if (backToLevelList) {
            isActivityChanging = true
            finish()
            return
        }

        if (playNextLevel) {
            arrangeNextLevel()
        } else {
            continuePlay()
            if (this::reportBottomSheet.isInitialized && reportBottomSheet.isAdded) {
                isCurrentLevelStop = true
            }
        }
    }

    override fun challengeDetails() {
        challengeDetailsDialog.show(supportFragmentManager, challengeDetailsDialog.tag)
    }

    private fun continuePlay() {
        isCurrentLevelStop = false
        if (binding.questionsViewpager.currentItem < questions.size - 1) {
            jumpToNextQuestion(binding.questionsViewpager.currentItem + 1)
        }
    }

    override fun startChallenge() {
        isActivityChanging = true
        AppEvent.getInstance(this@GameLevelQuestionActivity)
            .logTakeChallengeEvent(
                dataManager.mPref.prefGetUserInfo().name ?: "",
                challenge?.title ?: "Challenge"
            )
        startActivity(
            Intent(
                this@GameLevelQuestionActivity,
                GameChallengeActivity::class.java
            ).putExtra("challenge", challenge)
        )
        clearChallenge()
        cancelTimer()
        finish()
    }


    private fun arrangeNextLevel() {
        isCurrentLevelStop = false
        gameResumable = false
        gameLevel = nextLevel
        currentLevel = nextLevel
        currentLevelScore = 0f
        targetLevelScore = nextLevel?.target_points?.toFloat() ?: 0f
        loadGameQuestions()
        resumeUserState()
        animateHeartUpdate("+${currentRewardHearts}")
    }

    override fun onClick(v: View?) {

    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
    }

    override fun onCloseChallengeDetailsClicked() {
        challengeDetailsDialog.dismiss()
    }

    override fun onCloseClicked() {
        gameLevelInfoDialog.dismiss()
        isActivityChanging = true
        finish()
    }

    override fun onOkBtnClicked(title: String) {
        if (!challengeDetailsDialog.isAdded) challengeDetailsDialog.dismiss()
    }

    override fun onReportSubmitted(question_id: String, type: String, details: String) {
        viewModel.apiReportAboutQuestion(question_id, type, details, this)
        showToast(this, "Report Submitted!")
        Handler().postDelayed({
            continuePlay()
        }, QUESTION_JUMP_DELAY * 2)
    }

    override fun onVideoReportSubmitted(question_id: String, type: String, details: String) {

    }

    override fun onReportDialogDismiss() {
        Handler().postDelayed({
            continuePlay()
        }, QUESTION_JUMP_DELAY * 2)
    }

    override fun onPurchaseDialogDismiss() {
        isActivityChanging = true
        cancelTimer()
        finish()
    }

    override fun onPurchaseNowClick() {
        isActivityChanging = true
        isCurrentLevelStop = false
        goToHeartAddPage()
        gameLevelOverDialog.dismiss()
    }

    private fun goToHeartAddPage() {
        var hearts = currentHearts
        if (currentHearts > 0 && shouldHeartDeduct()) {
            if (answerElapsedTime != 0L) {
                viewModel.insertGameHeart(gameHeartDTO(GAME_HEART_MINUS))
            }
        }
        startActivity(
            Intent(this, HeartAddActivity::class.java)
                .putExtra("from_game", true)
                .putExtra("hearts", hearts)
                .putExtra("is_from_game","yes")
        )
    }

    override fun onTouchCancel() {
        onTouchRelease()
    }

}