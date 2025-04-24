package com.gmpire.guruklub.view.activity.library

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.FragmentLibraryQuestionBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.base.DATA_VIEW_TYPE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception

class QuestionsFragment : BaseFragment(),
    IDatabaseCallBack {

    private var favouritePosition: Int = -1
    private var binding: FragmentLibraryQuestionBinding? = null
    private lateinit var viewmodel: LibraryViewmodel
    private var libraryQuestions: ArrayList<Question> = arrayListOf()
    private lateinit var categoryId: String
    var questionPosition: Int = -1
    private var passingFilterValues: FilterValues? = FilterValues()
    var questions: MutableLiveData<ArrayList<Question>> = MutableLiveData()

    fun QuestionsFragment() {
        // doesn't do anything special
    }

    companion object {
        @JvmStatic
        private var f = QuestionsFragment()
        fun newInstance(loadMoreQuestionListener: LoadMoreQuestionListener): QuestionsFragment {
            val args = Bundle()
            //args.putString(ConstantField.ACCESS_TITLE, questions)
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
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_library_question, container, false)
        return binding?.root
    }

    override fun viewRelatedTask() {
        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewmodel::class.java)
        categoryId = dataManager.mPref.prefGetUserInfo().category_id.toString()
        viewmodel.iDatabaseCallBack = this
    }

    fun loadQuestions(questions: ArrayList<Question>?) {
        if (binding != null) {
            if (questions!!.isNotEmpty()) {
                initQuestion(questions)
                binding?.emptyView?.visibility = View.GONE
                binding?.rvSentence?.visibility = View.VISIBLE
            } else {
                binding?.rvSentence?.visibility = View.GONE
                binding?.emptyView?.visibility = View.VISIBLE
            }
        }
    }

    fun hideQuestionList() {
        binding?.emptyView?.visibility = View.GONE
        binding?.rvSentence?.visibility = View.GONE
        libraryQuestions.clear()
    }

    private fun insertEmptyQuestion(questionList: ArrayList<Question>): ArrayList<Question> {
        val questionSize = questionList.size
        if (questionSize > 5) {
            val emptyQues = Question()
            emptyQues.answer = 101 // To mark as ad question
            questionList.add(5, emptyQues)
        }
        return questionList
    }

    private fun initQuestion(libraryQuestions: ArrayList<Question>) {
        this.libraryQuestions = arrayListOf()
        val tempList: ArrayList<Question> = arrayListOf()
        tempList.addAll(libraryQuestions)
        this.libraryQuestions.clear()
        this.libraryQuestions.addAll(insertEmptyQuestion(tempList))
        binding?.rvSentence?.layoutManager = LinearLayoutManager(activity)
        binding?.rvSentence?.adapter = BaseRecyclerAdapter(activity, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                model as Question
                when (view.id) {
                    R.id.question_root_layout -> {
                        viewmodel.dataDeleteAllTempQuestions()
                        var sizeOfQuestion = libraryQuestions.size
                        if (position == sizeOfQuestion) {
                            var new_position = position - 1
                            passingFilterValues?.subject_id = libraryQuestions[new_position]?.subject_id
                            passingFilterValues?.section_id = libraryQuestions[new_position].section_id
                            passingFilterValues?.topic_id = libraryQuestions[new_position].topic_id
                        } else {
                            passingFilterValues?.subject_id = libraryQuestions[position].subject_id
                            passingFilterValues?.section_id = libraryQuestions[position].section_id
                            passingFilterValues?.topic_id = libraryQuestions[position].topic_id
                        }
                        viewmodel.insertAllTemp(libraryQuestions.filter { it.answer != 101 })
                        questionPosition = if (position > ConstantField.NATIVE_AD_INTERVAL) {
                            position - 1
                        } else {
                            position
                        }
                    }
                    R.id.bookmark_iv -> {
                        favouritePosition = position
                        if (model.is_bookmarked == 0) {
                            viewmodel.apiBookmarkQuestion(
                                model.id,
                                categoryId,
                                this@QuestionsFragment
                            )
                        } else {
                            viewmodel.apiUnBookmarkQuestion(
                                model.id,
                                categoryId,
                                this@QuestionsFragment
                            )
                        }
                    }
                }
            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                if (viewType == DATA_VIEW_TYPE) {
                    return QuestionsViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context)
                            , R.layout.item_question_linear_flat
                            , parent, false
                        )
                        , this@QuestionsFragment
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
        }, this.libraryQuestions)

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
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
                        showToast(activity, baseData.message[0])
                        libraryQuestions[favouritePosition].is_bookmarked = 1
                        binding?.rvSentence?.adapter?.notifyItemChanged(favouritePosition)
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
                        showToast(activity, baseData.message[0])
                        libraryQuestions[favouritePosition].is_bookmarked = 0
                        binding?.rvSentence?.adapter?.notifyItemChanged(favouritePosition)
                    }
                }
            }
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "apiUnBookmarkQuestion" || key == "apiBookmarkQuestion") {
            if (isLoader) {
                showProgressDialog()
            } else {
                hideProgressDialog()
            }
        }
    }

    override fun onClick(v: View?) {
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "insertAllTemp" -> {
                val intent = Intent(
                    activity,
                    QuestionDetailsActivity::class.java
                )
                intent.putExtra("position", questionPosition)
                intent.putExtra(
                    "filterValues", Gson().toJson(passingFilterValues)
                )
                startActivityForResult(intent, 2)
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
        print("$optName->$result")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            try {
                val bookmarkMap = data!!.getSerializableExtra("bookmark_map") as HashMap<Int, Int>
                refineQuestionList(bookmarkMap)
            } catch (ex: Exception) {
                print("$ex----Bookmark Map isnull")
            }
        }
    }

    private fun refineQuestionList(bookmarkMap: HashMap<Int, Int>) {
        if (bookmarkMap.isNotEmpty()) {
            bookmarkMap.forEach {
                val pos = if (it.key >= ConstantField.NATIVE_AD_INTERVAL) {
                    it.key + 1
                } else {
                    it.key
                }
                libraryQuestions[pos].is_bookmarked = it.value
                binding?.rvSentence?.adapter?.notifyItemChanged(pos)
            }
        }
    }

    interface LoadMoreQuestionListener {
        fun onLoadMore(page: Int)
    }

}