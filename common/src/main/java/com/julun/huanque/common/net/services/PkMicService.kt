package com.julun.huanque.common.net.services

import com.julun.huanque.common.basic.Root
import com.julun.huanque.common.basic.VoidForm
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.*
import com.julun.huanque.common.bean.beans.ProgramLiveIndexInfo
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * pk连麦相关
 */
interface PkMicService {

    /**
     * 查询当前直播间pk信息
     */
    @POST("live/room/pk/info")
    fun roomPkInfo(@Body info: PKInfoForm): Observable<Root<PKInfoBean>>

    /**
     * 查询当前创建pk信息
     */
    @POST("live/room/pk/my")
    fun getMyPkOrCreate(@Body info: ProgramIdForm): Observable<Root<CreatePKInfoBean>>

    /**
     * 创建pk
     */
    @POST("live/room/pk/create")
    fun create(@Body form: CreatePkForm): Observable<Root<VoidResult>>

    /**
     * 接受pk
     */
    @POST("live/room/pk/accept")
    fun accept(@Body info: AcceptPkForm): Observable<Root<VoidResult>>

    /**
     * 拒绝pk
     */
    @POST("live/room/pk/reject")
    fun reject(@Body info: RejectPkForm): Observable<Root<VoidResult>>

    /**
     * 取消pk
     */
    @POST("live/room/pk/cancel")
    fun cancel(@Body info: RejectPkForm): Observable<Root<VoidResult>>

    /**
     * 手动结束pk
     */
    @POST("live/room/pk/finishPk")
    fun finishPk(@Body form: VoidForm = VoidForm()): Observable<Root<VoidResult>>

    /**
     * PK使用道具
     */
    @POST("live/room/pk/useProp")
    fun useProp(@Body form: ProgramIdForm): Observable<Root<VoidResult>>

    /**
     * 查询当前的pk战绩
     */
    @POST("live/room/pk/history")
    fun history(@Body form: PKHistoryForm): Observable<Root<PKHistory>>


    /**
     * 领取免费礼物
     */
    @POST("pk/obtainFreeGift")
    fun obtainFreeGift(@Body form: SessionForm): Observable<Root<ObtainFreeGiftDto>>


    /**
     * 创建连麦
     */
    @POST("live/room/mic/create")
    fun micCreate(@Body form: RoomMicCreateForm): Observable<Root<VoidResult>>

    /**
     * 拒绝连麦
     */
    @POST("live/room/mic/reject")
    fun micReject(@Body form: RoomMicIdForm): Observable<Root<VoidResult>>

    /**
     * 接受连麦
     */
    @POST("live/room/mic/accept")
    fun micAccept(@Body form: RoomMicIdForm): Observable<Root<VoidResult>>

    /**
     * 结束连麦
     */
    @POST("live/room/mic/finish")
    fun micFinish(@Body form: ProgramIdForm): Observable<Root<VoidResult>>

    /**
     * 获取最近连麦主播列表
     */
    @POST("live/room/mic/queryRecent")
    fun queryRecent(@Body form: SessionForm): Observable<Root<ArrayList<ProgramLiveIndexInfo>>>

    /**
     * 获取主播连麦信息
     */
    @POST("live/room/mic/queryMicInfo")
    fun queryMicInfo(@Body form: ProgramIdForm): Observable<Root<QueryMicInfo>>

    /**
     * 连麦设置接口
     */
    @POST("live/room/mic/settings")
    fun micSettings(@Body session: SessionForm): Observable<Root<MicroSettingInfo>>

    /**
     * 查询PK竞猜信息
     */
    @POST("live/pk/guess/queryGuessInfo")
    fun queryGuessInfo(@Body form: PkGuessQueryForm): Observable<Root<GuessInfoResult>>

    /**
     * pk竞猜
     */
    @POST("live/pk/guess/saveGuess")
    fun saveGuess(@Body form: PkGuessForm): Observable<Root<VoidResult>>


    /**
     * PK道具数据
     */
    @POST("live/room/consume/pkPropInfo")
    fun pkPropInfo(@Body form: ProgramIdForm): Observable<Root<PKGiftBalanceBean>>












}