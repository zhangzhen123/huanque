package com.julun.huanque.core.ui.main.makefriend

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.*
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.UserLevelInfo
import com.julun.huanque.common.bean.forms.RecomListForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.HomeService
import com.julun.huanque.common.suger.dataConvert
import com.julun.huanque.common.suger.errorMsg
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
    val stateList: LiveData<ReactiveData<RootListLiveData<HomeItemBean>>> = queryState.switchMap { type ->
        liveData {
            if (type == QueryType.REFRESH) {
                offset = 0
            }
            request({
                val user = service.homeInfo().dataConvert()

                val recList = runCatching<RootListLiveData<UserLevelInfo>> {
                    service.homeRecomList(RecomListForm(offset)).dataConvert()
                }.getOrNull()
                //todo test
                val list = arrayListOf<HomeItemBean>()
                list.add(HomeItemBean(HomeItemBean.HEADER, user))
                repeat(10) {
                    list.add(HomeItemBean(HomeItemBean.NORMAL, Any()))
                }
                list.add(HomeItemBean(HomeItemBean.GUIDE_TO_ADD_TAG, Any()))
                list.add(HomeItemBean(HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION, Any()))
                //todo test
                val hasMore = offset <= 20
                val rList = RootListLiveData(type != QueryType.LOAD_MORE, list, hasMore)
                offset += list.size
                emit(ReactiveData(NetStateType.SUCCESS, rList))
            }, error = { e ->
                logger("报错了：$e")
                emit(ReactiveData(NetStateType.ERROR, error = e.errorMsg()))
            }, final = {
                logger("最终返回")
            }, needLoadState = type == QueryType.INIT)
        }

    }

}

