package com.gmpire.guruklub.view.activity.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.leaderboard.LeaderBoard
import com.gmpire.guruklub.data.model.leaderboard.LeaderBoardResponse
import com.gmpire.guruklub.data.model.leaderboard.PlayedModelTestModel
import com.gmpire.guruklub.databinding.ActivityLeaderBoardBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService
import com.gmpire.guruklub.view.activity.leaderDetailsActivity.LeaderDetailsActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.newsDetails.NewsDetailsActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_leader_board.*
import okhttp3.ResponseBody
import retrofit2.Response

class LeaderBoardActivity : BaseActivity() {
    private lateinit var binding: ActivityLeaderBoardBinding
    private var copyPlayedModelTestModel = ArrayList<PlayedModelTestModel>()
    private var filterlist = ArrayList<PlayedModelTestModel>()
    private var page: Int = 1
    private var hasNextPage: Boolean = false
    private lateinit var viewmodel: ProfileViewModel
    private var isParticipationHide = true
    private var playedModelTests = ArrayList<PlayedModelTestModel>()
    private lateinit var leaderBoards: ArrayList<LeaderBoard>
    private var selectedModelTestId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leader_board)
    }

    override fun viewRelatedTask() {
        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)

        initAutoSearchEditText()

        viewmodel.apiGetPlayedModelTestItems(
            dataManager.mPref.prefGetUserInfo().category_id.toString(),
            this
        )

        binding.rlParticipationSelector.setOnClickListener(this)

        binding.registerModelTestBtn.setOnClickListener(this)
        setToolbar(this, binding.toolbar, "Live Exam Result", true)

        register_model_test_btn

        binding.root.setOnClickListener {
            hideKeyboard()
        }
        //binding.toolbar.appCompatImageViewLogo.visibility = View.VISIBLE
    }


    private fun initCopyPlayedData() {

        var sizeofPlayedModelTest = this.playedModelTests.size - 1
        this.copyPlayedModelTestModel.clear()
        for (i in 0..sizeofPlayedModelTest) {
            var copyDatas = playedModelTests[i].copy(
                playedModelTests[i].id,
                playedModelTests[i].title,
                playedModelTests[i].isSelected,
                playedModelTests[i].total_participate
            )

            copyPlayedModelTestModel.add(copyDatas)
        }
    }

    private fun initAutoSearchEditText() {
        Log.d("autosearch", "autosearch")


        binding.searchModelTest.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                fitler(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                copyPlayedModelTest()
            }

        })

    }

    private fun copyPlayedModelTest() {
        var sizeofPlayedModelTest = this.copyPlayedModelTestModel.size - 1
        playedModelTests.clear()
        for (i in 0..sizeofPlayedModelTest) {
            var copyDatas = copyPlayedModelTestModel[i].copy(
                copyPlayedModelTestModel[i].id,
                copyPlayedModelTestModel[i].title,
                copyPlayedModelTestModel[i].isSelected,
                copyPlayedModelTestModel[i].total_participate
            )

            playedModelTests.add(copyDatas)
        }
    }

    private fun fitler(userInput: String) {
        this.filterlist.clear()
        for (item in playedModelTests) {
            if (item.title.toLowerCase().contains(userInput.toLowerCase())) {
                this.filterlist.add(item)
            }
        }

        var sizeoffilterlist = this.filterlist.size - 1
        if (sizeoffilterlist >= 0) {
            binding.rvParticipationName.visibility = View.VISIBLE
            binding.emptyMessage.visibility = View.GONE
            this.playedModelTests.clear()
            this.playedModelTests.addAll(filterlist)
            binding.rvParticipationName.adapter?.notifyDataSetChanged()
        } else if (sizeoffilterlist < 0) {
            binding.emptyMessage.visibility = View.VISIBLE
            binding.rvParticipationName.visibility = View.GONE
        }


    }

    private fun participationSelectorRvHideOrVisible() {
        if (isParticipationHide) {
            isParticipationHide = false
            binding.ivArrow.rotation = 180.0.toFloat()
            binding.rvParticipationName.visibility = View.VISIBLE
            val animation: Animation = AnimationUtils.loadAnimation(
                this@LeaderBoardActivity,
                R.anim.slide_in_top
            )
            binding.rvParticipationName.animation = animation
        } else {
            binding.ivArrow.rotation = 360.0.toFloat()
            val animation: Animation = AnimationUtils.loadAnimation(
                this@LeaderBoardActivity,
                R.anim.slide_out_top
            )
            binding.rvParticipationName.animation = animation
            binding.rvParticipationName.visibility = View.GONE
            isParticipationHide = true
        }
    }


    private fun initExams() {
        initCopyPlayedData()
        binding.rvParticipationName.layoutManager = LinearLayoutManager(this@LeaderBoardActivity)

        binding.rvParticipationName.adapter =
            BaseRecyclerAdapter(this, object : IAdapterListener {
                override fun <T> callBack(position: Int, model: T, view: View) {
                    model as PlayedModelTestModel
                    selectedModelTestId = model.id
                    page = 1
                    startActivity(
                        Intent(
                            this@LeaderBoardActivity,
                            LeaderDetailsActivity::class.java
                        ).putExtra(
                            "leaderboard",
                            model
                        )
                    )
                }

                override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                    return ParticipationViewHolder(
                        DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_participation_name,
                            parent,
                            false
                        ), this@LeaderBoardActivity
                    )
                }

                override fun loadMoreItem() {
                }
            }, playedModelTests)

    }

    private fun initLeaderBoard(leaderBoards: ArrayList<LeaderBoard>) {
        if (page != 1) {
            this.leaderBoards.addAll(leaderBoards)
            binding.rvLeaderBoard.adapter?.notifyDataSetChanged()
        } else {
            val mlayoutmanager = LinearLayoutManager(this)
            mlayoutmanager.orientation = LinearLayoutManager.HORIZONTAL
            binding.rvLeaderBoard.layoutManager = mlayoutmanager
            binding.rvLeaderBoard.itemAnimator = DefaultItemAnimator()
            binding.rvLeaderBoard.adapter =
                BaseRecyclerAdapter(this, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {

                    }

                    override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                        return LeaderBoardViewHolder(

                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context),
                                R.layout.item_leader_board,
                                parent,
                                false
                            ), this@LeaderBoardActivity
                        )
                    }

                    override fun loadMoreItem() {
                        if (page <= 10) {
                            if (hasNextPage) {
                                viewmodel.apiGetLeaderboard(
                                    selectedModelTestId.toString(),
                                    page.toString(),
                                    this@LeaderBoardActivity
                                )
                            }
                        }
                    }
                }, leaderBoards)
        }

    }

    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "apiGetLeaderboard") {
            if (page == 1) {
                if (isLoader) {
                    showProgressDialog("Please Wait")
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
        }
    }

    override fun navigateToHome() {
        finishAffinity()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetPlayedModelTestItems" -> {
                val type = object : TypeToken<BaseModel<ArrayList<PlayedModelTestModel>>>() {}.type
                result.data?.body()?.let {
                    val baseModel = Gson().fromJson<BaseModel<ArrayList<PlayedModelTestModel>>>(
                        result.data.body()?.string(), type
                    )
                    if (baseModel.status_code == 200) {
                        if (baseModel.data?.size ?: 0 > 0) {
                            playedModelTests = baseModel.data ?: arrayListOf()
                            if (playedModelTests.isNotEmpty()) {
                                selectedModelTestId = playedModelTests[0].id
                                playedModelTests[0].isSelected = true
                                initExams()
                                page = 1
                                binding.tvModelTestName.text = playedModelTests[0].title
                                binding.tvTotalParticipate.text =
                                    "Total Participants - ${playedModelTests[0].total_participate}"
                            }
                            binding.emptyViewLayout.visibility = View.GONE
                        } else {
                            binding.emptyViewLayout.visibility = View.VISIBLE
                        }
                    }
                }
            }
            "apiGetLeaderboard" -> {
                val type = object : TypeToken<BaseModel<LeaderBoardResponse>>() {}.type
                result.data?.body()?.let {
                    val baseData =
                        Gson().fromJson<BaseModel<LeaderBoardResponse>>(
                            result.data.body()?.string(),
                            type
                        )
                    if (baseData.status_code == 200) {
                        try {
                            if (baseData.data != null) {
                                if (baseData.data?.leader_board != null && baseData.data?.leader_board?.isNotEmpty()!!) {
                                    leaderBoards =
                                        baseData.data?.leader_board as java.util.ArrayList<LeaderBoard>
                                    initLeaderBoard(leaderBoards)

                                    baseData.data?.my_position?.let {
                                        binding.myPositionLayout.visibility = View.VISIBLE
                                        binding.rlNoParticipation.visibility = View.GONE
                                        binding.tvMyPosition.text = it.position.toString()
                                        binding.tvMyName.text = it.name
                                        binding.tvMyScore.text = "${it.get_point}/${it.total_point}"
                                    } ?: kotlin.run {
                                        binding.myPositionLayout.visibility = View.VISIBLE
                                        binding.rlNoParticipation.visibility = View.VISIBLE
                                    }

                                    hasNextPage = baseData.data?.next_page != 0
                                    page = baseData.data?.next_page ?: 0

                                    binding.rvLeaderBoard.visibility = View.VISIBLE
                                    binding.emptyMessage.visibility = View.GONE
                                } else {
                                    binding.rvLeaderBoard.visibility = View.GONE
                                    binding.emptyMessage.visibility = View.VISIBLE
                                    binding.myPositionLayout.visibility = View.GONE
                                }
                            } else {
                                showToast(this, baseData.message[0])
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        showToast(this, baseData.message[0])
                    }
                }

            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {

            binding.registerModelTestBtn -> {
                val intent = Intent(this, MainActivity::class.java).putExtra(
                    "goto",
                    MyFirebaseMessagingService.GAME_SCREEN
                )
                finishAffinity()
                startActivity(intent)
            }

            binding.rlParticipationSelector -> {
                participationSelectorRvHideOrVisible()
            }

        }
    }
}