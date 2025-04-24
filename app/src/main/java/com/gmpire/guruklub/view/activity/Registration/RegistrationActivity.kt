package com.gmpire.guruklub.view.activity.Registration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.Registration
import com.gmpire.guruklub.databinding.ActivityRegistrationBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.LocalValidator
import com.gmpire.guruklub.view.activity.emailVerification.EmailVerificationActivity
import com.gmpire.guruklub.view.activity.phoneVerification.PhoneVerificationActivity
import com.gmpire.guruklub.view.activity.termsAndCondition.TermsConditionActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_registration.*
import okhttp3.ResponseBody
import retrofit2.Response

const val ACTIVITY_NAME = "activity_name"

class RegistrationActivity : BaseActivity(), View.OnClickListener {

    private lateinit var viewModel: RegistrationViewModel
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var emailOrPhone: String
    private lateinit var password: String
    private lateinit var confirm_password: String
    private var toc: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_registration)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(RegistrationViewModel::class.java)

        binding.rootLayout.setOnClickListener(this)

        binding.showPasswordCb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.passwordEt.transformationMethod =
                    HideReturnsTransformationMethod.getInstance();
                binding.confirmPasswordEt.transformationMethod =
                    HideReturnsTransformationMethod.getInstance();
            } else {
                binding.passwordEt.transformationMethod =
                    PasswordTransformationMethod.getInstance();
                binding.confirmPasswordEt.transformationMethod =
                    PasswordTransformationMethod.getInstance();
            }
        }
    }

    override fun viewRelatedTask() {
        binding.btnSignUp.setOnClickListener(this)
        binding.tvTermsAndConditions.setOnClickListener(this)
    }

    override fun navigateToHome() {

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiRegistrationRequest" -> {
                val type = object : TypeToken<Registration>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<Registration>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (baseData?.email_status == 0) {
                            startActivity(
                                Intent(this, PhoneVerificationActivity::class.java)
                                    .putExtra("phone", emailOrPhone)
                            )
                        } else {
                            startActivity(
                                Intent(this, EmailVerificationActivity::class.java)
                                    .putExtra("email", emailOrPhone)
                            )
                        }
                    }
                    showToast(this, baseData.message[0])
                }
            }
        }

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.rootLayout -> {
                hideKeyboard()
            }
            binding.btnSignUp -> {
                emailOrPhone = binding.emailEt.text.toString()
                password = binding.passwordEt.text.toString()
                confirm_password = binding.confirmPasswordEt.text.toString()
                if (cbTermsAndConditions.isChecked) toc = 1 else toc = 0

                if (emailOrPhoneValid(emailOrPhone) && passwordValid(
                        password,
                        confirm_password
                    ) && toChecked()
                ) {
                    viewModel.apiRegistrationRequest(
                        emailOrPhone,
                        password,
                        confirm_password,
                        toc,
                        this
                    )
                }

            }
            binding.tvTermsAndConditions -> {
                val intent = Intent(this, TermsConditionActivity::class.java)
                intent.putExtra(ACTIVITY_NAME, "registration")
                startActivityForResult(intent, 100)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 100) {
            if (resultCode === Activity.RESULT_OK) {
                toc = data?.getIntExtra("toc", 0) ?: 0
                if (toc == 1) {
                    binding.cbTermsAndConditions.isChecked = true
                }
            }
        }
    }

    private fun passwordValid(password: String, confirm_password: String): Boolean {
        if (password.isEmpty()) {
            binding.passwordEt.error = "Enter your password to continue"
            return false
        } else if (password.length < 4) {
            binding.passwordEt.error = "Password length should not less then four digit"
            return false
        } else if (confirm_password != password) {
            binding.confirmPasswordEt.error = "Passwords didn't matched"
            return false
        }
        return true
    }

    private fun emailOrPhoneValid(emailOrPhone: String): Boolean {
        if (emailOrPhone.isEmpty()) {
            binding.emailEt.error = "Enter your email to continue"
            return false
        } else {
            if (!LocalValidator.isEmailValid(emailOrPhone)
                && !LocalValidator.isPhoneValid(emailOrPhone)
            ) {
                binding.emailEt.error = "Please provide valid email/mobile no."
                return false
            }
        }
        return true
    }

    private fun toChecked(): Boolean {
        if (!cbTermsAndConditions.isChecked) {
            showToast(this, "You need to agree with our terms and conditions to continue with us")
            return false
        }
        return true
    }
}
