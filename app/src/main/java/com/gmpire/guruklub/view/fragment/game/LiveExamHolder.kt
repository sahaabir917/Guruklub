package com.gmpire.guruklub.view.fragment.game

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.DataManager
import com.gmpire.guruklub.data.model.modelTest.ModelTest
import com.gmpire.guruklub.data.model.modelTest.ModelTestRegistrationResponse
import com.gmpire.guruklub.databinding.ItemLiveExamBinding
import com.gmpire.guruklub.util.ColorUtil
import com.gmpire.guruklub.util.DateUtil
import com.gmpire.guruklub.util.DisplayUtil
import com.gmpire.guruklub.view.activity.gameHelp.ContentActivity
import com.gmpire.guruklub.view.activity.modelTestActivity.ModelTestActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.dialog.UpdateProfileDialog
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by Tahsin Rahman on 12/12/20.
 */


class LiveExamHolder(
    itemView: ViewDataBinding,
    var context: Context,
    var currentTime: Date?,
    var dataManager: DataManager,
    var liveExamListener: LiveExamListener,
    var isFromPractice: Boolean
) :
    BaseViewHolder(itemView.root) {

    var binding = itemView as ItemLiveExamBinding
    private var testDate: Date? = null
    private var modelTest: ModelTest? = null
    private var modelTestRegistrationResponse: ModelTestRegistrationResponse? = null
    var isFromEmptyModelTest = false
    private var timer: CountDownTimer? = null
    private var elapsedTime: Long? = null

    override fun <T> onBind(position: Int, model: T, mCallback: IAdapterListener) {
        model as ModelTest

        modelTest = model
        // Empty live exam card
        if (modelTest?.id?.isEmpty()!!) {
            return
        }

        val drawableTop = binding.relativelayoutLiveExamTop.background as GradientDrawable
        val drawable = binding.relativeLayoutBorder.background as GradientDrawable
        val drawableTag = binding.relativeLayoutFreeTagRv.background as GradientDrawable

        if (model.hearts_cost.toInt() > 0) {
            binding.relativeLayoutFreeTagRv.visibility = View.VISIBLE
            binding.heartCostTextview.text = model.hearts_cost.toString()
        } else if (model.hearts_cost.toInt() <= 0) {
            binding.relativeLayoutFreeTagRv.visibility = View.VISIBLE
            binding.heartCostTextview.text = "Free"
        }

        var color = ColorUtil.getColorByPosition(position + 3)

        drawableTop.setColor(ContextCompat.getColor(context, color))
        drawableTag.setStroke(
            DisplayUtil.dpToPx(2, context),
            ContextCompat.getColor(context, color)
        )
        drawable.setStroke(
            DisplayUtil.dpToPx(2, context),
            ContextCompat.getColor(context, color)
        )

        if (isFromPractice) {
            setModelTestView(model, "Start Test")
        } else {
            try {
                elapsedTime =
                    (currentTime!!.time - DateUtil.simpleDateFormatServer.parse(model.date).time)
                val duration =
                    (model.duration.toInt() * 60 * 1000)

                if (elapsedTime!! < -1) {
                    if (model.updateFromMobileRes == 1) {
                        restOfTheSetModelTest(
                            model.tempStartOrResumeText,
                            model.syllabus,
                            model.register
                        )
                    } else {
                        setModelTestView(model, "Start Test")
                    }
                } else if (elapsedTime!! < duration) {
                    if (model.is_participated == 1 && model.is_completed == 0)
                        setModelTestView(model, "Resume")
                    else
                        setModelTestView(model, "Start Test")
                } else {
                    setNoModelTestView()
                    //syncModelTestData()
                    dataManager.mPref.prefSetModelTestResponseModelById(null, model.id)
                }
                return
            } catch (e: ParseException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

    private fun setModelTestView(
        modelTest: ModelTest,
        startOrResumeText: String
    ) {
        if (!isFromPractice) {
            testDate = DateUtil.simpleDateFormatServer.parse(modelTest.date)
            binding.textViewExamName.text = modelTest.title

            binding.btnJoinNowOrStartTest.visibility = View.VISIBLE
            binding.btnSyllabus.visibility = View.VISIBLE
            binding.textViewExamTime.text =
                "Time of test: ${DateUtil.simpleDateFormat.format(testDate)}"

            //check if model test is already registered by user or not
            if (modelTest.register == 1) {
                binding.btnJoinNowOrStartTest.text = startOrResumeText
                binding.btnJoinNowOrStartTest.isEnabled = false
                modelTestRegistrationResponse =
                    dataManager.mPref.prefGetModelTestRegistrationResponseById(modelTest.id)
                if (modelTestRegistrationResponse == null) {
                    //api request for registration of model test
                    isFromEmptyModelTest = true
                    liveExamListener.getModelRegData(
                        modelTest.id,
                        false,
                        modelTest.title,
                        modelTest.hearts_cost
                    )
                    return
                }
            } else {
                binding.btnJoinNowOrStartTest.text = "Join Now"
                binding.btnJoinNowOrStartTest.isEnabled = true
                binding.btnJoinNowOrStartTest.setOnClickListener {
                    //api request for registration for model test
                    if (!dataManager.mPref.prefGetUserInfo().name.isNullOrEmpty()) {
                        if (dataManager.mPref.prefGetUserHeart()?.toInt() ?: 0 >=  modelTest.hearts_cost) {
                            if (modelTest.hearts_cost > 0) {
                                liveExamListener.initiateCostDialog(
                                    modelTest.hearts_cost,
                                    "live_exam",
                                    modelTest.id
                                )
                            } else {
                                liveExamListener.getModelRegData(
                                    modelTest.id,
                                    false,
                                    modelTest.title,
                                    modelTest.hearts_cost
                                )
                            }
                        } else {
                            liveExamListener.initiateHeartBuyDialog(modelTest.hearts_cost)
                        }
                    } else {
                        showAlertDialog()
                    }
                }
            }
        } else {
            binding.textViewLiveTop.text = "Recorded Live Exam"
            binding.textViewExamName.text = modelTest.title
            binding.textViewExamTime.visibility = View.GONE
            binding.btnJoinNowOrStartTest.text = startOrResumeText
            binding.btnJoinNowOrStartTest.isEnabled = false
        }
        restOfTheSetModelTest(startOrResumeText, modelTest.syllabus, modelTest.register)
    }

    private fun showAlertDialog() {
        var updateProfileDialog =
            UpdateProfileDialog.newInstance("You need to update your profile before joining the exam. Do you want to update your profile ?","profile_update")
        updateProfileDialog.show(
            (context as FragmentActivity).supportFragmentManager,
            updateProfileDialog.tag
        )
    }

    private fun restOfTheSetModelTest(startOrResumeText: String, syllabus: String, register: Int) {
        binding.btnSyllabus.setOnClickListener {
            context.startActivity(
                Intent(context, ContentActivity::class.java).putExtra(
                    "syllabus",
                    syllabus
                ).putExtra(ContentActivity.ACTIVITY_TITLE, ContentActivity.TITLE_SYLLABUS)
            )
        }

        //check current time is smaller then model test time, if current time is smaller then start timer else init start test or set no model test view
        if (!isFromPractice) {
            if ((testDate?.time ?: 0) - (currentTime?.time ?: 0) > 0) {
                startTimer((testDate?.time ?: 0) - (currentTime?.time ?: 0))
            } else {
                if (register == 1) {
                    initStartTest(startOrResumeText)
                } else {
                    setNoModelTestView()
                }
            }
        } else if (isFromPractice) {
            initRecordLiveTest(startOrResumeText)
        }

    }

    private fun initRecordLiveTest(startOrResumeText: String) {
        binding.btnJoinNowOrStartTest.text = startOrResumeText
        binding.btnJoinNowOrStartTest.isEnabled = true

        binding.btnJoinNowOrStartTest.setOnClickListener {
            Log.d("Pass Model Response->", modelTest?.id)
            if (dataManager.mPref.prefGetUserHeart()?.toInt() ?: 0 >= modelTest?.hearts_cost ?: 0) {
                isFromEmptyModelTest = true
                modelTest?.let { it1 ->
                    if (it1.hearts_cost > 0) {
                        liveExamListener.initiateCostDialogPractice(
                            it1.hearts_cost,
                            "practice_exam",
                            it1.id,
                            it1.title
                        )
                    } else {
                        liveExamListener.getModelRegData(
                            it1.id, true, modelTest?.title.toString(), it1.hearts_cost
                        )
                    }
                }
            } else {
                liveExamListener.initiateHeartBuyDialog(modelTest?.hearts_cost?:0)
            }
        }
        if(!isFromPractice) {
            binding.textViewCountdown.visibility = View.VISIBLE
            binding.textViewCountdown.text = "00:00:00:00"
        }
    }

    private fun startTimer(l: Long) {
        binding.textViewCountdown.visibility = View.VISIBLE
        if (timer != null) {
            timer?.cancel()
        }
        timer = object : CountDownTimer(l, 1000) {
            override fun onTick(mil: Long) {

                var millisUntilFinished = mil
                val days: Long = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                millisUntilFinished -= TimeUnit.DAYS.toMillis(days)

                val hours: Long = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                millisUntilFinished -= TimeUnit.HOURS.toMillis(hours)

                val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

                val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

                val f: NumberFormat = DecimalFormat("00")

                binding.textViewCountdown.text =
                    "$days day ${f.format(hours)} hr ${f.format(minutes)} min ${
                    f.format(
                        seconds
                    )
                    } sec left"

            }

            override fun onFinish() {
                if (modelTest?.register == 1) {
                    initStartTest("Start Test")
                } else {
                    setNoModelTestView()
                }
                timer?.cancel()
            }
        }.start()
    }

    private fun initStartTest(startOrResumeText: String) {
        try {
            elapsedTime =
                (currentTime!!.time - DateUtil.simpleDateFormatServer.parse(modelTest?.date).time)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        modelTestRegistrationResponse?.let {
            val duration = (it.duration.toInt() * 60 * 1000)
            if (elapsedTime!! < duration) {
                // Already participated in the exam
                if (modelTest?.is_completed == 1) {
                    if(!isFromPractice) {
                        binding.textViewCountdown.text =
                            context.getString(R.string.already_participated)
                        binding.textViewCountdown.visibility = View.VISIBLE
                    }
                    return
                }

                binding.btnJoinNowOrStartTest.text = startOrResumeText
                binding.btnJoinNowOrStartTest.isEnabled = true

                binding.btnJoinNowOrStartTest.setOnClickListener {
                    liveExamListener.examInitiated()
                    context.startActivity(
                        Intent(context,
                            ModelTestActivity::class.java
                        ).putExtra("model_test_id", modelTest?.id.toString())
                            .putExtra("model_test_title", modelTest?.title.toString())
                            .putExtra("heartCosting", modelTest?.hearts_cost ?: 0 > 0)
                    )
                }
                if(!isFromPractice) {
                    binding.textViewCountdown.visibility = View.VISIBLE
                    binding.textViewCountdown.text = "00:00:00:00"
                }
            } else {
                setNoModelTestView()
                dataManager.mPref.prefSetModelTestResponseModelById(null, modelTest?.id.toString())
            }
        } ?: kotlin.run {
            Toast.makeText(
                context,
                "registration response model data not found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setNoModelTestView() {
        if (timer != null)
            timer?.cancel()

        binding.btnJoinNowOrStartTest.visibility = View.GONE
        binding.btnSyllabus.visibility = View.GONE
        binding.textViewExamTime.text = "No upcoming test"
        binding.textViewCountdown.text = "00:00:00:00"
        binding.textViewCountdown.visibility = View.GONE

        if (modelTest?.register == 0) {
            binding.textViewExamTime.text =
                "Not registered"
        }
    }

    interface LiveExamListener {
        fun getModelRegData(
            modelTestId: String,
            isFromPractice: Boolean,
            title: String,
            heartsCost: Int
        )
        fun purchaseExam(modelTestId: String, examFees: String, heart_cost: Int)
        fun initiateCostDialog(heartCost : Int, type : String, examId : String)
        fun initiateCostDialogPractice(heartCost : Int, type : String, practiceExamId : String, practiceExamTitle : String)
        fun initiateHeartBuyDialog(heartCost: Int)
        fun examInitiated()
    }

}