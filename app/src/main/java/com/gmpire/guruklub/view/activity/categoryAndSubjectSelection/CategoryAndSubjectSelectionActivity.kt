package com.gmpire.guruklub.view.activity.categoryAndSubjectSelection

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.categoryAndSubject.Category
import com.gmpire.guruklub.data.model.categoryAndSubject.Subject
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.ActivityCategorySubjectSelectionBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.dialog.DialogMultiselection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response

class CategoryAndSubjectSelectionActivity : BaseActivity(),
    DialogMultiselection.OnDoneBtnClickListener {

    private var selectedItemIds: String = ""
    private lateinit var dialogMultiselection: DialogMultiselection
    private lateinit var viewModel: CategoryAndSubjectSelectionViewModel
    private lateinit var binding: ActivityCategorySubjectSelectionBinding
    private var categorieNames = ArrayList<String>()

    private var categories = ArrayList<Category>()
    private var subjects = ArrayList<Subject>()
    private var selectedSubjects = ArrayList<Subject>()

    private var subjectsNames = ArrayList<String>()
    private var selectedCategoryPosition: Int = 0
    private var selectedSubjectPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_category_subject_selection)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(CategoryAndSubjectSelectionViewModel::class.java)

        viewModel.apiGetCategoryList(this)

    }

    override fun viewRelatedTask() {

        setToolbar(this, binding.toolbar, "Category and Subject", true)
        binding.submitBtn.setOnClickListener(this)

        binding.selectSubjectLayout.isEnabled = false

        binding.selectSubjectLayout.setOnClickListener {
            showSelectionDialog()
        }

    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetCategoryList" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Category>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Category>>>(
                            result.data.body()?.string(), type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            categories = baseData.data ?: arrayListOf()
                            setCategorySpinner()
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }

            }

            "apiGetSubjectListByCategory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Subject>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Subject>>>(
                            result.data.body()?.string(), type
                        )
                    if (baseData.status_code == 200) {

                        binding.selectSubjectTextTv.visibility = View.VISIBLE
                        binding.selectSubjectLayout.visibility = View.VISIBLE

                        subjects = baseData.data ?: arrayListOf()
                        if (subjects.size > 0) {
                            binding.selectSubjectTextTv.visibility = View.VISIBLE
                            binding.selectSubjectLayout.visibility = View.VISIBLE
                            binding.selectSubjectLayout.isEnabled = true
                        } else {
                            binding.selectSubjectTextTv.visibility = View.GONE
                            binding.selectSubjectLayout.visibility = View.GONE
                            binding.selectSubjectLayout.isEnabled = false
                            binding.subjectNameTv.text = "No subjects found for this category"
                        }

                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }

            "apiSetCategoryAndSubject" -> {
                val type = object : TypeToken<BaseModel<UserInfo>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<UserInfo>>(result.data.body()?.string(), type)
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            val userInfo = baseData.data
                            dataManager.mPref.prefSetUserInfo(userInfo)
                            dataManager.mPref.prefLogin()
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity()
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }
    }


    private fun setCategorySpinner() {
        categorieNames.clear()
        categorieNames.add("Select Category")

        categories.forEach {
            categorieNames.add(it.name.toString())
        }

        val adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, categorieNames)
        binding.categorySpinner.adapter = adapter

        binding.categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    binding.categoryNameTv.text = categorieNames[p2]

                    selectedItemIds = ""
                    binding.subjectNameTv.text = "Select Subject"

                    if (p2 != 0) {
                        viewModel.apiGetSubjectListByCategory(
                            categories[p2 - 1].id.toString(),
                            this@CategoryAndSubjectSelectionActivity
                        )
                    } else {
                        binding.selectSubjectLayout.isEnabled = false
                    }
                    selectedCategoryPosition = p2
                }

            }

    }


    private fun showSelectionDialog() {
        dialogMultiselection =
            DialogMultiselection(this, "Select Subjects", subjects as ArrayList<BaseItem>, this,"")
        dialogMultiselection.show()
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.submitBtn -> {
                if (selectedCategoryPosition == 0) {
                    showToast(this, "You need to select a category to continue")
                    return
                }
                viewModel.apiSetCategoryAndSubject(
                    categories[selectedCategoryPosition - 1].id.toString(),
                    selectedItemIds,
                    "1",
                    this
                )

            }
        }
    }

    override fun onDoneBtnClick(selectedItemIds: ArrayList<BaseItem>, title: String) {
        var atleastOneSelected = false
        this.selectedItemIds = ""
        binding.subjectNameTv.text = ""

        selectedItemIds.forEach {
            if (it.isSelected) {
                atleastOneSelected = true
                binding.subjectNameTv.append(it.name + ",")
                if (this.selectedItemIds == "") {
                    this.selectedItemIds = it.id.toString()
                } else {
                    this.selectedItemIds = this.selectedItemIds + "," + it.id
                }
            }
        }

        if (!atleastOneSelected) {
            binding.subjectNameTv.text = "Select Subject"
        }
    }
}
