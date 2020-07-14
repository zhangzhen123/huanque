package com.julun.huanque.core.ui.recharge

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.julun.huanque.core.ui.recharge.RechargeCenterFragment

/**
 * Created by my on 2018/07/26 0026.
 */
class RechargePagerAdapter(fm: androidx.fragment.app.FragmentManager, context: Context) : FragmentStatePagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var tabList = ArrayList<String>()//当前分类的列表
    private var fragmentMap = HashMap<Int, RechargeCenterFragment>()

    fun setTabList(list: ArrayList<String>) {
        tabList = list
    }

    override fun getItem(position: Int): Fragment {
        if (fragmentMap[position] != null) {
            var fragment = fragmentMap[position]
            if (fragment != null) {
                return fragment
            }
        }
        return getFragment(position)
    }

    private fun getFragment(position: Int): RechargeCenterFragment {
//        if (tabList == null || tabList.size <= position) {
//            return null
//        }
        val fragment = RechargeCenterFragment.newInstance(position)
        fragmentMap.put(position, fragment)
        return fragment
    }

    override fun getCount(): Int {
        return tabList.size
    }

}