package com.gmpire.guruklub.view.activity.viewSolutions

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
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.data.model.viewSolution.GameSolutionResponse
import com.gmpire.guruklub.data.model.viewSolution.GameSubjectSectionTopic
import com.gmpire.guruklub.databinding.ActivityViewSolutionsBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.library.QuestionDetailsActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
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

class ViewSolutionsActivity : BaseActivity(), IDatabaseCallBack {

    private var isBlur: Boolean = false
    private var favouritePosition: Int = -1
    private var hasNextPage: Boolean = false
    private lateinit var binding: ActivityViewSolutionsBinding
    private lateinit var viewModel: ViewSolutionsViewModel
    private var questions: MutableLiveData<ArrayList<Question>> = MutableLiveData()
    lateinit var spinnerAdapterSubject: CustomSpinnerAdapter
    lateinit var spinnerAdapterSection: CustomSpinnerAdapter
    lateinit var spinnerAdapterTopic: CustomSpinnerAdapter
    lateinit var spinnerAdapterBatch: CustomSpinnerAdapter
    lateinit var spinnerAdapterDifficulty: CustomSpinnerAdapter
    lateinit var spinnerAdapterType: CustomSpinnerAdapter
    lateinit var game_id: String
    var subjectList = ArrayList<Common>()
    var sectionList = ArrayList<Common>()
    var topicList = ArrayList<Common>()
    lateinit var gameSubjectSectionTopic: GameSubjectSectionTopic
    private var viewSolutionQuestions: ArrayList<Question> = arrayListOf()
    private var viewSolutionAllTest: ArrayList<Question> = arrayListOf()
    var questionPosition: Int = -1

    var page: Int = 1

