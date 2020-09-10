package com.julun.huanque.core.widgets.live.simpleffects

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.julun.huanque.common.bean.beans.SendGiftEvent
import com.julun.huanque.common.suger.sortList
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.view_simple_effects_container.view.*
import java.util.*

/**
 *
 *@author zhangzhen
 *@data 2019/3/13
 *
 **/

class SimpleGiftEffectsContainerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs) {
    private val logger = ULog.getLogger("EffectsContainerView")

    private val messageHandler: Handler by lazy {
        Handler()
    }
    private var isTasking = false
    private val messageRunnable: Runnable by lazy {
        Runnable {
            isTasking = true
            checkAndPick(giftAnimView1, giftAnimView2)
            checkAndPick(giftAnimView2, giftAnimView1)
            if (queueGiftEffects.isNotEmpty()) {
                messageHandler.postDelayed(messageRunnable,
                    MINDELAY
                )
            } else {
                isTasking = false
            }
        }
    }

    //礼物特效队列
    private val queueGiftEffects: LinkedList<SendGiftEvent> = LinkedList()

    companion object {
        const val MINDELAY = 100L
        fun getUserOrderKey(sendGift: SendGiftEvent?): String {
            return sendGift?.userId.toString() + "_" + sendGift?.giftId
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_simple_effects_container, this)
        if (!isInEditMode) {
            giftAnimView1.setOtherAnimationView(giftAnimView2, this)
            giftAnimView2.setOtherAnimationView(giftAnimView1, this)
        }
    }

    //    // 弹出一个相同用户的礼物
//    fun popSameUserGiftTaskWithLastTask(lastGift: SendGiftEvent): SendGiftEvent? {
//        return shiftOneGiftTask(getUserOrderKey(lastGift))
//    }

    fun haveHighlyGift(lastGift: SendGiftEvent?, thatChild: SimpleGiftEffectsView?): Boolean {
        if (lastGift == null || thatChild == null)
            return false
        var has = false
        //存在更高价值或相等的礼物并且另一个不在播并且是不是自己
        run breaking@{
            queueGiftEffects.forEach {
                if (it.totalBeans >= lastGift.totalBeans && !thatChild.isPlaySame(it) && lastGift.isMyself == 0) {
                    has = true
                    return@breaking
                }
            }
        }
        return has
    }

    // 接收送礼通知消息
    fun handleSendGiftNotification(newGift: SendGiftEvent) {
        logger.info("收到一条送礼通知 $newGift")
        //先塞进队列 排序
        queueGiftEffects.offer(newGift)
        queueGiftEffects.sortList(Comparator { e1: SendGiftEvent, e2: SendGiftEvent ->
            //先判断是不是自己
            var result = e2.isMyself - e1.isMyself
            //再判断礼物价值
            if (result == 0) {
                result = e2.totalBeans - e1.totalBeans
            }
            //如果正在播 优先
            if (result == 0) {
                result = isInPlay(e2) - isInPlay(e1)
            }
            //再判断时间
            if (result == 0) {
                result = (e1.time - e2.time).toInt()
            }
            result
        })
        if (giftAnimView1.isPlaySame(newGift)) {
            logger.info("giftAnimView1正在播 立即执行$newGift")
            checkAndPick(giftAnimView1, giftAnimView2)
        } else if (giftAnimView2.isPlaySame(newGift)) {
            logger.info("giftAnimView2正在播 立即执行$newGift")
            checkAndPick(giftAnimView2, giftAnimView1)
        }
        startTask()
    }

    //判断是否在播放
    private fun isInPlay(e: SendGiftEvent): Int {
        return if (giftAnimView1.isPlaySame(e) || giftAnimView2.isPlaySame(e)) 1 else 0
    }

    //    var isTasking=false //是否正在执行任务
    private fun startTask() {
        if (!isTasking) {
            messageHandler.removeCallbacks(messageRunnable)
            messageHandler.postDelayed(messageRunnable,
                MINDELAY
            )
        }
    }

    fun checkAndPick(thisChild: SimpleGiftEffectsView, thatChild: SimpleGiftEffectsView?): SendGiftEvent? {
        if (thatChild == null) return null
        if (queueGiftEffects.isEmpty()) return null
        var validItem: SendGiftEvent? = null
//        logger.info("queueGiftEffects:$queueGiftEffects")
//        logger.info("queueGiftEffects size:${queueGiftEffects.size}")
        val lastIndex = queueGiftEffects.size - 1
        for (i in 0..lastIndex) {
            val it = queueGiftEffects[i]
//            logger.info("当前的位置：${queueGiftEffects.indexOf(it)}")
            //另一个不在播的取出
            if (!thatChild.isPlaySame(it)) {
                validItem = it
                break
            }
        }
        validItem?.let {
            if (thisChild.isCanPlay(it)) {
//                logger.info("找到合适的开始播 位置：${queueGiftEffects.indexOf(it)}:$it")
                queueGiftEffects.remove(it)
                thisChild.startPlaying(it)
                return validItem
            }
        }
        return null
    }

    private fun clearQueueData() {
        queueGiftEffects.clear()
    }
    fun resetView(){
        clearQueueData()
        messageHandler.removeCallbacks(messageRunnable)
        isTasking=false
        giftAnimView1.resetView()
        giftAnimView2.resetView()
    }
    fun destroyResource() {
        clearQueueData()
        messageHandler.removeCallbacksAndMessages(null)
        isTasking=false
        giftAnimView1.destroyResource()
        giftAnimView2.destroyResource()
    }

}