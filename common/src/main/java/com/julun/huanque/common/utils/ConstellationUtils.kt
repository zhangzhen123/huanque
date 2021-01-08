package com.julun.huanque.common.utils

import com.julun.huanque.common.bean.beans.ConstellationBean
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2021/1/5 15:26
 *@描述 星座工具类
 */
object ConstellationUtils {

    /**
     * 根据生日获取星座
     *
     * @param birthday
     * @return
     */
    fun getConstellation(birthday: Date): ConstellationBean {
        val calendar = Calendar.getInstance()
        calendar.time = birthday
        val value = ConstellationBean()
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        if (month == 1 && day >= 20 || month == 2 && day <= 18) {
            value.name = "水瓶座"
            value.type = "Aguarius"
        }
        if (month == 2 && day >= 19 || month == 3 && day <= 20) {
            value.name = "双鱼座"
            value.type = "Pisces"
        }
        if (month == 3 && day >= 21 || month == 4 && day <= 19) {
            value.name = "白羊座"
            value.type = "Aries"
        }
        if (month == 4 && day >= 20 || month == 5 && day <= 20) {
            value.name = "金牛座"
            value.type = "Taurus"
        }
        if (month == 5 && day >= 21 || month == 6 && day <= 21) {
            value.name = "双子座"
            value.type = "Genimi"
        }
        if (month == 6 && day >= 22 || month == 7 && day <= 22) {
            value.name = "巨蟹座"
            value.type = "Cancer"
        }
        if (month == 7 && day >= 23 || month == 8 && day <= 22) {
            value.name = "狮子座"
            value.type = "Leonis"
        }
        if (month == 8 && day >= 23 || month == 9 && day <= 22) {
            value.name = "处女座"
            value.type = "Virgo"
        }
        if (month == 9 && day >= 23 || month == 10 && day <= 22) {
            value.name = "天秤座"
            value.type = "Libra"
        }
        if (month == 10 && day >= 23 || month == 11 && day <= 21) {
            value.name = "天蝎座"
            value.type = "Scorpius"
        }
        if (month == 11 && day >= 22 || month == 12 && day <= 21) {
            value.name = "射手座"
            value.type = "Sagittarius"
        }
        if (month == 12 && day >= 22 || month == 1 && day <= 19) {
            value.name = "摩羯座"
            value.type = "Capricornus"
        }
        return value;
    }
}