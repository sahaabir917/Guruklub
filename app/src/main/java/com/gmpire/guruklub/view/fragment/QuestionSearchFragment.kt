package com.gmpire.guruklub.view.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.QuestionDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.FragmentQuestionSearchBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.library.QuestionDetailsActivity
import com.gmpire.guruklub.view.activity.questionSearch.QuestionSearchViewModel
import com.gmpire.guruklub.view.activity.viewSolutions.ViewSolutionQuestionsViewHolder
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.base.DATA_VIEW_TYPE
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBusBuilder
import com.michaelflisar.rxbus2.rx.RxBusMode
import kotlinx.android.synthetic.main.activity_question_search.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*


class QuestionSearchFragment : BaseFragment(), IDatabaseCallBack {
    lateinit var binding: FragmentQuestionSearchBinding
    private lateinit var viewModel: QuestionSearchViewModel
    private var questionList = arrayListOf<Question>()
    val filterValues = FilterValues()
    var questionPosition: Int = -1
    var page: Int = 1
    var keyWord = ""
    private var selectedQuestion: Question? = null

    companion object {
        private var f = QuestionSearchFragment()
        fun newInstance(title: String): QuestionSearchFragment {
            val args = Bundle()
            args.putString(ConstantField.ACCESS_TITLE, title)
            f.arguments = args
            Log.d("TAG", f.toString())
            return f
        }
    }

    override fun viewRelatedTask() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(QuestionSearchViewModel::class.java)
        viewModel.iDatabaseCallBack = this

        filterValues.category_id = dataManager.mPref.prefGetUserInfo().category_id.toString()
        initSearchView()
        initQuestion()
        viewModel.getFiveRecentQuestions()
        binding.swipeRefreshLayoutSearch.isEnabled = false
        appCompatImageViewCross.setOnClickListener(this)

