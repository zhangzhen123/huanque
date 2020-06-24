@file:JvmName("DateHelper")

package com.julun.huanque.common.utils

import java.text.DateFormat
import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by nirack on 16-10-22.
 */
object DateHelper {

    val FORMAT_YMD_STR = "yyyyMMdd"
    val FORMAT_YMD = "yyyy-MM-dd"

    val FORMAT_YMD_CHINESE_CHARACTER = "yyyy年MM月dd日"

    val FORMAT_TIME = "yyyy-MM-dd HH:mm:ss"
    val FORMAT_TIME_VOD = "yyyyMMddHHmmss"

    val df: DateFormat = SimpleDateFormat(FORMAT_TIME)
    val sdf: DateFormat = SimpleDateFormat(FORMAT_YMD)
    val ymvod: DateFormat = SimpleDateFormat(FORMAT_TIME_VOD)
    val ymd: DateFormat = SimpleDateFormat(FORMAT_YMD_STR)

    val SECOND = "second"
    val MINUTE = "minute"
    val HOUR = "hour"
    val DAY = "day"
    /**
     * 将指定日期按指定的format格式化。
     * @return
     */
    fun format(date: Date?, format: String): String {
        if (null == date) {
            return ""
        } else {
            return SimpleDateFormat(format).format(date)
        }
    }


    /**
     * 获取指定日期,输出格式化为yyyy-MM-dd HH:mm:ss
     * @return
     */
    fun format(date: Date?): String {
        if (date == null)
            return ""
        return df.format(date)
    }

    /**
     * 获取当前时刻日期，格式为yyyy-MM-dd HH:mm:ss
     * @return
     */
    fun formatNowTime(): String {
        val date = Date()
        return df.format(date)
    }

    /**
     * 获取当前时刻日期，格式为yyyy-MM-dd
     * @return
     */
    fun formatNow(): String {
        val date = Date()
        return sdf.format(date)
    }

    /**
     * 获取当前时刻日期
     * @return
     */
    fun now(): Date {
        return Date()
    }

