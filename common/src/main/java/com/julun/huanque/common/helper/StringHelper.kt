@file:JvmName("StringHelper")

package com.julun.huanque.common.helper

import android.text.TextUtils
import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.BuildConfig
import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.math.RoundingMode
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.collections.ArrayList


/**
 * Created by nirack on 16-10-27.
 */

object StringHelper {

    private val p = Pattern.compile("\\{\\d+\\}")

    private val anyCharacter = Pattern.compile("\\{[a-zA-Z0-9\u4E00-\u9FA5]*\\}")

    val varExtractorWithDollor = Pattern.compile("\\$\\{[a-zA-Z0-9\u4E00-\u9FA5.]*\\}")

    val EMPTY_STRING = ""

    val DEFAULT_SPLIT_SEPARATOR = ","

    val SPLIT_DOT = "."

    /**
     * 分到元的转换单位.
     */
    val CENT2YUAN = 100.0

    /**
     * 人民币元格式,带小数点后两位
     */
    val RMB_FORMAT = DecimalFormat("¥ ####0.00")
    val RMB_YUAN_FORMAT = DecimalFormat("####0.00")
    val STRING_ZERO = "0"
    val NORMAL_FORMAT = DecimalFormat("#,###")

    /**
     * 一分钟几秒
     */
    val SECONDS_IN_MINUTE = 60

    /**
     * 一小时几秒
     */
    val SECONDS_IN_HOUR = SECONDS_IN_MINUTE * 60

    /**
     * 一天几秒
     */
    val SECONDS_IN_DAY = SECONDS_IN_HOUR * 24

    /**
     * 生成uuid
     */
    fun uuid(): String = UUID.randomUUID().toString()
    /**
     * 生成uuid 去掉“-”
     */
    fun uuid2(): String = UUID.randomUUID().toString().replace("-","")
    /**
     * 判断字符串是否为空

     * @return boolean 空返回true 不为空返回lase
     */
    fun isEmpty(str: String?): Boolean = str?.trim().isNullOrBlank() || "null".equals(str?.trim())

    /**
     * 头部签名使用的判空，和后端保持一致
     */
    fun isWebEmpty(str: String?): Boolean = (str?.length ?: 0) == 0

    /**
     * 判断字符不为NULL，""，null值
     */
    fun isNotEmpty(str: String?): Boolean = !isEmpty(str)


    /**
     * 将给定的params按顺序拼接起来

     * @param params 需要拼接的参数
     * *
     * @return
     */
    fun append(vararg params: Serializable): String = "${params.map { it?.toString() }.joinToString("")}"

    /**
     * 字符串左边补0

     * @param str 字符串
     * *
     * @param num 补0后长度
     * *
     * @return
     */
    fun leftWithZero(str: String, num: Int): String {
        val sb = StringBuffer()
        for (i in 0..num - str.length - 1) {
            sb.append("0")
        }
        return sb.append(str).toString()
    }

    /**
     * 是否數字

     * @param str
     * *
     * @return
     */
    fun isNumeric(str: String): Boolean {
        var i = str.length
        while (--i >= 0) {
            if (!Character.isDigit(str[i])) {
                return false
            }
        }
        return true
    }

    /**
     * 用分隔符将字符串数组连接成字符串

     * @param sep  分隔符
     * *
     * @param args 对象数组,必须保证每个元素不为空,否则将以 空白字符串代替
     * *
     * @return
     */
    fun join(sep: String = DEFAULT_SPLIT_SEPARATOR, vararg args: Any): String =
        if (args == null || args.size == 0) "" else args.joinToString(sep)


    /**
     * 清除特殊字符

     * @param str
     * *
     * @return
     * *
     * @throws PatternSyntaxException
     */
    @Throws(PatternSyntaxException::class)
    fun stringFilter(str: String): String {
        // 只允许字母和数字
        // String   regEx  =  "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        val regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"
        val p = Pattern.compile(regEx)
        val m = p.matcher(str)
        return m.replaceAll("").trim { it <= ' ' }
    }

    /**
     *
     *
     * 根据参数的类型,解析含有占位符的字符串，
     * 字符类型按:'参数'的格式替换 数字类型不加任何字符直接替换
     *

     * @param source 需要格式化的字符串
     * *
     * @param params 要替换的可变参数
     * *
     * @return 格式化后的字符串
     */
    fun format(source: String, vararg params: Any): String {
        return parses(source, "'", *params)
    }

