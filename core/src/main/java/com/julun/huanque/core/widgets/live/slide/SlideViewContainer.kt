package com.julun.huanque.core.widgets.live.slide

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R

/**
 *可左右滑动的容器
 *@author zhangzhen
 *@data 2017/4/13
 *
 *新增垂直滑动逻辑 上下滑动容器[ScrollVerticalContainer]
 *@author zhangzhen
 *@data 2019/7/12
 *
 *新增左滑调出关注列表[SlideViewRightDrawer]
 *@author zhangzhen
 *@data 2019/7/15
 *
 *
 **/
class SlideViewContainer @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, style: Int = 0) :
    ConstraintLayout(context, attrs, style) {

    private val logger = ULog.getLogger("SlideViewContainer")

    private val MAX_SETTLE_DURATION = 600 // ms

    private val SC = 0.21f//有效手势滑动距离比

    private val LOSCR = 0.7f//有效当前位置距离比 根据该值 判断是否收缩回去或展示 右侧的

    //垂直方向的上下滑动位置比例临界值
    private val HLOSCR = 0.13f

//    private var mIsScrollStarted: Boolean = false

    private var outContainer: ScrollVerticalContainer? = null
    //正在执行动作时
//    private var isSetting = false

    var scrollEnable: Boolean = true//是否可用滑动效果 默认是可以的

    private val SCREEN_WIDTH: Int by lazy { ScreenUtils.getScreenWidth() }

    //右抽屉的宽度
    val RIGHTDRAWERWIDTH: Int by lazy { resources.getDimensionPixelOffset(R.dimen.right_drawer_width) }

    //水平滑动累积值
    var mScrollXDistance = 0f

    //垂直滑动累积值
    var mScrollYDistance = 0f

    //右抽屉滑动累积值
    var mRightDrawerDistance = 0f

    //左右滑动显隐的监听
    var onHideOrShowListener: OnHideOrShowListener? = null

    //上下切换的监听
    var onSwitchListener: OnSwitchListener? = null

    //滑动监听
    var onScrollListener: OnScrollListener? = null

    //记录当前的滑动垂直模式
    var isVerticalMode = false

    //记录当前的滑动水平模式
    var isHorizontalMode = false

    //是不是右侧抽屉滑动模式
    var isRightDrawerMode = false

    //右侧抽屉
    var rightDrawer: SlideViewRightDrawer? = null

    private val myHandler: Handler by lazy { Handler() }
    val myGestureListener: GestureDetector.SimpleOnGestureListener by lazy {
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
//                logger.info("onScroll:distanceX $distanceX distanceY:$distanceY")
                if (/*isSetting || */(rightDrawer != null && rightDrawer!!.isSetting) || (outContainer != null && outContainer!!.isSetting)) {
                    return false
                }
                if (Math.abs(distanceX) < Math.abs(distanceY)) {
//                    logger.info("当前处于垂直滑动 过滤")
                    if (isHorizontalMode) {
//                        logger.info("当前处于水平模式 不再执行垂直")
                        return true
                    }
                    if (scrollX != 0 && scrollX != -SCREEN_WIDTH) {
                        return true
                    }
                    isVerticalMode = true
                    mScrollYDistance += distanceY
                    onScrollListener?.onScrollVertical(distanceY.toInt())
                    outContainer?.scrollBy(0, distanceY.toInt())
//                    this@SlideViewContainer.scrollBy(0, distanceY.toInt())
                    return true
                } else {
                    if (isVerticalMode) {
//                        logger.info("当前处于垂直模式 不再执行水平")
                        return true
                    }
                    isHorizontalMode = true
//                    mScrollXDistance += distanceX
                    if (scrollX >= 0 && distanceX > 0) {
                        rightDrawer?.let {
                            if (it.scrollX <= 0/*&&isCanDragRightDrawer(e1.x)*/) {
                                isRightDrawerMode = true
                                //抵消惯性
                                val min = Math.min(0f - it.scrollX, distanceX)
                                mRightDrawerDistance += min
                                it.scrollBy(min.toInt(), 0)
//                                logger.info("rightDrawer向左划入 distanceX=$distanceX min=$min  rightDrawer.scx=${rightDrawer?.scrollX}")
                            } else {
                                isRightDrawerMode = false
//                                logger.info("不做任何操作 distanceX=$distanceX scrollX=$scrollX rightDrawer.scx=${rightDrawer?.scrollX}")
                            }
                        }
                    } else {
                        if (rightDrawer != null && rightDrawer!!.scrollX > -RIGHTDRAWERWIDTH) {
                            isRightDrawerMode = true
                            mRightDrawerDistance += distanceX
                            rightDrawer?.scrollBy(distanceX.toInt(), 0)
//                            logger.info("rightDrawer向右划出 distanceX=$distanceX rightDrawer.scx=${rightDrawer?.scrollX}")
                        } else {
//                            logger.info("正常的清屏 distanceX=$distanceX scrollX=$scrollX ")
                            isRightDrawerMode = false
                            mScrollXDistance += distanceX
                            if (distanceX > 0) {
                                //抵消惯性
                                val min = Math.min(0f - scrollX, distanceX)
                                onScrollListener?.onScrollHorizontal(min.toInt())
                                this@SlideViewContainer.scrollBy(min.toInt(), 0)
                            } else {
                                onScrollListener?.onScrollHorizontal(distanceX.toInt())
                                this@SlideViewContainer.scrollBy(distanceX.toInt(), 0)
                            }
                        }
                    }
                    return true
                }
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                logger.info("onFling:velocityX $velocityX")
                return super.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onDown(e: MotionEvent?): Boolean {

//                mIsScrollStarted = true
                mScroller.abortAnimation()

//                outContainer?.abortAnimation()
//                outContainer?.mIsScrollStarted = true

//                rightDrawer?.abortAnimation()
                //每次重置模式
                isVerticalMode = false
                isHorizontalMode = false
                isRightDrawerMode = false
                logger.info("onDown ${e?.action}")
                return true
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                closeRightDrawer()
                return super.onSingleTapUp(e)
            }
        }

    }

    private var mDetector: GestureDetectorCompat? = null
    private val MEASURED_WIDTH by lazy { this.measuredWidth }//组件的宽度

    //    private val SCREENHEIGHT by lazy { ScreenUtils.getScreenHeight() }
    private var mScroller: Scroller
    private val sInterpolator = Interpolator { t ->
        var t = t
        t -= 1.0f
        t * t * t * t * t + 1.0f
    }

    init {
        logger.info("开始初始化滑动控件")
        mDetector = GestureDetectorCompat(context, myGestureListener)
        mScroller = Scroller(context, sInterpolator)
        //
    }

    /**
     * 是否可以拖起右抽屉 只有滑动点起始于右半屏才能唤起
     */
    @Deprecated("先不要")
    fun isCanDragRightDrawer(pointX: Float): Boolean {
        logger.info("isCanDragRightDrawer:$pointX")
        return pointX > SCREEN_WIDTH / 2.0f
    }

    override fun onAttachedToWindow() {
        logger.info("开始展现onAttachedToWindow")
        if (parent != null)
            outContainer = parent as ScrollVerticalContainer
        outContainer?.onScrollListener = object : ScrollVerticalContainer.OnScrollListener {
            override fun onScroll(dx: Int, dy: Int) {
//                if (isVerticalMode) {
                onScrollListener?.onScrollVertical(dy)
//                }
            }
        }
        rightDrawer = findViewById(R.id.right_drawer)
        //默认先隐藏起来
        rightDrawer?.scrollTo(-RIGHTDRAWERWIDTH, 0)
        outContainer?.setOnTouchListener { v, event ->
            filterEvent(event)
        }
        super.onAttachedToWindow()
    }

    //    override fun onTouchEvent(event: MotionEvent): Boolean {
