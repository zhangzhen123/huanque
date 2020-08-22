package com.julun.huanque.core.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.julun.huanque.common.bean.beans.UserDataTab
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.core.ui.live.fragment.SendGiftFragment
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by zz on 2016/1/6.
 * 主页面界面的适配器
 */
class PlumFlowerFragmentAdapter(val fragmentList : List<Fragment>,fm: FragmentManager, context: Context) : FragmentStatePagerAdapter(fm) {


//    fun setTypeList(tagList: List<UserDataTab>) {
//        this.typeList = tagList
//    }
//
//    fun getTypeList(): List<UserDataTab> {
//        return typeList
//    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

}
