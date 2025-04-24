package com.gmpire.guruklub.view.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.user.DeviceInfoResponse
import com.gmpire.guruklub.databinding.FragmentInviteFriendPromoCodeBinding
import com.gmpire.guruklub.util.DeviceInfoHelper
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainViewModel
import com.gmpire.guruklub.view.base.BaseDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response


class InviteFriendPromoCodeDialog : BaseDialogFragment() {

    private lateinit var binding: FragmentInviteFriendPromoCodeBinding
    private lateinit var inviteFriendDialogListener: InviteFriendDialogListener
    private lateinit var viewModel: MainViewModel
    private var isAlreadySubmitted = false

    override fun viewRelatedTask() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(MainViewModel::class.java)
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

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_invite_friend_promo_code,
                container,
                false
            )
        binding.submitBtn.setOnClickListener {
            var promoCode = binding.searchEt.text.toString()
            if (promoCode != null) {
                isAlreadySubmitted = true
                viewModel.apiSendPromoCode(
                    this,
                    promoCode,
                    DeviceInfoHelper.getDeviceId(this.activity!!) ?: ""
                )
            } else {
                showToast(context!!, "Please Enter a Referral Code")
            }
        }

        binding.relativeLayoutCloseDialog.setOnClickListener {
            dialog?.dismiss()
        }

        return binding.root
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiSendPromoCode" -> {
                val type = object : TypeToken<BaseModel<DeviceInfoResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<DeviceInfoResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        showToast(context!!, baseData.message[0])
                        if (baseData.status_code == 200) {
                            dataManager.mPref.prefSetPrefRefViewCount(
                                baseData.data?.popup_viewed ?: ""
                            )
                        }
                        dialog?.dismiss()
                    }
                    if (baseData.status_code == 500) {
                        showToast(context!!, baseData.message[0])
                    }
                }
            }
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {

    }

    override fun onError(err: Throwable, key: String) {
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!isAlreadySubmitted) {
            viewModel.apiSendPromoCode(
                this,
                "",
                DeviceInfoHelper.getDeviceId(this.activity!!) ?: ""
            )
        }
        super.onDismiss(dialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is InviteFriendDialogListener) {
            inviteFriendDialogListener = context
        }
    }

    override fun onErrorCode(
        code: Int,
        message: String,
        result: LiveDataResult<Response<ResponseBody>>
    ) {
    }

    interface InviteFriendDialogListener {
        fun onInviteClose()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            InviteFriendPromoCodeDialog().apply {
            }
    }
}