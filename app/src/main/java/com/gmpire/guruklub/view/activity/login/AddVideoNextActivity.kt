package com.gmpire.guruklub.view.activity.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.databinding.ActivityAddVideoBinding
import com.gmpire.guruklub.databinding.ActivityAddVideoNextBinding
import com.gmpire.guruklub.util.GeneratDropdownItem
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.question.QuestionViewModel
import com.gmpire.guruklub.view.adapter.CustomAutoCompleteAdapter
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.util.ArrayList

class AddVideoNextActivity : BaseActivity() {
    private var isBlur: Boolean = false
    private val category_id: String = "1"
    private lateinit var binding: ActivityAddVideoNextBinding
    private lateinit var viewModel: QuestionViewModel
    lateinit var spinnerAdapterSubject: CustomSpinnerAdapter
    lateinit var spinnerAdapterSection: CustomSpinnerAdapter
    lateinit var spinnerAdapterTopic: CustomSpinnerAdapter
    lateinit var spinnerAdapterDifficulty: CustomSpinnerAdapter
    var videoTitle : String = ""
    var videoUrl : String = ""
    val filterValues = FilterValues()

    override fun viewRelatedTask() {

        setToolbar(this,binding.toolbar,"Add Videos",true)

        viewModel.apiGetSubjectByCategory(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )

        videoTitle = intent.extras?.get("videoTitle").toString()
        videoUrl = intent.extras?.get("videoUrl").toString()

        val item = ArrayList<Common>()
        item.add(Common("1", "Basic"))
        item.add(Common("2", "Intermediate"))
        item.add(Common("3", "Advanced"))

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
        if(hint == "Select Subject"){
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
                            this@AddVideoNextActivity
                        )
                        filterValues.subject_id = spinnerAdapterSubject.getSelectedId()
                        if(filterValues.subject_id!="0"){
                            binding.dropdownIcon2.setImageResource(R.drawable.ic_down_arrow_black_png)
                            isBlur = true
                        }
                        else{
                            binding.dropdownIcon2.setImageResource(R.drawable.blur_down_arrow)
                            isBlur = false
                        }
                    }
                    binding.spSection -> {
                        viewModel.apiGetTopicBySection(
                            spinnerAdapterSection.getSelectedId(),
                            this@AddVideoNextActivity
                        )
                        filterValues.section_id = spinnerAdapterSection.getSelectedId()
                        if(filterValues.section_id == "0"){
                            binding.dropdownIcon3.setImageResource(R.drawable.blur_down_arrow)
                            isBlur = false
                        }
                        else{
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

    override fun navigateToHome() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_video_next)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(QuestionViewModel::class.java)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
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
            "apiAddVideos" -> {
                val type = object : TypeToken<BaseModel<ArrayList<EmptyModel>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<EmptyModel>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        val intent = Intent(this, AddVideoSuccessFull::class.java)
                        intent.putExtra("Uploaded","successfull" )
                        this.startActivity(intent)
                    }
                    else{
                        val intent = Intent(this,AddVideoSuccessFull::class.java)
                        intent.putExtra("Uploaded","unsuccessfull" )
                        this.startActivity(intent)
                    }
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when(v){
            binding.btnBack ->{
                onBackPressed()
            }
            binding.btnNext ->{
                viewModel.apiAddVideos(category_id,filterValues.topic_id,videoTitle,videoUrl,this)
            }
        }
    }
}