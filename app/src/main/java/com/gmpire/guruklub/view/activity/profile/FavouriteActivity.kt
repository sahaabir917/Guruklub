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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.EmptyModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.library.Common
import com.gmpire.guruklub.data.model.library.FilterValues
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.databinding.ActivityFavouriteBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.NativeAdViewHolder
import com.gmpire.guruklub.view.activity.library.QuestionDetailsActivity
import com.gmpire.guruklub.view.activity.viewSolutions.ViewSolutionQuestionsViewHolder
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.base.DATA_VIEW_TYPE
import com.gmpire.guruklub.view.fragment.favourite.FavouriteViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import com.michaelflisar.rxbus2.RxBusBuilder
import com.michaelflisar.rxbus2.rx.RxBusMode
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response

class FavouriteActivity : BaseActivity(), IDatabaseCallBack {
    private var isBlur: Boolean = false
    private lateinit var binding: ActivityFavouriteBinding
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
    private var hasNextPage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_favourite)
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
        binding.showSectionlayout.setOnClickListener(this)
        binding.llMakeChoice.visibility = View.VISIBLE
        binding.ivDropdownArrow.rotation = 360.0.toFloat()

        binding.relativeLayoutSubject.setOnClickListener {
            binding.spSubject.performClick()
        }

        binding.spSectionlayout.setOnClickListener {
            binding.spSection.performClick()
        }

        binding.spTopicLayout.setOnClickListener {
            binding.spTopic.performClick()
        }

        setToolbar(this, binding.toolbar, "Bookmarks", true)
    }

    private fun insertEmptyQuestion(questionList: ArrayList<Question>): ArrayList<Question> {
        /*var placement = ConstantField.NATIVE_AD_INTERVAL
        var increment = 0
        if (favouriteQuestions.isNotEmpty())
            placement =
                ConstantField.NATIVE_AD_INTERVAL - (favouriteQuestions.size % (ConstantField.NATIVE_AD_INTERVAL + 1))*/
        val questionSize = questionList.size
        if (questionSize > 5) {
            val emptyQues = Question()
            emptyQues.answer = 101 // To mark as ad question
            questionList.add(5, emptyQues)
        }

        return questionList
    }

    private fun initQuestion(favouriteQuestionsLoc: ArrayList<Question>) {
        if (page != 1) {
            this.favouriteQuestions.addAll(favouriteQuestionsLoc)
            binding.rvFavouriteQuestions.adapter?.notifyDataSetChanged()
        } else {
            favouriteQuestions.clear()
            this.favouriteQuestions = insertEmptyQuestion(favouriteQuestionsLoc)
            binding.rvFavouriteQuestions.layoutManager = LinearLayoutManager(this)
            binding.rvFavouriteQuestions.adapter =
                BaseRecyclerAdapter(this, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {
                        model as Question
                        when (view.id) {
                            R.id.question_root_layout -> {
                                viewmodel.dataDeleteAllTempQuestions()
                                viewmodel.insertAllTemp(favouriteQuestions.filter { it.answer != 101 } as ArrayList<Question>)
                                questionPosition =
                                    if (position > ConstantField.NATIVE_AD_INTERVAL) {
                                        position - 1
                                    } else {
                                        position
                                    }
                            }
                            R.id.bookmark_iv -> {
                                removePosition = position
                                viewmodel.apiUnBookmarkQuestion(
                                    model.id,
                                    filterValues.category_id.toString(),
                                    this@FavouriteActivity
                                )
                                favouriteQuestions.removeAt(removePosition)
                                if (favouriteQuestions.size == 0) {
                                    binding.rvFavouriteQuestions.visibility = View.GONE
                                    binding.emptyMessage.visibility = View.VISIBLE
                                }
                                RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED)
                                    .send(UpdateClass())
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
                                , this@FavouriteActivity
                                , "Bookmark"
                            )
                        } else {
                            return NativeAdViewHolder(
                                DataBindingUtil.inflate(
                                    LayoutInflater.from(parent.context)
                                    , R.layout.item_empty_native_ads
                                    , parent, false
                                )
                                , this@FavouriteActivity
                            )
                        }
                    }

                    override fun loadMoreItem() {
                        if (hasNextPage) {
                            viewmodel.apiGetFavouriteQuestion(
                                page.toString(),
                                filterValues,
                                dataManager.mPref.prefGetUserInfo().id,
                                this@FavouriteActivity
                            )
                        }
                    }
                }, favouriteQuestions)
        }
    }

    fun initMakeChoiceItemSpinner(sp: View, hint: String, item: ArrayList<Common>) {
        sp as AppCompatSpinner
        var sa = this?.let {
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
                sa?.setPosition(position)
                when (sp) {
                    binding.spSubject -> {
                        viewmodel.apiGetSectionBySubject(
                            spinnerAdapterSubject.getSelectedId(),
                            this@FavouriteActivity
                        )
                        filterValues.subject_id = spinnerAdapterSubject.getSelectedId()
                        if (filterValues.subject_id != "0") {
                            binding.sectionIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                            isBlur = true
                        } else {
                            binding.sectionIcon.setImageResource(R.drawable.blur_down_arrow)
                            isBlur = false
                        }
                    }
                    binding.spSection -> {
                        viewmodel.apiGetTopicBySection(
                            spinnerAdapterSection.getSelectedId(),
                            this@FavouriteActivity
                        )
                        filterValues.section_id = spinnerAdapterSection.getSelectedId()
                        if (filterValues.section_id == "0") {
                            binding.topicIcon.setImageResource(R.drawable.blur_down_arrow)
                            isBlur = false
                        } else {
                            binding.topicIcon.setImageResource(R.drawable.ic_down_arrow_black_png)
                            isBlur = true
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

        try {
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
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "apiGetFavouriteQuestion") {
            if (page == 1) {
                super.onLoading(isLoader, key)
            }
            if (page != 1) {
                if (isLoader) {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                } else {
                    binding.loadingProgressBar.visibility = View.GONE
                }
            }
        } else if (key == "apiUnBookmarkQuestion") {
            if (isLoader) {
                showProgressDialog("Please wait")
            } else {
                hideProgressDialog()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.makeChoiceLayout -> {
            }

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
                favouriteQuestions.clear()
                page = 1
                viewmodel.apiGetFavouriteQuestion(
                    page.toString(),
                    filterValues,
                    dataManager.mPref.prefGetUserInfo().id, this
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
                val intent = Intent(this, QuestionDetailsActivity::class.java)
                intent.putExtra("position", questionPosition)
                intent.putExtra("page", page)
                intent.putExtra("question_type", ConstantField.QUESTION_TYPE_FAVOURITE)
                intent.putExtra("filterValues", Gson().toJson(filterValues))
                intent.putExtra("hasNextPage", hasNextPage)
                startActivity(intent)
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
            this.favouriteQuestions[pos].is_bookmarked = it.value
        }
        binding.rvFavouriteQuestions.adapter?.notifyDataSetChanged()

    }
}