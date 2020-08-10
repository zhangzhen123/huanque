package com.julun.huanque.common.utils

import android.text.TextUtils
import com.julun.huanque.common.helper.reportCrash
import java.lang.StringBuilder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

private const val SECOND = 1000L
const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR

/**
 * 格式化剩余时间  eg:1天10:12:12
 * @param time 毫秒
 */
fun formatPrivateExperienceTime(time: Long): String {
    val fTime = StringBuilder()
    //天
    if (time > DAY) {
        fTime.append("${time / DAY}").append("天")
    }
    //小时
    val hourTime = time % DAY

    val hour = hourTime / HOUR
    if (hour <= 9) {
        fTime.append("0")
    }
    fTime.append("$hour").append(":")
    //分钟
    val minute = time % HOUR / MINUTE
    if (minute <= 9) {
        fTime.append("0")
    }
    fTime.append("$minute").append(":")
    //秒
    val second = time % MINUTE / SECOND
    if (second <= 9) {
        fTime.append("0")
    }
    fTime.append("$second")

    return fTime.toString()
}

/**
 * Created by dong on 2018/3/16.
 */
object TimeUtils {
    val TIME_FORMAT_YEAR_1 = "yyyy-MM-dd HH:mm"

    val TIMEFORMAT1: String = "HH:mm:ss"
    val TIMEFORMAT2: String = "mm:ss"
    val TIMEFORMAT3: String = "ss:SS"
    val TIMEFORMAT4: String = "hh:mm"

    fun formatTime(date: Long): String {
        return SimpleDateFormat(TIMEFORMAT2).format(Date(date))
    }

    fun formatTime(date: Long, timeType: String): String {
        return SimpleDateFormat(timeType).format(Date(date))
    }

    fun doWithData(previousLive: String?): String {
        if (previousLive?.isNotEmpty() == true) {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            try {
                val datePrevious = format.parse(previousLive)?.time ?: 0
                val currentTime = System.currentTimeMillis()
                val intervalSecond = (currentTime - datePrevious) / 1000
                var tempStr: String
                when (intervalSecond) {
                    in 1..59 -> {
                        tempStr = "1分钟前"
                    }
                    in 60..(60 * 60 - 1) -> {
                        tempStr = "${intervalSecond / 60}分钟前"
                    }
                    in (60 * 60)..(60 * 60 * 24 - 1) -> {
                        tempStr = "${intervalSecond / (60 * 60)}小时前"
                    }
                    else -> {
                        tempStr = if (intervalSecond < 0) {
                            ""
                        } else {
                            "${(intervalSecond / (60 * 60 * 24))}天前"
                        }
                    }
                }

                return tempStr

            } catch (pE: ParseException) {
                reportCrash("AnchorIsNotOnLineFragment", pE)
                return ""
            }


        } else {
            return ""
        }

    }

    /**
     * 转换成时间戳
     */
    fun getMillisTime(formatDate: String): Long {
        if (TextUtils.isEmpty(formatDate)) {
            return 0L
        }
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.parse(formatDate).time
    }

