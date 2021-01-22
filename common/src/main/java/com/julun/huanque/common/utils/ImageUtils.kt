package com.julun.huanque.common.utils

import android.graphics.*
import android.graphics.drawable.Animatable
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.ViewGroup
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.fresco.animation.drawable.AnimatedDrawable2
import com.facebook.imagepipeline.common.ImageDecodeOptions
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.common.RotationOptions
import com.facebook.imagepipeline.core.ImagePipeline
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.julun.huanque.common.R
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.WebpAnimatorListener
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.fresco.BlurAndColorPostProcessor
import com.julun.huanque.common.utils.fresco.LoopCountModifyingBackend
import com.julun.huanque.common.utils.fresco.ColorPostprocessor
import com.julun.huanque.common.widgets.live.WebpGifView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.util.concurrent.TimeUnit


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/18 16:41
 *
 *@Description: ImageUtils
 *
 */
object ImageUtils {

    private val KEYS_POOL: MutableMap<String, String> = mutableMapOf()

    private val CACHE_KEY_PREFIX = "CUSTOM_IMG_CACHE_KEY"

    fun getChacheKey(url: String, smallSize: Boolean = true, requestBmp: ImageRequest): String {
        return "$CACHE_KEY_PREFIX$url"
    }

    fun getBitmapFromCache(): Bitmap? {
/*

        val closeableReference = CloseableReference.of<CloseableImage>(
                CloseableStaticBitmap(bitmap,
                        SimpleBitmapReleaser.getInstance(),
                        ImmutableQualityInfo.FULL_QUALITY, 0))
// 存入 Fresco
        Fresco.getImagePipelineFactory().bitmapMemoryCache.cache(getChacheKey("", requestBmp =), closeableReference)
*/

        return null
    }