//        logger.info("onTouchEvent："+event?.x)
//        filterEvent(event)
//        return super.onTouchEvent(event)
//    }
//
//    private var mLastMotionX: Float = 0.toFloat()
//    private var mLastMotionY: Float = 0.toFloat()
//    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
//        logger.info("onInterceptTouchEvent："+ev?.x)
//        val x = ev.getX()
//        val y = ev.getY()
//        when (ev.getAction()) {
//            MotionEvent.ACTION_DOWN -> {
//                mLastMotionX = x
//                mLastMotionY = y
//            }
//            MotionEvent.ACTION_MOVE ->
//                return Math.abs(x - mLastMotionX) > Math.abs(y - mLastMotionY)
//            MotionEvent.ACTION_UP,
//                // 当从本空间移到外部控件时,本控件不再处理,交由父类处理
//            MotionEvent.ACTION_CANCEL //从本控件移到外部控件时触发
//            -> return false
//        }
//        return super.onInterceptTouchEvent(ev)
//    }
    override fun onDetachedFromWindow() {
        // To be on the safe side, abort the scroller
        if (mScroller != null && !mScroller.isFinished) {
            mScroller.abortAnimation()
        }
        myHandler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    private fun filterEvent(event: MotionEvent): Boolean {
        if (scrollEnable && mDetector != null) {
            //把离开事件给过滤出来
            when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    logger.info("mScrollXDistance=$mScrollXDistance mScrollYDistance=$mScrollYDistance")
                    //
                    if (isHorizontalMode) {
                        if (isRightDrawerMode) {
//                            logger.info("右抽屉模式$mRightDrawerDistance")
                            rightDrawerShowOrHide()
                            mRightDrawerDistance = 0f
                        } else {
//                            logger.info("当前水平滑动不在原点$mScrollXDistance")
                            showOrHide()
                        }
                        //初始化移动的距离
                        mScrollXDistance = 0f
                    } else if (isVerticalMode) {
//                        logger.info("当前垂直滑动不在原点$mScrollYDistance")
                        if (event.action == MotionEvent.ACTION_CANCEL) {
                            outContainer?.smoothScrollTo(0, 0)
                        } else {
                            scrollOrRecover()
                        }
                        mScrollYDistance = 0f
                    } else {
                        finishScroll()
//                        outContainer?.finishScroll()
//                        rightDrawer?.finishScroll()
                    }
                }
            }
            return mDetector!!.onTouchEvent(event)
        } else {
            return false
        }
    }

    /**
     * 这里的逻辑只处理清屏侧滑的
     */
    private fun showOrHide() {

        val sc = Math.abs(mScrollXDistance / MEASURED_WIDTH)//有效滑动比

        val losc = Math.abs(scrollX / MEASURED_WIDTH.toFloat())//当前位置比

//        logger.info("当前的位置scrollX：" + scrollX + " mScrollXDistance:" + mScrollXDistance +
//                "有效滑动比：" + sc + "当前位置比:" + losc)
        //等于0的代表点击或点触 不需要处理
        if (sc == 0f) {
            return
        }
        if (sc > SC) {
            //达到有效拖动距离
            if (mScrollXDistance > 0) {
                //向左展示
                smoothScrollTo(0, 0)

                onHideOrShowListener?.onchange(true)
            } else {
                //向右收回
                smoothScrollTo(-MEASURED_WIDTH, 0)
                onHideOrShowListener?.onchange(false)
            }
        } else {
            //距离不够返回原处
//            if (mScrollXDistance >= 0 && losc > LOSCR) {
//                //向左展示距离不够收回了
//                smoothScrollTo(-MEASURED_WIDTH, 0)
//                onHideOrShowListener?.onchange(false)
//            } else {
//                //向右收回距离不够返回原状态
//                smoothScrollTo(0, 0)
//                onHideOrShowListener?.onchange(true)
//            }
            //距离不够的 如果当前在右侧就继续收回 否则在左侧正常展示
            if (losc > LOSCR) {
                smoothScrollTo(-MEASURED_WIDTH, 0)
                onHideOrShowListener?.onchange(false)
            } else {
                smoothScrollTo(0, 0)
                onHideOrShowListener?.onchange(true)
            }
        }
    }

    //是否可以上下滑动切换
    var canScrollPager: Boolean = false


    /**
     * 这里的逻辑只处理垂直滑动
     */
    private fun scrollOrRecover() {
        val out = outContainer ?: return
        if (!canScrollPager) {
            out.smoothScrollTo(0, 0)
            return
        }
        //考虑到可视高度可变
        val totalHeight = out.measuredHeight
        logger.info("当前的容器总高度：$totalHeight")
        val sc = Math.abs(mScrollYDistance / totalHeight)//有效滑动比

        val losc = out.scrollY / totalHeight.toFloat()//当前位置比

//        logger.info("当前的位置scrollY：" + out.scrollY + " mScrollYDistance:" + mScrollYDistance +
//                "有效滑动比：" + sc + "当前位置比:" + losc)
        //等于0的代表点击或点触 不需要处理
        if (sc == 0f) {
            return
        }
        if (losc > HLOSCR) {
            //向上滑动
            out.smoothScrollTo(0, totalHeight)
            myHandler.postDelayed({
                onSwitchListener?.onSwitch(true)
            }, ScrollVerticalContainer.DELAY_CALLBACK_TIME)
        } else if (losc < -HLOSCR) {
            //向下滑动
            out.smoothScrollTo(0, -totalHeight)
            myHandler.postDelayed({
                onSwitchListener?.onSwitch(false)
            }, ScrollVerticalContainer.DELAY_CALLBACK_TIME)
        } else {
            out.smoothScrollTo(0, 0)
        }
//        }
    }

    /**
     * 对外开放 重置当前slideView位置
     */
    fun resetSelf() {
        val x = mScroller.currX
        val y = mScroller.currY
        val dx = 0 - x
        val dy = 0 - y
        mScroller.startScroll(x, y, dx, dy)
        ViewCompat.postInvalidateOnAnimation(this)
//        scrollTo(0, 0)
    }

    /**
     * 对外开放 重置右抽屉操作
     */
    fun resetRightDrawer() {
        //重置右抽屉位置
        rightDrawer?.scrollTo(-RIGHTDRAWERWIDTH, 0)
    }

    /**
     * 对外开放 显示右抽屉操作
     */
    fun showRightDrawer() {
        //划出右抽屉
        rightDrawer?.scrollTo(0, 0)
    }

    /**
     * 关闭又抽屉 带动画
     */
    private fun closeRightDrawer() {
        if (rightDrawer?.scrollX ?: 0 >= 0) {
            rightDrawer?.smoothScrollTo(-RIGHTDRAWERWIDTH, 0)
        }
    }

    /**
     * 这里的逻辑只处理右抽屉侧滑的
     */
    private fun rightDrawerShowOrHide() {

        val rightView = rightDrawer ?: return
        val sc = Math.abs(mRightDrawerDistance / RIGHTDRAWERWIDTH)//有效滑动比

        val losc = Math.abs(rightView.scrollX / RIGHTDRAWERWIDTH.toFloat())//当前位置比

//        logger.info("rightView当前的位置scrollX：" + rightView.scrollX + " mRightDrawerDistance:" + mRightDrawerDistance +
//                "有效滑动比：" + sc + "当前位置比:" + losc)
        //等于0的代表点击或点触 不需要处理
        if (sc == 0f) {
            return
        }
        if (sc > SC) {
            //达到有效拖动距离
            if (mRightDrawerDistance > 0) {
                //向左展示
                rightView.smoothScrollTo(0, 0)
            } else {
                //向右收回
                rightView.smoothScrollTo(-RIGHTDRAWERWIDTH, 0)
            }
        } else {
            //距离不够的 如果当前在右侧就继续收回 否则在左侧正常展示
            if (losc > LOSCR) {
                rightView.smoothScrollTo(-RIGHTDRAWERWIDTH, 0)
            } else {
                rightView.smoothScrollTo(0, 0)
            }
        }
    }

    override fun computeScroll() {
//        mIsScrollStarted = true
        // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
        if (mScroller.computeScrollOffset()) {
//            isSetting = true
//            if (isHorizontalMode) {
            onScrollListener?.onScrollHorizontal(mScroller.currX - scrollX/*, mScroller!!.currY - scrollY*/)
            scrollTo(mScroller.currX, mScroller.currY)
            ViewCompat.postInvalidateOnAnimation(this)
//            }
        } else {
//            isSetting = false
        }
    }

    private fun smoothScrollTo(x: Int, y: Int) {
        val sx: Int
        val wasScrolling = mScroller != null && !mScroller.isFinished
        if (wasScrolling) {
//            sx = if (mIsScrollStarted) mScroller.currX else mScroller.startX
            sx = mScroller.currX
            // And abort the current scrolling.
//            mScroller.abortAnimation()
        } else {
            sx = scrollX
        }
        val sy = scrollY
        val dx = x - sx
        val dy = y - sy
        if (dx == 0 && dy == 0) {
            return
        }
        val duration: Int = MAX_SETTLE_DURATION
        // Reset the "scroll started" flag. It will be flipped to true in all places
        // where we call computeScrollOffset().
//        mIsScrollStarted = false
        mScroller.startScroll(sx, sy, dx, dy, duration)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * 完成未完成的滑动到最后
     */
    private fun finishScroll() {

//        val oldX = scrollX
//        val oldY = scrollY
//        val x = mScroller.currX
//        val y = mScroller.currY
//        val dx = x - oldX
//        val dy = y - oldY
//        logger.info("completeScroll x=$x y=$y")
//        if (dx != 0 || dy != 0) {
//            scrollTo(x, y)
//            onScrollListener?.onScrollHorizontal(dx/*, mScroller!!.currY - scrollY*/)
//        }
        val oldX = scrollX
        val oldY = scrollY
        val x = mScroller.currX
        val y = mScroller.currY
        if (oldX != x || oldY != y) {
            val dx = x - oldX
            val dy = y - oldY
//            logger.info("dx=$dx dy=$dy mScroller.isFinished:${mScroller.isFinished}")
            mScroller.startScroll(oldX, oldY, dx, dy)
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    //显示总布局
    fun openViews() {
        smoothScrollTo(0, 0)
        onHideOrShowListener?.onchange(true)
    }

    //隐藏总布局
    fun clearViews() {
        smoothScrollTo(-MEASURED_WIDTH, 0)
        onHideOrShowListener?.onchange(false)
    }

    interface OnHideOrShowListener {
        fun onchange(isShow: Boolean)
    }

    interface OnScrollListener {
        fun onScrollHorizontal(dx: Int)
        fun onScrollVertical(dy: Int)
    }

    interface OnSwitchListener {
        /***
         * @param next 是不是下一个 反之是上一个
         *
         */
        fun onSwitch(next: Boolean)
    }
}