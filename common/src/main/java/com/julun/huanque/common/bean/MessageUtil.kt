package com.julun.huanque.common.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.TplBeanExtraContext
import com.julun.huanque.common.constant.MessageDisplayType
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.julun.huanque.common.widgets.live.chatInput.EmojiUtil
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by nirack on 16-11-4.
 */

//fontWeight:
class StyleParam(
    var styleType: String = "", var preffix: String = "", var color: String? = null,
    var bgColor: String = "", var radius: Int = -1, var el: String = "", var source: String = "",
    var fontWeight: String = "", var underLineColor: String? = null
)

//直播间消息实体类
class ChatMessageBean(var content: Any, var showType: Int) : MultiItemEntity {
    override val itemType: Int
        get() = showType
}

open class TplBean(
    var textParams: HashMap<String, String> = hashMapOf(),
    var context: TplBeanExtraContext? = null,
    var textTpl: String = "",
    var textTouch: String? = null,
    var styleParamMap: MutableMap<String, StyleParam> = mutableMapOf(), //styleParams字符串转换为map
    var textStyles: String = "",
    var display: String = "",
    var userInfo: RoomUserChatExtra? = null,
    //是否隐藏背景  true的时候隐藏，其它时候显示(处理系统消息可能不需要背景)
//    var notShowBackColor: Boolean = false,
    //本地字段，暂时丢弃，没有问题，后期删除
//    var useBg: Boolean = false,//是否需要背景
    var realTxt: String = "",
    var paramIndexInRealText: MutableMap<String, MutableList<Int>> = mutableMapOf(),
    val randomGeneratedId: String = StringHelper.uuid(),
    //私聊消息的标识位
    var privateMessage: Boolean = false,
    var finalProcessed: Boolean = false,
    //周星类型
    open var beanType: String = ""
) {


    /**
     * 常常报错的  解析消息的时候,参数可能有问题 的 错误,可能的原因是 没有加锁引起的并发冲突,
     * 同一个消息如果在多个地方处理 #extra 方法 ,而碰巧又同时在不同线程里执行连次该方法,就可能引起这个错误
     * 加上 @Synchronized 标记解决这个问题
     *
     */
    @Synchronized
    fun preProcess(): TplBean {
        var bean = this
        if (!finalProcessed) {
            bean = this.extra()
        }
        return bean
    }

    private fun extra(): TplBean {
        //防止多次处理造成混乱
        if (finalProcessed) return this

        //处理styleParamMap  不走默认json解析器时这里styleParamMap不会有值只能手动设置
        if (styleParamMap.isEmpty() && textStyles.isNotEmpty()) {
            val textStyleList = JsonUtil.deserializeAsObjectList(textStyles, StyleParam::class.java)
            textStyleList?.let {
                styleParamMap.putAll(textStyleList.map { it.el to it })
            }
        }
        //不再使用 清空掉
        textStyles = ""

        //emoji图标
        val pattern = Pattern.compile("\\[[a-zA-Z0-9\u4E00-\u9FA5]*\\]")

        val oneEmptyChar = " "//填充个空格.投机取巧之策,表示汗颜
        val imagePlaceHolderChar = "#"//填充图片用于占位的字符,这里不要使用空格,否则会出现图片无法显示的情况
        //下面一行,图片的 margin 无奈之举....
        this.textTpl = this.textTpl.replace("\${levelValue}", "$oneEmptyChar\${levelValue}")
        //去掉 礼物和数量之间的空格,通过图片的margin来处理来
//        this.textTpl = this.textTpl.replace("\${goodsPicId} x\${formObj.count}", "\${goodsPicId}×\${formObj.count}")

//     ${userLevel}${royalLevel}${nickname} 送了 ${extMap.giftName}${extMap.giftPic} x${formMap.count}

        // 搞emoji
        val emojiMatch: Matcher = pattern.matcher(textTpl)
        while (emojiMatch.find()) {
            val group = emojiMatch.group()
            val replace = group.replace("[", "\${").replace("]", "}")
            styleParamMap.put(
                replace,
                StyleParam(
                    el = replace,
                    styleType = MessageUtil.KEY_IMG,
                    preffix = MessageUtil.PREFIX_EMOJI,
                    source = MessageUtil.KEY_LOCAL
                )
            )
            val indexOf = EmojiUtil.EmojiTextArray.indexOf(group)
            textTpl = textTpl.replace(group, replace)
            textParams.put(replace, "$indexOf")
        }

        //加上发言人信息
        userInfo?.let {
            val nicknameKey = "\${extra.user.nickName}"
            val royalIconKey = "\${extra.user.royalLevelIcon}"
            val levelIconKey = "\${extra.user.userLevelIcon}"
            val guardIconKey = "\${extra.user.guardIcon}"
            val anchorLevelKey = "\${extra.user.anchorLevelIcon}"

//            val roomManagerPicKey = "\${extra.user.roomManagerPic}"
//            val officalManagerPicKey = "\${extra.user.officalManagerPic}"

            val prepositionKey = "\${preposition}" //介词
            val toNicknameKey = "\${toNickName}" //目标人姓名
            val anchorLevel: Int = it.anchorLevel//主播等级
            //1.首先设置全局颜色
            styleParamMap[MessageUtil.KEY_ALL] = StyleParam(styleType = MessageUtil.KEY_BASIC, color = "#FFFFFF",fontWeight = DraweeSpanTextView.BOLD)
            //2.主播的发言处理
            if (anchorLevel > 0 && it.targetUserObj?.nickname !== null && "${it.senderId}" == "${SessionUtils.getUserId()}") {

                textTpl = "$nicknameKey $prepositionKey $toNicknameKey $textTpl"
                textParams.put(prepositionKey, "对")
                styleParamMap.put(prepositionKey, StyleParam(el = prepositionKey, color = "#000000"))
                textParams.put(toNicknameKey, "${it.targetUserObj?.nickname}")
                styleParamMap.put(toNicknameKey, StyleParam(el = toNicknameKey, color = "#FFD630"))
            } else {
                textTpl = "$nicknameKey $textTpl"
            }
            //3.用户昵称处理
            val tNickName: String = if (it.nickname.isNotBlank()) {
                if (privateMessage) {
                    //私聊消息，昵称之后添加'：'，对添加的‘：’也渲染
                    "${it.nickname}："
                } else {
                    it.nickname
                }
            } else {
                it.nickname
            }


            textParams.put(nicknameKey, tNickName)
            val nick = if (StringHelper.isEmpty(it.nickcolor)) null else it.nickcolor
            styleParamMap.put(nicknameKey, StyleParam(el = nicknameKey, color = nick))

            //4.添加勋章
            val goodsList = it.badgesPic
            goodsList.reversed().forEachIndexed { i, s ->
                val goodsIconKey = "\${extra.user.goodsIconKey$i}"
                textTpl = "$goodsIconKey$textTpl"
                textParams.put(goodsIconKey, "$s")
                styleParamMap.put(
                    goodsIconKey,
                    StyleParam(
                        el = guardIconKey,
                        styleType = MessageUtil.KEY_IMG,
                        source = MessageUtil.KEY_REMOTE,
                        preffix = MessageUtil.PREFIX_USER_LEVEL
                    )
                )
            }
//            //5.房管图标 官方图标
//            if (it.roomManagerPic.isNotBlank()) {
//                textTpl = "$roomManagerPicKey$textTpl"
//                textParams.put(roomManagerPicKey, it.roomManagerPic)
//                styleParamMap.put(roomManagerPicKey, StyleParam(el = roomManagerPicKey, styleType = MessageUtil.KEY_IMG, source = MessageUtil.KEY_REMOTE))
//            }
//            if (it.officalManagerPic.isNotBlank()) {
//                textTpl = "$officalManagerPicKey$textTpl"
//                textParams.put(officalManagerPicKey, it.officalManagerPic)
//                styleParamMap.put(officalManagerPicKey, StyleParam(el = officalManagerPicKey, styleType = MessageUtil.KEY_IMG, source = MessageUtil.KEY_REMOTE))
//            }
            //6.主播等级处理  普通用户等级和贵族处理
            if (anchorLevel >= 0) {//主播
                textTpl = "$anchorLevelKey$textTpl"
                textParams.put(anchorLevelKey, "$anchorLevel")
                styleParamMap.put(
                    anchorLevelKey,
                    StyleParam(
                        el = anchorLevelKey,
                        styleType = MessageUtil.KEY_IMG,
                        source = MessageUtil.KEY_LOCAL,
                        preffix = MessageUtil.PREFIX_ANCHOR_LEVEL
                    )
                )

            } else {//普通用户
//                val guard = it.roomGuardPic
//                if (StringHelper.isNotEmpty(guard)) {
//                    textTpl = "$guardIconKey$textTpl"
//                    textParams.put(guardIconKey, "$guard")
//                    styleParamMap.put(guardIconKey, StyleParam(el = guardIconKey, styleType = MessageUtil.KEY_IMG, source = MessageUtil.KEY_REMOTE, preffix = MessageUtil.PREFFIX_USER_LEVEL))
//                }
                //贵族等级图片改成远程
                val royalPic = it.royalPic
                val royalLevel = it.royalLevel
                if (royalPic.isNotEmpty()) {
                    textTpl = "$royalIconKey$textTpl"
                    textParams.put(royalIconKey, royalPic)
                    styleParamMap.put(
                        royalIconKey,
                        StyleParam(
                            el = royalIconKey,
                            styleType = MessageUtil.KEY_IMG,
                            source = MessageUtil.KEY_REMOTE,
                            preffix = MessageUtil.PREFIX_ROYAL_LEVEL
                        )
                    )
                } else if (royalLevel > -1 && royalLevel < 9) {
                    //兼容老版本发言消息
                    textTpl = "$royalIconKey$textTpl"
                    textParams.put(royalIconKey, "$royalLevel")
                    styleParamMap.put(
                        royalIconKey,
                        StyleParam(
                            el = royalIconKey,
                            styleType = MessageUtil.KEY_IMG,
                            source = MessageUtil.KEY_LOCAL,
                            preffix = MessageUtil.PREFIX_ROYAL_LEVEL
                        )
                    )
                }
//                if (it.displayType?.contains(MessageDisplayType.MYSTERY) != true || it.userLevel < 1) {
                    //不是神秘人
                    textTpl = "$levelIconKey$textTpl"
                    textParams.put(levelIconKey, "${it.userLevel}")
//                }
                styleParamMap.put(
                    levelIconKey,
                    StyleParam(
                        el = levelIconKey,
                        styleType = MessageUtil.KEY_IMG,
                        source = MessageUtil.KEY_LOCAL,
                        preffix = MessageUtil.PREFIX_USER_LEVEL
                    )
                )

            }

        }
//        //处理背景颜色圆角 如果没有 本地设置默认颜色 圆角
//        if(useBg){
//            val allparams: StyleParam? = styleParamMap[MessageUtil.KEY_ALL]
//            if (allparams != null) {
//                if (allparams.bgColor.isBlank()) {
//                    allparams.bgColor = MessageUtil.MESSAGE_BG
//                }
//                if (allparams.radius == -1) {
//                    allparams.radius = MessageUtil.MESSAGE_BG_RADIUS
//                }
//            }
//        }

        val paramKeySorted: ArrayList<String> = arrayListOf<String>().apply {
            val matcher = StringHelper.varExtractorWithDollor.matcher(textTpl)
            while (matcher.find()) {
                add(matcher.group())
            }
        }
        if (paramKeySorted.size == 0) {
            this.realTxt = textTpl       //这里返回之前需要将 realTxt 赋值,否则部分消息无法渲染
            return this
        }

        // 过滤 0等级的贵族 , 处理远程图片的 间隔
        val iterator = paramKeySorted.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()

            val styleParam = styleParamMap[it] ?: continue
            //                    ?: return@forEach    //过滤没有配置
            if (styleParam.styleType != MessageUtil.KEY_IMG) {
                continue
//                return@forEach
            }

            if (styleParam.preffix == MessageUtil.PREFIX_ROYAL_LEVEL) {
                val paramValue: String? = textParams[it]
                if (StringHelper.isEmpty(paramValue) || "0" == paramValue) {//0或者没有填写,都是错误的,需要过滤掉
                    textTpl = textTpl.replace(it, "")
                    styleParamMap.remove(it)
                    textParams.remove(it)
                    iterator.remove()
                    continue
                }
            }

            //${goodsPicId}
            if (styleParam.source == MessageUtil.KEY_REMOTE && it.indexOf("giftPic") >= 0) {//远程图片,并且 是 礼物图片
                textTpl = textTpl.replace(it, "$oneEmptyChar$it")
            }

            //除了ｅｍｏｊｉ之外所有的图片左右加空格
            if (styleParam.styleType == MessageUtil.KEY_IMG && styleParam.preffix != MessageUtil.PREFIX_EMOJI) {
                textTpl = textTpl.replace(it, "$it$oneEmptyChar")// "　"
            }
        }

        realTxt = textTpl
        paramIndexInRealText.clear()


        paramKeySorted.forEach {//按照textTpl里变量出现的顺序来替换和记录下标
                paramKey ->
            run {
                var position = realTxt.indexOf(paramKey)
                if (position == -1) {//chuxian出现这种情况是不应该,上传个错误日志
                    val extraDescription = "解析消息的时候,参数可能有问题 , textTpl = ＜＜$textTpl＞＞ ； realText = ＜＜$realTxt＞＞ " +
                            ", 当前出错的 paramKey => $paramKey , 是否是图片类型 ${styleParamMap[paramKey]?.styleType} " +
                            ",  paramKeySorted ==>> $paramKeySorted , styleParamMap ==> $styleParamMap " +
                            ",  paramIndexInRealText ==>> $paramIndexInRealText " +
                            ", textParams ==> ${this.textParams}"
                    reportCrash(extraDescription)
                    return@forEach
                }
                var list: MutableList<Int> = paramIndexInRealText[paramKey] ?: mutableListOf()
                list.add(position)
                var paramValue: String = textParams[paramKey].toString() // 16-11-6 这里先假设所有的var都有对应的值,不做空值判断了
//                //这里对姓名进行截取
//                if(needCutName&&"\${userObj.nickname}"==(paramKey)){
//                    if(paramValue.length>5)  {
//                        paramValue=paramValue.substring(0,5)+"..."
//                    }
//                }
                var replace = paramValue
                val styleParam = styleParamMap[paramKey]
                if (MessageUtil.KEY_IMG == styleParam?.styleType) {
                    // 这里有一个特例 ,原定计划是所有的图片(其实是指各个等级的图标,不能为0,但是emoji是可以有 0 开始的...所以需要特殊处理)
                    if ((styleParam.source != MessageUtil.KEY_LOCAL || styleParam.preffix == MessageUtil.PREFIX_EMOJI) || (StringHelper.isNotEmpty(
                            paramValue
                        ) && paramValue!!.toInt() > 0)
                    ) {
                        replace = imagePlaceHolderChar
                    } else {
                        replace = ""
                    }
                }

                realTxt = realTxt.replaceFirst(paramKey, replace)//重新设置目标字符串

                paramIndexInRealText[paramKey] = list
            }
        }

        finalProcessed = true
        return this
    }

    /**
     * 私聊，没有昵称的情况下，特殊处理
     */
    @Synchronized
    fun specialExtra(): TplBean {
        realTxt = textTpl
        styleParamMap[MessageUtil.KEY_ALL] = StyleParam(
            styleType = MessageUtil.KEY_BASIC, color = userInfo?.textColor
                ?: "#FFFFFF"
        )
        return this

    }

}

