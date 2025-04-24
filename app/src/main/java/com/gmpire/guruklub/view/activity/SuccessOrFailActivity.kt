package com.gmpire.guruklub.view.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.ActivitySuccessOrFailBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.question.QuestionAddActivity
import com.gmpire.guruklub.view.base.BaseActivity
import okhttp3.ResponseBody
import retrofit2.Response

class SuccessOrFailActivity : BaseActivity() {
    private var isGame: String = ""
    private lateinit var binding: ActivitySuccessOrFailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_success_or_fail)
    }

    override fun viewRelatedTask() {

        setToolbar(this, binding.toolbar, "", true, false)

        if (intent.getBooleanExtra("isSuccess", false)) {
            binding.ivSuccessOrFail.setImageResource(R.drawable.success_ic2)
            binding.btnAddQuestion.text = "Add More Questions"
            binding.viewTitle.text = "Added Successfully"
        } else {
            binding.ivSuccessOrFail.setImageResource(R.drawable.fail_ic)
            binding.btnAddQuestion.text = "Try Again"
            binding.viewTitle.text = "Failed"
        }

        if (intent.hasExtra("isGame")) {
            isGame = intent.getStringExtra("isGame")
            if(isGame == "yes"){
                binding.titleLayout.visibility = View.GONE
                binding.toolbar.drawerNavigationIcon.visibility = View.GONE
                binding.appbtnLayout.visibility = View.GONE
                binding.gamebtnLayout.visibility = View.VISIBLE
                binding.btnAddQuestion.layoutParams.height = 300
                binding.btnBack.layoutParams.height = 300
                binding.rootLayout.setBackgroundResource(R.drawable.question_success_background)
                binding.btnAddQuestion.setBackgroundResource(R.drawable.yellow_box_with_colorfull_bg)
                binding.btnBack.setBackgroundResource(R.drawable.yellow_box_with_colorfull_bg)
                binding.rlContent.setBackgroundColor(Color.parseColor("#350e57"))
                val param = binding.btnBack.layoutParams as ViewGroup.MarginLayoutParams
                param.topMargin = 0
                binding.btnBack.layoutParams = param
                binding.tvTitle.setTextColor(Color.parseColor("#ffffff"))
                binding.tvDescription.setTextColor(Color.parseColor("#ffffff"))
            }
        }

        binding.tvTitle.text = intent.getStringExtra("title")
        binding.tvDescription.text = intent.getStringExtra("description")

        binding.btnAddQuestion.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnAddQuestion1.setOnClickListener(this)
        binding.btnBack1.setOnClickListener(this)

    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
    }

    override fun onClick(v: View?) {

        when (v) {
            binding.btnAddQuestion -> {
                if (intent.getBooleanExtra("isSuccess", false)) {
                    startActivity(Intent(this, QuestionAddActivity::class.java).putExtra("isGame",isGame))
                    finishAffinity()
                } else {
                    onBackPressed()
                }
            }

            binding.btnBack -> {
                val isNavigateFromGame = dataManager.mPref.prefIsNavigateFromGame()
                if (isNavigateFromGame) {
                    startActivity(Intent(this, GameLevelActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finishAffinity()
            }

            binding.btnAddQuestion1 -> {
                if (intent.getBooleanExtra("isSuccess", false)) {
                    startActivity(Intent(this, QuestionAddActivity::class.java).putExtra("isGame",isGame))
                    finishAffinity()
                } else {
                    onBackPressed()
                }
            }

            binding.btnBack1 -> {
                val isNavigateFromGame = dataManager.mPref.prefIsNavigateFromGame()
                if (isNavigateFromGame) {
                    startActivity(Intent(this, GameLevelActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finishAffinity()
            }

        }
    }
}
