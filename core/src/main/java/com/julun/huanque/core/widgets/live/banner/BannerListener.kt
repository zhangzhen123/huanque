package com.julun.huanque.core.widgets.live.banner

import com.julun.huanque.common.bean.beans.RoomBanner


interface BannerListener {
    fun doCheck(bannerCodes: String)

    /**
     * 广告失效或者不是活动页面 点击事件相关操作
     */
    fun doAction(roomBean: RoomBanner)
}