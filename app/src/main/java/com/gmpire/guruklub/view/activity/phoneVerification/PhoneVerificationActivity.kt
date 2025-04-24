package com.gmpire.guruklub.view.activity.phoneVerification

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.data.model.user.PhoneVarification
import com.gmpire.guruklub.databinding.ActivityPhoneVerificationBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.resetPassword.ResetPasswordActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PhoneVerificationActivity : BaseActivity() {

    private var from: String? = null
    private lateinit var timer: CountDownTimer
    private lateinit var viewModel: PhoneVerificationViewModel
    private lateinit var binding: ActivityPhoneVerificationBinding
    private var code: String? = null
    private lateinit var phone: String
    private var codeFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(PhoneVerificationViewModel::class.java)

        try {
            if (intent.extras?.containsKey("phone")!!)
                phone = intent.getStringExtra("phone")
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        codeFromIntent = intent.getStringExtra("code")

        // viewModel.apiPhoneVerificationResend(phone, this)
        from = intent.getStringExtra("from_activity")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_verification)

        timer = object : CountDownTimer(5 * 60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.countdownTimerTv.text =
                    SimpleDateFormat("mm:ss").format(Date(millisUntilFinished))
            }

            override fun onFinish() {
                binding.countdownTimerTv.visibility = View.GONE
                binding.resendBtn.visibility = View.VISIBLE
            }
        }

        timer.start()

    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbarPhoneVerification, "Phone Verification", true)

        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.textView.text = Html.fromHtml(text)

        binding.verifyBtn.setOnClickListener(this)
        binding.resendBtn.setOnClickListener(this)

    }

    override fun navigateToHome() {
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiPhoneVerification" -> {
                val type = object : TypeToken<BaseModel<UserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<UserInfo>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            val userInfo = baseData.data
                            if(userInfo?.category_id.isNullOrEmpty()) {
                                userInfo?.category_id = "1"
                            }
                            dataManager.mPref.prefSetUserInfo(userInfo)
                            dataManager.mPref.prefLogin()
                            if (baseData.token != null) {
                                dataManager.mPref.prefSetToken(baseData.token)
                            }
                            //startActivity(Intent(this, CategoryAndSubjectSelectionActivity::class.java))
//                            startActivity(Intent(this, MainActivity::class.java))
                            startActivity(
                                Intent(this, MainActivity::class.java).putExtra(
                                    "login_count",
                                    baseData.data?.user_settings?.login_count
                                )
                            )
                            finishAffinity()
                        }
                    } else if (baseData.status_code == 455) {
                        binding.verificationCodeTv.error = baseData.message[0]
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }

            "apiPhoneVerificationResend" -> {
                val type = object : TypeToken<BaseModel<PhoneVarification>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<PhoneVarification>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        viewModel.apiPhoneVerification(
                            phone,
                            baseData.data?.phone_code.toString(),
                            this
                        )
                        binding.resendBtn.visibility = View.GONE
                        binding.countdownTimerTv.visibility = View.VISIBLE
                        timer.start()
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.verifyBtn -> {
                code = binding.verificationCodeTv.text.toString()

                try {
                    if (code == null || code!!.length < 4) {
                        binding.verificationCodeTv.error =
                            "Please provide the 4 digit code we have sent to your phone number"
                        return
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (from != null) {
                    // viewModel.apiForgetPasswordCodeVerify(email, code, this)
                    if (codeFromIntent != null) {
                        if (codeFromIntent == code) {
                            startActivity(
                                Intent(
                                    this,
                                    ResetPasswordActivity::class.java
                                ).putExtra("from_activity", "forget_pass").putExtra("email", phone)
                            )
                        } else {
                            showToast(this, "Please type the correct verification code.")
                        }
                    }
                } else {
                    viewModel.apiPhoneVerification(phone, code.toString(), this)
                }
            }
            binding.resendBtn -> {
                viewModel.apiPhoneVerificationResend(phone, this)
            }
        }

    }
}
