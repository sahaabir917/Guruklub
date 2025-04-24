package com.gmpire.guruklub.view.activity.gameheart

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.adspricinglist.AdsPricingList
import com.gmpire.guruklub.data.model.adsubscribepaymentresponse.AdSubscribePaymentResponse
import com.gmpire.guruklub.data.model.adsubscribepaymentresponse.PaymentInfo
import com.gmpire.guruklub.data.model.gameheartpackages.GameHeartPackage
import com.gmpire.guruklub.data.model.heartsettings.HeartSettings
import com.gmpire.guruklub.data.model.heartsettings.HeartsAddResponse
import com.gmpire.guruklub.data.model.paymentexc.LimitedHeartPaymentExcResponse
import com.gmpire.guruklub.data.model.usersreference.ReferenceResponse
import com.gmpire.guruklub.databinding.ActivityHeartAddBinding
import com.gmpire.guruklub.util.GameServiceHelper
import com.gmpire.guruklub.util.IDatabaseCallBack
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.appEvents.AppEvent
import com.gmpire.guruklub.view.activity.gamelevel.GAME_HEART_MINUS
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelViewModel
import com.gmpire.guruklub.view.activity.gamesubscribe.SubscribeDialog
import com.gmpire.guruklub.view.activity.question.QuestionAddActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelInfoDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
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
import kotlin.math.abs

