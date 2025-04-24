package com.gmpire.guruklub.view.activity.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.SectionDTO
import com.gmpire.guruklub.data.local_db.dto.SubjectDTO
import com.gmpire.guruklub.data.local_db.dto.TopicDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.categoryAndSubject.BaseItem
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.data.model.viewSolution.GameSolutionResponse
import com.gmpire.guruklub.data.model.viewSolution.GameSubjectSectionTopic
import com.gmpire.guruklub.databinding.ActivityErrorBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.library.QuestionDetailsActivity
import com.gmpire.guruklub.view.activity.viewSolutions.ViewSolutionQuestionsViewHolder
import com.gmpire.guruklub.view.activity.viewSolutions.ViewSolutionsViewModel
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.base.DATA_VIEW_TYPE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response

class ErrorActivity : BaseActivity(), IDatabaseCallBack {
    private lateinit var binding: ActivityErrorBinding
    private var hasNextPage: Boolean = false
    private lateinit var viewmodel: ViewSolutionsViewModel
    private var questions: MutableLiveData<java.util.ArrayList<Question>> = MutableLiveData()
    lateinit var spinnerAdapterSubject: CustomSpinnerAdapter
    lateinit var spinnerAdapterSection: CustomSpinnerAdapter
    lateinit var spinnerAdapterTopic: CustomSpinnerAdapter
    lateinit var spinnerAdapterBatch: CustomSpinnerAdapter
    lateinit var spinnerAdapterDifficulty: CustomSpinnerAdapter
    var errorQuestions = ArrayList<Question>()
    var subjectList = java.util.ArrayList<BaseItem>()
    var sectionList = java.util.ArrayList<BaseItem>()
    var topicList = java.util.ArrayList<BaseItem>()
    var gameSubjectSectionTopic: GameSubjectSectionTopic? = null
    val filterValues = FilterValues()
    var page: Int = 1
    var favouritePosition: Int = -1
    var questionPosition: Int = -1
    var isFilterDataPresent = false
    var isBlur: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_error)
    }

    override fun viewRelatedTask() {
        page = 1
        viewmodel =
            ViewModelProviders.of(this, viewModelFactory).get(ViewSolutionsViewModel::class.java)
        viewmodel.iDatabaseCallBack = this@ErrorActivity

        viewmodel.apiGetGameSubjectSectionTopic("", this)
        filterValues.category_id = dataManager.mPref.prefGetUserInfo().category_id.toString()
        filterValues.type = "wrong"

        val item = java.util.ArrayList<Common>()
        item.add(Common("1", "Basic"))
        item.add(Common("2", "Intermediate"))
        item.add(Common("3", "Advanced"))

        //initMakeChoiceItemSpinner(binding.spDifficulty, "Select Difficulty", item)

        /*viewmodel.apiGetBatchByCategory(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )*/
        viewmodel.checkFilterValuesAvailable()

        viewmodel.apiGetGameSolution(
            page.toString(),
            dataManager.mPref.prefGetUserInfo().id,
            "",
            "profile",
            filterValues,
            this
        )

        binding.makeChoiceLayout.setOnClickListener(this)
        binding.btnMakeChoice.setOnClickListener(this)
        binding.showSectionlayout.setOnClickListener(this)

        binding.relativeLayoutSubject.setOnClickListener {
            binding.spSubject.performClick()
        }

        binding.spSectionlayout.setOnClickListener {
            binding.spSection.performClick()
        }

        binding.spTopicLayout.setOnClickListener {
            binding.spTopic.performClick()
        }

        binding.llMakeChoice.visibility = View.VISIBLE
        binding.ivDropdownArrow.rotation = 360.0.toFloat()

        setToolbar(this, binding.toolbar, "My Errors", true)
        //binding.toolbar.appCompatTextViewLogo.visibility = View.VISIBLE
    }

    private fun insertEmptyQuestion(questionList: ArrayList<Question>): ArrayList<Question> {
        /* var placement = ConstantField.NATIVE_AD_INTERVAL
         if (errorQuestions.isNotEmpty())
             placement =
                 ConstantField.NATIVE_AD_INTERVAL - (errorQuestions.size % (ConstantField.NATIVE_AD_INTERVAL + 1))*/

        val questionSize = questionList.size
        if (questionSize > 5) {
            val emptyQues = Question()
            emptyQues.answer = 101 // To mark as ad question
            questionList.add(5, emptyQues)
        }

        /*  var increment = 0
          val questionSize = questionList.size
          while (questionSize > placement) {
              val emptyQues = Question()
              emptyQues.answer = 101 // To mark as ad question
              questionList.add(placement + increment, emptyQues)
              increment++
              placement += ConstantField.NATIVE_AD_INTERVAL
          }*/
        return questionList
    }

    private fun initQuestion(errorQuestionsLoc: ArrayList<Question>) {
        if (page != 1) {
            this.errorQuestions.addAll(errorQuestionsLoc)
            binding.rvErrorQuestions.adapter?.notifyDataSetChanged()
        } else {
            this.errorQuestions = insertEmptyQuestion(errorQuestionsLoc)
            binding.rvErrorQuestions.layoutManager = LinearLayoutManager(this)
            binding.rvErrorQuestions.adapter =
                BaseRecyclerAdapter(this, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {
                        model as Question
                        when (view.id) {
                            R.id.question_root_layout -> {
                                viewmodel.dataDeleteAllTempQuestions()
                                val filteredList = errorQuestions.filter { it.answer != 101 }
                                viewmodel.insertAllTemp(filteredList)
                                questionPosition =
                                    if (position > ConstantField.NATIVE_AD_INTERVAL) {
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
                                        filterValues.category_id.toString(),
                                        this@ErrorActivity
                                    )
                                    if (favouritePosition < errorQuestions.size) {
                                        errorQuestions[favouritePosition].is_bookmarked = 1
                                    }
                                } else {
                                    viewmodel.apiUnBookmarkQuestion(
                                        model.id,
                                        filterValues.category_id.toString(),
                                        this@ErrorActivity
                                    )
                                    if (favouritePosition < errorQuestions.size) {
                                        errorQuestions[favouritePosition].is_bookmarked = 0
                                    }
                                }
                                binding.rvErrorQuestions.adapter?.notifyItemChanged(
                                    favouritePosition
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
                                , this@ErrorActivity,
                                "Error"
                            )
                        } else {
                            return NativeAdViewHolder(
                                DataBindingUtil.inflate(
                                    LayoutInflater.from(parent.context)
                                    , R.layout.item_empty_native_ads
                                    , parent, false
                                )
                                , this@ErrorActivity
                            )
                        }
                    }

                    override fun loadMoreItem() {
                        if (hasNextPage) {
                            viewmodel.apiGetGameSolution(
                                page.toString(),
                                dataManager.mPref.prefGetUserInfo().id,
                                "",
                                "profile",
                                filterValues,
                                this@ErrorActivity
                            )
                        }
                    }


                }, this.errorQuestions)
        }
    }

    fun initMakeChoiceItemSpinner(sp: View, hint: String, item: java.util.ArrayList<BaseItem>) {
        sp as AppCompatSpinner
        try {
            val sa = CustomSpinnerAdapter(
                this,
                GeneratDropdownItem.getDropdownItems(
                    JSONArray(Gson().toJson(item)),
                    "name",
                    "id",
                    hint
                )
            )
            if (hint == "Select Subject") {
                isBlur = true
            }
            sp.adapter = sa.getAdapter(isBlur)
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

                            filterValues.subject_id = spinnerAdapterSubject.getSelectedId()
                            sectionList.clear()
                            if (filterValues.subject_id != "0") {
                                binding.sectionIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                                isBlur = true
                            } else {
                                binding.sectionIcon.setImageResource(R.drawable.blur_down_arrow)
                                isBlur = false
                            }
                            if (isFilterDataPresent) {
                                viewmodel.dataGetAllSectionListBySubject(
                                    filterValues.subject_id ?: ""
                                )
                            } else {
                                gameSubjectSectionTopic?.section_list?.forEach {
                                    if (it.subject_id == spinnerAdapterSubject.getSelectedId()) {
                                        sectionList.add(BaseItem(it.section_id, it.section_name))
                                    }
                                }

                                initMakeChoiceItemSpinner(
                                    binding.spSection,
                                    "Select Section",
                                    sectionList
                                )
                            }
                        }
                        binding.spSection -> {
                            filterValues.section_id = spinnerAdapterSection.getSelectedId()
                            topicList.clear()
                            if (filterValues.section_id == "0") {
                                binding.topicIcon.setImageResource(R.drawable.blur_down_arrow)
                                isBlur = false
                            } else {
                                binding.topicIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                                isBlur = true
                            }
                            if (isFilterDataPresent) {
                                viewmodel.dataGetAllTopicListBySection(
                                    filterValues.section_id ?: ""
                                )
                            } else {
                                gameSubjectSectionTopic?.topic_list?.forEach {
                                    if (it.section_id == spinnerAdapterSection.getSelectedId()) {
                                        topicList.add(BaseItem(it.topic_id, it.topic_name))
                                    }
                                }
                                initMakeChoiceItemSpinner(
                                    binding.spTopic,
                                    "Select Topic",
                                    topicList
                                )
                            }

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

            when (sp) {
                binding.spSubject -> spinnerAdapterSubject = sa
                binding.spSection -> spinnerAdapterSection = sa
                binding.spTopic -> spinnerAdapterTopic = sa
                binding.spBatch -> spinnerAdapterBatch = sa
                binding.spDifficulty -> spinnerAdapterDifficulty = sa
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun navigateToHome() {

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetGameSubjectSectionTopic" -> {
                val type = object : TypeToken<BaseModel<GameSubjectSectionTopic>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameSubjectSectionTopic>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        gameSubjectSectionTopic = baseData.data
                        subjectList.clear()
                        gameSubjectSectionTopic?.let {
                            it.subject_list.forEach {
                                subjectList.add(BaseItem(it.subject_id, it.subject_name))
                            }
                            initMakeChoiceItemSpinner(
                                binding.spSubject,
                                "Select Subject",
                                subjectList
                            )
                            binding.makeChoiceLayout.visibility = View.VISIBLE
                        }
                    }
                }
            }
            "apiGetBatchByCategory" -> {
                val type = object : TypeToken<BaseModel<java.util.ArrayList<BaseItem>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<java.util.ArrayList<BaseItem>>>(
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
            "apiGetGameSolution" -> {
                val type = object : TypeToken<BaseModel<GameSolutionResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameSolutionResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            if (baseData.data?.data != null && baseData.data?.data?.size ?: 0 > 0) {
                                initQuestion(baseData.data?.data as java.util.ArrayList<Question>)
                                hasNextPage = baseData.data?.next_page != 0
                                page = baseData.data?.next_page ?: 0
                                binding.rvErrorQuestions.visibility = View.VISIBLE
                                binding.emptyMessage.visibility = View.GONE
                            } else {
                                binding.rvErrorQuestions.visibility = View.GONE
                                binding.emptyMessage.visibility = View.VISIBLE
                            }
                        } else {
                            showToast(this, baseData.message[0])
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
                        binding.rvErrorQuestions.stopScroll()
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
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
                        binding.rvErrorQuestions.stopScroll()
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.showSectionlayout -> {
                if (binding.spSectionlayout.isVisible && binding.spTopicLayout.isVisible) {
                    binding.spSectionlayout.visibility = View.GONE
                    binding.spTopicLayout.visibility = View.GONE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)
                } else {
                    binding.spSectionlayout.visibility = View.VISIBLE
                    binding.spTopicLayout.visibility = View.VISIBLE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_substract)
                }
            }

            binding.btnMakeChoice -> {
                binding.llMakeChoice.visibility = View.VISIBLE

                errorQuestions.clear()
                page = 1
                viewmodel.apiGetGameSolution(
                    page.toString(),
                    dataManager.mPref.prefGetUserInfo().id,
                    "",
                    "profile",
                    filterValues,
                    this
                )
                binding.spSectionlayout.visibility = View.GONE
                binding.spTopicLayout.visibility = View.GONE
                binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)
            }
        }
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "insertAllTemp" -> {
                hideProgressDialog()
                val intent = Intent(
                    this,
                    QuestionDetailsActivity::class.java
                )
                intent.putExtra("position", questionPosition)
                intent.putExtra("page", page)
                intent.putExtra("question_type", ConstantField.QUESTION_TYPE_ERROR)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("hasNextPage", hasNextPage)
                startActivityForResult(intent, 2)
            }
            "checkFilterValuesAvailable" -> {
                val subCount = result as Int
                if (subCount > 0) {
                    //initMakeChoiceItemSpinner()
                    isFilterDataPresent = true
                    viewmodel.dataGetSubjectListByCategory()
                } else {
                    viewmodel.apiGetGameSubjectSectionTopic(
                        dataManager.mPref.prefGetUserInfo().category_id.toString(),
                        this
                    )
                }
            }
            "dataGetSubjectListByCategory" -> {
                val subjectListDTO = result as List<SubjectDTO>
                val subjectList = SubjectDTO.toSubjects(subjectListDTO)
                initMakeChoiceItemSpinner(
                    binding.spSubject,
                    "Select Subject",
                    subjectList as java.util.ArrayList<BaseItem>
                )
            }
            "dataGetAllSectionListBySubject" -> {
                val sectionListDTO = result as List<SectionDTO>
                val sectionList = SectionDTO.toSections(sectionListDTO)
                initMakeChoiceItemSpinner(
                    binding.spSection,
                    "Select Section",
                    sectionList as java.util.ArrayList<BaseItem>
                )
            }
            "dataGetAllTopicListBySection" -> {
                val topicListDTO = result as List<TopicDTO>
                val topicList = TopicDTO.toTopics(topicListDTO)
                initMakeChoiceItemSpinner(
                    binding.spTopic,
                    "Select Topic",
                    topicList as java.util.ArrayList<BaseItem>
                )
            }
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "apiGetGameSolution") {
            if (page == 1) {
                super.onLoading(isLoader, key)
            } else if (page != 1) {
                if (isLoader) {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                } else {
                    binding.loadingProgressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
        println("$optName -> $result")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            val bookmarkMap = data!!.getSerializableExtra("bookmark_map") as HashMap<Int, Int>
            refineQuestionList(bookmarkMap)
        }
    }

    private fun refineQuestionList(bookmarkMap: HashMap<Int, Int>) {
        bookmarkMap.forEach {
            val pos = if (it.key >= ConstantField.NATIVE_AD_INTERVAL) {
                it.key + 1
            } else {
                it.key
            }
            this.errorQuestions[pos].is_bookmarked = it.value
        }
        binding.rvErrorQuestions.adapter?.notifyDataSetChanged()

    }
}