    /**
     *
     *
     * 忽略参数的类型，按照给定的startChar,endChar替换字符串
     *

     * @param source 源字符串
     * *
     * @param params 根据占位符给定的参数
     * *
     * @return
     */
    fun formatIgnoreType(source: String, vararg params: Any): String {
        return parses(source, "", *params)
    }

    /**
     *
     *  功能：格式化带有'{0}'占位符的字符串

     * @param source 源字符串
     * *
     * @param split  添加在目标匹配的字符串开始和结尾的字符
     * *
     * @param params 可变的参数，根据{0}的个数传入相应的参数值
     * *
     * @return 格式化后的字符串
     */
    private fun parses(source: String, split: String, vararg params: Any): String {
        val m = p.matcher(source)
        var i = 0
        var endIndex = 0
        val buf = StringBuffer()
        while (m.find()) {
            var p = "''"
            if (params[i] != null) {
                if (params[i] is String || params[i] is Date
                    || params[i] is Char
                )
                    p = split + params[i++].toString() + split
                else
                    p = params[i++].toString()
                endIndex = m.end()
            }
            m.appendReplacement(buf, p)
        }
        if (endIndex > 0)
            buf.append(source.substring(endIndex)).toString()
        else
            return source
        return buf.toString()
    }

    /**
     * 如果一个字符串为空,返回参数默认值.

     * @param target
     * *
     * @param defaults
     * *
     * @return
     */
    fun ifEmpty(target: String, defaults: String): String {
        return if (isEmpty(target)) defaults else target
    }

    /**
     * 如果一个字符串为空,返回参数默认值.

     * @param target
     * *
     * @return
     */
    fun ifEmpty(target: String): String {
        return if (isEmpty(target)) EMPTY_STRING else target
    }

    fun ifNull(target: String?): String {
        return target ?: EMPTY_STRING
    }

    fun ifNull(target: Int?): String {
        return if (target == null) EMPTY_STRING else target.toString()
    }

    fun ifNullOrZero(target: Int?): String {
        return if (target == null || target === 0) EMPTY_STRING else target.toString()
    }

    /**
     * 是否是一个http请求(包括 https)

     * @param url
     * *
     * @return
     */
    fun isHttpUrl(url: String?): Boolean {
        return isNotEmpty(url) && (url!!.indexOf("http://") > -1 || url.indexOf("https://") > -1)
    }

    fun isNotHttpUrl(url: String?): Boolean {
        return !isHttpUrl(url)
    }

    fun getOssImgUrl(url: String): String {
        if (isNotHttpUrl(url)) {
            return BuildConfig.IMAGE_SERVER_URL + url
        }
        return url
    }

    /**
     * 获取音频地址 与图片一样
     */
    fun getOssAudioUrl(url: String): String {
        if (isNotHttpUrl(url)) {
            return BuildConfig.IMAGE_SERVER_URL + url
        }
        return url
    }

    /**
     * 获取视频地址
     */
    fun getOssVideoUrl(url: String): String {
        if (isNotHttpUrl(url)) {
            return BuildConfig.IMAGE_SERVER_URL + url
        }
        return url
    }

    /**
     * 格式化钱 返回带 人民币符号,两位小数点的字符串. 比如 ￥ 123.33

     * @param cents 以分为单位的数字
     * *
     * @return
     */
    fun formatMoney(cents: Int?): String {
        if (cents == null || cents === 0) {
            return "¥ 0.00"
        }
        return RMB_FORMAT.format(cents / CENT2YUAN)
    }

    fun formatYuanMoney(cents: Int?): String {
        if (cents == null || cents === 0) {
            return "0.00"
        }
        return RMB_YUAN_FORMAT.format(cents / CENT2YUAN)
    }

    /**
     * 格式化钱 返回带 人民币符号,如果 digits 大于 0 ,带 小数点,否则不带 的字符串. 比如 ￥ 123.33

     * @param cents  以分为单位的数字
     * *
     * @param digits 小数点之后的位数.
     * *
     * @return
     */
    fun formatMoney(cents: Int, digits: Int): String {
        var pattern = "¥ ####"
        if (digits > 0) {
            pattern += "."
            for (index in 0..digits - 1) {
                pattern += "0"
            }
        }
        val df2 = DecimalFormat(pattern)
        val format = df2.format(cents / CENT2YUAN)
        return format
    }

