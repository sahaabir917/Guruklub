package com.gmpire.guruklub.view.activity.notificationDetails

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.notification.NotificationModel
import com.gmpire.guruklub.databinding.ActivityNotificationDetailsBinding
import com.gmpire.guruklub.util.DateUtil
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Response

class NotificationDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationDetailsBinding
    private lateinit var notificationModel: NotificationModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification_details)
        try {
            if (intent.hasExtra("notification")) {
                val notificationString = intent.getStringExtra("notification")
                notificationModel =
                    Gson().fromJson(notificationString, NotificationModel::class.java)
                setToolbar(this, binding.toolbar, notificationModel.title, true)
                setNotificationData()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun viewRelatedTask() {
    }

    override fun navigateToHome() {
    }

    private fun setNotificationData() {
        binding.notificationTitleTv.text = notificationModel.title
        binding.notificationDetailsTv.text = notificationModel.details

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.notificationDetailsTv.text =
                Html.fromHtml(notificationModel.details, Html.FROM_HTML_MODE_COMPACT)
        } else {
            binding.notificationDetailsTv.text =
                Html.fromHtml(notificationModel.details)
        }


        notificationModel.picture?.let {
            Glide.with(this).load(BuildConfig.SERVER_URL + it).into(binding.notificationImageIv)
            binding.notificationImageIv.visibility = View.VISIBLE
        } ?: kotlin.run {
            binding.notificationImageIv.visibility = View.GONE
        }

        binding.dateTv.text =
            DateUtil.simpleDateFormat.format(DateUtil.simpleDateFormatServer.parse(notificationModel.created_at))
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
    }

    override fun onClick(v: View?) {
    }

    override fun onLoading(isLoader: Boolean, key: String) {}

    override fun onBackPressed() {
        if (intent.getStringExtra("from") != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        } else {
            super.onBackPressed()
        }
    }
}
