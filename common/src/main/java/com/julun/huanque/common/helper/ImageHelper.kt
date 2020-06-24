package com.julun.huanque.common.helper

import com.julun.huanque.common.bean.MessageUtil
import com.julun.huanque.common.bean.StyleParam
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.widgets.live.chatInput.EmojiUtil

object ImageHelper {

    //设置用户等级图片
    fun getUserLevelImg(lev: Int? = -1, levelString: String? = ""): Int {
        var level = lev
//        if (level == -1) {
//            try {
//                level = Integer.parseInt(levelString)
//            } catch (e: Exception) {
//                reportCrash("要取用的贵族等级 <$lev 或者 $levelString> 不正确,必须是 1~40的数字", e)
//            }
//        }
//        var resId: Int = R.mipmap.user_lv1
//        when (level) {
//            null, -1, 0, 1 -> resId = R.mipmap.user_lv1
//            2 -> resId = R.mipmap.user_lv2
//            3 -> resId = R.mipmap.user_lv3
//            4 -> resId = R.mipmap.user_lv4
//            5 -> resId = R.mipmap.user_lv5
//            6 -> resId = R.mipmap.user_lv6
//            7 -> resId = R.mipmap.user_lv7
//            8 -> resId = R.mipmap.user_lv8
//            9 -> resId = R.mipmap.user_lv9
//            10 -> resId = R.mipmap.user_lv10
//            11 -> resId = R.mipmap.user_lv11
//            12 -> resId = R.mipmap.user_lv12
//            13 -> resId = R.mipmap.user_lv13
//            14 -> resId = R.mipmap.user_lv14
//            15 -> resId = R.mipmap.user_lv15
//            16 -> resId = R.mipmap.user_lv16
//            17 -> resId = R.mipmap.user_lv17
//            18 -> resId = R.mipmap.user_lv18
//            19 -> resId = R.mipmap.user_lv19
//            20 -> resId = R.mipmap.user_lv20
//            21 -> resId = R.mipmap.user_lv21
//            22 -> resId = R.mipmap.user_lv22
//            23 -> resId = R.mipmap.user_lv23
//            24 -> resId = R.mipmap.user_lv24
//            25 -> resId = R.mipmap.user_lv25
//            26 -> resId = R.mipmap.user_lv26
//            27 -> resId = R.mipmap.user_lv27
//            28 -> resId = R.mipmap.user_lv28
//            29 -> resId = R.mipmap.user_lv29
//            30 -> resId = R.mipmap.user_lv30
//            31 -> resId = R.mipmap.user_lv31
//            32 -> resId = R.mipmap.user_lv32
//            33 -> resId = R.mipmap.user_lv33
//            34 -> resId = R.mipmap.user_lv34
//            35 -> resId = R.mipmap.user_lv35
//            36 -> resId = R.mipmap.user_lv36
//            37 -> resId = R.mipmap.user_lv37
//            38 -> resId = R.mipmap.user_lv38
//            39 -> resId = R.mipmap.user_lv39
//            40 -> resId = R.mipmap.user_lv40
//            41 -> resId = R.mipmap.user_lv41
//            42 -> resId = R.mipmap.user_lv42
//            43 -> resId = R.mipmap.user_lv43
//            44 -> resId = R.mipmap.user_lv44
//            45 -> resId = R.mipmap.user_lv45
//            46 -> resId = R.mipmap.user_lv46
//            47 -> resId = R.mipmap.user_lv47
//            48 -> resId = R.mipmap.user_lv48
//            49 -> resId = R.mipmap.user_lv49
//            50 -> resId = R.mipmap.user_lv50
//            else -> {
//                if (level < -1) {
//                    resId = R.mipmap.user_lv1
//                } else if (level > 50) {
//                    resId = R.mipmap.user_lv50
//                }
//            }
//        }
        return 0
    }

    //设置用户贵族等级图片
    fun getRoyalLevelImgRound(lev: Int? = -1, levelString: String? = ""): Int {
//        var level = lev
//        if (level == -1) {
//            try {
//                level = Integer.parseInt(levelString)
//            } catch (e: Exception) {
//                reportCrash("要取用的贵族等级 <$lev 或者 $levelString> 不正确,必须是 1~8的数字", e)
//            }
//        }
//        var resId: Int = 0
//        when (level) {
//            null, -1, 0, 1 -> resId = R.mipmap.royal_lv1
//            2 -> resId = R.mipmap.royal_lv2
//            3 -> resId = R.mipmap.royal_lv3
//            4 -> resId = R.mipmap.royal_lv4
//            5 -> resId = R.mipmap.royal_lv5
//            6 -> resId = R.mipmap.royal_lv6
//            7 -> resId = R.mipmap.royal_lv7
//            8 -> resId = R.mipmap.royal_lv8
//            9 -> resId = R.mipmap.royal_lv9
//            else -> {
//                if (level < -1) {
//                    resId = R.mipmap.royal_lv1
//                } else if (level > 9) {
//                    resId = R.mipmap.royal_lv9
//                }
//            }
//        }
        return 0
    }

