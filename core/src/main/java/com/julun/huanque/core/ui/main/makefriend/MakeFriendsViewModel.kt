package com.julun.huanque.core.ui.main.makefriend

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListLiveData
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.request
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

    private val service: HomeService by lazy {
        Requests.create(HomeService::class.java)
    }
    var offset = 0
    val stateList: LiveData<RootListLiveData<HomeItemBean>> = queryState.switchMap { type ->
        liveData<RootListLiveData<HomeItemBean>> {
            if (type == QueryType.REFRESH) {
                offset = 0
            }
            request({
//                    val user = service.queryUserDetailInfo(SessionForm()).dataConvert()
                delay(200)
                //todo test

                val list = arrayListOf<HomeItemBean>()
                list.add(HomeItemBean(HomeItemBean.HEADER, Any()))
                repeat(10) {
                    list.add(HomeItemBean(HomeItemBean.NORMAL, Any()))
                }
                list.add(HomeItemBean(HomeItemBean.GUIDE_TO_ADD_TAG, Any()))
                list.add(HomeItemBean(HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION, Any()))
                //todo test
                val hasMore = offset <= 20
                val rList = RootListLiveData(type != QueryType.LOAD_MORE, list, hasMore)
                offset += list.size
                emit(rList)
            }, error = { e ->
                logger("报错了：$e")
            }, final = {
                logger("最终返回")
            }, loadState = if (type == QueryType.INIT) loadState else null)
        }

    }

}

