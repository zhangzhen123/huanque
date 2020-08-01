package com.julun.huanque.common.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.PkMicService
import com.julun.huanque.common.suger.handleResponse
import com.julun.huanque.common.suger.whatEver
import com.julun.huanque.common.utils.ToastUtils


/**
 * 连麦viewmodel
 */
class ConnectMicroViewModel : BaseViewModel() {

    private val service: PkMicService by lazy { Requests.create(PkMicService::class.java) }

    /*连麦*/
    //创建连麦显示控制位
    val createConnectShow: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //创建Pk显示控制位
    val createPkShow: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //传递主播信息  接收过后记得设置为null，方便下一次接收
    val anchorData: MutableLiveData<AnchorBasicInfo> by lazy { MutableLiveData<AnchorBasicInfo>() }

    //确认连麦弹窗显示控制位
    val notarizeShowState: MutableLiveData<MicOperateBean> by lazy { MutableLiveData<MicOperateBean>() }

    //刷新确认连麦信息
    val notarizeShowData: MutableLiveData<MicOperateBean> by lazy { MutableLiveData<MicOperateBean>() }

    //创建连麦接口状态
    val createState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //确认接口状态
    val acceptState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //连麦信息状态
    val queryMicData: MutableLiveData<QueryMicInfo> by lazy { MutableLiveData<QueryMicInfo>() }

    //是否处于PK中
    val inPk: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //是否处于连麦中
    val inMicro: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //调用后台接口标识
    val remoteState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //创建按钮状态
    val createFinalState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //接收连麦状态
    val acceptFinalState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    /*PK*/
    //Pk相关数据
    val pkData: MutableLiveData<CreatePKInfoBean> by lazy { MutableLiveData<CreatePKInfoBean>() }

    //创建PK接口状态
    val createPkState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //确认PK弹窗显示控制位
    val notarizePKShowState: MutableLiveData<PKCreateEvent> by lazy { MutableLiveData<PKCreateEvent>() }

    //刷新确认创建PK信息
    val notarizePKShowData: MutableLiveData<PKCreateEvent> by lazy { MutableLiveData<PKCreateEvent>() }

    //PK接受接口状态
    val acceptPKState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //接收连麦状态
    val acceptPKFinalState: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    //保存推流Mode 默认是单流  4
    var mPublishMode = 4
    var microRule = ""

    /**
     * 查询主播连麦信息
     */
    fun queryMicInfo(programId: Long) {

        service.queryMicInfo(ProgramIdForm(programId))
            .handleResponse(makeSubscriber {
                queryMicData.value = it
            })
    }

    /**
     * 创建连麦
     */
    fun micCreate(num: Int, programIds: String) {
        service.micCreate(RoomMicCreateForm(num, programIds))
            .handleResponse(makeSubscriber<VoidResult> {
                createState.value = true
            }.ifError {
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                }
            }.withSpecifiedCodes(ErrorCodes.CREATE_MICRO_FAIL, ErrorCodes.PK_ERROR, ErrorCodes.PK_ERROR2)
                .withFinalCall { createFinalState.postValue(true) })
    }

    /**
     * 确认连麦
     */
    fun micAccept(midId: Long) {
        service.micAccept(RoomMicIdForm(midId))
            .handleResponse(makeSubscriber<VoidResult> {
                acceptState.value = true
            }.ifError {
            }.withFinalCall { acceptFinalState.value = true })
    }

    /**
     * 放弃连麦
     */
    fun micReject(micId: Long) {
        service.micReject(RoomMicIdForm(micId))
            .whatEver()

    }

    /**
     * 挂断连麦
     */
    fun micFinish(programId: Long) {
        service.micFinish(ProgramIdForm(programId))
            .handleResponse(makeSubscriber {
            })
    }

    /**
     * 连麦设置接口
     */
    fun micSettings() {
        service.micSettings(SessionForm())
            .handleResponse(makeSubscriber {
                microRule = it.micRuleUrl
            })
    }

    /*PK相关*/
    /**
     * 查询pk相关的信息
     */
    fun getMyPkOrCreate(id: ProgramIdForm) {
        service
            .getMyPkOrCreate(id)
            .handleResponse(makeSubscriber<CreatePKInfoBean> {
                pkData.value = it
            }.ifError {
                it.printStackTrace()
            })
    }

    /**
     * 创建PK
     */
    fun createPk(form: CreatePkForm) {
        service
            .create(form)
            .handleResponse(makeSubscriber<VoidResult> {
                createPkState.value = true
            }.ifError {
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                }
                it.printStackTrace()
            }.withFinalCall { createFinalState.postValue(true) }
                .withSpecifiedCodes(ErrorCodes.PK_ERROR, ErrorCodes.PK_ERROR2))
    }

    /**
     * 接受PK
     */
    fun acceptPk(pkId: Int) {
        service
            .accept(AcceptPkForm(pkId = pkId))
            .handleResponse(makeSubscriber<VoidResult> {
                acceptPKState.value = true
            }.ifError {
                if (it is ResponseError) {
                    ToastUtils.show(it.busiMessage)
                }
                it.printStackTrace()
            }.withFinalCall { acceptPKFinalState.value = true })
    }

    /**
     * 拒绝PK
     */
    fun rejectPk(pkId: Int) {
        service
            .reject(RejectPkForm(pkId))
            .whatEver()
    }

    /**
     * 倒计时结束之后，取消PK
     */
    fun finishPk() {
        service
            .finishPk().whatEver()
    }
//    fun cancelPk(form: RejectPkForm) {
//       service
//                .cancel(form)
//                .bindUntilEvent(view, FragmentEvent.DESTROY_VIEW)
//                .handleResponse(makeSubscriber<VoidResult> {
//                    view.dismiss()
//                }.ifError {
//                    it.printStackTrace()
//                })
//    }
}