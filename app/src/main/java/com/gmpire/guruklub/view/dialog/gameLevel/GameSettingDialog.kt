package com.gmpire.guruklub.view.dialog.gameLevel

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.adspricinglist.AdsPricingList
import com.gmpire.guruklub.data.model.gamelevel.GameUserInfo
import com.gmpire.guruklub.data.model.gameusersettings.UserSettings
import com.gmpire.guruklub.databinding.ActivityGameSettingBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelViewModel
import com.gmpire.guruklub.view.base.BaseDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response

class GameSettingDialog() : BaseDialogFragment() {

    private lateinit var binding: ActivityGameSettingBinding
    private lateinit var gamesettinglistener: GameSettingDialogListener
    private lateinit var viewModel: GameLevelViewModel
    private lateinit var pricingList: ArrayList<AdsPricingList>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle1)
    }

    override fun viewRelatedTask() {
        checkBackgroundMusicSettings()
        checkGameSound()
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(GameLevelViewModel::class.java)
        viewModel.getAdsPricingList(this)

        binding.gameMusicToggleBtn.setOnClickListener {
            val backgroundMusic = dataManager.mPref.getGameUserSetting()?.game_background_music ?: 0
            if (backgroundMusic == 0) {
                ChangeGameSettings(1)
                binding.gameMusicToggleBtn.setImageResource(R.drawable.subscribe_switch_on_btn)
                var gamebtnmusic =
                    (dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: 0).toString()
                if (gamebtnmusic == "true") {
                    gamebtnmusic = "1"
                } else if (gamebtnmusic == "false") {
                    gamebtnmusic = "0"
                }
                viewModel.apiSetGameSound(
                    "1",
                    gamebtnmusic,
                    this
                )
                gamesettinglistener.onMusicbtnClicked(true)
            } else {
                binding.gameMusicToggleBtn.setImageResource(R.drawable.subscribe_switch_off_btn)
                ChangeGameSettings(0)
                var gamebtnmusic =
                    (dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: 0).toString()
                if (gamebtnmusic == "true") {
                    gamebtnmusic = "1"
                } else if (gamebtnmusic == "false") {
                    gamebtnmusic = "0"
                }
                viewModel.apiSetGameSound(
                    "0",
                    gamebtnmusic,
                    this
                )
                gamesettinglistener.onMusicbtnClicked(false)
            }
        }

        binding.gameSoundToggleBtn.setOnClickListener {
            val gameSound = dataManager.mPref.getGameUserSetting()?.game_sound
            if (gameSound == 0) {
                binding.gameSoundToggleBtn.setImageResource(R.drawable.subscribe_switch_on_btn)
                ChangeGameSoundSettings(1)
                var gameBackgroundMusic =
                    (dataManager.mPref.getGameUserSetting()?.hasBackgroundSound() ?: 0).toString()
                if (gameBackgroundMusic == "true") {
                    gameBackgroundMusic = "1"
                } else if (gameBackgroundMusic == "false") {
                    gameBackgroundMusic = "0"
                }
                viewModel.apiSetGameSound(
                    gameBackgroundMusic,
                    "1",
                    this
                )
            } else {
                binding.gameSoundToggleBtn.setImageResource(R.drawable.subscribe_switch_off_btn)
                ChangeGameSoundSettings(0)
                var gameBackgroundMusic =
                    (dataManager.mPref.getGameUserSetting()?.hasBackgroundSound() ?: 0).toString()
                if (gameBackgroundMusic == "true") {
                    gameBackgroundMusic = "1"
                } else if (gameBackgroundMusic == "false") {
                    gameBackgroundMusic = "0"
                }
                viewModel.apiSetGameSound(
                    gameBackgroundMusic,
                    "0",
                    this
                )
            }
        }
    }

    private fun checkGameSound() {
        val gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        if (gameSound) {
            binding.gameSoundToggleBtn.setImageResource(R.drawable.subscribe_switch_on_btn)
        } else {
            binding.gameSoundToggleBtn.setImageResource(R.drawable.subscribe_switch_off_btn)
        }
    }


    private fun checkBackgroundMusicSettings() {
        val backgroundMusic = dataManager.mPref.getGameUserSetting()?.hasBackgroundSound() ?: false
        if (backgroundMusic) {
            binding.gameMusicToggleBtn.setImageResource(R.drawable.subscribe_switch_on_btn)
        } else {
            binding.gameMusicToggleBtn.setImageResource(R.drawable.subscribe_switch_off_btn)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GameSettingDialogListener) {
            gamesettinglistener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.activity_game_setting, container, false)

        binding.closeBtnIv.setOnClickListener {
            gamesettinglistener.OnCloseBtnClicked()
        }

        binding.subscribetext.setOnClickListener {
            gamesettinglistener.OnSubscribeBtnClicked()
        }

        binding.textView23.setOnClickListener {
            gamesettinglistener.OnFreeAdBtnClicked()
        }

        return binding.root
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiSetGameSound" -> {
                val type = object : TypeToken<BaseModel<GameUserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameUserInfo>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        Log.d("basedata", baseData.data.toString())
                        showToast(context!!, baseData.message[0])
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
                        binding.textView23.text = "Remove Ads- $price Tk for $days days"
                    }
                }
            }
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {

    }

    override fun onError(err: Throwable, key: String) {

    }

    override fun onErrorCode(
        code: Int,
        message: String,
        result: LiveDataResult<Response<ResponseBody>>
    ) {

    }

    private fun ChangeGameSettings(background_music: Int) {
        var ads_free = dataManager.mPref.getGameUserSetting()?.ads_free
        var game_sound = dataManager.mPref.getGameUserSetting()?.game_sound
        var id = dataManager.mPref.getGameUserSetting()?.id
        var shareLimit = dataManager.mPref.getGameUserSetting()?.user_share_limit
        var user_ad_limit = dataManager.mPref.getGameUserSetting()?.user_ads_limit
        var count_consecutive =  dataManager.mPref.getGameUserSetting()?.corr_ans_consecutive_count
        var user_practice_hearts_limit =  dataManager.mPref.getGameUserSetting()?.user_practice_hearts_limit
        dataManager.mPref.setGameUserSetting(
            UserSettings(
                id ?: 0,
                shareLimit ?: "0",
                user_ad_limit ?: "0",
                ads_free ?: "0",
                game_sound ?: 0,
                background_music,
                "1",
                count_consecutive ?: "0",
                user_practice_hearts_limit ?: "0"
            )
        )
    }

    private fun ChangeGameSoundSettings(gamesound: Int) {
        var ads_free = dataManager.mPref.getGameUserSetting()?.ads_free
        var background_music = dataManager.mPref.getGameUserSetting()?.game_background_music
        var id = dataManager.mPref.getGameUserSetting()?.id
        var shareLimit = dataManager.mPref.getGameUserSetting()?.user_share_limit
        var user_ad_limit = dataManager.mPref.getGameUserSetting()?.user_ads_limit
        var count_consecutive =  dataManager.mPref.getGameUserSetting()?.corr_ans_consecutive_count
        var user_practice_hearts_limit =  dataManager.mPref.getGameUserSetting()?.user_practice_hearts_limit
        dataManager.mPref.setGameUserSetting(
            UserSettings(
                id ?: 0,
                shareLimit ?: "0",
                user_ad_limit ?: "0",
                ads_free ?: "0",
                gamesound,
                background_music ?: 0,
                "1",
                count_consecutive ?: "0",
                user_practice_hearts_limit ?: "0"
            )
        )
        gamesettinglistener.onGameBtnSwitchClicked()
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            GameSettingDialog().apply {
                arguments = Bundle().apply {
                }
            }
    }

    interface GameSettingDialogListener {
        fun OnCloseBtnClicked()
        fun OnSubscribeBtnClicked()
        fun OnFreeAdBtnClicked()
        fun onMusicbtnClicked(boolean: Boolean)
        fun onGameBtnSwitchClicked()
    }

}