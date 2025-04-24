package com.gmpire.guruklub.view.fragment.infoCentre

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProviders
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.databinding.FragmentInfoCentreBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.util.GeneratDropdownItem
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.infoCenter.InfoCenterViewModel
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.fragment.news.NewsFragment
import com.gmpire.guruklub.view.fragment.populer.PopularFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class InfoCentreFragment : BaseFragment(), NewsFragment.ProgressBarChildListener {

    public fun InfoCentreFragment() {}

    private lateinit var binding: FragmentInfoCentreBinding
    private lateinit var viewmodel: InfoCenterViewModel
    private lateinit var spinnerAdapterCategory: CustomSpinnerAdapter
    val newsFragment = NewsFragment()
    val popularFragment = PopularFragment()
    var isAllNews: Boolean = true
    var filterValues = FilterValues()
    private var selectedDateTo = ""
    private var selectedDateFrom = ""
    private var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

    override fun viewRelatedTask() {
        viewmodel =
            ViewModelProviders.of(this, viewModelFactory).get(InfoCenterViewModel::class.java)

        filterValues.category_id = ""

        initKeyboardHiding()

        viewmodel.apiGetNewsCategory(this)

        binding.linearlayout18.setOnClickListener(this)
        binding.btnMakeChoice.setOnClickListener(this)
        binding.btnClear.setOnClickListener(this)
        binding.dateLayout.setOnClickListener(this)
        binding.dateLayout2.setOnClickListener(this)
        binding.allNewsBtn.setOnClickListener(this)
        binding.popularNewsLayout.setOnClickListener(this)
        binding.showSectionlayout.setOnClickListener(this)
        binding.appBar.setOnClickListener(this)
        binding.scroll.setOnClickListener(this)
        binding.relativeLayout2.setOnClickListener {
            binding.spCategory.performClick()
        }
        binding.llMakeChoice.visibility = View.VISIBLE
    }

    private fun initAllNews() {
        binding.makeChoiceLayout.visibility = View.VISIBLE
        binding.spinnerLayout.visibility = View.VISIBLE
        binding.searchAndDatesLayout.visibility = View.GONE
        isAllNews = true
        var drawableTop = binding.allNewsBtn.background as GradientDrawable
        var color = ColorUtil.getColorForableordisable("infocenterbtnDisable")
        drawableTop.setColor(ContextCompat.getColor(requireActivity(), color))

        var drawableTop1 = binding.popularNewsLayout.background as GradientDrawable
        var color1 = ColorUtil.getColorForableordisable("infocenterbtnAble")
        drawableTop1.setColor(ContextCompat.getColor(requireActivity(), color1))
    }

    private fun initKeyboardHiding() {
        binding.searchEt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboards(v)
            }
        }
    }

    private fun hideKeyboards(view: View?) {
        val inputMethodManager: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info_centre, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view_info_centre, newsFragment)
                .setReorderingAllowed(true)
                .commit()
        }
        initAllNews()
    }


    fun initCategorySpinner(sp: View, hint: String, item: ArrayList<Common>) {
        sp as AppCompatSpinner
        val sa: CustomSpinnerAdapter = CustomSpinnerAdapter(
            requireActivity(),
            GeneratDropdownItem.getDropdownItems(
                JSONArray(Gson().toJson(item)),
                "name",
                "id",
                hint
            )
        )
        sp.adapter = sa.getAdapter()
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        }
        when (sp) {
            binding.spCategory -> spinnerAdapterCategory = sa
        }
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
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
                                binding.spCategory, "Select Your Choice",
                                it1
                            )
                        }
                    } else {
                        showToast(activity, "No data found!!")
                    }
                }
            }
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        when (key) {
            "apiGetNews" -> {
                if (isLoader)
                    showProgressDialog()
                else
                    hideProgressDialog()
            }
            "apiGePopularNews" -> {
                if (isLoader)
                    showProgressDialog()
                else
                    hideProgressDialog()
            }
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            binding.btnClear -> {
                selectedDateTo = ""
                selectedDateFrom = ""
                filterValues.category_id = ""
                binding.searchEt.setText("")

                binding.dateTv.text = "Date From"
                binding.dateTv2.text = "Date To"
                binding.spCategory.setSelection(0)
            }

            binding.btnMakeChoice -> {
                if ((selectedDateFrom.isEmpty() && selectedDateTo.isNotEmpty()) || (selectedDateFrom.isNotEmpty() && selectedDateTo.isEmpty())) {
                    showToast(activity, "Please select both date ranges.")
                    return
                } else if (selectedDateFrom.isEmpty() && selectedDateTo.isEmpty()) {
                    // do nothing
                } else {
                    val format = SimpleDateFormat("yyyy-MM-dd")
                    val dateFrom = format.parse(selectedDateFrom)
                    val dateTo = format.parse(selectedDateTo)

                    if (dateFrom.after(dateTo)) {
                        showToast(activity, "Start date can not be larger than end date")
                        return
                    }
                }

                // Send filter values to fragments
                if (isAllNews) {
                    newsFragment.updateFilterCategories(
                        filterValues.category_id,
                        binding.searchEt.text.toString(),
                        selectedDateFrom,
                        selectedDateTo
                    )
                } else if (!isAllNews) {
                    popularFragment.updateFilterCategories(
                        filterValues.category_id,
                        binding.searchEt.text.toString(),
                        selectedDateFrom,
                        selectedDateTo
                    )
                }

                binding.searchAndDatesLayout.visibility = View.GONE
                binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)

            }

            binding.linearlayout18 -> {
                if (binding.llMakeChoice.isVisible) {
                    binding.linearlayout18.visibility = View.VISIBLE
                    binding.llMakeChoice.visibility = View.GONE
                } else {
                    binding.linearlayout18.visibility = View.GONE
                    binding.llMakeChoice.visibility = View.VISIBLE
                }
            }

            binding.showSectionlayout -> {
                if (binding.searchAndDatesLayout.isVisible) {
                    binding.searchAndDatesLayout.visibility = View.GONE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)

                } else {
                    binding.searchAndDatesLayout.visibility = View.VISIBLE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_substract)
                }

            }

            binding.appBar -> {
                hideKeyboard()
            }
            binding.scroll -> {
                hideKeyboard()
            }

            binding.dateLayout -> {
                val c = Calendar.getInstance()
                if (selectedDateFrom.isNotEmpty()) {
                    c.time = simpleDateFormat.parse(selectedDateFrom)
                }

                val y = c.get(Calendar.YEAR)
                val m = c.get(Calendar.MONTH)
                val d = c.get(Calendar.DAY_OF_MONTH)

                val dpd = activity?.let {
                    DatePickerDialog(
                        it, AlertDialog.THEME_HOLO_LIGHT,
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
                }
                dpd?.show()
            }

            binding.allNewsBtn -> {
                binding.makeChoiceLayout.visibility = View.VISIBLE
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view_info_centre, newsFragment)
                    .setReorderingAllowed(true)
                    .commit()
                binding.spinnerLayout.visibility = View.VISIBLE
                binding.searchAndDatesLayout.visibility = View.GONE
                isAllNews = true
                var drawableTop = binding.popularNewsLayout.background as GradientDrawable
                var color = ColorUtil.getColorForableordisable("infocenterbtnAble")
                drawableTop.setColor(ContextCompat.getColor(requireActivity(), color))

                var drawableTop1 = binding.allNewsBtn.background as GradientDrawable
                var color1 = ColorUtil.getColorForableordisable("infocenterbtnDisable")
                drawableTop1.setColor(ContextCompat.getColor(requireActivity(), color1))
            }

            binding.popularNewsLayout -> {
                binding.makeChoiceLayout.visibility = View.VISIBLE
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view_info_centre, popularFragment)
                    .setReorderingAllowed(true)
                    .commit()
                binding.spinnerLayout.visibility = View.VISIBLE
                binding.searchAndDatesLayout.visibility = View.GONE
                isAllNews = false
                var drawableTop = binding.allNewsBtn.background as GradientDrawable
                var color = ColorUtil.getColorForableordisable("infocenterbtnAble")
                drawableTop.setColor(ContextCompat.getColor(requireActivity(), color))

                var drawableTop1 = binding.popularNewsLayout.background as GradientDrawable
                var color1 = ColorUtil.getColorForableordisable("infocenterbtnDisable")
                drawableTop1.setColor(ContextCompat.getColor(requireContext(), color1))
            }


            binding.dateLayout2 -> {
                val c = Calendar.getInstance()
                if (selectedDateTo.isNotEmpty()) {
                    c.time = simpleDateFormat.parse(selectedDateTo)
                }

                val y = c.get(Calendar.YEAR)
                val m = c.get(Calendar.MONTH)
                val d = c.get(Calendar.DAY_OF_MONTH)

                val dpd = activity?.let {
                    DatePickerDialog(
                        it, AlertDialog.THEME_HOLO_LIGHT,
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
                }
                dpd?.show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            InfoCentreFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun manageVisibility(show: Boolean) {
        if (show) {
            showProgressDialog()
        } else {
            hideProgressDialog()
        }
    }
}