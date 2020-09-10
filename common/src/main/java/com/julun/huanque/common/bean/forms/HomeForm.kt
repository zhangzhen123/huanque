package com.julun.huanque.common.bean.forms

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/3 16:35
 *
 *@Description: 主页相关的请求form
 *
 */
class RecomListForm(var offset: Int? = null)

class BuyBirdForm(var programId: Long? = null, var upgradeLevel: Int)

data class BirdCombineForm(
    var upgradeId1: Long? = null,
    var upgradeId2: Long? = null,
    var upgradePos1: Int? = null,
    var upgradePos2: Int? = null
)

class RecycleBirdForm(var programId: Long? = null, var upgradeId: Long)

class TaskBirdReceive(var taskCode: String)

class TaskBirdActiveReceive(var activeCode: String)

class BirdFunctionForm(var functionCode: String)
