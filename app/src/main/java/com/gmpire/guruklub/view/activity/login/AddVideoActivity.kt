package com.gmpire.guruklub.view.activity.login

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.ActivityAddVideoBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.base.BaseActivity
import kotlinx.android.synthetic.main.answer_description_bottomsheet.view.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception
import java.net.URL

class AddVideoActivity : BaseActivity() {
    private var isVideoTitle: Boolean = false
    private var isValidYoutube: Boolean = false
    private lateinit var videoTitle: String
    private lateinit var binding: ActivityAddVideoBinding
    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Add New Video", true)
        initSearchEditText()
        initTitleText()
        hideKeyboard()
        binding.btnDone.setOnClickListener(this)
        binding.root.setOnClickListener(this)
        binding.btnOpenYoutube.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
    }

    private fun initTitleText() {
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkVideoTitle()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    private fun checkVideoTitle() {
        videoTitle = binding.editText.text.toString()
        if (videoTitle.length > 0) {
            isVideoTitle = true
        } else {
            isVideoTitle = false
        }
        checkValidation()
    }

    private fun checkValidation() {
        if (isValidYoutube && isVideoTitle) {
            binding.btnDone.isEnabled = true
            binding.btnDone.setTextColor(this.resources.getColor(R.color.newwhite))
            val drawableTop = binding.btnDone.background as GradientDrawable
            var color = ColorUtil.getColorForableordisable("youtubebtnable")
            drawableTop.setColor(ContextCompat.getColor(this@AddVideoActivity, color))
        } else {
            binding.btnDone.isEnabled = false
            binding.btnDone.setTextColor(this.resources.getColor(R.color.disabledtextcolor))
            val drawableTop = binding.btnDone.background as GradientDrawable
            var color = ColorUtil.getColorForableordisable("youtubebtndisable")
            drawableTop.setColor(ContextCompat.getColor(this@AddVideoActivity, color))
        }

    }


    private fun initSearchEditText() {
        binding.videourl.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                findYoutubeVideo(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    private fun findYoutubeVideo(videoUrl: String) {

        try {
            val videoId = getVideoId(videoUrl)
                ?: throw Exception("Invalid VIDEO") //you will get this video id "tResEeK6P5I"

            val thumbnailMq = "http://img.youtube.com/vi/$videoId/mqdefault.jpg"
            Glide.with(this)
                .applyDefaultRequestOptions(
                    RequestOptions()
                        .placeholder(R.drawable.youtube_thumb_lower_png)
                        .error(R.drawable.youtube_thumb_lower_png)
                )
                .load(thumbnailMq)
                .into(binding.imageView24)
            isValidYoutube = true
            binding.sharerule.visibility = View.GONE
            checkValidation()
        } catch (ex: Exception) {
            ex.printStackTrace()
            binding.btnDone.isEnabled = false
            isValidYoutube = false
            binding.sharerule.visibility = View.VISIBLE
            checkValidation()
            binding.btnDone.setTextColor(this.resources.getColor(R.color.disabledtextcolor))
            val drawableTop = binding.btnDone.background as GradientDrawable
            var color = ColorUtil.getColorForableordisable("youtubebtndisable")
            drawableTop.setColor(ContextCompat.getColor(this@AddVideoActivity, color))
        }
    }

    private fun getVideoId(videoUrl: String): String? {

        val url = URL(videoUrl)
        val host = url.host

        if (host == "youtu.be") {
            val splits = videoUrl.split("/")
            return splits[splits.size - 1]
        }
        if (host == "youtube.com") {
            return videoUrl.split("v=")[1]
        }

        return null
    }

    override fun navigateToHome() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_video)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnDone -> {
                var videoTitle = binding.editText.text.toString()
                var videoUrl = binding.videourl.text.toString()
                val intent = Intent(this, AddVideoNextActivity::class.java)
                intent.putExtra("videoTitle", videoTitle)
                intent.putExtra("videoUrl", videoUrl)
                startActivity(intent)
            }
            binding.btnOpenYoutube -> {
//                val intent = Intent(this, YoutubeActivity::class.java)
//                startActivity(intent)

                val customTabBuilder = CustomTabsIntent.Builder()
                val customTabIntent = customTabBuilder.build()
                customTabIntent.launchUrl(this@AddVideoActivity, Uri.parse("https://youtube.com/"))


            }
            binding.btnCancel -> {
                onBackPressed()
            }
            binding.root -> {
                hideKeyboard()
            }
        }
    }
}