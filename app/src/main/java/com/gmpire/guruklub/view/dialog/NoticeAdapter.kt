package com.gmpire.guruklub.view.dialog

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.Notice
import com.gmpire.guruklub.databinding.ItemNoticeBoardBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.dialog.notice.NoticeWebViewClient


class NoticeAdapter(itemView: ViewDataBinding, context: Context) : BaseViewHolder(itemView.root) {

    var mContext = context
    var binding = itemView as ItemNoticeBoardBinding

    override fun <T> onBind(position: Int, model: T, mCallback: IAdapterListener) {
        if (model is Notice) {
            binding.textView30.text = model.title ?: ""

            if(model.notify_icon == null){
                binding.imageView14.visibility =View.GONE
            }

            model.notify_icon?.let {
                binding.imageView14.visibility = View.VISIBLE
                Glide.with(mContext)
                    .load(BuildConfig.SERVER_URL + model.notify_icon)
                    .placeholder(R.drawable.ic_placeholder_user)
                    .error(R.drawable.ic_placeholder_user)
                    .into(binding.imageView14)
            }

//            binding.textView31.setHtml(model.notify_description.toString())
            val webView = binding.textView31 as WebView
            webView.loadData(model?.notify_description ?: "", "text/html; charset=utf-8", "UTF-8")
            webView.setPadding(0, 0, 0, 0)
            webView.webViewClient = NoticeWebViewClient(mContext)

            webView.settings.javaScriptEnabled = true
            webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN;
            webView.settings.loadWithOverviewMode = true;


            binding.btnlayout.setOnClickListener{
                mCallback.callBack(position,model,binding.btnlayout)
            }


            if(model.button_text == null ) {
                binding.btnlayout.visibility = View.GONE
            }

            else if(model.button_text!=null && model.button_text!= ""){
                binding.btnlayout.visibility = View.VISIBLE
                binding.btnlayout.text = model.button_text
            }




            binding.imageView16.setOnClickListener {
                mCallback.callBack(position,model,binding.imageView16)
            }


//            val webView = binding.wvNotice as WebView
//            webView.loadData(model.description ?: "", "text/html; charset=utf-8", "UTF-8")
//            webView.setPadding(0, 0, 0, 0)
//            webView.webViewClient = NoticeWebViewClient(mContext)
//
//            webView.settings.javaScriptEnabled = true
//            webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN;
//            webView.settings.loadWithOverviewMode = true;
        }
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }
}