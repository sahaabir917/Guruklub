package com.gmpire.guruklub.view.dialog.gameLevel

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.FragmentGameHeartEmptyDialogBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.BottomSheet.ReportBottomSheet
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import com.gmpire.guruklub.view.base.BaseDialogFragment
import okhttp3.ResponseBody
import retrofit2.Response


class GameLevelOverDialog : BaseDialogFragment() {
    private lateinit var binding: FragmentGameHeartEmptyDialogBinding
    private lateinit var backgroundMusic: MediaPlayer

    var listener: IPurchaseDialogClicked? = null

    override fun viewRelatedTask() {

        binding.closeBtnIv.setOnClickListener {
            checkGameSound()
            listener?.onPurchaseDialogDismiss()
            dismiss()
        }

        binding.tvPurchaseNow.setOnClickListener {
            checkGameSound()
            listener?.onPurchaseNowClick()
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as IPurchaseDialogClicked
    }

    private fun checkGameSound() {
        val gameSound = dataManager.mPref.getGameUserSetting()?.hasGameSound() ?: false
        if (gameSound) {
            playBtnSound()
        }
    }

    private fun playBtnSound() {
        backgroundMusic = MediaPlayer.create(context, R.raw.button_press)
        backgroundMusic.isLooping = false
        backgroundMusic.start()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle1)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_game_heart_empty_dialog,
                container,
                false
            )
        return binding.root
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
        fun newInstance() =
            GameLevelOverDialog()
                .apply {
                    arguments = Bundle().apply {
                    }
                }
    }

    interface IPurchaseDialogClicked {
        fun onPurchaseDialogDismiss()
        fun onPurchaseNowClick()
    }

}