package com.julun.huanque.message.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.julun.huanque.common.bean.beans.UserDataTab
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.message.fragment.ContactsFragment
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by zz on 2016/1/6.
 * 主页面界面的适配器
 */
class ProgramFragmentAdapter(fm: FragmentManager,var context: Context) : FragmentPagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var fragmentMap = HashMap<Int, ContactsFragment>()
    private var typeList: List<UserDataTab> = ArrayList()//当前分类的列表


    fun setTypeList(tagList: List<UserDataTab>) {
        this.typeList = tagList
    }

    fun getTypeList(): List<UserDataTab> {
        return typeList
    }

    override fun getCount(): Int {
        return typeList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentMap[position] ?: getFragment(position)
    }

    private fun getFragment(position: Int): ContactsFragment {
//        if (typeList == null || typeList.size <= position) {
//            return null
//        }
        val item = typeList[position]
        val fragment = ContactsFragment.newInstance(item.userDataTabType)

        fragmentMap.put(position, fragment)
        return fragment
    }

    /**
     * 获取当前显示的Fragment，如果tab 未设置  返回null
     */
    fun getCurrentFragment(position: Int): Fragment? {
        return if (ForceUtils.isIndexNotOutOfBounds(position, typeList)) {
            getItem(position)
        } else {
            null
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return typeList[position].userTabName
    }

}
