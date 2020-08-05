package com.julun.huanque.common.widgets

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.suger.inVisible
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs


/**
 * 可以滑动抹除有色区域的View（类似刮刮卡）
 * @author WanZhiYuan
 * @date 2019/11/12
 */
class ScratchMaskView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val logger = ULog.getLogger(this.javaClass.name)

    private var drawPaint: Paint? = null
    private var trackBitmap: Bitmap? = null // 轨迹图片，一会儿要在此图片上画滑动轨迹，然后通过 Xfermode 技术同遮罩层图片融合就能实现涂抹的效果
    private var trackBitmapCanvas: Canvas? = null
    private var xfermode: Xfermode? = null
    private var linePaint: Paint? = null    // 触摸轨迹线画笔

    private var defaultMaskColor = Color.LTGRAY
    private var maskBitmap: Bitmap? = null  // 遮罩层图片
    private var dstRect: Rect? = null
    private var srcRect: Rect? = null

    private var onViewListener: onViewStatusListener? = null

    private var mExecutorService: ExecutorService? = null

    private var mIsCompleted: Boolean = false

    private var mIsMove: Boolean = false

    init {
        drawPaint = Paint()
        drawPaint?.color = Color.BLACK
        drawPaint?.isFilterBitmap = true

        linePaint = Paint()
        linePaint?.color = Color.BLACK
        linePaint?.style = Paint.Style.STROKE
        linePaint?.strokeJoin = Paint.Join.ROUND // 前圆角
        linePaint?.strokeCap = Paint.Cap.ROUND // 后圆角
        linePaint?.strokeWidth = 50f // 笔宽

        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)

    }

    private fun computeSrcRect(sourceSize: Point, targetSize: Point, scaleType: ImageView.ScaleType): Rect {
        if (scaleType == ImageView.ScaleType.CENTER_INSIDE || scaleType == ImageView.ScaleType.MATRIX || scaleType == ImageView.ScaleType.FIT_XY) {
            return Rect(0, 0, sourceSize.x, sourceSize.y)
        } else {
            var scale: Float
            if (abs(sourceSize.x - targetSize.x) < abs(sourceSize.y - targetSize.y)) {
                scale = sourceSize.x.toFloat() / targetSize.x
                if ((targetSize.y * scale).toInt() > sourceSize.y) {
                    scale = sourceSize.y.toFloat() / targetSize.y
                }
            } else {
                scale = sourceSize.y.toFloat() / targetSize.y
                if ((targetSize.x * scale).toInt() > sourceSize.x) {
                    scale = sourceSize.x.toFloat() / targetSize.x
                }
            }
            val srcLeft: Int
            val srcTop: Int
            val srcWidth = (targetSize.x * scale).toInt()
            val srcHeight = (targetSize.y * scale).toInt()
            if (scaleType == ImageView.ScaleType.FIT_START) {
                srcLeft = 0
                srcTop = 0
            } else if (scaleType == ImageView.ScaleType.FIT_END) {
                if (sourceSize.x > sourceSize.y) {
                    srcLeft = sourceSize.x - srcWidth
                    srcTop = 0
                } else {
                    srcLeft = 0
                    srcTop = sourceSize.y - srcHeight
                }
            } else {
                if (sourceSize.x > sourceSize.y) {
                    srcLeft = (sourceSize.x - srcWidth) / 2
                    srcTop = 0
                } else {
                    srcLeft = 0
                    srcTop = (sourceSize.y - srcHeight) / 2
                }
            }
            return Rect(srcLeft, srcTop, srcLeft + srcWidth, srcTop + srcHeight)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val layerCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
        canvas.drawBitmap(trackBitmap?:return, paddingLeft.toFloat(), paddingTop.toFloat(), drawPaint)
        drawPaint?.xfermode = xfermode
        canvas.drawBitmap(maskBitmap?:return, srcRect, dstRect?:return, drawPaint)
        drawPaint?.xfermode = null
        if (!isInEditMode) {
            canvas.restoreToCount(layerCount)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val width = right - left
        val height = bottom - top

        // 初始化轨迹图片，稍后将在此图上绘制触摸轨迹
        if (trackBitmap == null) {
            trackBitmap = Bitmap.createBitmap(width - paddingRight - paddingLeft, height - paddingBottom - paddingTop, Bitmap.Config.ARGB_8888)
            trackBitmapCanvas = Canvas(trackBitmap!!)
            // 如果已经创建了但是宽高有变化就重新创建
        } else if (trackBitmap!!.width != width - paddingRight - paddingLeft || trackBitmap!!.height != height - paddingBottom - paddingTop) {
            trackBitmap?.recycle()
            trackBitmap = Bitmap.createBitmap(width - paddingRight - paddingLeft, height - paddingBottom - paddingTop, Bitmap.Config.ARGB_8888)
            trackBitmapCanvas?.setBitmap(trackBitmap)
        }

        // 初始化遮罩图片
        if (maskBitmap == null) {
            maskBitmap = Bitmap.createBitmap(width - paddingRight - paddingLeft, height - paddingBottom - paddingTop, Bitmap.Config.ARGB_8888)
            Canvas(maskBitmap!!).drawColor(defaultMaskColor)
        }

        // 初始化dst位置
        if (dstRect == null) {
            dstRect = Rect(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        } else {
            dstRect?.set(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        }

        srcRect = computeSrcRect(Point(maskBitmap!!.width, maskBitmap!!.height), Point(dstRect!!.width(), dstRect!!.height()), ImageView.ScaleType.CENTER_CROP)
    }

    private var downX: Float = 0f
    private var downY: Float = 0f
    private var moveX: Float = 0f
    private var moveY: Float = 0f
    private var mVelocityTracker: VelocityTracker? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mIsCompleted) {
            return false
        }
        //获取一个可以测试速率的工具
        mVelocityTracker = mVelocityTracker ?: VelocityTracker.obtain()
        mVelocityTracker?.addMovement(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mIsMove = false
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                downX = event.x
                downY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker?.computeCurrentVelocity(500)
                val xVelocity = abs(mVelocityTracker?.getXVelocity(0)?.toInt() ?: 0)
                val yVelocity = abs(mVelocityTracker?.getYVelocity(0)?.toInt() ?: 0)
                if (xVelocity == 0 && yVelocity == 0) {
                    return false
                }else if(xVelocity > 30 || yVelocity > 30){
                    //当X或者Y轴的移动速度超过了30速率，就算是他滑动了遮罩，这个30是我随便定的，可以看情况修改
                    mIsMove = true
                    moveX = event.x
                    moveY = event.y
                    trackBitmapCanvas?.drawLine(downX, downY, moveX, moveY, linePaint!!)
                    downX = moveX
                    downY = moveY
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!mIsMove) {
                    return false
                }
                if (isEnabled && isClickable) {
                    onViewListener?.onTouchUp()
                }
//                computeScratchArea()
            }
        }
        return true
    }

    /**
     * 设置遮罩图片
     * @param maskBitmap 遮罩图片
     */
    fun setMaskImage(maskBitmap: Bitmap) {
        this.maskBitmap = maskBitmap
        requestLayout()
    }

    /**
     * 设置遮罩图片
     * @param color 遮罩图片的颜色，稍后将使用此颜色创建一张图片
     */
    fun setMaskImage(color: Int) {
        this.defaultMaskColor = color
        if (maskBitmap != null) {
            if (!maskBitmap!!.isRecycled) {
                maskBitmap!!.recycle()
            }
            maskBitmap = null
        }
        requestLayout()
    }

    /**
     * 设置画笔的宽度
     *
     * @param strokeWidth 画笔的宽度
     */
    fun setStrokeWidth(strokeWidth: Int) {
        this.linePaint?.strokeWidth = strokeWidth.toFloat()
    }

    fun viewListener(listener: onViewStatusListener) {
        this.onViewListener = listener
    }

    interface onViewStatusListener {
        fun onTouchUp()
        fun onComplete()
    }

    private var mAlphaAnimator: ObjectAnimator? = null

    /**
     * 执行透明动画
     */
    fun performAnimation() {
        if (mAlphaAnimator?.isRunning == true) {
            return
        }
        this.mIsCompleted = true
        mAlphaAnimator = ObjectAnimator.ofFloat(this, ALPHA, 1f, 0f)
        mAlphaAnimator?.duration = 500L
        mAlphaAnimator?.interpolator = LinearInterpolator()
        mAlphaAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                this@ScratchMaskView.inVisible()
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        })
        mAlphaAnimator?.start()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mAlphaAnimator?.isRunning == true) {
            mAlphaAnimator?.cancel()
        }
        mAlphaAnimator?.removeAllListeners()
        mAlphaAnimator = null
        if (onViewListener != null) {
            onViewListener = null
        }
        if (trackBitmap != null && trackBitmap?.isRecycled == true) {
            trackBitmap?.recycle()
            trackBitmap = null
        }
        if (maskBitmap != null && maskBitmap?.isRecycled == true) {
            maskBitmap?.recycle()
            maskBitmap = null
        }
        //释放测速工具
        mVelocityTracker?.recycle()
        mVelocityTracker = null
