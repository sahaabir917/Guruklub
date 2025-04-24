package com.gmpire.guruklub.view.activity.profile

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.game.GameChallengeItem
import com.gmpire.guruklub.data.model.profile.PerformanceHistory
import com.gmpire.guruklub.databinding.ActivityPerformanceHistoryBinding
import com.gmpire.guruklub.util.GeneratDropdownItem
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.EmptyViewHolder
import com.gmpire.guruklub.view.activity.gameResultActivity.GameResultActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.adapter.CustomSpinnerAdapter
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Response

class PerformanceHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityPerformanceHistoryBinding
    private lateinit var viewmodel: ProfileViewModel
    private lateinit var gameTypeSpAdapter: CustomSpinnerAdapter
    private var performanceHistories: ArrayList<PerformanceHistory> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_performance_history)
        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)
    }

    override fun viewRelatedTask() {
        setToolbar(this, binding.toolbar, "Exam History", true, false)
        viewmodel.fetchGetGameChallenges(this)
        viewmodel.fetchPerformanceHistory(this)
        binding.rlGameType.setOnClickListener {
            binding.spGameType.performClick()
        }
        binding.toolbar.drawerTitle.setTypeface(binding.toolbar.drawerTitle.typeface, Typeface.BOLD)
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun initGameType(items: ArrayList<GameChallengeItem>?) {

        gameTypeSpAdapter = CustomSpinnerAdapter(
            this,
            GeneratDropdownItem.getDropdownItems(
                JSONArray(Gson().toJson(items)),
                "name",
                "id",
                "No Exam Type Selected"
            )
        )

        binding.spGameType.adapter = gameTypeSpAdapter.getAdapter()

        binding.spGameType.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                gameTypeSpAdapter.setPosition(position)
                if (position != 0) {
                    initPerformanceHistory(performanceHistories.filter { item -> item.challenge_id == gameTypeSpAdapter.getSelectedId() } as ArrayList<PerformanceHistory>)
                } else {
                    initPerformanceHistory(performanceHistories)
                }
            }
        })


    }

    private fun initPerformanceHistory(performanceHistories: ArrayList<PerformanceHistory>) {
        binding.rvPerformanceHistory.layoutManager = LinearLayoutManager(this)
        binding.rvPerformanceHistory.adapter = BaseRecyclerAdapter(this, object : IAdapterListener {
            override fun <T> callBack(position: Int, model: T, view: View) {
                model as PerformanceHistory
                startActivity(
                    Intent(this@PerformanceHistoryActivity, GameResultActivity::class.java)
                        .putExtra("from_activity", "profile")
                        .putExtra("game_id", model.game_id)
                )
            }

            override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                if (viewType != -1) {
                    return PerformanceHistoryViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_performance_history,
                            parent,
                            false
                        ), this@PerformanceHistoryActivity
                    )
                } else {
                    return EmptyViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.empty_page,
                            parent,
                            false
                        ), this@PerformanceHistoryActivity, "No Performance History Found!!"
                    )

                }
            }

            override fun loadMoreItem() {
            }
        }, performanceHistories)

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "fetchPerformanceHistory" -> {
                val type = object : TypeToken<BaseModel<ArrayList<PerformanceHistory>>>() {}.type
                result.data?.body()?.let {
                    val baseModel = Gson().fromJson<BaseModel<ArrayList<PerformanceHistory>>>(
                        result.data.body()?.string(), type
                    )
                    if (baseModel.status_code == 200) {
                        baseModel.data?.let {
                            performanceHistories = baseModel.data ?: arrayListOf()
                            initPerformanceHistory(performanceHistories)
                        }
                    }
                }
            }
            "fetchGetGameChallenges" -> {
                val type = object : TypeToken<BaseModel<ArrayList<GameChallengeItem>>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<ArrayList<GameChallengeItem>>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        baseData.data?.let {
                            initGameType(baseData.data)
                        }

                    } else {
                        showToast(this, baseData.message[0])
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {

    }
}
