package com.julun.huanque.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.julun.huanque.common.bean.events.PayResultEvent
import com.julun.huanque.common.bean.events.WeiXinCodeEvent
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.PayResult
import com.julun.huanque.common.constant.PayType
import com.julun.huanque.common.database.table.Session
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.support.LoginManager
import com.julun.huanque.support.WXApiManager
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus

/**
 * 微信相关数据处理
 * @author WanZhiYuan
 * @since 4.27
 * @date 2020/03/05
 */
class WechatViewModel : BaseViewModel() {

    val finish: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    val showDialog: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    /**
     * 支付回调
     */
    fun onPayEntryResp(resp: BaseResp?) {
        logger("充值回调type=[" + resp?.type + "], errCode=[" + resp?.errCode + "]")
//        SessionUtils.setWeiXinUse(false)
        when (resp?.type) {
            ConstantsAPI.COMMAND_PAY_BY_WX -> {
                val payResult = when (resp.errCode) {
                    BaseResp.ErrCode.ERR_OK -> {
                        PayResult.PAY_SUCCESS
                    }
                    BaseResp.ErrCode.ERR_USER_CANCEL -> PayResult.PAY_CANCEL
                    BaseResp.ErrCode.ERR_COMM -> PayResult.PAY_FAIL
                    else -> PayResult.PAY_FAIL
                }
                EventBus.getDefault().post(PayResultEvent(payResult, PayType.WXPayApp))
                finish.value = true
            }
        }
    }

    /**
     * 登录或分享回调
     */
    fun onEntryResp(baseResp: BaseResp?) {
        logger("微信onResp, errCode = ${baseResp?.errCode} type = ${baseResp?.type}")
//        SessionUtils.setWeiXinUse(false)
        when (baseResp?.type) {
            ConstantsAPI.COMMAND_SENDAUTH -> {
                //登录
                processLoginResult(baseResp?.errCode, baseResp?.errStr, (baseResp as? SendAuth.Resp)?.code)
            }
            ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX -> {
                //分享
                processShareResult(baseResp?.errCode, baseResp?.errStr)
            }
            else -> {
                logger("微信其他功能 -> type = ${baseResp?.type}")
                finish.value = true
            }
        }
    }

    private fun processLoginResult(errCode: Int? = null, errStr: String? = null, code: String? = null) {
        when (errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                logger("请求微信登录成功返回，code = $code")
                if(code!=null){
                    EventBus.getDefault().post(WeiXinCodeEvent(code))
                }
                finish.value = true
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                logger("请求微信登录取消")
                finish.value = true
            }
            else -> {
                logger("请求微信登录其他情况, errStr = $errStr")
                finish.value = true
                showDialog.value = errStr
            }
        }
    }

//    private fun goToShowMsg(showReq: ShowMessageFromWX.Req) {
//        val wxMsg = showReq.message
//        val obj = wxMsg.mediaObject as WXAppExtendObject
//        val msg = StringBuffer() // ��֯һ������ʾ����Ϣ����
//        msg.append("description: ")
//        msg.append(wxMsg.description)
//        msg.append("\n")
//        msg.append("extInfo: ")
//        msg.append(obj.extInfo)
//        msg.append("\n")
//        msg.append("filePath: ")
//        msg.append(obj.filePath)
//        val intent = Intent(this, ShowFromWXActivity::class.java)
//        intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title)
//        intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString())
//        intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData)
//        startActivity(intent)
//        finish()
//    }

    private fun processShareResult(errCode: Int, errStr: String? = null) {
        when (errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                if (WXApiManager.getShareObject().shareWay == "Other") {
                    //分享对象有异常，shareway为other
                    finish.value = true
                    return
                }
                logger("微信分享成功")
                ToastUtils.show("微信分享成功")
                finish.value = true
//                if (WXApiManager.getShareObject().isFromPage == PageTypes.Dynamic || WXApiManager.getShareObject().isFromPage == PageTypes.DynamicVideo) {
//                    Requests.create(AppService::class.java).shareToDynamic(ShareDynamicFrom(WXApiManager.getShareObject())).handleResponse(makeSubscriber<VoidResult> {
//                        logger("动态页分享保存记录成功")
//                        EventBus.getDefault().post(WXApiManager.getShareObject())
//                        finish.value = true
//                    }.ifError {
//                        if (it is ResponseError) {
//                            logger("动态页分享保存记录失败 , ${it.busiMessage}")
//                        } else {
//                            logger("动态页分享保存记录失败 , ${it.message}")
//                        }
//                        finish.value = true
//                    })
//                } else {
//                    Requests.create(AppService::class.java).shareToWorld(ShareForm(WXApiManager.getShareObject())).handleResponse(makeSubscriber<ShareBean> {
//                        logger("分享保存记录成功")
//                        EventBus.getDefault().post(it)
//                        finish.value = true
//                    }.ifError {
//                        if (it is ResponseError) {
//                            logger("分享保存记录失败 , ${it.busiMessage}")
//                        } else {
//                            logger("分享保存记录失败 , ${it.message}")
//                        }
//                        finish.value = true
//                    })
//                }
//                huanqueService.getService(IStatistics::class.java)?.onShare(WXApiManager.getShareObject().source
//                        ?: "",
//                        WXApiManager.getShareObject().programId ?: "",
//                        WXApiManager.getShareObject().platForm ?: "")
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                logger("用户取消分享")
                ToastUtils.show("您取消了微信分享")
                finish.value = true
            }
            else -> {
                logger("微信分享失败，原因：$errStr")
                ToastUtils.show("微信分享失败，原因：$errStr")
                finish.value = true
            }
        }
    }

}