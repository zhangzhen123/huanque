package com.julun.huanque.common.manager

import android.annotation.SuppressLint
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.commonviewmodel.OrderViewModel
import com.julun.huanque.common.suger.show
import java.util.*
import java.util.logging.Logger
import kotlin.math.min

/**
 *@创建者   dong
 *@创建时间 2020/1/15 15:46
 *@描述 activity内  Dialog顺序显示的管理类
 *
 * @alter zhangzhen
 * 根据产品的思想 弹窗进行分级 分为全局弹窗和局部弹窗
 * 全局弹窗优先级大于局部弹窗  为了整合所有的弹窗 这里该类对自己所在[act]的弹窗顺序显示的同时优先顺序显示全局弹窗 当前[act]关闭时清空其所属的[mDialogOrderList]队列
 * 全局弹窗[mGlobalDialogOrderList]队列不受影响
 *
 * @alter 由于AppBaseDialog初始化时就依赖activity 实际弹出时依附的activity会不一致 所以这里只对BaseDialogFragment(只有它实现了关闭唤醒队列)进行队列操作
 *
 */
class OrderDialogManager(val act: BaseActivity) {

    companion object {

        const val USER_POSITIVE = -2//用户手动打开（对于有排队可能的弹窗）
        const val NO_ORDER = -1//无排序
        const val LOCAL_ORDER = 0//局部页面排队
        const val GLOBAL_ORDER = 1//全局排队

        //全局的Dialog对象使用的数据对象 注 这里类型只接受Dialog或者DialogFragment
        private val mGlobalDialogOrderList = LinkedList<BaseDialogFragment>()

        //有些弹窗不能在一些指定Activity页面弹出 这里记录下来每次弹出时过滤处理
        private val mGlobalDialogNoActivityListMap = hashMapOf<BaseDialogFragment, MutableList<String>>()

        //记录当前有焦点的act的弹窗管理器
        private var currentManager: OrderDialogManager? = null

        //记录上次弹出是否失败 失败的话打开新界面时直接触发
        private var lastPopFailed = false

        /**
         * 启动弹窗管理
         */
        fun startManager(manager: OrderDialogManager?) {
            currentManager = manager
            //每次打开新页面不是必须触发 除非上一次弹出失败
            if (lastPopFailed) {
                lastPopFailed = false
                currentManager?.popData()
            }

        }

        /**
         * 由于是静态变量引用的manager引用act 所以在主页关闭时释放相应引用防止泄漏
         */
        fun release() {
            mGlobalDialogOrderList.clear()
            mGlobalDialogNoActivityListMap.clear()
            currentManager = null
        }

        /**
         * [data]要弹的窗
         * [noActivityList]该弹窗不能弹的页面集合
         */
        fun addGlobalOrderDialog(data: BaseDialogFragment?, noActivityList: MutableList<String>? = null) {
            if (data == null) return
            var index = mGlobalDialogOrderList.size
            //当前对象的排序值
            val currentOrder = data.order()
            if (currentOrder < 0) {
                //未处理的弹窗，不接受
                ULog.i("DXC 接收到未处理的弹窗数据 ${data.javaClass}")
                return
            }
            run {
                mGlobalDialogOrderList.forEachIndexed { i, da ->
                    val oriOrder = da.order()
                    if (currentOrder < oriOrder) {
                        index = i
                        return@run
                    }
                }
            }

            val realIndex = min(index, mGlobalDialogOrderList.size)
            mGlobalDialogOrderList.add(realIndex, data)

            if(noActivityList!=null){
                mGlobalDialogNoActivityListMap.put(data,noActivityList)
            }

            currentManager?.popData()
        }
    }

    //排序的局部弹窗Dialog使用的数据对象  这里类型只接受Dialog或者DialogFragment
    private val mDialogOrderList = LinkedList<BaseDialogFragment>()

    private val logger: Logger = ULog.getLogger("OrderDialogManager")

    //弹窗排序使用的ViewModel
    private var mOrderViewModel = ViewModelProviders.of(act).get(OrderViewModel::class.java)