class HeartAddActivity : BaseActivity(), RewardedVideoAdListener,
    GameLevelInfoDialog.GameLevelInfoDialogListener, SSLCTransactionResponseListener,
    SubscribeDialog.SubscriptionListener,
    IDatabaseCallBack {

    private var isFromGame: String? = null
    private lateinit var gameLevelInfoDialog: GameLevelInfoDialog
    private var isInvite: Boolean = false
    private var isActivityChanging: Boolean = false
    private var heartSettingsData: ArrayList<HeartSettings> = arrayListOf()
    private var packageData: ArrayList<GameHeartPackage> = arrayListOf()
    private lateinit var binding: ActivityHeartAddBinding
    private lateinit var viewModel: GameLevelViewModel
    private lateinit var paymentInfo: PaymentInfo
    private lateinit var pricingList: ArrayList<AdsPricingList>

    private var packagePrice: String = "10"
    private var packageId: String = "1"
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private var isHomeButtonPressed = true
    private var gameSound: Boolean? = false
    private var currentHearts: Int = 0
    var isAdsFree: Boolean = false
    private val subscribeDialog = SubscribeDialog.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_heart_add)
    }

    override fun viewRelatedTask() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(GameLevelViewModel::class.java)
        viewModel.iDatabaseCallBack = this
        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        viewModel.getLimitedHeartPackage(this)
        viewModel.getHeartSettings(this)
        viewModel.getAdsPricingList(this)
        updateUserHeart()

        if (intent.hasExtra("is_from_game")) {
            isFromGame = intent.getStringExtra("is_from_game")
            if (isFromGame == "yes") {
                binding.linearLayout7.visibility = View.VISIBLE
                binding.linearLayout8.visibility = View.VISIBLE
                binding.linearLayout17.visibility = View.VISIBLE
                binding.linearLayout25.visibility = View.GONE
                binding.linearLayout20.visibility = View.GONE
            }
            else if(isFromGame == "no"){
                binding.linearLayout7.visibility = View.GONE
                binding.linearLayout8.visibility = View.GONE
                binding.linearLayout17.visibility = View.GONE
                binding.linearLayout25.visibility = View.VISIBLE
               // binding.linearLayout20.visibility = View.VISIBLE
            }
        }

        bindHearts()
        binding.exitlayout.setOnClickListener(this)
        binding.playBtnLayout.setOnClickListener(this)
        binding.linearLayoutSpinnerHeart.setOnClickListener(this)
        binding.btnAddQuestion.setOnClickListener(this)

        val levelTxt = "Level: " + dataManager.mPref.prefGetGameCurrentLevel()?.level

        binding.tvLevelTxt.text = levelTxt

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        isAdsFree = dataManager.mPref.prefGetIsAdFree()
        loadRewardedVideoAd()
        setOnCLickListeners()
    }

    private fun bindHearts() {
        if (currentHearts < 0) currentHearts = 0
        binding.tvCurrentHearts.text = currentHearts.toString()
    }

    private fun setOnCLickListeners() {
        binding.rlWatchAd.setOnClickListener {
            checkGameSound()
            val userAdLimit = dataManager.mPref.prefGetUserAdLimit()?.toInt() ?: 0
            if (userAdLimit <= 0) {
                showToast(this, "You have reached your daily ads limit.")
                return@setOnClickListener
            }

            if (currentHearts > 0) {
                showToast(this, "You have enough hearts.")
                return@setOnClickListener
            }
            isHomeButtonPressed = false
            if (!isAdsFree) {
                if (mRewardedVideoAd.isLoaded) {
                    mRewardedVideoAd.show()
                    stopBackgroundMusic()
                } else {
                    Toast.makeText(this, "Please try again after a while...", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.rlInviteFriends.setOnClickListener {
            checkGameSound()
            isInvite = true
            val getReferenceToken = dataManager.mPref.getReferenceToken()
            if (getReferenceToken == null) {
                viewModel.getRefToken(this)
            } else {
                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                share.putExtra(Intent.EXTRA_SUBJECT, "Download " + getString(R.string.app_name))
                share.putExtra(
                    Intent.EXTRA_TEXT,
                    "Hello, I am learning from GuruKlub, you can learn too!\n\nPlease use this REFERRAL CODE : \"$getReferenceToken\" after downloading GuruKlub from https://play.google.com/store/apps/details?id=${packageName} '\n'"
                )
                startActivity(
                    Intent.createChooser(
                        share,
                        "Download " + getString(R.string.app_name)
                    )
                )
            }
        }

        binding.rlBuyHearts.setOnClickListener {
            checkGameSound()
            isHomeButtonPressed = false
            if (GameServiceHelper.isMusicRunning)
                GameServiceHelper.stopMusic(this)
            viewModel.getHeartAddPayment(this, packageId)
        }

        binding.linearLayout25.setOnClickListener{
            checkGameSound()
            viewModel.getAdsPricingList(this)
            viewModel.apiGetAdFreePaymentInfo(this)
        }

        binding.linearLayout20.setOnClickListener {
            checkGameSound()
            showSubscribeDialog()
        }
    }

    private fun stopBackgroundMusic() {
        if (GameServiceHelper.isMusicRunning)
            GameServiceHelper.stopMusic(this)
    }

    private fun initSslPayment() {
        val sslCommerzInitialization = SSLCommerzInitialization(
            "gurukluborglive",
            "607FA869551A380646",
            packagePrice.toDouble(),
            SSLCCurrencyType.BDT,
            paymentInfo.tnx_id,
            "education",
            SSLCSdkType.LIVE
        )
        IntegrateSSLCommerz
            .getInstance(this)
            .addSSLCommerzInitialization(sslCommerzInitialization)
            .buildApiCall(this);
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(
            getString(R.string.ad_unit_id_reward_test),
            AdRequest.Builder().build()
        )
    }


    private fun initBuyLife(packageData: ArrayList<GameHeartPackage>) {
        if (packageData.isNotEmpty()) {
            binding.tvBuyAmount.text = packageData[0].hearts
            binding.tvMoneyAmount.text = packageData[0].price
            packageId = packageData[0].id
            packagePrice = packageData[0].price
        }

        binding.lifemoneyrl.layoutManager = LinearLayoutManager(this@HeartAddActivity)
        binding.lifemoneyrl.adapter = BaseRecyclerAdapter(this, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                checkGameSound()
                model as GameHeartPackage
                binding.tvBuyAmount.text = model.hearts
                binding.tvMoneyAmount.text = model.price + " tk"
                packagePrice = model.price
                packageId = model.id
                if (binding.recyclerViewLayout.isVisible) {
                    binding.recyclerViewLayout.visibility = View.GONE
                }
            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return BuyHeartHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_buy_life_layout,
                        parent,
                        false
                    ),
                    this@HeartAddActivity
                )
            }

            override fun loadMoreItem() {
            }

        }, packageData)
    }

    override fun onResume() {
        super.onResume()
        mRewardedVideoAd.resume(this)
        if (isInvite) {
            //create a dialog fragment and show it
            gameLevelInfoDialog = GameLevelInfoDialog.newInstance(
                getString(R.string.invite_friend_title),
                getString(R.string.invite_friend_text),
                false,
                false
            )
            gameLevelInfoDialog.show(supportFragmentManager, gameLevelInfoDialog.tag)
            isInvite = false
        }
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

    override fun onPause() {
        super.onPause()
        if (!isActivityChanging) {
            GameServiceHelper.stopMusic(this)
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (isLoader) {
            binding.progressBarGameLevelHeart.visibility = View.VISIBLE
        } else {
            binding.progressBarGameLevelHeart.visibility = View.GONE
        }
    }

    override fun navigateToHome() {
    }

    private fun startHeartBeatAnimation() {
        binding.ivHeartAdd.setImageResource(R.drawable.heart_animation)
        val heartBeatAnimation: AnimationDrawable = binding.ivHeartAdd.drawable as AnimationDrawable
        heartBeatAnimation.isOneShot = true
        heartBeatAnimation.start()
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
        startHeartBeatAnimation()
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "getLimitedHeartPackage" -> {
                val type = object : TypeToken<BaseModel<ArrayList<GameHeartPackage>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<GameHeartPackage>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                packageData =
                                    baseData.data ?: arrayListOf()
                                initBuyLife(packageData)
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            "getRefToken" -> {
                // Dummy
                val type = object : TypeToken<BaseModel<ReferenceResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ReferenceResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                gameLevelInfoDialog = GameLevelInfoDialog.newInstance(
                                    getString(R.string.invite_friend_title),
                                    getString(R.string.invite_friend_text),
                                    false,
                                    false
                                )
                                inviteFriends(baseData.data!!.reference_token)
                                dataManager.mPref.setReferenceCode(baseData.data?.reference_token)
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            "getHeartSettings" -> {
                val type = object : TypeToken<BaseModel<ArrayList<HeartSettings>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<HeartSettings>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                heartSettingsData =
                                    baseData.data ?: arrayListOf()
                                showSettingsData(heartSettingsData)
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            "updateAdHearts" -> {
                val type = object : TypeToken<BaseModel<HeartsAddResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<HeartsAddResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                val reward: HeartsAddResponse = baseData.data!!
                                val userAdLimit = baseData.data?.user_new_ads_limit
                                dataManager.mPref.prefSetUserAdLimit(userAdLimit)
                                currentHearts = reward.current_hearts.toInt()
                                animateHeartUpdate("+${reward.rewarded_hearts}")
                                updateUserHeart()
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            "getHeartAddPayment" -> {
                val type = object : TypeToken<BaseModel<AdSubscribePaymentResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<AdSubscribePaymentResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        paymentInfo = baseData?.data!!.payment_info
                        initSslPayment()
                    }
                }
            }
            "getPaymentExecution" -> {
                val type = object : TypeToken<BaseModel<LimitedHeartPaymentExcResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<LimitedHeartPaymentExcResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                val response: LimitedHeartPaymentExcResponse = baseData.data!!
                                animateHeartUpdate("+${abs((response.current_hearts.toInt() - currentHearts))}")
                                AppEvent.getInstance(this@HeartAddActivity)
                                    .logLimitedHeartPurchaseEvent(
                                        dataManager.mPref.prefGetUserInfo().name ?: "",
                                        response.current_hearts.toInt() - currentHearts
                                    )
                                dataManager.mPref.prefSetUserHeart(response.current_hearts)
                                updateUserHeart()
                            }
                        } catch (ex: Exception) {
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

            "getAdsPricingList" -> {
                val type = object : TypeToken<BaseModel<ArrayList<AdsPricingList>>>() {}.type
                result.data?.body()?.let {
                    val baseData = Gson().fromJson<BaseModel<ArrayList<AdsPricingList>>>(
                        result.data.body()?.string(),
                        type
                    )
                    if (baseData.status_code == 200) {
                        pricingList = baseData?.data!!
                        var price = pricingList[0].price
                        var days = pricingList[0].days
                        binding.textview23.text = "Remove Ads- $price Tk for $days days"
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
                        initSslPaymentforAdsfreeandsubscription()
                    } else if (baseData.status_code == 500) {
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }
    }

    private fun updateUserHeart() {
        viewModel.dataGetUserHearts()
    }

    private fun showSettingsData(heartSettingsData: ArrayList<HeartSettings>) {
        val sizeData = heartSettingsData.size - 1
        for (i in 0..sizeData) {
            if (heartSettingsData[i].type == "Ads") {
                binding.watchAdDisplayedMoney.text = heartSettingsData[i].hearts
            } else if (heartSettingsData[i].type == "Social Share") {
                binding.invitedFriendMoneyAmount.text = heartSettingsData[i].hearts
            } else if (heartSettingsData[i].type == "Question Add") {
                binding.addQuestionHeartAmount.text = heartSettingsData[i].hearts
            }

        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.exitlayout -> {

                if(isFromGame == "yes"){
                    isActivityChanging = true
                    checkGameSound()
                    finish()
                }
                else if(isFromGame == "no"){
                    if (GameServiceHelper.isMusicRunning)
                        GameServiceHelper.stopMusic(this)
                    finish()
                }


            }
            binding.playBtnLayout -> {
                if(isFromGame == "yes"){
                    isActivityChanging = true
                    checkGameSound()
                    finish()
                }
                else if(isFromGame =="no"){
                   isActivityChanging = true
                    startActivity(Intent(this@HeartAddActivity,GameLevelActivity::class.java))
                    finish()
                }

            }

            binding.linearLayoutSpinnerHeart -> {
                checkGameSound()
                if (binding.recyclerViewLayout.isVisible) {
                    binding.recyclerViewLayout.visibility = View.GONE
                } else {
                    binding.recyclerViewLayout.visibility = View.VISIBLE
                }
            }

            binding.btnAddQuestion -> {
                checkGameSound()
                gameLevelInfoDialog = GameLevelInfoDialog.newInstance(
                    getString(R.string.add_question_text),
                    getString(R.string.add_question_text_details),
                    true,
                    false
                )
                gameLevelInfoDialog.show(supportFragmentManager, gameLevelInfoDialog.tag)
            }
        }
    }

    private fun inviteFriends(refToken: String) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        share.putExtra(Intent.EXTRA_SUBJECT, "Download " + getString(R.string.app_name))
        share.putExtra(
            Intent.EXTRA_TEXT,
            "Hello, I am learning from GuruKlub, you can also learn and please use this Promo Code : \"$refToken\" after Download GuruKlub from https://play.google.com/store/apps/details?id=${packageName} '\n'"
        )
        startActivity(
            Intent.createChooser(
                share,
                "Download " + getString(R.string.app_name)
            )
        )
    }

    override fun onRewardedVideoAdClosed() {
        if (!GameServiceHelper.isMusicRunning && dataManager.mPref.getIsMusicOn())
            GameServiceHelper.playMusic(this)
    }

    override fun onRewardedVideoAdLeftApplication() {

    }

    override fun onRewardedVideoAdLoaded() {

    }

    override fun onRewardedVideoAdOpened() {
        isHomeButtonPressed = false
        stopBackgroundMusic()
    }

    override fun onRewardedVideoCompleted() {
        viewModel.updateAdHearts(this)
    }

    override fun onRewarded(p0: RewardItem?) {

    }

    override fun onRewardedVideoStarted() {

    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
    }

    override fun transactionFail(p0: String?) {
        viewModel.getFailedTransaction("FAILED",  p0.toString(), paymentInfo.tnx_id, this)
    }

    override fun merchantValidationError(p0: String?) {
        viewModel.getFailedTransaction("INVALID",  p0.toString(), paymentInfo.tnx_id, this)
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

    private fun initSslPaymentforAdsfreeandsubscription() {
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


    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isHomeButtonPressed) {
            stopBackgroundMusic()
        }
        isHomeButtonPressed = true
    }


    private fun checkGameSound() {
        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        if (gameSound == true) {
            playBtnSound()
        }
    }

    private fun playBtnSound() {
        gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        val backgroundMusic = MediaPlayer.create(this, R.raw.button_press)
        backgroundMusic.isLooping = false
        backgroundMusic.start()
    }

    private fun configureHearts() {
        //dataManager.mPref.prefSetUserHeart(currentHearts.toString())
        bindHearts()
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "dataGetUserHearts" -> {
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
                configureHearts()
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {

    }

    override fun onOkBtnClicked(title: String) {
        if (this::gameLevelInfoDialog.isInitialized) gameLevelInfoDialog.dismiss()
        if (title.contains("question", true)) {
            checkGameSound()
            isActivityChanging = true
            isHomeButtonPressed = false
            dataManager.mPref.prefNavigateFromGame(true)
            if (GameServiceHelper.isMusicRunning)
                GameServiceHelper.stopMusic(this)
            startActivity(Intent(this, QuestionAddActivity::class.java).putExtra("isGame", "yes"))
        }

    }

    override fun onCloseClicked() {
        checkGameSound()
        if (this::gameLevelInfoDialog.isInitialized) gameLevelInfoDialog.dismiss()
    }

    override fun onCloseBtnClicked() {
        checkGameSound()
        subscribeDialog.dismiss()
    }

    override fun switchBtnClicked() {
        checkGameSound()
    }
}