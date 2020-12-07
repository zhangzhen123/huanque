package com.julun.huanque.common.statistics

import android.os.Handler
import com.julun.huanque.common.bean.forms.StatisticForm
import com.julun.huanque.common.bean.forms.StatisticItem
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.StatisticService
import com.julun.huanque.common.suger.nothing
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.ULog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CopyOnWriteArrayList


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/7 17:24
 *
 *@Description: StatisticManager 统计埋点管理器 每隔一定时间 上报一次
 *
 */
object StatisticManager {
    private val logger = ULog.getLogger("StatisticManager")

    private val service: StatisticService by lazy { Requests.create(StatisticService::class.java) }

    //eventType
    const val Click = "Click"
    const val Scan = "Scan"


    //多长时间执行一次
    private const val DELAY = 50 * 1000L


    /** 暂时有最普通的list，后面看换成队列什么的 **/
    private var queue = LinkedList<StatisticItem>()

    private val handler: Handler by lazy {
        Handler()
    }
    private var isTasking = false
    private val messageRunnable: Runnable by lazy {
        Runnable {
            isTasking = true
            doWork()
            if (queue.isNotEmpty()) {
                handler.postDelayed(
                    messageRunnable,
                    DELAY
                )
            } else {
                isTasking = false
            }
        }
    }

    // 将数据压入缓冲区
    fun push(data: StatisticItem) {
        // 将数据添加到队列末尾
        queue.add(data)

        // 如果当前状态是暂停的，重新启动定时器消费
        if (!isTasking) {
            logger.info("handler post:${Thread.currentThread().name}")
            isTasking = true
            handler.postDelayed(messageRunnable, DELAY)
        }
    }

    private fun doWork() {
        logger.info("handler post:${Thread.currentThread().name}")
        val listForm = mutableListOf<StatisticItem>()
        queue.groupBy { it.eventCode }.forEach { pair ->
            val form = StatisticItem()
            val first = pair.value.firstOrNull()
            var count = 0
            pair.value.forEach {
                count += it.clickNum
            }
            if (first != null) {
                form.eventCode = first.eventCode
                form.eventType = first.eventType

                form.enterTime = first.enterTime
                form.leaveTime = first.leaveTime

                form.clickNum = count
            }
            listForm.add(form)
        }
        queue.clear()
        val formString = JsonUtil.serializeAsString(listForm)
        GlobalScope.launch {
            logger.info("launch:${Thread.currentThread().name}")
            val form = StatisticForm(formString)
            service.dataStat(form).nothing()
        }

    }

    fun destroy() {
        queue.clear()
        handler.removeCallbacksAndMessages(null)
    }

}