    init {
        mOrderViewModel.popState.observe(act, Observer {
            if (it == true) {
//                logger.info("我收到了通知开始执行取数据")
                popData()
            }
        })
        mOrderViewModel.dialog.observe(act, Observer {
            if (it != null) {
                showDialog(it)
                mOrderViewModel.dialog.value = null
            }
        })
        act.lifecycle.addObserver(@SuppressLint("RestrictedApi")
        object : GenericLifecycleObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {

                    Lifecycle.Event.ON_CREATE -> {
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        startManager(this@OrderDialogManager)
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        clear()
                        act.lifecycle.removeObserver(this)
                    }
                }
            }

        })
        currentManager=this@OrderDialogManager
    }

    private fun showDialog(it: BaseDialogFragment) {
        if (!it.isAdded) {
            it.show(act, it.TAG)
        }
    }

    /**
     * 添加顺序DialogBean
     */
    @Synchronized
    fun addOrderDialogBean(data: BaseDialogFragment) {
        var index = mDialogOrderList.size
        //当前对象的排序值
        val currentOrder = data.order()
        if (currentOrder < 0) {
            //未处理的弹窗，不接受
//            logger.info("DXC 接收到未处理的弹窗数据 ${data.javaClass}")
            return
        }
        run {
            mDialogOrderList.forEachIndexed { i, da ->
                val oriOrder = da.order()
                if (currentOrder < oriOrder) {
                    index = i
                    return@run
                }
            }
        }

        val realIndex = min(index, mDialogOrderList.size)
        mDialogOrderList.add(realIndex, data)
        popData()
    }

    /**
     * 如果是用户手动打开了某个已经在队列的dialog 打开时移除队列中的相应dialog
     */
    fun checkAndRemoveOrderDialog(dialog: DialogFragment) {

        val iterator1 = mDialogOrderList.iterator()

        while (iterator1.hasNext()) {
            if (iterator1.next().javaClass == dialog.javaClass) {
                iterator1.remove()
            }
        }
        val iterator2 = mGlobalDialogOrderList.iterator()
        while (iterator2.hasNext()) {
            if (iterator2.next().javaClass == dialog.javaClass) {
                iterator2.remove()
            }
        }

    }

    /**
     * 取出一个数据,并发送出去
     */
    @Synchronized
    private fun popData() {
        logger.info("当前的队列弹窗数目：global=$mGlobalDialogOrderList local=${mDialogOrderList} 状态：${mOrderViewModel.mDispatchState}")
        if (mDialogOrderList.isEmpty() && mGlobalDialogOrderList.isEmpty()) {
            return
        }

        if (mOrderViewModel.popState.value != true && mOrderViewModel.mDispatchState) {
            return
        }
        //如果当前的activity已经失去焦点 不再弹窗 等待恢复后继续
        if (!act.isThisActivityForeground()) {
            lastPopFailed = true
            return
        }
        if (act.isFinishing) {
            lastPopFailed = true
            logger.info("act正在关闭中")
            return
        }
        var tempData: BaseDialogFragment? = null
        //优先取全局弹窗
        if (mGlobalDialogOrderList.isNotEmpty()) {
            val first = mGlobalDialogOrderList.first
            var canShow = true
            val mNoFateActivityList = mGlobalDialogNoActivityListMap[first]
            mNoFateActivityList?.forEach {
                if (it.contains(act.localClassName)) {
                    canShow = false
                    return@forEach
                }
            }
            tempData = if (!canShow) {
                null
            } else {
                val dialog=mGlobalDialogOrderList.pop()
                mGlobalDialogNoActivityListMap.remove(dialog)
                dialog
            }
        }
        if (tempData == null)
            if (mDialogOrderList.isNotEmpty()) {
                tempData = mDialogOrderList.pop()
            }

        if (tempData != null) {
            //存在未分发的数据
//            act.dispatchOrderData(tempData)
            //通过LiveData分发 保证生命周期正确
            mOrderViewModel.dialog.value = tempData
            mOrderViewModel.popState.value = null
            mOrderViewModel.mDispatchState = true
        } else {
            //所有数据分发结束
            mOrderViewModel.popState.value = null
            mOrderViewModel.mDispatchState = false
        }
    }

    /**
     * 当前的[act]关闭时 清空该界面的局部弹窗队列
     */
    fun clear() {
        mDialogOrderList.clear()
    }
}