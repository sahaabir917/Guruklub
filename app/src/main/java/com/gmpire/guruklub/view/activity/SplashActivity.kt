package com.gmpire.guruklub.view.activity

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.user.DeviceInfoResponse
import com.gmpire.guruklub.databinding.ActivitySplashBinding
import com.gmpire.guruklub.util.ConnectivityUtil
import com.gmpire.guruklub.util.DeviceInfoHelper
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.login.LoginViewModel
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trncic.library.DottedProgressBar
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.SocketException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashActivity : BaseActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var binding : ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        this.viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(LoginViewModel::class.java)

        if (!ConnectivityUtil.isOnline(this)) {
           splashRedirection()
        }
        else {
            viewModel.userVersionCheck(
                BuildConfig.VERSION_NAME,
                this
            )
        }

        try {
            val info = packageManager.getPackageInfo(
                BuildConfig.APPLICATION_ID,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        binding.progressSplash.startProgress()
        // Version Name
        binding.tvVersionName.text = "Version: ${BuildConfig.VERSION_NAME}"
    }


    private fun sendDeviceInfo() {
        dataManager.mPref.getFcmToken(this@SplashActivity)?.let {
            viewModel.fetchDeviceFcmToken(
                android.os.Build.DEVICE,
                android.os.Build.MODEL,
                android.os.Build.PRODUCT,
                android.os.Build.VERSION.RELEASE,
                DeviceInfoHelper.getDeviceId(this@SplashActivity) ?: "",
                dataManager.mPref.getFcmToken(this@SplashActivity).toString(), this@SplashActivity
            )
        }
    }


    private fun splashRedirection() {
        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                finishAffinity()
            else {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }, 2000)

    }

    override fun viewRelatedTask() {
    }

    override fun navigateToHome() {}

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "fetchDeviceFcmToken" -> {
                try {
                    val type = object : TypeToken<BaseModel<DeviceInfoResponse>>() {}.type
                    result.data?.body()?.let {
                        val baseData =
                            Gson().fromJson<BaseModel<DeviceInfoResponse>>(
                                result.data.body()?.string(), type
                            )
                        if (baseData.status_code == 200) {
                            dataManager.mPref.prefSetPrefRefViewCount(
                                baseData.data?.popup_viewed ?: ""
                            )
                        }
                    }
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            "userVersionCheck" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (!baseData.status)
                            initShowUpdateDialog()
                        else {
                            splashRedirection()
                            sendDeviceInfo()
                        }
                        Log.d("message", baseData.message.toString())
                    } else {
                        showToast(this, "No data found!!")
                        finish()
                    }
                }
            }
        }
    }

    override fun onError(err: Throwable, key: String) {
        super.onError(err, key)
        if (err is SocketException) {
            splashRedirection()
        }
        Log.d("Error", err.toString())
    }

    private fun initShowUpdateDialog() {
        AlertDialog.Builder(this)
            .setTitle("New version of app is available in the play store!")
            .setNegativeButton("Cancel") { dialog, which ->
                finishAffinity()
            }
            .setPositiveButton(
                "UPDATE"
            ) { dialog, which ->
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        )
                    )
                }
            }.show()

    }

    override fun onLoading(isLoader: Boolean, key: String) {

    }

    override fun onClick(v: View?) {
    }

}