    fun requestImageForBitmap(
        url: String,
        callback: (Bitmap?) -> Unit,
        drawableResId: Int /*= R.drawable.logo_placeholder*/,
        width: Int,
        height: Int
    ) {

        val builder = ImageRequestBuilder
            .newBuilderWithSource(Uri.parse(StringHelper.getOssImgUrl(url)))
            .setProgressiveRenderingEnabled(true)
        if (width != 0 && height != 0) {
            builder.resizeOptions = ResizeOptions(width, height)
        }
        val imageRequest = builder.build()

        val imagePipeline: ImagePipeline = Fresco.getImagePipeline()
        val dataSource: DataSource<CloseableReference<CloseableImage>> =
            imagePipeline.fetchDecodedImage(imageRequest, null)

        dataSource.subscribe(object : BaseBitmapDataSubscriber() {


            /**
             * @param bitmap 关于这个 bitmap对象,将会在方法调用完毕之后被销毁,由框架负责这个事情.如下面api说明:
             * The framework will free the bitmap's memory after this method has completed.
             */
            override fun onNewResultImpl(bitmap: Bitmap?) {
                if (bitmap != null) {
                    callback(bitmap)
                } else {
                    if (drawableResId > 0) {
                        callback(
                            BitmapFactory.decodeResource(
                                CommonInit.getInstance().getApp().resources, drawableResId
                            )
                        )
                    } else {
                        callback(null)
                    }
                }
            }

            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                if (drawableResId > 0) {
                    callback(
                        BitmapFactory.decodeResource(
                            CommonInit.getInstance().getApp().resources, drawableResId
                        )
                    )
                } else {
                    callback(null)
                }
            }

        }, CallerThreadExecutor.getInstance())
    }

    fun requestImageForBitmap(
        url: String,
        callback: (Bitmap?) -> Unit,
        drawableResId: Int = R.mipmap.logo_placeholder
    ) {
        requestImageForBitmap(url, callback, drawableResId, 0, 0)
    }

    /**
     * 给imageView设置网络图片
     * @param imgView
     * @param url 路径
     */
    fun loadImage(imgView: SimpleDraweeView, url: String, width: Float = 50f, height: Float = 50f) {
        loadImageInPx(imgView, url, DensityHelper.dp2px(width), DensityHelper.dp2px(height))
    }

    /**
     * 加载本地图片文件
     */
    fun loadNativeFilePath(imgView: SimpleDraweeView, filePath: String, width: Float = 50f, height: Float = 50f) {
        if (TextUtils.isEmpty(filePath)) {
            return
        }
        val prefix = "file://"
        val uri = if (filePath.startsWith(prefix)) {
            Uri.parse(filePath)
        } else {
            Uri.parse("$prefix$filePath")
        }
        val request = ImageRequestBuilder.newBuilderWithSource(uri)
            .setResizeOptions(
                ResizeOptions(DensityHelper.dp2px(width), DensityHelper.dp2px(height))
            ).build()
        val controller = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setAutoPlayAnimations(true)
            .setOldController(imgView.controller)
//                .setControllerListener(BaseControllerListener<ImageInfo>())
            .build()
        imgView.controller = controller
//        imgView.setImageURI(Uri.parse("file://$filePath"))
    }

    /**
     * 给imageView设置网络图片
     * @param imgView
     * @param url 路径
     */
    fun loadImageInPx(imgView: SimpleDraweeView, url: String, width: Int, height: Int) {
        var uriString = StringHelper.getOssImgUrl(url)
        val uri = if (uriString != null) Uri.parse(uriString) else null
        val request = ImageRequestBuilder.newBuilderWithSource(uri)
            .setResizeOptions(ResizeOptions(width, height))
            //渐进式渲染表示的是当我们加载一张图片的时候，如果网络比较缓慢，那么图片会从模糊到清晰渐渐呈现，这被称之为渐进式JPEG
            .setProgressiveRenderingEnabled(true)
            .build()

        val controller = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setAutoPlayAnimations(true)
            .setOldController(imgView.controller)
//                .setControllerListener(BaseControllerListener<ImageInfo>())
            .build()
        imgView.controller = controller
//        imgView.setImageURI(StringHelper.getOssImgUrl(url))
    }

    /**
     * 使用fresco
     * 给imageView设置网络图片  不裁剪
     * @param SimpleDraweeView
     * @param url 路径
     */
    fun loadImageNoResize(imgView: SimpleDraweeView, url: String) {

        val controller = Fresco.newDraweeControllerBuilder()
            .setUri(Uri.parse(StringHelper.getOssImgUrl(url)))
            .setAutoPlayAnimations(true)
            .setOldController(imgView.controller)
//                .setControllerListener(BaseControllerListener<ImageInfo>())
            .build()

        imgView.controller = controller

    }

    /**
     * 使用fresco
     * 给imageView设置网络图片  带模糊效果
     * @param imgView
     * @param url 路径
     * @param iterations 迭代次数，越大越模糊。
     * @param blurRadius 模糊图半径，必须大于0，越大越模糊。
     * @param colors 图片处理时着色器需要的颜色集合
     */
    fun loadImageWithBlur(
        imgView: SimpleDraweeView,
        url: String,
        iterations: Int,
        blurRadius: Int,
        width: Int = 0,
        height: Int = 0,
        colors: IntArray? = null
    ) {

        val builder = ImageRequestBuilder
            .newBuilderWithSource(Uri.parse(StringHelper.getOssImgUrl(url)))
            .setProgressiveRenderingEnabled(true)
            .setPostprocessor(BlurAndColorPostProcessor(iterations, blurRadius, colors))
        if (width != 0 && height != 0) {
            builder.resizeOptions = ResizeOptions(width, height)
        }

        val imageRequest = builder.build()

        val controller = Fresco.newDraweeControllerBuilder()
            .setImageRequest(imageRequest)
//            .setAutoPlayAnimations(true)
            .setOldController(imgView.controller)
            .build()
        imgView.controller = controller

    }

    /**
     * 设置加载的图片填充指定颜色
     */
    fun loadImageWithShadow(
        imgView: SimpleDraweeView,
        url: String,
        width: Int = 0,
        height: Int = 0, colors: IntArray? = null
    ) {

        val builder = ImageRequestBuilder
            .newBuilderWithSource(Uri.parse(StringHelper.getOssImgUrl(url)))
            .setProgressiveRenderingEnabled(true)
            .setPostprocessor(ColorPostprocessor(colors))
        if (width != 0 && height != 0) {
            builder.resizeOptions = ResizeOptions(DensityHelper.dp2px(width), DensityHelper.dp2px(height))
        }

        val imageRequest = builder.build()

        val controller = Fresco.newDraweeControllerBuilder()
            .setImageRequest(imageRequest)
            .setAutoPlayAnimations(true)
            .setOldController(imgView.controller)
            .build()
        imgView.controller = controller

    }

    /**
     * 使用fresco
     * 给imageView设置本地图片
     * @param SimpleDraweeView
     * @param placeHolderResId 资源id
     */
    fun loadImageLocal(imgView: SimpleDraweeView, placeHolderResId: Int) {
//        imgView.setImageURI(Uri.parse("res://${huanqueInit.getInstance().getContext().packageName}/$placeHolderResId"))
        imgView.setImageURI(
            "res://${CommonInit.getInstance().getApp().packageName}/$placeHolderResId"
        )
    }

    fun loadNativeFilePathWithPx(
        imgView: SimpleDraweeView,
        filePath: String,
        width: Int,
        height: Int
    ) {
        if (!TextUtils.isEmpty(filePath)) {
            val uri = Uri.parse("file://$filePath")
            val build = ImageRequestBuilder.newBuilderWithSource(uri)

            if (width > 0 && height > 0) {
                build.resizeOptions = ResizeOptions(width, height)
            }
            val request = build.build()
            val controller = Fresco.newDraweeControllerBuilder().setImageRequest(request)
                .setAutoPlayAnimations(true).setOldController(imgView.controller).build()
            imgView.controller = controller
        }
    }

    /**
     * 图片置灰
     */
    fun putImageAsh(sdv: SimpleDraweeView) {
        sdv.hierarchy?.apply {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val filter = ColorMatrixColorFilter(matrix)
            setActualImageColorFilter(filter)
        }
    }

    /**
     * 加载本地Gif图片
     */
    fun loadGifImageLocal(imgView: SimpleDraweeView, placeHolderResId: Int) {
        var draweeController = Fresco.newDraweeControllerBuilder()
            .setAutoPlayAnimations(true)
            .setUri(
                Uri.parse(
                    "res://${CommonInit.getInstance().getApp().packageName}/$placeHolderResId"
                )
            )//设置uri
            .setOldController(imgView.controller)
            .build()
        imgView.controller = draweeController

    }

    /**
     * 加载本地Webp或Gif图片 带裁剪
     */
    fun loadWebpImageLocalWithResize(
        imgView: WebpGifView,
        placeHolderResId: Int,
        count: Int,
        width: Float,
        height: Float
    ) {
        var uri = "res://${CommonInit.getInstance().getApp().packageName}/$placeHolderResId"
        imgView.setURIWithResize(uri, count, width, height)
    }

    /**
     * 加载本地Webp或Gif图片
     */
    fun loadWebpImageLocal(imgView: WebpGifView, placeHolderResId: Int, count: Int = 1) {
        var uri = "res://${CommonInit.getInstance().getApp().packageName}/$placeHolderResId"
        imgView.setURI(uri, count)
    }

    /**
     * 通过给定的宽度，自动适应高度
     * * @param simpleDraweeView view
     * * @param url  Uri
     * * @param imageWidth 宽度
     */
    fun loadImageWithWidth(simpleDraweeView: SimpleDraweeView, url: String, imageWidth: Int) {
        val layoutParams = simpleDraweeView.layoutParams
        layoutParams.width = imageWidth
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        val controllerListener = object : BaseControllerListener<ImageInfo>() {
            override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, anim: Animatable?) {
                if (imageInfo == null) {
                    return
                }
                val height = imageInfo.height
                val width = imageInfo.width
                simpleDraweeView.aspectRatio = width / height.toFloat()
            }

            override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                Log.d("TAG", "Intermediate image received")
            }

            override fun onFailure(id: String?, throwable: Throwable?) {
                throwable!!.printStackTrace()
            }
        }

        val controller =
            Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener)
                .setUri(Uri.parse(StringHelper.getOssImgUrl(url))).build()
        simpleDraweeView.controller = controller
    }

    /**
     * 通过给定的高度，自动适应宽度
     * * @param simpleDraweeView view
     * * @param url  Uri
     * * @param imageHeight 高度
     */
    @Deprecated("simpleDraweeView.aspectRatio 某些状态下不起作用  用loadImageWithHeight_2替代")
    fun loadImageWithHeight(simpleDraweeView: SimpleDraweeView, url: String, imageHeight: Int) {
        val layoutParams = simpleDraweeView.layoutParams

        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.height = imageHeight
        val controllerListener = object : BaseControllerListener<ImageInfo>() {
            override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, anim: Animatable?) {
                if (imageInfo == null) {
                    return
                }
                val height = imageInfo.height
                val width = imageInfo.width

                simpleDraweeView.aspectRatio = width / height.toFloat()
            }

            override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                Log.d("TAG", "Intermediate image received")
            }

            override fun onFailure(id: String?, throwable: Throwable?) {
                throwable!!.printStackTrace()
            }
        }

        val controller =
            Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener)
                .setUri(Uri.parse(StringHelper.getOssImgUrl(url))).build()
        simpleDraweeView.controller = controller
    }

    /**
     * 通过给定的高度，自动适应宽度 通过修改布局参数实现
     * * @param simpleDraweeView view
     * * @param url  Uri
     * * @param imageHeight 高度
     */
    fun loadImageWithHeight_2(simpleDraweeView: SimpleDraweeView, url: String, imageHeight: Int) {
        val layoutParams = simpleDraweeView.layoutParams

        layoutParams.height = imageHeight
        val controllerListener = object : BaseControllerListener<ImageInfo>() {
            override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, anim: Animatable?) {
                if (imageInfo == null) {
                    return
                }
                val height = imageInfo.height
                val width = imageInfo.width
                layoutParams.width = ((imageHeight * width).toFloat() / height.toFloat()).toInt()
                simpleDraweeView.layoutParams = layoutParams
            }

            override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                Log.d("TAG", "Intermediate image received")
            }

            override fun onFailure(id: String?, throwable: Throwable?) {
                throwable!!.printStackTrace()
            }
        }

        val controller =
            Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener)
                .setAutoPlayAnimations(true).setUri(Uri.parse(StringHelper.getOssImgUrl(url)))
                .build()
        simpleDraweeView.controller = controller
    }

    /**
     * 通过给定的宽度，自动适应高度
     * * @param simpleDraweeView view
     * * @param url  Uri
     * [controllerListener]回调的监听器
     */
    fun loadImageWithListener(
        simpleDraweeView: SimpleDraweeView,
        url: String,
        controllerListener: BaseControllerListener<ImageInfo>
    ) {
        val controller =
            Fresco.newDraweeControllerBuilder().setControllerListener(controllerListener)
                .setUri(Uri.parse(StringHelper.getOssImgUrl(url))).setAutoPlayAnimations(true)
                .setOldController(simpleDraweeView.controller).build()
        simpleDraweeView.controller = controller
    }

    fun loadRemoteImageInpx(imgView: SimpleDraweeView, url: Uri, width: Int, height: Int) {
        //        Uri uri = Uri.parse(url);
        val build = ImageRequestBuilder.newBuilderWithSource(url)

        if (width > 0 && height > 0) {
            build.resizeOptions = ResizeOptions(width, height)
        }
        val request = build.build()
        val controller =
            Fresco.newDraweeControllerBuilder().setImageRequest(request).setAutoPlayAnimations(true)
                .setOldController(imgView.controller).build()
        imgView.controller = controller
    }

    /**
     * 显示动图
     */
    fun showAnimator(
        sdv: SimpleDraweeView,
        file: File,
        count: Int,
        duration: Int = 0,
        readyCallBack: ((Boolean) -> Unit)? = null
    ) {
        if (!file.exists()) {
            if (readyCallBack != null) {
                readyCallBack(false)
            }
            return
        }
        val uri = "file:///${file.absolutePath}"
        val draweeController = Fresco.newDraweeControllerBuilder()
            .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                override fun onSubmit(id: String?, callerContext: Any?) {
                }

                override fun onFinalImageSet(
                    id: String?,
                    imageInfo: ImageInfo?,
                    animatable: Animatable?
                ) {
                    if (animatable == null) return
//                        val ani: Animatable = animatable
//                        if (ani is AbstractAnimatedDrawable) {
//                            //设置播放次数
//                            val field = AbstractAnimatedDrawable::class.java.getDeclaredField("mLoopCount")
//                            field.isAccessible = true
//                            field.set(ani, count)
//
//                            animatable.start()
//                            if (readyCallBack != null) {
//                                readyCallBack(true)
//                            }
//                        }
                    if (animatable is AnimatedDrawable2) {
                        animatable.animationBackend =
                            LoopCountModifyingBackend(animatable.animationBackend, count)//设置循环次数
                    }
                    animatable.start()
                    if (readyCallBack != null) {
                        readyCallBack(true)
                    }
                }

                override fun onFailure(id: String?, throwable: Throwable?) {
                    super.onFailure(id, throwable)
                }

                override fun onRelease(id: String?) {
                }
            }).setOldController(sdv.controller)
            .setUri(Uri.parse(uri))//设置uri
            .build()
        sdv.controller = draweeController

    }

    /**
     * 显示远程动画
     */
    fun showAnimator(
        sdv: SimpleDraweeView,
        uri: String,
        count: Int = 1,
        animatorListener: WebpAnimatorListener? = null
    ) {
        val tempUri = if (uri.startsWith("res://") || uri.startsWith("asset://")) {
            //本地资源
            uri
        } else {
            StringHelper.getOssImgUrl(uri)
        }
        val draweeController = Fresco.newDraweeControllerBuilder()
            .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                override fun onSubmit(id: String?, callerContext: Any?) {
                }

                override fun onFinalImageSet(
                    id: String?,
                    imageInfo: ImageInfo?,
                    animatable: Animatable?
                ) {
                    if (animatable is AnimatedDrawable2) {

                        //设置循环次数
                        animatable.animationBackend =
                            LoopCountModifyingBackend(animatable.animationBackend, count)
                        logger("Private  animtion dur = ${animatable.loopDurationMs * count}")
                        animatorListener?.let { l ->
                            l.onStart()
                            val dur = animatable.loopDurationMs * count
                            Observable.timer(dur, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ l.onEnd() }, { it.printStackTrace() })
                        }
                        animatable.start()
                    }
                }

                override fun onFailure(id: String?, throwable: Throwable?) {
                    super.onFailure(id, throwable)
                    throwable?.printStackTrace()
                    animatorListener?.onError()
                }

                override fun onRelease(id: String?) {
                }
            }).setOldController(sdv.controller)
            .setUri(Uri.parse(tempUri))//设置uri
            .build()
        sdv.controller = draweeController
    }


    /**
     * 使用动画的最后一帧作为预览图
     */
    fun showAnimatorFinalFrame(sdv: SimpleDraweeView, uri: String, width: Int, height: Int) {
        ULog.i("DXCImage uri = $uri,width = $width,height=$height")
        val tempUri = if (uri.startsWith("res://") || uri.startsWith("asset://")) {
            //本地资源
            uri
        } else {
            StringHelper.getOssImgUrl(uri)
        }
        val requestOptions = ImageDecodeOptions.newBuilder()
            .setForceStaticImage(true)
            .setDecodePreviewFrame(true).setUseLastFrameForPreview(true).build()
        val imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(tempUri))
            .setResizeOptions(ResizeOptions(width, height))
            .setRotationOptions(RotationOptions.autoRotateAtRenderTime())
            .setImageDecodeOptions(requestOptions)
            .build()
        val draweeController = Fresco.newDraweeControllerBuilder()
            .setImageRequest(imageRequest)
            .setOldController(sdv.controller)
