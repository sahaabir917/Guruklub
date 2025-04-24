package com.gmpire.guruklub.view.activity.notification

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.notification.NotificationModel
import com.gmpire.guruklub.databinding.ActivityNotificationBinding
import com.gmpire.guruklub.util.ConnectivityUtil
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService
import com.gmpire.guruklub.view.activity.gameheart.HeartAddActivity
import com.gmpire.guruklub.view.activity.gamelevel.GameLevelActivity
import com.gmpire.guruklub.view.activity.login.AllVideosActivity
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.notificationDetails.NotificationDetailsActivity
import com.gmpire.guruklub.view.activity.profile.ErrorActivity
import com.gmpire.guruklub.view.activity.profile.FavouriteActivity
import com.gmpire.guruklub.view.activity.profile.LeaderBoardActivity
import com.gmpire.guruklub.view.activity.profile.ProfileActivity
import com.gmpire.guruklub.view.activity.questionSearch.QuestionSearchActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseActivity
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.fragment.notification.NotificationViewHolder
import com.gmpire.guruklub.view.fragment.notification.NotificationViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response

class NotificationActivity : BaseActivity() {

    private var hasNextPage: Boolean = false
    private var page: Int = 1
    private lateinit var viewmodel: NotificationViewModel
    lateinit var binding: ActivityNotificationBinding
    private var title: String? = null
    private var notifications = ArrayList<NotificationModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification)
    }

    override fun viewRelatedTask() {
        viewmodel =
            ViewModelProviders.of(this, viewModelFactory).get(NotificationViewModel::class.java)
        page = 1

        setToolbar(this, binding.toolbar, "Notifications", true)

        if (ConnectivityUtil.isOnline(this)) {
            viewmodel.apiGetAllNotifications(
                dataManager.mPref.prefGetUserInfo().id,
                page.toString(),
                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                this
            )
        } else {
            //showAlertDialog()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            page = 1
            viewmodel.apiGetAllNotifications(
                dataManager.mPref.prefGetUserInfo().id,
                page.toString(),
                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                this
            )
        }

        binding.textViewReadAll.paint?.isUnderlineText = true
    }

    override fun navigateToHome() {
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {
        when (key) {
            "apiGetAllNotifications" -> {
                val type =
                    object :
                        TypeToken<BaseModel<PaginationModel<ArrayList<NotificationModel>>>>() {}.type
                result.data?.body()?.let {

                    val baseData =
                        Gson().fromJson<BaseModel<PaginationModel<ArrayList<NotificationModel>>>>(
                            result.data.body()?.string(),
                            type
                        )

                    if (baseData.status_code == 200 && baseData.data?.data != null) {
                        if (baseData.data?.data?.size ?: 0 > 0) {
                            initNotifications(baseData?.data?.data ?: arrayListOf())
                            hasNextPage = baseData.data?.next_page != 0
                            page = baseData.data?.next_page ?: 0

                            binding.rvNotifications.visibility = View.VISIBLE
                            binding.emptyMessage.visibility = View.GONE
                        } else {
                            binding.rvNotifications.visibility = View.GONE
                            binding.emptyMessage.visibility = View.VISIBLE
                        }

                    } else {
                        binding.rvNotifications.visibility = View.GONE
                        binding.emptyMessage.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    private fun initNotifications(notifications: ArrayList<NotificationModel>) {
        if (page != 1) {
            this.notifications.addAll(notifications)
            binding.rvNotifications.adapter?.notifyDataSetChanged()
        } else {
            this.notifications = notifications
            binding.rvNotifications.layoutManager = LinearLayoutManager(this)
            binding.rvNotifications.adapter =
                BaseRecyclerAdapter(this, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {
                        model as NotificationModel
                        when (model.action) {
                            MyFirebaseMessagingService.NO_ACTION -> {
                                if (model.hasLink()) {
                                    intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse(model.link)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            }
                            MyFirebaseMessagingService.HOME_SCREEN -> {
                                val intent = Intent(
                                    this@NotificationActivity,
                                    MainActivity::class.java
                                ).putExtra(
                                    "goto",
                                    MyFirebaseMessagingService.HOME_SCREEN
                                )
                                finishAffinity()
                                startActivity(intent)
                            }
                            MyFirebaseMessagingService.GAME_SCREEN -> {
                                val intent = Intent(
                                    this@NotificationActivity,
                                    MainActivity::class.java
                                ).putExtra(
                                    "goto",
                                    MyFirebaseMessagingService.GAME_SCREEN
                                )
                                finishAffinity()
                                startActivity(intent)
                            }
                            MyFirebaseMessagingService.LIBRARY_SCREEN_MAIN -> {
                                val intent = Intent(
                                    this@NotificationActivity,
                                    MainActivity::class.java
                                ).putExtra(
                                    "goto",
                                    MyFirebaseMessagingService.LIBRARY_SCREEN_MAIN
                                )
                                finishAffinity()
                                startActivity(intent)
                            }
                            MyFirebaseMessagingService.NOTIFICATION_DETAILS -> {
                                startActivity(
                                    Intent(
                                        this@NotificationActivity,
                                        NotificationDetailsActivity::class.java
                                    ).putExtra("notification", Gson().toJson(model))
                                )
                            }
                            MyFirebaseMessagingService.INFO_CENTRE_MAIN -> {
                                val intent = Intent(
                                    this@NotificationActivity,
                                    MainActivity::class.java
                                ).putExtra(
                                    "goto",
                                    MyFirebaseMessagingService.INFO_CENTRE_MAIN
                                )
                                finishAffinity()
                                startActivity(intent)
                            }
                            MyFirebaseMessagingService.PROFILE_MAIN -> {
                                startActivity(
                                    Intent(
                                        this@NotificationActivity,
                                        ProfileActivity::class.java
                                    )
                                )
                            }
                            MyFirebaseMessagingService.PROFILE_LEADERBOARD -> {
                                startActivity(
                                    Intent(
                                        this@NotificationActivity,
                                        LeaderBoardActivity::class.java
                                    )
                                )
                            }
                            MyFirebaseMessagingService.PROFILE_ERROR -> {
                                startActivity(
                                    Intent(
                                        this@NotificationActivity,
                                        ErrorActivity::class.java
                                    )
                                )
                            }
                            MyFirebaseMessagingService.PROFILE_BOOKMARKS -> {
                                startActivity(
                                    Intent(
                                        this@NotificationActivity,
                                        FavouriteActivity::class.java
                                    )
                                )
                            }
                            MyFirebaseMessagingService.HELP_N_SUPPORT -> {

                            }
                            MyFirebaseMessagingService.NOTIFICATION_DETAILS -> {
                                startActivity(
                                    Intent(
                                        this@NotificationActivity,
                                        NotificationDetailsActivity::class.java
                                    ).putExtra("notification", Gson().toJson(model))
                                )
                            }
                            MyFirebaseMessagingService.HOME_SEARCH -> {
                                startActivity(
                                    Intent(
                                        this@NotificationActivity,
                                        QuestionSearchActivity::class.java
                                    )
                                )
                            }
                            MyFirebaseMessagingService.GURUKLUB_GAME -> {
                                startActivity(
                                    Intent(
                                        this@NotificationActivity,
                                        GameLevelActivity::class.java
                                    )
                                )
                            }
                            MyFirebaseMessagingService.PLAY_STORE -> {
                                val appPackageName =
                                    packageName
                                try {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=$appPackageName")
                                        )
                                    )
                                } catch (anfe: ActivityNotFoundException) {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                                        )
                                    )
                                }
                            }

                            MyFirebaseMessagingService.GAME_PURCHASE_HEART ->{
                                startActivity(
                                    Intent(this@NotificationActivity, HeartAddActivity::class.java).putExtra(
                                        "is_from_game",
                                        "no"
                                    )
                                )
                            }

                            MyFirebaseMessagingService.Add_HEARTS ->{
                                startActivity(
                                    Intent(this@NotificationActivity, HeartAddActivity::class.java).putExtra(
                                        "is_from_game",
                                        "yes"
                                    )
                                )
                            }

                            MyFirebaseMessagingService.VIDEOS ->{
                                startActivity(Intent(this@NotificationActivity, AllVideosActivity::class.java))
                            }

                            MyFirebaseMessagingService.DRAWER_MENU ->{
                                finish()
                                startActivity(Intent(this@NotificationActivity,MainActivity::class.java).putExtra("goto",
                                    MyFirebaseMessagingService.DRAWER_MENU
                                ))
                            }

                        }
                    }

                    override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                        return NotificationViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context)
                                , R.layout.item_notification
                                , parent, false
                            )
                            , this@NotificationActivity
                        )
                    }

                    override fun loadMoreItem() {
                        if (hasNextPage) {
                            viewmodel.apiGetAllNotifications(
                                dataManager.mPref.prefGetUserInfo().id,
                                page.toString(),
                                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                                this@NotificationActivity
                            )
                        }
                    }

                }, notifications)
        }
    }


    override fun onLoading(isLoader: Boolean, key: String) {
        if (key == "apiGetAllNotifications") {
            if (page != 1) {
                if (isLoader) {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                } else {
                    binding.loadingProgressBar.visibility = View.GONE
                }
            } else {
                binding.swipeRefreshLayout.isRefreshing = isLoader
            }
        }
    }

    override fun onClick(v: View?) {

    }

}