    /**
     * 格式化钱 返回带 人民币符号,如果 digits 大于 0 ,带 小数点,否则不带 的字符串. 比如 ￥ 123.33

     * @param cents  以分为单位的数字
     * *
     * @param digits 小数点之后的位数.
     * *
     * @return
     */
    fun formatMoneyNoSignal(cents: Int, digits: Int): String {
        var pattern = "####"
        if (digits > 0) {
            pattern += "."
            for (index in 0..digits - 1) {
                pattern += "0"
            }
        }
        val df2 = DecimalFormat(pattern)
        val format = df2.format(cents / CENT2YUAN)
        return format
    }

    fun appendMoney(money: Serializable): String {
        return append("¥ ", money)
    }

    fun appendMoney(fuhao: String, money: Serializable): String {
        return append(fuhao, money)
    }

    fun appendNumSymbol(money: Serializable): String {
        return append("x ", money)//×
    }

    fun second2time(seconds: Int): String {
        var seconds = seconds
        if (seconds < 1) {
            return STRING_ZERO
        }
        var result = EMPTY_STRING
        //日时分秒
        val days = seconds!! / SECONDS_IN_DAY
        if (days > 0) {
//            result += "$days天"
            result += "${days}天"
            seconds = seconds % SECONDS_IN_DAY
        }
        val hours = seconds / SECONDS_IN_HOUR
        if (hours > 0) {
            result += "${hours}小时"
            seconds = seconds % SECONDS_IN_HOUR
        }
        val minutes = seconds / SECONDS_IN_MINUTE
        if (minutes > 0) {
            result += "${minutes}分钟"
            seconds = seconds % SECONDS_IN_MINUTE
        }
        if (seconds > 0) {
            result += "${seconds}秒"
        }

        return result
    }

    fun stringValue(tag: Any?): String = if (tag == null) EMPTY_STRING else tag.toString()


    /**
     * 将汉字替换为空字符串.
     * @param raw
     * *
     * @return
     */
    fun replaceChineseCharacterWithEmptyString(raw: String): String = raw.replace("[^x00-xff]*".toRegex(), "")

    /**
     * 将所有的数字、字母及标点全部转为全角字符
     */
    fun toDBC(input: String): String {
        val c = input.toCharArray()
        for (i in c.indices) {
            if (c[i].toInt() == 12288) {
                c[i] = 32.toChar()
                continue
            }
            if (c[i].toInt() in 65281..65374)
                c[i] = (c[i].toInt() - 65248).toChar()
        }
        return String(c)
    }

    /**
     * 用空格填充数字两边,
     * @param number 要格式化的数字
     * @param length  格式化之后的最小长度
     */
    fun paddingNumberWithEmptyChar(number: Int, length: Int): String {
        val tmp = "$number"
        val diff: Int = length - tmp.length
        if (diff <= 0) return tmp
        if (diff == 1) {
            return " $number"
        }
        val left = diff / 2
        val plusMore = if (diff % 2 == 1) 1 else 0
        val RMB_YUAN_FORMAT = DecimalFormat("${generatePlaceHolders(left + plusMore)}0${generatePlaceHolders(left)}")
        return RMB_YUAN_FORMAT.format(number)
    }

    /**
     * 用空格填充数字两边,并且是使得两边空格一样多
     * @param number 要格式化的数字
     * @param length  格式化之后的最小长度
     */
    fun paddingNumberWithEmptyCharEven(number: Int, minLength: Int): String {
        var length: Int = "$number".length
        val diff = minLength - length
        if (diff <= 0) {
            return paddingNumberWithEmptyChar(number, length)
        }

        if (diff % 2 != 0) {
            length = minLength + 1
        } else {
            length = minLength
        }
        return paddingNumberWithEmptyChar(number, length)
    }

    private fun generatePlaceHolders(left: Int) = (1..left).map { " " }.toString().replace(", ", "").substring(1, left + 1)
    fun formatNum(value: Int): String {
        if (value < 10000)
            return "${value}萌豆"
        else
            return "${value / 10000}万萌豆"
    }

    //格式化数字 三位一个逗号
    fun formatNumber(num: Double): String {
        return NORMAL_FORMAT.format(num)
    }

    //格式化数字 三位一个逗号
    fun formatNumber(num: Int): String {
        return NORMAL_FORMAT.format(num)
    }

    //格式化数字 三位一个逗号
    fun formatNumber(num: Long): String {
        return NORMAL_FORMAT.format(num)
    }

    /**
     * 格式化数字 大于999
     */
    fun formatNum(num: Long): String {
        if (num < 10000) {
            return num.toString()
        }
//        if (num < 10000) {
//            return DecimalFormat("#.#").format((num / 1000.0)) + "k"
//        }
        return DecimalFormat("#.#").format((num / 10000.0)) + "w"
    }

