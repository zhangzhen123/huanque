package com.julun.huanque.common.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.beans.TplBeanExtraContext
import com.julun.huanque.common.helper.TplHelper
import com.julun.huanque.common.helper.StringHelper
import java.util.*

/**
 * Created by nirack on 16-11-4.
 */

//fontWeight:
class StyleParam(
    var styleType: String = "", var preffix: String = "", var color: String? = null,
    var bgColor: String = "", var radius: Int = -1, var el: String = "", var source: String = "",
    var fontWeight: String = "", var underLineColor: String? = null, var lightColor: String = ""
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
            bean = TplHelper.tplProcess(this)
        }
        return bean
    }


    /**
     * 私聊，没有昵称的情况下，特殊处理
     */
    @Synchronized
    fun specialExtra(): TplBean {
        realTxt = textTpl
        styleParamMap[TplHelper.KEY_ALL] = StyleParam(
            styleType = TplHelper.KEY_BASIC, color = userInfo?.textColor
                ?: "#FFFFFF"
        )
        return this

    }

}
