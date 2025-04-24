package com.gmpire.guruklub.view.activity.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.gmpire.guruklub.BuildConfig
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.local_db.dto.GameHeartDTO
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.login.UserInfo
import com.gmpire.guruklub.data.model.profile.performance.PerformanceSummery
import com.gmpire.guruklub.data.model.profile.performance.SubjectWisePerformance
import com.gmpire.guruklub.databinding.ActivityProfileBinding
import com.gmpire.guruklub.util.*
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService
import com.gmpire.guruklub.view.activity.forgetPassword.ForgetPasswordActivity
import com.gmpire.guruklub.view.activity.gameResultActivity.GameResultActivity
import com.gmpire.guruklub.view.activity.gamelevel.GAME_HEART_MINUS
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.profileSetup.ProfileSetupActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.michaelflisar.rxbus2.RxBusBuilder
import com.michaelflisar.rxbus2.rx.RxBusMode
import kotlinx.android.synthetic.main.activity_profile.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.reflect.InvocationTargetException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat

class ProfileActivity : BaseActivity(), IDatabaseCallBack {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private var dateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm a")
    private var dobDateFormat = SimpleDateFormat("dd'th' MMM, yyyy")
    private var joinDateFormat = SimpleDateFormat("MMM, yyyy")
    private var dateFormatServer = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    private var dobDateFormatServer = SimpleDateFormat("yyyy-MM-dd")
    private lateinit var performanceSummery: PerformanceSummery
    private var currentHearts = 0


