package com.gmpire.guruklub.view.activity.howToGetCoin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.ActivityHowToGetCoinBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.question.QuestionAddActivity
import com.gmpire.guruklub.view.base.BaseActivity
import okhttp3.ResponseBody
import retrofit2.Response

class HowToGetCoinActivity : BaseActivity(), View.OnClickListener {


    private lateinit var binding: ActivityHowToGetCoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_how_to_get_coin)
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "How to get coin", true)
        binding.llAddQuestion.setOnClickListener(this)
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.llAddQuestion -> {
                startActivity(Intent(this, QuestionAddActivity::class.java))
            }
        }
    }
}
