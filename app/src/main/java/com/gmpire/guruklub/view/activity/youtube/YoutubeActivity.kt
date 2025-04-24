package com.gmpire.guruklub.view.activity.youtube

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.ActivityYoutubeBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.base.BaseActivity
import okhttp3.ResponseBody
import retrofit2.Response

class YoutubeActivity : BaseActivity() {
    private lateinit var binding: ActivityYoutubeBinding
    private lateinit var webView: WebView
    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Youtube", true)
        LoadWebView()
    }

    private fun LoadWebView() {
        binding.webview.settings.javaScriptEnabled = true

        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }
        }
        binding.webview.loadUrl("https://www.youtube.com/")
    }


//    override fun onBackPressed() {
//        if(binding.webview.canGoBack()){
//            binding.webview.goBack()
//        }
//        else{
//        super.onBackPressed()
//        }
//    }

    override fun navigateToHome() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_youtube)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {

    }
}