    /**
     *  获取贵族图标长版(此方法只获取本地图标 由于上神以上规则修改 资源改成远程)
     *  @param lev 贵族等级Int类型
     *  @param levelString 贵族等级String类型
     */

    fun getRoyalLevelImgLong(lev: Int? = -1, levelString: String? = ""): Int {
//        var level = lev
//        if (level == -1) {
//            try {
//                level = Integer.parseInt(levelString)
//            } catch (e: Exception) {
//                reportCrash("要取用的贵族等级 <$lev 或者 $levelString> 不正确,必须是 1~8的数字", e)
//            }
//        }
//        var resId: Int = 0
//        when (level) {
//            null, -1, 0, 1 -> resId = R.mipmap.royal_long_lv1
//            2 -> resId = R.mipmap.royal_long_lv2
//            3 -> resId = R.mipmap.royal_long_lv3
//            4 -> resId = R.mipmap.royal_long_lv4
//            5 -> resId = R.mipmap.royal_long_lv5
//            6 -> resId = R.mipmap.royal_long_lv6
//            7 -> resId = R.mipmap.royal_long_lv7
//            8 -> resId = R.mipmap.royal_long_lv8
//            else -> {
//                if (level < -1) {
//                    resId = R.mipmap.royal_long_lv1
//                } else if (level > 8) {
//                    resId = R.mipmap.royal_long_lv8
//                }
//            }
//        }
        return 0
    }


