package com.gmpire.guruklub.view.activity.emailVerification

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.ActivityEmailVerificationBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.profileSetup.ProfileSetupActivity
import com.gmpire.guruklub.view.activity.resetPassword.ResetPasswordActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_email_verification.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class EmailVerificationActivity : BaseActivity(), View.OnClickListener {

    private var from: String? = null
    private lateinit var code: String
    private lateinit var timer: CountDownTimer
    private lateinit var viewModel: EmailVerificationViewModel
    private lateinit var binding: ActivityEmailVerificationBinding
    private lateinit var email: String
    private var codeFromIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_email_verification)

        email = intent.getStringExtra("email")
        codeFromIntent = intent.getStringExtra("code")

        from = intent.getStringExtra("from_activity")

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(EmailVerificationViewModel::class.java)


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

        binding.rootLayout.setOnClickListener(this)

    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Email Verification", true)

        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.textView.text = (Html.fromHtml(text))

        binding.btnGo.setOnClickListener(this)
        binding.resendBtn.setOnClickListener(this)
    }

    override fun navigateToHome() {
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiEmailVerification" -> {
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
                            if (baseData.token != null) {
                                dataManager.mPref.prefSetToken(baseData.token)
                            }
                            dataManager.mPref.prefLogin()
//                            startActivity(Intent(this, MainActivity::class.java))
                            startActivity(
                                Intent(this, MainActivity::class.java).putExtra(
                                    "login_count",
                                    baseData.data?.user_settings?.login_count
                                )
                            )
                        }
                    } else if (baseData.status_code == 455) {
                        binding.verificationCodeTv.error = baseData.message[0]
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }

            "apiForgetPasswordCodeVerify" -> {
                val type = object : TypeToken<BaseModel<UserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<UserInfo>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        val userInfo = baseData.data
                        if(userInfo?.category_id.isNullOrEmpty()) {
                            userInfo?.category_id = "1"
                        }
                        dataManager.mPref.prefSetUserInfo(userInfo)
                        startActivity(
                            Intent(
                                this,
                                ResetPasswordActivity::class.java
                            ).putExtra("from_activity", "forget_pass").putExtra("email", email)
                        )
                    } else if (baseData.status_code == 455) {
                        binding.verificationCodeTv.error = baseData.message[0]
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }

            "apiEmailVerificationResend" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        binding.resendBtn.visibility = View.GONE
                        binding.countdownTimerTv.visibility = View.VISIBLE
                        timer.start()
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
            "apiForgetPassword" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        binding.resendBtn.visibility = View.GONE
                        binding.countdownTimerTv.visibility = View.VISIBLE
                        timer.start()
                    } else if (baseData.status_code == 455) {
                        binding.textInputLayout.error = baseData.message[0]
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.rootLayout -> {
                hideKeyboard()
            }

            binding.btnGo -> {
                code = verification_code_tv.text.toString()
                if (code.length < 6) {
                    verification_code_tv.error =
                        "Please provide the 6 digit code that sent to your email"
                    return
                }

                if (from != null) {
                    // viewModel.apiForgetPasswordCodeVerify(email, code, this)
                    if (codeFromIntent != null) {
                        if (codeFromIntent == code) {
                            startActivity(
                                Intent(
                                    this,
                                    ResetPasswordActivity::class.java
                                ).putExtra("from_activity", "forget_pass").putExtra("email", email)
                            )
                        } else {
                            showToast(this, "Please type the correct verification code.")
                        }
                    }
                } else {
                    viewModel.apiEmailVerification(email, code, this)
                }

            }
            binding.resendBtn -> {
                if (from != null) {
                    viewModel.apiForgetPassword(email, this)
                } else {
                    viewModel.apiEmailVerificationResend(email, this)
                }
            }
        }

    }
}
