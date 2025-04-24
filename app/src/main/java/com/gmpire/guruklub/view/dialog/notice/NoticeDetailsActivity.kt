package com.gmpire.guruklub.view.dialog.notice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.Notice
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.databinding.ActivityNoticeDetailsBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Response

class NoticeDetailsActivity : BaseActivity() {

    private var notice: Notice? = null

    private lateinit var binding: ActivityNoticeDetailsBinding
    override fun viewRelatedTask() {

        setToolbar(this, binding.toolbar, "", true)
        binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
        val text =
            "<font color=#000000>Guru</font><font color=#4A148C>Klub</font>"
        binding.toolbar.appCompatTextViewLogo.text = (Html.fromHtml(text))


        notice = Gson().fromJson(
            intent.getStringExtra("noticevalues"),
            Notice::class.java
        )

        binding.tvNoticeTitle.text = notice?.title

        initWebView()
        if(notice?.picture!=null){
            Glide.with(this)
                .load(BuildConfig.SERVER_URL + notice?.picture)
                .placeholder(R.drawable.ic_placeholder_user)
                .error(R.drawable.ic_placeholder_user)
                .into(binding.noticeImage)
        }
        else{
            binding.noticeImage.visibility = View.GONE
        }


    }

    private fun initWebView() {
            val webView = binding.wvNotice as WebView
            webView.loadData(notice?.description ?: "", "text/html; charset=utf-8", "UTF-8")
            webView.setPadding(0, 0, 0, 0)
            webView.webViewClient = NoticeWebViewClient(this)

            webView.settings.javaScriptEnabled = true
            webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN;
            webView.settings.loadWithOverviewMode = true;
    }

    override fun navigateToHome() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notice_details)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {

    }
}