object MessageUtil {
    //同一个图片最多尝试下载的次数
    val IMAGE_TRY_ERROR_TIMES: Int = 3

    //加载出错的url,几次超过一定的次数,不再加载
    var errorImageUrlMap: MutableMap<String, Int> = mutableMapOf()

    //已经加载过的图片的
//    var charRecordImageDownloaded:MutableMap<String,Boolean> = mutableMapOf()
    //加载之后未能加载到图片的地址....
    var filteredImageUrls: MutableList<String> = mutableListOf()
    val pattern = Pattern.compile("\\[[a-zA-Z0-9\u4E00-\u9FA5]*\\]")
    fun decodeMessageContent(data: String): TplBean = JsonUtil.deserializeAsObject<TplBean>(data, TplBean::class.java)

    const val KEY_ALL = "ALL"
    const val KEY_BASIC = "basic"
    const val KEY_IMG = "img"

    //本地图片
    const val KEY_LOCAL = "local"

    //网络图片
    const val KEY_REMOTE = "remote"

    //表情
    const val PREFIX_EMOJI = "emoji"

    //用户等级
    const val PREFIX_USER_LEVEL = "user-level"

    //贵族等级
    const val PREFIX_ROYAL_LEVEL = "royal-level"

    //主播等级
    const val PREFIX_ANCHOR_LEVEL = "anchor-level"

    //聊天默认背景颜色
    const val MESSAGE_BG = "#59000000"

    //默认背景颜色圆角大小
    const val MESSAGE_BG_RADIUS = 5
}