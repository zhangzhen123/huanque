package com.julun.huanque.common.constant

object ChannelCodeKey {
    const val PID = "pid"
    const val UID = "uid"
}

object HomeTabType {
    const val Hot = "Hot"
    const val Dancer = "Dancer"
    const val Follow = "Follow"

    const val Nearby = "Nearby"//附近
    const val Favorite = "Favorite"//喜爱
}

object ProgramItemType {
    const val NORMAL = 1//普通样式
    const val BANNER = 2//广告轮播
}

/**
 *
 * 可选值：Live,直播模块，Social 社交模块（交友）
 */
object DefaultHomeTab {
    const val Live = "Live"//直播模块
    const val Social = "Social"//社交模块
}


object SquareTabType {

    const val FOLLOW = "Follow"
    const val RECOMMEND = "Recom"

    const val HOT = "Heat"
    const val NEW = "Time"
}

object ActivityRequestCode {
    const val SELECT_CIRCLE = 345
    const val MANAGER_TAG_RESULT_CODE = 346 //标签管理result_code
}

object PublicStateCode {
    const val CIRCLE_DATA = "Circle_Data"
}

object BottomActionCode {
    const val DELETE = "delete"
    const val REPORT = "report"
    const val SHARE = "share"
    const val BLACK = "black"
    const val CANCEL = "cancel"

    const val REPLACE = "Replace"
    const val ADD = "Add"
    const val REPLACE_HEAD = "Replace_head"
    const val AUTH_HEAD = "auth_head"
}

object StatisticCode {
    const val PubPost = "PubPost#"//发动态

    const val PostHome = "PostHome"//动态首页

    const val Follow = "Follow#"//关注


    const val Post = "Post"//浏览鹊巢页面（浏览动态）

    const val Home = "Home"//鹊巢首页点击发动态
    const val LiveRoom = "LiveRoom"//直播间
    const val MyPost = "MyPost"//我的动态页面点击发动态
    const val Group = "Group"//圈子详情页面点击发动态

    const val Link  = "Link "//联系人
    const val Other  = "Other "//其他页面

    const val EnterGroup  = "EnterGroup#"//进入圈子
    const val List  = "List "//





}


object MyTagType {
    const val AUTH = "Auth"
    const val LIKE = "Like"
}

object ManagerTagCode {
    const val TAG_LIST = "tag_list"
    const val TAG_INFO= "tag_info"
    const val MANAGER_PAGER_TYPE= "manager_pager_type"
}

/**
 *  审核状态 Wait 等待审核 Reject 被拒绝 Pass 审核通过
 */
object TagPicAuthStatus{
   const val Wait="Wait"
   const val Reject="Reject"
   const val Pass="Pass"
}

//User：用户卡片 Guide：引导卡片
object  CardType{
    const val USER = "User"
    const val GUIDE = "Guide"
}
object  GuideShowType{
    const val POPUP = "Popup"
    const val CARD = "Card"
}