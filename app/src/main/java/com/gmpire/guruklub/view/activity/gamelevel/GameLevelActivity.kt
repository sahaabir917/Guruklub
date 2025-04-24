package com.gmpire.guruklub.view.activity.gamelevel

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.adspricinglist.AdsPricingList
import com.gmpire.guruklub.data.model.adsubscribepaymentresponse.AdSubscribePaymentResponse
import com.gmpire.guruklub.data.model.adsubscribepaymentresponse.PaymentInfo
import com.gmpire.guruklub.data.model.gameChallenge.GameChallenge
import com.gmpire.guruklub.data.model.gamelevel.GameLevelItem
import com.gmpire.guruklub.data.model.gamelevel.GameUserInfo
import com.gmpire.guruklub.data.model.gamelevel.GameUserState
import com.gmpire.guruklub.data.model.gamelevel.Level
import com.gmpire.guruklub.data.model.gamerule.GameRule
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.data.model.paymentexc.AdsFreeSubscribePaymentExcResponse
import com.gmpire.guruklub.databinding.ActivityGameLevelBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.util.appEvents.AppEvent
import com.gmpire.guruklub.view.activity.friendrequest.FriendRequestFragment
import com.gmpire.guruklub.view.activity.gameLevelQuestion.GameLevelQuestionActivity
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
import com.gmpire.guruklub.view.activity.gamesubscribe.SubscribeDialog
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelInfoDialog
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelOverDialog
import com.gmpire.guruklub.view.dialog.gameLevel.GameSettingDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener
import okhttp3.ResponseBody
import retrofit2.Response

const val GAME_HEART_MINUS = 0
const val SCORE_BASED_CHALLENGE = 1
const val LEVEL_UP_CHALLENGE = 2

