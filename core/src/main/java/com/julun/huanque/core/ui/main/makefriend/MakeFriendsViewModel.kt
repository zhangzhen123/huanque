package com.julun.huanque.core.ui.main.makefriend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.HeadNavigateInfo
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.HomeRecomItem
import com.julun.huanque.common.bean.beans.HomeRemind
import com.julun.huanque.common.bean.forms.RecomListForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.suger.*
import kotlinx.coroutines.delay


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:07
 *
 *@Description: MakeFriendsViewModel 交友逻辑
 *
 */
class MakeFriendsViewModel : BaseViewModel() {

    companion object {

        //添加资料完善引导的位置
        const val GUIDE_INDEX_01 = 14

        //添加tag引导的位置
        const val GUIDE_INDEX_02 = 29
    }

    private val service: HomeService by lazy {
        Requests.create(HomeService::class.java)
    }
    private var offset: Int? = 0
    var curRemind: HomeRemind? = null
    var needGuide1 = true
    var needGuide2 = true

    //引导是否被玩家主动关闭了 主动关闭的在app杀掉前再也不出现
    var guideCloseByUser1 = false
    var guideCloseByUser2 = false

    //记录全部的列表
    private var totalList = mutableListOf<HomeRecomItem>()

    val flowerPic: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    var currentRealOffset:Int?=null
    val stateList: LiveData<ReactiveData<RootListData<HomeItemBean>>> = queryState.switchMap { type ->
        liveData {
            val form: RecomListForm = if (type != QueryType.LOAD_MORE) {
                totalList.clear()
                RecomListForm()
            } else {
                RecomListForm(offset,currentRealOffset)
            }

            request({
                val homeListData = service.homeRecom(form).dataConvert()
                val list = arrayListOf<HomeItemBean>()
                //去重操作
                val resultList = homeListData.list.removeDuplicate(totalList)
                totalList.addAll(resultList)
                if (type != QueryType.LOAD_MORE) {
                    val headNavigateInfo = HeadNavigateInfo(homeListData.modules, homeListData.taskBar)
                    list.add(HomeItemBean(HomeItemBean.HEADER, headNavigateInfo))

                    resultList.forEach {
                        list.add(HomeItemBean(HomeItemBean.NORMAL, it))

                    }
                    //记录第一次返回的引导参数
                    curRemind = homeListData.remind
                    if (!guideCloseByUser1) {
                        needGuide1 = true
                    }
                    if (!guideCloseByUser2) {
                        needGuide2 = true
                    }
                    flowerPic.value = homeListData.flowerPic
                } else {
                    resultList.forEach {
                        list.add(HomeItemBean(HomeItemBean.NORMAL, it))
                    }
                }
                //处理引导插入
                if (totalList.size >= GUIDE_INDEX_01) {
                    if (curRemind?.coverRemind == true) {
                        if (needGuide1) {
                            needGuide1 = false
                            logger("添加完善引导12")
                            val index = GUIDE_INDEX_01 - (totalList.size - list.size)
                            list.add(index, HomeItemBean(HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION, curRemind!!))
                        }

                    } else if (curRemind?.tagRemind == true) {
                        if (needGuide2) {
                            needGuide2 = false
                            logger("添加tag引导11")
                            val index = GUIDE_INDEX_01 - (totalList.size - list.size)
                            list.add(index, HomeItemBean(HomeItemBean.GUIDE_TO_ADD_TAG, Any()))
                        }

                    }

                }
                if (totalList.size >= GUIDE_INDEX_02 && curRemind?.tagRemind == true) {
                    if (needGuide2) {
                        needGuide2 = false
                        logger("添加完善引导22")
                        val index = GUIDE_INDEX_02 - (totalList.size - list.size)
                        list.add(index, HomeItemBean(HomeItemBean.GUIDE_TO_ADD_TAG, Any()))
                    }
                }
                val rList = RootListData(isPull = type != QueryType.LOAD_MORE, list = list, hasMore = homeListData.hasMore)
                offset = homeListData.offset
                currentRealOffset=homeListData.realOffset

                emit(ReactiveData(NetStateType.SUCCESS, rList,queryType = type))
            }, error = { e ->
                logger("报错了：$e")
                emit(e.convertListError(queryType = type))
            }, needLoadState = type == QueryType.INIT)

        }

    }


}

