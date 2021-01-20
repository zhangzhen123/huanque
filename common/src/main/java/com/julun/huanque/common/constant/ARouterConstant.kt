package com.julun.huanque.common.constant


/**
 * 阿里路由路径常量配置类
 * 通常在路由表中找不到对应配置的类原因：
 * 1、路径不对，本来是在一个model中的类，配置了另一个model的名称，那么会在另一个model中配置这个路由，
 * 但是这个时候在这个model中找不到对应的类，就不会配置到路由表中，而且因为通过注释在编译时生成的类文件，名称是唯一的，在其中一个model
 * 中生成了对应的类后，其他model就不再生成了，就会出现明明路由中配置了对应的类，但是找不到的原因
 */
object ARouterConstant {
    /*--GROUP相关开始--*/
    //core使用
    private const val CORE = "core"

    //app使用
    private const val APP = "app"

    //common
    private const val COMMON = "common"

    //message使用
    private const val MESSAGE = "message"

    //PrivateConversationActivity
    //声网播放器使用
    private const val AGORA = "AGORA"

    //MSA
    private const val MSA = "msa_service"

    /*--GROUP相关结束--*/

    /*--PATH相关开始--*/
    private const val PREFIX_ACTIVITY = "/activity"
    private const val PREFIX_FRAGMENT = "/fragment"
    private const val SERVICE = "/service"

    /*LMAPP模块*/

    const val MAIN_ACTIVITY = "/$APP$PREFIX_ACTIVITY/MAIN_ACTIVITY"

    //直播设置页面
    const val PLAYER_SETTING_ACTIVITY = "/$APP$PREFIX_ACTIVITY/PlayerSettingActivity"

    //派单弹窗
    const val FATE_QUICK_MATCH_FRAGMENT = "/$APP$PREFIX_FRAGMENT/FateQuickMatchFragment"

    const val TEST_ACTIVITY = "/$APP$PREFIX_ACTIVITY/TestActivity"

    const val APP_COMMON_SERVICE = "/$APP$SERVICE/AppCommonService"
    const val LOGIN_ACTIVITY = "/$APP$PREFIX_ACTIVITY/LoginActivity"
    const val Welcome_Activity = "/$APP$PREFIX_ACTIVITY/WelcomeActivity"

    //环境切换
    const val ENVIRONMENT_CONFIGURATION_ACTIVITY = "/$APP$PREFIX_ACTIVITY/EnvironmentConfigurationActivity"

    const val PHONE_NUM_LOGIN_ACTIVITY = "/$APP$PREFIX_ACTIVITY/PhoneNumLoginActivity"

    //语音页面
    const val VOICE_CHAT_ACTIVITY = "/$AGORA$PREFIX_ACTIVITY/VoiceChatActivity"

    //匿名语音
    const val ANONYMOUS_VOICE_ACTIVITY = "/$AGORA$PREFIX_ACTIVITY/AnonymousVoiceActivity"

    //message模块
    const val PRIVATE_CONVERSATION_ACTIVITY = "/$MESSAGE$PREFIX_ACTIVITY/PrivateConversationActivity"
    const val PIC_CONTENT_ACTIVITY = "/$MESSAGE$PREFIX_ACTIVITY/PicContentActivity"
    const val PROP_FRAGMENT = "/$MESSAGE$PREFIX_FRAGMENT/PropFragment"

    //缘分页面
    const val YUAN_FEN_ACTIVITY = "/$MESSAGE$PREFIX_ACTIVITY/YuanFenActivity"

    //会话列表页面
    const val MessageFragment = "/$MESSAGE$PREFIX_FRAGMENT/MessageFragment"

    //联系人页面
    const val ContactsActivity = "/$MESSAGE$PREFIX_ACTIVITY/ContactsActivity"

    const val SysMsgActivity = "/$MESSAGE$PREFIX_ACTIVITY/SysMsgActivity"

    //搭讪常用语页面
    const val USE_FUL_WORD_ACTIVITY = "/$MESSAGE$PREFIX_ACTIVITY/UsefulWordActivity"

    //亲密特权弹窗
    const val SINGLE_INTIMATE_PRIVILEGE_FRAGMENT = "/$MESSAGE$PREFIX_FRAGMENT/SingleIntimateprivilegeFragment"

    //举报页面
    const val REPORT_ACTIVITY = "/$APP$PREFIX_ACTIVITY/ReportActivity"

    //充值页面
    const val RECHARGE_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/RechargeCenterActivity"

    //主页页面
    const val HOME_PAGE_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/HomePageActivity"
    //编辑个人信息
    const val EDIT_INFO_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/EditInfoActivity"

    //花魁榜
    const val PLUM_FLOWER_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/PlumFlowerActivity"

    //余额不足弹窗
    const val BalanceNotEnoughFragment = "/$CORE$PREFIX_FRAGMENT/BalanceNotEnoughFragment"

    //首充弹窗
    const val FIRST_RECHARGE_FRAGMENT = "/$CORE$PREFIX_FRAGMENT/FirstRechargeFragment"

    //乐园弹窗
    const val BIRD_DIALOG_FRAGMENT = "/$CORE$PREFIX_FRAGMENT/BirdDialogFragment"


    //语音签名界面
    const val VOICE_SIGN_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/VoiceSignActivity"

    //动态详情
    const val DYNAMIC_DETAIL_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/DynamicDetailActivity"

    //用户动态
    const val USER_DYNAMIC_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/UserDynamicActivity"

    //圈子动态
    const val CIRCLE_DYNAMIC_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/CircleDynamicActivity"


    //邀友分享
    const val INVITE_SHARE_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/InviteShareActivity"

    //提现页面
    const val WITHDRAW_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/WithdrawActivity"

    //直播间
    const val PLAYER_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/PlayerActivity"

    //乐园
    const val LEYUAN_BIRD_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/LeYuanBirdActivity"

    //发布动态
    const val PUBLISH_STATE_ACTIVITY = "/$CORE$PREFIX_ACTIVITY/PublishStateActivity"


    /** 实名认证 start **/
    private const val REAL_NAME = "realname"

    //实名认证首页
    const val REAL_NAME_MAIN_ACTIVITY = "/$REAL_NAME$PREFIX_ACTIVITY/RealNameActivity"

    //头像认证授权页
    const val REAL_HEAD_ACTIVITY = "/$REAL_NAME$PREFIX_ACTIVITY/RealHeadActivity"

    //实名认证服务
    const val REALNAME_SERVICE = "/$REAL_NAME$SERVICE/RealNameService"

    /** 实名认证 end **/

    //微信支付 微信授权 微博授权 微信分享 微博分享服务
    const val LOGIN_SHARE_SERVICE = "/$APP$SERVICE/LoginAndShareService"

    //H5页面
    const val WEB_ACTIVITY = "/$COMMON$PREFIX_ACTIVITY/WebActivity"

    /** MSA 模块 **/
    const val MSA_SERVICE = "/$MSA$SERVICE/MSAService"
}