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
    const val CORE = "core"

    //app使用
    private const val APP = "app"

    //common
    private const val COMMON = "common"

    //声网播放器使用
    const val AGORA = "AGORA"

    /*--GROUP相关结束--*/

    /*--PATH相关开始--*/
    private const val PREFIX_ACTIVITY = "/activity"
    private const val PREFIX_FRAGMENT = "/fragment"
    private const val SERVICE = "/service"

    /*LMAPP模块*/

    const val MAIN_ACTIVITY = "/$APP$PREFIX_ACTIVITY/MAIN_ACTIVITY"

    const val TEST_ACTIVITY = "/$APP$PREFIX_ACTIVITY/TEST_ACTIVITY"

    const val APP_COMMON_SERVICE = "/$APP$SERVICE/AppCommonService"
    const val LOGIN_ACTIVITY = "/$APP$PREFIX_ACTIVITY/LoginActivity"

    //语音页面
    const val VOICE_CHAT_ACTIVITY = "/$AGORA$PREFIX_ACTIVITY/VoiceChatActivity"
}