    val filterValues = FilterValues()
    private lateinit var categoryId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_solutions)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(ViewSolutionsViewModel::class.java)
    }

    override fun viewRelatedTask() {
        categoryId = dataManager.mPref.prefGetUserInfo().category_id.toString()
        setToolbar(this, binding.toolbar, "View Solutions", true, false)

        game_id = intent.getStringExtra("game_id")
        viewModel.iDatabaseCallBack = this

        val item = ArrayList<Common>()
        item.add(Common("1", "Basic"))
        item.add(Common("2", "Intermediate"))
        item.add(Common("3", "Advanced"))

        initMakeChoiceItemSpinner(binding.spDifficulty, "Select Difficulty", item)

        val items = ArrayList<Common>()
        items.add(Common("correct", "Correct Answers"))
        items.add(Common("wrong", "Wrong Answers"))
        items.add(Common("noanswer", "Unanswered Questions"))

        initMakeChoiceItemSpinner(binding.spQuestionFilter, "All Questions", items)

        page = 1

        binding.rlSubject.setOnClickListener {
            binding.spSubject.performClick()
        }

        binding.rlSection.setOnClickListener {
            binding.spSection.performClick()
        }

        binding.rlTopic.setOnClickListener {
            binding.spTopic.performClick()
        }


        if (game_id == "-2") {
            binding.rlSubject.visibility = View.GONE
            binding.rlSection.visibility = View.GONE
            binding.rlTopic.visibility = View.GONE

            try {
                val questionsJson = intent.getStringExtra("questions")
                val quesListType = object : TypeToken<ArrayList<Question>>() {}.type
                val passedQuestions =
                    Gson().fromJson<ArrayList<Question>>(questionsJson, quesListType)
                viewSolutionAllTest.addAll(passedQuestions)
                initQuestion(passedQuestions)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } else {
            viewModel.apiGetGameSubjectSectionTopic(game_id, this)
            viewModel.apiGetGameSolution(
                page.toString(),
                dataManager.mPref.prefGetUserInfo().id,
                game_id,
                "game",
                filterValues,
                this
            )
        }

        binding.btnMakeChoice.setOnClickListener(this)
        binding.showSectionlayout.setOnClickListener(this)
        binding.llMakeChoice.visibility = View.VISIBLE


    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
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
                        try {
                            gameSubjectSectionTopic = baseData.data!!
                            subjectList.clear()
                            gameSubjectSectionTopic.subject_list.forEach {
                                subjectList.add(Common(it.subject_id, it.subject_name))
                            }

                            baseData.data?.let {
                                initMakeChoiceItemSpinner(
                                    binding.spSubject,
                                    "Select Subject",
                                    subjectList
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
            "apiGetBatchByCategory" -> {
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
                                binding.spBatch,
                                "Select Batch",
                                baseData.data
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
                        baseData.data?.let {
                            if (baseData.data?.show_details == 1) {
                                it.data?.let {
                                    initQuestion(baseData.data?.data as ArrayList<Question>)
                                    hasNextPage = baseData.data?.next_page != 0
                                    page = baseData.data?.next_page ?: 0
                                    binding.rvSolutionQuestions.visibility = View.VISIBLE
                                    binding.emptyMessage.visibility = View.GONE
                                } ?: kotlin.run {
                                    binding.rvSolutionQuestions.visibility = View.GONE
                                    binding.emptyMessage.visibility = View.VISIBLE
                                    hasNextPage = false
                                    page = 0
                                }
                            } else {
                                binding.cardViewViewSolutionsInfo.visibility = View.VISIBLE
                                binding.cardViewliveexaminfo.visibility = View.VISIBLE
                                //binding.emptyMessage.visibility = View.VISIBLE
                            }
                        } ?: kotlin.run {
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
                        this.viewSolutionQuestions[favouritePosition].is_bookmarked = 1
                        binding.rvSolutionQuestions.adapter?.notifyItemChanged(favouritePosition)
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
                        this.viewSolutionQuestions[favouritePosition].is_bookmarked = 0
                        binding.rvSolutionQuestions.adapter?.notifyItemChanged(favouritePosition)
                    }
                }
            }
        }
    }

    private fun insertEmptyQuestion(questionList: ArrayList<Question>): ArrayList<Question> {
        /* var placement = ConstantField.NATIVE_AD_INTERVAL
         if (viewSolutionQuestions.isNotEmpty())
             placement =
                 ConstantField.NATIVE_AD_INTERVAL - (viewSolutionQuestions.size % (ConstantField.NATIVE_AD_INTERVAL + 1))*/

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


    private fun initQuestion(viewSolutionQuestions: ArrayList<Question>) {
        if (page != 1) {
            this.viewSolutionQuestions.addAll(viewSolutionQuestions)
            binding.rvSolutionQuestions.adapter?.notifyDataSetChanged()
        } else {
            this.viewSolutionQuestions = insertEmptyQuestion(viewSolutionQuestions)
            binding.rvSolutionQuestions.layoutManager = LinearLayoutManager(this)
            binding.rvSolutionQuestions.adapter =
                BaseRecyclerAdapter(this, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {
                        model as Question
                        when (view.id) {
                            R.id.question_root_layout -> {
                                viewModel.dataDeleteAllTempQuestions()
                                viewModel.insertAllTemp(viewSolutionQuestions.filter { it.answer != 101 } as ArrayList<Question>)
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
                                    viewModel.apiBookmarkQuestion(
                                        model.id, categoryId,
                                        this@ViewSolutionsActivity
                                    )
                                } else {
                                    viewModel.apiUnBookmarkQuestion(
                                        model.id, categoryId,
                                        this@ViewSolutionsActivity
                                    )
                                }
                            }

//                            R.id.bookmark_iv2 -> {
//                                favouritePosition = position
//                                if (model.is_bookmarked == 0) {
//                                    viewModel.apiBookmarkQuestion(
//                                        model.id, categoryId,
//                                        this@ViewSolutionsActivity
//                                    )
//                                } else {
//                                    viewModel.apiUnBookmarkQuestion(
//                                        model.id, categoryId,
//                                        this@ViewSolutionsActivity
//                                    )
//                                }
//                            }

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
                                , this@ViewSolutionsActivity
                                , "viewsolution"
                            )
                        } else {
                            return NativeAdViewHolder(
                                DataBindingUtil.inflate(
                                    LayoutInflater.from(parent.context)
                                    , R.layout.item_empty_native_ads
                                    , parent, false
                                )
                                , this@ViewSolutionsActivity
                            )
                        }
                    }

                    override fun loadMoreItem() {
                        if (hasNextPage) {
                            viewModel.apiGetGameSolution(
                                page.toString(),
                                dataManager.mPref.prefGetUserInfo().id,
                                game_id,
                                "game",
                                filterValues,
                                this@ViewSolutionsActivity
                            )
                        }
                    }
                }, this.viewSolutionQuestions)
        }
    }


    fun initMakeChoiceItemSpinner(sp: View, hint: String, item: ArrayList<Common>?) {
        sp as AppCompatSpinner
        val sa: CustomSpinnerAdapter

        sa =
            CustomSpinnerAdapter(
                this,
                GeneratDropdownItem.getDropdownItems(
                    JSONArray(Gson().toJson(item)),
                    "name",
                    "id",
                    hint
                )
            )
        if (hint == "Select Subject" || hint == "All Questions") {
            isBlur = true
        }
        sp.setAdapter(sa.getAdapter(isBlur))
        sp.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
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
                        gameSubjectSectionTopic.section_list.forEach {
                            if (it.subject_id == spinnerAdapterSubject.getSelectedId()) {
                                sectionList.add(Common(it.section_id, it.section_name))
                            }
                        }

                        initMakeChoiceItemSpinner(binding.spSection, "Select Section", sectionList)

                    }
                    binding.spSection -> {

                        filterValues.section_id = spinnerAdapterSection.getSelectedId()
                        if (filterValues.section_id == "0") {
                            binding.topicIcon.setImageResource(R.drawable.blur_down_arrow)
                            isBlur = false
                        } else {
                            binding.topicIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                            isBlur = true
                        }
                        topicList.clear()
                        gameSubjectSectionTopic.topic_list.forEach {
                            if (it.section_id == spinnerAdapterSection.getSelectedId()) {
                                topicList.add(Common(it.topic_id, it.topic_name))
                            }
                        }
                        initMakeChoiceItemSpinner(binding.spTopic, "Select Topic", topicList)

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
                    binding.spQuestionFilter -> {
                        filterValues.type = spinnerAdapterType.getSelectedId()
                    }

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        })

        when (sp) {
            binding.spSubject -> spinnerAdapterSubject = sa
            binding.spSection -> spinnerAdapterSection = sa
            binding.spTopic -> spinnerAdapterTopic = sa
            binding.spBatch -> spinnerAdapterBatch = sa
            binding.spDifficulty -> spinnerAdapterDifficulty = sa
            binding.spQuestionFilter -> spinnerAdapterType = sa
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

            binding.linearlayout18 -> {
                if (binding.linearlayout18.isVisible) {
                    binding.linearlayout18.visibility = View.GONE
                    binding.llMakeChoice.visibility = View.VISIBLE
                }
            }

            binding.showSectionlayout -> {
                if (binding.rlSection.isVisible && binding.rlTopic.isVisible && binding.rlSubject.isVisible) {
                    binding.rlSection.visibility = View.GONE
                    binding.rlTopic.visibility = View.GONE
                    binding.rlSubject.visibility = View.GONE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)
                } else {
                    binding.rlSection.visibility = View.VISIBLE
                    binding.rlTopic.visibility = View.VISIBLE
                    binding.rlSubject.visibility = View.VISIBLE
                    binding.imageViewFilterToggle.setImageResource(R.drawable.ic_substract)
                }
            }

            binding.btnMakeChoice -> {
//                binding.llMakeChoice.visibility = View.GONE
//                binding.ivDropdownArrow.rotation = 360.0.toFloat()
                viewSolutionQuestions.clear()
                page = 1

                // test exam
                if (game_id == "-2") {
                    filterOnlyByQuestionType(filterValues.type)
                } else {
                    viewModel.apiGetGameSolution(
                        page.toString(),
                        dataManager.mPref.prefGetUserInfo().id,
                        game_id,
                        "game",
                        filterValues,
                        this
                    )
                }
                binding.rlTopic.visibility = View.GONE
                binding.rlSection.visibility = View.GONE
                binding.rlSubject.visibility = View.GONE
                binding.allquestion.visibility = View.VISIBLE
                binding.imageViewFilterToggle.setImageResource(R.drawable.ic_plus)
            }
        }
    }

    private fun filterOnlyByQuestionType(type: String?) {
        if (type == "0") {
            viewSolutionQuestions.clear()
            viewSolutionQuestions.addAll(viewSolutionAllTest)
            binding.rvSolutionQuestions.adapter?.notifyDataSetChanged()
            return
        }

        val filtered = viewSolutionAllTest.filter { it.answer_type == type }
        viewSolutionQuestions.clear()
        viewSolutionQuestions.addAll(filtered)
        binding.rvSolutionQuestions.adapter?.notifyDataSetChanged()
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "apiGetGameSolution") {
            if (page == 1) {
                if (isLoader) {
                    showProgressDialog("please wait")
                } else {
                    hideProgressDialog()
                }
            } else {
                if (isLoader) {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                } else {
                    binding.loadingProgressBar.visibility = View.GONE
                }
            }
        } else if (key == "apiUnBookmarkQuestion" || key == "apiBookmarkQuestion") {
            if (isLoader) {
                showProgressDialog("Please wait")
            } else {
                hideProgressDialog()
            }
        }
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "insertAllTemp" -> {
                hideProgressDialog()
                val intent = Intent(this, QuestionDetailsActivity::class.java)
                intent.putExtra("position", questionPosition)
                intent.putExtra("page", page)
                intent.putExtra("question_type", ConstantField.QUESTION_TYPE_VIEW_SOLUTION)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("game_id", game_id)
                intent.putExtra("hasNextPage", hasNextPage)
                startActivityForResult(intent, 2)
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
        print(optName + result.toString())
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
            viewSolutionQuestions[pos].is_bookmarked = it.value
        }
        binding?.rvSolutionQuestions.adapter?.notifyDataSetChanged()

    }


}
