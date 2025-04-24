package com.gmpire.guruklub.view.dialog.notice

import android.content.Context
import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import com.gmpire.guruklub.R

class NoticeWebViewClient(var context: Context) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url
        url?.let {
            val customTabBuilder = CustomTabsIntent.Builder()
            val customTabIntent = customTabBuilder.build()
            customTabIntent.launchUrl(context, it)
        }
        return true
    }
}

class GameWebViewClient(var context: Context) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url
        url?.let {
            val customTabBuilder = CustomTabsIntent.Builder()
            val customTabIntent = customTabBuilder.build()
            customTabIntent.launchUrl(context, it)
        }
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view?.setBackgroundColor(Color.parseColor("#220545"))
    }

}