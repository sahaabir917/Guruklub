package com.gmpire.guruklub.view.activity.questionSearch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.gmpire.guruklub.databinding.ActivityQuestionSearchBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.library.QuestionDetailsActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.viewSolutions.ViewSolutionQuestionsViewHolder
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.base.DATA_VIEW_TYPE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import com.michaelflisar.rxbus2.RxBusBuilder
import com.michaelflisar.rxbus2.rx.RxBusMode
import kotlinx.android.synthetic.main.activity_question_search.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*


class QuestionSearchActivity : BaseActivity(), IDatabaseCallBack {

    private lateinit var binding: ActivityQuestionSearchBinding
    private lateinit var viewModel: QuestionSearchViewModel
    private var questionList = arrayListOf<Question>()
    val filterValues = FilterValues()
    var questionPosition: Int = -1
    var page: Int = 1
    var keyWord = ""
    private var passingFilterValues: FilterValues? = FilterValues()
    private var selectedQuestion: Question? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_question_search)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(QuestionSearchViewModel::class.java)
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Search Question", true, false)
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

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
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
                                this@QuestionSearchActivity
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

        appCompatEditTextSearch.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        }
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun initQuestion() {
        binding.rvSearchQuestions.layoutManager = LinearLayoutManager(this)
        binding.rvSearchQuestions.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                    model as Question
                    when (view.id) {
                        R.id.question_root_layout -> {
                            // Manage recent search
                            passingFilterValues?.subject_id = questionList[position].subject_id
                            passingFilterValues?.section_id = questionList[position].section_id
                            passingFilterValues?.topic_id = questionList[position].topic_id
                            selectedQuestion = model
                            viewModel.checkIfAlreadyAdded(model.id)
                            viewModel.getQuestionCount()
                            viewModel.dataDeleteAllTempQuestions()
                            viewModel.insertAllTemp(questionList.filter { it.answer != 101 })
                            questionPosition = if (position > ConstantField.NATIVE_AD_INTERVAL) {
                                position - 1
                            } else {
                                position
                            }
                        }
                        R.id.bookmark_iv -> {
                            questionPosition = position
                            if (model.is_bookmarked == 0) {
                                viewModel.apiBookmarkQuestion(
                                    model.id,
                                    filterValues.category_id.toString(),
                                    this@QuestionSearchActivity
                                )
                            } else {
                                viewModel.apiUnBookmarkQuestion(
                                    model.id,
                                    filterValues.category_id.toString(),
                                    this@QuestionSearchActivity
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
                            , this@QuestionSearchActivity
                            , "Search"
                        )
                    } else {
                        return NativeAdViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context)
                                , R.layout.item_empty_native_ads
                                , parent, false
                            )
                            , this@QuestionSearchActivity
                        )
                    }
                }

                override fun loadMoreItem() {
                }
            }, this.questionList)

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "getQuestionByKeywords" -> {
                val type =
                    object : TypeToken<BaseModel<PaginationModel<ArrayList<Question>>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<PaginationModel<ArrayList<Question>>>>(
                            result.data.body()?.string(),
                            type
                        )

                    Log.d("basedata",baseData.toString())

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
                        else{
                            showToast(this,baseData.message[0])
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
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                        showToast(this, baseData.message[0])
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
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                        showToast(this, baseData.message[0])
                        questionList[questionPosition].is_bookmarked = 0
                        binding.rvSearchQuestions.adapter?.notifyItemChanged(questionPosition)
                    }
                }
            }
        }
    }

    private fun insertEmptyQuestion() {
      /*  var placement = ConstantField.NATIVE_AD_INTERVAL
        var increment = 0*/

        val questionSize = questionList.size
        if(questionSize > 5) {
            val emptyQues = Question()
            emptyQues.answer = 101 // To mark as ad question
            questionList.add(5, emptyQues)
        }

      /*  val questionSize = questionList.size
        while (questionSize > placement) {
            val emptyQues = Question()
            emptyQues.answer = 101 // To mark as ad question
            questionList.add(placement + increment, emptyQues)
            increment++
            placement += ConstantField.NATIVE_AD_INTERVAL
        }*/
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
                    selectedQuestion?.let { viewModel.insertQuestion(it, true) }
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
                    this,
                    QuestionDetailsActivity::class.java
                )
                intent.putExtra("position", questionPosition)
                intent.putExtra(
                    "filterValues", Gson().toJson(passingFilterValues))
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
