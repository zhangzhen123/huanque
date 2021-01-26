package com.julun.huanque.core.adapter

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.core.ui.homepage.HomeTownCircumFragment

/**
 *@创建者   dong
 *@创建时间 2020/12/28 20:32
 *@描述 家乡周边Adapter
 */
class HomeTownCircumAdapter(fm: FragmentManager, val typeList: MutableList<String>) : FragmentStatePagerAdapter(fm) {
    private val mFragmentList = SparseArray<BaseFragment>()

    private fun getFragment(position: Int): BaseFragment {
        val type = typeList[position]
        val frag = HomeTownCircumFragment.newInstance(type)
        mFragmentList.put(position, frag)
        return frag
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position] ?: getFragment(position)
    }

    override fun getCount(): Int {
        return typeList.size
    }
}