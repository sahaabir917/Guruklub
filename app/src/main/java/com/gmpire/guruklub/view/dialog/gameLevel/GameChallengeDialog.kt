package com.gmpire.guruklub.view.dialog.gameLevel

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.gameChallenge.GameChallenge
import com.gmpire.guruklub.data.model.gameheartpackages.GuruKlubGameHeartChallengeCriteria
import com.gmpire.guruklub.databinding.FragmentGameChallengeBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.base.BaseDialogFragment
import okhttp3.ResponseBody
import retrofit2.Response
import toElipsisTxt


class GameChallengeDialog : BaseDialogFragment() {
    private lateinit var binding: FragmentGameChallengeBinding
    private lateinit var actionListener: ActionListener
    private var gameChallenge: GameChallenge? = null
    private var playNextLevel = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle1)
        isCancelable = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actionListener = context as ActionListener
        gameChallenge = arguments?.getSerializable("challenge") as GameChallenge
        playNextLevel = arguments?.getBoolean("playNextLevel", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_game_challenge, container, false)
        bindProfileImage()
        return binding.root
    }

    private fun bindProfileImage() {
        val userInfo = dataManager.mPref.prefGetUserInfo()

        Glide.with(this)
            .load(BuildConfig.SERVER_URL + userInfo.picture)
            .placeholder(R.drawable.ic_placeholder_user)
            .error(R.drawable.ic_placeholder_user)
            .into(binding.icvUserIcon)
    }


    override fun viewRelatedTask() {
        binding.tvTakeChallenge.setOnClickListener {
            dismiss()
            actionListener.startChallenge()
        }

        binding.tvNotNow.setOnClickListener {
            dismiss()
            actionListener.declineChallenge(playNextLevel)
        }

        binding.tvKnowMore.setOnClickListener {
            actionListener.challengeDetails()
        }

        bindUi()
    }

    private fun bindUi() {

        val headerTxt = "Win " + gameChallenge?.reward_hearts ?: ""
        binding.tvChallengeHeader.text = headerTxt
        binding.tvBonusHeart.text = gameChallenge?.reward_hearts?.toString() ?: ""
        binding.tvTargetScore.text = gameChallenge?.target_points?.toString() ?: ""
        binding.tvQuestions.text = "Unlimited"
        gameChallenge?.question_number?.let {
            if (it == 0)
                binding.tvQuestions.text = "Unlimited"
            else
                binding.tvQuestions.text = it.toString()
        }

        binding.tvTime.text =
            getTimeFormattedString((gameChallenge?.challenge_time?.toInt() ?: 0) * 60)

        if (gameChallenge?.criteria_type == GuruKlubGameHeartChallengeCriteria.MARATHON) {
            binding.tvTime.text = "Unlimited"
        }

        val userName = dataManager.mPref.prefGetUserInfo().name

        val subTitle = (userName?.toElipsisTxt() ?: "")

        binding.tvChallengeSubtitle.text = subTitle

    }


    private fun getTimeFormattedString(seconds: Int): String {
        if (seconds == 0) return "---"
        var hour = seconds / 60
        val min = hour % 60
        val sec = seconds % 60
        hour /= 60
        return "${String.format("%02d", hour)}:${String.format("%02d", min)}:${
        String.format(
            "%02d",
            sec
        )
        }"
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

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

    companion object {
        @JvmStatic
        fun newInstance(challenge: GameChallenge, playNextLevel: Boolean) =
            GameChallengeDialog().apply {
                arguments = Bundle().apply {
                    putSerializable("challenge", challenge)
                    putBoolean("playNextLevel", playNextLevel)
                }
            }
    }

    interface ActionListener {
        fun startChallenge()
        fun declineChallenge(playNextLevel: Boolean)
        fun challengeDetails()
    }

}