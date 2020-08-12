package com.julun.huanque.common.utils

import java.text.DecimalFormat

/**
 *@创建者   dong
 *@创建时间 2020/8/12 9:06
 *@描述
 */
object NumberFormatUtils {

    /**
     * 数字格式化为1位小数
     */
    fun formatWithdecimal1(num: Double): String {
        val df = DecimalFormat("#.0")
        return df.format(num)
    }

}