    fun getAnchorLevelResId(level: Int = -1): Int {
//        var resId = 0
//        when (level) {
//            0, 1 -> resId = R.mipmap.anchor_lv1
//            2 -> resId = R.mipmap.anchor_lv2
//            3 -> resId = R.mipmap.anchor_lv3
//            4 -> resId = R.mipmap.anchor_lv4
//            5 -> resId = R.mipmap.anchor_lv5
//            6 -> resId = R.mipmap.anchor_lv6
//            7 -> resId = R.mipmap.anchor_lv7
//            8 -> resId = R.mipmap.anchor_lv8
//            9 -> resId = R.mipmap.anchor_lv9
//            10 -> resId = R.mipmap.anchor_lv10
//            11 -> resId = R.mipmap.anchor_lv11
//            12 -> resId = R.mipmap.anchor_lv12
//            13 -> resId = R.mipmap.anchor_lv13
//            14 -> resId = R.mipmap.anchor_lv14
//            15 -> resId = R.mipmap.anchor_lv15
//            16 -> resId = R.mipmap.anchor_lv16
//            17 -> resId = R.mipmap.anchor_lv17
//            18 -> resId = R.mipmap.anchor_lv18
//            19 -> resId = R.mipmap.anchor_lv19
//            20 -> resId = R.mipmap.anchor_lv20
//            21 -> resId = R.mipmap.anchor_lv21
//            22 -> resId = R.mipmap.anchor_lv22
//            23 -> resId = R.mipmap.anchor_lv23
//            24 -> resId = R.mipmap.anchor_lv24
//            25 -> resId = R.mipmap.anchor_lv25
//            26 -> resId = R.mipmap.anchor_lv26
//            27 -> resId = R.mipmap.anchor_lv27
//            28 -> resId = R.mipmap.anchor_lv28
//            29 -> resId = R.mipmap.anchor_lv29
//            30 -> resId = R.mipmap.anchor_lv30
//            31 -> resId = R.mipmap.anchor_lv31
//            32 -> resId = R.mipmap.anchor_lv32
//            33 -> resId = R.mipmap.anchor_lv33
//            34 -> resId = R.mipmap.anchor_lv34
//            35 -> resId = R.mipmap.anchor_lv35
//            36 -> resId = R.mipmap.anchor_lv36
//            37 -> resId = R.mipmap.anchor_lv37
//            38 -> resId = R.mipmap.anchor_lv38
//            39 -> resId = R.mipmap.anchor_lv39
//            40 -> resId = R.mipmap.anchor_lv40
//            41 -> resId = R.mipmap.anchor_lv41
//            42 -> resId = R.mipmap.anchor_lv42
//            43 -> resId = R.mipmap.anchor_lv43
//            44 -> resId = R.mipmap.anchor_lv44
//            45 -> resId = R.mipmap.anchor_lv45
//            46 -> resId = R.mipmap.anchor_lv46
//            47 -> resId = R.mipmap.anchor_lv47
//            48 -> resId = R.mipmap.anchor_lv48
//            49 -> resId = R.mipmap.anchor_lv49
//            50 -> resId = R.mipmap.anchor_lv50
//            51 -> resId = R.mipmap.anchor_lv51
//            52 -> resId = R.mipmap.anchor_lv52
//            53 -> resId = R.mipmap.anchor_lv53
//            54 -> resId = R.mipmap.anchor_lv54
//            55 -> resId = R.mipmap.anchor_lv55
//            56 -> resId = R.mipmap.anchor_lv56
//            57 -> resId = R.mipmap.anchor_lv57
//            58 -> resId = R.mipmap.anchor_lv58
//            59 -> resId = R.mipmap.anchor_lv59
//            60 -> resId = R.mipmap.anchor_lv60
//            61 -> resId = R.mipmap.anchor_lv61
//            62 -> resId = R.mipmap.anchor_lv62
//            63 -> resId = R.mipmap.anchor_lv63
//            64 -> resId = R.mipmap.anchor_lv64
//            65 -> resId = R.mipmap.anchor_lv65
//            66 -> resId = R.mipmap.anchor_lv66
//            67 -> resId = R.mipmap.anchor_lv67
//            68 -> resId = R.mipmap.anchor_lv68
//            69 -> resId = R.mipmap.anchor_lv69
//            70 -> resId = R.mipmap.anchor_lv70
//            71 -> resId = R.mipmap.anchor_lv71
//            72 -> resId = R.mipmap.anchor_lv72
//            73 -> resId = R.mipmap.anchor_lv73
//            74 -> resId = R.mipmap.anchor_lv74
//            75 -> resId = R.mipmap.anchor_lv75
//            76 -> resId = R.mipmap.anchor_lv76
//            77 -> resId = R.mipmap.anchor_lv77
//            78 -> resId = R.mipmap.anchor_lv78
//            79 -> resId = R.mipmap.anchor_lv79
//            80 -> resId = R.mipmap.anchor_lv80
//            81 -> resId = R.mipmap.anchor_lv81
//            82 -> resId = R.mipmap.anchor_lv82
//            83 -> resId = R.mipmap.anchor_lv83
//            84 -> resId = R.mipmap.anchor_lv84
//            85 -> resId = R.mipmap.anchor_lv85
//            86 -> resId = R.mipmap.anchor_lv86
//            87 -> resId = R.mipmap.anchor_lv87
//            88 -> resId = R.mipmap.anchor_lv88
//            89 -> resId = R.mipmap.anchor_lv89
//            90 -> resId = R.mipmap.anchor_lv90
//            else -> {
//                if (level < 0) {
//                    resId = R.mipmap.anchor_lv1
//                } else if (level > 90) {
//                    resId = R.mipmap.anchor_lv45
//                }
//            }
//        }
        return 0
    }

    fun getRankResId(index: Int = -1): Int {
        var resId = 0
//        when (index) {
//            0 -> resId = R.mipmap.rank_1
//            1 -> resId = R.mipmap.rank_2
//            2 -> resId = R.mipmap.rank_3
//            else -> {
//                if (index < 0) {
//                    resId = R.mipmap.rank_1
//                } else if (index > 2) {
//                    resId = R.mipmap.rank_3
//                }
//            }
//        }
        return resId
    }

    /**
     * 获取本地的诸如 emoj,用户等级,主播等级和贵族等级的图片资源id
     */
    fun getLocalImageResId(paramValue: String?, styleParam: StyleParam): Int {
        return if (MessageUtil.PREFFIX_USER_LEVEL == styleParam.preffix) {
            if (paramValue?.toInt() == -1) -1 else
                getUserLevelImg(levelString = paramValue)
        } else if (MessageUtil.PREFFIX_ROYAL_LEVEL == styleParam.preffix) {
            if (paramValue?.toInt() == -1) -1 else
                getRoyalLevelImgLong(levelString = paramValue)
        } else if (MessageUtil.PREFFIX_ANCHOR_LEVEL == styleParam.preffix) {
            getAnchorLevelResId(paramValue!!.toInt())
        } else if (MessageUtil.PREFFIX_EMOJI == styleParam.preffix) {
            EmojiUtil.EmojiResArray["$paramValue".toInt()]
        } else {
            ULog.i("图片类型不明,既不是用户等级,又不是贵族等级,还不是emoji表情")
            -1
//            throw RuntimeException("图片类型不明,既不是用户等级,又不是贵族等级,还不是emoji表情")
        }
    }
}