    /*对贡献榜格式进行处理*/
    fun formatCoinsCount(count: Long): String {
        return if (count < 10000) {
            "$count"
        } else {
            val format = DecimalFormat("#.0")
            format.roundingMode = RoundingMode.DOWN
            format.format((count / 10000.0)) + "万"
        }
    }

    /*对PK结果格式进行处理*/
    fun formatScore(count: Long): String {
        return if (count < 1000000) {
            "$count"
        } else {
            val format = DecimalFormat("#.00")
            format.roundingMode = RoundingMode.DOWN
            format.format((count / 10000.0)) + "万"
        }
    }

    /*对玩家经验值格式进行处理*/
    fun formatUserScore(count: Long): String {
        return if (count < 999999999) {
            "$count"
        } else {
            val format = DecimalFormat("#")
            format.roundingMode = RoundingMode.HALF_UP
            format.format((count / 10000.0)) + "万"
        }
    }

    /*对消息未读数格式进行处理*/
    fun formatMessageCount(count: Int): String {
        return if (count <= 99) {
            "$count"
        } else {
            "99+"
        }
    }

    /*文字超过3个加省略号*/
    fun formatName(name: String): String {
        return if (name.length > 3) {
            "${name.substring(0, 3)}…"
        } else {
            name
        }
    }

    /**
     * 格式化萌豆  3位一个‘，’，最多保留两位小数
     */
    fun formatMengDou(num: Double): String {
        val format = NumberFormat.getInstance()
        format.maximumFractionDigits = 2
        return format.format(num)
    }

    /**
     * 对请求的body进行处理
     */
    fun parseBodyString(bodyString: String): String {
        var str = ""
        if (bodyString.isBlank()) return str
        val result = JSONObject.parseObject(bodyString)
        val listKey = ArrayList(result.keys)
        Collections.sort(listKey)


        var temp: String
        var value: String
        for (key in listKey) {
            value = result.getString(key)
            if (isWebEmpty(value)) {
                continue
            }
            temp = key + "=" + (URLEncoder.encode(value, "UTF-8").replace("+", "%20"))
            if ("" == str) {
                str = temp
            } else {
                str += "&" + temp
            }
        }
        return str
    }

    /**
     * 因okhttp3的请求头不允许中文出现的情况，检测非法字符进行encode上传
     * 如果发现非法字符，采用UrlEncode对其进行编码
     */
    fun getValidUA(userAgent: String): String {
        if (TextUtils.isEmpty(userAgent)) {
            return ""
        }
        var validUA = ""
        val uaWithoutLine = userAgent.replace("\n", "")
        var i = 0
        val length = uaWithoutLine.length
        while (i < length) {
            val c = userAgent[i]
            if (c <= '\u001f' || c >= '\u007f') {
                try {
                    validUA = URLEncoder.encode(uaWithoutLine, "UTF-8")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                return validUA
            }
            i++
        }
        return uaWithoutLine
    }

    /**
     * 获取URL参数对应的值
     *
     * @param urlParams url地址参数：如http://www.baidu.com?a=123&c=123或者d=123&3=11
     * @param name      参数名称
     * @return
     */
    fun getQueryString(urlParams: String, name: String): String? {
        val pattern = Pattern.compile("(^|&|\\?)$name=([^&]*)(&|$)")
        return getQueryString(urlParams, pattern)
    }

    /**
     * 获取URL参数对应的值
     *
     * @param urlParams url地址参数：如http://www.baidu.com?a=123&c=123或者d=123&3=11
     * @param pattern   匹配正则
     * @return
     */
    fun getQueryString(urlParams: String, pattern: Pattern): String? {
        val matcher = pattern.matcher(urlParams)
        if (matcher.find()) {
            val data = matcher.group().replace("&", "").split("=")
            if (data.size > 1) {
                return data[1]
            }
        }
        return null
    }

    /**
     * 查询对应的广告配置
     * @param userId 用户id
     * @param str 字符串 -> 例：userId_时间戳#userId_Type#
     * @return 返回一个列表，里面保存用户id和(时间戳 or Type)
     */
    fun queryAdConfig(userId: String, str: String): List<String>? {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(str)) {
            return null
        }
        try {
            val pattern = Pattern.compile("$userId[0-9_.a-zA-Z]+")
            val matcher = pattern.matcher(str)
            if (matcher.find()) {
                return matcher.group().split("_")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


}