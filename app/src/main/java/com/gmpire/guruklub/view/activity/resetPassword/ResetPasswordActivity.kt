package com.gmpire.guruklub.view.activity.resetPassword

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.ActivityResetPasswordBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.login.LoginActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response

class ResetPasswordActivity : BaseActivity(), View.OnClickListener {

    private lateinit var fromActivity: String
    private lateinit var password: String
    private lateinit var confirmPassword: String
    private lateinit var email: String
    private lateinit var viewModel: ResetPasswordViewModel

    private lateinit var binding: ActivityResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)

        email = intent.getStringExtra("email")
        fromActivity = intent.getStringExtra("from_activity")

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(ResetPasswordViewModel::class.java)


        binding.rootLayout.setOnClickListener(this)
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Reset Password", true)
        binding.btnUpdate.setOnClickListener(this)
    }

    override fun navigateToHome() {
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiChangePassword" -> {
                val type = object : TypeToken<BaseModel<UserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<UserInfo>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        showToast(this, baseData.message[0])
                        if (fromActivity == "settings") {
                            onBackPressed()
                        } else {
                            dataManager.mPref.prefLogout(this)
                            finishAffinity()
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
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
            binding.btnUpdate -> {
                password = binding.passwordTet.text.toString()
                confirmPassword = binding.confirmPasswordTet.text.toString()
                if (password.isEmpty()) {
                    binding.passwordTil.error = "Enter password"
                    return
                } else if (password.length < 4) {
                    binding.passwordTil.error = "Password length should be greater then 3"
                    return
                }

                if (confirmPassword.isEmpty()) {
                    binding.confirmPasswordTil.error = "Confirm your new password"
                    return
                } else if (confirmPassword.length < 4) {
                    binding.confirmPasswordTil.error = "Password length should be greater then 3"
                    return
                } else if (password != confirmPassword) {
                    binding.confirmPasswordTil.error = "Passwords not matched"
                    return
                }

                viewModel.apiChangePassword(email, password, confirmPassword, this)
            }
        }
    }
}

