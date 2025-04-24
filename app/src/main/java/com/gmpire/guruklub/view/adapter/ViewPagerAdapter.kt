package com.gmpire.guruklub.view.adapter

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter


class ViewPagerAdapter(supportFragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private var mFragmentList = ArrayList<Fragment>()
    private var mFragmentTitleList = ArrayList<String>()

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    fun addFragment(fragment: Fragment, title: String, position: Int) {
        mFragmentList.add(position, fragment)
        mFragmentTitleList.add(title)
    }

    fun removeFragment(fragment: Fragment, title: String) {
        mFragmentList.remove(fragment)
        mFragmentTitleList.remove(title)
    }

    fun removeAll() {
        mFragmentList.clear()
        mFragmentTitleList.clear()
    }

    fun removeFragmentAt(position: Int) {
        mFragmentList.removeAt(position)
        mFragmentTitleList.removeAt(position)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mFragmentTitleList[position]
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }


    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun restoreState(arg0: Parcelable?, arg1: ClassLoader?) {
    }
}