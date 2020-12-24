package com.julun.huanque.core.ui.main.heartbeat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.LikeForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.suger.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:05
 *
 *@Description: HomeViewModel 首页逻辑处理
 *
 */
class FavoriteViewModel : BaseViewModel() {

    private val service: HomeService by lazy { Requests.create(HomeService::class.java) }

    //协程请求示例
    val firstListData: LiveData<ReactiveData<FavoriteListData<FavoriteUserBean>>> = queryState.switchMap {
        liveData {
            request({
                //首次请求默认给0
                val rt = service.getLikeList(LikeForm(0)).dataConvert()
//                firstListData.value=rt.list

//                val tabTitles: ArrayList<PagerTab> = arrayListOf()
//                if (tabObj != null && tabObj.homeCategories.isNotEmpty()) {
//                    tabTitles.addAll(tabObj.homeCategories)
//                } else {
//                    logger("没有取到存储的tab 使用默认值")
//                    tabTitles.add(PagerTab("热门", typeCode = HomeTabType.Hot))
////                    tabTitles.add(ProgramTab("舞神", typeCode = HomeTabType.Dancer))
//                }
//                //固定在尾部加个 关注tab
//
//                emit(tabTitles.convertRtData())
                rt.isPull = true
                emit(rt.convertRtData())
            }, error = { e ->
                logger("报错了：$e")
                emit(e.convertError())
            }, final = {
                logger("最终返回")
            }, needLoadState = it == QueryType.INIT)
        }

    }

}