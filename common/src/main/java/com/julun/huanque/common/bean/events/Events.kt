package com.julun.huanque.common.bean.events

import java.io.Serializable

/**
 * 接收到私聊消息使用
 */
class EventMessageBean(var tardetId: String = "") : Serializable

/**
 * 信息填写完成事件
 * @param sextype 性别
 * @param nickname 昵称
 * @param birthday 生日
 * @param headerPic 头像
 */
class ImformationCompleteBean(var sextype: String = "", var nickname: String = "", var birthday: String = "", var headerPic: String = "")