package com.julun.huanque.common.utils

import com.julun.huanque.common.bean.beans.IntimatePrivilege

/**
 *@创建者   dong
 *@创建时间 2020/7/21 20:23
 *@描述 亲密特权
 */
object IntimateUtil {
    val intimatePrivilegeList = mutableListOf<IntimatePrivilege>()

    init {
        intimatePrivilegeList.add(IntimatePrivilege("config/icon/color_CWMY.png", "config/icon/gay_CWMY.png", "CWMY", 1, "成为密友"))
        intimatePrivilegeList.add(IntimatePrivilege("config/icon/color_FSTP.png", "config/icon/gay_FSTP.png", "FSTP", 2, "发送图片"))
        intimatePrivilegeList.add(IntimatePrivilege("config/icon/color_YYTH.png", "config/icon/gay_YYTH.png", "YYTH", 3, "语音通话"))
        intimatePrivilegeList.add(IntimatePrivilege("config/icon/color_CSM.png", "config/icon/gay_CSM.png", "CSM", 3, "传送门"))
        intimatePrivilegeList.add(IntimatePrivilege("config/icon/color_LTQP.png", "config/icon/gay_LTQP.png", "LTQP", 4, "聊天气泡"))
        intimatePrivilegeList.add(IntimatePrivilege("config/icon/color_ZSBQ.png", "config/icon/gay_ZSBQ.png", "ZSBQ", 4, "专属表情"))
        intimatePrivilegeList.add(IntimatePrivilege("config/icon/color_LTBJ.png", "config/icon/gay_LTBJ.png", "LTBJ", 5, "聊天背景"))
        intimatePrivilegeList.add(IntimatePrivilege("config/icon/color_MFLT.png", "config/icon/gay_MFLT.png", "MFLT", 6, "免费聊天"))
        intimatePrivilegeList.add(IntimatePrivilege("config/icon/color_MFYY.png", "config/icon/gay_MFYY.png", "MFYY", 7, "免费语音"))
    }
}