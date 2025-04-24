package com.gmpire.guruklub.view.fragment.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.categoryAndSubject.Category
import com.gmpire.guruklub.data.model.categoryAndSubject.Subject
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.databinding.FragmentSettingsBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.resetPassword.ResetPasswordActivity
import com.gmpire.guruklub.view.activity.settings.SettingsViewModel
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.dialog.DialogMultiselection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception


class SettingsFragment : BaseFragment(), DialogMultiselection.OnDoneBtnClickListener {

    private var selectedItemIds: String = ""
    private var dialogMultiselection: DialogMultiselection? = null
    private var categorieNames = ArrayList<String>()
    private var categories = ArrayList<Category>()
    private var subjects = ArrayList<Subject>()
    private var selectedCategoryPosition: Int = 0
    private var first: Boolean = true

    private lateinit var userInfo: UserInfo

    private lateinit var viewModel: SettingsViewModel
    private lateinit var binding: FragmentSettingsBinding


    private var isNotificationOn: Int = 1

    override fun viewRelatedTask() {

        binding.notificationCv.setOnClickListener(this)

        userInfo = dataManager.mPref.prefGetUserInfo()
        isNotificationOn = userInfo.notification

        if (isNotificationOn == 1) {
            setNotificationOn()
        } else {
            setNotificationOff()
        }

        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel::class.java)

        viewModel.apiGetCategoryList(this)

        binding.selectSubjectLayout.isEnabled = false

        binding.selectSubjectLayout.setOnClickListener {
            showSelectionDialog()
        }

        binding.btnSave.setOnClickListener(this)
        binding.updatePassLayout.setOnClickListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        return binding.root
    }

    private fun showSelectionDialog() {
        dialogMultiselection =
            activity?.let {
                DialogMultiselection(
                    it,
                    "Select Subjects",
                    subjects as ArrayList<BaseItem>,
                    this,
                    ""
                )
            }
        dialogMultiselection?.show()
    }

    private fun setCategorySpinner() {
        categorieNames.clear()
        categorieNames.add("Select Category")

        categories.forEach {
            categorieNames.add(it.name.toString())
        }

        val adapter =
            activity?.let {
                ArrayAdapter<String>(
                    it,
                    android.R.layout.simple_dropdown_item_1line,
                    categorieNames
                )
            }
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
                            this@SettingsFragment
                        )
                    } else {
                        binding.selectSubjectLayout.isEnabled = false
                    }
                    selectedCategoryPosition = p2
                }

            }

        try {
            var pos = 0
            categories.forEach {
                if (it.id == userInfo.category_id) {
                    pos = categories.indexOf(it) + 1
                }
            }
            binding.categorySpinner.setSelection(pos)
        } catch (e: Exception) {

        }

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetCategoryList" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Category>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Category>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            categories = baseData.data ?: arrayListOf()
                            setCategorySpinner()
                        }
                    } else {
                        showToast(activity, baseData.message[0])
                    }
                }

            }

            "apiGetSubjectListByCategory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Subject>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Subject>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        binding.selectSubjectTextTv.visibility = View.VISIBLE
                        binding.selectSubjectLayout.visibility = View.VISIBLE

                        subjects = baseData.data ?: arrayListOf()
                        if (subjects.size > 0) {
                            binding.selectSubjectTextTv.visibility = View.VISIBLE
                            binding.selectSubjectLayout.visibility = View.VISIBLE
                            binding.selectSubjectLayout.isEnabled = true
                            if (first) {
                                var sub_ids = userInfo.subject_id?.split(",")
                                if (sub_ids?.size ?: 0 > 0) {
                                    binding.subjectNameTv.text = ""
                                    subjects.forEach { sub ->
                                        sub_ids?.forEach {
                                            if (sub.id == it) {
                                                binding.subjectNameTv.append(sub.name + ", ")
                                            }
                                        }
                                    }
                                    binding.subjectNameTv.text =
                                        binding.subjectNameTv.text.substring(
                                            0,
                                            binding.subjectNameTv.text.length - 2
                                        )
                                }

                                first = false
                            }
                        } else {
                            binding.selectSubjectTextTv.visibility = View.GONE
                            binding.selectSubjectLayout.visibility = View.GONE
                            binding.selectSubjectLayout.isEnabled = false
                            binding.subjectNameTv.text = "No subjects found for this category"
                        }


                    } else {
                        showToast(activity, baseData.message[0])
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
                            if (userInfo?.category_id.isNullOrEmpty()) {
                                userInfo?.category_id = "1"
                            }
                            dataManager.mPref.prefSetUserInfo(userInfo)
                            showToast(activity, "Setting saved")
                            startActivity(Intent(activity, MainActivity::class.java))
                            //finishAffinity()
                        }
                    } else {
                        showToast(activity, baseData.message[0])
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.notificationCv -> {
                if (isNotificationOn == 1) {
                    setNotificationOff()
                    isNotificationOn = 0
                } else {
                    setNotificationOn()
                    isNotificationOn = 1
                }
            }
            binding.btnSave -> {

                if (selectedCategoryPosition == 0) {
                    viewModel.apiSetCategoryAndSubject(
                        userInfo.category_id.toString(),
                        userInfo.subject_id.toString(),
                        userInfo.notification.toString(),
                        this
                    )
                } else {
                    viewModel.apiSetCategoryAndSubject(
                        categories[selectedCategoryPosition - 1].id.toString(),
                        selectedItemIds,
                        isNotificationOn.toString(),
                        this
                    )
                }
            }
            binding.updatePassLayout -> {
                startActivity(
                    Intent(
                        activity,
                        ResetPasswordActivity::class.java
                    ).putExtra("from_activity", "settings").putExtra("email", userInfo.email)
                )
            }
        }
    }

    private fun setNotificationOff() {
        binding.tvTextOn.background = null
        binding.tvTextOff.background =
            activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_outline_off) }
        activity?.let { ContextCompat.getColor(it, R.color.white) }?.let {
            binding.tvTextOff.setTextColor(
                it
            )
        }
        activity?.let {
            ContextCompat.getColor(
                it,
                R.color.colorPrimary
            )
        }?.let {
            binding.tvTextOn.setTextColor(
                it
            )
        }
    }

    private fun setNotificationOn() {
        binding.tvTextOff.background = null
        binding.tvTextOn.background =
            activity?.let { ContextCompat.getDrawable(it, R.drawable.bg_outline_on) }
        activity?.let { ContextCompat.getColor(it, R.color.white) }?.let {
            binding.tvTextOn.setTextColor(
                it
            )
        }
        activity?.let {
            ContextCompat.getColor(
                it,
                R.color.colorPrimary
            )
        }?.let {
            binding.tvTextOff.setTextColor(
                it
            )
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}