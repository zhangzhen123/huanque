package com.julun.huanque.common.arouter.interceptor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.BaseDialogBean
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
import com.julun.huanque.common.net.RequestCaller
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.SocialService
import com.julun.huanque.common.suger.handleResponse
import com.julun.huanque.common.suger.whatEver
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.utils.svga.SVGAHelper.logger
import io.rong.imlib.model.Conversation
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

/**
 *@创建者   dong
 *@创建时间 2020/7/22 20:08
 *@描述 跳转语音通话页面的拦截器
 */
@Interceptor(priority = 1, name = "VoiceChat")
class VoiceChatInterceptor : IInterceptor, RequestCaller {
    private var mContext: Context? = null
    private val socialService: SocialService by lazy { Requests.create(SocialService::class.java) }

    //传递的用户ID
    private var mUserId = 0L

    //主叫还是被叫
    private var mType = ""

    private var mPostcard: Postcard? = null
    private var mCallback: InterceptorCallback? = null

    override fun process(postcard: Postcard?, callback: InterceptorCallback?) {
        if ((postcard?.path ?: "") == ARouterConstant.VOICE_CHAT_ACTIVITY) {
            //关闭悬浮窗
            EventBus.getDefault().post(HideFloatingEvent())
            mPostcard = postcard
            mCallback = callback
            //跳转语音会话页面
            postcard?.extras?.let { bundle ->
                mUserId = bundle.getLong(ParamConstant.UserId)
                mType = bundle.getString(ParamConstant.TYPE) ?: ""
                //耳机是否插入
                bundle.putBoolean(ParamConstant.Earphone, GlobalUtils.getEarphoneLinkStatus())
            }


            if (mType == ConmmunicationUserType.CALLING) {
                //主叫
                //判断权限是否存在
                mContext?.let { con ->
                    if (ActivityCompat.checkSelfPermission(con, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        //有对应权限
                        createVoiceChat()
                    } else {
                        //没有权限
                        val message = "无法获取到录音权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }
            } else if (mType == ConmmunicationUserType.CALLED) {
                //被叫直接跳转
                callback?.onContinue(postcard)
            }
        } else {
            callback?.onContinue(postcard)
        }
    }

    override fun init(context: Context?) {
        mContext = context
    }

    /**
     * 创建语音会话弹窗
     */
    private fun createVoiceChat(confirm: String? = null) {

        socialService.createCommunication(CreateCommunicationForm(mUserId, confirm)).handleResponse(makeSubscriber<NetcallBean> {
            //设置创建数据
            mPostcard?.extras?.putSerializable(ParamConstant.NetCallBean, it)
            //继续跳转
            mCallback?.onContinue(mPostcard)

        }.ifError {
            if (it is ResponseError) {
                when (it.busiCode) {
                    1001 -> {
                        //鹊币不足
                        balanceNotEnoughDialog()
                    }
                    1401 -> {
                        //特权未解锁
                        ToastUtils.show("亲密等级达到lv3才能语音通话哦")
//                        showIntimateDialog()
                    }
                    1402 -> {
                        //显示价格弹窗
                        showFeeDialog(it.busiMessage)
                    }
                    1403 -> {
                        //对方正在通话中
                        insertBusyMessage()
                    }
                    else -> {
                    }
                }
            }
        }.withSpecifiedCodes(1402, 1001, 1401, 1403))
    }

    /**
     * 标记收费标识已经显示过
     */
    private fun markFeeRemind() {
        socialService.markFeeRemind().whatEver()
    }

    /**
     * 显示价格弹窗
     */
    private fun showFeeDialog(price: String) {
        markFeeRemind()
        CommonInit.getInstance().getCurrentActivity()?.let { act ->
            MyAlertDialog(act).showAlertWithOKAndCancel(
                "语音通话${price}鹊币/分钟",
                MyAlertDialog.MyDialogCallback(onRight = {
                    SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_FEE_DIALOG_SHOW, true)
                    createVoiceChat(BusiConstant.True)
                }, onCancel = {
                    SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_FEE_DIALOG_SHOW, true)
                }), "语音通话费用", "发起通话"
            )
        }
    }

    /**
     * 余额不足,显示余额不足弹窗
     */
    private fun balanceNotEnoughDialog() {
        val act = CommonInit.getInstance().getCurrentActivity() as? AppCompatActivity
        if (act != null) {
//            ToastUtils.show("余额不足")
            val dialogFragment = ARouter.getInstance().build(ARouterConstant.BalanceNotEnoughFragment).navigation() as? BaseDialogFragment
            dialogFragment?.show(act.supportFragmentManager, "BalanceNotEnoughFragment")
        }
    }

    /**
     * 模拟插入对方忙消息
     */
    private fun insertBusyMessage() {
        val sId = "${SessionUtils.getUserId()}"
        val chatExtra = RoomUserChatExtra()
        chatExtra.apply {
            headPic = SessionUtils.getHeaderPic()
            senderId = SessionUtils.getUserId()
            nickname = SessionUtils.getNickName()
            sex = SessionUtils.getSex()
            userAbcd = AppHelper.getMD5(sId)
        }
        RongCloudManager.sendSimulateMessage(
            "$mUserId",
            "${SessionUtils.getUserId()}",
            chatExtra,
            Conversation.ConversationType.PRIVATE,
            MessageCustomBeanType.Voice_Conmmunication_Simulate,
            VoiceConmmunicationSimulate(type = VoiceResultType.RECEIVE_BUSY)
        )
    }

    /**
     * 显示亲密度弹窗
     */
    private fun showIntimateDialog() {
        val act = (CommonInit.getInstance().getCurrentActivity() as? AppCompatActivity) ?: return
        IntimateUtil.intimatePrivilegeList.forEach {
            if (it.key == "YYTH") {
                val bundle = Bundle()
                bundle.putSerializable(ParamConstant.IntimatePrivilege, it)
                val dialog =
                    ARouter.getInstance().build(ARouterConstant.SINGLE_INTIMATE_PRIVILEGE_FRAGMENT).with(bundle).navigation() as? BaseDialogFragment
                dialog?.show(act.supportFragmentManager, "BaseDialogFragment")
                return
            }
        }
    }

    override fun getRequestCallerId() = StringHelper.uuid()
}