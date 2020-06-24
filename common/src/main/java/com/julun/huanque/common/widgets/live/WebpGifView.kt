package com.julun.huanque.common.widgets.live

import android.content.Context
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Handler
import android.util.AttributeSet
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.fresco.animation.drawable.AnimatedDrawable2
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.julun.huanque.common.utils.DensityUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.fresco.LoopCountModifyingBackend

/**
 *
 * 可回调的webp gif 动图播放
 *@author zhangzhen
 *@data 2017/6/15
 *
 **/
open class WebpGifView(context: Context?, attrs: AttributeSet?) : SimpleDraweeView(context, attrs) {
    private val logger = ULog.getLogger("WebpGifView")

    private var mHandler: Handler? = null
    private var mRunnable: Runnable? = null
    private var callBack: GiftViewPlayCallBack? = null
    fun setCallBack(callBack: GiftViewPlayCallBack) {
        this.callBack = callBack
    }
    fun resetView(){
        controller?.animatable?.stop()
        mHandler?.removeCallbacks(mRunnable ?: return)
    }
    /**
     * uri 动图的地址
     * count 播放次数
     */
    fun setURI(uri: String, count: Int) {
        logger.info("当前的uri:$uri")
        mRunnable = mRunnable ?: Runnable {
            this.controller?.animatable?.stop()
//            logger.info("当前动画播放完成了")
            callBack?.onEnd()
        }
        val draweeController = Fresco.newDraweeControllerBuilder()
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onSubmit(id: String?, callerContext: Any?) {
                        logger.info("onSubmit")
                    }

                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        logger.info("onFinalImageSet")
                        if (animatable == null) return
//                        imageInfo?.let {
//                            val radio = it.height / it.width.toFloat()
//                            val viewRadio = this@WebpGifView.height / this@WebpGifView.width.toFloat()
//                            if (radio > viewRadio) {
//                                hierarchy.actualImageScaleType = ScalingUtils.ScaleType.CENTER_CROP
//                            } else {
//                                hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_END
//                            }
//                        }

//                        val ani: Animatable = animatable
//                        if (ani is AbstractAnimatedDrawable) {
//                            val dur = ani.duration
//                            logger.info("播放时间：" + dur)
//                            //设置播放次数
//                            val ct = if (count == 0) 1 else count
//                            val field = AbstractAnimatedDrawable::class.java.getDeclaredField("mLoopCount")
//                            field.isAccessible = true
//                            field.set(ani, ct)
//
//                            animatable.start()
//                            callBack?.onStart()
//
//                            mHandler = mHandler ?: Handler()
//                            if (duration != 0) {
//                                mHandler?.postDelayed(mRunnable, duration * 1000L)
//                            } else
//                                mHandler?.postDelayed(mRunnable, (dur * ct).toLong())
//                        }
                        if (animatable is AnimatedDrawable2) {
                            animatable.animationBackend = LoopCountModifyingBackend(animatable.animationBackend, count)//设置循环次数
                            val dur = animatable.loopDurationMs
                            logger.info("播放时间：" + dur)
                            animatable.start()
                            callBack?.onStart()
                            val ct = if (count == 0) 1 else count
                            mHandler = mHandler ?: Handler()
                            mHandler?.postDelayed(mRunnable, (dur * ct))
                        }
                    }

                    override fun onFailure(id: String?, throwable: Throwable?) {
                        logger.info("onFailure")
                        super.onFailure(id, throwable)
                        callBack?.onError()
                    }

