package com.gmpire.guruklub.view.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.FragmentUpdateProfileDialogBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.profileSetup.ProfileSetupActivity
import com.gmpire.guruklub.view.base.BaseDialogFragment
import com.gmpire.guruklub.view.dialog.gameLevel.GameLevelInfoDialog
import okhttp3.ResponseBody
import retrofit2.Response


class UpdateProfileDialog : BaseDialogFragment() {
    private lateinit var updateProfileListener: UpdateProfileListeners
    private lateinit var mContext: Context
    private  var comingFor: String? =null

    // TODO: Rename and change types of parameters
    private var message: String? = null
//    private var param2: String? = null
    private lateinit var binding : FragmentUpdateProfileDialogBinding
    override fun viewRelatedTask() {
        if(comingFor == "reference_code"){
            binding.titleText.text = "Invite Friends"
            binding.okBtn.text = "Refer Now"
        }
        binding.message.text = message
        binding.okBtn.setOnClickListener{
            when(comingFor){
                "profile_update" ->{
                    val intent = Intent(activity, ProfileSetupActivity::class.java)
                    intent.putExtra("isEdit", true)
                    startActivity(intent)
                    dialog?.dismiss()
                }
                "reference_code" ->{
                    dialog?.dismiss()
                    updateProfileListener.onOkShareBtnClicked()
                }
            }
        }
        binding.relativeLayoutCloseDialog.setOnClickListener {
            dialog?.dismiss()
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mContext = requireContext()
        updateProfileListener = context as UpdateProfileListeners
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle1)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_update_profile_dialog, container, false)

        try {
            message = arguments?.getString("message") as String
            comingFor = arguments?.getString("comingfor") as String
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }

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
        fun newInstance(Message: String,comingfor:String) =
            UpdateProfileDialog().apply {
                arguments = Bundle().apply {
                    this.putString("message", Message)
                    this.putString("comingfor",comingfor)
                }
            }
    }

    interface UpdateProfileListeners{
        fun onOkShareBtnClicked()
    }
}