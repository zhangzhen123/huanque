package com.julun.huanque.core.widgets.live.message

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.bean.beans.BarrageEvent
import com.julun.huanque.common.bean.beans.UserInfoBean
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.message_dispatch.EventMessageType

import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.widgets.live.chatInput.EmojiUtil
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.BusiConstantCore

import kotlinx.android.synthetic.main.view_barrage_layout.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.rightPadding
import java.util.*

/**
 *
 * 展示弹幕
 *@author zhangzhen
 *@data 2017/5/10
 *
 **/

class BarrageAnimatorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    //弹幕的队列
    val queueListOfBarrage: MutableList<BarrageEvent> = Collections.synchronizedList(mutableListOf())
    val DURATION = 10
    var playHandler: Handler? = null
    private var playerViewModel: PlayerViewModel? = null
    var anchorId = 0L

    init {
//        LayoutInflater.from(context).inflate(R.layout.view_barrage_layout_container, this)
        if (!isInEditMode) {
            @SuppressLint("HandlerLeak")
            playHandler = object : Handler() {
                override fun handleMessage(msg: Message?) {
                    msg?.let {
                        when (msg.what) {
                            1 -> {
                                isPlay = false
                                if (queueListOfBarrage.size > 0) playBarrageAnimator(null)
                            }

                            else -> return
                        }

                    }
                }

            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val activity = context as? PlayerActivity
        activity?.let { playerViewModel = ViewModelProviders.of(it).get(PlayerViewModel::class.java) }
    }


    fun inflateNewBarrage() = LayoutInflater.from(context).inflate(R.layout.view_barrage_layout, null) as RelativeLayout
    fun removeUsedView(removeView: View) {
        this.removeView(removeView)
    }


    var isPlay = false
    private var animatorSet: AnimatorSet? = null
    private fun setContentInfo(barrageView: RelativeLayout, event: BarrageEvent) {
        this.addView(barrageView)
        if (event.eventCode == EventMessageType.BROADCAST.name) {
            barrageView.barrage_container?.setBackgroundResource(R.mipmap.barrage_global_bg)
            barrageView.barrage_content?.setTextColor(Color.parseColor("#FFE53C"))
        } else if (event.eventCode == EventMessageType.SendDanmu.name) {
//            barrage_container?.setBackgroundResource(R.mipmap.barrage_current_bg)
//            barrage_content?.setTextColor(Color.parseColor("#42D5FC"))
        }
        ImageUtils.loadImage(barrageView.user_headImage, event.headPic, 40f, 40f)
//        royalLevelImage?.imageResource = ImageUtils.getRoyalLevelImg(event.royalValue)
        if (event.anchorLevel > 0) {
            //主播身份
            barrageView.user_level?.show()
            barrageView.sdv.hide()
            barrageView.user_level?.imageResource = ImageHelper.getAnchorLevelResId(event.anchorLevel)
        } else {
            //用户身份
            if (event.mystery) {
                barrageView.user_level?.hide()
                barrageView.sdv?.show()
                ImageUtils.loadImageWithHeight_2(barrageView.sdv, BusiConstantCore.MysteryURL, DensityHelper.dp2px(15f))
            } else {
                barrageView.user_level?.show()

                //显示上神图标
                if(event.royalPic.isNotEmpty()){
                    barrageView.sdv.show()
                    ImageUtils.loadImageWithHeight_2(barrageView.sdv, event.royalPic, DensityHelper.dp2px(15f))
                }else{
                    barrageView.sdv.hide()
                }
                barrageView.user_level?.imageResource = ImageHelper.getUserLevelImg(event.userLevel)
                //todo
//                when (event.royalDmLevel) {
//                    1 ->{
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.royal_lv1), null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg_01
//                    }
//                    2 ->{
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.royal_lv2), null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg_02
//                    }
//                    3 ->{
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.royal_lv3), null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg_03
//                    }
//                    4 ->{
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.royal_lv4), null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg_04
//                    }
//                    5 -> {
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.royal_lv5), null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg_05
//                    }
//                    6 -> {
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.royal_lv6), null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg_06
//                    }
//                    7 -> {
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.royal_lv7), null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg_07
//                    }
//                    8 -> {
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.barrage_royal_lv8), null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg_08
//                    }
//                    9 -> {
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.mipmap.barrage_royal_lv9), null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg_09
//                    }
//                    else -> {
//                        barrageView.barrage_content?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
//                        barrageView.barrage_content?.backgroundResource = R.drawable.barrage_bg
//                    }
//                }
                if(event.royalDmLevel>0){
                    barrageView.barrage_content.rightPadding=0
                }else{
                    barrageView.barrage_content.rightPadding=DensityHelper.dp2px(5f)
                }
            }
        }
        barrageView.user_nickname?.text = event.nickname
        barrageView.barrage_content?.text = EmojiUtil.message2emoji(event.content)
    }


    fun playBarrageAnimator(bean: BarrageEvent?) {
        bean?.let {
            queueListOfBarrage.add(bean)
        }

//        println("播放playBarrageAnimator")
        if (isPlay || queueListOfBarrage.size <= 0) return
        val barrageView = inflateNewBarrage()
        var tempEvent = queueListOfBarrage[0]
        barrageView.onClickNew {
            //            (context as PlayerActivity).openUserInfoView(tempEvent.userId,false)
//            playerViewModel?.userInfoView?.value = UserInfoBean(tempEvent.userId, anchorId == tempEvent.userId, tempEvent.royalLevel, tempEvent.headPic)
        }
        setContentInfo(barrageView, tempEvent)
        if (queueListOfBarrage.size > 0) {
            queueListOfBarrage.removeAt(0)
        }

        animatorSet = AnimatorSet()
        var width = ScreenUtils.getViewRealWidth(barrageView).toFloat()
//        ULog.i("当前的宽度：" + width)
        val animator1 = ObjectAnimator.ofFloat(barrageView, "translationX", ScreenUtils.screenWidthFloat, -width)
        animator1.duration = (1000 * DURATION).toLong()
        animator1.interpolator = LinearInterpolator()
        animatorSet?.play(animator1)
        animatorSet?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                val time = (width * animator1.duration / ScreenUtils.getScreenWidth()).toLong()

                isPlay = true
                playHandler?.sendEmptyMessageDelayed(1, time)
//                    barrage_container?.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
//                    isPlay = false
//                    if (queueListOfBarrage.size <= 0) {
//                        barrage_container?.visibility = View.GONE
//                        return
//                    }
//                    //
//                    setContentInfo(inflateNewBarrage(),queueListOfBarrage[0])
//                    if (queueListOfBarrage.size > 0)
//                        queueListOfBarrage.removeAt(0)
//                    animation.startDelay = 300
//                    animation.start()
                removeUsedView(barrageView)
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
        animatorSet?.start()
    }

    fun clearQueue() {
        queueListOfBarrage.clear()
        isPlay = false
        playHandler?.removeCallbacksAndMessages(null)//退出时取消handler队列事件 防止内存泄漏
    }

    fun clear() {
        clearQueue()
        removeAllViews()
    }
}