package com.gmpire.guruklub.view.activity.friendrequest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.FragmentHeartChooseDialogBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.playwithfriends.PlayWithFriendActivity
import com.gmpire.guruklub.view.base.BaseDialogFragment
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelInfoDialog
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.android.synthetic.main.custom_exam_bottom_sheet.view.*
import okhttp3.ResponseBody
import retrofit2.Response

class HeartChooseDialog : BaseDialogFragment() {
    private  var user_heart: String = "0"
    private var progressbar: Int = 0
    private lateinit var binding: FragmentHeartChooseDialogBinding
    private lateinit var heartChooseDialogListener: HeartChooseDialogListener

    override fun viewRelatedTask() {
        initSeekBar()
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        heartChooseDialogListener = context as HeartChooseDialogListener
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyle1)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_heart_choose_dialog,
                container,
                false
            )
        try {
            user_heart = arguments?.getString("user_hearts") as String?:"0"

        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }

        binding.closeBtnIv.setOnClickListener{
            dialog?.dismiss()
        }

        binding.okBtnLayout.setOnClickListener{
            dialog?.dismiss()
            startActivity(Intent(activity,PlayWithFriendActivity::class.java))
        }

        binding.ruleTitle.text = "Select How Many Heart"

        return binding.root
    }

    private fun initSeekBar() {
        var maxHeart = user_heart.toFloat()
        binding.seekbar.max = maxHeart
        binding.seekbar.min = 1F
        binding.seekbar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
//                binding.textViewNumOfQues.text =
//                    ((seekParams.thumbPosition + 1) * 40).toString()
                binding.heartCount.text =  seekParams.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar) {}
            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar) {
            }
        }
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
        fun newInstance(user_hearts : String) =
            HeartChooseDialog().apply {
                arguments = Bundle().apply {
                    this.putString("user_hearts", user_hearts)
                }
            }
    }

    interface HeartChooseDialogListener {

    }
}