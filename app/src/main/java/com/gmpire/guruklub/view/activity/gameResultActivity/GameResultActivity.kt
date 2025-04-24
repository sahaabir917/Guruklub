package com.gmpire.guruklub.view.activity.gameResultActivity

import android.content.Intent
import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.game.GameResultResponse
import com.gmpire.guruklub.data.model.game.GameResultSubmitRequestItem
import com.gmpire.guruklub.data.model.game.SubjectBasedPerformance
import com.gmpire.guruklub.databinding.ActivityGameResultBinding
import com.gmpire.guruklub.util.DateUtil.Companion.simpleDateFormat
import com.gmpire.guruklub.util.DateUtil.Companion.simpleDateFormatServer
import com.gmpire.guruklub.view.activity.subjectBasedPerformace.SubjectBasedPerformanceActivity
import com.gmpire.guruklub.view.activity.viewSolutions.ViewSolutionsActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.gmpire.guruklub.data.model.question.Question
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.google.android.gms.ads.*
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.michaelflisar.rxbus2.RxBus
import okhttp3.ResponseBody
import org.apache.http.impl.cookie.DateParseException
import retrofit2.Response
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GameResultActivity : BaseActivity(), View.OnClickListener {

    private lateinit var gameResultResponse: GameResultResponse
    private lateinit var viewModel: GameResultActivityViewModel
    private lateinit var result: GameResultSubmitRequestItem
    private lateinit var binding: ActivityGameResultBinding
    private lateinit var from_activity: String
    private lateinit var passedQuestions: ArrayList<Question>

    private lateinit var manager: ReviewManager
    private lateinit var reviewInfo: ReviewInfo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game_result)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(GameResultActivityViewModel::class.java)
        binding.viewSolutionTv.setOnClickListener(this)
    }

    private fun setUpResultChart(gameResultResponse: GameResultResponse) {
        binding.resultPieChart.setUsePercentValues(true)
        val xvalues = ArrayList<PieEntry>()
        xvalues.add(
            PieEntry(
                gameResultResponse.correct_answer.toFloat(),
                "Correct(${gameResultResponse.correct_answer})"
            )
        )
        xvalues.add(
            PieEntry(
                gameResultResponse.wrong_answer.toFloat(),
                "Wrong(${gameResultResponse.wrong_answer})"
            )
        )
        xvalues.add(
            PieEntry(
                gameResultResponse.un_answer.toFloat(),
                "Unanswered(${gameResultResponse.un_answer})"
            )
        )
        val dataSet = PieDataSet(xvalues, "")
        val data = PieData(dataSet)
        val colors = ArrayList<Int>()
        this.let { ContextCompat.getColor(it, R.color.paleBlue) }.let { colors.add(it) }
        this.let { ContextCompat.getColor(it, R.color.darkOrange) }.let { colors.add(it) }
        this.let { ContextCompat.getColor(it, R.color.paleOrange) }.let { colors.add(it) }

        dataSet.colors = colors

        // In Percentage
        data.setValueFormatter(ChartValueFormatter())

        binding.resultPieChart.data = data
        binding.resultPieChart.description.text = ""
        binding.resultPieChart.isDrawHoleEnabled = false
        data.setValueTextSize(13f)

        // binding.resultPieChart.setOnChartValueSelectedListener(this)
        chartDetails(binding.resultPieChart, Typeface.SANS_SERIF)
    }

    fun chartDetails(mChart: PieChart, tf: Typeface) {
        //  mChart.description.isEnabled = true
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING

        var score =
            "Score\n${df.format((gameResultResponse.get_point / gameResultResponse.total_point.toDouble()) * 100)}%"

        mChart.centerText =
            this?.let { ContextCompat.getColor(it, R.color.colorPrimary) }?.let {
                StringUtil.getInstance().getSS(score).addSizeSpan(1.2f, 0, score.length)
                    .addStyleSpan(
                        Typeface.BOLD, 0, score.length
                    ).addForegroundColorSpan(
                        it,
                        5,
                        score.length
                    ).addSizeSpan(1.2f, 5, score.length).ss
            }


//        mChart.setCenterTextSize(10F)
        mChart.setCenterTextTypeface(tf)
        mChart.isDrawHoleEnabled = true
        val l = mChart.legend
//        mChart.legend.isWordWrapEnabled = true
//        mChart.legend.isEnabled = false
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
//        l.formSize = 20F
//        l.formToTextSpace = 5f
//        l.form = Legend.LegendForm.SQUARE
//        l.textSize = 12f
        l.orientation = Legend.LegendOrientation.HORIZONTAL
//        l.isWordWrapEnabled = true
        //  l.setDrawInside(false)
        //      mChart.setTouchEnabled(false)
        mChart.setDrawEntryLabels(false)
        mChart.setExtraOffsets(20f, 0f, 20f, 0f)
        mChart.setUsePercentValues(true)
        // mChart.rotationAngle = 0f
        mChart.setUsePercentValues(true)
        mChart.setDrawCenterText(true)
//        mChart.animateX(1000)

        //  mChart.description.isEnabled = true
        mChart.isRotationEnabled = true

        mChart.notifyDataSetChanged();
        mChart.invalidate();

        binding.totalQuestionNoTv.text =
            "Total Number Of Questions : ${gameResultResponse.total_question}"
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Exam Result", true)

        from_activity = intent.getStringExtra("from_activity")

        // Review info
        manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                reviewInfo = request.result
            }
        }

        when (from_activity) {
            "game" -> {
                result = intent.getSerializableExtra("result") as GameResultSubmitRequestItem
                result.category_id = dataManager.mPref.prefGetUserInfo().category_id
                viewModel.apiGameResult(result, this)
            }
            "profile" -> {
                val game_id = intent.getStringExtra("game_id")
                viewModel.apiGameResultByGameId(game_id ?: "", this)
            }
            "model_test" -> {
                result = intent.getSerializableExtra("result") as GameResultSubmitRequestItem
                result.category_id = dataManager.mPref.prefGetUserInfo().category_id
                setToolbar(this, binding.toolbar, "Live Exam Result", true)
                if (result.model_test_id == null || result.model_test_id == "0") {
                    var model_test_id = dataManager.mPref.getModelTestId()
                    result.model_test_id = model_test_id
                    viewModel.apiGameResult(result, this)
                    dataManager.mPref.setModeltestid("null")
                } else {
                    viewModel.apiGameResult(result, this)
                    dataManager.mPref.setModeltestid("null")
                }
                if (::reviewInfo.isInitialized) {
                    manager.launchReviewFlow(this, reviewInfo)
                }
            }
            "test" -> {
                result = intent.getSerializableExtra("result") as GameResultSubmitRequestItem
                setToolbar(this, binding.toolbar, "Test Exam Result", true)

                binding.performanceListTitle.visibility = View.GONE
                binding.linearLayoutPerformanceList.visibility = View.GONE
                binding.showAllBtn.visibility = View.GONE

                binding.cardViewTestInfo.visibility = View.VISIBLE

                val questionsJson = intent.getStringExtra("questions")
                val quesListType = object : TypeToken<ArrayList<Question>>() {}.type
                passedQuestions = Gson().fromJson<ArrayList<Question>>(questionsJson, quesListType)

                produceOfflineResult()
            }
        }

        binding.performanceSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> {
                            binding.performanceRecView.visibility = View.GONE
                        }
                        1 -> {
                            initSubjectBasedPerformance()
                            binding.performanceRecView.visibility = View.VISIBLE
                        }
                        2 -> {
                            initSectionBasedPerformance()
                            binding.performanceRecView.visibility = View.VISIBLE
                        }
                        3 -> {
                            initTopicBasedPerformance()
                            binding.performanceRecView.visibility = View.VISIBLE
                        }
                    }
                }
            }


        binding.showAllBtn.setOnClickListener {
            if (this::gameResultResponse.isInitialized) {
                startActivity(
                    Intent(
                        this,
                        SubjectBasedPerformanceActivity::class.java
                    ).putExtra("game_id", gameResultResponse.game_id.toString())
                )
            }
        }

