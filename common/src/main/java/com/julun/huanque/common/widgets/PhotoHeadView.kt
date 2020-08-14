package com.julun.huanque.common.widgets

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.julun.huanque.common.R
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import kotlinx.android.synthetic.main.view_photo_head.view.*


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/7 16:07
 *
 *@Description: PhotoHeadView 带相框的头像 单纯只处理头像边框 不处理其他属性
 *
 */
class PhotoHeadView : FrameLayout {


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_photo_head, this)
    }

    /**
     * 针对宽高相同的
     * [headUrl]头像地址
     * [frameUrl]相框地址
     * [headSize]头像大小
     * [frameWidth]相框大小
     *
     */
    fun setImage(
        headUrl: String, frameUrl: String? = null, headSize: Int = 46, frameWidth: Int = 58,
        frameHeight: Int = 74
    ) {
        setImageCustom(
            headUrl = headUrl,
            frameUrl = frameUrl,
            headWidth = headSize,
            headHeight = headSize,
            frameWidth = frameWidth,
            frameHeight = frameHeight
        )

    }

    /**
     * 针对宽高不相同的
     * [headUrl]头像地址
     * [frameUrl]相框地址
     *
     */
    fun setImageSymmetry(
        headUrl: String,
        frameUrl: String,
        headWidth: Int = 46,
        headHeight: Int = 46,
        frameWidth: Int = 58,
        frameHeight: Int = 74
    ) {
        setImageCustom(
            headUrl = headUrl,
            frameUrl = frameUrl,
            headWidth = headWidth,
            headHeight = headHeight,
            frameWidth = frameWidth,
            frameHeight = frameHeight
        )
    }

    /**
     *
     * [headUrl]头像地址
     * [frameUrl]相框地址
     *
     */
    fun setImageCustom(
        headUrl: String? = null, headRes: Int? = null, frameUrl: String? = null, frameRes: Int? = null, headWidth: Int = 46,
        headHeight: Int = 46,
        frameWidth: Int = 58,
        frameHeight: Int = 74
    ) {
        val headLp = header_pic.layoutParams as FrameLayout.LayoutParams
        headLp.width = dp2px(headWidth)
        headLp.height = dp2px(headHeight)

        val frameLp = header_photo_frame.layoutParams as FrameLayout.LayoutParams
        frameLp.width = dp2px(frameWidth)
        frameLp.height = dp2px(frameHeight)
        requestLayout()
        if (!headUrl.isNullOrEmpty()) {
            header_pic.loadImage(headUrl, dp2pxf(headWidth), dp2pxf(headHeight))
        } else if (headRes != null) {
            header_pic.loadImageLocal(headRes)
        }

        if (!frameUrl.isNullOrEmpty()) {
            header_photo_frame.show()
            header_photo_frame.loadImage(frameUrl, dp2pxf(frameWidth), dp2pxf(frameHeight))
        } else if (frameRes != null) {
            header_photo_frame.show()
            header_photo_frame.loadImageLocal(frameRes)
        } else {
            header_photo_frame.hide()
        }
    }

    /**
     *
     * [headUrl]头像地址
     * [frameUrl]相框地址
     * 针对根据一边大小固定 另一半自适应的情况的方法
     *
     */
    fun setImageCustomByOneFrameSide(
        headUrl: String? = null, headRes: Int? = null, frameUrl: String? = null, headSize: Int = 46,
        frameWidth: Int = LayoutParams.WRAP_CONTENT,
        frameHeight: Int = LayoutParams.WRAP_CONTENT
    ) {
        if (frameWidth == LayoutParams.WRAP_CONTENT && frameHeight == LayoutParams.WRAP_CONTENT) {
            throw Exception("请至少设置一个固定的边大小")
        }

        val headLp = header_pic.layoutParams as FrameLayout.LayoutParams
        headLp.width = dp2px(headSize)
        headLp.height = dp2px(headSize)

        val frameLp = header_photo_frame.layoutParams as FrameLayout.LayoutParams
        if (frameWidth > 0) {
            frameLp.width = dp2px(frameWidth)
        }
        if (frameHeight > 0) {
            frameLp.height = dp2px(frameHeight)
        }

        requestLayout()
        if (!headUrl.isNullOrEmpty()) {
            header_pic.loadImage(headUrl, dp2pxf(headSize), dp2pxf(headSize))
        } else if (headRes != null) {
            header_pic.loadImageLocal(headRes)
        }

        if (!frameUrl.isNullOrEmpty()) {
            header_photo_frame.show()
            if (frameWidth == LayoutParams.WRAP_CONTENT) {
                ImageUtils.loadImageWithHeight_2(header_photo_frame, frameUrl, dp2px(frameHeight))
            }

            if (frameHeight == LayoutParams.WRAP_CONTENT) {
                ImageUtils.loadImageWithWidth(header_photo_frame, frameUrl, dp2px(frameWidth))
            }

        } else {
            header_photo_frame.hide()
        }
    }

}
