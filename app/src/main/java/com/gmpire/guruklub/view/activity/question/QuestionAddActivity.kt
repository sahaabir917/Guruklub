package com.gmpire.guruklub.view.activity.question

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ActivityQuestionAddBinding
import com.gmpire.guruklub.util.GeneratDropdownItem
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.adapter.CustomAutoCompleteAdapter
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_question_add.*
import kotlinx.android.synthetic.main.activity_question_search.*
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.util.ArrayList
import androidx.core.view.marginStart as marginStart1

class QuestionAddActivity : BaseActivity() {

    private var isBlur: Boolean = false
    private lateinit var binding: ActivityQuestionAddBinding
    private lateinit var viewModel: QuestionViewModel
    lateinit var spinnerAdapterSubject: CustomSpinnerAdapter
    lateinit var spinnerAdapterSection: CustomSpinnerAdapter
    lateinit var spinnerAdapterTopic: CustomSpinnerAdapter
    lateinit var spinnerAdapterDifficulty: CustomSpinnerAdapter
    lateinit var autocompleteAdapter: CustomAutoCompleteAdapter
    private var isGame: String = ""

    val filterValues = FilterValues()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_question_add)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(QuestionViewModel::class.java)
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "", true, false)

        if (intent.hasExtra("isGame")) {
            isGame = intent.getStringExtra("isGame")
            Log.d("isGame", isGame)
            if (isGame == "yes") {
                binding.rootLayout.setBackgroundResource(R.drawable.page_level_background)
                binding.titlebarLayout.visibility = View.GONE
                binding.btnBack.setBackgroundResource(R.drawable.yellow_box_with_colorfull_bg)
                binding.btnNext.setBackgroundResource(R.drawable.yellow_box_with_colorfull_bg)
                binding.btnBack.layoutParams.width = 0
                binding.btnBack.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.btnNext.layoutParams.width = 0
                binding.btnNext.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.toolbarlayout.visibility = View.GONE
                val param = binding.questionAddLayout.layoutParams as ViewGroup.MarginLayoutParams
                param.topMargin = 200
                binding.questionAddLayout.layoutParams = param
                binding.titleLayout2.visibility = View.VISIBLE
            }
        }

        viewModel.apiGetSubjectByCategory(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )

        val item = ArrayList<Common>()
        item.add(Common("1", "Basic"))
        item.add(Common("2", "Intermediate"))
        item.add(Common("3", "Advanced"))

        initAutoCompleteQuestionTitle()
        initMakeChoiceItemSpinner(binding.spDifficulty, "Select Difficulty", item)

        filterValues.category_id = dataManager.mPref.prefGetUserInfo().category_id.toString()
        filterValues.difficulty_id = "1"
        binding.btnNext.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.root.setOnClickListener(this)
        binding.spSubjectlayout.setOnClickListener {
            binding.spSubject.performClick()
        }
        binding.spSectionlayout.setOnClickListener {
            binding.spSection.performClick()
        }
        binding.spTopicLayout.setOnClickListener {
            binding.spTopic.performClick()
        }
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun initAutoCompleteQuestionTitle() {
        autocompleteAdapter =
            CustomAutoCompleteAdapter(
                this,
                android.R.layout.select_dialog_item,
                mutableListOf(),
                viewModel.dataManager
            )
        actvQuestionTitle.setAdapter(
            autocompleteAdapter
        )
        actvQuestionTitle.setLoadingIndicator(binding.pbQuestionAdd)
        actvQuestionTitle.threshold = 3
        actvQuestionTitle.setOnItemClickListener { adapterView, view, i, l ->
            showDuplicateWarningDialog()
        }

        actvQuestionTitle.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboards(v)
            }
        }

    }

    private fun hideKeyboards(view: View?) {
        val inputMethodManager: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showDuplicateWarningDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.duplicate_ques_warning))
            .setCancelable(true)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun initMakeChoiceItemSpinner(sp: View, hint: String, item: ArrayList<Common>?) {
        sp as AppCompatSpinner
        var sa = CustomSpinnerAdapter(
            this,
            GeneratDropdownItem.getDropdownItems(
                JSONArray(Gson().toJson(item)),
                "name",
                "id",
                hint
            )
        )
        if (hint == "Select Subject") {
            isBlur = true
        }
        sp.adapter = sa.getAdapter(isBlur)
        sp.setOnTouchListener { v, event ->
            hideKeyboard()
            false
        }
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                sa.setPosition(position)
                when (sp) {
                    binding.spSubject -> {
                        viewModel.apiGetSectionBySubject(
                            spinnerAdapterSubject.getSelectedId(),
                            this@QuestionAddActivity
                        )
                        filterValues.subject_id = spinnerAdapterSubject.getSelectedId()
                        if (filterValues.subject_id != "0") {
                            binding.dropdownIcon2.setImageResource(R.drawable.ic_down_arrow_black_png)
                            isBlur = true
                        } else {
                            binding.dropdownIcon2.setImageResource(R.drawable.blur_down_arrow)
                            isBlur = false
                        }
                    }
                    binding.spSection -> {
                        viewModel.apiGetTopicBySection(
                            spinnerAdapterSection.getSelectedId(),
                            this@QuestionAddActivity
                        )
                        filterValues.section_id = spinnerAdapterSection.getSelectedId()
                        if (filterValues.section_id == "0") {
                            binding.dropdownIcon3.setImageResource(R.drawable.blur_down_arrow)
                            isBlur = false
                        } else {
                            binding.dropdownIcon3.setImageResource(R.drawable.ic_down_arrow_black_png)
                            isBlur = true
                        }
                    }
                    binding.spTopic -> {
                        filterValues.topic_id = spinnerAdapterTopic.getSelectedId()
                    }
                    binding.spDifficulty -> {
                        // filterValues.difficulty_id = spinnerAdapterDifficulty.getSelectedId()
                    }

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        when (sp) {
            binding.spSubject -> spinnerAdapterSubject = sa
            binding.spSection -> spinnerAdapterSection = sa
            binding.spTopic -> spinnerAdapterTopic = sa
            binding.spDifficulty -> spinnerAdapterDifficulty = sa
        }

    }

    override fun onLoading(isLoader: Boolean, key: String) {
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetSubjectByCategory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            initMakeChoiceItemSpinner(
                                binding.spSubject,
                                "Select Subject",
                                baseData.data
                            )
                        }
                    }
                }
            }
            "apiGetSectionBySubject" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            initMakeChoiceItemSpinner(
                                binding.spSection,
                                "Select Section",
                                baseData.data
                            )
                        }
                    }
                }
            }
            "apiGetTopicBySection" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            initMakeChoiceItemSpinner(
                                binding.spTopic,
                                "Select Topic",
                                baseData.data
                            )
                        }
                    }
                }
            }
            "getQuestionByKeyword" -> {
                val type =
                    object : TypeToken<BaseModel<PaginationModel<ArrayList<Question>>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<PaginationModel<ArrayList<Question>>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200 && baseData.data?.data != null) {
                        val questionList = baseData.data?.data
                        if (questionList?.size ?: 0 > 0) {
                            val checkIfDuplicate =
                                questionList?.any { it.title == actvQuestionTitle.text.toString() }
                            if (checkIfDuplicate == true)
                                showDuplicateWarningDialog()
                            else {
                                hideProgressDialog()
                                goToNextActivity()
                            }
                        } else {
                            hideProgressDialog()
                            goToNextActivity()
                        }
                    }
                    hideProgressDialog()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnNext -> {
                if (filterValues.topic_id == "0") {
                    showToast(this, "Choose a topic to continue")
                    return
                }
                val questionTile = actvQuestionTitle.text.toString()
                if (questionTile.isNotEmpty()) {
                    viewModel.getQuestionByKeyword(
                        dataManager.mPref.prefGetUserInfo().id,
                        questionTile,
                        this
                    )
                    showProgressDialog("Loading")
                } else
                    showToast(this, "Add a title to the question.")
            }

            binding.btnBack -> {
                onBackPressed()
            }
            binding.root -> {
                hideKeyboard()
            }
        }
    }

    private fun goToNextActivity() {
        val intent = Intent(this, QuestionAddNextActivity::class.java)
        intent.putExtra("question_title", actvQuestionTitle.text.toString())
        intent.putExtra("filter", Gson().toJson(filterValues))
        intent.putExtra("isGame",isGame)
        startActivity(intent)
    }
}
