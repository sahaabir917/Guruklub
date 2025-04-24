package com.gmpire.guruklub.view.activity.leaderDetailsActivity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.leaderboard.LeaderBoard
import com.gmpire.guruklub.data.model.leaderboard.LeaderBoardResponse
import com.gmpire.guruklub.data.model.leaderboard.MyPosition
import com.gmpire.guruklub.data.model.leaderboard.PlayedModelTestModel
import com.gmpire.guruklub.databinding.ActivityLeaderDetailsBinding
import com.gmpire.guruklub.util.DisplayUtil
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.profile.ProfileViewModel
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_leader_details.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception
import java.lang.NullPointerException

class LeaderDetailsActivity : BaseActivity() {


    private lateinit var alldetails: ArrayList<LeaderBoardResponse>
    var selectedQuickExamPos = 0
    private lateinit var selectedModelTestId: String
    private lateinit var viewmodel: ProfileViewModel
    private lateinit var playedModelTestModel: PlayedModelTestModel
    private lateinit var binding: ActivityLeaderDetailsBinding
    private var page: Int = 1
    private var selectedModeId: Int = -1
    private lateinit var leaderBoards: ArrayList<LeaderBoard>
    private var hasNextPage: Boolean = false
    private lateinit var firstThreeTopper: ArrayList<LeaderBoard>
    private lateinit var lastLeaders: ArrayList<LeaderBoard>

    init {
        firstThreeTopper = ArrayList<LeaderBoard>()
        lastLeaders = ArrayList<LeaderBoard>()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leader_details)

    }

    override fun viewRelatedTask() {

        viewmodel = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)

        try {
            if (intent.extras?.containsKey("leaderboard") == true) {
                playedModelTestModel =
                    intent.getSerializableExtra("leaderboard") as PlayedModelTestModel

                binding.examname.text = playedModelTestModel.title.toString()
                binding.totalParticipation.text = "Total participation - " + playedModelTestModel.total_participate.toString()
                selectedModelTestId = playedModelTestModel.id
                page = 1

                viewmodel.apiGetLeaderboard(
                    selectedModelTestId.toString(),
                    page.toString(),
                    this
                )

                setToolbar(this, binding.toolbar, "", true)
                binding.toolbar.appCompatImageViewLogo.visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        // To handle nestedScrollView pagination issue
        binding.allpartciptorname.viewTreeObserver?.addOnScrollChangedListener {
            val view =
                binding.allpartciptorname.getChildAt(binding.allpartciptorname.childCount - 1)

            val diff =
                view.bottom - (binding.allpartciptorname.height + binding.allpartciptorname.scrollY)

            if (diff == 0) {
                //your api call to fetch data
                if (page <= 10) {
                    if (hasNextPage) {
                        viewmodel.apiGetLeaderboard(
                            selectedModelTestId,
                            page.toString(),
                            this@LeaderDetailsActivity
                        )
                    }
                }
            }
        }

        ViewCompat.setNestedScrollingEnabled(rvLeaderBoard2, false)


    }

    override fun navigateToHome() {

    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
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

                                    var sizeofLeaderBoard = leaderBoards.size - 1

                                    binding.allpartciptorname.visibility = View.VISIBLE
                                    binding.Noparticipatonlayout.visibility = View.GONE
                                    if (page == 1) {
                                        if (sizeofLeaderBoard >= 2) {
                                            for (i in 0..2) {
                                                firstThreeTopper.add(leaderBoards[i])
                                            }

                                            initLeaderBoard(firstThreeTopper)

                                            lastLeaders.clear()

                                            for (i in 3..sizeofLeaderBoard) {
                                                lastLeaders.add(leaderBoards[i])
                                            }
                                            initHorizontalLeaderBoard(lastLeaders)

                                        } else if (sizeofLeaderBoard < 2) {
                                            initLeaderBoard(leaderBoards)
                                        }
                                    } else if (page > 1) {
                                        for (i in 0..sizeofLeaderBoard) {
                                            lastLeaders.add(leaderBoards[i])
                                        }
                                        initHorizontalLeaderBoard(lastLeaders)
                                    }

                                    Log.d("firstThreeTopper", firstThreeTopper.toString())

                                    binding.rvLeaderBoard2.visibility = View.VISIBLE
//                                    hasNextPage = baseData.data?.next_page != 0
//                                    page = baseData.data?.next_page ?: 0

                                } else {
                                    binding.allpartciptorname.visibility = View.GONE
                                    binding.Noparticipatonlayout.visibility = View.VISIBLE
                                }

                                if (baseData.data?.my_position != null) {
                                    binding.myPositionLayout1.visibility = View.VISIBLE
                                    binding.myPositionLayout4.visibility = View.GONE
                                    binding.myname.text =
                                        baseData.data?.my_position?.name.toString()
                                    binding.myexamdate.text =
                                        baseData.data?.my_position?.submit_date
                                    binding.serialNumber2.text =
                                        baseData.data?.my_position?.position.toString()
                                    binding.scoreCard.text =
                                        baseData.data?.my_position?.get_point + "/" + baseData.data?.my_position?.total_point
//                                    var name = baseData.data?.my_position?.name.toString()
                                    if (baseData.data?.my_position?.profile_pic != null) {
                                        Glide.with(this)
                                            .load(baseData.data?.my_position?.profile_pic)
                                            .override(100, 100)
                                            .into(binding.myimage1)
                                    } else {
                                        binding.myimage1.setImageResource(R.drawable.ic_placeholder_user)
                                    }

                                } else {
                                    binding.myPositionLayout4.visibility = View.VISIBLE
                                    binding.myPositionLayout1.visibility = View.GONE
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


    private fun initHorizontalLeaderBoard(lastToppers: ArrayList<LeaderBoard>) {
        if (page != 1) {
            this.leaderBoards.addAll(lastToppers)
            binding.rvLeaderBoard2.adapter?.notifyDataSetChanged()
        } else {
            val mlayoutmanager = LinearLayoutManager(this)
            binding.rvLeaderBoard2.layoutManager = mlayoutmanager
            binding.rvLeaderBoard2.itemAnimator = DefaultItemAnimator()
            binding.rvLeaderBoard2.adapter =
                BaseRecyclerAdapter(this, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {
                    }

                    override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                        return HorizontalLeaderBoardViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context)
                                , R.layout.item_leader_board_horizontal
                                , parent, false
                            )
                            , this@LeaderDetailsActivity
                        )
                    }

                    override fun loadMoreItem() {
//                        if (page <= 10) {
//                            if (hasNextPage) {
//                                /* viewmodel.apiGetLeaderboard(
//                                     selectedModelTestId.toString(),
//                                     page.toString(),
//                                     this@LeaderDetailsActivity
//                                 )*/
//                            }
//                        }
                    }
                }, lastToppers)
        }
    }

    private fun initLeaderBoard(leaderBoards: ArrayList<LeaderBoard>) {

        if (page == 1) {
            selectedQuickExamPos = 0
            binding.viewPagerLeader.clearOnPageChangeListeners()

            binding.viewPagerLeader.adapter =
                LeaderDetailsAdapter(this@LeaderDetailsActivity, leaderBoards)
            binding.viewPagerLeader.offscreenPageLimit = leaderBoards.size


            binding.viewPagerLeader.addOnPageChangeListener(object :
                ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    binding.leaderPagerPageIndicatorView.selection = position
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
            binding.viewPagerLeader.currentItem = 0
            binding.viewPagerLeader.pageMargin = DisplayUtil.dpToPx(16, this)
        }


    }


    override fun onClick(v: View?) {

    }
}