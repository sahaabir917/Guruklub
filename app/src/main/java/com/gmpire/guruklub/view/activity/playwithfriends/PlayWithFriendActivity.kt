package com.gmpire.guruklub.view.activity.playwithfriends

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.ActivityPlayWithFriendBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.base.BaseActivity
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.concurrent.TimeUnit


class PlayWithFriendActivity : BaseActivity() {
   private lateinit var binding: ActivityPlayWithFriendBinding
    var mCountDownTimer: CountDownTimer? = null

    override fun viewRelatedTask() {
        initProgressbar()
        binding.linearLayout20.setOnClickListener(this)
    }

    private fun initProgressbar() {
        var i = 0
        binding.progress.setProgress(i)
        mCountDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("Log_tag", "Tick of Progress$i$millisUntilFinished")
                binding.textView28.text = String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(
                                                millisUntilFinished
                                        )
                                )
                )
                i++
                binding.progress.setProgress(i as Int * 100 / (10000 / 1000))
            }

            override fun onFinish() {
                //Do what you want
                i++
                binding.textView28.text = "00:00"
                binding.progress.setProgress(100)
                binding.textView28.visibility = View.GONE
                binding.progress.visibility = View.GONE
                binding.textView20.visibility = View.GONE
                binding.textView29.visibility = View.VISIBLE
                binding.linearLayout20.visibility = View.VISIBLE
            }
        }
        (mCountDownTimer as CountDownTimer).start()
    }

    override fun navigateToHome() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_play_with_friend)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_play_with_friend)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {
        when(v){
            binding.linearLayout20->{
                binding.linearLayout20.visibility = View.GONE
                binding.textView29.visibility = View.GONE
                binding.questionsViewpager.visibility = View.VISIBLE
            }
        }
    }
}