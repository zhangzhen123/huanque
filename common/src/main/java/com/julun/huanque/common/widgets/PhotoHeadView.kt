package com.julun.huanque.common.widgets

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.julun.huanque.common.R
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.loadImageLocal
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
     * [frameSize]相框大小
     *
     */
    fun setImage(headUrl: String, frameUrl: String, headSize: Int = 46, frameSize: Int = 66) {
        setImageCustom(
            headUrl = headUrl,
            frameUrl = frameUrl,
            headWidth = headSize,
            headHeight = headSize,
            frameWidth = frameSize,
            frameHeight = frameSize
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
     * 针对宽高相同的
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
            header_pic.loadImage(headUrl, dp2pxf(headWidth), dp2pxf(headWidth))
        } else if (headRes != null) {
            header_pic.loadImageLocal(headRes)
        }

        if (!frameUrl.isNullOrEmpty()) {
            header_photo_frame.loadImage(frameUrl, dp2pxf(headWidth), dp2pxf(headWidth))
        } else if (frameRes != null) {
            header_photo_frame.loadImageLocal(frameRes)
        }
    }


}
