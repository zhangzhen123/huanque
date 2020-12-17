package com.julun.huanque.core.ui.main.heartbeat

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.HomeTabType
import com.julun.huanque.common.suger.convertError
import com.julun.huanque.common.suger.convertRtData
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/30 20:05
 *
 *@Description: HomeViewModel 首页逻辑处理
 *
 */
class HeartbeatViewModel : BaseViewModel() {

    //协程请求示例
    val tabList: LiveData<ReactiveData<ArrayList<PagerTab>>> = queryState.switchMap {
        liveData {
            request({

//                val tabObj = StorageHelper.getProgramTabObj()
                val tabTitles: ArrayList<PagerTab> = arrayListOf()
//                if (tabObj != null && tabObj.homeCategories.isNotEmpty()) {
//                    tabTitles.addAll(tabObj.homeCategories)
//                } else {
//                    logger("没有取到存储的tab 使用默认值")
//                    tabTitles.add(ProgramTab("热门", typeCode = HomeTabType.Hot))
//                }
                //固定在尾部加个 关注tab
                tabTitles.add(PagerTab("附近", typeCode = HomeTabType.Nearby))
                tabTitles.add(PagerTab("喜欢", typeCode = HomeTabType.Favorite))

                emit(tabTitles.convertRtData())
            }, error = { e ->
                logger("报错了：$e")
                emit(e.convertError())
            }, final = {
                logger("最终返回")
            }, needLoadState = it== QueryType.INIT)
        }

    }

}