    /**
     * 得到当前的时间
     */
    fun getTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate)
    }

    /**
     * 获取当前的时间并显示成日期
     */
    fun getTimeShowDate(): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val curDate = Date(System.currentTimeMillis())
        return formatter.format(curDate)
    }

    /**
     * 格式化时间
     * @param time 时间戳
     */
    fun getDataFormat(time: Long): String {
        if (time == 0L) {
            return ""
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val curDate = Date(time)
        return formatter.format(curDate)
    }

    /**
     * 获取中国时区格式化时间
     */
    fun getDateFormat(): SimpleDateFormat {
        if (null == DateLocal.get()) {
            DateLocal.set(SimpleDateFormat("yyyy-MM-dd", Locale.CHINA))
        }
        return DateLocal.get()!!
    }

    private val DateLocal = ThreadLocal<SimpleDateFormat>()

    /**
     * 判断是否为今天(效率比较高)
     *
     * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @return true今天 false不是
     * @throws ParseException
     */
    fun isToday(day: String): Boolean {
        try {
            val pre = Calendar.getInstance()
            val predate = Date(System.currentTimeMillis())
            pre.time = predate
            val cal = Calendar.getInstance()
            val date = getDateFormat().parse(day)
            cal.time = date
            if (cal.get(Calendar.YEAR) === pre.get(Calendar.YEAR)) {
                val diffDay = cal.get(Calendar.DAY_OF_YEAR) - pre.get(Calendar.DAY_OF_YEAR)

                if (diffDay == 0) {
                    return true
                }
            }
            return false
        } catch (e: ParseException) {
            return false
        }
    }

    /**
     * 时间转化为星期
     *
     * @param indexOfWeek 星期的第几天
     */
    fun getWeekDayStr(indexOfWeek: Int): String {
        var weekDayStr = ""
        when (indexOfWeek) {
            1 -> weekDayStr = "星期日"
            2 -> weekDayStr = "星期一"
            3 -> weekDayStr = "星期二"
            4 -> weekDayStr = "星期三"
            5 -> weekDayStr = "星期四"
            6 -> weekDayStr = "星期五"
            7 -> weekDayStr = "星期六"
        }
        return weekDayStr
    }

    /**
     * 时间戳转化对应的时间输出格式
     * @param createTime 毫秒
     * 时间小于60秒，显示“刚刚“；
     * 时间大于60秒，小于60分钟，显示格式为“X分钟前”；
     * 时间大于60分钟，小于24小时，显示格式为 "时:分"，如“10:56”；
     * 一天到二天内的消息显示为：“昨天 时:分”，如“昨天 11:21”；
     * 时间大于2天小于30天，显示格式为 “月-日 时:分" ，如“1-16 10:56；
     * 时间大于30天，小于12个月，显示格式为“月-日”；
     * 时间以年为跨度时，显示格式为“年-月-日”，如“2018-12-11”；
     * 时、分不足二位时，前面用0补齐，月、日不足二位时不补位。如：2018-7-13 09:22
     */
    fun formatDetailTime(createTime: Long?): String {
        if (createTime == null || createTime <= 0L) {
            return "刚刚"
        }
        val tMin = 60 * 1000
        val tHour = 60 * tMin
        val formartTime = getDataFormat(createTime)
        try {
            val tDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(formartTime)
            val today = Date()
            val thisYearDf = SimpleDateFormat("yyyy")
            val thisYear = Date(thisYearDf.parse(thisYearDf.format(today)).time)
            if (tDate != null) {
                val dTime = today.time - tDate.time
                if (!tDate.before(thisYear)) {
                    if (dTime < tMin) {
                        return "刚刚"
                    } else if (dTime < tHour) {
                        return "${Math.ceil((dTime / tMin).toDouble()).toInt()}分钟前"
                    }
                }
            }

            val inputTime = Calendar.getInstance()
            inputTime.timeInMillis = createTime
            val currenTimeZone = inputTime.time
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            if (calendar.before(inputTime)) {
                val sdf = SimpleDateFormat("HH:mm")
                return sdf.format(currenTimeZone)
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            if (calendar.before(inputTime)) {
                val sdf = SimpleDateFormat("HH:mm")
                return "昨天 " + sdf.format(currenTimeZone)
            }
            //还原为之前的月份
//            calendar.add(Calendar.DAY_OF_MONTH, -5)
//            if (calendar.before(inputTime)) {
//                val sdf = SimpleDateFormat("HH:mm")
//                return getWeekDayStr(inputTime.get(Calendar.DAY_OF_WEEK)) + " " + sdf.format(currenTimeZone)
//            }
            else {
                //还原到之前的月份
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                if (calendar.before(inputTime)) {
                    val currentTime = System.currentTimeMillis()
                    var day: Long = 0L
                    if (currentTime > createTime) {
                        val dTime = currentTime - createTime
                        day = dTime / 1000 / 60 / 60 / 24
                    }
                    val sdf = SimpleDateFormat("MM" + "-" + "dd" + " ")
                    val temp1 = sdf.format(currenTimeZone)
                    if (day < 30) {
                        val sdf1 = SimpleDateFormat("HH:mm")
                        val temp2 = sdf1.format(currenTimeZone)
                        return temp1 + temp2
                    } else {
                        return temp1
                    }
                } else {
                    val sdf = SimpleDateFormat("yyyy" + "-" + "MM" + "-" + "dd" + " " + "HH" + ":" + "mm")
                    return sdf.format(currenTimeZone)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportCrash("时间解析错误 + createTime = ${createTime}", e)
            return "刚刚"
        }
    }

    /**
     * 时间戳转化对应的时间输出格式
     * @param createTime 毫秒
     * 今天：显示hh:mm
     * 昨日送达消息，显示时间：昨日 hh:mm。例如昨日11:50，分钟向下取整，忽略秒。
     * 最近2-6天消息，显示时间：星期 hh:mm。例如星期五 11:40，分钟向下取整，忽略秒。
     * 最近7天以上消息，显示时间：yy-mm-dd hh:mm。例如2018-12-6 11:50，分钟向下取整，忽略秒
     * @param yearPattern 年月日的样式
     */
    fun formatMessageTime(createTime: Long?, yearPattern: String = "yyyy-MM-dd HH:mm"): String {
        if (createTime == null || createTime <= 0L) {
            return ""
        }
        try {
            val inputTime = Calendar.getInstance()
            inputTime.timeInMillis = createTime
            val currenTimeZone = inputTime.time
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            if (calendar.before(inputTime)) {
                //今天的日期
                val sdf = SimpleDateFormat("HH:mm")
                return sdf.format(currenTimeZone)
            }
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            if (calendar.before(inputTime)) {
                //昨天的日期
                val sdf = SimpleDateFormat("HH:mm")
                return "昨天 " + sdf.format(currenTimeZone)
            }
            calendar.add(Calendar.DAY_OF_MONTH, -5)
            if (calendar.before(inputTime)) {
                val sdf = SimpleDateFormat("HH:mm")
                return getWeekDayStr(inputTime.get(Calendar.DAY_OF_WEEK)) + " " + sdf.format(currenTimeZone)
            } else {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
                return sdf.format(currenTimeZone)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reportCrash("时间解析错误 + createTime = ${createTime}", e)
            return ""
        }
    }

    /**
     * [time]秒数 时间转化 单位 秒 转换成 HH:mm:ss
     */
    fun countDownTimeFormat(time: Long): String {
        if (time == 0L) {
            return "00:00:00"
        }
        val mi = 60
        val hh = mi * 60
//        val dd = hh * 24

//        val day = time / dd
        val hour = time / hh
        val minute = (time - hour * hh) / mi
        val second = (time - hour * hh - minute * mi)

        val strHour: String
        strHour = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }
        val strMin: String
        strMin = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
        val strS: String
        strS = if (second < 10) {
            "0$second"
        } else {
            "$second"
        }

        return "$strHour:$strMin:$strS"
    }

    /**
     * [time]秒数 时间转化 单位 秒 转换成 mm:ss
     */
    fun countDownTimeFormat1(time: Long): String {
        if (time <= 0L) {
            return "00:00"
        }
        val mi = 60
//        val hh = mi * 60
//        val dd = hh * 24

//        val day = time / dd
//        val hour = time / hh
        val minute = time / mi
        val second = (time - minute * mi)

        val strMin: String
        strMin = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
        val strS: String
        strS = if (second < 10) {
            "0$second"
        } else {
            "$second"
        }

        return "$strMin:$strS"
    }

    /**
     * [time]秒数 时间转化 单位 秒 转换成 x天x时x分x秒
     */
    fun countDownTimeFormat2(time: Long): String {
        if (time == 0L) {
            return "0天00时00分00秒"
        }
        val mi = 60
        val hh = mi * 60
        val dd = hh * 24

        val day = time / dd
        val hour = (time - day * dd) / hh
        val minute = (time - day * dd - hour * hh) / mi
        val second = (time - day * dd - hour * hh - minute * mi)

        val dayStr: String
        dayStr = "$day"
        val strHour: String
        strHour = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }
        val strMin: String
        strMin = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
        val strS: String
        strS = if (second < 10) {
            "0$second"
        } else {
            "$second"
        }

        return "${dayStr}天${strHour}时${strMin}分${strS}秒"
    }


    /**
     * [time]秒数 时间转化 单位 秒 转换成 x时x分x秒
     */
    fun countDownTimeFormat3(time: Long): String {
        if (time == 0L) {
            return "00时00分00秒"
        }
        val mi = 60
        val hh = mi * 60
        val hour = time / hh
        val minute = (time - hour * hh) / mi
        val second = (time - hour * hh - minute * mi)

        val strHour: String
        strHour = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }
        val strMin: String
        strMin = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
        val strS: String
        strS = if (second < 10) {
            "0$second"
        } else {
            "$second"
        }

        return "${strHour}时${strMin}分${strS}秒"
    }

    /**
     * [time]秒数 时间转化 单位 秒 转换成 x天x时x分
     */
    fun countDownTimeFormat4(time: Long): String {
        if (time == 0L) {
            return "0天00时00分"
        }
        val mi = 60
        val hh = mi * 60
        val dd = hh * 24

        val day = time / dd
        val hour = (time - day * dd) / hh
        val minute = (time - day * dd - hour * hh) / mi
//        val second = (time - day * dd - hour * hh - minute * mi)

        val dayStr: String
        dayStr = "$day"
        val strHour: String
        strHour = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }
        val strMin: String
        strMin = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
//        val strS: String
//        strS = if (second < 10) {
//            "0$second"
//        } else {
//            "$second"
//        }

        return "${dayStr}天${strHour}时${strMin}分"
    }

    /**
     * [time]秒数 时间转化 单位 秒 转换成 x天x:x:x
     */
    fun countDownTimeFormat5(time: Long): String {
        if (time == 0L) {
            return "0天00:00:00"
        }
        val mi = 60
        val hh = mi * 60
        val dd = hh * 24

        val day = time / dd
        val hour = (time - day * dd) / hh
        val minute = (time - day * dd - hour * hh) / mi
        val second = (time - day * dd - hour * hh - minute * mi)

        val dayStr: String
        dayStr = "$day"
        val strHour: String
        strHour = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }
        val strMin: String
        strMin = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
        val strS: String
        strS = if (second < 10) {
            "0$second"
        } else {
            "$second"
        }
        return "${dayStr}天${strHour}:${strMin}:${strS}"
    }

    /**
     * 时间转化 单位 秒 转换成 HH:mm:ss
     */
    fun countDownTimeFormatCahtRoom(time: Long): String {
        if (time == 0L) {
            return "00:00"
        }
        val mi = 60
        val hh = mi * 60
//        val dd = hh * 24

//        val day = time / dd
        val hour = time / hh
        val minute = (time - hour * hh) / mi
        val second = (time - hour * hh - minute * mi)

        val strHour: String
        strHour = if (hour < 10) {
            "0$hour"
        } else {
            "$hour"
        }
        val strMin: String
        strMin = if (minute < 10) {
            "0$minute"
        } else {
            "$minute"
        }
        val strS: String
        strS = if (second < 10) {
            "0$second"
        } else {
            "$second"
        }


        return if (hour > 0) {
            "$strHour:$strMin:$strS"
        } else {
            "$strMin:$strS"
        }
    }

    /**
     * 封盘剩余时间(PK使用)
     */
    fun sealedTime(time: Long): String {
        var strTime = StringBuffer()
        val second = time % 60
        val min = time / 60
        if (min != 0L) {
            strTime.append("${min}分")
        }
        if (second >= 10) {
            strTime.append("$second")
        } else {
            if (strTime.isNotEmpty()) {
                strTime.append("0")
            }
            strTime.append("$second")
        }
        strTime.append("秒")
        return strTime.toString()
    }

    /**
     * 获取格式化时间的一部分
     * @param format 格式化时间 -> 必须是此格式(2018-12-6 11:50:00)
     * @param getYear 是否截取年月日 true , false -> 截取时分秒
     */
    fun getPartOfFormatDate(format: String, getYear: Boolean): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val d = formatter.parse(format)
            return if (getYear) {
                SimpleDateFormat("yyyy.MM.dd").format(d)
            } else {
                SimpleDateFormat("HH:mm:ss").format(d)
            }
        } catch (e: Exception) {
            ""
        }
    }
}