package com.gmpire.guruklub.view.activity.infoCenter

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.infocenter.NewsResponse
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.databinding.ActivityInfoCenterBinding
import com.gmpire.guruklub.util.GeneratDropdownItem
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.TabsPagerAdapter
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.fragment.news.NewsFragment
import com.gmpire.guruklub.view.fragment.populer.PopularFragment
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class InfoCenterActivity : BaseActivity() {

    private var currenttab: Int = 0
    private lateinit var binding: ActivityInfoCenterBinding
    private lateinit var viewmodel: InfoCenterViewModel
    lateinit var spinnerAdapterCategory: CustomSpinnerAdapter
    var newsResponseLiveData: MutableLiveData<NewsResponse> = MutableLiveData()
    var popularNewsResponseLiveData: MutableLiveData<NewsResponse> = MutableLiveData()
    var filterValues = FilterValues()
    private var selectedDateTo = ""
    private var selectedDateFrom = ""
    private var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_info_center)
        viewmodel =
            ViewModelProviders.of(this, viewModelFactory).get(InfoCenterViewModel::class.java)

        filterValues.category_id = ""

        viewmodel.apiGetNews("1", "", "", "", "", this)

        viewmodel.apiGetNewsCategory(this)

        binding.makeChoiceLayout.setOnClickListener(this)
        binding.btnMakeChoice.setOnClickListener(this)
        binding.dateLayout.setOnClickListener(this)
        binding.dateLayout2.setOnClickListener(this)

        binding.llMakeChoice.visibility = View.GONE
        binding.ivDropdownArrow.rotation = 360.0.toFloat()

    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "InfoCentre", true)
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun initTablayout(
        newsResponseLiveData: MutableLiveData<NewsResponse>,
        popularNewsResponseLiveData: MutableLiveData<NewsResponse>,
        batch: ArrayList<Common>
    ) {
        if (binding.viewPager.adapter != null) {
            newsResponseLiveData.postValue(newsResponseLiveData.value)
            popularNewsResponseLiveData.postValue(popularNewsResponseLiveData.value)
        } else {
            binding.tabLayout.tabMode = TabLayout.MODE_FIXED
            binding.viewPager.adapter = TabsPagerAdapter(supportFragmentManager)
            /*    .addFragment(
                    NewsFragment(newsResponseLiveData), "News"
                )
                .addFragment(
                    PopularFragment(popularNewsResponseLiveData), "Popular"
                )*/
//                .addFragment(
//                    PreviousQuestionBatchFragment(batch), "Previous Question"
//                )
            binding.tabLayout.setupWithViewPager(binding.viewPager)
        }

        binding.viewPager.adapter?.notifyDataSetChanged()

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currenttab = position
                if (position == 2) {
                    binding.makeChoiceLayout.visibility = View.GONE
                } else {
                    binding.makeChoiceLayout.visibility = View.VISIBLE
                }
            }

        })
        binding.viewPager.currentItem = currenttab


        binding.makeChoiceLayout.visibility = View.VISIBLE
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetNews" -> {
                val type = object : TypeToken<BaseModel<NewsResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<NewsResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200 && baseData.data != null) {
                        newsResponseLiveData.value = baseData.data
                        viewmodel.apiGePopularNews(
                            "1",
                            filterValues.category_id.toString(),
                            binding.searchEt.text.toString(),
                            selectedDateFrom,
                            selectedDateTo,
                            this
                        )
                    } else {
                        showToast(this, "No data found!!")
                        finish()
                    }
                }
            }
            "apiGePopularNews" -> {
                val type = object : TypeToken<BaseModel<NewsResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<NewsResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200 && baseData.data != null) {
                        popularNewsResponseLiveData.value = baseData.data
                        viewmodel.apiGetBatchByCategory(
                            dataManager.mPref.prefGetUserInfo().category_id.toString(),
                            this
                        )
                    } else {
                        showToast(this, "No data found!!")
                        finish()
                    }
                }
            }
            "apiGetNewsCategory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let { it1 ->
                            initCategorySpinner(
                                binding.spCategory, "Select Category",
                                it1
                            )
                        }
                    } else {
                        showToast(this, "No data found!!")
                        finish()
                    }
                }
            }
            "apiGetBatchByCategory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            baseData.data?.let {
                                initTablayout(
                                    newsResponseLiveData,
                                    popularNewsResponseLiveData,
                                    baseData.data ?: arrayListOf()
                                )
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    fun initCategorySpinner(sp: View, hint: String, item: ArrayList<Common>) {
        sp as AppCompatSpinner

        val sa: CustomSpinnerAdapter = CustomSpinnerAdapter(
            this,
            GeneratDropdownItem.getDropdownItems(
                JSONArray(Gson().toJson(item)),
                "name",
                "id",
                hint
            )
        )
        sp.adapter = sa.getAdapter()
        sp.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                sa.setPosition(position)
                when (sp) {
                    binding.spCategory -> {
                        if (position > 0)
                            filterValues.category_id = spinnerAdapterCategory.getSelectedId()
                        else
                            filterValues.category_id = ""
                    }

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        })

        when (sp) {
            binding.spCategory -> spinnerAdapterCategory = sa
        }

    }

    override fun onLoading(isLoader: Boolean, key: String) {
        when (key) {
            "apiGetNews" -> {
                if (isLoader)
                    showProgressDialog("Please Wait")
                else
                    hideProgressDialog()
            }
            "apiGePopularNews" -> {
                if (isLoader)
                    showProgressDialog("Please Wait")
                else
                    hideProgressDialog()
            }
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.btnMakeChoice -> {
                if ((selectedDateFrom.isEmpty() && selectedDateTo.isNotEmpty()) || (selectedDateFrom.isNotEmpty() && selectedDateTo.isEmpty())) {
                    showToast(this, "Please select both date ranges.")
                    return
                } else if (selectedDateFrom.isEmpty() && selectedDateTo.isEmpty()) {
                    // do nothing
                } else {
                    val format = SimpleDateFormat("yyyy-MM-dd")
                    val dateFrom = format.parse(selectedDateFrom)
                    val dateTo = format.parse(selectedDateTo)

                    if (dateFrom.after(dateTo)) {
                        showToast(this, "Start date can not be larger than end date")
                        return
                    }
                }

                viewmodel.apiGetNews(
                    "1",
                    filterValues.category_id.toString(),
                    binding.searchEt.text.toString(),
                    selectedDateFrom,
                    selectedDateTo,
                    this
                )
                binding.llMakeChoice.visibility = View.GONE
                binding.ivDropdownArrow.rotation = 360.0.toFloat()
            }

            binding.makeChoiceLayout -> {
                if (binding.llMakeChoice.isVisible) {
                    binding.llMakeChoice.visibility = View.GONE
                    binding.ivDropdownArrow.rotation = 360.0.toFloat()
                } else {
                    binding.llMakeChoice.visibility = View.VISIBLE
                    binding.ivDropdownArrow.rotation = 180.0.toFloat()
                }
            }

            binding.dateLayout -> {
                val c = Calendar.getInstance()
                if (selectedDateFrom.isNotEmpty()) {
                    c.time = simpleDateFormat.parse(selectedDateFrom)
                }

                val y = c.get(Calendar.YEAR)
                val m = c.get(Calendar.MONTH)
                val d = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    this, android.app.AlertDialog.THEME_HOLO_LIGHT,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, monthOfYear, dayOfMonth)
                        selectedDateFrom =
                            simpleDateFormat.format(Date(cal.timeInMillis))
                        binding.dateTv.text = selectedDateFrom
                    },
                    y,
                    m,
                    d
                )
                dpd.show()
            }

            binding.dateLayout2 -> {
                val c = Calendar.getInstance()
                if (selectedDateTo.isNotEmpty()) {
                    c.time = simpleDateFormat.parse(selectedDateTo)
                }

                val y = c.get(Calendar.YEAR)
                val m = c.get(Calendar.MONTH)
                val d = c.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(
                    this, android.app.AlertDialog.THEME_HOLO_LIGHT,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        val cal = Calendar.getInstance()
                        cal.set(year, monthOfYear, dayOfMonth)
                        selectedDateTo =
                            simpleDateFormat.format(Date(cal.timeInMillis))
                        binding.dateTv2.text = selectedDateTo
                    },
                    y,
                    m,
                    d
                )
                dpd.show()
            }

        }
    }


}
