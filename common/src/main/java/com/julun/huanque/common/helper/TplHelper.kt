package com.julun.huanque.common.helper

import com.julun.huanque.common.bean.StyleParam
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.julun.huanque.common.widgets.live.chatInput.EmojiUtil
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/28 16:28
 *
 *@Description: 专门处理富文本模板解析/组装相关工具类
 *
 */
object TplHelper {

    //加载之后未能加载到图片的地址....
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
    fun tplProcess(tplBean: TplBean): TplBean {
        tplBean.apply {

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
                        styleType = TplHelper.KEY_IMG,
                        preffix = TplHelper.PREFIX_EMOJI,
                        source = TplHelper.KEY_LOCAL
                    )
                )
                val indexOf = EmojiUtil.EmojiTextArray.indexOf(group)
                textTpl = textTpl.replace(group, replace)
                textParams.put(replace, "$indexOf")
            }

            //加上发言人信息
            val user = userInfo
            if (user != null) {
                val nicknameKey = "\${extra.user.nickName}"
                val royalIconKey = "\${extra.user.royalLevelIcon}"
                val levelIconKey = "\${extra.user.userLevelIcon}"
                val guardIconKey = "\${extra.user.guardIcon}"
                val anchorLevelKey = "\${extra.user.anchorLevelIcon}"

//            val roomManagerPicKey = "\${extra.user.roomManagerPic}"
//            val officalManagerPicKey = "\${extra.user.officalManagerPic}"

                val prepositionKey = "\${preposition}" //介词
                val toNicknameKey = "\${toNickName}" //目标人姓名
                val anchorLevel: Int = user.anchorLevel//主播等级
                //1.首先设置全局颜色
                styleParamMap[TplHelper.KEY_ALL] =
                    StyleParam(styleType = TplHelper.KEY_BASIC, color = "#FFFFFF", fontWeight = DraweeSpanTextView.BOLD)
                //2.主播的发言处理
                if (anchorLevel > 0 && user.targetUserObj?.nickname !== null && "${user.senderId}" == "${SessionUtils.getUserId()}") {

                    textTpl = "$nicknameKey $prepositionKey $toNicknameKey $textTpl"
                    textParams.put(prepositionKey, "对")
                    styleParamMap.put(prepositionKey, StyleParam(el = prepositionKey, color = "#000000"))
                    textParams.put(toNicknameKey, "${user.targetUserObj?.nickname}")
                    styleParamMap.put(toNicknameKey, StyleParam(el = toNicknameKey, color = "#FFD630"))
                } else {
                    textTpl = "$nicknameKey $textTpl"
                }
                //3.用户昵称处理
                val tNickName: String = if (user.nickname.isNotBlank()) {
                    if (privateMessage) {
                        //私聊消息，昵称之后添加'：'，对添加的‘：’也渲染
                        "${user.nickname}："
                    } else {
                        user.nickname
                    }
                } else {
                    user.nickname
                }


                textParams.put(nicknameKey, tNickName)
                val nick = if (StringHelper.isEmpty(user.nickColor)) null else user.nickColor
                styleParamMap[nicknameKey] = StyleParam(el = nicknameKey, color = nick)
                if (user.lightColor.isNotEmpty()) {
                    styleParamMap[nicknameKey] = StyleParam(el = nicknameKey, lightColor = user.lightColor)
                }

                //4.添加勋章
                val goodsList = user.badgesPic
                goodsList.reversed().forEachIndexed { i, s ->
                    val goodsIconKey = "\${extra.user.goodsIconKey$i}"
                    textTpl = "$goodsIconKey$textTpl"
                    textParams.put(goodsIconKey, "$s")
                    styleParamMap.put(
                        goodsIconKey,
                        StyleParam(
                            el = guardIconKey,
                            styleType = TplHelper.KEY_IMG,
                            source = TplHelper.KEY_REMOTE,
                            preffix = TplHelper.PREFIX_USER_LEVEL
                        )
                    )
                }
//            //5.房管图标 官方图标
//            if (it.roomManagerPic.isNotBlank()) {
//                textTpl = "$roomManagerPicKey$textTpl"
//                textParams.put(roomManagerPicKey, it.roomManagerPic)
//                styleParamMap.put(roomManagerPicKey, StyleParam(el = roomManagerPicKey, styleType = MessageHelper.KEY_IMG, source = MessageHelper.KEY_REMOTE))
//            }
//            if (it.officalManagerPic.isNotBlank()) {
//                textTpl = "$officalManagerPicKey$textTpl"
//                textParams.put(officalManagerPicKey, it.officalManagerPic)
//                styleParamMap.put(officalManagerPicKey, StyleParam(el = officalManagerPicKey, styleType = MessageHelper.KEY_IMG, source = MessageHelper.KEY_REMOTE))
//            }
                //6.主播等级处理  普通用户等级和贵族处理
                if (anchorLevel >= 0) {//主播
                    textTpl = "$anchorLevelKey$textTpl"
                    textParams.put(anchorLevelKey, "$anchorLevel")
                    styleParamMap.put(
                        anchorLevelKey,
                        StyleParam(
                            el = anchorLevelKey,
                            styleType = TplHelper.KEY_IMG,
                            source = TplHelper.KEY_LOCAL,
                            preffix = TplHelper.PREFIX_ANCHOR_LEVEL
                        )
                    )

                } else {//普通用户
//                val guard = it.roomGuardPic
//                if (StringHelper.isNotEmpty(guard)) {
//                    textTpl = "$guardIconKey$textTpl"
//                    textParams.put(guardIconKey, "$guard")
//                    styleParamMap.put(guardIconKey, StyleParam(el = guardIconKey, styleType = MessageHelper.KEY_IMG, source = MessageHelper.KEY_REMOTE, preffix = MessageHelper.PREFFIX_USER_LEVEL))
//                }
                    //贵族等级图片改成远程
                    val royalPic = user.royalPic
                    if (royalPic.isNotEmpty()) {
                        textTpl = "$royalIconKey$textTpl"
                        textParams.put(royalIconKey, royalPic)
                        styleParamMap.put(
                            royalIconKey,
                            StyleParam(
                                el = royalIconKey,
                                styleType = TplHelper.KEY_IMG,
                                source = TplHelper.KEY_REMOTE,
                                preffix = TplHelper.PREFIX_ROYAL_LEVEL
                            )
                        )
                    }
                    if (user.userLevel > 0) {
                        textTpl = "$levelIconKey$textTpl"
                        textParams.put(levelIconKey, "${user.userLevel}")
                        styleParamMap.put(
                            levelIconKey,
                            StyleParam(
                                el = levelIconKey,
                                styleType = TplHelper.KEY_IMG,
                                source = TplHelper.KEY_LOCAL,
                                preffix = TplHelper.PREFIX_USER_LEVEL
                            )
                        )
                    }


                }

            }
