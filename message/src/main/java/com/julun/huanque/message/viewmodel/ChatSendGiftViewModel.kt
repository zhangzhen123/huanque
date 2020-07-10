package com.julun.huanque.message.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ReactiveData
import com.julun.huanque.common.bean.beans.ChatGift
import com.julun.huanque.common.bean.beans.ChatGiftInfo
import com.julun.huanque.common.bean.beans.ChatGroupGift
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/9 19:54
 *
 *@Description: 聊天赠送礼物
 *
 */
class ChatSendGiftViewModel : BaseViewModel() {
    private val service: SocialService by lazy { Requests.create(SocialService::class.java) }
    //礼物分页数目
    private val pageLimit=8
    val giftList: LiveData<ReactiveData<ChatGiftInfo>> = queryState.switchMap { type ->
        liveData {

            request({
                val homeListData = service.giftsInfo().dataConvert()
                //将数据分页处理
                homeListData.giftList.sliceBySubLengthNew(pageLimit).forEachIndexed { _, list ->
                    homeListData.viewPagerData.add(ChatGroupGift(list as MutableList<ChatGift>))
                }
                emit(ReactiveData(NetStateType.SUCCESS, homeListData))
            }, error = { e ->
                logger("报错了：$e")
                emit(ReactiveData(NetStateType.ERROR, error = e.errorMsg()))
            }, final = {
                logger("最终返回")
            }, needLoadState = type == QueryType.INIT)

        }

    }


}