    /**
     * 获取当前日期0点的日期时间
     * @return
     */
    fun today(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun getCurrYear(date: Date): Int {
        val c = Calendar.getInstance()
        c.time = date
        return c.get(Calendar.YEAR)
    }


    /**
     * 获取当前日期年份

     * @return
     */
    fun getCurrYear(): Int {
        val c = Calendar.getInstance()
        c.time = Date()
        return c.get(Calendar.YEAR)
    }

    /**
     * 获取当前日期月份
     * @return
     */
    fun getCurrMonth(): Int {
        val c = Calendar.getInstance()
        c.time = Date()
        return c.get(Calendar.MONTH)
    }

    /**
     * 获取当前日期天
     * @return
     */
    fun getCurrDAY(): Int {
        val c = Calendar.getInstance()
        c.time = Date()
        return c.get(Calendar.DATE)
    }

    /**
     * 获取当前小时
     * @return
     */
    fun getCurrHour(): Int {
        val c = Calendar.getInstance()
        c.time = Date()
        return c.get(Calendar.HOUR_OF_DAY)
    }

    fun getCurrMinute(): Int {
        val c = Calendar.getInstance()
        c.time = Date()
        return c.get(Calendar.MINUTE)
    }

    /**
     * 将时间字符串格式化yyyy-MM-dd HH:mm:ss
     * @return
     */
    fun format(datestr: String): String {
        var date: Date
        try {
            date = df.parse(datestr)
        } catch (e: ParseException) {
            date = Date()
        }

        return df.format(date)
    }

    /**
     * 将时间字符串格式yyyy-MM-dd HH:mm:ss 转化成 yyyy-MM-dd
     * @return
     */
    fun transform(datestr: String): String {
        var date: Date
        return try {
            date = df.parse(datestr)
            sdf.format(date)
        } catch (e: ParseException) {
            datestr
        }
    }

    /**
     * 将短时间格式时间转换为字符串 yyyy-MM-dd
     * @param dateDate
     * *
     * @return
     */
    fun dateToStr(dateDate: Date?): String {
        if (dateDate == null)
            return ""
        return SimpleDateFormat(FORMAT_YMD).format(dateDate)
    }

    fun dateToStrYmvod(dateDate: Date?): String {
        if (dateDate == null)
            return ""
        return ymvod.format(dateDate)
    }

    fun dateToStrYMD(dateDate: Date?): String {
        if (dateDate == null)
            return ""
        val dateString = ymd.format(dateDate)
        return dateString
    }

    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     * @param strDate
     * *
     * @return
     */
    fun strToDate(strDate: String): Date {
        val pos = ParsePosition(0)
        return SimpleDateFormat(FORMAT_YMD).parse(strDate, pos)
    }

    fun strYMDToDate(strDate: String): Date {
        val pos = ParsePosition(0)
        val strtodate = ymd.parse(strDate, pos)
        return strtodate
    }

    /**
     * 将短时间格式字符串转换为时间 yyyyMMddHHmmss
     * @param strDate
     * *
     * @return
     */
    fun vodStrToDate(strDate: String): Date {
        val pos = ParsePosition(0)
        val strtodate = ymvod.parse(strDate, pos)
        return strtodate
    }

    /**
     * 当前日期按分隔符号"spt"分隔返回显示，不含时分秒
     * @return
     */
    fun getDate(spt: String): String {
        val sdf = SimpleDateFormat("yyyy${spt}MM${spt}dd")
        val date = Date()
        return sdf.format(date)
    }

    /**
     * 获取给定时间加Ｎ天后的日期,
     * 如果N为负数那么就是给定时间减天
     * @return date
     */
    fun addDate(date: Date, day: Int): Date {
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.DATE, day)
        return c.time
    }

    /**
     * 获取两个日期之间的间隔天数(1)
     * @return
     */
    fun getDiffDays(beginDate: Date, endDate: Date): Int {
        val lBeginTime = beginDate.time
        val lEndTime = endDate.time
        val iDay = ((lEndTime - lBeginTime) / 86400000).toInt()
        return iDay
    }

    /**
     * 获取两个日期之间的间隔天数(2)
     * @return
     */
    fun getDifferDays(beginDate: String, endDate: String): Int {
        var date1: Date? = null
        var date2: Date? = null
        var days = 0
        try {
            date1 = sdf.parse(beginDate)
            date2 = sdf.parse(endDate)
            days = ((date2!!.time - date1!!.time) / 86400000).toInt()
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return days
    }

    fun getDiffMonth(begin: Date, end: Date): Int {
        val bc = Calendar.getInstance()
        bc.timeInMillis = begin.time
        //bc.setTime(begin.getTime());
        val be = Calendar.getInstance()
        be.time = end
        val beginYear = bc.get(Calendar.YEAR)
        val beginMonth = bc.get(Calendar.MONTH)

        val endYear = be.get(Calendar.YEAR)
        val endMonth = be.get(Calendar.MONTH)

        val difMonth = (endYear - beginYear) * 12 + (endMonth - beginMonth)

        return difMonth
    }

    /**
     * 获取本月第一天（根据当前时间）
     * @return
     */
    fun getFirstDateInCurrentWeek(): String {
        val mondayPlus: Int
        val cd = Calendar.getInstance()
        // 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
        val dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1 // 因为按中国礼拜一作为第一天所以这里减1
        if (dayOfWeek == 0) {
            mondayPlus = -6
        } else {
            mondayPlus = 1 - dayOfWeek
        }
        val currentDate = GregorianCalendar()
        currentDate.add(GregorianCalendar.DATE, mondayPlus)
        val monday = currentDate.time

        val preMonday = format(monday, FORMAT_YMD)
        return preMonday
    }

    fun getFirstDateInNextWeek(): String {
        var date = getFirstDateInCurrentWeek()
        date = dateToStr(addDate(strToDate(date), 7))
        return date

    }

    /**
     * 获取本月第一天（根据当前时间）
     * @return
     */
    fun getFirstDateInCurrentMonth(): String {
        return getFirstDateInCurrentMonth(Date())
    }

    fun getFirstDateInCurrentMonth(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR).toString()
        val month = (calendar.get(Calendar.MONTH) + 1).toString()
        val day = "01"
        return year + "-" + (if (month.length == 1) "0" + month else month) + "-" + day
    }

