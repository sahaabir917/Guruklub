package com.gmpire.guruklub.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.gmpire.guruklub.view.fragment.news.NewsFragment


import java.util.ArrayList

/**
 * Created by Tahsin Rahman on 28/10/20.
 */


class TabsPagerAdapter2(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
          /*  0 -> {
                fragment = ()
            }
            1 -> {
                fragment = FragmentTwo()
            }
            2 -> {
                fragment = FragmentThree()
            }*/
        }
        return NewsFragment()
    }


    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: String? = null
        if (position == 0) {
            title = "First"
        } else if (position == 1) {
            title = "Second"
        } else if (position == 2) {
            title = "Third"
        }
        return title
    }
}