//                .setUri(Uri.parse(tempUri))//设置uri
            .build()
        sdv.controller = draweeController
    }


    /**
     * @see demo
     * 图文混排工具
     * @param arrayList 传入对应混排list,使用方法 -> [demo]
     * @param space 图标和之前内容之间的间距  默认 " "(一个空格)
     */
    fun renderTextAndImage(arrayList: ArrayList<TIBean>, space: String = " "): BaseTextBean? {
        if (arrayList.isEmpty()) {
            return null
        }
        try {
            var textIndex = 0
            var text = ""
            val textBean = BaseTextBean()
            arrayList.forEachIndexed { index, bean ->
                if (index == 0) {
                    when (bean.type) {
                        TIBean.TEXT -> {
                            if (TextUtils.isEmpty(bean.text)) {
                                return@forEachIndexed
                            }
                            val testParam = copyImageParams(TextParam(), bean) as TextParam
                            testParam.indexStart = textIndex
                            text = bean.text
                            textIndex += bean.text.length - 1
                            textBean.textParams.add(testParam)
                        }
                        else -> {
                            val imgParam = copyImageParams(ImageParam(), bean) as ImageParam
                            imgParam.index = textIndex
                            text = "#"
                            textIndex += 0
                            textBean.imgParams.add(imgParam)
                        }
                    }
                } else {
                    when (bean.type) {
                        TIBean.TEXT -> {
                            if (TextUtils.isEmpty(bean.text)) {
                                return@forEachIndexed
                            }
                            val testParam = copyImageParams(TextParam(), bean) as TextParam
                            testParam.indexStart = textIndex + 1
                            text += bean.text
                            textBean.textParams.add(testParam)
                            textIndex += bean.text.length
                        }
                        else -> {
                            val imgParam = copyImageParams(ImageParam(), bean) as ImageParam
                            val imageSpaceStr = "${space}#"
                            textIndex += imageSpaceStr.length
                            text += imageSpaceStr
                            imgParam.index = textIndex
                            textBean.imgParams.add(imgParam)
                        }
                    }
                }
            }
            textBean.realText = text
            return textBean
        } catch (e: Exception) {
            reportCrash("renderTextAndImage解析异常", e)
            return null
        }
    }

    private fun copyImageParams(params: BaseParams, bean: TIBean): BaseParams {
        if (params is TextParam) {
            params.textColor = bean.textColor
            params.textColorInt = bean.textColorInt
            params.textSize = bean.textSize
            params.text = bean.text
            params.styleSpan = bean.styleSpan
        } else if (params is ImageParam) {
            params.imgRes = bean.imgRes
            params.url = bean.url
            params.height = bean.height
            params.width = bean.width
            params.isCircle = bean.isCircle
            params.borderRedId = bean.borderRedId
            params.borderWidth = bean.borderWidth
        }
        return params
    }

}