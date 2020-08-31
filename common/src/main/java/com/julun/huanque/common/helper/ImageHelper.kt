package com.julun.huanque.common.helper

import android.text.TextUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.R
import com.julun.huanque.common.bean.StyleParam
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.MeetStatus
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.widgets.emotion.Emotions

object ImageHelper {

    //设置用户等级图片
    fun getUserLevelImg(lev: Int? = -1, levelString: String? = ""): Int {
        var level = lev
        if (level == -1) {
            try {
                level = Integer.parseInt(levelString ?: "")
            } catch (e: Exception) {
                reportCrash("要取用的贵族等级 <$lev 或者 $levelString> 不正确,必须是 1~40的数字", e)
            }
        }
        var resId: Int = R.mipmap.user_lv0
        when (level) {
            1 -> resId = R.mipmap.user_lv1
            2 -> resId = R.mipmap.user_lv2
            3 -> resId = R.mipmap.user_lv3
            4 -> resId = R.mipmap.user_lv4
            5 -> resId = R.mipmap.user_lv5
            6 -> resId = R.mipmap.user_lv6
            7 -> resId = R.mipmap.user_lv7
            8 -> resId = R.mipmap.user_lv8
            9 -> resId = R.mipmap.user_lv9
            10 -> resId = R.mipmap.user_lv10
            11 -> resId = R.mipmap.user_lv11
            12 -> resId = R.mipmap.user_lv12
            13 -> resId = R.mipmap.user_lv13
            14 -> resId = R.mipmap.user_lv14
            15 -> resId = R.mipmap.user_lv15
            16 -> resId = R.mipmap.user_lv16
            17 -> resId = R.mipmap.user_lv17
            18 -> resId = R.mipmap.user_lv18
            19 -> resId = R.mipmap.user_lv19
            20 -> resId = R.mipmap.user_lv20
            21 -> resId = R.mipmap.user_lv21
            22 -> resId = R.mipmap.user_lv22
            23 -> resId = R.mipmap.user_lv23
            24 -> resId = R.mipmap.user_lv24
            25 -> resId = R.mipmap.user_lv25
            26 -> resId = R.mipmap.user_lv26
            27 -> resId = R.mipmap.user_lv27
            28 -> resId = R.mipmap.user_lv28
            29 -> resId = R.mipmap.user_lv29
            30 -> resId = R.mipmap.user_lv30
            31 -> resId = R.mipmap.user_lv31
            32 -> resId = R.mipmap.user_lv32
            33 -> resId = R.mipmap.user_lv33
            34 -> resId = R.mipmap.user_lv34
            35 -> resId = R.mipmap.user_lv35
            36 -> resId = R.mipmap.user_lv36
            37 -> resId = R.mipmap.user_lv37
            38 -> resId = R.mipmap.user_lv38
            39 -> resId = R.mipmap.user_lv39
            40 -> resId = R.mipmap.user_lv40
            41 -> resId = R.mipmap.user_lv41
            42 -> resId = R.mipmap.user_lv42
            43 -> resId = R.mipmap.user_lv43
            44 -> resId = R.mipmap.user_lv44
            45 -> resId = R.mipmap.user_lv45
            46 -> resId = R.mipmap.user_lv46
            47 -> resId = R.mipmap.user_lv47
            48 -> resId = R.mipmap.user_lv48
            49 -> resId = R.mipmap.user_lv49
            50 -> resId = R.mipmap.user_lv50
            51 -> resId = R.mipmap.user_lv51
            52 -> resId = R.mipmap.user_lv52
            53 -> resId = R.mipmap.user_lv53
            54 -> resId = R.mipmap.user_lv54
            55 -> resId = R.mipmap.user_lv55
            56 -> resId = R.mipmap.user_lv56
            57 -> resId = R.mipmap.user_lv57
            58 -> resId = R.mipmap.user_lv58
            59 -> resId = R.mipmap.user_lv59
            60 -> resId = R.mipmap.user_lv60
            61 -> resId = R.mipmap.user_lv61
            62 -> resId = R.mipmap.user_lv62
            63 -> resId = R.mipmap.user_lv63
            64 -> resId = R.mipmap.user_lv64
            65 -> resId = R.mipmap.user_lv65
            66 -> resId = R.mipmap.user_lv66
            67 -> resId = R.mipmap.user_lv67
            68 -> resId = R.mipmap.user_lv68
            69 -> resId = R.mipmap.user_lv69
            70 -> resId = R.mipmap.user_lv70
            71 -> resId = R.mipmap.user_lv71
            72 -> resId = R.mipmap.user_lv72
            73 -> resId = R.mipmap.user_lv73
            74 -> resId = R.mipmap.user_lv74
            75 -> resId = R.mipmap.user_lv75
            76 -> resId = R.mipmap.user_lv76
            77 -> resId = R.mipmap.user_lv77
            78 -> resId = R.mipmap.user_lv78
            79 -> resId = R.mipmap.user_lv79
            80 -> resId = R.mipmap.user_lv80
            else -> {
                resId = R.mipmap.user_lv0
            }
        }
        return resId
    }

