package com.julun.huanque.common.constant

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/22 21:08
 *
 *@Description: IntentParamKey
 *
 */
enum class IntentParamKey {
    EMPTY, COUNT_NUMBER, CODE, FORM, EXTRA_FLAG_GO_HOME //特殊标记,返回首页
    ,
    STATUS_FLAG //状态标记
    ,
    HTTP_URL //超链接
    ,
    ALERT_MESSAGE //提示消息
    ,
    PERMISSION_NEEDED //需要请求的权限
    ,
    PARAM_RESTORE_STATE //
    ,
    RETURN_AFTER_JUMP //
    ,
    PROGRAM_ID, STREAM_ID, USER_ID, ANCHOR_ID, ANCHOR_INFO //主播的信息
    ,
    LIVE_INFO //直播的信息
    ,
    SHARE_OBJECT //分享对象
    ,
    PAY_RESULT, RUNWAY, USER_COUNT, IS_ANCHOR //是否是主播
    ,
    REPORT_LIST //举报列表
    ,
    PAGE_NAME //页面名称
    ,
    MANAGER_NAME //管理内容
    ,
    NICK_NAME //昵称
    ,
    MANAGER_LIST //管理列表
    ,
    ID, TYPE //类型
    ,
    IS_JUMP_USER_CENTER //跳转到我的界面
    ,
    POST_ID,//动态id
    CHAT_ROOM_ID //聊天室id
    ,
    WIDTH //宽度
    ,
    HEIGHT //高度
    ,
    GRAVITY //位置
    ,
    BG //背景
    ,
    IMAGE //图片
    ,
    STRING //字符串
    ,
    URL //url
    ,
    POSITION, TITLE, LIST, BEAN, TARGET_INDEX //跳转到首页的某个栏目
    ,
    RELOAD, OPEN_GIFT //打开礼物面板并附带giftId 默认0代表只打开 -1代表不生效
    ,
    OPEN_SHARE //打开分享
    ,
    SOURCE //来源
    ,
    OPERATE, COUPON_ID //优惠券id
    ,
    PAGE_TYPE //页面类型
    ,
    OPEN_OPERATE //打开操作
    ,
    SYS_MSG_ID//系统消息id
    ,
    TAB_TYPE//直播tab类型
}