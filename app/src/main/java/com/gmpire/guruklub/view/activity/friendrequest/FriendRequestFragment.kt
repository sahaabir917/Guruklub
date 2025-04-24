package com.gmpire.guruklub.view.activity.friendrequest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.FragmentFriendRequestBinding
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.adapter.TabsPagerAdapter
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.fragment.home.HomeFragment
import com.google.android.material.tabs.TabLayout
import okhttp3.ResponseBody
import retrofit2.Response


class FriendRequestFragment : BaseFragment() {
    private lateinit var binding: FragmentFriendRequestBinding
    private lateinit var tabAdapter: TabsPagerAdapter
    private var currenttab: Int = 0
    var width: Int = 0
    var isFromNotification: Boolean = false
    override fun viewRelatedTask() {
        initTabLayout()
    }

    private fun initTabLayout() {
        if (binding.viewPager.adapter != null) {

        } else {
            val myFriendFragment = MyFriendFragment()
            val addFriendFragment = AddFriendFragment()
            val pendingRecivedPendingFriendRequest = RecivedPendingFriendRequest()
            val PendingSentFriendRequest = RequestFriendFragment()
            val previousQuestionBatchFragment = HomeFragment()

            binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
            binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            childFragmentManager.executePendingTransactions()

            tabAdapter = TabsPagerAdapter(childFragmentManager)
            tabAdapter
                .addFragment(
                    myFriendFragment, "My Friends"
                )
                .addFragment(
                    addFriendFragment, "Add Friend"
                )
                .addFragment(
                    pendingRecivedPendingFriendRequest, "Requests"
                )
                .addFragment(
                    PendingSentFriendRequest, "Sent Requests"
                )


            binding.viewPager.adapter = tabAdapter
            binding.tabLayout.setupWithViewPager(binding.viewPager)
        }
        if (isFromNotification) {
            currenttab = 2
        } else {
            currenttab = 0
        }
        binding.viewPager.adapter?.notifyDataSetChanged()

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currenttab = position
            }

        })
        binding.viewPager.currentItem = currenttab
        binding.viewPager.offscreenPageLimit = 1

    }

    override fun onAttach(context: Context) {
        isFromNotification = arguments?.getBoolean("isFromNotification", false) ?: false
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_friend_request, container, false)
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_friend_request, container, false)
        return binding.root
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {

    }

    companion object {

        @JvmStatic
        fun newInstance(isFromNotification: Boolean) =
            FriendRequestFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isFromNotification", isFromNotification)
                }
            }
    }
}