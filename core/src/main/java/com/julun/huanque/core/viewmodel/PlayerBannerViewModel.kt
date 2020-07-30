package com.julun.huanque.core.viewmodel

import androidx.lifecycle.MutableLiveData
import com.julun.huanque.common.bean.beans.BannerStatusBean
import com.julun.huanque.common.bean.beans.PKInfoBean
import com.julun.huanque.common.bean.beans.RoomBanner
import com.julun.huanque.common.bean.beans.SingleBannerCheckResult
import com.julun.huanque.common.bean.forms.ProgramIdForm
import com.julun.huanque.common.commonviewmodel.BaseViewModel
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.LiveRoomService


/**
 * Created by dong on 2018/4/12.
 */
class PlayerBannerViewModel : BaseViewModel() {
    //直播间广告数据
    val roomBannerData: MutableLiveData<ArrayList<RoomBanner>> by lazy { MutableLiveData<ArrayList<RoomBanner>>() }
    //Pk相关数据
    val pkData: MutableLiveData<PKInfoBean> by lazy { MutableLiveData<PKInfoBean>() }

    val statusBean: MutableLiveData<BannerStatusBean> by lazy { MutableLiveData<BannerStatusBean>() }
    //需要展示H5的code
    val popCode: MutableLiveData<MutableList<String>> by lazy { MutableLiveData<MutableList<String>>() }
    //Pk结果返回
//    val pkResultData : MutableLiveData<PKRes>
    private var checkBannering = false

    //获取PK信息
    fun getPkInfo(info: ProgramIdForm) {
//        Requests.create(LiveRoomService::class.java)
//                .roomPkInfo(info)
//                .handleResponse(makeSubscriber<PKInfoBean> {
//                    pkData.value = it
//                })
    }

    /**
     * 检测banner状态
     */
    fun checkBanner(bannerCodes: String, programId: Int) {
        if (checkBannering) {
            return
        }
        checkBannering = true
        //todo
//        Requests.create(LiveRoomService::class.java)
//                .allBannerStatus(BannerStatusForm(bannerCodes, programId))
//                .handleResponse(makeSubscriber<ArrayList<SingleBannerCheckResult>> {
//                    val codes = mutableListOf<String>()
//                    it.forEach { result ->
//                        if (result.showPopup) {
//                            codes.add(result.adCode)
//                        }
//                    }
//                    popCode.value = codes
//                }.ifError { popCode.value = null }.withFinalCall { checkBannering = false })
    }
}