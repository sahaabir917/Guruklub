package com.gmpire.guruklub.view.activity.subjectBasedPerformace

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.game.SubjectBasedPerformanceResponse
import com.gmpire.guruklub.databinding.ActivitySubjectBasedPerformanceBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.NullPointerException


class SubjectBasedPerformanceActivity : BaseActivity() {

    private lateinit var subjectBasedPerformaceResponse: java.util.ArrayList<SubjectBasedPerformanceResponse>
    private lateinit var viewModel: SubjectBasedPerformanceViewModel
    private lateinit var binding: ActivitySubjectBasedPerformanceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subject_based_performance)

        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(SubjectBasedPerformanceViewModel::class.java)

        viewModel.apiSubjectPerformance(intent.getStringExtra("game_id"), this)

    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Subject Based Performance", true)
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiSubjectPerformance" -> {
                val type = object :
                    TypeToken<BaseModel<ArrayList<SubjectBasedPerformanceResponse>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<SubjectBasedPerformanceResponse>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        if (baseData.data != null) {
                            try {
                                subjectBasedPerformaceResponse = baseData.data!!
                                setupPerformanceData()
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                }
            }
        }
    }

    private fun setupPerformanceData() {
        binding.subjectPerformanceRecview.layoutManager = LinearLayoutManager(this)
        binding.subjectPerformanceRecview.setHasFixedSize(true)
        binding.subjectPerformanceRecview.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {

                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return SubjectPerformanceViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(
                                this@SubjectBasedPerformanceActivity
                            ), R.layout.item_subject_performance, parent, false
                        ), this@SubjectBasedPerformanceActivity
                    )
                }

                override fun loadMoreItem() {
                }

            }, subjectBasedPerformaceResponse)
    }


    override fun onClick(v: View?) {

    }
}
