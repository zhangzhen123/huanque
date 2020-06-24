package com.julun.huanque.common.message_dispatch

import android.os.Handler
import com.julun.huanque.common.bean.events.AnimatorEvent
import com.julun.huanque.common.utils.ULog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 缓存定时器
 * 实现 push/shift 结构的缓冲定时器，外部可以将任何数据push到队列中，但是数据shift会按照一定时间弹出，从而实现一个缓冲模式
 * Created by djp on 2016/12/19.
 */
class BufferedTimer {
    private val logger = ULog.getLogger("MessageProcess")
    // 定时器已经初始化
    private val TIMER_STATE_READY = 0
    // 定时器运行状态
    private val TIMER_STATE_RUNNING = 1
    // 定时器暂停中
    private val TIMER_STATE_PAUSE = 2
    // 定时器已销毁
    private val TIMER_STATE_DESTORY = 3

    private var timerState: Int = -1

    /** 暂时有最普通的list，后面看换成队列什么的 **/
    private var queue = mutableListOf<Any>()
    private var callback: ((bufferedSelf: BufferedTimer) -> Float)? = null

    private var handler: Handler? = null

    constructor() {
        handler = Handler()
//        EventDispatcherCenter.registerEventHandler(this)
        EventBus.getDefault().register(this)
    }

    // 初始化带返回值的匿名方法块
    fun initWithRefreshViewBlock(callback: (bufferedSelf: BufferedTimer) -> Float) {
        this.callback = callback
        timerState = TIMER_STATE_READY
    }

    fun count(): Int {
        return queue.size
    }

    // 将数据压入缓冲区
    fun push(data: Any): Boolean {
        if (timerState == TIMER_STATE_DESTORY) {
            logger.info("定时器已经被销毁了")
            return false
        }
        synchronized(queue) {
            // 将数据添加到队列末尾
            queue.add(data)
        }
        // 如果当前状态是暂停的，重新启动定时器消费
        if (timerState == TIMER_STATE_PAUSE) {
            startIfTimerNotRunning()
        }
        return true
    }

    // 启动计时器
    fun start(): Boolean {
        if (timerState != TIMER_STATE_READY) {
            return false
        }
        // 如果没有数据，首次启动就不用执行了
        if (queue.size == 0) {
            timerState = TIMER_STATE_PAUSE;
            return true
        }

        return startIfTimerNotRunning()
    }

    // 没有启动，则启动计时器
    fun startIfTimerNotRunning(): Boolean {
        if (timerState == TIMER_STATE_RUNNING) {
            return false
        } else {
            callRefreshViewWhenNotPauseState()
            return true
        }
    }

    // 弹出队列中第一个值
    fun shiftOneObject(): Any? {
        synchronized(queue) {
            if (queue.size > 0) {
                var firstItem = queue[0]
                queue.removeAt(0)
                return firstItem
            }
            return null
        }
    }

    // 取队列中前N条记录
    fun shiftWithCount(count: Int): List<Any>? {
        synchronized(queue) {
            if (queue.size == 0) return null
            var lastIndex = count
            if (queue.size < count) {
                lastIndex = queue.count()
            }
            var subList = queue.subList(0, lastIndex)
            var newQueue = mutableListOf<Any>()
            if (subList.size > 0) {
                queue.forEachIndexed { i, any ->
                    if (i >= lastIndex) {
                        newQueue.add(any)
                    }
                }
            }
            queue = newQueue
            return subList
        }
    }

    // 延迟handler
    fun buildTaskBlock(callback: () -> Unit, afterSeconds: Long) {
        handler!!.postDelayed({
            callback()
        }, afterSeconds)
    }

    private fun callRefreshViewWhenNotPauseState() {
//        logger.info("callRefreshViewWhenNotPauseState:${Thread.currentThread()}")
        // 定时器已删除，不需要执行了
        if (timerState == TIMER_STATE_DESTORY) return

        timerState = TIMER_STATE_RUNNING
        logger.info("handler post:${Thread.currentThread().name}")
        buildTaskBlock({
            // 刷新完当前的视图，如果返回延迟时间大于0
            val nextTaskDelaySeconds = this.callback!!.invoke(this)
            if (nextTaskDelaySeconds > 0) {
                // 执行下一个周期任务
                buildTaskBlock({
                    callRefreshViewWhenNotPauseState()
                }, (nextTaskDelaySeconds * 1000).toLong())
            } else if (nextTaskDelaySeconds == MessageReceptor.EVENT_CLEAR) {
                // 暂停定时器了
                timerState = TIMER_STATE_PAUSE
            } else if (nextTaskDelaySeconds == MessageReceptor.EVENT_WAIT_MESSAGE) {
                needWaitMessage = true
            }
        }, 0)

    }

    var needWaitMessage = false
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun startNewAnimator(event: AnimatorEvent) {
        if (needWaitMessage) {
            logger.info("收到动画播放完成后的回调")
            callRefreshViewWhenNotPauseState()
        }
    }

    fun destory() {
        timerState = TIMER_STATE_DESTORY
        queue.clear()
        handler!!.removeCallbacksAndMessages(null)
        handler = null
//        EventDispatcherCenter.unRegisterEventHandler(this)
        EventBus.getDefault().unregister(this)
    }

}
