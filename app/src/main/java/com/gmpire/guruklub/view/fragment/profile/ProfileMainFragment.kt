package com.gmpire.guruklub.view.fragment.profile

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gmpire.guruklub.R
import com.gmpire.guruklub.databinding.FragmentHomeBinding
import com.gmpire.guruklub.databinding.FragmentProfileMainBinding
import com.gmpire.guruklub.util.ConstantField
import com.gmpire.guruklub.util.LiveDataResult
import com.gmpire.guruklub.view.activity.profile.ErrorFragment
import com.gmpire.guruklub.view.activity.profile.LeaderBoardFragment
import com.gmpire.guruklub.view.activity.profile.ProfileFragment
import com.gmpire.guruklub.view.adapter.TabsPagerAdapter
import com.gmpire.guruklub.view.base.BaseFragment
import com.gmpire.guruklub.view.fragment.favourite.FavouriteFragment
import com.gmpire.guruklub.view.fragment.home.HomeFragment
import com.google.android.material.tabs.TabLayout
import okhttp3.ResponseBody
import retrofit2.Response

private const val NAVIGATE_TO = "navigate_to"

class ProfileMainFragment : BaseFragment() {

    fun ProfileMainFragment() {
        // doesn't do anything special
    }

    companion object {
        private var f = ProfileMainFragment()
        @JvmStatic
        fun newInstance(navigateTo: Int): ProfileMainFragment {
            val args = Bundle()
            args.putInt(NAVIGATE_TO, navigateTo)
            if (!f.isAdded) {
                f.arguments = args
                Log.d("TAG", f.toString())
            }
            return f
        }
    }

    lateinit var binding: FragmentProfileMainBinding
    var navigate = -1
    override fun viewRelatedTask() {
        try {
            if (arguments?.containsKey(NAVIGATE_TO)!!) {
                navigate = arguments?.getInt(NAVIGATE_TO) ?: 0
            }
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            initTablayout(navigate)
        }
    }

    private fun initTablayout(navigateTo: Int) {
        binding.viewPager.adapter = TabsPagerAdapter(childFragmentManager)
            .addFragment(
                ProfileFragment()
                , "Profile"
            )
            .addFragment(
                LeaderBoardFragment()
                , "LeaderBoard"
            )
            .addFragment(
                ErrorFragment()
                , "Errors"
            )
            .addFragment(
                FavouriteFragment()
                , "Bookmarks"
            )
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.tabLayout.tabMode = TabLayout.MODE_FIXED
        binding.viewPager.offscreenPageLimit = 4


        if (navigateTo > -1) {
            binding.viewPager.setCurrentItem(navigateTo, true)
        }
    }

    fun navigate(navigateTo: Int) {
        if (::binding.isInitialized) {
            if (binding.viewPager.adapter?.count!! > navigateTo) {
                binding.viewPager.setCurrentItem(navigateTo, true)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_profile_main, container, false)
        return binding.root
    }

    override fun onSuccess(result: LiveDataResult<Response<ResponseBody>>, key: String) {

    }

    override fun onClick(v: View?) {

    }
}