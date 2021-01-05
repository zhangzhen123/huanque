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
class RecomListForm(var offset: Int? = null, var realOffset: Int? = null)

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


class PostDetailForm(var postId: Long)


class HomePostForm(var offset: Int, var postType: String?)

class PostListsForm(var offset: Int, var userId: Long?)

class PostForm(var postId: Long)

class GroupPostForm(var groupId: Long? = null, var orderType: String? = null, var offset: Int? = null)

class NearbyForm(
    var offset: Int,
    var lat: Double,
    var lng: Double,
    var province: String = "",
    var city: String = "",
    var districe: String = ""
)

class LikeForm(
    var offset: Int,
    var tagId: Int? = null
)

class TagForm(
    var tagId: Int
)

class TagListForm(
    var tagIds: String
)

class TagDetailForm(
    var tagId: Int,
    var friendId: Long? = null,
    var offset: Int = 0
)

class TagUserForm(
    var tagId: Int,
    var friendId: Long
)


class SaveSearchConfigForm(
    var sexType: String,
    var minAge: Int, var maxAge: Int,
    var distance: Long,
    var wishes: String,
    var tagIds: String
)



