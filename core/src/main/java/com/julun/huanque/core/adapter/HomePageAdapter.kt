package com.julun.huanque.core.adapter

import android.content.Context
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.julun.huanque.core.ui.homepage.HomePageInformationFragment

/**
 *@创建者   dong
 *@创建时间 2020/12/22 17:14
 *@描述
 */
class HomePageAdapter(fm: FragmentManager, var context: Context) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var mFragmentList = SparseArray<Fragment>()
    override fun getItem(position: Int): Fragment {
        return mFragmentList[position] ?: getFragment(position)
    }

    private fun getFragment(position: Int): Fragment {
        val fragment = HomePageInformationFragment.newInstance()
        mFragmentList.put(position, fragment)
        return fragment
    }

    override fun getCount() = 2
}