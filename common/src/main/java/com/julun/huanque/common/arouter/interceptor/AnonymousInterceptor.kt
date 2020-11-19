package com.julun.huanque.common.arouter.interceptor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.NetcallBean
import com.julun.huanque.common.bean.beans.RoomUserChatExtra
import com.julun.huanque.common.bean.events.HideFloatingEvent
import com.julun.huanque.common.bean.forms.CreateCommunicationForm
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.VoiceFloatingManager
import com.julun.huanque.common.net.RequestCaller
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.handleResponse
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.whatEver
import com.julun.huanque.common.utils.*
import io.rong.imlib.model.Conversation
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/7/22 20:08
 *@描述 跳转语音通话页面的拦截器
 */
@Interceptor(priority = 2, name = "Anonymous")
class AnonymousInterceptor : IInterceptor {
    override fun process(postcard: Postcard?, callback: InterceptorCallback?) {
        if ((postcard?.path ?: "") == ARouterConstant.ANONYMOUS_VOICE_ACTIVITY) {
            if (SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_ON_LINE, false)) {
                ToastUtils.show("正在语音通话，请稍后再试")
                return
            }
        } else {
            callback?.onContinue(postcard)
        }
    }

    override fun init(context: Context?) {
    }

}