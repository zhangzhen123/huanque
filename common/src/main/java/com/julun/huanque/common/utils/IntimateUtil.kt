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
        intimatePrivilegeList.add(
            IntimatePrivilege(
                "成为密友",
                "config/icon/color_CWMY.png",
                "config/icon/gay_CWMY.png",
                "config/icon/color_privilege_MY.png",
                "CWMY",
                1,
                "成为密友，Ta才会想起你呀。"
            )
        )
        intimatePrivilegeList.add(
            IntimatePrivilege(
                "发送图片",
                "config/icon/color_FSTP.png",
                "config/icon/gay_FSTP.png",
                "config/icon/color_privilege_MY.png",
                "FSTP",
                2,
                "我有一张自拍，难道你不想看吗？"
            )
        )
        intimatePrivilegeList.add(
            IntimatePrivilege(
                "语音通话",
                "config/icon/color_YYTH.png",
                "config/icon/gay_YYTH.png",
                "config/icon/color_privilege_MY.png",
                "YYTH",
                3,
                "明明可以靠声音，为什么还要打字？"
            )
        )
        intimatePrivilegeList.add(
            IntimatePrivilege(
                "传送门",
                "config/icon/color_CSM.png",
                "config/icon/gay_CSM.png",
                "config/icon/color_privilege_MY.png",
                "CSM",
                3,
                "喂喂喂，我在这个直播间快点来找我。"
            )
        )
        intimatePrivilegeList.add(
            IntimatePrivilege(
                "聊天气泡",
                "config/icon/color_LTQP.png",
                "config/icon/gay_LTQP.png",
                "config/icon/color_privilege_MY.png",
                "LTQP",
                4,
                "我说话的时候必须带点个性的。"
            )
        )
        intimatePrivilegeList.add(
            IntimatePrivilege(
                "专属表情",
                "config/icon/color_ZSBQ.png",
                "config/icon/gay_ZSBQ.png",
                "config/icon/color_privilege_MY.png",
                "ZSBQ",
                4,
                "这么有趣的表情，怎么能不发？"
            )
        )
        intimatePrivilegeList.add(
            IntimatePrivilege(
                "聊天背景",
                "config/icon/color_LTBJ.png",
                "config/icon/gay_LTBJ.png",
                "config/icon/color_privilege_MY.png",
                "LTBJ",
                5,
                "你值得拥有一个独一无二的背景。"
            )
        )
        intimatePrivilegeList.add(
            IntimatePrivilege(
                "免费聊天",
                "config/icon/color_MFLT.png",
                "config/icon/gay_MFLT.png",
                "config/icon/color_privilege_MY.png",
                "MFLT",
                6,
                "大家都这么熟了，聊天就别收钱啦!"
            )
        )
        intimatePrivilegeList.add(
            IntimatePrivilege(
                "免费语音",
                "config/icon/color_MFYY.png",
                "config/icon/gay_MFYY.png",
                "config/icon/color_privilege_MY.png",
                "MFYY",
                7,
                "语音免费打，真的超划算呢！"
            )
        )
    }
}