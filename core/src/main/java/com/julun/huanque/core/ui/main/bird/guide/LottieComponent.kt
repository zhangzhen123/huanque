package com.julun.huanque.core.ui.main.bird.guide

import android.view.LayoutInflater
import android.view.View
import com.binioter.guideview.Component
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.layer_bird_guide_lottie.view.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/11 14:58
 *
 *@Description: LottieComponent
 *
 */
class LottieComponent(var guideType: Int) : Component {
    var listener: OnListener? = null
    override fun getView(inflater: LayoutInflater): View {
        val ll = inflater.inflate(R.layout.layer_bird_guide_lottie, null)
        ll.close.onClickNew {
            logger("点击了关闭引导")
            listener?.onclickView(ll.close.id)
        }
        when (guideType) {
            0 -> {
                ll.title.text = "点击购买第一只奶嘴鹊"
                ll.click_holder.onClickNew {
                    logger("点击了购买1")
                    listener?.onclickView(ll.click_holder.id)
                }
            }
            1 -> {
                ll.title.text = "点击购买第二只奶嘴鹊"
                ll.click_holder.onClickNew {
                    logger("点击了购买2")
                    listener?.onclickView(ll.click_holder.id)
                }
            }

        }


        return ll
    }

    override fun getAnchor(): Int {
        return Component.ANCHOR_TOP
    }

    override fun getFitPosition(): Int {
        return Component.FIT_CENTER
    }

    override fun getXOffset(): Int {
        return 0
    }

    override fun getYOffset(): Int {
        return 80
    }

    interface OnListener {
        fun onclickView(viewId: Int)
    }
}