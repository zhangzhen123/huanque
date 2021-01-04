package com.julun.huanque.common.manager

import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.viewmodel.HuanQueViewModel
import com.julun.huanque.common.viewmodel.TagManagerViewModel
import com.julun.huanque.common.viewmodel.VoiceChatViewModel

/**
 *@创建者   dong
 *@创建时间 2020/9/21 20:45
 *@描述 保存HuanqueViewModel的manager
 */
object HuanViewModelManager {
    //派单ViewModel
    val huanQueViewModel = HuanQueViewModel(CommonInit.getInstance().getApp())

    //语音ViewModel
    val mVoiceChatViewModel: VoiceChatViewModel by lazy { VoiceChatViewModel(CommonInit.getInstance().getApp()) }

    //免打扰列表
    var blockList = mutableListOf<String>()

    //标签管理列表
    val tagManagerViewModel: TagManagerViewModel = TagManagerViewModel()
}