    /**
     * 获取本月最后一天（根据当前时间）
     */
    fun getLastDateInCurrentMonth(date: Date): String {
        var str = ""
        val lastDate = Calendar.getInstance()
        lastDate.time = date
        lastDate.set(Calendar.DATE, 1)//设为当前月的1号
        lastDate.add(Calendar.MONTH, 1)//加一个月，变为下月的1号
        lastDate.add(Calendar.DATE, -1)//减去一天，变为当月最后一天

        str = sdf.format(lastDate.time)
        return str
    }

    /**
     * 获取本年度第一天（根据当前时间）
     * @return
     */
    fun getFirstDateInCurrentYear(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR).toString()
        val month = "01"
        val day = "01"
        return "$year-$month-$day"
    }

    /**
     * 获取下个月的第一天，根据输入时间
     * @return
     */
    fun getNextMonth(date: Date): Date {
        val c = Calendar.getInstance()
        c.time = date
        c.set(Calendar.DAY_OF_MONTH, 1)//set the first day.
        c.add(Calendar.MONTH, 1) //set next month
        return c.time
    }

    fun getNextMonthByNum(date: Date, num: Int): Date {
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.MONTH, num) //set next month
        return c.time
    }

    /**
     * 获取明天日期 格式yyyy-MM-dd
     * @return
     */
    fun getNextDay(): String {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_MONTH, 1)
        return sdf.format(c.time)
    }

    /**
     * 日期加上num秒 如DateHelper.addNumDate(date, 100, "second");
     * @param date
     * *
     * @param num
     * *
     * @param type
     * *
     * @return
     */
    fun addNumDate(date: Date, num: Int, type: String?): Date {
        var result: Long = 0
        if (type == null || type == SECOND) {
            result = date.time + num * 1000
        } else if (type == MINUTE) {
            result = date.time + num * 60 * 1000
        } else if (type == HOUR) {
            result = date.time + num * 60 * 60 * 1000
        } else if (type == DAY) {
            result = date.time + num * 24 * 60 * 60 * 1000
        }
        val pos = ParsePosition(0)
        val strtodate = df.parse(df.format(Date(result)), pos)
        return strtodate
    }

    /**
     * 获得一个月共有多少天
     * @param date
     * *
     * @return
     */
    fun getDateSumDay(date: String): String {
        var day = ""
        val month = date.substring(5, 7)
        if (month == "02") {
            if (isLeapYear(date)) {
                day = "29"
            } else {
                day = "28"
            }
        } else if (month == "01" || month == "03" || month == "05" || month == "07" || month == "08" || month == "10" || month == "12") {
            day = "31"
        } else {
            day = "30"
        }
        val dateInfo1 = date.substring(0, 8) + "" + day
        return dateInfo1
    }

    /**
     * 指定日期(默认当天)的第一个时刻
     * @param date
     * *
     * @return
     */
    fun firstTimestampMonth(date: Date?): String {
        var firstDate = sdf.format(Date()) + " 00:00:00"
        if (date != null)
            firstDate = sdf.format(date) + " 00:00:00"
        return firstDate
    }

    /**
     * 指定日期(默认当天)的最后时刻
     * @param date
     * *
     * @return
     */
    fun lastTimestampDate(date: Date?): String {
        var lastTime = sdf.format(Date()) + " 23:59:59"
        if (date != null)
            lastTime = sdf.format(date) + " 23:59:59"
        return lastTime
    }

    /**
     * 比较两个字符型日期是否一致 yyyy-MM-dd
     * @param d1
     * *
     * @param d2
     * *
     * @return
     */
    fun compareDate(d1: String, d2: String): Int {
        var d1 = d1
        var d2 = d2

        if (d1.trim { it <= ' ' }.length > 10) {
            d1 = d1.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        }
        if (d2.trim { it <= ' ' }.length > 10) {
            d2 = d2.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        }
        val date1 = GregorianCalendar()
        val temp1 = d1.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        date1.set(Calendar.YEAR, Integer.parseInt(temp1[0]))
        date1.set(
            Calendar.MONTH,
            Integer.parseInt(if (temp1[1].substring(0, 1) == "0") temp1[1].substring(1) else temp1[1]) - 1
        )
        date1.set(
            Calendar.DATE,
            Integer.parseInt(if (temp1[2].substring(0, 1) == "0") temp1[2].substring(1) else temp1[2])
        )

        val date2 = GregorianCalendar()
        val temp2 = d2.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        date2.set(Calendar.YEAR, Integer.parseInt(temp2[0]))
        date2.set(
            Calendar.MONTH,
            Integer.parseInt(if (temp2[1].substring(0, 1) == "0") temp2[1].substring(1) else temp2[1]) - 1
        )
        date2.set(
            Calendar.DATE,
            Integer.parseInt(if (temp2[2].substring(0, 1) == "0") temp2[2].substring(1) else temp2[2])
        )

        val result = date1.compareTo(date2)
        return result
    }

    /**
     * 在日期中取出指定部分的字符串值(年、月、日、时、分、秒）
     * date 格式yyyy-MM-dd HH:mm:ss
     * @param date
     * *
     * @param part 1年 2月 5日 10时 12分 13秒　Calendar.MONTH
     * *
     * @return
     */
    fun getDatePart(date: String, part: Int): String {
        try {
            val time = df.parse(date)
            df.format(time)
            if (Calendar.MONTH == part)
                return (df.calendar.get(part) + 1).toString()
            else
                return df.calendar.get(part).toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return ""
        }

    }

    fun getWeekText(week: Int): String {
        return when (week) {
            Calendar.SUNDAY -> "周日"
            Calendar.SATURDAY -> "周六"
            Calendar.FRIDAY -> "周五"
            Calendar.THURSDAY -> "周四"
            Calendar.WEDNESDAY -> "周三"
            Calendar.TUESDAY -> "周二"
            Calendar.MONDAY -> "周一"
            else -> ""
        }
    }

    /**
     * 判断date1是否在date2之前
     * @param date1  df格式
     * *
     * @param date2
     * *
     * @return
     */
    fun isDateBefore(date1: String, date2: String): Boolean {
        try {
            return df.parse(date1).before(df.parse(date2))
        } catch (e: ParseException) {
            return false
        }

    }

    /**
     * 判断当前时间是否在时间date2之前
     * @param date2 df格式
     * *
     * @return
     */
    fun isDateBefore(date2: String): Boolean {
        try {
            val date1 = Date()
            return date1.before(df.parse(date2))
        } catch (e: ParseException) {
            return false
        }

    }

    fun isDateBefore(date: Date): Boolean {
        val date1 = Date()
        return date1.before(date)
    }

    /**
     * 是否为同一天
     * @param srcDate
     * *
     * @param tarDate
     * *
     * @return
     */
    fun isSameDay(srcDate: Date, tarDate: Date): Boolean {
        val c1 = Calendar.getInstance()
        c1.time = srcDate
        val c2 = Calendar.getInstance()
        c2.time = tarDate
        val Y = Calendar.YEAR
        val M = Calendar.MONTH
        val D = Calendar.DATE
        if (c1.get(Y) == c2.get(Y) && c1.get(M) == c2.get(M)
            && c1.get(D) == c2.get(D)
        ) {
            return true
        }
        return false
    }

    /**
     * 给出两个日期，计算他们之间相差的年数|月数|天数
     * @param c1 日期1
     * *
     * @param c2 日期2
     * *
     * @param what 比较模式，如果是Calendar.YEAR则在年份上比较；
     * *             如果是Calendar.MONTH则在月数上比较；
     * *             如果是Calendar.DATE则在天数上比较（默认情形）
     * *
     * @return 相差的年数或月数或天数
     */
    fun compare(c1: Calendar, c2: Calendar, what: Int): Int {
        var number = 0
        when (what) {
            Calendar.YEAR -> number = c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR)
            Calendar.MONTH -> {
                val years = compare(c1, c2, Calendar.YEAR)
                number = 12 * years + c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH)
            }
            Calendar.DATE -> number = ((c1.timeInMillis - c2.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            else -> number = ((c1.timeInMillis - c2.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        }
        return number
    }

    /**
     * 2个日期相差多少月
     * @param startDate
     * *
     * @param endDate
     * *
     * @return
     */
    fun compareToMonthByDate(startDate: Date, endDate: Date): Int {
        val startCal = GregorianCalendar()
        val endCal = GregorianCalendar()
        startCal.time = startDate
        endCal.time = endDate
        val a = compare(endCal, startCal, 2)
        return a
    }

    /**
     * @param startDate
     * *
     * @param endDate
     * *
     * @return
     */
    fun compareToMonthByString(startDate: String, endDate: String): Int {
        var number = 0
        try {
            val d1 = sdf.parse(startDate)
            val d2 = sdf.parse(endDate)
            val startCal = GregorianCalendar()
            val endCal = GregorianCalendar()
            startCal.time = d1
            endCal.time = d2
            number = compare(endCal, startCal, 2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return number
    }


    /**
     * 判断是否润年
     * @param ddate
     * *
     * @return
     */
    fun isLeapYear(ddate: String): Boolean {
        /**
         * 详细设计： 1.被400整除是闰年，否则： 2.不能被4整除则不是闰年 3.能被4整除同时不能被100整除则是闰年
         * 3.能被4整除同时能被100整除则不是闰年
         */
        val d = strToDate(ddate)
        val gc = Calendar.getInstance() as GregorianCalendar
        gc.time = d
        val year = gc.get(Calendar.YEAR)
        if (year % 400 == 0)
            return true
        else if (year % 4 == 0) {
            if (year % 100 == 0)
                return false
            else
                return true
        } else
            return false
    }

    /**
     * 判断日期是否为当天
     * @param date
     * *
     * @return
     */
    fun isToday(date: Date): Boolean {

        return dateToStr(date) == dateToStr(now())
    }


    fun addMonths(date: Date, month: Int): Date {
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.MONTH, month)
        return c.time
    }

    /**
     * 按照指定格式将给出的字符串转化成Date对象
     * @param strDate
     * *
     * @param format
     * *
     * @return
     */
    @Throws(Exception::class)
    fun parseDate(strDate: String, format: String): Date {
        val pos = ParsePosition(0)
        val df = SimpleDateFormat(format)
        val strtodate = df.parse(strDate, pos)
        return strtodate
    }

    fun getNexYearByNum(date: Date, num: Int): Date {
        val c = Calendar.getInstance()
        c.time = date
        c.add(Calendar.YEAR, num) //set next month
        return c.time
    }

    //中国习惯 星期一为第一天
    fun getDayOfWeekChina(): Int {
        val cd = Calendar.getInstance()
        // 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
        var dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1 // 因为按中国礼拜一作为第一天所以这里减1
        if (dayOfWeek == 0)
            dayOfWeek = 7
        return dayOfWeek
    }


}