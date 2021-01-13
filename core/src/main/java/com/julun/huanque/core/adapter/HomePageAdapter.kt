package com.julun.huanque.core.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.constant.SquareTabType
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.core.ui.dynamic.UserDynamicFragment
import com.julun.huanque.core.ui.homepage.HomePageInformationFragment
import com.julun.huanque.core.ui.main.dynamic_square.DynamicTabFragment
import com.julun.huanque.core.ui.main.heartbeat.FavoriteFragment

/**
 *@创建者   dong
 *@创建时间 2020/12/22 17:14
 *@描述
 */
@SuppressLint("WrongConstant")
class HomePageAdapter(fm: FragmentManager, var userId:Long) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var mFragmentList = SparseArray<Fragment>()
    override fun getItem(position: Int): Fragment {
        return mFragmentList[position] ?: getFragment(position)
    }

    private fun getFragment(position: Int): Fragment {
        val fragment: Fragment =
            when (position) {
                0 -> {
                    HomePageInformationFragment.newInstance()
                }
                1 -> {
                    UserDynamicFragment.newInstance(userId)
                }
                else -> {
                    Fragment()
                }
            }

        mFragmentList.put(position, fragment)
        return fragment
    }

    override fun getCount() = 2
}