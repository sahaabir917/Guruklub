package com.gmpire.guruklub.view.fragment.notification

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.data.model.BaseModel
import com.gmpire.guruklub.data.model.PaginationModel
import com.gmpire.guruklub.data.model.notification.NotificationModel
import com.gmpire.guruklub.databinding.FragmentNotificationBinding
import com.gmpire.guruklub.util.ConnectivityUtil
import com.gmpire.guruklub.util.ConstantField
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.util.fcm.MyFirebaseMessagingService
import com.gmpire.guruklub.view.activity.main.MainActivity
import com.gmpire.guruklub.view.activity.notificationDetails.NotificationDetailsActivity
import com.gmpire.guruklub.view.activity.questionSearch.QuestionSearchActivity
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.base.BaseRecyclerAdapter
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.gmpire.guruklub.view.fragment.helpAndSupport.HelpAndSupportFragment
import com.gmpire.guruklub.view.fragment.infoCentre.InfoCentreFragment
import com.gmpire.guruklub.view.fragment.profile.ProfileMainFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Response
import java.net.UnknownHostException

class NotificationFragment : BaseFragment(), View.OnClickListener {

    private var hasNextPage: Boolean = false
    private var page: Int = 1
    private lateinit var viewmodel: NotificationViewModel
    lateinit var binding: FragmentNotificationBinding
    private var title: String? = null
    private var notifications = ArrayList<NotificationModel>()

    companion object {
        private var f = NotificationFragment()
        fun newInstance(title: String): NotificationFragment {
            val args = Bundle()
            args.putString(ConstantField.ACCESS_TITLE, title)
            f.arguments = args
            Log.d("TAG", f.toString())
            return f
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false);
        return binding.root
    }

    override fun viewRelatedTask() {
        viewmodel =
            ViewModelProviders.of(this, viewModelFactory).get(NotificationViewModel::class.java)
        page = 1

        if (ConnectivityUtil.isOnline(activity)) {
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
            binding.rvNotifications.layoutManager = LinearLayoutManager(activity)
            binding.rvNotifications.adapter =
                BaseRecyclerAdapter(activity, object : IAdapterListener {
                    override fun <T> callBack(position: Int, model: T, view: View) {
                        model as NotificationModel
                        when (model.action) {
                            MyFirebaseMessagingService.NO_ACTION -> {
                            }
                            MyFirebaseMessagingService.HOME_SCREEN -> {
                                (activity as MainActivity).changeFragment(0)
                            }
                            MyFirebaseMessagingService.GAME_SCREEN -> {
                                (activity as MainActivity).changeFragment(1)
                            }
                            MyFirebaseMessagingService.LIBRARY_SCREEN_MAIN -> {
                                (activity as MainActivity).changeFragment(2)
                            }
                            MyFirebaseMessagingService.NOTIFICATION_DETAILS -> {
                                startActivity(
                                    Intent(
                                        activity,
                                        NotificationDetailsActivity::class.java
                                    ).putExtra("notification", Gson().toJson(model))
                                )
                            }
                            MyFirebaseMessagingService.INFO_CENTRE_MAIN -> {
                                (activity as MainActivity).changeFragment(3)
                            }
                            MyFirebaseMessagingService.PROFILE_MAIN -> {
                                (activity as MainActivity).navigateProfile(
                                    ProfileMainFragment.newInstance(0),
                                    "Profile"
                                    , 0
                                )
                            }
                            MyFirebaseMessagingService.PROFILE_LEADERBOARD -> {
                                (activity as MainActivity).navigateProfile(
                                    ProfileMainFragment.newInstance(1),
                                    "Profile",
                                    1
                                )
                            }
                            MyFirebaseMessagingService.PROFILE_ERROR -> {
                                (activity as MainActivity).navigateProfile(
                                    ProfileMainFragment.newInstance(2),
                                    "Profile",
                                    2
                                )
                            }
                            MyFirebaseMessagingService.PROFILE_BOOKMARKS -> {
                                (activity as MainActivity).navigateProfile(
                                    ProfileMainFragment.newInstance(3),
                                    "Profile", 3
                                )
                            }
                            MyFirebaseMessagingService.HELP_N_SUPPORT -> {
                                (activity as MainActivity).showDrawerNavigationScreen(
                                    HelpAndSupportFragment(),
                                    "Help And Support"
                                )
                            }
                            MyFirebaseMessagingService.HOME_SEARCH -> {
                                startActivity(
                                    Intent(
                                        activity,
                                        QuestionSearchActivity::class.java
                                    )
                                )
                            }
                            MyFirebaseMessagingService.PLAY_STORE -> {
                                val appPackageName =
                                    activity?.packageName
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
                        }
                    }

                    override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                        return NotificationViewHolder(
                            DataBindingUtil.inflate(
                                LayoutInflater.from(parent.context)
                                , R.layout.item_notification
                                , parent, false
                            )
                            , activity?.applicationContext!!
                        )
                    }

                    override fun loadMoreItem() {
                        if (hasNextPage) {
                            viewmodel.apiGetAllNotifications(
                                dataManager.mPref.prefGetUserInfo().id,
                                page.toString(),
                                dataManager.mPref.prefGetUserInfo().category_id.toString(),
                                activity!!
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


    override fun onError(err: Throwable, key: String) {
    }

    override fun onClick(v: View?) {

    }
}