    private var isSocialReg = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
    }

    override fun viewRelatedTask() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)
        viewModel.iDatabaseCallBack = this

        binding.btnPerformanceHistory.setOnClickListener(this)

        binding.userInfo = dataManager.mPref.prefGetUserInfo()

        setProfileInfo(dataManager.mPref.prefGetUserInfo())

        viewModel.fetchPerformanceSummery(this)
        if (dataManager.mPref.prefGetUserInfo().id.isNotEmpty()) {
            viewModel.apiGetOverallPerformance(
                dataManager.mPref.prefGetUserInfo().id.toLong(),
                this
            )
        }

        RxBusBuilder.create(UpdateProfile::class.java)
            .withKey(RxBusEvents.PROFILE_UPDATED)
            .withBound(this)
            .withMode(RxBusMode.Main)
            .subscribe {
                it.profile
                binding.userInfo = it.profile
                setProfileInfo(it.profile)
            }

        binding.btnGoToGame.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).putExtra(
                "goto",
                MyFirebaseMessagingService.GAME_SCREEN
            )
            finishAffinity()
            startActivity(intent)
        }

        setToolbar(this, binding.toolbar, "Profile", true)

        var gameLevel = dataManager.mPref.prefGetGameCurrentLevel()?.level
        if (!gameLevel.isNullOrBlank())
            binding.tvLevel.text = "level - $gameLevel"

        viewModel.getAllGameHeartData()

    }

    private fun setProfileInfo(userInfo: UserInfo) {
        if (userInfo.reg_type == ConstantField.REG_SOCIAL) {
            isSocialReg = true
        }

        try {
            binding.tvDOB.text =
                "Dob: ${dobDateFormat.format(dobDateFormatServer.parse(userInfo.dob ?: ""))}"
            binding.tvJoinDate.text =
                "Joined: ${joinDateFormat.format(dateFormatServer.parse(userInfo.join_date))}"
        } catch (ex: ParseException) {
            ex.printStackTrace()
        } catch (ex: InvocationTargetException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        binding.ivProfile.clipToOutline = true
        var context = application.applicationContext
        if (isValidContextForGlide(context)) {
            Glide.with(context)
                .load(BuildConfig.SERVER_URL + userInfo.picture)
                .placeholder(R.drawable.ic_placeholder_user)
                .error(R.drawable.ic_placeholder_user)
                .into(binding.ivProfile)
        }
        binding.btnEdit.setOnClickListener(this)
    }

    private fun isValidContextForGlide(context: Context?): Boolean {
        if (context == null)
            return false
        if (context is Activity) {
            val activity: Activity = context
            if (activity.isDestroyed || activity.isFinishing) {
                return false
            }
        }
        return true
    }

    private fun setUpResultChart(performanceSummery: PerformanceSummery) {
        binding.pcPerformanceSummary.setUsePercentValues(true)
        val xvalues = ArrayList<PieEntry>()
        performanceSummery.total_correct_question?.toFloat()?.let {
            PieEntry(
                it,
                "Correct(${performanceSummery.total_correct_question})"
            )
        }?.let {
            xvalues.add(
                it
            )
        }

        performanceSummery.total_wrong_question?.toFloat()?.let {
            PieEntry(
                it,
                "Wrong(${performanceSummery.total_wrong_question})"
            )
        }?.let {
            xvalues.add(
                it
            )
        }

        performanceSummery.total_unanswer_question?.toFloat()?.let {
            PieEntry(
                it,
                "Unanswered(${performanceSummery.total_unanswer_question})"
            )
        }?.let {
            xvalues.add(
                it
            )
        }

        val dataSet = PieDataSet(xvalues, "")
        val data = PieData(dataSet)
        val colors = ArrayList<Int>()
        this.let { ContextCompat.getColor(it, R.color.paleBlue) }.let { colors.add(it) }
        this.let { ContextCompat.getColor(it, R.color.darkOrange) }.let { colors.add(it) }
        this.let { ContextCompat.getColor(it, R.color.paleOrange) }
            .let { colors.add(it) }
        dataSet.colors = colors

        // In Percentage
        data.setValueFormatter(ChartValueFormatter())

        binding.pcPerformanceSummary.data = data
        binding.pcPerformanceSummary.description.text = ""
        binding.pcPerformanceSummary.isDrawHoleEnabled = false
        data.setValueTextSize(13f)


        // binding.resultPieChart.setOnChartValueSelectedListener(this)
        chartDetails(binding.pcPerformanceSummary, Typeface.SANS_SERIF)

        binding.emptyLayout.visibility = View.GONE
        binding.performanceLayout.visibility = View.VISIBLE
    }


    fun chartDetails(mChart: PieChart, tf: Typeface) {
        //  mChart.description.isEnabled = true
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING

        var totalNumber = (performanceSummery.total_correct_question?.toDouble() ?: 0.0) -
                (((performanceSummery.total_wrong_question?.toDouble() ?: 0.0) * 0.5))
        var totalQuestions = performanceSummery.total_total_question.toDouble()
        var percentage = (totalNumber * 100) / totalQuestions

        var score =
            "Score\n${df.format(percentage)}%"

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
//        l.isWordWrapEnabled = trueg
        //  l.setDrawInside(false)
        //      mChart.setTouchEnabled(false)
        mChart.setDrawEntryLabels(false)
        mChart.setExtraOffsets(20f, 0f, 20f, 0f)
        mChart.setUsePercentValues(true)
        // mChart.rotationAngle = 0f
        mChart.setUsePercentValues(true)
        mChart.setDrawCenterText(true)
        //  mChart.description.isEnabled = true
        mChart.isRotationEnabled = true
        mChart.notifyDataSetChanged();
        mChart.invalidate();

        binding.totalQuestionNoTv.setHtml("Total Number Of Questions : <b> <font size=\"50\" color=\"#BA1414\"> ${performanceSummery.total_total_question} </font> </b>")

    }


    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "fetchPerformanceSummery" -> {
                val type = object : TypeToken<BaseModel<PerformanceSummery>>() {}.type
                result.data?.body()?.let {
                    val baseModel = Gson().fromJson<BaseModel<PerformanceSummery>>(
                        result.data.body()?.string(), type
                    )

                    if (baseModel.status_code == 200 && baseModel.data != null) {
                        try {
                            performanceSummery = baseModel.data!!

                            performanceSummery.total_correct_question?.let {
                                setUpResultChart(performanceSummery)
                            } ?: kotlin.run {
                                binding.emptyLayout.visibility = View.VISIBLE
                                binding.performanceLayout.visibility = View.GONE
                            }

                            performanceSummery.best_performance?.let {
                                binding.tvBestGameType.text =
                                    "${performanceSummery.best_performance?.challenge_name}, ${dateFormat.format(
                                        dateFormatServer.parse(performanceSummery.best_performance?.created_at)
                                    )}"

                                binding.btnBestPerformance.setOnClickListener {
                                    startActivity(
                                        Intent(this, GameResultActivity::class.java)
                                            .putExtra("from_activity", "profile")
                                            .putExtra(
                                                "game_id",
                                                performanceSummery.best_performance?.game_id
                                            )
                                    )
                                }
                            } ?: kotlin.run {
                                binding.bestPerformanceLayout.visibility = View.GONE
                            }

                            performanceSummery.worst_performance?.let {
                                binding.tvWorstGameType.text =
                                    "${performanceSummery.worst_performance?.challenge_name}, ${dateFormat.format(
                                        dateFormatServer.parse(performanceSummery.worst_performance?.created_at)
                                    )}"
                                binding.btnWorstPerformance.setOnClickListener {
                                    startActivity(
                                        Intent(this, GameResultActivity::class.java)
                                            .putExtra("from_activity", "profile")
                                            .putExtra(
                                                "game_id",
                                                performanceSummery.worst_performance?.game_id
                                            )
                                    )
                                }
                            } ?: kotlin.run {
                                binding.worstPerformanceLayout.visibility = View.GONE
                            }

                            performanceSummery.recent_game_id?.let {
                                binding.btnRecentPerformance.isEnabled = true
                                binding.btnRecentPerformance.setOnClickListener {
                                    startActivity(
                                        Intent(this, GameResultActivity::class.java)
                                            .putExtra("from_activity", "profile")
                                            .putExtra(
                                                "game_id",
                                                performanceSummery.recent_game_id
                                            )
                                    )
                                }
                            } ?: kotlin.run {
                                binding.btnRecentPerformance.isEnabled = false
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                            binding.emptyLayout.visibility = View.VISIBLE
                            binding.performanceLayout.visibility = View.GONE
                        } catch (e: Exception) {
                            e.printStackTrace()
                            binding.emptyLayout.visibility = View.VISIBLE
                            binding.performanceLayout.visibility = View.GONE
                        }
                    } else {
                        binding.emptyLayout.visibility = View.VISIBLE
                        binding.performanceLayout.visibility = View.GONE
                    }
                }
            }
            "apiGetOverallPerformance" -> {
                val type = object :
                    TypeToken<BaseModel<PaginationModel<ArrayList<SubjectWisePerformance>>>>() {}.type
                result.data?.body()?.let {
                    val baseModel =
                        Gson().fromJson<BaseModel<PaginationModel<ArrayList<SubjectWisePerformance>>>>(
                            result.data.body()?.string(), type
                        )
                    if (baseModel.status_code == 200 && baseModel.data != null) {
                        val subjectList = baseModel.data?.data

                        if (baseModel.data?.data != null)
                            initSubjectGraph(baseModel.data?.data)
                        else
                            binding.linearLayoutSuccessGraph.visibility = View.GONE

                        val barEntriesSubjects = mutableListOf<BarEntry>()

                        val subjectNames = arrayListOf<String>()
                        val quesDets = arrayListOf<String>()
                        var highestVal = 0.0
                        subjectList?.forEachIndexed { pos, it ->
                            val percentage = it.performance_percent ?: 0.0
                            if (highestVal < percentage)
                                highestVal = percentage
                            val barEntry = BarEntry(pos.toFloat(), percentage.toFloat())
                            val refinedCA = if (it.correct_answer.isNullOrEmpty()) {
                                "0"
                            } else {
                                it.correct_answer
                            }

                            val refinedTQ = if (it.total_question.isNullOrEmpty()) {
                                "0"
                            } else {
                                it.total_question
                            }
                            val quesDet = "$refinedCA/$refinedTQ"
                            quesDets.add(quesDet)
                            barEntriesSubjects.add(barEntry)
                            if (!it.name.isNullOrEmpty()) {
                                var subjectName = it.name.toString()
                                if (subjectName.length > 20) {
                                    subjectName = subjectName.substring(0, 19) + "..."
                                }
                                subjectNames.add(subjectName)
                            }
                        }

                        val barDataSetSubjects = CustomBarDataSet(barEntriesSubjects, "")
                        barDataSetSubjects.colors = mutableListOf(
                            this.let { it1 -> ContextCompat.getColor(it1, R.color.red) },
                            this.let { it1 -> ContextCompat.getColor(it1, R.color.green500) },
                            this.let { it1 -> ContextCompat.getColor(it1, R.color.yellow500) }
                        )

                        barDataSetSubjects.valueTextColor = Color.BLACK
                        barDataSetSubjects.valueTextSize = 14f
                        barDataSetSubjects.valueFormatter = PercentageExtFormatter(quesDets)
                        val barDataSubject = BarData(barDataSetSubjects)
                        val xAxis = chartPerformance.xAxis
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.valueFormatter = PerformanceFormatter(subjectNames)
                        xAxis.setDrawGridLines(false)
                        xAxis.textSize = 12f
                        xAxis.isGranularityEnabled = true
                        xAxis.granularity = 1f
                        xAxis.setLabelCount(subjectNames.size, false)
                        //xAxis.setCenterAxisLabels(true)

                        chartPerformance.data = barDataSubject
                        chartPerformance.invalidate()

                        if (barDataSubject.yMax == 0f) {
                            chartPerformance.axisLeft.setDrawLabels(false)
                            chartPerformance.axisRight.setDrawLabels(false)
                        } else {
                            chartPerformance.axisLeft.setDrawLabels(true)
                            chartPerformance.axisRight.setDrawLabels(true)
                        }

                        val rightYAxis: YAxis = chartPerformance.axisRight
                        rightYAxis.setAxisMaxValue(100f)
                        rightYAxis.setAxisMinValue(0f)
                        rightYAxis.labelCount = 5

                        val leftYAxis: YAxis = chartPerformance.axisLeft
                        leftYAxis.setAxisMaxValue(100f)
                        leftYAxis.setAxisMinValue(0f)
                        leftYAxis.labelCount = 5

                        chartPerformance.description = null
                        chartPerformance.legend.isEnabled = false
                        chartPerformance.setTouchEnabled(false)
                        chartPerformance.legend.isWordWrapEnabled = true

                        val rightOffSet: Float = when (highestVal) {
                            in 90.0..100.0 -> {
                                90f
                            }
                            in 80.0..90.0 -> {
                                60f
                            }
                            else -> {
                                40f
                            }
                        }
                        chartPerformance.setExtraOffsets(20f, 0f, rightOffSet, 0f)
                    } else {
                        binding.linearLayoutSuccessGraph.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun initSubjectGraph(data: ArrayList<SubjectWisePerformance>?) {
        binding.rvSuccessGraph.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return SuccessGraphHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context)
                            , R.layout.item_success_graph
                            , parent, false
                        )
                        , this@ProfileActivity
                    )
                }

                override fun loadMoreItem() {
                }
            }, data!!)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnPerformanceHistory -> {
                startActivity(Intent(this, PerformanceHistoryActivity::class.java))
            }
            binding.btnEdit -> {

                val layoutInflater =
                    this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
                val popupView: View? =
                    layoutInflater?.inflate(R.layout.item_popup_menu_for_profile, null)

                val closelayout = popupView?.findViewById<RelativeLayout>(R.id.closewindow)
                val profile_edit = popupView?.findViewById<LinearLayout>(R.id.profile_edit_layout)
                val resetlayout = popupView?.findViewById<LinearLayout>(R.id.resetpasswordlayout)
                val view1 = popupView?.findViewById<View>(R.id.view1)
                val popupWindow = PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                popupWindow.setBackgroundDrawable(BitmapDrawable())
                popupWindow.isOutsideTouchable = true
                popupWindow.setOnDismissListener {
                }

                if (isSocialReg) {
                    resetlayout!!.visibility = View.GONE
                    view1!!.visibility = View.GONE
                }

                popupWindow.showAsDropDown(binding.btnEdit)
                profile_edit?.setOnClickListener {
                    val intent = Intent(this, ProfileSetupActivity::class.java)
                    intent.putExtra("isEdit", true)
                    startActivity(intent)
                    popupWindow.dismiss()
                }

                resetlayout?.setOnClickListener {
                    startActivity(Intent(this, ForgetPasswordActivity::class.java))
                }

                closelayout?.setOnClickListener {
                    popupWindow.dismiss()
                }

            }
        }
    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (isLoader) {
            showProgressDialog("Please Wait")
        } else {
            hideProgressDialog()
        }
    }

    override fun onSuccessDB(result: Any, optName: String) {
        when (optName) {
            "getAllGameHeartData" -> {
                currentHearts = dataManager.mPref.prefGetUserHeart()?.toInt() ?: 0
                val hearts = result as List<GameHeartDTO>
                hearts.forEach {
                    if (it.heart_type == GAME_HEART_MINUS) {
                        currentHearts -= 1
                    } else {
                        if (!it.practice.isNullOrEmpty()) {
                            currentHearts += if (it.practice == "0")
                                dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_hearts?.toInt()
                                    ?: 0
                            else
                                dataManager.mPref.getUserGlobalSetting()?.hearts_settings?.practice_random_hearts?.toInt()
                                    ?: 0
                        }
                    }
                }
                bindHearts()
            }
        }
    }

    override fun onFailedDB(result: Any, optName: String) {
    }

    private fun bindHearts() {
        binding.lifeamounts.text = currentHearts.toString()
    }
}