    //设置用户贵族等级图片
    fun getRoyalLevelImgRound(royalLevel: Int): Int {
//        var level = lev
//        if (level == -1) {
//            try {
//                level = Integer.parseInt(levelString)
//            } catch (e: Exception) {
//                reportCrash("要取用的贵族等级 <$lev 或者 $levelString> 不正确,必须是 1~8的数字", e)
//            }
//        }
        var resId: Int = 0
        return resId
    }

    //获取头像框
    fun getRoyalHeadFrameImg(royalLevel: Int): Int {
        var resId: Int = 0
        when (royalLevel) {
            1 -> resId = R.mipmap.bg_royal_header_1
            2 -> resId = R.mipmap.bg_royal_header_2
            3 -> resId = R.mipmap.bg_royal_header_3
            4 -> resId = R.mipmap.bg_royal_header_4
            5 -> resId = R.mipmap.bg_royal_header_5
            6 -> resId = R.mipmap.bg_royal_header_6
            7 -> resId = R.mipmap.bg_royal_header_7
            else -> {
                0
            }
        }
        return resId
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


    /**
     * 获取主播等级长图标
     */
    fun getAnchorLevelResId(level: Int = -1): Int {
        var resId = 0
        when (level) {
            1 -> resId = R.mipmap.anchor_lv1
            2 -> resId = R.mipmap.anchor_lv2
            3 -> resId = R.mipmap.anchor_lv3
            4 -> resId = R.mipmap.anchor_lv4
            5 -> resId = R.mipmap.anchor_lv5
            else -> {
                resId = R.mipmap.anchor_lv1
            }
        }
        return resId
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

    }

    /**
     * 获取主播等级小图标
     */
    fun getAnchorLevelShortResId(level: Int = -1): Int {
        var resId = 0
        when (level) {
            1 -> resId = R.mipmap.anchor_short_lv1
            2 -> resId = R.mipmap.anchor_short_lv2
            3 -> resId = R.mipmap.anchor_short_lv3
            4 -> resId = R.mipmap.anchor_short_lv4
            5 -> resId = R.mipmap.anchor_short_lv5
            else -> {
                resId = R.mipmap.anchor_short_lv1
            }
        }
        return resId
    }

    fun getRankResId(index: Int = -1): Int {
        var resId = 0
        when (index) {
            0 -> resId = R.mipmap.icon_rank_1
            1 -> resId = R.mipmap.icon_rank_2
            2 -> resId = R.mipmap.icon_rank_3
            else -> {
                if (index < 0) {
                    resId = R.mipmap.icon_rank_1
                } else if (index > 2) {
                    resId = R.mipmap.icon_rank_3
                }
            }
        }
        return resId
    }

    /**
     * 获取本地的诸如 emoj,用户等级,主播等级和贵族等级的图片资源id
     */
    fun getLocalImageResId(paramValue: String?, styleParam: StyleParam): Int {
        return if (TplHelper.PREFIX_USER_LEVEL == styleParam.preffix) {
            if (paramValue?.toInt() == -1) -1 else
                getUserLevelImg(levelString = paramValue)
        } else if (TplHelper.PREFIX_ROYAL_LEVEL == styleParam.preffix) {
            if (paramValue?.toInt() == -1) -1 else
                getRoyalLevelImgLong(levelString = paramValue)
        } else if (TplHelper.PREFIX_ANCHOR_LEVEL == styleParam.preffix) {
            getAnchorLevelShortResId(paramValue!!.toInt())
        } else if (TplHelper.PREFIX_EMOJI == styleParam.preffix) {
//            EmojiUtil.EmojiResArray["$paramValue".toInt()]
            try {
                val paramName = styleParam.el
                val name = "[${paramName.substring(paramName.indexOf("$") + 2, paramName.length - 1)}]"
                Emotions.getDrawableResByName(name)
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }

        } else {
            ULog.i("图片类型不明,既不是用户等级,又不是贵族等级,还不是emoji表情")
            -1
//            throw RuntimeException("图片类型不明,既不是用户等级,又不是贵族等级,还不是emoji表情")
        }
    }

    /**
     * 图文混排工具
     * @param arrayList 传入对应混排list
     */
    fun renderTextAndImage(arrayList: ArrayList<TIBean>): BaseTextBean? {
        if (arrayList.isEmpty()) {
            return null
        }
        try {
            var textIndex = 0
            var text = ""
            val textBean = BaseTextBean()
            arrayList.forEachIndexed { index, bean ->
                if (index == 0) {
                    when (bean.type) {
                        TIBean.TEXT -> {
                            if (TextUtils.isEmpty(bean.text)) {
                                return@forEachIndexed
                            }
                            val testParam = copyImageParams(TextParam(), bean) as TextParam
                            testParam.indexStart = textIndex
                            text = bean.text
                            textIndex += bean.text.length - 1
                            textBean.textParams.add(testParam)
                        }
                        else -> {
                            val imgParam = copyImageParams(ImageParam(), bean) as ImageParam
                            imgParam.index = textIndex
                            text = "#"
                            textIndex += 0
                            textBean.imgParams.add(imgParam)
                        }
                    }
                } else {
                    when (bean.type) {
                        TIBean.TEXT -> {
                            if (TextUtils.isEmpty(bean.text)) {
                                return@forEachIndexed
                            }
                            val testParam = copyImageParams(TextParam(), bean) as TextParam
                            testParam.indexStart = textIndex + 1
                            text += bean.text
                            textBean.textParams.add(testParam)
                            textIndex += bean.text.length
                        }
                        else -> {
                            val imgParam = copyImageParams(ImageParam(), bean) as ImageParam
                            textIndex += 2
                            text += " #"
                            imgParam.index = textIndex
                            textBean.imgParams.add(imgParam)
                        }
                    }
                }
            }
            textBean.realText = text
            return textBean
        } catch (e: Exception) {
            reportCrash("renderTextAndImage解析异常", e)
            e.printStackTrace()
            return null
        }
    }


    private fun copyImageParams(params: BaseParams, bean: TIBean): BaseParams {
        if (params is TextParam) {
            params.textColor = bean.textColor
            params.textColorInt = bean.textColorInt
            params.textSize = bean.textSize
            params.text = bean.text
            params.styleSpan = bean.styleSpan
        } else if (params is ImageParam) {
            params.imgRes = bean.imgRes
            params.url = bean.url
            params.height = bean.height
            params.width = bean.width
            params.isCircle = bean.isCircle
            params.borderRedId = bean.borderRedId
            params.borderWidth = bean.borderWidth
        }
        return params
    }

    /**
     * 设置默认头像
     */
    fun setDefaultHeaderPic(sdv: SimpleDraweeView, sex: String, official: Boolean = false) {
        val hierarchy = sdv.hierarchy
        val defaultHeader = if (official) {
            //官方
            R.mipmap.icon_logo_avatar_yellow
        } else {
            if (sex == Sex.MALE) {
                //男性
                R.mipmap.icon_logo_avatar_blue
            } else {
                //女性
                R.mipmap.icon_logo_avatar_red
            }
        }
        //设置占位图
        hierarchy.setPlaceholderImage(defaultHeader)
    }


    /**
     * 获取亲密度等级图片
     */
    fun getIntimateLevelPic(level: Int): Int {
        return when (level) {
            1 -> R.mipmap.intimate_level_1
            2 -> R.mipmap.intimate_level_2
            3 -> R.mipmap.intimate_level_3
            4 -> R.mipmap.intimate_level_4
            5 -> R.mipmap.intimate_level_5
            6 -> R.mipmap.intimate_level_6
            7 -> R.mipmap.intimate_level_7
            else -> {
                0
            }
        }
    }

    /**
     * 获取欢遇标识
     */
    fun getMeetStatusResource(status: String): Int {
        return when (status) {
            MeetStatus.Wait -> {
                //待欢遇
                R.mipmap.icon_huanyu_disable
            }
            MeetStatus.Meet -> {
                //欢遇中
                R.mipmap.icon_huanyu
            }
            else -> {
                0
            }
        }
    }
}