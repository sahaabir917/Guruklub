package com.gmpire.guruklub.view.activity.forgetPassword

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.ForgetPWResponse
import com.gmpire.guruklub.data.model.Registration
import com.gmpire.guruklub.databinding.ActivityForgetPasswordBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.LocalValidator
import com.gmpire.guruklub.view.activity.emailVerification.EmailVerificationActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.phoneVerification.PhoneVerificationActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response

class ForgetPasswordActivity : BaseActivity(), View.OnClickListener {

    private lateinit var emailOrPhone: String
    private lateinit var viewModel: ForgetPasswordViewModel

    private lateinit var binding: ActivityForgetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(ForgetPasswordViewModel::class.java)

        binding.rootLayout.setOnClickListener(this)
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Reset Password", true)
        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.textView.text = Html.fromHtml(text)
        binding.btnEmail.setOnClickListener(this)
    }

    override fun navigateToHome() {
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiForgetPassword" -> {
                val type = object : TypeToken<ForgetPWResponse>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<ForgetPWResponse>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (baseData?.email_status == 0) {
                            startActivity(
                                Intent(this, PhoneVerificationActivity::class.java)
                                    .putExtra("phone", emailOrPhone)
                                    .putExtra("from_activity", "password_reset")
                                    .putExtra("code", baseData.code.toString())
                            )
                        } else {
                            startActivity(
                                Intent(this, EmailVerificationActivity::class.java)
                                    .putExtra("email", emailOrPhone)
                                    .putExtra("from_activity", "password_reset")
                                    .putExtra("code", baseData.code.toString())
                            )
                        }
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

            binding.btnEmail -> {
                emailOrPhone = binding.emailEt.text.toString()
                if (!LocalValidator.isEmailValid(emailOrPhone) && !(LocalValidator.isPhoneValid(
                        emailOrPhone
                    ))
                ) {
                    binding.textInputLayout.error = "Enter a valid email address/mobile number"
                    return
                }
                viewModel.apiForgetPassword(emailOrPhone, this)
            }
        }
    }
}

