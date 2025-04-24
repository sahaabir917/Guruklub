package com.gmpire.guruklub.view.fragment.favourite

import android.content.Intent
import android.os.Bundle
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
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.FragmentFavouriteBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.library.QuestionDetailsActivity
import com.gmpire.guruklub.view.activity.profile.FavouriteQuestionsViewHolder
import com.gmpire.guruklub.view.activity.viewSolutions.ViewSolutionQuestionsViewHolder
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.base.DATA_VIEW_TYPE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import com.michaelflisar.rxbus2.RxBusBuilder
import com.michaelflisar.rxbus2.rx.RxBusMode
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response
import java.lang.Exception

class FavouriteFragment : BaseFragment(), IDatabaseCallBack {

    private var hasNextPage: Boolean = false
    private lateinit var binding: FragmentFavouriteBinding
    private lateinit var viewmodel: FavouriteViewModel
    private var favouriteQuestions = ArrayList<Question>()
    lateinit var spinnerAdapterSubject: CustomSpinnerAdapter
    lateinit var spinnerAdapterSection: CustomSpinnerAdapter
    lateinit var spinnerAdapterTopic: CustomSpinnerAdapter
    lateinit var spinnerAdapterBatch: CustomSpinnerAdapter
    lateinit var spinnerAdapterDifficulty: CustomSpinnerAdapter
    var removePosition = -1
    val filterValues = FilterValues()
    var page: Int = 1
    var questionPosition: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_favourite, container, false);
        return binding.root
    }

    override fun viewRelatedTask() {
        viewmodel =
            ViewModelProviders.of(this, viewModelFactory).get(FavouriteViewModel::class.java)
        filterValues.category_id = dataManager.mPref.prefGetUserInfo().category_id.toString()
        viewmodel.iDatabaseCallBack = this

        page = 1
        viewmodel.apiGetFavouriteQuestion(
            page.toString(),
            filterValues,
            dataManager.mPref.prefGetUserInfo().id,
            this
        )
        viewmodel.apiGetSubjectByCategory(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )

        RxBusBuilder.create(UpdateClass::class.java)
            .withKey(RxBusEvents.FAVOURITE_CHANGED)
            .withBound(this)
            .withMode(RxBusMode.Main)
            .subscribe {
                page = 1
                binding.rvFavouriteQuestions.stopScroll()
                favouriteQuestions.clear()
                viewmodel.apiGetFavouriteQuestion(
                    page.toString(),
                    filterValues,
                    dataManager.mPref.prefGetUserInfo().id,
                    this
                )
            }

        val item = java.util.ArrayList<Common>()
        item.add(Common("1", "Basic"))
        item.add(Common("2", "Intermediate"))
        item.add(Common("3", "Advanced"))

        initMakeChoiceItemSpinner(binding.spDifficulty, "Select Difficulty", item)

        viewmodel.apiGetBatchByCategory(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )

        binding.makeChoiceLayout.setOnClickListener(this)
        binding.btnMakeChoice.setOnClickListener(this)
        binding.llMakeChoice.visibility = View.GONE
        binding.ivDropdownArrow.rotation = 360.0.toFloat()
    }

    private fun insertEmptyQuestion(questionList: ArrayList<Question>): ArrayList<Question> {
        var placement = ConstantField.NATIVE_AD_INTERVAL
        var increment = 0
        if (favouriteQuestions.isNotEmpty())
            placement =
                ConstantField.NATIVE_AD_INTERVAL - (favouriteQuestions.size % (ConstantField.NATIVE_AD_INTERVAL + 1))

        val questionSize = questionList.size
        while (questionSize > placement) {
            val emptyQues = Question()
            emptyQues.answer = 101 // To mark as ad question
            questionList.add(placement + increment, emptyQues)
            increment++
            placement += ConstantField.NATIVE_AD_INTERVAL
        }
        return questionList
    }

    private fun initQuestion(favouriteQuestionsLoc: ArrayList<Question>) {
        if (page != 1) {
            this.favouriteQuestions.addAll(insertEmptyQuestion(favouriteQuestionsLoc))
            binding.rvFavouriteQuestions.adapter?.notifyDataSetChanged()
        } else {
            this.favouriteQuestions = insertEmptyQuestion(favouriteQuestionsLoc)
            binding.rvFavouriteQuestions.layoutManager = LinearLayoutManager(activity)
            binding.rvFavouriteQuestions.adapter =
                BaseRecyclerAdapter(activity, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {
                        model as Question
                        when (view.id) {
                            R.id.question_root_layout -> {
                                viewmodel.dataDeleteAllTempQuestions()
                                viewmodel.insertAllTemp(favouriteQuestions.filter { it.answer != 101 } as ArrayList<Question>)
                                questionPosition = position - (position / (ConstantField.NATIVE_AD_INTERVAL + 1))
                            }
                            R.id.bookmark_iv -> {
                                removePosition = position
                                viewmodel.apiUnBookmarkQuestion(
                                    model.id,
                                    filterValues.category_id.toString(),
                                    this@FavouriteFragment
                                )
                                favouriteQuestions.removeAt(removePosition)
                                if (favouriteQuestions.size == 0) {
                                    binding.rvFavouriteQuestions.visibility = View.GONE
                                    binding.emptyMessage.visibility = View.VISIBLE
                                } else {
                                    /*val tempList =
                                        favouriteQuestions.filter { it.answer != 101 } as ArrayList<Question>
                                    favouriteQuestions.clear()
                                    insertEmptyQuestion(tempList)
                                    binding.rvFavouriteQuestions.adapter?.notifyDataSetChanged()*/
                                }
                                RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                                    UpdateClass()
                                )
                            }
                        }
                    }

                    override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                        if (viewType == DATA_VIEW_TYPE) {
                            return ViewSolutionQuestionsViewHolder(
                                DataBindingUtil.inflate(
                                    LayoutInflater.from(parent.context)
                                    , R.layout.item_view_solutions_question
                                    , parent, false
                                )
                                , activity
                                    ,"Bookmark"
                            )
                        } else {
                            return NativeAdViewHolder(
                                DataBindingUtil.inflate(
                                    LayoutInflater.from(parent.context)
                                    , R.layout.item_empty_native_ads
                                    , parent, false
                                )
                                , activity
                            )
                        }
                    }

                    override fun loadMoreItem() {
                        if (hasNextPage) {
                            viewmodel.apiGetFavouriteQuestion(
                                page.toString(),
                                filterValues,
                                dataManager.mPref.prefGetUserInfo().id,
                                this@FavouriteFragment
                            )
                        }
                    }
                }, favouriteQuestions)
        }
    }

    fun initMakeChoiceItemSpinner(sp: View, hint: String, item: ArrayList<Common>) {
        sp as AppCompatSpinner
        var sa = activity?.let {
            CustomSpinnerAdapter(
                it,
                GeneratDropdownItem.getDropdownItems(
                    JSONArray(Gson().toJson(item)),
                    "name",
                    "id",
                    hint
                )
            )
        }
        sp.adapter = sa?.getAdapter()
        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                sa?.setPosition(position)
                when (sp) {
                    binding.spSubject -> {
                        viewmodel.apiGetSectionBySubject(
                            spinnerAdapterSubject.getSelectedId(),
                            this@FavouriteFragment
                        )
                        filterValues.subject_id = spinnerAdapterSubject.getSelectedId()
                    }
                    binding.spSection -> {
                        viewmodel.apiGetTopicBySection(
                            spinnerAdapterSection.getSelectedId(),
                            this@FavouriteFragment
                        )
                        filterValues.section_id = spinnerAdapterSection.getSelectedId()
                    }
                    binding.spTopic -> {
                        filterValues.topic_id = spinnerAdapterTopic.getSelectedId()
                    }
                    binding.spBatch -> {
                        filterValues.batch_id = spinnerAdapterBatch.getSelectedId()
                    }
                    binding.spDifficulty -> {
                        filterValues.difficulty_id = spinnerAdapterDifficulty.getSelectedId()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        try {
            when (sp) {
                binding.spSubject -> spinnerAdapterSubject = sa!!
                binding.spSection -> spinnerAdapterSection = sa!!
                binding.spTopic -> spinnerAdapterTopic = sa!!
                binding.spBatch -> spinnerAdapterBatch = sa!!
                binding.spDifficulty -> spinnerAdapterDifficulty = sa!!
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetFavouriteQuestion" -> {
                val type =
                    object : TypeToken<BaseModel<PaginationModel<ArrayList<Question>>>>() {}.type
                result.data?.body()?.let {

                    if (result.data.body() != null) {
                        val baseData =
                            Gson().fromJson<BaseModel<PaginationModel<ArrayList<Question>>>>(
                                result.data.body()?.string(),
                                type
                            )
                        if (baseData.status_code == 200 && baseData.data?.data != null) {
                            if (baseData.data?.data?.size ?: 0 > 0) {
                                initQuestion(baseData.data?.data!!)
                                hasNextPage = baseData.data?.next_page != 0
                                page = baseData.data?.next_page ?: 0

                                binding.rvFavouriteQuestions.visibility = View.VISIBLE
                                binding.emptyMessage.visibility = View.GONE
                                binding.makeChoiceLayout.visibility = View.VISIBLE
                            } else {
                                binding.rvFavouriteQuestions.visibility = View.GONE
                                binding.emptyMessage.visibility = View.VISIBLE
                                //binding.makeChoiceLayout.visibility = View.GONE
                            }
                        } else {
                            binding.rvFavouriteQuestions.visibility = View.GONE
                            binding.emptyMessage.visibility = View.VISIBLE
                           // binding.makeChoiceLayout.visibility = View.GONE
                        }
                    }
                }
            }
            "apiGetSubjectByCategory" -> {
                val type = object : TypeToken<BaseModel<java.util.ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Common>>>(
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
                val type = object : TypeToken<BaseModel<java.util.ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Common>>>(
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
                val type = object : TypeToken<BaseModel<java.util.ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Common>>>(
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
            "apiGetBatchByCategory" -> {
                val type = object : TypeToken<BaseModel<java.util.ArrayList<Common>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<Common>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            initMakeChoiceItemSpinner(
                                binding.spBatch,
                                "Select Batch",
                                baseData.data ?: arrayListOf()
                            )
                        }
                    }
                }
            }
            "apiUnBookmarkQuestion" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        showToast(activity, baseData.message[0])
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                    }
                }
            }
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "apiGetFavouriteQuestion") {
            if (page != 1) {
                if (isLoader) {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                } else {
                    binding.loadingProgressBar.visibility = View.GONE
                }
            }
        } else if (key == "apiUnBookmarkQuestion") {
            if (isLoader) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.makeChoiceLayout -> {
                if (binding.llMakeChoice.isVisible) {
                    binding.llMakeChoice.visibility = View.GONE
                    binding.ivDropdownArrow.rotation = 360.0.toFloat()
                } else {
                    binding.llMakeChoice.visibility = View.VISIBLE
                    binding.ivDropdownArrow.rotation = 180.0.toFloat()
                }
            }

            binding.btnMakeChoice -> {
                binding.llMakeChoice.visibility = View.GONE
                binding.ivDropdownArrow.rotation = 360.0.toFloat()
                favouriteQuestions.clear()
                page = 1
                viewmodel.apiGetFavouriteQuestion(
                    page.toString(),
                    filterValues,
                    dataManager.mPref.prefGetUserInfo().id, this
                )
            }
        }
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "insertAllTemp" -> {
                hideProgressDialog()
                val intent = Intent(activity, QuestionDetailsActivity::class.java)
                intent.putExtra("position", questionPosition)
                startActivity(intent)
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
        print(optName + result.toString())
    }
}