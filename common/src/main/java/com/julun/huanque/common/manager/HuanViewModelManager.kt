package com.julun.huanque.common.manager

import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.viewmodel.HuanQueViewModel

/**
 *@创建者   dong
 *@创建时间 2020/9/21 20:45
 *@描述 保存HuanqueViewModel的manager
 */
object HuanViewModelManager {
    var huanQueViewModel = HuanQueViewModel(CommonInit.getInstance().getApp())

    //免打扰列表
    var blockList = mutableListOf<String>()
}