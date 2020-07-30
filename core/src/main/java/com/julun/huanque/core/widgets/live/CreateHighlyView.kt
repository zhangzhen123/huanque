package com.julun.huanque.core.widgets.live

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.facebook.drawee.backends.pipeline.Fresco
import com.julun.huanque.common.bean.beans.AnimModel
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.view_live_layer_anchor.view.*
import kotlinx.android.synthetic.main.view_live_layer_gif.view.*
import kotlinx.android.synthetic.main.view_live_layer_luck_gift.view.*
import kotlinx.android.synthetic.main.view_live_layer_open_guard.view.*
import kotlinx.android.synthetic.main.view_live_layer_user.view.*
import org.jetbrains.anko.imageResource

/**
 * 创建各种动画：升级、中奖、开通守护
 * Created by djp on 2016/12/18.
 */
class CreateHighlyView(context: Context) {
    private val logger = ULog.getLogger("CreateHighlyView")

    private val layoutInflater: LayoutInflater by lazy { LayoutInflater.from(context) }
    private var anchorView: View? = null
    private var userView: View? = null
    private var luckView: View? = null
    private var superLuckView: View? = null
    private var openGuardView: View? = null
    private var gifView: View? = null

    // 主播升级了
    fun viewWithAnchor(animModel: AnimModel): View {
        if (anchorView == null) {
            anchorView = layoutInflater.inflate(R.layout.view_live_layer_anchor, null)
        }
        with(anchorView!!) {
            anchorText.text = "恭喜${animModel.nickname}成功升级到"
            anchorLevel.text = "Lv${animModel.levelValue}"
            anchorLevelImage.imageResource = ImageHelper.getAnchorLevelResId(animModel.levelValue)
        }
        return anchorView!!
    }

    // 用户升级了
    fun viewWithUser(animModel: AnimModel): View {
        if (userView == null) {
            userView = layoutInflater.inflate(R.layout.view_live_layer_user, null)
        }
        val word =
            when {
                animModel.levelValue <= 10 -> {
                    "升级至"
                }
                animModel.levelValue in 11..20 -> {
                    "荣升"
                }
                animModel.levelValue > 20 -> {
                    "豪升"
                }
                else -> {
                    "升级至"
                }
            }
        with(userView!!) {
            userText.text = "恭喜${animModel.nickname}$word"
            userLevel.text = "Lv${animModel.levelValue}"
            userLevelImage.imageResource = ImageHelper.getUserLevelImg(animModel.levelValue)
        }
        return userView!!
    }

    // 用户中奖啦
    fun viewWithLuckGift(animModel: AnimModel): View {
        if (luckView == null) {
            luckView = layoutInflater.inflate(R.layout.view_live_layer_luck_gift, null)
        }
        with(luckView!!) {
            luckUser.text = "恭喜${animModel.nickname}喜中"
            luckText.text = "${animModel.extraObject["prize"]}萌豆"
        }
        return luckView!!
    }

    //用于赠送超级礼物中奖了
    fun viewWithSuperLuckGift(animModel: AnimModel): View {
        if (superLuckView == null) {
            superLuckView = layoutInflater.inflate(R.layout.view_live_layer_super_luck_gift, null)
        }
        with(superLuckView!!) {
            //            val text = "<font color = '#333333'> 恭喜${animModel.extraObject["nickname"]}在赠送“${animModel.extraObject["goodsName"]}”" +
//                    "喜中</font> <font color = '#e482ec'>${animModel.extraObject["prize"]}萌豆</font>"
//
//            luckUser.text = Html.fromHtml(text)
            luckUser.text = "恭喜${animModel.extraObject["nickname"]}在赠送“${animModel.extraObject["goodsName"]}”喜中"
            luckText.text = "${animModel.extraObject["prize"]}萌豆"
        }
        return superLuckView!!
    }

    // 开通守护了
    fun viewWithOpenGuard(animModel: AnimModel): View {
        if (openGuardView == null) {
            openGuardView = layoutInflater.inflate(R.layout.view_live_layer_open_guard, null)
        }
        with(openGuardView!!) {
            openGuardMan.text = "${animModel.nickname}"
            openGuardText.text = "开通了守护"
        }
        return openGuardView!!
    }

    // 播放gif动画
    fun viewWithGIF(animModel: AnimModel): View {
        if (gifView == null) {
            logger.info("创建gif动画View")
            gifView = layoutInflater.inflate(R.layout.view_live_layer_gif, null)
        }
        // 注意：按后台传过来的是dp处理
        var gifUrl = StringHelper.getOssImgUrl(animModel.extraObject["GIF"].toString())
        var width = if (animModel.extraObject["width"] != null)
            DensityHelper.dp2px(animModel!!.extraObject["width"].toString().toFloat()) else 0
        logger.info("天使动画宽度=$width")
        with(gifView!!) {
            var lp = gifImage.layoutParams as FrameLayout.LayoutParams
            // 超过屏幕就整个宽度了
            if (width > 0 && width < ScreenUtils.getScreenWidth()) {
                lp.width = width
            } else {
                lp.width = FrameLayout.LayoutParams.MATCH_PARENT
            }
            gifImage.layoutParams = lp
            gifImage.controller = Fresco.newDraweeControllerBuilder().setAutoPlayAnimations(true)
                .setOldController(gifImage.controller).setTapToRetryEnabled(true).setUri(Uri.parse(gifUrl)).build()
        }
        return gifView!!
    }

    fun destoryResource() {
//        layoutInflater = null
        userView = null
        anchorView = null
        luckView = null
        openGuardView = null
        gifView = null
    }
}