                    override fun onRelease(id: String?) {
                        logger.info("onRelease")
                        callBack?.onRelease()
                    }
                }).setOldController(this.controller)
                .setUri(Uri.parse(uri))//设置uri
                .build()
        this.controller = draweeController

    }

    fun setURISpecial(uri: String, count: Int) {
        logger.info("当前的uri:$uri")
        mRunnable = mRunnable ?: Runnable {
            this.controller?.animatable?.stop()
//            logger.info("当前动画播放完成了")
            callBack?.onEnd()
        }
        val draweeController = Fresco.newDraweeControllerBuilder()
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onSubmit(id: String?, callerContext: Any?) {
                        logger.info("onSubmit")
                    }

                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        logger.info("onFinalImageSet")
                        if (animatable == null) return
                        imageInfo?.let {
                            val radio = it.height / it.width.toFloat()
                            val viewRadio = this@WebpGifView.height / this@WebpGifView.width.toFloat()
                            if (radio > viewRadio) {
                                hierarchy.actualImageScaleType = ScalingUtils.ScaleType.CENTER_CROP
                            } else {
                                hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_END
                            }
                        }

                        if (animatable is AnimatedDrawable2) {
                            animatable.animationBackend = LoopCountModifyingBackend(animatable.animationBackend, count)//设置循环次数
                            val dur = animatable.loopDurationMs
                            logger.info("播放时间：" + dur)
                            animatable.start()
                            callBack?.onStart()
                            val ct = if (count == 0) 1 else count
                            mHandler = mHandler ?: Handler()
                            mHandler?.postDelayed(mRunnable, (dur * ct))
                        }
                    }

                    override fun onFailure(id: String?, throwable: Throwable?) {
                        logger.info("onFailure")
                        super.onFailure(id, throwable)
                        callBack?.onError()
                    }

                    override fun onRelease(id: String?) {
                        logger.info("onRelease")
                        callBack?.onRelease()
                    }
                }).setOldController(this.controller)
                .setUri(Uri.parse(uri))//设置uri
                .build()
        this.controller = draweeController

    }

    /**
     * uri 动图的地址
     * count 播放次数
     * 可裁剪大小
     */
    fun setURIWithResize(uri: String, count: Int, width: Float, height: Float) {
        logger.info("setURIWithResize当前的uri:$uri")
        mRunnable = mRunnable ?: Runnable {
            this.controller?.animatable?.stop()
            logger.info("当前动画播放完成了")
            callBack?.onEnd()
        }
        val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                .setResizeOptions(ResizeOptions(DensityUtils.dp2px(width), DensityUtils.dp2px(height))
                ).build()
        val draweeController = Fresco.newDraweeControllerBuilder()
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onSubmit(id: String?, callerContext: Any?) {
                        logger.info("onSubmit")
                    }

                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        logger.info("onFinalImageSet")
                        if (animatable == null) return
//                        val ani: Animatable = animatable
//                        if (ani is AbstractAnimatedDrawable) {
//                            val dur = ani.duration
//                            logger.info("播放时间：" + dur)
//                            //设置播放次数
//                            val field = AbstractAnimatedDrawable::class.java.getDeclaredField("mLoopCount")
//                            field.isAccessible = true
//                            field.set(ani, count)
//
//                            animatable.start()
//                            callBack?.onStart()
//                            val ct = if (count == 0) 1 else count
//                            mHandler = mHandler ?: Handler()
//                            mHandler?.postDelayed(mRunnable, (dur * ct).toLong())
//                        }
                        if (animatable is AnimatedDrawable2) {
                            animatable.animationBackend = LoopCountModifyingBackend(animatable.animationBackend, count)//设置循环次数
                            val dur = animatable.loopDurationMs
                            logger.info("播放时间：" + dur)
                            animatable.start()
                            callBack?.onStart()
                            val ct = if (count == 0) 1 else count
                            mHandler = mHandler ?: Handler()
                            mHandler?.postDelayed(mRunnable, (dur * ct).toLong())
                        }

                    }

                    override fun onFailure(id: String?, throwable: Throwable?) {
                        logger.info("onFailure")
                        super.onFailure(id, throwable)
                        callBack?.onError()
                    }

                    override fun onRelease(id: String?) {
                        logger.info("onRelease")
                        callBack?.onRelease()
                    }
                }).setOldController(this.controller)
                .setImageRequest(request)
                .build()
        this.controller = draweeController

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler?.removeCallbacksAndMessages(null)
        mRunnable = null
        mHandler = null
    }

    interface GiftViewPlayCallBack {
        fun onStart()
        fun onError()
        fun onEnd()
        // 解决视图隐藏时不播动画的情况
        fun onRelease()
    }
}