//        mExecutorService?.shutdownNow()
    }

    /**
     * 开启线程执行计算
     */
    private fun computeScratchArea() {
        mExecutorService = mExecutorService ?: Executors.newSingleThreadExecutor()
        if (!mIsCompleted) {
            mExecutorService?.execute(runnable)
        } else {
            mExecutorService?.shutdownNow()
        }
    }

    /**
     * 开一个线程，计算遮罩层的透明区域，到达70%以下就算完成了
     */
    private val runnable = Runnable {
        try {
            if (mIsCompleted) {
                return@Runnable
            }
            if (maskBitmap == null) {
                return@Runnable
            }
            val bitmap = trackBitmap
            val w = width
            val h = height
            val pixels = IntArray(w * h)

            var wipeArea = 0f
            val totalArea = (w * h).toFloat()
            //获取像素数据
            bitmap?.getPixels(pixels, 0, w, 0, 0, w, h)
            //遍历色素值为0的像素，也就是透明区域
            for (i in 0 until w) {
                for (j in 0 until h) {
                    val index = i + j * w
                    if (pixels[index] == 0) {
                        wipeArea++
                    }
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                //因为这是一个百分百透明的区域，所以我们要获取把透明区域涂抹掉的比例
                //获取透明像素点的比例，小于70%就算完成
                val percent = (wipeArea * 100 / totalArea).toInt()
                logger.info(percent.toString())
                if (percent < 70 && !mIsCompleted) {
                    mIsCompleted = true
                    postInvalidate()
                    logger.info(".........$mIsCompleted")
                    if (onViewListener != null) {
                        post { onViewListener?.onComplete() }
                    }
                }
            }
        } catch (e: Exception) {
            //如果在子线程计算时，View被销毁了，做一次保护避免问题
//            reportCrash("遮罩计算透明度异常", e)
        }
    }
}