//        binding.goBackBtn.setOnClickListener {
//            onBackPressed()
//        }

    }

    private fun produceOfflineResult() {
        val totalQues = result.questions?.size ?: 0
        var correctAns = 0
        var unasnwered = 0

        for ((index, it) in result.questions!!.withIndex()) {
            if (it.answer == -1) {
                unasnwered++
                passedQuestions[index].answer_type = "noanswer"
                continue
            }
            if (it.answer == (it.correctAnswer + 1)) {
                correctAns++
                passedQuestions[index].answer_type = "correct"
            } else {
                passedQuestions[index].answer_type = "wrong"
            }
        }

        val wrongAnswer = totalQues - (correctAns + unasnwered)
        val getPoint = correctAns - (wrongAnswer * 0.5)
        gameResultResponse = GameResultResponse(
            correctAns,
            simpleDateFormatServer.format(Date()).toString(),
            -2,
            getPoint,
            listOf(),
            listOf(),
            listOf(),
            totalQues.toString(),
            totalQues.toString(),
            6,
            unasnwered,
            wrongAnswer
        )
        publishResult()
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun initSubjectBasedPerformance() {
        if (!this::gameResultResponse.isInitialized)
            return

        binding.suggestionTv.visibility = View.GONE
        binding.performanceRecView.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return SubjectPerformaceItemViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                this@GameResultActivity
                            ), R.layout.item_subject_based_performance_short, parent, false
                        ), this@GameResultActivity
                    )
                }

                override fun loadMoreItem() {
                }

            }, gameResultResponse.subject_based_performance as ArrayList<SubjectBasedPerformance>)
    }

    private fun initSectionBasedPerformance() {
        if (!this::gameResultResponse.isInitialized)
            return

        binding.suggestionTv.visibility = View.GONE
        binding.performanceRecView.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return SectionPerformaceItemViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                this@GameResultActivity
                            ), R.layout.item_subject_based_performance_short, parent, false
                        ), this@GameResultActivity
                    )
                }

                override fun loadMoreItem() {
                }

            }, gameResultResponse.section_based_performance as ArrayList<SubjectBasedPerformance>)
    }

    private fun initTopicBasedPerformance() {
        if (!this::gameResultResponse.isInitialized)
            return

        binding.suggestionTv.visibility = View.VISIBLE
        binding.performanceRecView.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return TopicPerformaceItemViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                this@GameResultActivity
                            ), R.layout.item_topic_based_performance_short, parent, false
                        ), this@GameResultActivity
                    )
                }

                override fun loadMoreItem() {
                }

            }, gameResultResponse.topic_based_performance as ArrayList<SubjectBasedPerformance>)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGameResult" -> {
                val type = object : TypeToken<BaseModel<GameResultResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameResultResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        RxBus.get().withKey(RxBusEvents.EXAM_ATTENDED).send(
                            UpdateClass()
                        )
                        RxBus.get().withKey(RxBusEvents.FAVOURITE_CHANGED).send(
                            UpdateClass()
                        )
                        if (baseData.data != null) {
                            try {
                                gameResultResponse = baseData.data!!
                                publishResult()
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }

            "apiGameResultByGameId" -> {
                val type = object : TypeToken<BaseModel<GameResultResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<GameResultResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            try {
                                gameResultResponse = baseData.data!!
                                publishResult()
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }
    }

    private fun publishResult() {
        try {
            binding.dateTv.text =
                simpleDateFormat.format(simpleDateFormatServer.parse(gameResultResponse.game_date))
                    .toString()
        } catch (e: DateParseException) {
            e.printStackTrace()
        } catch (e: DateParseException) {
            e.printStackTrace()
        }

        try {
            binding.overallTimeTv.text =
                "Total time spent : " + SimpleDateFormat("mm:ss").format(Date(gameResultResponse.total_time.toLong()))
        } catch (e: DateParseException) {
            e.printStackTrace()
        } catch (e: DateParseException) {
            e.printStackTrace()
        }
        binding.pointsTv.text =
            "Marks : ${gameResultResponse.get_point}/${gameResultResponse.total_point}"

        binding.performanceRecView.layoutManager =
            LinearLayoutManager(this)
        binding.performanceRecView.setHasFixedSize(true)

        //binding.performanceSpinner.setSelection(1)
        if (gameResultResponse.section_based_performance.isNotEmpty()) {
            initSubjectBasedPerformance()
            binding.performanceRecView.visibility = View.VISIBLE
        }

        setUpResultChart(gameResultResponse)

    }

    override fun onClick(v: View?) {
        when (v) {
            binding.viewSolutionTv -> {
                if (this::gameResultResponse.isInitialized) {
                    val intent = Intent(this, ViewSolutionsActivity::class.java)
                        .putExtra(
                            "game_id", gameResultResponse.game_id.toString()
                        )

                    // test exam
                    if (gameResultResponse.game_id == -2) {
                        intent.putExtra("questions", Gson().toJson(passedQuestions))
                    }

                    startActivity(intent)
                }
            }
        }
    }
}


