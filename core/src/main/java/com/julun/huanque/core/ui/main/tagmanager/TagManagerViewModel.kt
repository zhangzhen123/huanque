package com.julun.huanque.core.ui.main.tagmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.DynamicItemBean
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.bean.beans.TagManagerBean
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.SquareTabType
import com.julun.huanque.common.constant.TagTabType
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.ProgramService
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date 2019/7/16 19:29
 *
 *@Description 关注列表
 *
 */
class TagManagerViewModel : BaseViewModel() {

    private val programService: ProgramService by lazy { Requests.create(ProgramService::class.java) }

    val tagChange: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val currentTagList = mutableListOf<TagManagerBean>()
    val tabList: LiveData<ReactiveData<ArrayList<PagerTab>>> = queryState.switchMap {
        liveData {
            request({
                //todo

                currentTagList.addAll(
                    mutableListOf(
                        TagManagerBean("颜值", 10, SquareTabType.FOLLOW),
                        TagManagerBean("好身材", 5, TagTabType.goodFigure),
                        TagManagerBean("运动", 88, TagTabType.Sport),
                        TagManagerBean("风格", 0, TagTabType.Style),
                        TagManagerBean("游戏", 0, TagTabType.Game),
                        TagManagerBean("推荐"),
                        TagManagerBean("测试"),
                        TagManagerBean("测试")
                    )
                )
                tagChange.value = true
                val tabTitles: ArrayList<PagerTab> = arrayListOf()
                tabTitles.add(PagerTab("颜值", typeCode = SquareTabType.FOLLOW))
                tabTitles.add(PagerTab("推荐", typeCode = SquareTabType.RECOMMEND))
                tabTitles.add(PagerTab("颜值", typeCode = TagTabType.Beauty))
                tabTitles.add(PagerTab("好身材", typeCode = TagTabType.goodFigure))
                tabTitles.add(PagerTab("运动", typeCode = TagTabType.Sport))
                tabTitles.add(PagerTab("风格", typeCode = TagTabType.Style))
                tabTitles.add(PagerTab("游戏", typeCode = TagTabType.Game))
                emit(tabTitles.convertRtData())
            }, error = { e ->
                logger("报错了：$e")
                emit(e.convertError())
            }, final = {
                logger("最终返回")
            }, needLoadState = true)
        }

    }

    fun addTag(itemBean: TagManagerBean) {
        var isExist=false
        currentTagList.forEach {
            if (it.type == itemBean.type) {
                it.num = it.num + itemBean.num
                isExist=true
            }
        }
        if(!isExist){
            currentTagList.add(0,itemBean)
        }
        tagChange.value = true
    }

    fun removeTag(itemBean: TagManagerBean) {
        val iterator = currentTagList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.type == itemBean.type) {
                item.num = item.num - itemBean.num
                if (item.num <= 0) {
                    iterator.remove()
                }
            }
        }
        tagChange.value = true
    }

    fun removeTypeTag(type: String) {
        val iterator = currentTagList.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.type == type) {
                iterator.remove()
            }
        }
        tagChange.value = true
    }

}