//        //处理背景颜色圆角 如果没有 本地设置默认颜色 圆角
//        if(useBg){
//            val allparams: StyleParam? = styleParamMap[MessageHelper.KEY_ALL]
//            if (allparams != null) {
//                if (allparams.bgColor.isBlank()) {
//                    allparams.bgColor = MessageHelper.MESSAGE_BG
//                }
//                if (allparams.radius == -1) {
//                    allparams.radius = MessageHelper.MESSAGE_BG_RADIUS
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
                if (styleParam.styleType != TplHelper.KEY_IMG) {
                    continue
//                return@forEach
                }

                if (styleParam.preffix == TplHelper.PREFIX_USER_LEVEL) {
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
                if (styleParam.source == TplHelper.KEY_REMOTE && it.indexOf("giftPic") >= 0) {//远程图片,并且 是 礼物图片
                    textTpl = textTpl.replace(it, "$oneEmptyChar$it")
                }

                //除了ｅｍｏｊｉ之外所有的图片左右加空格
                if (styleParam.styleType == TplHelper.KEY_IMG && styleParam.preffix != TplHelper.PREFIX_EMOJI) {
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
                    if (TplHelper.KEY_IMG == styleParam?.styleType) {
                        // 这里有一个特例 ,原定计划是所有的图片(其实是指各个等级的图标,不能为0,但是emoji是可以有 0 开始的...所以需要特殊处理)
                        if ((styleParam.source != TplHelper.KEY_LOCAL || styleParam.preffix == TplHelper.PREFIX_EMOJI) || (StringHelper.isNotEmpty(
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
        }

        return tplBean
    }

}