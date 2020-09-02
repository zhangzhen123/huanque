package com.julun.huanque.message.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.beans.ConversationBasicBean
import com.julun.huanque.common.bean.beans.IntimateBean
import com.julun.huanque.common.commonviewmodel.BaseViewModel

/**
 *@创建者   dong
 *@创建时间 2020/7/9 19:10
 *@描述 亲密度详情
 */
class IntimateDetailViewModel : BaseViewModel() {

    val basicBean: MutableLiveData<ConversationBasicBean> by lazy { MutableLiveData<ConversationBasicBean>() }

    //对方ID
    var friendId = 0L

}