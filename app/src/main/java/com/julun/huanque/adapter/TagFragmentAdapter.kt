package com.julun.huanque.adapter

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.fragment.SelectTagFragment

/**
 *@创建者   dong
 *@创建时间 2020/12/17 14:28
 *@描述
 */
class TagFragmentAdapter(act: FragmentActivity, val typeList: MutableList<String>) :
    FragmentStateAdapter(act) {
    private val mFragmentList = SparseArray<BaseFragment>()

    override fun getItemCount() = typeList.size

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position] ?: getFragment(position)
    }

    private fun getFragment(position: Int): BaseFragment {
        val type = typeList[position]
        val frag = SelectTagFragment(type)
        mFragmentList.put(position, frag)
        return frag
    }
}