        RxBusBuilder.create(UpdateClass::class.java)
            .withKey(RxBusEvents.FAVOURITE_CHANGED)
            .withBound(this)
            .withMode(RxBusMode.Main)
            .subscribe {
                page = 1
                viewModel.getQuestionByKeyword(
                    dataManager.mPref.prefGetUserInfo().id,
                    keyWord,
                    this
                )
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_question_search, container, false)
        //MobileAds.initialize(activity)
        return binding.root
    }


    private fun initQuestion() {
        binding.rvSearchQuestions.layoutManager = LinearLayoutManager(activity)
        binding.rvSearchQuestions.adapter =
            BaseRecyclerAdapter(activity, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                    model as Question
                    when (view.id) {
                        R.id.question_root_layout -> {
                            // Manage recent search
                            selectedQuestion = model
                            viewModel.checkIfAlreadyAdded(model.id)
                            viewModel.getQuestionCount()
                            viewModel.dataDeleteAllTempQuestions()
                            viewModel.insertAllTemp(questionList.filter { it.answer != 101 })
                            questionPosition =
                                position - (position / (ConstantField.NATIVE_AD_INTERVAL + 1))
                        }
                        R.id.bookmark_iv -> {
                            questionPosition = position
                            if (model.is_bookmarked == 0) {
                                viewModel.apiBookmarkQuestion(
                                    model.id,
                                    filterValues.category_id.toString(),
                                    this@QuestionSearchFragment
                                )
                            } else {
                                viewModel.apiUnBookmarkQuestion(
                                    model.id,
                                    filterValues.category_id.toString(),
                                    this@QuestionSearchFragment
                                )
                            }
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
                        ,"questionsearchfragment"
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
                }
            }, this.questionList)

    }

    private fun initSearchView() {
        appCompatEditTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                Handler().postDelayed({
                    if (p0 != null) {
                        if (p0.toString().length > 2) {
                            keyWord = p0.toString()
                            viewModel.getQuestionByKeyword(
                                dataManager.mPref.prefGetUserInfo().id,
                                p0.toString(),
                                this@QuestionSearchFragment
                            )
                        }
                    }
                }, 450)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (appCompatEditTextSearch.text.toString().isNotEmpty()) {
                    appCompatImageViewCross.visibility = View.VISIBLE
                } else {
                    appCompatImageViewCross.visibility = View.GONE
                }
            }
        })
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (swipeRefreshLayoutSearch != null) {
            if (isLoader) {
                swipeRefreshLayoutSearch.post { swipeRefreshLayoutSearch.isRefreshing = true }
            } else {
                swipeRefreshLayoutSearch.post {
                    swipeRefreshLayoutSearch.isRefreshing = false
                }
            }
        }
    }


    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "getQuestionByKeyword" -> {
                val type =
                    object : TypeToken<BaseModel<PaginationModel<ArrayList<Question>>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<PaginationModel<ArrayList<Question>>>>(
                            result.data.body()?.string(),
                            type
                        )

                    if (baseData != null) {
                        if (baseData.status_code == 200 && baseData.data?.data != null) {
                            val questionList = baseData.data?.data
                            if (questionList?.size ?: 0 > 0) {
                                this.questionList.clear()
                                this.questionList.addAll(questionList ?: arrayListOf())
                                if (questionList!!.size > ConstantField.NATIVE_AD_INTERVAL) {
                                    insertEmptyQuestion()
                                }
                                binding.rvSearchQuestions.adapter?.notifyDataSetChanged()
                                binding.rvSearchQuestions.visibility = View.VISIBLE
                                binding.emptyMessage.visibility = View.GONE
                                binding.recentSearch.visibility = View.GONE
                            } else {
                                binding.rvSearchQuestions.visibility = View.GONE
                                binding.emptyMessage.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            "apiBookmarkQuestion" -> {
                val type = object : TypeToken<BaseModel<EmptyModel>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<EmptyModel>>(
                            result.data.body()?.string(),
                            type
                        )

                    if (baseData.status_code == 200) {
                        showToast(activity, baseData.message[0])
                        questionList[questionPosition].is_bookmarked = 1
                        binding.rvSearchQuestions.adapter?.notifyItemChanged(questionPosition)
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
                        questionList[questionPosition].is_bookmarked = 0
                        binding.rvSearchQuestions.adapter?.notifyItemChanged(questionPosition)
                    }
                }
            }
        }
    }

    private fun insertEmptyQuestion() {
        var placement = ConstantField.NATIVE_AD_INTERVAL
        var increment = 0

        val questionSize = questionList.size
        while (questionSize > placement) {
            val emptyQues = Question()
            emptyQues.answer = 101 // To mark as ad question
            questionList.add(placement + increment, emptyQues)
            increment++
            placement += ConstantField.NATIVE_AD_INTERVAL
        }
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "getFiveRecentQuestions" -> {
                val quesListDTO = result as List<QuestionDTO>
                val quesList = QuestionDTO.toQuestions(quesListDTO)
                if (quesList.isNotEmpty()) {
                    this.questionList.clear()
                    this.questionList.addAll(quesList)
                    binding.rvSearchQuestions.adapter?.notifyDataSetChanged()
                    binding.rvSearchQuestions.visibility = View.VISIBLE
                    binding.emptyMessage.visibility = View.GONE
                    binding.recentSearch.visibility = View.VISIBLE
                } else {
                    binding.rvSearchQuestions.visibility = View.GONE
                    binding.emptyMessage.visibility = View.VISIBLE
                    binding.recentSearch.visibility = View.GONE
                }
                hideProgressDialog()
            }
            "checkIfAlreadyAdded" -> {
                val questionCount = result as Int
                if (questionCount == 0) {
                    selectedQuestion?.let { viewModel.insertQuestion(it, false) }
                } else {
                    selectedQuestion?.let {  viewModel.insertQuestion(it, true) }
                }
            }
            "insertQuestion" -> {
            }
            "getQuestionCount" -> {
                val questionCount = result as Int
                if (questionCount == 5) {
                    viewModel.deleteOldestQuestion()
                }
            }
            "deleteOldestQuestion" -> {
            }
            "insertAllTemp" -> {
                hideProgressDialog()
                val intent = Intent(
                    activity,
                    QuestionDetailsActivity::class.java
                )
                intent.putExtra("position", questionPosition)
                startActivity(intent)
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
        println("$optName -> $result")
        hideProgressDialog()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.appCompatImageViewCross -> {
                appCompatEditTextSearch.setText("")
                this.questionList.clear()
                binding.rvSearchQuestions.adapter?.notifyDataSetChanged()
                binding.rvSearchQuestions.visibility = View.GONE
                binding.emptyMessage.visibility = View.VISIBLE
            }
        }
    }

}