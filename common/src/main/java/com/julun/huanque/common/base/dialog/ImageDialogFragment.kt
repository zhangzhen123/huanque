package com.julun.huanque.common.base.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import com.julun.huanque.common.IntentParamKey
import com.julun.huanque.common.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.DensityUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import kotlinx.android.synthetic.main.dialog_image.*
import org.jetbrains.anko.backgroundResource

/**
 * 一个展示图片的弹窗
 * @author WanZhiYuan
 */
class ImageDialogFragment : BaseDialogFragment() {

    private var mWidth: Float = 0f
    private var mHeight: Float = 0f

    override fun getLayoutId(): Int = R.layout.dialog_image

    override fun needEnterAnimation(): Boolean = false

    override fun onStart() {
        super.onStart()
        val window = dialog?.window ?: return
        val params = window.attributes
        params?.dimAmount = 0.2f
        params.gravity = Gravity.CENTER
        val width = if (mWidth != 0f) {
            DensityUtils.dp2px(mWidth)
        } else {
            DensityUtils.dp2px(275f)
        }
        val height = if (mHeight != 0f) {
            DensityUtils.dp2px(mHeight)
        } else {
            DensityUtils.dp2px(264f)
        }
        params.width = width.coerceAtMost(ScreenUtils.getScreenWidth())
        params.height = height.coerceAtMost(ScreenUtils.getScreenHeight())
        window.attributes = params
    }

    override fun initViews() {
        prepare()
        ivClose.onClickNew {
            dismiss()
        }
    }

    override fun reCoverView() {
        prepare()
    }

    private fun prepare() {
        mWidth = arguments?.getFloat(IntentParamKey.WIDTH.name) ?: 0f
        mHeight = arguments?.getFloat(IntentParamKey.HEIGHT.name) ?: 0f
        val pic = arguments?.getString(IntentParamKey.IMAGE.name) ?: ""
        val bg = arguments?.getInt(IntentParamKey.BG.name) ?: 0
        val title = arguments?.getString(IntentParamKey.TITLE.name) ?: ""
        if (!TextUtils.isEmpty(pic)) {
//            val params = sdvPicture.layoutParams as ConstraintLayout.LayoutParams
//            params.width = DensityUtils.dp2px(mWidth)
//            params.height = DensityUtils.dp2px(mHeight)
//            sdvPicture.layoutParams = params
//            ImageUtils.loadImage(sdvPicture, pic, mWidth,mHeight)
            ImageUtils.loadImageWithWidth(sdvPicture, pic, DensityUtils.dp2px(mWidth))
        }
        if (TextUtils.isEmpty(title)) {
            tvTitle.hide()
        } else {
            tvTitle.show()
            tvTitle.text = title
        }
        try {
            if (bg != 0) {
                svImageRootView.backgroundResource = bg
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setParams(width: Float, height: Float, pic: String, bgId: Int) {
        val bundle = arguments ?: Bundle()
        bundle?.putFloat(IntentParamKey.WIDTH.name, width)
        bundle?.putFloat(IntentParamKey.HEIGHT.name, height)
        bundle?.putString(IntentParamKey.IMAGE.name, pic)
        bundle?.putInt(IntentParamKey.BG.name, bgId)
        arguments = bundle
    }

    fun setParams(width: Float, height: Float, pic: String, bgId: Int, title: String) {
        setParams(width, height, pic, bgId)
        val bundle = arguments ?: Bundle()
        bundle?.putString(IntentParamKey.TITLE.name, title)
        arguments = bundle
    }

}