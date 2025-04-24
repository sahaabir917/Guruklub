package com.gmpire.guruklub.view.fragment.library

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.library.*
import com.gmpire.guruklub.databinding.FragmentLibraryBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.activity.library.LibraryActivity
import com.gmpire.guruklub.view.activity.library.LibraryViewmodel
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBusBuilder
import com.michaelflisar.rxbus2.rx.RxBusMode
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import kotlin.NullPointerException


class LibraryFragment : BaseFragment() {

    lateinit var binding: FragmentLibraryBinding
    private var title: String? = null
    private lateinit var viewmodel: LibraryViewmodel
    lateinit var spinnerAdapterSubject: CustomSpinnerAdapter
    lateinit var spinnerAdapterSection: CustomSpinnerAdapter
    lateinit var spinnerAdapterTopic: CustomSpinnerAdapter

    var resumeStudy: Library? = null
    val filterValues = FilterValues()

    companion object {
        private var f = LibraryFragment()
        fun newInstance(title: String): LibraryFragment {
            val args = Bundle()
            args.putString(ConstantField.ACCESS_TITLE, title)
            f.arguments = args
            Log.d("TAG", f.toString())
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library, container, false);
        return binding.root
    }


    override fun viewRelatedTask() {
        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewmodel::class.java)
        filterValues.category_id = dataManager.mPref.prefGetUserInfo().category_id.toString()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewmodel.apiGetSubjectByCategoryLibrary(
                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                this
            )
            viewmodel.fetchMostPopular(
                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                this
            )
            viewmodel.fetchRecentLearn(
                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                this
            )
            // updateResumeAndRecommendedStudy()
        }

        viewmodel.apiGetSubjectByCategoryLibrary(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )
        viewmodel.fetchMostPopular(dataManager.mPref.prefGetUserInfo().category_id.toString(), this)
        viewmodel.fetchRecentLearn(dataManager.mPref.prefGetUserInfo().category_id.toString(), this)

        binding.makeChoiceLayout.setOnClickListener(this)
        binding.btnMakeChoice.setOnClickListener(this)

        binding.llMakeChoice.visibility = View.VISIBLE
        //binding.ivDropdownArrow.rotation = 360.0.toFloat()

        // updateResumeAndRecommendedStudy()


        RxBusBuilder.create(UpdateClass::class.java)
            .withKey(RxBusEvents.RESUME_STUDY_CHANGED)
            .withBound(this)
            .withMode(RxBusMode.Main)
            .subscribe {
                // updateResumeAndRecommendedStudy()
                viewmodel.fetchMostPopular(
                    dataManager.mPref.prefGetUserInfo().category_id.toString(),
                    this
                )
                viewmodel.fetchRecentLearn(
                    dataManager.mPref.prefGetUserInfo().category_id.toString(),
                    this
                )
            }

        RxBusBuilder.create(UpdateClass::class.java)
            .withKey(RxBusEvents.FAVOURITE_CHANGED)
            .withBound(this)
            .withMode(RxBusMode.Main)
            .subscribe {

            }
    }

    private fun updateResumeAndRecommendedStudy() {
        resumeStudy = dataManager.mPref.prefGetResumeStudy()
        resumeStudy?.let {
            binding.resumeStudyTv.text =
                "Resume study: ${resumeStudy?.title},${resumeStudy?.subject_name}"
            it.recommended?.let {
                binding.recommendedStudyTv.text =
                    "Recommended Study: ${it.title},${it.subject_name}"
                // binding.recommendedStudyLayout.visibility = View.VISIBLE
            } ?: kotlin.run {
                binding.recommendedStudyLayout.visibility = View.GONE
            }
            //binding.resumeStudyLayout.visibility = View.VISIBLE

        } ?: kotlin.run {
            binding.resumeStudyLayout.visibility = View.GONE
            binding.recommendedStudyLayout.visibility = View.GONE
        }


        binding.resumeStudyLayout.setOnClickListener(this)
        binding.recommendedStudyLayout.setOnClickListener(this)
    }

    private fun initRecentlyLear(recentlyLearns: ArrayList<MostPopularAndRecentLearning>) {

        binding.rvRecentlyLearns.minimumHeight = recentlyLearns.size * 100

        binding.rvRecentlyLearns.layoutManager = LinearLayoutManager(activity)
        binding.rvRecentlyLearns.adapter = BaseRecyclerAdapter(activity, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {

                model as MostPopularAndRecentLearning

                filterValues.subject_id = model.subject_id.toString()
                filterValues.section_id = model.section_id.toString()
                filterValues.topic_id = model.topic_id.toString()

                val intent = Intent(activity, LibraryActivity::class.java)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("subject_name", model.subject_name)
                activity?.startActivity(intent)
                clearFilterValues()


            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                return RecentlyLearnViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context)
                        , R.layout.item_recently_learn
                        , parent, false
                    )
                    , activity
                )
            }

            override fun loadMoreItem() {
            }
        }, recentlyLearns)

    }


    private fun initMostPopulars(mostPopulars: ArrayList<MostPopularAndRecentLearning>) {

        binding.rvMostPopular.minimumHeight = mostPopulars.size * 100
        binding.rvMostPopular.layoutManager = LinearLayoutManager(activity)

        binding.rvMostPopular.adapter = BaseRecyclerAdapter(activity, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {

                model as MostPopularAndRecentLearning

                filterValues.subject_id = model.subject_id.toString()
                filterValues.section_id = model.section_id.toString()
                filterValues.topic_id = model.topic_id.toString()

                val intent = Intent(activity, LibraryActivity::class.java)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("subject_name", model.subject_name)
                activity?.startActivity(intent)
                clearFilterValues()


            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {

                return RecentlyLearnViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context)
                        , R.layout.item_recently_learn
                        , parent, false
                    )
                    , activity
                )
            }

            override fun loadMoreItem() {

            }
        }, mostPopulars)

    }

    fun initMakeChoiceItemSpinner(sp: View, hint: String, item: ArrayList<Common>) {
        sp as AppCompatSpinner
        try {
            val sa = CustomSpinnerAdapter(
                requireActivity(),
                GeneratDropdownItem.getDropdownItems(
                    JSONArray(Gson().toJson(item)),
                    "name",
                    "id",
                    hint
                )
            )
            sp.adapter = sa.getAdapter()
            if (!filterValues.subject_id.isNullOrEmpty()) {
                val pos = sa.getPositionById(filterValues.subject_id ?: "")
                sp.setSelection(pos)
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
                            viewmodel.apiGetSectionBySubject(
                                spinnerAdapterSubject.getSelectedId(),
                                this@LibraryFragment
                            )
                            filterValues.subject_id = spinnerAdapterSubject.getSelectedId()
                        }
                        binding.spSection -> {
                            viewmodel.apiGetTopicBySection(
                                spinnerAdapterSection.getSelectedId(),
                                this@LibraryFragment
                            )
                            filterValues.section_id = spinnerAdapterSection.getSelectedId()
                        }
                        binding.spTopic -> {
                            filterValues.topic_id = spinnerAdapterTopic.getSelectedId()
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
            }

        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetSubjectByCategoryLibrary" -> {
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
                                baseData.data ?: arrayListOf()
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
                                baseData.data ?: arrayListOf()
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
                                baseData.data ?: arrayListOf()
                            )
                        }
                    }
                }
            }
            "fetchMostPopular" -> {
                val type =
                    object : TypeToken<BaseModel<ArrayList<MostPopularAndRecentLearning>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<MostPopularAndRecentLearning>>>(
                            result.data.body()?.string(),
                            type
                        )

                    if (baseData.status_code == 200) {
                        if (baseData.data?.size ?: 0 > 0) {
                            initMostPopulars(baseData.data!!)
                            // binding.mostPopulerLayout.visibility = View.VISIBLE
                        } else {
                            binding.mostPopulerLayout.visibility = View.GONE
                        }

                    }
                }

            }
            "fetchRecentLearn" -> {
                val type =
                    object : TypeToken<BaseModel<ArrayList<MostPopularAndRecentLearning>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<MostPopularAndRecentLearning>>>(
                            result.data.body()?.string(),
                            type
                        )

                    if (baseData.status_code == 200) {
                        if (baseData.data?.size ?: 0 > 0) {
                            initRecentlyLear(baseData.data ?: arrayListOf())
                            //   binding.recentlyLearnedLayout.visibility = View.VISIBLE
                        } else {
                            binding.recentlyLearnedLayout.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    override fun onLoading(isLoader: Boolean, key: String) {
        progressBarListener?.hideProgress()
    }

    override fun onError(err: Throwable, key: String) {
        showToast(context, err.message.toString())
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.makeChoiceLayout -> {
                /*  if (binding.llMakeChoice.isVisible) {
                      binding.llMakeChoice.visibility = View.GONE
                      binding.ivDropdownArrow.rotation = 360.0.toFloat()
                  } else {
                      binding.llMakeChoice.visibility = View.VISIBLE
                      binding.ivDropdownArrow.rotation = 180.0.toFloat()
                  }*/
            }
            binding.btnMakeChoice -> {
                if (filterValues.topic_id == "0") {
                    showToast(activity, "choose a topic to continue")
                    return
                }
                //binding.llMakeChoice.visibility = View.GONE
                // binding.ivDropdownArrow.rotation = 360.0.toFloat()

                val intent = Intent(activity, LibraryActivity::class.java)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("subject_name", spinnerAdapterSubject.getSelectedItem())
                activity?.startActivity(intent)
                clearFilterValues()

            }

            binding.resumeStudyLayout -> {
                filterValues.subject_id = resumeStudy?.subject_id.toString()
                filterValues.section_id = resumeStudy?.section_id.toString()
                filterValues.topic_id = resumeStudy?.topic_id.toString()

                val intent = Intent(activity, LibraryActivity::class.java)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("subject_name", resumeStudy?.subject_name)
                activity?.startActivity(intent)
                clearFilterValues()

            }

            binding.recommendedStudyLayout -> {
                filterValues.subject_id = resumeStudy?.subject_id.toString()
                filterValues.section_id = resumeStudy?.recommended?.section_id.toString()
                filterValues.topic_id = resumeStudy?.recommended?.topic_id.toString()

                val intent = Intent(activity, LibraryActivity::class.java)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("subject_name", resumeStudy?.recommended?.subject_name)
                activity?.startActivity(intent)

                clearFilterValues()
            }

        }

    }

    private fun clearFilterValues() {
        filterValues.topic_id = "0"
    }
}
