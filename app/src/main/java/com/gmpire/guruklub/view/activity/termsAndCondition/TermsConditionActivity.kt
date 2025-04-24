package com.gmpire.guruklub.view.activity.termsAndCondition

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.toc.TermsOfConditionModel
import com.gmpire.guruklub.databinding.ActivityTermsConditionBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.Registration.ACTIVITY_NAME
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception


class TermsConditionActivity : BaseActivity() {

    private lateinit var viewModel: TermsAndConditionsViewModel
    private lateinit var binding: ActivityTermsConditionBinding
    private var callingActivityName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms_condition)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(TermsAndConditionsViewModel::class.java)

        try {
            if (intent.hasExtra(ACTIVITY_NAME)) {
                callingActivityName = intent.extras?.get(ACTIVITY_NAME) as String
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        if (callingActivityName == "content")
            binding.acceptBtn.visibility = View.GONE

        viewModel.apiGetTermsAndCondition(this)
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Terms & Conditions", true)
        binding.acceptBtn.setOnClickListener(this)
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetTermsAndCondition" -> {
                val type = object : TypeToken<BaseModel<TermsOfConditionModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<TermsOfConditionModel>>(
                            result.data.body()?.string(), type
                        )
                    if (baseData.status_code == 200) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            binding.tocTv.text = Html.fromHtml(
                                baseData.data?.description,
                                Html.FROM_HTML_MODE_COMPACT
                            );
                        } else {
                            binding.tocTv.text = Html.fromHtml(baseData.data?.description);
                        }
                        binding.tocTv.isClickable = true;
                        binding.tocTv.movementMethod = LinkMovementMethod.getInstance();
                    }
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.acceptBtn -> {
                val returnIntent = Intent()
                returnIntent.putExtra("toc", 1)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
    }
}
