package com.gmpire.guruklub.view.activity.newUserFaq

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.toc.TermsOfConditionModel
import com.gmpire.guruklub.databinding.ActivityGameHelpBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.gameHelp.ContentViewModel
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response


class NewUserFaqActivity : BaseActivity() {

    private lateinit var viewModel: ContentViewModel
    private lateinit var binding: ActivityGameHelpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_user_faq)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(ContentViewModel::class.java)

  //      viewModel.apiGetFaq(this)
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Exam Rule", true)
        binding.okBtn.setOnClickListener(this)
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetGameRule" -> {
                val type = object : TypeToken<BaseModel<TermsOfConditionModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<TermsOfConditionModel>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                      //      binding.ruleTv.text = Html.fromHtml(baseData.data!!.description, Html.FROM_HTML_MODE_COMPACT);
                        } else {
                        //    binding.ruleTv.text = Html.fromHtml(baseData.data!!.description);
                        }
                    }
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when(v){
            binding.okBtn->{
              onBackPressed()
            }
        }
    }
}
