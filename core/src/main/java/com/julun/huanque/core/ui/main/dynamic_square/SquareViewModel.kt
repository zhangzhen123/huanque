package com.julun.huanque.core.ui.main.dynamic_square

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.SquareTabType
import com.julun.huanque.common.helper.StorageHelper
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
class SquareViewModel : BaseViewModel() {

    //协程请求示例
    val tabList: LiveData<ReactiveData<ArrayList<PagerTab>>> = queryState.switchMap {
        liveData {
            request({

                val tabObj = StorageHelper.getProgramTabObj()
                val tabTitles: ArrayList<PagerTab> = arrayListOf()
                tabTitles.add(PagerTab("关注", typeCode = SquareTabType.FOLLOW))
                tabTitles.add(PagerTab("推荐", typeCode = SquareTabType.RECOMMEND))
                emit(tabTitles.convertRtData())
            }, error = { e ->
                logger("报错了：$e")
                emit(e.convertError())
            }, final = {
                logger("最终返回")
            }, needLoadState = true)
        }

    }

}