class GameLevelActivity : BaseActivity(),
    GameSettingDialog.GameSettingDialogListener,
    GameLevelInfoDialog.GameLevelInfoDialogListener,
    SubscribeDialog.SubscriptionListener,
    GameLevelOverDialog.IPurchaseDialogClicked,
    SSLCTransactionResponseListener, IDatabaseCallBack {

    private lateinit var backgroundMusic: MediaPlayer
    private lateinit var levelData: ArrayList<Level>
    private lateinit var paymentInfo: PaymentInfo
    private lateinit var pricingList: ArrayList<AdsPricingList>
    private var isActivityChanging: Boolean = false
    private lateinit var binding: ActivityGameLevelBinding
    private lateinit var viewModel: GameLevelViewModel
    private lateinit var adsFreeSubscribePaymentExcResponse: AdsFreeSubscribePaymentExcResponse
    private var page: Int = 1
    private var gameSound: Boolean = false
    private var currentLevel: Level? = null
    private var isHomeButtonPressed = true
    private var currentHearts: Int = 0
    private var selectedLevel: Level? = null
    private var challenges: List<GameChallenge> = mutableListOf()
    private var gameUserInfo: GameUserInfo? = null

    private val subscribeDialog = SubscribeDialog.newInstance()
    private val gameHeartPurchaseDialog = GameLevelOverDialog.newInstance()
    private val gameSettingDialog = GameSettingDialog.newInstance()
    private lateinit var gameLevelInfoDialog: GameLevelInfoDialog
    private var endPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game_level)
    }

    override fun viewRelatedTask() {
        page = 1
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(GameLevelViewModel::class.java)
        viewModel.iDatabaseCallBack = this
        binding.userInfo = dataManager.mPref.prefGetUserInfo()
        bindHearts()

        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        setProfileInfo(dataManager.mPref.prefGetUserInfo())

        viewModel.getGameRules(this)

        binding.settingsLayout.setOnClickListener(this)
        binding.heartlayout.setOnClickListener(this)
        binding.gameRuleLayout.setOnClickListener(this)
        binding.exitlayout.setOnClickListener(this)
        binding.linearLayout13.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        isActivityChanging = false
        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        bindHearts()
        viewModel.apiGetGameLevel(this)
        viewModel.apiGetUserInfo(this)
    }

    override fun onPause() {
        super.onPause()
        if (!isActivityChanging) {
            GameServiceHelper.stopMusic(this)
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (isLoader) {
            binding.progressBarGameLevel.visibility = View.VISIBLE
        } else {
            binding.progressBarGameLevel.visibility = View.GONE
        }
    }

    private fun setProfileInfo(userInfo: UserInfo) {
        binding.userProfilePic.clipToOutline = true
        binding.userProfilePic.elevation = 1.4f
        Glide.with(this)
            .load(BuildConfig.SERVER_URL + userInfo.picture)
            .placeholder(R.drawable.ic_placeholder_user)
            .error(R.drawable.ic_placeholder_user)
            .into(binding.userProfilePic)
    }

    private fun initLevels(
        levelData: ArrayList<Level>
    ) {
        if (page != 1) {
            this.levelData.addAll(levelData)
            binding.levelRl.adapter?.notifyDataSetChanged()
        } else {
            binding.levelRl.layoutManager = GridLayoutManager(this, 3)
            levelData.sortBy {
                it.position
            }
            binding.levelRl.adapter = BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, gameLevel: T, view: View) {
                    checkGameSound()
                    gameLevel as Level
                    selectedLevel = gameLevel
                    goAndPlay()

                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return LevelPageViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_level_page,
                            parent,
                            false
                        ),
                        this@GameLevelActivity,
                        currentLevel,
                        dataManager.mPref.prefGetUserInfo()
                    )
                }

                override fun loadMoreItem() {
                }
            }, levelData)

            Handler().postDelayed({
                binding.levelRl.scrollToPosition(
                    currentLevel?.position ?: 0
                )
            }, 200)
        }
    }


    private fun goAndPlay() {
        if (currentHearts <= 0 && shouldHeartDeduct()) {
            showHeartPurchaseDialog()
        } else {
            if (selectedLevel?.position ?: 0 <= currentLevel?.position ?: 0) {
                isHomeButtonPressed = false
                isActivityChanging = true
                startActivity(
                    Intent(
                        this@GameLevelActivity,
                        GameLevelQuestionActivity::class.java
                    ).putExtra(
                        "game_level", selectedLevel
                    ).putExtra(
                        "my_level", currentLevel
                    ).putExtra(
                        "challenges", ArrayList(challenges)
                    )
                )
            } else {
                showToast(this@GameLevelActivity, "You need to Level Up")
            }
        }
    }

    private fun showHeartPurchaseDialog() {
        try {
            val transaction = supportFragmentManager.beginTransaction()
            if (!gameHeartPurchaseDialog.isAdded)
                gameHeartPurchaseDialog.show(transaction, "LevelOverDialog")
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onBackPressed() {
        checkGameSound()
        isHomeButtonPressed = false
        if (GameServiceHelper.isMusicRunning)
            GameServiceHelper.stopMusic(this)
        super.onBackPressed()
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetGameLevel" -> {
                val type = object : TypeToken<BaseModel<GameLevelItem>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameLevelItem>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                if (baseData.data?.levels != null && baseData.data?.levels?.isNotEmpty()!!) {
                                    levelData = baseData.data?.levels as java.util.ArrayList<Level>
                                    challenges = baseData.data?.challenge ?: mutableListOf()
                                    val currentLevelId = baseData.data?.my_level_id ?: 0
                                    currentLevel = levelData.find {
                                        return@find it.id == currentLevelId.toString()
                                    }
                                    endPosition =
                                        ((((currentLevel?.position ?: 0) - 1) / 3) + 3) * 4

                                    if (endPosition < levelData.size)
                                        levelData = ArrayList(levelData.subList(0, endPosition))

                                    initLevels(levelData)
                                    currentHearts = baseData.data?.my_hearts?.toInt() ?: 0

                                    baseData.data?.resume?.apply {
                                        level = currentLevel
                                    }

                                    dataManager.mPref.prefSetGameCurrentLevel(currentLevel)
                                    setGameStateToLocal(baseData.data?.resume)
                                    viewModel.dataGetUserHearts()
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }

            "apiGetUserInfo" -> {
                val type = object : TypeToken<BaseModel<GameUserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameUserInfo>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                gameUserInfo = baseData.data
                                bindUserUpdatedInfo()
                                checkHasBackGroundMusic()
                                RxBus.get().withKey(RxBusEvents.HEARTS_UPDATED).send(
                                    UpdateClass()
                                )
                            }
                        } catch (ex: java.lang.Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }

            "apiGetAdFreePaymentInfo" -> {
                val type = object : TypeToken<BaseModel<AdSubscribePaymentResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<AdSubscribePaymentResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        isHomeButtonPressed = false
                        if (GameServiceHelper.isMusicRunning)
                            GameServiceHelper.stopMusic(this)
                        paymentInfo = baseData?.data!!.payment_info
                        initSslPayment()
                    } else if (baseData.status_code == 500) {
                        showToast(this, baseData.message[0])
                    }
                }
            }
            "getAdsPricingList" -> {
                val type = object : TypeToken<BaseModel<ArrayList<AdsPricingList>>>() {}.type
                result.data?.body()?.let {
                    val baseData = Gson().fromJson<BaseModel<ArrayList<AdsPricingList>>>(
                        result.data.body()?.string(),
                        type
                    )
                    if (baseData.status_code == 200) {
                        pricingList = baseData?.data!!
                    }
                }
            }
            "getGameRules" -> {
                val type = object : TypeToken<BaseModel<GameRule>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameRule>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                gameLevelInfoDialog = GameLevelInfoDialog.newInstance(
                                    "Game Rules",
                                    baseData?.data?.game_rules!!,
                                    false,
                                    false
                                )
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(this!!, baseData.message[0])
                    }
                }
            }
            "getPaymentExecution" -> {
                val type =
                    object : TypeToken<BaseModel<AdsFreeSubscribePaymentExcResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<AdsFreeSubscribePaymentExcResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                adsFreeSubscribePaymentExcResponse = baseData.data!!
                                dataManager.mPref.prefSetIsAdFree(true)
                                AppEvent.getInstance(this@GameLevelActivity)
                                    .logAdsFreeEvent(
                                        dataManager.mPref.prefGetUserInfo().name ?: "",
                                        pricingList[0].days.toInt(),
                                        pricingList[0].price.toFloat()
                                    )
                            }
                        } catch (ex: java.lang.Exception) {
                            ex.printStackTrace()
                        }
                    } else {
                        showToast(this, baseData.message[0])
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
                        showToast(applicationContext, baseData.message[0])
                    }
                }
            }
        }
    }

    private fun setGameStateToLocal(resume: GameUserState?) {
        if (resume != null) {
            val localState = dataManager.mPref.prefGetUserGameState()
            if (
                localState?.level_id?.toInt() ?: 0 == resume.level_id.toInt()
                &&
                localState?.user_id?.toInt() ?: 0 == resume.user_id.toInt()
            ) {
                resume.answeredQuestions = localState?.answeredQuestions ?: mutableListOf()
            }
        }

        dataManager.mPref.prefSetUserGameState(resume)
    }

    private fun checkHasBackGroundMusic() {
        var gameBackGroundMusic = dataManager.mPref.getGameUserSetting()?.hasBackgroundSound()
        if (gameBackGroundMusic == true) {
            dataManager.mPref.setIsMusicOn(true)
            if (!GameServiceHelper.isMusicRunning)
                GameServiceHelper.playMusic(this)
        } else if (gameBackGroundMusic == false) {
            Log.d("no sound", "No Sound")
            dataManager.mPref.setIsMusicOn(false)
        }
    }

    private fun bindUserUpdatedInfo() {
        gameUserInfo?.run {
            dataManager.mPref.prefSetIsAdFree(user_settings.ads_free == "1")
            dataManager.mPref.prefSetUserAdLimit(user_settings.user_ads_limit)
            setGameStateToLocal(resume)
            dataManager.mPref.prefSetGameCurrentLevel(current_level)
            dataManager.mPref.prefSetCurrentSubscription(hearts_subscription)
            dataManager.mPref.setGameUserSetting(user_settings)
            dataManager.mPref.setUserGlobalSetting(global_settings!!)
            dataManager.mPref.prefSetUserHeart(current_hearts!!)
            dataManager.mPref.prefSetHeartGift(hearts_gift)
        }
    }

    private fun initSslPayment() {
        val sslCommerzInitialization = SSLCommerzInitialization(
            "gurukluborglive",
            "607FA869551A380646",
            pricingList[0].price.toDouble(),
            SSLCCurrencyType.BDT,
            paymentInfo.tnx_id,
            "education",
            SSLCSdkType.LIVE
        )
        IntegrateSSLCommerz
            .getInstance(this)
            .addSSLCommerzInitialization(sslCommerzInitialization)
            .buildApiCall(this)
    }

    private fun configureHearts() {
        //dataManager.mPref.prefSetUserHeart(currentHearts.toString())
        bindHearts()
    }

    private fun bindHearts() {
        binding.myLife.text = currentHearts.toString()
    }

    override fun OnCloseBtnClicked() {
        checkGameSound()
        gameSettingDialog.dismiss()
    }

    override fun OnSubscribeBtnClicked() {
        checkGameSound()
        showSubscribeDialog()
    }

    override fun OnFreeAdBtnClicked() {
        checkGameSound()
        viewModel.getAdsPricingList(this)
        viewModel.apiGetAdFreePaymentInfo(this@GameLevelActivity)
    }

    override fun onMusicbtnClicked(hasMusic: Boolean) {
        if (hasMusic) {
            dataManager.mPref.setIsMusicOn(true)
            if (!GameServiceHelper.isMusicRunning)
                GameServiceHelper.playMusic(this)
        } else {
            dataManager.mPref.setIsMusicOn(false)
            if (GameServiceHelper.isMusicRunning)
                GameServiceHelper.stopMusic(this)
        }
    }

    override fun onGameBtnSwitchClicked() {
        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
    }


    private fun showSubscribeDialog() {
        try {
            val transaction = supportFragmentManager.beginTransaction()
            if (!subscribeDialog.isAdded)
                subscribeDialog.show(transaction, "LoginDialog")
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onClick(v: View?) {
        checkGameSound()
        when (v) {
            binding.settingsLayout -> {
                gameSettingDialog.show(supportFragmentManager, gameSettingDialog.tag)
            }
            binding.exitlayout -> {
                if (GameServiceHelper.isMusicRunning)
                    GameServiceHelper.stopMusic(this)
                finish()
            }

            binding.linearLayout13 ->{
              startActivity(Intent(this,FriendRequestActivity::class.java))
            }

            binding.heartlayout -> {
                isActivityChanging = true
                if (dataManager.mPref.prefGetLoginMode()) {
                    isHomeButtonPressed = false
                    val intent = Intent(this, HeartAddActivity::class.java)
                    intent.putExtra("is_from_game", "yes")
                    startActivity(intent)
                }
            }

            binding.gameRuleLayout -> {
                if (this::gameLevelInfoDialog.isInitialized)
                    gameLevelInfoDialog.show(supportFragmentManager, gameLevelInfoDialog.tag)
            }
        }
    }

    private fun checkGameSound() {
        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        if (gameSound) {
            playBtnSound()
        }
    }


    private fun playBtnSound() {
        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        backgroundMusic = MediaPlayer.create(this, R.raw.button_press)
        backgroundMusic.isLooping = false
        backgroundMusic.start()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isHomeButtonPressed) {
            if (GameServiceHelper.isMusicRunning)
                GameServiceHelper.stopMusic(this)
        }
        isHomeButtonPressed = true
    }

    override fun onCloseBtnClicked() {
        checkGameSound()
        subscribeDialog.dismiss()
    }

    override fun switchBtnClicked() {
        checkGameSound()
    }

    override fun transactionFail(p0: String?) {
        viewModel.getFailedTransaction("FAILED", p0.toString(), paymentInfo.tnx_id, this)
    }

    override fun merchantValidationError(p0: String?) {
        viewModel.getFailedTransaction("INVALID", p0.toString(), paymentInfo.tnx_id, this)
    }

    override fun transactionSuccess(p0: SSLCTransactionInfoModel?) {
        Log.d("transactions", p0.toString())
        showToast(this, "Transaction success")
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

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "dataGetUserHearts" -> {
                val hearts = result as List<GameHeartDTO>
                hearts.forEach {
                    if (it.heart_type == GAME_HEART_MINUS) {
                        currentHearts -= 1
                    } else {
                        if (!it.practice.isNullOrEmpty()) {
                            currentHearts += if (it.practice == "0")
                                dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_hearts?.toInt()
                                    ?: 0
                            else
                                dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_random_hearts?.toInt()
                                    ?: 0
                        }
                    }
                }
                configureHearts()
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
    }

    override fun onCloseClicked() {
        checkGameSound()
        gameLevelInfoDialog.dismiss()
    }

    override fun onOkBtnClicked(title: String) {

    }

    override fun onPurchaseDialogDismiss() {

    }

    override fun onPurchaseNowClick() {
        startActivity(Intent(this, HeartAddActivity::class.java).putExtra("is_from_game", "yes"))
    }
}