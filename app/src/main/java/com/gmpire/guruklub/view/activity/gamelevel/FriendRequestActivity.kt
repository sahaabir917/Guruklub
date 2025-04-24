package com.gmpire.guruklub.view.activity.gamelevel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.ActivityFriendRequestBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.friendrequest.FriendRequestFragment
import com.gmpire.guruklub.view.activity.newsDetails.NewsDetailsActivity
import com.gmpire.guruklub.view.base.BaseActivity
import com.google.firebase.auth.UserInfo
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Response

class FriendRequestActivity : BaseActivity() {
    private var isFromNotification: Boolean = false
    private var users_model: UserInfo? = null
    private lateinit var binding : ActivityFriendRequestBinding

    override fun viewRelatedTask() {

        try {
            if (intent.hasExtra("friend_request_object")) {
                val newsString = intent.extras?.getString("friend_request_object")
                users_model = Gson().fromJson(newsString, UserInfo::class.java)
                isFromNotification = true
                var friendRequestFragment = FriendRequestFragment.newInstance(isFromNotification)
                val transaction =
                    supportFragmentManager.beginTransaction()
                transaction.replace(R.id.play_with_friend_fragment, friendRequestFragment)
                transaction.commit()
            }
            else{
                isFromNotification = false
                var friendRequestFragment = FriendRequestFragment.newInstance(isFromNotification)
                val transaction =
                    supportFragmentManager.beginTransaction()
                transaction.replace(R.id.play_with_friend_fragment, friendRequestFragment)
                transaction.commit()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }






    }

    override fun navigateToHome() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_friend_request)
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {

    }
}