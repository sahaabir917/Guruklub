package com.gmpire.guruklub.view.activity.gameHelp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.toc.TermsOfConditionModel
import com.gmpire.guruklub.databinding.ActivityGameHelpBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response


class ContentActivity : BaseActivity() {

    companion object {
        val TITLE_ABOUT = "About"
        val TITLE_GAME_RULES = "Exam Rules"
        val TITLE_FAQ = "FAQ"
        val TITLE_SYLLABUS = "Details"
        val ACTIVITY_TITLE = "activity_title"
    }

    private lateinit var viewModel: ContentViewModel
    private lateinit var binding: ActivityGameHelpBinding
    private lateinit var activity_title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game_help)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(ContentViewModel::class.java)

        activity_title = intent.getStringExtra(ACTIVITY_TITLE)
        setToolbar(this, binding.toolbar, activity_title, true)

    }

    override fun viewRelatedTask() {
        when (activity_title) {
            TITLE_ABOUT -> {
                binding.tvTitle.text = "About Us"
                viewModel.apiGetContent("about", this)
            }
            TITLE_GAME_RULES -> {
                binding.tvTitle.text = "Exam Rules"
                viewModel.apiGetContent("game_rules", this)
            }
            TITLE_FAQ -> {
                binding.tvTitle.text = "Frequently Asked Question (FAQ)"
                viewModel.apiGetContent("faq", this)
            }
            TITLE_SYLLABUS -> {
                binding.tvTitle.text = "Live Event Details"
                showSyllabusContent(intent.getStringExtra("syllabus"))
            }

        }
        binding.okBtn.setOnClickListener(this)
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun showSyllabusContent(syllabus: String) {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.tvContent.text = Html.fromHtml(
                syllabus,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            binding.tvContent.text = Html.fromHtml(syllabus);
        }
*/

        var text = "<html><body style=\"text-align:justify\">"
        text += syllabus
        text += "</body></html>"
        val settings = binding.webContent.settings
        settings.defaultTextEncodingName = "utf-8"
        binding.webContent.loadData(
            text,
            "text/html; charset=utf-8",
            "utf-8"
        )

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetContent" -> {
                val type = object : TypeToken<BaseModel<TermsOfConditionModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<TermsOfConditionModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        var text = "<html><body style=\"text-align:justify\">"
                        text += baseData.data?.description
                        text += "</body></html>"
                        val settings = binding.webContent.settings
                        settings.defaultTextEncodingName = "utf-8"
                        binding.webContent.loadData(
                            text,
                            "text/html; charset=utf-8",
                            "utf-8"
                        )
                    }
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.okBtn -> {
                onBackPressed()
            }
        }
    }
}
