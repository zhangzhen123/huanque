package com.julun.huanque.core.widgets.live.message

import android.content.Context
import android.util.AttributeSet
import com.julun.huanque.common.bean.events.AnimatorEvent
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.widgets.live.WebpGifView
import org.greenrobot.eventbus.EventBus

/**
 *
 *@author zhangzhen
 *@data 2017/9/6
 * 高级webp动画 就是只播放高级礼物动画的
 *
 **/
class HighlyWebpView constructor(context: Context?, attrs: AttributeSet?) : WebpGifView(context, attrs) {

    constructor(context: Context?) : this(context,null)

//    // 顶部栏高度
//    private val HEADER_HEIGHT: Int by lazy { resources.getDimensionPixelSize(R.dimen.live_header_height) }
    var logger = ULog.getLogger("HighlyWebpView")

    private val mGiftViewPlayCallBack by lazy {
        object : WebpGifView.GiftViewPlayCallBack {
            override fun onStart() {
            }

            override fun onError() {
                logger.info("加载出错了")
                EventBus.getDefault().post(AnimatorEvent())

            }

            override fun onEnd() {
                logger.info("加载动画完成")
                this@HighlyWebpView.hide()
                EventBus.getDefault().post(AnimatorEvent())
            }

            override fun onRelease() {
            }

        }
    }

    init {
        //此处要根据该控件所在的父布局类型 不然可能会报错
        setCallBack(mGiftViewPlayCallBack)
    }



}