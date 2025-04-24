package com.gmpire.guruklub.view.activity.gameChallenge

import android.content.Intent
import android.graphics.Color
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
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.gameChallenge.GameChallenge
import com.gmpire.guruklub.data.model.gameChallenge.GameMilestoneChallengeResponse
import com.gmpire.guruklub.data.model.gameheartpackages.GameChallengeState
import com.gmpire.guruklub.data.model.gameheartpackages.GuruKlubGameHeartChallengeCriteria
import com.gmpire.guruklub.data.model.gamequestions.GameQuestionSet
import com.gmpire.guruklub.data.model.heartsettings.HeartVariation
import com.gmpire.guruklub.data.model.question.GameResponseQuestion
import com.gmpire.guruklub.databinding.ActivityGameChallengeBinding
import com.gmpire.guruklub.util.GameServiceHelper
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.BottomSheet.ReportBottomSheet
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
import com.gmpire.guruklub.view.activity.gameheart.HeartChallengeSuccessDialogFragment
import com.gmpire.guruklub.view.activity.gamelevel.GAME_HEART_MINUS
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.customView.CustomScrollView
import com.gmpire.guruklub.view.dialog.gameLevel.GameChallengeFailedDialog
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelInfoDialog
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelOverDialog
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.LoadAdError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.SocketException
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class GameChallengeActivity : BaseActivity(),
    GameChallengeQuestionAdapter.OnActionListener,
    HeartChallengeSuccessDialogFragment.ChallengeSuccessListener,
    GameLevelOverDialog.IPurchaseDialogClicked,
    GameChallengeFailedDialog.IChallengeFailedDialog,
    IDatabaseCallBack, ReportBottomSheet.IBottomSheetDialogClicked,
    GameLevelInfoDialog.GameLevelInfoDialogListener, CustomScrollView.OnScrollTouchCancelListener {


    private lateinit var binding: ActivityGameChallengeBinding
    private lateinit var viewModel: GameChallengeQuestionViewModel
    private var challenge: GameChallenge? = null
    private var questions = arrayListOf<GameQuestionSet>()
    private var adapter: GameChallengeQuestionAdapter? = null
    private var timer: CountDownTimer? = null
    private var answeredQuestions = mutableListOf<GameResponseQuestion>()
    private lateinit var gameChallengeSuccessDialog: HeartChallengeSuccessDialogFragment
    private var gameHeartPurchaseDialog = GameLevelOverDialog.newInstance()
    private lateinit var gameChallengeFailedDialog: GameChallengeFailedDialog
    private lateinit var challengeBackDialog: GameLevelInfoDialog
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var reportBottomSheet: ReportBottomSheet

    private var targetScore: Float = 0f
    private var currentScore: Float = 0f
    private var currentHearts = 0
    private var challengeTimeConsumed: Long = 0
    private var challengeTime: Long = 0L
    private var gameSound: Boolean = false
    private var isAnimationStarted = false
    private var answerElapsTime: Long = 0
    private val QUESTION_JUMP_DELAY: Long = 1000
    private var isActivityChanging: Boolean = false
    private var challengeStartedAt: Long = 0
    private var isGameOnHold = false
    private var isTransitionTime = false
    private var holdUsed = false
    private var challengeCompleted = false
    private var interstitialCount = 29
    private lateinit var mInterstitialAd: InterstitialAd
    private var pagerPosition = 0
    private lateinit var timerInstance: CountDownTimer
    private lateinit var mediaPlayerCountDown: MediaPlayer
    private var isAdsFree: Boolean = false
    private var isSkipped = false
    private var isHeartCollectClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game_challenge)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(GameChallengeQuestionViewModel::class.java)
        currentHearts = dataManager.mPref.prefGetUserHeart()?.toInt() ?: 0
        viewModel.getAllGameHeartData()
        dataManager.mPref.prefSetChallengeState(null)
    }


    override fun viewRelatedTask() {
        viewModel.iDatabaseCallBack = this
        getIntentExtra()
        bindUiData()
        loadChallengeQuestions()
        binding.ivHeartAdd.setOnClickListener {
            checkGameSound()
            isActivityChanging = true
            startActivity(Intent(this@GameChallengeActivity, HeartAddActivity::class.java).putExtra("is_from_game","yes"))
        }
        gameChallengeFailedDialog =
            GameChallengeFailedDialog.newInstance(challenge?.criteria_type?.name)
        isAdsFree = dataManager.mPref.prefGetIsAdFree() ?: false

        buildInterstitialAd()

        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
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
                isGameOnHold = false
                isTransitionTime = false
                holdUsed = false
            }
        }
    }

    private fun bindUiData() {
        bindScore()
        bindHearts()
    }

    private fun bindHearts() {
        binding.tvCurrentHearts.text = currentHearts.toString()
    }


    private fun heartsUpdate() {
        if (!shouldHeartDeduct()) {
            if (isSkipped) {
                jumpToNextQuestion(binding.challengeQuestionsViewpager.currentItem + 1, 2)
                isSkipped = false
            }
            return
        }

        if (currentHearts != 0) {
            startHeartBeatAnimation()
            viewModel.insertGameHeart(gameHeartDTO(GAME_HEART_MINUS))
        }
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

    private fun playCountDownSound() {
        if (hasAnyVisibleDialog()) {
            stopCountDownSound()
            return
        }
        stopCountDownSound()
        if (gameSound) {
            try {
                mediaPlayerCountDown = MediaPlayer.create(this, R.raw.ticking)
                if (!mediaPlayerCountDown.isPlaying) {
                    mediaPlayerCountDown.isLooping = false
                    mediaPlayerCountDown.start()
                }
            } catch (e: java.lang.IllegalStateException) {
                e.printStackTrace()
            }
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
        } catch (e: java.lang.IllegalStateException) {
            e.printStackTrace()
        }
    }


    private fun startHeartBeatAnimation() {
        binding.ivHeartAdd.setImageResource(R.drawable.heart_animation)
        val heartBeatAnimation: AnimationDrawable = binding.ivHeartAdd.drawable as AnimationDrawable
        heartBeatAnimation.isOneShot = true
        heartBeatAnimation.start()
    }


    private fun gameHeartDTO(type: Int): GameHeartDTO {
        val userInfo = dataManager.mPref.prefGetUserInfo()
        return GameHeartDTO(user_id = userInfo.id.toInt(), heart_type = type, practice = null)
    }


    private fun hasAnyVisibleDialog(): Boolean {
        return this::gameChallengeFailedDialog.isInitialized && gameChallengeFailedDialog.isAdded == true
                || this::gameChallengeSuccessDialog.isInitialized && gameChallengeSuccessDialog.isAdded == true
                || this::challengeBackDialog.isInitialized && challengeBackDialog.isAdded == true
                || gameHeartPurchaseDialog.isAdded == true
    }

    private fun loadChallengeQuestions() {
        challenge?.id?.let {
            viewModel.apiMilestoneChallenge("1", it.toString(), this)
        }
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onPause() {
        if (!isActivityChanging) {
            GameServiceHelper.stopMusic(this)
        }
        stopCountDownSound()
        // If visible for some reason
        binding.tvUpdateHeart.visibility = View.GONE
        super.onPause()
        storeChallengeState()
        if (!challengeCompleted && challenge?.criteria_type != GuruKlubGameHeartChallengeCriteria.CLOCK_SHOOT && !isTransitionTime) {
            heartsUpdate()
        }
        isGameOnHold = true

        if (challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.MARATHON)
            cancelTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
        Log.d("Timer ticking->", "Timer null!")
    }

    private fun storeChallengeState() {
        val state = GameChallengeState()
        state.categoryId = "1"
        state.score = currentScore
        state.questions = answeredQuestions
        dataManager.mPref.prefSetChallengeState(state)
    }

    override fun onResume() {
        super.onResume()
        isActivityChanging = false
        isGameOnHold = false
        binding.tvUpdateHeart.visibility = View.GONE
        viewModel.getAllGameHeartData()
        if (challengeCompleted) {
            processChallengeAfterResume()
            return
        }
        if (currentHearts <= 0 && shouldHeartDeduct()) {
            showHeartPurchaseDialog()
        } else {
            resumeChallengeState()
        }
    }

    private fun processChallengeAfterResume() {
        challenge?.let {
            if (it.criteria_type != GuruKlubGameHeartChallengeCriteria.MARATHON) {
                if (challengeTime > 0 && challengeStartedAt > 0) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - challengeStartedAt >= gameChallengeTime()) {
                        if (currentScore < targetScore) {
                            challengeFailed()
                        } else {
                            showGameChallengeSuccessDialog()
                        }
                        return@let
                    }
                }
            }
        }
    }

    private fun resumeChallengeState() {
        val state = dataManager.mPref.prefGetChallengeState()
        state?.let {
            currentScore = it.score
            bindHearts()
            //bindQuestions()
        }

        challenge?.let {
            if (it.criteria_type != GuruKlubGameHeartChallengeCriteria.MARATHON) {
                if (challengeTime > 0 && challengeStartedAt > 0) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - challengeStartedAt >= gameChallengeTime()) {
                        challengeFailed()
                        return@let
                    }
                }
            }
            // preventing for first load
            if (questions.size > 0) {
                stopCountDownSound()
                isAnimationStarted = false
                jumpToNextQuestion(binding.challengeQuestionsViewpager.currentItem + 1, 1)
            }
        }
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiMilestoneChallenge" -> {
                binding.challengeQuestionsViewpager.visibility = View.VISIBLE
                val type = object : TypeToken<BaseModel<GameMilestoneChallengeResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameMilestoneChallengeResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            questions =
                                baseData.data?.questions as java.util.ArrayList<GameQuestionSet>

                            challengeTime = baseData.data?.challenge?.challenge_time ?: 0
                            targetScore = baseData.data?.challenge?.target_points?.toFloat() ?: 0f
                            if (questions.size > 0) {
                                bindQuestions()

                            } else {
                                binding.challengeQuestionsViewpager.visibility = View.GONE
                                showToast(this@GameChallengeActivity, "Something went wrong")
                            }
                            bindScore()
                        }
                    } else {
                        showToast(
                            this@GameChallengeActivity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
            "apiMilestoneChallengeDone" -> {
                try {
                    val type = object : TypeToken<BaseModel<Any>>() {}.type
                    result.data?.body()?.let {
                        val baseData =
                            Gson().fromJson<BaseModel<Any>>(
                                result.data.body()?.string(),
                                type
                            )
                        Log.d("ChallengeTest->", baseData.toString())
                        if (baseData.status_code == 200) {
                            isActivityChanging = true
                            goBackToLevelActivity()
                        } else {
                            Toast.makeText(
                                this@GameChallengeActivity,
                                if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0]),
                                Toast.LENGTH_SHORT
                            ).show()
                            isHeartCollectClicked = false
                        }
                    }
                } catch (ex: Exception) {
                    Toast.makeText(
                        this@GameChallengeActivity,
                        "Something went wrong!",
                        Toast.LENGTH_SHORT
                    ).show()
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
                            this@GameChallengeActivity,
                            if (baseData.message.isEmpty()) "Something went wrong" else (baseData.message[0])
                        )
                    }
                }
            }
        }
    }


    override fun onCollectHearts() {
        if (!isHeartCollectClicked) {
            isHeartCollectClicked = true
            challenge?.id?.let { challengeId ->
                viewModel.apiMilestoneChallengeDone(
                    "1",
                    challengeId.toString(),
                    answeredQuestions,
                    this
                )
            }
        }
    }

    private fun bindQuestions() {
        adapter = null
        Log.d("QuestionSize->", questions.size.toString())
        binding.challengeQuestionsViewpager.visibility = View.VISIBLE
        adapter = GameChallengeQuestionAdapter(this, questions, challenge, this)
        binding.challengeQuestionsViewpager.adapter = adapter

        if (challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.MARATHON) {
            startGameCountDownMarathon(
                if (questions[binding.challengeQuestionsViewpager.currentItem].is_math == "1")
                    getMathQuestionTime() else getQuestionTime()
            )
        } else {
            startGameCountDown(gameChallengeTime())
        }

        binding.challengeQuestionsViewpager.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                val s = state
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // If marathon type of challenge
                if (challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.MARATHON) {
                    startGameCountDownMarathon(
                        if (questions[binding.challengeQuestionsViewpager.currentItem].is_math == "1")
                            getMathQuestionTime() else getQuestionTime()
                    )
                }
            }

            override fun onPageSelected(position: Int) {
                pagerPosition = position
            }
        })
    }


    private fun startGameCountDown(l: Long) {
        timer?.let {
            it.cancel()
            clearTimerTxt()
        }
        timer = object : CountDownTimer(l, 1000) {
            override fun onFinish() {
                timer?.let {
                    timeExpired()
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                timerInstance = this
                answerElapsTime += 1
                challengeTimeConsumed += 1
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


                if (millisUntilFinished > 10000) {
                    stopCountDownSound()
                    binding.textViewTime.clearAnimation()
                }


                Log.d("Timer ticking->", millisUntilFinished.toString())
            }
        }.start()

        if (challenge?.criteria_type != GuruKlubGameHeartChallengeCriteria.MARATHON) {
            challengeStartedAt = System.currentTimeMillis()
        }
    }

    private fun cancelTimer() {
        if (this::timerInstance.isInitialized) {
            timerInstance.cancel()
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

    private fun timeExpired() {
        showToast(applicationContext, "Times Up!")
        adapter?.disableAllAnswerClick(
            binding.challengeQuestionsViewpager.findViewWithTag("root" + binding.challengeQuestionsViewpager.currentItem)
        )

        val qs = questions[binding.challengeQuestionsViewpager.currentItem]
        challengeTimeConsumed += 1
        answeredQuestions.add(
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
        stopCountDownSound()

        isAnimationStarted = false
        if (challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.COUNT_DOWN
            || challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.CLOCK_SHOOT
        ) {
            if (!isTargetAchieved())
                challengeFailed()
        }
    }


    private fun startGameCountDownMarathon(l: Long) {
        timer?.let {
            it.cancel()
            clearTimerTxt()
        }
        timer = object : CountDownTimer(l, 1000) {
            override fun onFinish() {
                timer?.let {
                    timeExpiredMarathon()
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                answerElapsTime += 1
                timerInstance = this
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

                if (millisUntilFinished > 10000) {
                    stopCountDownSound()
                    binding.textViewTime.clearAnimation()
                }
            }
        }.start()
    }


    private fun timeExpiredMarathon() {
        isAnimationStarted = false
        isTransitionTime = true
        adapter?.disableAllAnswerClick(
            binding.challengeQuestionsViewpager.findViewWithTag("root" + binding.challengeQuestionsViewpager.currentItem)
        )
        val qs = questions[binding.challengeQuestionsViewpager.currentItem]
        answeredQuestions.add(
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
        // Show right answer & wait 5 sec to show user
        adapter?.showRightAnswer(
            binding.challengeQuestionsViewpager,
            binding.challengeQuestionsViewpager.currentItem
        )
        stopCountDownSound()
        // updateScore(false)
        heartsUpdate()
        jumpToNextQuestion(binding.challengeQuestionsViewpager.currentItem + 1, 3)
    }


    private fun challengeFailed() {
        challengeCompleted = true
        timer?.cancel()
        showChallengeFailedDialog()
        dataManager.mPref.prefSetChallengeState(null)
    }


    override fun onChallengeFailedDialogDismiss() {
        showToast(applicationContext, "Better Luck Next Time!")
        isActivityChanging = true
        cancelTimer()
        goBackToLevelActivity()
    }

    override fun onTryChallengeAgain() {
        if (challenge?.criteria_type != GuruKlubGameHeartChallengeCriteria.CLOCK_SHOOT) {
            if (currentHearts == 0 && shouldHeartDeduct()) {
                showHeartPurchaseDialog()
            }
            challengeCompleted = false
            currentScore = 0f
            challengeTimeConsumed = 0
            challengeTime = challenge?.challenge_time ?: 0
            loadChallengeQuestions()
        } else {
            isActivityChanging = true
            goBackToLevelActivity()
        }
    }

    private fun showChallengeFailedDialog() {
        try {
            stopCountDownSound()
            val transaction = supportFragmentManager.beginTransaction()
            if (!gameChallengeFailedDialog.isAdded)
                gameChallengeFailedDialog.show(transaction, "GameChallengeFailedDialog")
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    private fun jumpToNextQuestion(position: Int, delay: Int) {
        isAnimationStarted = false
        Handler().postDelayed({
            if (!holdUsed) {
                isAnimationStarted = false

                if (currentHearts == 0 && shouldHeartDeduct()) {
                    showHeartPurchaseDialog()
                    return@postDelayed
                }

                if (isTargetAchieved() && !isGameOnHold) {
                    showGameChallengeSuccessDialog()
                    return@postDelayed
                }

                if (!dataManager.mPref.prefGetIsAdFree()) {
                    if (pagerPosition != 0 && pagerPosition % interstitialCount == 0) {
                        interstitialCount += 30
                        if (mInterstitialAd.isLoaded) {
                            isTransitionTime = true
                            mInterstitialAd.show()
                            buildInterstitialAd()
                            return@postDelayed
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.")
                        }
                    }
                }

                if (binding.challengeQuestionsViewpager.currentItem < questions.size - 1 && !isGameOnHold) {
                    binding.challengeQuestionsViewpager.setCurrentItem(position, true)
                    isTransitionTime = false
                }
            }
            holdUsed = false
        }, QUESTION_JUMP_DELAY * delay)
    }

    private fun getQuestionTime(): Long {
        return (challenge?.question_time ?: 0L) * 1000
    }

    private fun getMathQuestionTime(): Long {
        return (challenge?.math_question_time ?: 0L) * 1000
    }

    private fun gameChallengeTime(): Long {
        return (challengeTime * 60) * 1000
    }

    private fun clearTimerTxt() {
        binding.textViewTime.text = "00.00"
        binding.textViewTime.setTextColor(Color.parseColor("#FFD904"))
        binding.textViewTime.clearAnimation()
    }

    private fun updateScore(correct: Boolean) {
        if (correct) {
            currentScore += (challenge?.correct_points ?: 0f)
        } else {
            if (currentScore >= 0) {
                currentScore -= abs(challenge?.wrong_points ?: 0f)
            }
            if (currentScore < 0) currentScore = 0f
        }
        bindScore()
    }

    private fun showGameChallengeSuccessDialog() {
        stopCountDownSound()
        challengeCompleted = true
        challenge?.let {
            timer?.cancel()
            isGameOnHold = true
            dataManager.mPref.prefSetChallengeState(null)
            try {
                gameChallengeSuccessDialog =
                    HeartChallengeSuccessDialogFragment.newInstance(
                        it.reward_hearts.toString(),
                        dataManager.mPref.prefGetUserInfo().name.toString()
                    )
                val transaction = supportFragmentManager.beginTransaction()
                if (!gameChallengeSuccessDialog.isAdded)
                    gameChallengeSuccessDialog.show(transaction, "GameChallengeDialog")
            } catch (ex: IllegalStateException) {
                ex.printStackTrace()
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun bindScore() {
        binding.tvTargetScore.text = targetScore.toString()
        binding.tvCurrentLevelScore.text =
            if (currentScore > targetScore) targetScore.toString() else currentScore.toString()
        bounceScoreTxt()
    }

    private fun bounceScoreTxt() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.game_score_bounce)
        anim.duration = 200 //You can manage the blinking time with this parameter
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        binding.tvCurrentLevelScore.startAnimation(anim)
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (isLoader)
            binding.progressBarGameChallenge.visibility = View.VISIBLE
        else
            binding.progressBarGameChallenge.visibility = View.GONE
    }

    override fun onClick(v: View?) {
    }


    private fun getIntentExtra() {
        try {
            challenge = intent.getSerializableExtra("challenge") as GameChallenge
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onSubmitAnswer(
        position: Int,
        isCorrect: Boolean,
        answerResponse: GameResponseQuestion
    ) {
        answerElapsTime = 0
        isTransitionTime = true
        isAnimationStarted = false
        stopCountDownSound()
        checkGameSound()
        adapter?.enableReportClick(binding.challengeQuestionsViewpager, position)
        answerResponse.time = answerElapsTime
        answeredQuestions.add(answerResponse)
        if (!isCorrect) heartsUpdate()
        updateScore(isCorrect)

        if (challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.MARATHON) {
            timer?.cancel()
        } else if (challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.CLOCK_SHOOT) {
            if (position == questions.size - 1) {
                if (!isTargetAchieved()) {
                    challengeFailed()
                    return@onSubmitAnswer
                }
            }
        }
        jumpToNextQuestion(position + 1, 3)
    }

    override fun onReportProblemClicked(position: Int) {
        if (challengeCompleted) return
        isGameOnHold = true
        checkGameSound()
        stopCountDownSound()
        if (position < questions.size) {
            reportBottomSheet = ReportBottomSheet(questions[position].id)
            reportBottomSheet.setBottomDialogListener(this, true, false)
            reportBottomSheet.show(
                supportFragmentManager,
                reportBottomSheet.tag
            )
        } else {
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        challengeBackDialog = GameLevelInfoDialog.newInstance(
            "Game Challenge",
            "Your progress of this Game Challenge will be lost. Do you really want to exit ?",
            true,
            false
        )
        if (this::challengeBackDialog.isInitialized)
            challengeBackDialog.show(supportFragmentManager, challengeBackDialog.tag)
    }

    override fun onBackClicked(position: Int) {
        this.onBackPressed()
    }

    override fun onQuestionSkipped(position: Int, answerResponse: GameResponseQuestion) {
        checkGameSound()
        stopCountDownSound()
        answerResponse.time = answerElapsTime
        answerElapsTime = 0
        isAnimationStarted = false
        isSkipped = true
        answeredQuestions.add(answerResponse)

        if (challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.MARATHON) {
            cancelTimer()
        } else if (challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.CLOCK_SHOOT) {
            if (position == questions.size - 1) {
                if (!isTargetAchieved()) {
                    challengeFailed()
                } else {
                    showGameChallengeSuccessDialog()
                }
            }
        }

        if (binding.challengeQuestionsViewpager.currentItem < questions.size - 1)
            heartsUpdate()

    }

    override fun loadMore() {
        if (challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.MARATHON || challenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.COUNT_DOWN) {
            loadChallengeQuestions()
        }
    }

    private fun showHeartPurchaseDialog() {
        try {
            stopCountDownSound()
            val transaction = supportFragmentManager.beginTransaction()
            if (!gameHeartPurchaseDialog.isAdded)
                gameHeartPurchaseDialog.show(transaction, "LevelOverDialog")
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun questionFinished() {

    }

    override fun onTouchHold() {
        if (isTransitionTime) {
            isGameOnHold = true
            holdUsed = true
        }
    }

    override fun onTouchRelease() {
        Log.d("Event->", "Released")
        if (isTransitionTime) {
            isTransitionTime = false
            isGameOnHold = false
            jumpToNextQuestion(binding.challengeQuestionsViewpager.currentItem + 1, 2)
        }
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "insertGameHeart" -> {
                animateHeartUpdate("-1")
                viewModel.apiHeartsUpdate("0", this)
                if (currentHearts <= 0 && shouldHeartDeduct()) {
                    // showHeartPurchaseDialog()
                } else {
                    if (isSkipped) {
                        jumpToNextQuestion(binding.challengeQuestionsViewpager.currentItem + 1, 0)
                        isSkipped = false
                    }
                }
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
            }
        }
    }

    private fun isTargetAchieved(): Boolean {
        return currentScore >= targetScore
    }


    private fun checkGameSound() {
        if (gameSound) {
            playBtnSound()
        }
    }


    private fun playBtnSound() {
        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        mediaPlayer = MediaPlayer.create(this, R.raw.button_press)
        mediaPlayer.isLooping = false
        mediaPlayer.start()
    }

    override fun onFailedDB(result: Any, optName: String) {
        //
    }

    override fun onPurchaseDialogDismiss() {
        isActivityChanging = true
        goBackToLevelActivity()
    }

    override fun onPurchaseNowClick() {
        isActivityChanging = true
        gameHeartPurchaseDialog.dismiss()
        startActivity(Intent(this, HeartAddActivity::class.java).putExtra("is_from_game","yes"))
    }

    override fun onReportSubmitted(question_id: String, type: String, details: String) {
        isGameOnHold = false
        viewModel.apiReportAboutQuestion(question_id, type, details, this)
        showToast(this, "Report Submitted!")
        jumpToNextQuestion(binding.challengeQuestionsViewpager.currentItem + 1, 2)

    }

    override fun onVideoReportSubmitted(question_id: String, type: String, details: String) {

    }

    override fun onReportDialogDismiss() {
        isGameOnHold = false
        jumpToNextQuestion(binding.challengeQuestionsViewpager.currentItem + 1, 2)
    }

    override fun onCloseClicked() {
        checkGameSound()
        stopCountDownSound()
        if (challengeBackDialog.isAdded) challengeBackDialog.dismiss()
        if (challengeCompleted) {
            showGameChallengeSuccessDialog()
        }
    }

    override fun onOkBtnClicked(title: String) {
        checkGameSound()
        isActivityChanging = true
        cancelTimer()
        goBackToLevelActivity()
    }

    private fun goBackToLevelActivity() {
        finish()
        var intent = Intent(this@GameChallengeActivity, GameLevelActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onTouchCancel() {
        onTouchRelease()
    }

    override fun onError(err: Throwable, key: String) {
        super.onError(err, key)
        if (err !is SocketException) {
            isGameOnHold = true
        }
        Log.d("ChallengeTest->", "Challenge Failed!")
        if (key == "apiMilestoneChallengeDone")
            isHeartCollectClicked = false
    }
}